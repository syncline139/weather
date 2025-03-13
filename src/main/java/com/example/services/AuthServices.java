package com.example.services;

import com.example.dao.AuthDao;
import com.example.models.Users;
import com.example.util.PasswordUtil;
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
     * Проверяем схожеться паролей и уникальность логина, а так же полсле все проверок хешируем пароль
     *
     * @param user передаем человека которого мы сохраним если пройдем провреки
     */
    public void save(Users user) {
        String login = user.getLogin();
        if (user.getPassword() != null &&
            user.getPassword().equals(user.getConfirmPassword()) &&
            authDao.uniqueLogin(login)) {

            String hashedPassword = PasswordUtil.hashPassword(user.getPassword());
            user.setPassword(hashedPassword);

            authDao.save(user);
        }
    }

}
