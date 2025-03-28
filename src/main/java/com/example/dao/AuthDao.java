package com.example.dao;

import com.example.models.Sessions;
import com.example.models.Users;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * В данном классе содержатся hibernate запросы к БД относящиеся к аунтентификации
 */
@Repository
@Transactional
@RequiredArgsConstructor
public class AuthDao {

    private final SessionFactory sessionFactory;


    /**
     * Сохраняет пользотвалея в БД
     *
     * @param user получаем сущность юзера с который дальше работаем
     */
    public void saveUser(Users user) {
        Session currentSession = sessionFactory.getCurrentSession();
        if (user != null) {
            currentSession.persist(user);
            currentSession.flush();
        }
    }

    public void saveSession(Sessions session) {
        Session currentSession = sessionFactory.getCurrentSession();
            currentSession.persist(session);

    }

    /**
     * Проверяет на уникальность login, если после запроса в count больше 0 символов значит логин уже занят
     *
     * @param login передаем логин что бы узнать его уникальность
     * @return возвращаем true если логин не занят
     */
    public boolean uniqueLogin(String login) {
        if (login == null || login.isEmpty()) {
            throw new IllegalArgumentException("логин не может быть null или пустым AuthDao/uniqueLogin");
        }
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

    /**
     * При успехе Hibernate извлекается объект Sessions, используя uuid в качестве идентификатора
     * затем валидируем сессию если заданная сесиия больше текущего значит все гуд
     *
     * @param id в 1 очередь метод получает строку id и пытается преобразовать её в объект UUID
     * @return true если сессия валиданая и false если сессия не найдена или срок истечения уже прошел
     */
    public boolean findByUUID(String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return false;
        }

        Session currentSession = sessionFactory.getCurrentSession();
        Sessions sessions = currentSession.get(Sessions.class, uuid);

        if (sessions != null) {
            LocalDateTime expiresAt = sessions.getExpiresAt();
            if (expiresAt != null && expiresAt.isAfter(LocalDateTime.now())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Запрос ищет просроченные UUID и удаляет
     */
    public void findAllExpiresat() {
        Session currentSession = sessionFactory.getCurrentSession();

        List<Sessions> expiresAt = currentSession.createQuery("from Sessions", Sessions.class)
                .getResultList();
        // проверяем каждую сесиию и если время у какой то меньше чем нынешнее значит оно прострочено и удаляем его
        for (Sessions s : expiresAt) {
            if (s.getExpiresAt().isBefore(LocalDateTime.now())) {
                currentSession.remove(s);
            }
        }
    }

    /**
     * @return возвращает список всех найденных сессий
     */
    public List<Sessions> findAllSession() {
        Session currentSession = sessionFactory.getCurrentSession();
        return currentSession.createQuery("from Sessions", Sessions.class)
                .getResultList();
    }


    public void deleteSession(Sessions session) {
        Session currentSession = sessionFactory.getCurrentSession();
            currentSession.remove(session);
    }

    public Sessions findSessionByUUID(UUID uuid) {
        Session currentSession = sessionFactory.getCurrentSession();
        return currentSession.createQuery("select s from Sessions s where id = :uuid", Sessions.class)
                .setParameter("uuid", uuid).getSingleResult();
    }

}
