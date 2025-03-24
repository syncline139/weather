package com.example.integration.dao;

import com.example.dao.AuthDao;
import com.example.integration.annotation.IT;
import com.example.models.Sessions;
import com.example.models.Users;
import org.h2.engine.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@IT
@Transactional
@Tag("daoTest")
public class FinderDaoTest {

    private AuthDao authDao;
    private SessionFactory sessionFactory;

    @Autowired
    public FinderDaoTest(AuthDao authDao, SessionFactory sessionFactory) {
        this.authDao = authDao;
        this.sessionFactory = sessionFactory;
    }

    private static final String LOGIN = "SYNCLINE";
    private static final String PASSWORD = "kot123123228";

    @BeforeEach
    @Transactional
    void setUp() {
        Session currentSession = sessionFactory.getCurrentSession();
        currentSession.createQuery("delete from Users").executeUpdate();
    }

    @Test
    @Tag("uniqueLogin")
    void uniqueLoginWhenLoginDoesNotExist() {
        boolean unique = authDao.uniqueLogin(LOGIN);
        assertThat(unique).isTrue();
    }

    @Test
    @Tag("uniqueLogin")
    void uniqueLoginWhenLoginExists() {
        Users users = new Users();
        users.setLogin(LOGIN);
        users.setPassword(PASSWORD);

        Session currentSession = sessionFactory.getCurrentSession();
        currentSession.persist(users);
        currentSession.flush();

        boolean unique = authDao.uniqueLogin(LOGIN);
        assertThat(unique).isFalse();
    }

    @Test
    @Tag("uniqueLogin")
    void uniqueLoginWithNullLogin() {
        assertAll(
                ()->assertThatThrownBy(() -> authDao.uniqueLogin(null))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("логин не может быть null или пустым AuthDao/uniqueLogin"),
                ()->assertThatThrownBy(() -> authDao.uniqueLogin(""))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("логин не может быть null или пустым AuthDao/uniqueLogin")
        );

    }

    @Test
    @Tag("uniqueLogin")
    void uniqueLoginWithEmptyLogin() {
        boolean unique = authDao.uniqueLogin("");
        assertThat(unique).isFalse();
    }

    @Test
    @Tag("saveUser")
    void saveUser() {
        Users user = new Users();
        user.setLogin(LOGIN);
        user.setPassword(PASSWORD);

        authDao.saveUser(user);

        assertThat(authDao.findByLogin(user.getLogin())).isNotNull();
    }

    // Сохранение сессии ( saveSession)
    @Test
    @Tag("saveSession")
    void saveSession() {
        Sessions sessions = new Sessions();

        sessions.setExpiresAt(
                LocalDateTime.of(3000, 11, 2, 2, 2));
        sessions.setUser(new Users());

        authDao.saveSession(sessions);

        UUID id = sessions.getId();
        String uuid = id.toString();

        assertThat(authDao.findByUUID(uuid)).isTrue();
    }

    @Test
    @Tag("findLogin")
    void findByLogin() {
        Users user = new Users();
        user.setLogin(LOGIN);
        user.setPassword(PASSWORD);

        Session currentSession = sessionFactory.getCurrentSession();
        currentSession.persist(user);
        currentSession.flush();

        Users maybeLogin = authDao.findByLogin(user.getLogin());
        assertThat(maybeLogin).isNotNull();
    }

    @Test
    @Tag("findAllExpiresat")
    void findAllExpiresat() {
        Sessions sessions = new Sessions();
        Users user = new Users();
        user.setLogin(LOGIN);
        user.setPassword(PASSWORD);
        authDao.saveUser(user);
        sessions.setUser(user);
        sessions.setExpiresAt(
                LocalDateTime.of(2000, 11, 1, 1, 1));


        authDao.saveSession(sessions);
        UUID uuid = sessions.getId();

        authDao.findAllExpiresat();

        Sessions foundSession = sessionFactory
                .getCurrentSession()
                .get(Sessions.class, uuid);
        assertThat(foundSession).isNull();
    }

    @Test
    @Tag("findAllSession")
    void findAllSession() {
        Sessions sessions1 = new Sessions();
        Users user1 = new Users();
        user1.setLogin(LOGIN);
        user1.setPassword(PASSWORD);
        authDao.saveUser(user1);
        sessions1.setUser(user1);
        sessions1.setExpiresAt(
                LocalDateTime.of(3000, 11, 1, 1, 1));


        Sessions sessions2 = new Sessions();
        Users user2 = new Users();
        user2.setLogin("Mixa");
        user2.setPassword(PASSWORD);
        authDao.saveUser(user2);
        sessions2.setUser(user2);
        sessions2.setExpiresAt(
                LocalDateTime.of(3000, 11, 1, 1, 1));


        authDao.saveSession(sessions1);
        authDao.saveSession(sessions2);


        List<Sessions> allSession = authDao.findAllSession();

        assertThat(allSession).hasSize(2);
    }

    @Test
    @Tag("deleteSession")
    void deleteSession() {
        Users users = new Users();
        users.setLogin(LOGIN);
        users.setPassword(PASSWORD);
        authDao.saveUser(users);

        Sessions sessions = new Sessions();
        sessions.setUser(users);
        sessions.setExpiresAt(
                LocalDateTime.of(3000, 11, 1, 1, 1));
        authDao.saveSession(sessions);

        authDao.deleteSession(sessions);

        Sessions foundSession = sessionFactory
                .getCurrentSession()
                .get(Sessions.class, sessions.getId());
        assertThat(foundSession).isNull();
    }

    @Test
    @Tag("findSessionByUUID")
    void findSessionByUUID() {
        Users users = new Users();
        users.setLogin(LOGIN);
        users.setPassword(PASSWORD);
        authDao.saveUser(users);

        Sessions sessions = new Sessions();
        sessions.setUser(users);
        sessions.setExpiresAt(
                LocalDateTime.of(3000, 11, 1, 1, 1));
        authDao.saveSession(sessions);


        Sessions sessionByUUID = authDao.findSessionByUUID(sessions.getId());
        assertThat(sessionByUUID).isNotNull();
    }


}
