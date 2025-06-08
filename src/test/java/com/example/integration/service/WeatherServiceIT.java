//package com.example.integration.service;
//
//import com.example.dto.response.WeatherResponseDto;
//import com.example.integration.annotation.IT;
//import com.example.services.WeatherService;
//import okhttp3.mockwebserver.MockResponse;
//import okhttp3.mockwebserver.MockWebServer;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.mock;
//
//@IT
//@Tag("WeatherService")
//@Transactional
//public class WeatherServiceIT {
//
//    private MockWebServer server;
//
//    @Autowired
//    private WeatherService weatherService;
//
//    @BeforeEach
//    void setUp() throws IOException {
//        server = new MockWebServer();
//        server.start();
//    }
//
//    @AfterEach
//    void tearDown() throws IOException {
//        server.shutdown();
//    }
//
//    @Test
//    void searchCityApiNull() throws Exception {
//        System.clearProperty("API");
//
//        WeatherService weatherService = new WeatherService();
//
//        String city = "Moscow";
//        String mockResponse = Files.readString(Path.of("src/test/resources/moscow_response.json"));
//
//        server.enqueue(new MockResponse()
//                .setResponseCode(200)
//                .setBody(mockResponse));
//
//        String baseUrl = server.url("/weather").toString();
//        System.setProperty("weather.api.base-url", baseUrl);
//
//        assertThatThrownBy(() -> weatherService.searchCity(city))
//                .isInstanceOf(IllegalStateException.class)
//                .hasMessage("API ключ не корректный");
//    }
//
//    @Test
//    void searchCityStatusCode404() throws Exception {
//        String city = "Moscow-invalid";
//        String mockResponse = Files.readString(Path.of("src/test/resources/moscow_response.json"));
//
//        server.enqueue(new MockResponse()
//                .setResponseCode(200)
//                .setBody(mockResponse));
//
//        String baseUrl = server.url("/weather").toString();
//        System.setProperty("weather.api.base-url", baseUrl);
//
//
//        assertThatThrownBy(() -> weatherService.searchCity(city))
//                .isInstanceOf(IOException.class)
//                .hasMessage("Не удалось получить данные о погоде: 404");
//
//    }
//
//}
