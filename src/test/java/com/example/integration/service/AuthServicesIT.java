package com.example.integration.service;

import com.example.dao.AuthDao;
import com.example.integration.annotation.IT;
import com.example.models.Sessions;
import com.example.models.Users;
import com.example.services.AuthServices;
import com.example.util.PasswordUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@IT
@Tag("AuthServicesTest")
@Transactional
public class AuthServicesIT {

    public static final String LOGIN = "dispersion";
    public static final String PASSWORD = "sdfsfsdfsd";
    private AuthServices authServices;
    private AuthDao authDao;

    @Autowired
    public AuthServicesIT(AuthServices authServices, AuthDao authDao) {
        this.authDao = authDao;
        this.authServices = authServices;
    }

    @Test
    @Tag("save")
    void shouldSaveUserWithValidData() {
        // Подготовка данных
        Users user = new Users();
        user.setLogin(LOGIN);
        user.setPassword(PASSWORD);
        user.setConfirmPassword(PASSWORD); // Пароли совпадают

        // Выполнение действия
        authServices.save(user);

        // Проверки
        assertThat(user.getPassword()).isNotEqualTo(PASSWORD); // Пароль изменился (захеширован)
        assertThat(user.getPassword()).hasSize(60);   // Длина хеша соответствует ожидаемой (для BCrypt)
    }


    @Test
    @Tag("createSession")
    void shouldCreateSessionForValidUser() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        Users user = new Users();
        user.setLogin(LOGIN);
        user.setPassword(PASSWORD);
        user.setConfirmPassword(PASSWORD);

        authServices.createSession(user, response);

        Cookie[] cookies = response.getCookies();

        assertThat(cookies[0].getName()).isEqualTo("SESSIONID");
        assertThat(cookies[0].getMaxAge()).isEqualTo(86400);
        assertThat(cookies[0].getPath()).isEqualTo("/");
    }

    @Test
    @Tag("createSession")
    void shouldThrowExceptionWhenUserIsNullInCreateSession() {
        MockHttpServletResponse response = new MockHttpServletResponse();


        assertThatThrownBy(() -> authServices.createSession(null, response))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("user не может быть null ( AuthServices/createSession");
    }

    @Test
    @Tag("exit")
    void shouldThrowExceptionWhenCookiesAreNullInExit() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();


        assertThatThrownBy(() -> authServices.exit(request, response))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("куки не должны быть null AuthServices/exit");
    }


    @Test
    @Tag("exit")
    void shouldThrowExceptionWhenUUIDNotFoundInExit() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();


        request.setCookies(new Cookie("SESSIONID", ""));

        assertThatThrownBy(() -> authServices.exit(request, response))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UUID не найдено в БД AuthServices/exit");
    }

    @Test
    @Tag("exit")
    void shouldHandleInvalidUUIDInExit() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setCookies(new Cookie("SESSIONID", "invalid-uuid-string"));


        assertThatThrownBy(() -> authServices.exit(request, response))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UUID не найдено в БД AuthServices/exit");
    }


    @Test
    @Tag("exit")
    void shouldExitWithValidSession() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Users user = new Users();
        user.setPassword(PASSWORD);
        user.setLogin(PasswordUtil.hashPassword(PASSWORD));
        authDao.saveUser(user);
        Sessions session = new Sessions();
        session.setUser(user);
        session.setExpiresAt(LocalDateTime.now().plusDays(1));
        authDao.saveSession(session);

        request.setCookies(new Cookie("SESSIONID", session.getId().toString()));

        authServices.exit(request, response);

        Cookie[] cookies = response.getCookies();
        assertThat(cookies[0].getName()).isEqualTo("SESSIONID");
        assertThat(cookies).hasSize(1);
        assertThat(cookies[0].getMaxAge()).isEqualTo(0);
        assertThat(cookies[0].getPath()).isEqualTo("/");
        assertThat(cookies[0].getValue()).isNull();
    }

    @Test
    @Tag("sessionClear")
    void shouldCallFindAllExpiresAtInSessionClear() {
        Users user = new Users();
        user.setLogin(LOGIN);
        user.setPassword(PasswordUtil.hashPassword(PASSWORD));
        authDao.saveUser(user);

        Sessions session = new Sessions();
        session.setUser(user);
        LocalDateTime expiresSoon = LocalDateTime.now().minusHours(2); // просроченно на 2 часа
        session.setExpiresAt(expiresSoon);
        authDao.saveSession(session);

        authServices.sessionClear();

        assertThat(authDao.findAllSession()).isEmpty();
    }
    @Test
    @Tag("extendSessions")
    void shouldExtendSessions() {
        Users user = new Users();
        user.setLogin(LOGIN);
        user.setPassword(PasswordUtil.hashPassword(PASSWORD));
        authDao.saveUser(user);

        Sessions session = new Sessions();
        session.setUser(user);
        LocalDateTime expiresSoon = LocalDateTime.now().plusHours(2); // истекает через 2 часа
        session.setExpiresAt(expiresSoon);
        authDao.saveSession(session);

        authServices.extendSessions();

        Sessions updatedSession = authDao.findSessionByUUID(session.getId());
        LocalDateTime expectedNewExpiresAt = LocalDateTime.now().plusDays(1);
        assertThat(updatedSession.getExpiresAt())
                .isAfter(expiresSoon) // -> новое время больше старого
                .isCloseTo(expectedNewExpiresAt, within(1, ChronoUnit.MINUTES));
    }

    @Test
    @Tag("extendSessions")
    void shouldNotExtendSessionWhenExpiresInMoreThanThreeHours() {
        Users user = new Users();
        user.setLogin(LOGIN);
        user.setPassword(PasswordUtil.hashPassword(PASSWORD));
        authDao.saveUser(user);

        Sessions session = new Sessions();
        session.setUser(user);
        LocalDateTime expiresSoon = LocalDateTime.now().plusHours(4); // истекает через 4 часа
        session.setExpiresAt(expiresSoon);
        authDao.saveSession(session);

        authServices.extendSessions();

        Sessions updatedSession = authDao.findSessionByUUID(session.getId());
        assertThat(updatedSession.getExpiresAt())
                .isEqualTo(expiresSoon); // время не изменилоась
    }

    @Test
    @Tag("extendSessions")
    void shouldHandleEmptySessionList() {
        authServices.extendSessions();
        List<Sessions> allSession = authDao.findAllSession();
        assertThat(allSession).isEmpty();
    }
}