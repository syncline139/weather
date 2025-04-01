package com.example.config;

import com.example.dao.AuthDao;
import com.example.services.AuthService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class AppStartupListener implements ServletContextListener {

    private AuthDao authDao;
    private AuthService authService;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        var context = WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());
        authDao = context.getBean(AuthDao.class);
        authService = context.getBean(AuthService.class);
        authDao.deleteAllSessions();
        System.out.println("Все сессии очищены при запуске Tomcat");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (authService != null) {
            authService.setShuttingDown(true);
        }
    }
}