package com.example.config;

import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

public class WeatherInitializer implements WebApplicationInitializer {

    public static final String USER_FILTER = "userFilter";

    @Override
    public void onStartup(ServletContext servletContext){
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(com.example.config.ApplicationConfiguration.class);

        servletContext.addListener(new ContextLoaderListener(context));
        servletContext.addListener(new OnStartupSessionCleaner());  // включаем в жизненный цикл очиститель сессий

        FilterRegistration.Dynamic userFilter = servletContext.addFilter(USER_FILTER,
                new DelegatingFilterProxy(USER_FILTER));
        userFilter.addMappingForUrlPatterns(null, false, "/*");
    }
}