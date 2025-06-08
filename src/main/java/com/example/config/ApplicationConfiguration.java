package com.example.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan("com.example")
@PropertySource("classpath:application.properties")
@Import({DatabaseConfiguration.class, FlywayConfiguration.class, MvcConfiguration.class})
public class ApplicationConfiguration {}