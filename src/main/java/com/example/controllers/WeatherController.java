package com.example.controllers;

import com.example.dao.AuthDao;
import com.example.dto.response.WeatherResponseDto;
import com.example.models.Users;
import com.example.services.AuthServices;
import com.example.services.WeatherService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.print.DocFlavor;

@Controller
@RequestMapping("/weather")
@RequiredArgsConstructor

public class WeatherController {

    private final AuthDao authDao;
    private final AuthServices authServices;
    private final WeatherService weatherService;

    @GetMapping()
    public String mainScreenPage(HttpServletRequest request, Model model) {

        HttpSession session = request.getSession(false);
        if (session != null) {
            String login = (String) session.getAttribute("login");
            model.addAttribute("login", login);
        }
        return "pages/index";
    }


    @PostMapping("/search-results")
    public String search(@RequestParam("nameCity") String nameCity, Model model, HttpServletRequest request
            ,RedirectAttributes redirectAttributes) {

        if (nameCity == null || nameCity.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Город не может быть пустым");
            return "redirect:/weather";
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            String login = (String) session.getAttribute("login");
            model.addAttribute("login", login);
        }

        WeatherResponseDto search = weatherService.search(nameCity);
        model.addAttribute("nameCity", search.getName());
        model.addAttribute("coord", search.getCoord());
        model.addAttribute("sys", search.getSys());

        return "pages/search-results";


    }

}
