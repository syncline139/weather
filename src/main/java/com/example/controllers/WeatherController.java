package com.example.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/weather")
public class WeatherController {

    @GetMapping()
    public String mainScreenPage() {
        return "pages/index";
    }

}
