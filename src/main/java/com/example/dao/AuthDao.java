package com.example.dao;

import com.example.models.Sessions;
import com.example.models.Users;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * В данном классе содержатся hibernate запросы к БД относящиеся к аунтентификации
 */
@Component
@Transactional
public class AuthDao {

    private final SessionFactory sessionFactory;

    @Autowired
    public AuthDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Сохраняет пользотвалея в БД
     *
     * @param user получаем сущность юзера с который дальше работаем
     */
    public void saveUser(Users user) {
        Session currentSession = sessionFactory.getCurrentSession();
        if (user != null) {
            currentSession.persist(user);
        }
    }

    public void saveSession(Sessions session) {
        Session currentSession = sessionFactory.getCurrentSession();
        if (session != null) {
            currentSession.persist(session);
        }
    }

    /**
     * Проверяет на уникальность login, если после запроса в count больше 0 символов значит логин уже занят
     *
     * @param login передаем логин что бы узнать его уникальность
     * @return возвращаем true если логин не занят
     */
    public boolean uniqueLogin(String login) {
        Session currentSession = sessionFactory.getCurrentSession();
        Long count = (Long) currentSession
                .createQuery("select count(u) from Users u where u.login = :login")
                .setParameter("login", login)
                .uniqueResult();

        return count == 0;
    }

    /**
     * Ищем в БД логин пришедший с формы от пользотваля
     *
     * @return если нашли логин возвращаем его
     */
    public Users findByLogin(String login) {
        Session currentSession = sessionFactory.getCurrentSession();
        return currentSession
                .createQuery("from Users u where u.login = :login", Users.class)
                .setParameter("login", login)
                .uniqueResult();
    }


}
