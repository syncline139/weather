package com.example.controllers;

import com.example.dao.AuthDao;
import com.example.models.Sessions;
import com.example.models.Users;
import com.example.services.AuthServices;
import com.example.util.PasswordUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * В данным классе содержаться контроллеры относящиеся к аунтентификации
 */
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthDao authDao;
    private final AuthServices authServices;


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
     *  TODO сделать документацию
     */
    @PatchMapping("/sign-up")
    public String registration(@ModelAttribute("user") @Valid Users user, BindingResult bindingResult) {
        try {
            authServices.save(user);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Пароли")) {
                bindingResult.rejectValue("confirmPassword", "error.confirmPassword", e.getMessage());
            } else {
                bindingResult.rejectValue("login", "error.login", e.getMessage());
            }
        } catch (IllegalStateException e) {
            bindingResult.rejectValue("login", "error.login", e.getMessage());
        }

        if (bindingResult.hasErrors()) {
            return "auth/sign-up";
        }

        return "auth/sign-in";
    }


    @GetMapping("/sign-in")
    public String authorizationPage(Users user, Model model) {
        model.addAttribute("user", user);
        return "auth/sign-in";
    }

    /**
     * Котнроллер отвечает за обработку валидации и куков он ищет юзера по логину
     * проходит все проверки и если вход успешен то пользотвалю отправляются куки
     * которые мы обрабатываем в сервисе
     *
     * @param response опралвяем куки пользотвалю
     * @return редирактим юзера на основню страницу
     *      TODO печинить аунтетификацию
     *      TODO при аунтентификации перебрасывает на страницу ошибки вместо того что бы выкинуть валидацию
     */
    @PostMapping("/sign-in")
    public String authentication(@ModelAttribute("login") Users user,
                                BindingResult bindingResult,
                                HttpServletResponse response,
                                HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            return "auth/sign-in";
        }

        Users byLogin = authDao.findByLogin(user.getLogin());
        if (byLogin == null) {
            bindingResult.rejectValue("user", "error.user", "Пользователь не найден");
            return "auth/sign-in";
        }
        if (!PasswordUtil.checkPassword(user.getPassword(), byLogin.getPassword())) {
            bindingResult.rejectValue("password", "error.password", "Неверный пароль");
            return "auth/sign-in";
        }

        authServices.createSession(byLogin, response);
        request.getSession().setAttribute("login", user.getLogin()); // отправляем в сесиию логин пользователя

        return "redirect:/";
    }

    @DeleteMapping("/logout")
    public String signOut(HttpServletRequest request, HttpServletResponse response) {
        authServices.exit(request, response);
        return "redirect:/auth/sign-in";
    }


}
