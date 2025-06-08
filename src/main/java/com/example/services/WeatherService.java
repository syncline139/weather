package com.example.services;

import com.example.dto.response.WeatherResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class WeatherService {


    @Value("${openweather.api.key}")
    private String API;

    @SneakyThrows
    public WeatherResponseDto searchCity(String nameCity) {
        if (API == null || API.isBlank()) {
            throw new IllegalStateException("API ключ не корректный");
        }
        String encoderCity = URLEncoder.encode(nameCity, StandardCharsets.UTF_8);
        var url = String.format("https://api.openweathermap.org/data/2.5/find?q=%s&appid=%s", encoderCity, API);

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
        return mapper.readValue(response.body(), WeatherResponseDto.class);
    }
}
