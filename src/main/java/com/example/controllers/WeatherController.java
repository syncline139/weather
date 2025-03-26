package com.example.controllers;

import com.example.dao.AuthDao;
import com.example.models.Users;
import com.example.services.AuthServices;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/weather")
@RequiredArgsConstructor

public class WeatherController {

    private final AuthDao authDao;
    private final AuthServices authServices;

    @GetMapping()
    public String mainScreenPage(HttpServletRequest request, Model model) {

        HttpSession session = request.getSession(false);
        if (session != null) {
            String login = (String) session.getAttribute("login");
            model.addAttribute("login", login);
        }
        return "pages/index";
    }

}
