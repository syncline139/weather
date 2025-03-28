package com.example.services;

import com.example.dao.AuthDao;
import com.example.dto.response.WeatherDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final AuthDao authDao;
    private final Environment environment;

    @SneakyThrows
    public WeatherDto search(String nameCity) {
        String apiKey = environment.getProperty("API");
        if (apiKey == null) {
            throw new IllegalArgumentException("API ключ не задан");
        }
        if (nameCity == null || nameCity.trim().isEmpty()) {
            throw new IllegalArgumentException("Город не может быть null или пустым");
        }

        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + nameCity + "&appid=" + apiKey + "&units=metric";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Ошибка API: " + response.statusCode());
        }

        String json = response.body();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, WeatherDto.class);
    }
}