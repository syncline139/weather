package com.example.integration.service;

import com.example.dto.response.WeatherResponseDto;
import com.example.integration.annotation.IT;
import com.example.services.WeatherService;
import lombok.SneakyThrows;
import lombok.Value;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apiguardian.api.API;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@IT
@Tag("WeatherService")
@Transactional
public class WeatherServiceIT {

    private MockWebServer server;

    @Autowired
    private WeatherService weatherService;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void searchSuccessfulResponse() throws Exception {
        String city = "Moscow";
        String mockResponse = Files.readString(Path.of("src/test/resources/moscow_response.json"));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse));

        String baseUrl = server.url("/weather").toString();
        System.setProperty("weather.api.base-url", baseUrl);

        WeatherResponseDto responseDto = weatherService.search(city);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getName()).isEqualTo("Moscow");
        assertThat(responseDto.getCoord().getLon()).isEqualTo(37.6156);
        assertThat(responseDto.getCoord().getLat()).isEqualTo(55.7522);
        assertThat(responseDto.getSys().getCountry()).isEqualTo("RU");
    }

    @Test
    void searchApiNull() throws Exception {
        System.clearProperty("API");

        WeatherService weatherService = new WeatherService();

        String city = "Moscow";
        String mockResponse = Files.readString(Path.of("src/test/resources/moscow_response.json"));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse));

        String baseUrl = server.url("/weather").toString();
        System.setProperty("weather.api.base-url", baseUrl);

        assertThatThrownBy(() -> weatherService.search(city))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("API ключ не корректный");
    }

    @Test
    void searchStatusCode404() throws Exception {
        String city = "Moscow-invalid";
        String mockResponse = Files.readString(Path.of("src/test/resources/moscow_response.json"));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse));

        String baseUrl = server.url("/weather").toString();
        System.setProperty("weather.api.base-url", baseUrl);


        assertThatThrownBy(() -> weatherService.search(city))
                .isInstanceOf(IOException.class)
                .hasMessage("Не удалось получить данные о погоде: 404");

    }

}
