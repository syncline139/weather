package com.example.controllers;

import com.example.dao.AuthDao;
import com.example.models.Users;
import com.example.services.AuthServices;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * В данным классе содержаться контроллеры относящиеся к аунтентификации
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthDao authDao;
    private final AuthServices authServices;

    public AuthController(AuthDao authDao, AuthServices authServices) {
        this.authDao = authDao;
        this.authServices = authServices;
    }

    /**
     * Возвращает странцу регестрации
     *
     * @param model создается новый пустой объект юзера
     * @return возвращаем странцу регестрации
     */
    @GetMapping("/sign-up")
    public String registrationPage(Model model) {
        model.addAttribute("user", new Users());
        return "auth/sign-up";
    }

    /**
     * Добавляет нового юзера прошедшего валдиацию в БД
     *
     * @param user передается человек уже с данными из формы
     * @param bindingResult ошбики валидации
     * @return перекидываем пользователя на страницу входа в акканут после успешной регестрации
     */
    @PatchMapping()
    public String registration(@ModelAttribute("user") @Valid Users user,
                               BindingResult bindingResult) {

        if (!user.getPassword().equals(user.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Пароли не совпадают");
        }
        if (!authDao.uniqueLogin(user.getLogin())) {
            bindingResult.rejectValue("login", "error.login", "Пользователь с таким логином уже существует");
        }
        if (bindingResult.hasErrors()) {
            return "auth/sign-up";
        }

            authServices.save(user);

        return "auth/sign-in";
    }

    @GetMapping("/sign-in")
    public String authorization() {
        return "auth/sign-in";
    }
}
