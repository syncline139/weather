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
     * Добавляет нового юзера прошедшего валдиацию в БД
     *
     * @param user          передается человек уже с данными из формы
     * @param bindingResult ошбики валидации
     * @return перекидываем пользователя на страницу входа в акканут после успешной регестрации
     */
    @PostMapping("/sign-up")
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
    public String authorizationPage(Model model) {
        model.addAttribute("user", new Users());
        return "auth/sign-in";
    }

    /**
     * Контроллер отвечает за обработку валидации и кук. Он ищет юзера по логину,
     * проходит все проверки и, если вход успешен, пользователю отправляются куки,
     * которые мы обрабатываем в сервисе.
     *
     * @param user          объект пользователя из формы
     * @param bindingResult результат валидации
     * @param response      отправляем куки пользователю
     * @param request       для работы с сессией
     * @param model         для передачи данных в шаблон
     * @return редиректим юзера на основную страницу или возвращаем форму с ошибками
     */
    @PostMapping("/sign-in")
    public String authentication(@ModelAttribute("user") Users user,
                                 BindingResult bindingResult,
                                 HttpServletResponse response,
                                 HttpServletRequest request,
                                 Model model) {


        Users byLogin = authDao.findByLogin(user.getLogin());
        if (byLogin == null) {
            bindingResult.rejectValue("login", "error.login", "Пользователь не найден");
            model.addAttribute("user", user);
            return "auth/sign-in";
        }

        // Проверка пароля
        if (!PasswordUtil.checkPassword(user.getPassword(), byLogin.getPassword())) {
            bindingResult.rejectValue("password", "error.password", "Неверный пароль");
            model.addAttribute("user", user);
            return "auth/sign-in";
        }

        authServices.createSession(byLogin, response);
        request.getSession().setAttribute("login", user.getLogin()); // Сохраняем логин в сессии
        request.getSession().setAttribute("id", byLogin.getId());

        return "redirect:/weather";
    }

    /**
     * После нажатия на кнопку 'logout' пользователю отправляем куки с чистой сессией и удаляем из БД
     *
     * @return редиректим юзера на страницу входа в акканут
     */
    @DeleteMapping("/logout")
    public String signOut(HttpServletRequest request, HttpServletResponse response) {
        authServices.exit(request, response);
        return "redirect:/auth/sign-in";
    }


}
