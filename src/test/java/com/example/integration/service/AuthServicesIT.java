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
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@IT
@Tag("AuthServicesTest")
@Transactional
public class AuthServicesIT {

    public static final String LOGIN = "dispersion";
    public static final String PASSWORD = "sdfsfsdfsd";
    private AuthServices authServices;

    @Autowired
    public AuthServicesIT(AuthServices authServices) {
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

        // Создание мока и сервиса
        AuthDao authDao = mock(AuthDao.class);
        AuthServices authServices = new AuthServices(authDao);

        // Настройка поведения мока
        when(authDao.uniqueLogin(LOGIN)).thenReturn(true); // Логин уникален

        // Выполнение действия
        authServices.save(user);

        // Проверки
        verify(authDao, times(1)).uniqueLogin(LOGIN); // Проверяем, что проверка уникальности вызвана
        verify(authDao, times(1)).saveUser(user);     // Проверяем, что пользователь сохранен
        assertThat(user.getPassword()).isNotEqualTo(PASSWORD); // Пароль изменился (захеширован)
        assertThat(user.getPassword()).hasSize(60);   // Длина хеша соответствует ожидаемой (для BCrypt)
    }

    @Test
    @Tag("save")
    void shouldThrowExceptionWhenUserIsNull() {
        assertThatThrownBy(() -> authServices.save(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Пользователь не может быть null");
    }

    @Test
    @Tag("save")
    void shouldThrowExceptionWhenLoginIsNullOrEmpty() {
        Users users1 = new Users();
        users1.setLogin(null);
        users1.setPassword(PASSWORD);
        Users users2 = new Users();
        users2.setLogin("");
        users2.setPassword(PASSWORD);

        AuthDao authDao = mock(AuthDao.class);
        AuthServices authServices = new AuthServices(authDao);

        assertAll(
                () -> assertThatThrownBy(() -> authServices.save(users1))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Логин не может быть пустым или null"),
                () -> assertThatThrownBy(() -> authServices.save(users2))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Логин не может быть пустым или null")
        );
    }

    @Test
    @Tag("save")
    void shouldThrowExceptionWhenPasswordsDoNotMatch() {
        Users users = new Users();
        users.setLogin(LOGIN);
        users.setPassword(PASSWORD);
        users.setConfirmPassword("SDSADFSDAFSADF");

        AuthDao authDao = mock(AuthDao.class);
        AuthServices authServices = new AuthServices(authDao);


        assertThatThrownBy(() -> authServices.save(users))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Пароли не совпадают");
    }

    @Test
    @Tag("save")
    void shouldThrowExceptionWhenLoginIsNotUnique() {
        Users users1 = new Users();
        users1.setLogin(LOGIN);
        users1.setPassword(PASSWORD);
        users1.setConfirmPassword(PASSWORD);

        AuthDao authDao = mock(AuthDao.class);
        AuthServices authServices = new AuthServices(authDao);

        authDao.saveUser(users1);
        Users users2 = new Users();
        users2.setLogin(LOGIN);
        users2.setPassword("123");
        users2.setConfirmPassword("123");

        assertThatThrownBy(() -> authServices.save(users2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageMatching("Пользователь с таким логином уже существует");
    }

    @Test
    @Tag("createSession")
    void shouldCreateSessionForValidUser() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        Users user = new Users();
        user.setLogin(LOGIN);
        user.setPassword(PASSWORD);
        user.setConfirmPassword(PASSWORD);

        this.authServices.createSession(user, response);

        Cookie[] cookies = response.getCookies();

        assertThat(cookies[0].getName()).isEqualTo("SESSIONID");
        assertThat(cookies[0].getMaxAge()).isEqualTo(86400);
        assertThat(cookies[0].getPath()).isEqualTo("/");
    }

    @Test
    @Tag("createSession")
    void shouldThrowExceptionWhenUserIsNullInCreateSession() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        AuthDao authDao = mock(AuthDao.class);
        AuthServices authServices = new AuthServices(authDao);

        assertThatThrownBy(() -> authServices.createSession(null, response))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("user не может быть null ( AuthServices/createSession");
    }

    @Test
    @Tag("exit")
    void shouldThrowExceptionWhenCookiesAreNullInExit() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        AuthDao authDao = mock(AuthDao.class);
        AuthServices authServices = new AuthServices(authDao);

        assertThatThrownBy(() -> authServices.exit(request, response))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("куки не должны быть null AuthServices/exit");
    }

    @Test
    @Tag("exit")
    void shouldThrowExceptionWhenSessionNameIsInvalidInExit() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        AuthDao authDao = mock(AuthDao.class);
        AuthServices authServices = new AuthServices(authDao);

        request.setCookies(new Cookie("cookie", "fake"));

        assertThatThrownBy(() -> authServices.exit(request, response))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Невереное название сессии AuthServices/exit");
    }

