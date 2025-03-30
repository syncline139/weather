package com.example.controllers;

import com.example.dao.AuthDao;
import com.example.dto.response.WeatherResponseDto;
import com.example.models.Users;
import com.example.services.AuthServices;
import com.example.services.LocationService;
import com.example.services.WeatherService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.print.DocFlavor;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/weather")
@RequiredArgsConstructor

public class WeatherController {

    private final AuthDao authDao;
    private final AuthServices authServices;
    private final WeatherService weatherService;
    private final LocationService locationService;

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
            , RedirectAttributes redirectAttributes, HttpSession session) {

        if (nameCity == null || nameCity.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Город не может быть пустым");
            return "redirect:/weather";
        }

        HttpSession sessionFalse = request.getSession(false);
        if (sessionFalse != null) {
            String login = (String) sessionFalse.getAttribute("login");
            model.addAttribute("login", login);
        }

        WeatherResponseDto search = weatherService.search(nameCity);

        String locationKey = UUID.randomUUID().toString(); // ключ для каждоый локации
        Map<String, WeatherResponseDto> pendingLocations = (Map<String, WeatherResponseDto>) session.getAttribute("pendingLocations");
        if (pendingLocations == null) {
            pendingLocations = new HashMap<>();
            session.setAttribute("pendingLocations", pendingLocations);
        }
        pendingLocations.put(locationKey, search);
        model.addAttribute("locationKey", locationKey); // Передаём ключ на фронтенд
        model.addAttribute("nameCity", search.getName());
        model.addAttribute("coord", search.getCoord());
        model.addAttribute("sys", search.getSys());

        return "pages/search-results";
    }

    @PostMapping("/add-location")
    public String addLocation(@RequestParam("locationKey") String locationKey,
                              HttpSession session) {
        Map<String, WeatherResponseDto> pendingLocations = (Map<String, WeatherResponseDto>) session.getAttribute("pendingLocations");
        if (pendingLocations != null) {
            WeatherResponseDto responseDto = pendingLocations.get(locationKey);
            if (responseDto != null) {
                Integer id = (Integer) session.getAttribute("id");
                locationService.saveLocation(
                        id,
                        responseDto.getName(),
                        responseDto.getCoord().getLat(),
                        responseDto.getCoord().getLon()
                );
            }
        }

        return "redirect:/weather";
    }
}




