package com.example.controllers;

import com.example.dao.AuthDao;
import com.example.dto.response.WeatherDto;
import com.example.services.AuthServices;
import com.example.services.WeatherService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final AuthDao authDao;
    private final AuthServices authServices;
    private final WeatherService weatherService;

    @GetMapping
    public String mainScreenPage(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String login = (String) session.getAttribute("login");
            model.addAttribute("login", login);
        }
        return "pages/index";
    }

    @PostMapping
    public String search(@RequestParam("nameCity") String nameCity, Model model) {
        WeatherDto weatherDto = weatherService.search(nameCity);
        model.addAttribute("weatherDto", weatherDto); // Передаем весь объект WeatherDto
        return "pages/search-results";
    }
}