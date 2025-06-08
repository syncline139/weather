package com.example.dao;

import com.example.models.Sessions;
import com.example.models.Users;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional
@RequiredArgsConstructor
public class AuthDao {

    private final SessionFactory sessionFactory;

    public void saveUser(Users user) {
        Session currentSession = sessionFactory.getCurrentSession();
            currentSession.persist(user);
            currentSession.flush();
    }

    public void saveSession(Sessions session) {
        Session currentSession = sessionFactory.getCurrentSession();
            currentSession.persist(session);

    }

    public boolean uniqueLogin(String login) {
        Session currentSession = sessionFactory.getCurrentSession();
        Long count = (Long) currentSession
                .createQuery("select count(u) from Users u where u.login = :login")
                .setParameter("login", login)
                .uniqueResult();

        return count == 0;
    }

    public Users findByLogin(String login) {
        Session currentSession = sessionFactory.getCurrentSession();
        return currentSession
                .createQuery("from Users u where u.login = :login", Users.class)
                .setParameter("login", login)
                .uniqueResult();
    }

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

    public void removeAllExpiresatElseOverdueTime() {
        sessionFactory.getCurrentSession()
                .createQuery("DELETE FROM Sessions s WHERE s.expiresAt < :now")
                .setParameter("now", LocalDateTime.now())
                .executeUpdate();
    }

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

    public void deleteAllSessions() {
        Session currentSession = sessionFactory.getCurrentSession();
        currentSession.createQuery("DELETE FROM Sessions ").executeUpdate();
    }

    public Users findById(int id) {
        Session currentSession = sessionFactory.getCurrentSession();
        return currentSession.createQuery(
                        "FROM Users u WHERE u.id = :id", Users.class)
                .setParameter("id", id)
                .getSingleResult();
    }
}
