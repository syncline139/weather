package com.example.integration.dao;

import com.example.dao.AuthDao;
import com.example.integration.annotation.IT;
import com.example.models.Users;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@IT
@Transactional
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

    //Уникально логина ( uniqueLogin )
    @Test
    void uniqueLoginWhenLoginDoesNotExist() {
        boolean unique = authDao.uniqueLogin(LOGIN);
        assertThat(unique).isTrue();
    }

    @Test
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
    void uniqueLoginWithNullLogin() {
        boolean unique = authDao.uniqueLogin(null);
        assertThat(unique).isFalse();
    }

    @Test
    void uniqueLoginWithEmptyLogin() {
        boolean unique = authDao.uniqueLogin("");
        assertThat(unique).isFalse();

    }

}
