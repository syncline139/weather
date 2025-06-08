package com.example.controllers;

import com.example.models.Users;
import com.example.services.AuthService;
import com.example.utils.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String PASSWORD_MISMATCH_ERROR = "error.confirmPassword";
    private static final String LOGIN_ERROR = "error.login";
    private static final String WRONG_PASSWORD_ERROR = "error.password";

    private final AuthService authService;

    @GetMapping("/sign-up")
    public String registrationPage(Model model) {
        model.addAttribute("user", new Users());
        return "auth/sign-up";
    }

    @PostMapping("/sign-up")
    public String registration(@ModelAttribute("user") @Valid Users user,
                               BindingResult bindingResult) {

        if (user.getPassword() != null && !user.getPassword().equals(user.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", PASSWORD_MISMATCH_ERROR, "Пароли не совпадают");
        }
        if (!authService.uniqueLogin(user.getLogin())) {
            bindingResult.rejectValue("login", LOGIN_ERROR, "Пользователь с таким логином уже существует");
        }
        if (bindingResult.hasErrors()) {
            return "auth/sign-up";
        }

        authService.save(user);
        return "auth/sign-in";
    }

    @GetMapping("/sign-in")
    public String authorizationPage(Model model) {
        model.addAttribute("user", new Users());
        return "auth/sign-in";
    }

    @PostMapping("/sign-in")
    public String authentication(@ModelAttribute("user") Users user,
                                 BindingResult bindingResult,
                                 HttpServletResponse response,
                                 HttpServletRequest request,
                                 Model model) {

        Users byLogin = authService.findByLogin(user.getLogin());
        if (byLogin == null) {
            bindingResult.rejectValue("login", LOGIN_ERROR, "Пользователь не найден");
            model.addAttribute("user", user);
            return "auth/sign-in";
        }

        if (user.getPassword() != null && !PasswordUtil.checkPassword(user.getPassword(), byLogin.getPassword())) {
            bindingResult.rejectValue("password", WRONG_PASSWORD_ERROR, "Неверный пароль");
            model.addAttribute("user", user);
            return "auth/sign-in";
        }

        authService.createSession(byLogin, response);
        //Сохранение данных в сессию
        request.getSession().setAttribute("login", user.getLogin());
        request.getSession().setAttribute("id", byLogin.getId());
        return "redirect:/weather";
    }

    @DeleteMapping("/logout")
    public String signOut(HttpServletRequest request, HttpServletResponse response) {
        authService.exit(request, response);
        return "redirect:/auth/sign-in";
    }


}
