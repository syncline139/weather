package com.example.exceptions;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String Exception(Exception exception, Model model) {
        model.addAttribute("errorPage", exception.getMessage());
        return "pages/error";
    }
}
