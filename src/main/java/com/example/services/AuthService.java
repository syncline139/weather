package com.example.services;

import com.example.dao.AuthDao;
import com.example.models.Sessions;
import com.example.models.Users;
import com.example.utils.PasswordUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class AuthService {

    public static final int TIME_LIVE_SESSION = 86400;
    public static final String SESSION = "SESSIONID";
    private final AuthDao authDao;

    // нужен для того что бы пока приложение останавливается, операция не выполнялась
    private volatile boolean isShuttingDown = false;

    public void create(Users user) {
            String hashedPassword = PasswordUtil.hashPassword(user.getPassword());
            user.setPassword(hashedPassword);
            authDao.saveUser(user);
    }

    public void createSession(Users user, HttpServletResponse response, HttpServletRequest request) {
        Sessions sessions = new Sessions();
        sessions.setUser(user);
        sessions.setExpiresAt(LocalDateTime.now().plusDays(1));
        authDao.saveSession(sessions);
        String token = sessions.getId().toString();

        Cookie cookie = new Cookie(SESSION, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(TIME_LIVE_SESSION);
        response.addCookie(cookie);
        request.getSession().setAttribute("login", user.getLogin());
        request.getSession().setAttribute("id", user.getId());
    }

    public void exit(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (SESSION.equals(c.getName())) {
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
                            Cookie cookie = new Cookie(SESSION, null);
                            cookie.setPath("/");
                            cookie.setMaxAge(0);
                            response.addCookie(cookie);
                            request.getSession().invalidate();
                        } else {
                            throw new IllegalArgumentException("Сессия не должна быть null");
                        }
                    } else {
                        throw new IllegalArgumentException("UUID не найдено в БД");
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("куки не должны быть null");
        }
    }


    @Scheduled(fixedRate = 3600 * 1000) // каждый час
    public void sessionClear() {
        if (!isShuttingDown) {
            authDao.removeAllExpiresatElseOverdueTime();
        }
    }

    public void setShuttingDown(boolean shuttingDown) {
        this.isShuttingDown = shuttingDown; // Устанавливаем флаг
    }

    @Scheduled(fixedRate = 3600 * 1000)
    public void extendSessions() {
        List<Sessions> sessionsList = authDao.findAllSession();
        LocalDateTime now = LocalDateTime.now();
        long fifteenMinutesMillis = 180 * 60 * 1000L;

        for (Sessions session : sessionsList) {
            long remainingMillis = Duration.between(now, session.getExpiresAt()).toMillis();
            if (remainingMillis < fifteenMinutesMillis) {
                session.setExpiresAt(now.plusDays(1));
                authDao.saveSession(session);
            }
        }
    }

    public boolean uniqueLogin(String login) {
        return authDao.uniqueLogin(login);
    }

    public Users findByLogin(String login) {
       return authDao.findByLogin(login);
    }
}


