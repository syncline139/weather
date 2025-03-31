package com.example.services;

import com.example.dao.AuthDao;
import com.example.dao.LocationDao;
import com.example.dto.response.LocationResponseDto;
import com.example.dto.response.WeatherResponseDto;
import com.example.models.Locations;
import com.example.models.Users;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationDao locationDao;
    private final AuthDao authDao;

    @Value("${API}")
    private String API;

    public void saveLocation(Integer id, String city, double lat, double lon) {

        if (id == null) {
            throw new IllegalArgumentException("id не может быть null");
        }

        Users user = authDao.findById(id);

        Locations locations = new Locations();
        locations.setUser(user);
        locations.setName(city);
        locations.setLatitude(lat);
        locations.setLongitude(lon);

        locationDao.save(locations);
    }

    @SneakyThrows
    public LocationResponseDto searchWeather(double lat, double lon) {
        if (API == null || API.isBlank()) {
            throw new IllegalStateException("API ключ не корректный");
        }

        var url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + API;

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


}
