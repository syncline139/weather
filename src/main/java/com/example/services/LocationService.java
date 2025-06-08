package com.example.services;

import com.example.dao.AuthDao;
import com.example.dao.LocationDao;
import com.example.dto.response.LocationResponseDto;
import com.example.dto.response.WeatherCardDto;
import com.example.models.Locations;
import com.example.models.Users;
import com.example.utils.WeatherCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

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

        var url = String.format("https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s", lat, lon, API);

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
                LocationResponseDto weather = searchWeather(lat, lon);
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
        return weatherCards;
    }
}
