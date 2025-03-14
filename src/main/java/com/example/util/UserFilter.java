package com.example.util;

import com.example.dao.AuthDao;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component("userFilter")
public class UserFilter implements Filter {

    private final AuthDao authDao;

    @Autowired
    public UserFilter(AuthDao authDao) {
        this.authDao = authDao;
    }


    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        // юзер без авторизации сможет зайти только на эти сайты
        String path = request.getRequestURI();
        if (path.equals("/auth/sign-in") || path.equals("/auth/sign-up")) {
            filterChain.doFilter(request, response);
            return;
        }

        Cookie[] cookies = request.getCookies();
        boolean sessionValid = false;

        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("SESSIONID".equals(c.getName())) {
                    String sessionId = c.getValue();
                    if (authDao.findByUUID(sessionId)) {
                        sessionValid = true;
                        break;
                    }
                }
            }
        }

        if (sessionValid) {
            filterChain.doFilter(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/auth/sign-in");
        }
    }
}
