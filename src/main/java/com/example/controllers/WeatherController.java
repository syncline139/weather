package com.example.controllers;

import com.example.dao.LocationDao;
import com.example.dto.response.WeatherCardDto;
import com.example.dto.response.WeatherResponseDto;
import com.example.models.Locations;
import com.example.services.LocationService;
import com.example.services.WeatherService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/weather")
@RequiredArgsConstructor

public class WeatherController {

    private final WeatherService weatherService;
    private final LocationService locationService;
    private final LocationDao locationDao;

    @GetMapping
    public String mainScreenPage(Model model, HttpSession httpSession) {
        String login = (String) httpSession.getAttribute("login");
        Integer userId = (Integer) httpSession.getAttribute("id");

        if (userId == null) {
            return "redirect:/auth/sign-in";
        }

        model.addAttribute("login", login);

        List<WeatherCardDto> weatherCards = locationService.findLocationData(userId);
        model.addAttribute("weatherCards", weatherCards);
        return "pages/index";
    }

    @DeleteMapping("/delete-card/{id}")
    public String deleteCard(@PathVariable("id") int locationId, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("id");
        if (userId != null) {
            locationService.deleteCard(userId, locationId);
        }
        return "redirect:/weather";
    }

    @PostMapping("/search-results")
    public String search(@RequestParam("nameCity") String nameCity,
                         Model model,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes,
                         HttpSession session) {
        if (nameCity == null || nameCity.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Город не может быть пустым");
            return "redirect:/weather";
        }

        HttpSession sessionFalse = request.getSession(false);
        if (sessionFalse == null) {
            return "redirect:/auth/sign-in";
        }

        String login = (String) sessionFalse.getAttribute("login");
        Integer userId = (Integer) sessionFalse.getAttribute("id");
        if (userId == null) {
            return "redirect:/auth/sign-in";
        }
        model.addAttribute("login", login);

        WeatherResponseDto search = weatherService.searchCity(nameCity);
        if (search == null || search.getList() == null || search.getList().isEmpty()) {
            throw new IllegalStateException();
        }

        List<Map<String, Object>> cities = locationService.searchCities(session, userId, search);

        if (cities.isEmpty()) {
            redirectAttributes.addFlashAttribute("successfulMessage", "Все найденные города уже добавлены");
            return "redirect:/weather";
        }

        model.addAttribute("cities", cities);
        return "pages/search-results";
    }

    @PostMapping("/add-location")
    public String addLocation(@RequestParam("locationKey") String locationKey,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Map<String, WeatherResponseDto.WeatherItem> pendingLocations = (Map<String, WeatherResponseDto.WeatherItem>) session.getAttribute("pendingLocations");
        if (pendingLocations == null || pendingLocations.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Локация не найдена в ожидающих");
            return "redirect:/weather";
        }

        WeatherResponseDto.WeatherItem selectedCity = pendingLocations.get(locationKey);
        if (selectedCity == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Выбранная локация не найдена");
            return "redirect:/weather";
        }

        Integer userId = (Integer) session.getAttribute("id");
        if (userId == null) {
            return "redirect:/auth/sign-in";
        }

        locationService.saveLocation(
                userId,
                selectedCity.getName(),
                selectedCity.getCoord().getLat(),
                selectedCity.getCoord().getLon()
        );
        pendingLocations.remove(locationKey);

        redirectAttributes.addFlashAttribute("successfulMessage", "Локация успешно добавлена");
        return "redirect:/weather";
    }


}




