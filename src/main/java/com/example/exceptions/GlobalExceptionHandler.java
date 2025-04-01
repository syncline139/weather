package com.example.util;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Клаасс отвечает за обработку всеъ исключений
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * При любом исключении возращаем пользотелю страницу с ошибкой
     */
    @ExceptionHandler(Exception.class)
    public String Exception(Exception exception, Model model) {
        model.addAttribute("errorPage", exception.getMessage());
        return "pages/error";
    }
}
