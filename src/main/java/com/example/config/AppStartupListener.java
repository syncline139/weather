package com.example.config;

import com.example.dao.AuthDao;
import com.example.services.AuthServices;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class AppStartupListener implements ServletContextListener {

    private AuthDao authDao;
    private AuthServices authServices;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        var context = WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());
        authDao = context.getBean(AuthDao.class);
        authServices = context.getBean(AuthServices.class);
        authDao.deleteAllSessions();
        System.out.println("Все сессии очищены при запуске Tomcat!");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (authServices != null) {
            authServices.setShuttingDown(true);
        }
        System.out.println("Приложение остановлено.");
    }
}