    @Test
    @Tag("exit")
    void shouldThrowExceptionWhenUUIDNotFoundInExit() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        AuthDao authDao = mock(AuthDao.class);
        AuthServices authServices = new AuthServices(authDao);

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

        AuthDao authDao = mock(AuthDao.class);
        AuthServices authServices = new AuthServices(authDao);

        when(authDao.findByUUID("invalid-uuid-string")).thenReturn(true);

        authServices.exit(request, response);

        verify(authDao, times(1)).findByUUID("invalid-uuid-string");
        verify(authDao, never()).findSessionByUUID(any(UUID.class));
    }

    @Test
    @Tag("exit")
    void shouldThrowExceptionWhenSessionIsNullInExit() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String sessionIdStr = UUID.randomUUID().toString();
        request.setCookies(new Cookie("SESSIONID", sessionIdStr));

        AuthDao authDao = mock(AuthDao.class);
        AuthServices authServices = new AuthServices(authDao);

        when(authDao.findByUUID(sessionIdStr)).thenReturn(true);
        when(authDao.findSessionByUUID(any(UUID.class))).thenReturn(null);

        assertThatThrownBy(() -> authServices.exit(request, response))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Сессия не должна быть null AuthServices/exit");

        verify(authDao, times(1)).findByUUID(sessionIdStr);
        verify(authDao, times(1)).findSessionByUUID(UUID.fromString(sessionIdStr));
        verify(authDao, never()).deleteSession(any(Sessions.class));

        when(authDao.findByUUID(sessionIdStr)).thenReturn(true);
        when(authDao.findSessionByUUID(any(UUID.class))).thenReturn(null);
    }

    @Test
    @Tag("exit")
    void shouldExitWithValidSession() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String sessionIdStr = UUID.randomUUID().toString();
        request.setCookies(new Cookie("SESSIONID", sessionIdStr));

        AuthDao authDao = mock(AuthDao.class);
        Sessions sessions = new Sessions();
        sessions.setUser(new Users());
        sessions.setExpiresAt(LocalDateTime.now().plusYears(1));
        AuthServices authServices = new AuthServices(authDao);

        when(authDao.findByUUID(sessionIdStr)).thenReturn(true);
        when(authDao.findSessionByUUID(UUID.fromString(sessionIdStr))).thenReturn(sessions);

        authServices.exit(request, response);

        verify(authDao, times(1)).deleteSession(sessions);
        Cookie[] cookies = response.getCookies();
        assertThat(cookies[0].getName()).isEqualTo("SESSIONID");
        assertThat(cookies).hasSize(1);
        assertThat(cookies[0].getMaxAge()).isEqualTo(0);
        assertThat(cookies[0].getPath()).isEqualTo("/");
        assertThat(cookies[0].getValue()).isNull();
    }

    @Test
    void shouldCallFindAllExpiresAtInSessionClear() {
        AuthDao authDao = mock(AuthDao.class);
        AuthServices authServices = new AuthServices(authDao);

        authServices.sessionClear();

        verify(authDao, times(1)).findAllExpiresat();
    }

    @Test
    void shouldExtendSessions() {
        AuthDao authDao = mock(AuthDao.class);
        AuthServices authServices = new AuthServices(authDao);
        Sessions sessions = new Sessions();
        sessions.setUser(new Users());
        sessions.setExpiresAt(LocalDateTime.now().plusDays(1));

        authDao.saveSession(sessions);
        authServices.extendSessions();

        verify(authDao, times(1)).saveSession(sessions);
    }
}