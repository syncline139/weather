package com.example.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility класс для работы с шифрованием и проверкой паролей.
 *
 * Этот класс использует алгоритм BCrypt для генерации хешей паролей и проверки
 * соответствия между открытым паролем и его хешированным представлением.
 *
 */
public class PasswordUtil {

    /**
     * Генерирует BCrypt-хеш для заданного открытого пароля.
     *
     * @param password открытый пароль, который необходимо захешировать
     * @return строковое представление BCrypt-хеша пароля
     */
    public static String hashPassword(String password) {
        String gensalt = BCrypt.gensalt();
        return BCrypt.hashpw(password, gensalt);
    }

    /**
     * Проверяет, соответствует ли открытый пароль заданному BCrypt-хешу.
     *
     * @param password     открытый пароль для проверки
     * @param hashPassword BCrypt-хеш, с которым производится сравнение
     * @return true если открытый пароль соответствует хешу иначе false
     */
    public static boolean checkPassword(String password, String hashPassword) {
        return BCrypt.checkpw(password, hashPassword);
    }
}
