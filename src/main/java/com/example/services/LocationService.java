package com.example.services;

import com.example.dao.AuthDao;
import com.example.dao.LocationDao;
import com.example.dto.response.LocationResponseDto;
import com.example.dto.response.WeatherCardDto;
import com.example.dto.response.WeatherResponseDto;
import com.example.models.Locations;
import com.example.models.Sessions;
import com.example.models.Users;
import com.example.utils.WeatherCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import kotlin.SinceKotlin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationService {

    public static final String WEATHER_API_URL_TEMPLATE = "https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s";
    private final LocationDao locationDao;
    private final AuthDao authDao;

    @Value("${openweather.api.key}")
    private String API;

    public void saveLocation(Integer id, String city, double lat, double lon) {
        Users user = authDao.findById(id);

        Locations locations = new Locations();
        locations.setUser(user);
        locations.setName(city);
        locations.setLatitude(lat);
        locations.setLongitude(lon);
        locationDao.save(locations);
    }

    public LocationResponseDto searchWeather(double lat, double lon) throws IOException, InterruptedException {
        if (API == null || API.isBlank()) {
            throw new IllegalStateException("API ключ не корректный");
        }

        var url = String.format(WEATHER_API_URL_TEMPLATE, lat, lon, API);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Не удалось получить данные о погоде: " + response.statusCode());
        }

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response.body(), LocationResponseDto.class);
    }
    public List<WeatherCardDto> findLocationData(int userId) {
        List<Locations> locations = locationDao.findLocationsByUserId(userId);
        List<WeatherCardDto> weatherCards = new ArrayList<>();

        for (Locations location : locations) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            try {
                LocationResponseDto weatherResponse = searchWeather(lat, lon);
                String translatedMain = weatherResponse.getWeather() != null && !weatherResponse.getWeather().isEmpty()
                        ? WeatherCondition.translate(weatherResponse.getWeather().getFirst().getMain())
                        : "Неизвестно";
                // Создаем новый объект с переведённым значением
                LocationResponseDto translatedWeather = new LocationResponseDto(
                        weatherResponse.getName(),
                        weatherResponse.getWeather(),
                        weatherResponse.getMain(),
                        weatherResponse.getSys()
                );
                translatedWeather.getWeather().getFirst().setMain(translatedMain);
                weatherCards.add(new WeatherCardDto(location.getId(), location.getName(), translatedWeather));
            } catch (Exception e) {
                log.error("Ошибка получения погоды для {}: {}", location.getName(), e.getMessage());
            }
        }
        return weatherCards;
    }

    public void deleteCard(int userId,int locationId ) {
        List<Locations> locations = locationDao.findLocationsByUserId(userId);
        if (locations.stream().anyMatch(location -> location.getId() == locationId)) {
            locationDao.deleteLocationById(locationId);
        }
    }

    public List<Map<String, Object>> searchCities(HttpSession session, int userId, WeatherResponseDto search ) {
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
        return cities;
    }
}
