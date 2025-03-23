package com.example.integration.service;

import com.example.dao.AuthDao;

import com.example.integration.annotation.IT;
import com.example.integration.config.TestConfig;
import com.example.models.Users;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

@IT
public class AuthServicesIT {

    @Autowired
    private AuthDao authDao;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    @Transactional
    void test() {
        Users user = new Users();
        user.setLogin("oleg");
        user.setPassword("123");

        System.out.println("Сохранение пользователя...");
        authDao.saveUser(user);
        System.out.println("Пользователь сохранен.");

        System.out.println("Поиск пользователя по логину...");
        Users foundUser = authDao.findByLogin("oleg");
        System.out.println("Пользователь найден: " + (foundUser != null ? foundUser.getLogin() : "null"));

        assertNotNull(foundUser, "Пользователь должен быть найден");
        assertEquals("oleg", foundUser.getLogin());
        assertEquals("123", foundUser.getPassword());
    }

    @Test
    @Transactional
    void testSchemaCreation() {
        Session currentSession = sessionFactory.getCurrentSession();
        Long count = (Long) currentSession
                .createQuery("select count(*) from Users")
                .uniqueResult();
        assertNotNull(count, "Таблица Users должна существовать");
    }
}
