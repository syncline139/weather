package com.example.controllers;

import com.example.dao.LocationDao;
import com.example.dto.response.WeatherCardDto;
import com.example.dto.response.LocationResponseDto;
import com.example.dto.response.WeatherResponseDto;
import com.example.exceptions.GlobalExceptionHandler;
import com.example.models.Locations;
import com.example.services.LocationService;
import com.example.services.WeatherService;
import com.example.utils.WeatherCondition;
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

    /**
     * Контроллер отвечает за вывод главной сраницы и полученным из сессии логином который был сохранен при аутентификации
     *
     * @return возвращаем основную страницу с карточками
     */

    @GetMapping
    public String mainScreenPage(Model model, HttpSession httpSession) {
        String login = (String) httpSession.getAttribute("login");
        Integer userId = (Integer) httpSession.getAttribute("id");

        if (userId == null) {
            return "redirect:/auth/sign-in";
        }

        model.addAttribute("login", login);

        List<Locations> locations = locationDao.findLocationsByUserId(userId);
        List<WeatherCardDto> weatherCards = new ArrayList<>();

        for (Locations location : locations) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            try {
                LocationResponseDto weather = locationService.searchWeather(lat, lon);

                String translatedMain = weather.getWeather() != null && !weather.getWeather().isEmpty()
                        ? WeatherCondition.translate(weather.getWeather().get(0).getMain())
                        : "Неизвестно";
                // Создаем новый объект с переведённым значением
                LocationResponseDto translatedWeather = new LocationResponseDto(
                        weather.getName(),
                        weather.getWeather(),
                        weather.getMain(),
                        weather.getSys()
                );
                translatedWeather.getWeather().get(0).setMain(translatedMain);
                weatherCards.add(new WeatherCardDto(location.getId(), location.getName(), translatedWeather));
            } catch (Exception e) {
                System.err.println("Ошибка получения погоды для " + location.getName() + ": " + e.getMessage());
            }
        }

        model.addAttribute("weatherCards", weatherCards);
        return "pages/index";
    }


    @DeleteMapping("/delete-card/{id}")
    public String deleteCard(@PathVariable("id") int locationId, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("id");
        if (userId != null) {
            List<Locations> locations = locationDao.findLocationsByUserId(userId);
            if (locations.stream().anyMatch(location -> location.getId() == locationId)) {
                locationDao.deleteLocationById(locationId);
            }
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

        // Подготовка списка городов с уникальными ключами
        Map<String, WeatherResponseDto.WeatherItem> pendingLocations = (Map<String, WeatherResponseDto.WeatherItem>) session.getAttribute("pendingLocations");
        if (pendingLocations == null) {
            pendingLocations = new HashMap<>();
            session.setAttribute("pendingLocations", pendingLocations);
        }

        List<Map<String, Object>> cities = new ArrayList<>();
        for (WeatherResponseDto.WeatherItem city : search.getList()) {
            // Проверка уникальности для каждого города
            if (!locationDao.uniqueLocationDate(city.getCoord().getLat(), city.getCoord().getLon(), userId)) {
                String locationKey = UUID.randomUUID().toString();
                pendingLocations.put(locationKey, city);
                Map<String, Object> cityData = new HashMap<>();
                cityData.put("locationKey", locationKey);
                cityData.put("name", city.getName());
                cityData.put("country", city.getSys().getCountry());
                cityData.put("coord", city.getCoord());
                cities.add(cityData);
            }
        }

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




