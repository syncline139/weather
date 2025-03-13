package com.example.services;

import com.example.dao.AuthDao;
import com.example.models.Users;
import org.springframework.stereotype.Service;

/**
 * В данном классе содержиться бизнес-логика относящиеся к аунтентификации
 */
@Service
public class AuthServices {
    private final AuthDao authDao;

    public AuthServices(AuthDao authDao) {
        this.authDao = authDao;
    }

    /**
     * Проверяем схожеться паролей и уникальность логина
     *
     * @param user передаем человека которого мы сохраним если пройдем провреки
     */
    public void save(Users user) {
        String login = user.getLogin();
        if (user.getPassword() != null &&
            user.getPassword().equals(user.getConfirmPassword()) &&
            authDao.uniqueLogin(login)) {

            authDao.save(user);
        }
    }

}
