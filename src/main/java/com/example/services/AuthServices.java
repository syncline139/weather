package com.example.services;

import com.example.dao.AuthDao;
import com.example.models.Sessions;
import com.example.models.Users;
import com.example.util.PasswordUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * В данном классе содержиться бизнес-логика относящиеся к аунтентификации
 */
@Service
@EnableScheduling
@RequiredArgsConstructor
public class AuthServices {

    private final AuthDao authDao;


    /**
     * Проверяем схожеться паролей и уникальность логина, а так же полсле все проверок хешируем пароль
     *
     * @param user передаем человека которого мы сохраним если пройдем провреки
     */
    public void save(Users user) {

        String login = user.getLogin();

        if (user.getPassword() != null &&
            user.getPassword().equals(user.getConfirmPassword()) &&
            authDao.uniqueLogin(login)) {

            String hashedPassword = PasswordUtil.hashPassword(user.getPassword());
            user.setPassword(hashedPassword);

            authDao.saveUser(user);
        }
    }

    /**
     * Отвечает за создание создание сесии для юзера который отправляеться в БД
     * Далее создаеться токен который извлекаеться из UUID в виде строки и называеться он 'SESSIONID'
     * Затем мы его добавляем в объект ответа который отправляеться клиенту
     *
     * @param user     привязываем переданного юзера к сессии
     * @param response добаляем куки с индификатором сесии
     */

    public void createSession(Users user, HttpServletResponse response) {

        Sessions sessions = new Sessions();
        sessions.setUser(user);
        sessions.setExpiresAt(LocalDateTime.now().plusDays(1));

        authDao.saveSession(sessions);

        String token = sessions.getId().toString();


        Cookie cookie = new Cookie("SESSIONID", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(86400); // 24 часа
        response.addCookie(cookie);


    }

    /**
     * Ищем сессию в Бд и удаляем ее и так же анулируем сессиию у юзера посылая ему новые куки
     *
     * @param request  принимаем куки активного пользователя
     * @param response посылаем новые куки пользотваля
     */
    public void exit(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("SESSIONID".equals(c.getName())) { // SESSIONID -> название наших куков
                    String sessionIdStr = c.getValue();
                    if (authDao.findByUUID(sessionIdStr)) {
                        UUID uuid;
                        try {
                            uuid = UUID.fromString(sessionIdStr);
                        } catch (IllegalArgumentException e) {
                            continue;
                        }
                        // удаляем сессию из Бд и удаляем куки у пользователя
                        Sessions session = authDao.findSessionByUUID(uuid);
                        if (session != null) {
                            authDao.deleteSession(session);
                            Cookie cookie = new Cookie("SESSIONID", null);
                            cookie.setPath("/");
                            cookie.setMaxAge(0);
                            response.addCookie(cookie);
                        }
                    }
                }
            }
        }
    }


    /**
     * Каждый час выполняется проверка на просроченные UUID
     */
    @Scheduled(fixedRate = 3600 * 1000) // каждый час
    public void sessionClear() {
        authDao.findAllExpiresat();
    }

    /**
     * Метод регулярно (каждые 22 часа) проверяет все активные сессии.
     * Если до истечения сессии осталось менее 3 часов, то считается
     * что пользователь был активен недавно и сессия продлевается на один день от текущего момента
     */
    @Scheduled(fixedRate = 4752000 * 1000) // каждые 22 часа
    public void extendSessions() {

        List<Sessions> sessionsList = authDao.findAllSession();
        LocalDateTime now = LocalDateTime.now();
        long fifteenMinutesMillis = 180 * 60 * 1000L; // 3 часа

        for (Sessions session : sessionsList) {
            long remainingMillis = Duration.between(now, session.getExpiresAt()).toMillis();
            if (remainingMillis < fifteenMinutesMillis) {
                // Продлеваем сессию на 1 день от текущего момента
                session.setExpiresAt(now.plusDays(1));
                authDao.saveSession(session);
            }
        }
    }


}


