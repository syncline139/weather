package com.example.util;

import com.example.dao.AuthDao;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component("userFilter")
@RequiredArgsConstructor
public class UserFilter implements Filter {

    private final AuthDao authDao;



    /**
     * Фильтр для проверки аутентификации юзера
     * Данный фильтр выполняет следующие действия:
     *  -> Проверяет наличие cookie с именем "SESSIONID" и валидирует сессию
     *  -> Не дает юзеру зайти на страницу регестрации или авторизации если тот аутентифицирован
     *  -> Не дает юзера зайти на на страници пока тот не аутентифицирован
     * @param servletRequest  запрос
     * @param servletResponse ответ
     * @param filterChain цепочка фильтров через которую передается запрос
     * @throws IOException в случае ошибки ввода/вывода
     * @throws ServletException в случае ошибки сервлета
     */
    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = request.getRequestURI().substring(request.getContextPath().length());

        Cookie[] cookies = request.getCookies();
        boolean authenticatedValid = false;
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("SESSIONID".equals(c.getName())) {
                    String sessionId = c.getValue();
                    if (authDao.findByUUID(sessionId)) {
                        authenticatedValid = true;
                        break;
                    }
                }
            }
        }

        if (authenticatedValid && (path.equals("/auth/sign-in") || path.equals("/auth/sign-up"))) {
            response.sendRedirect(request.getContextPath() + "/weather");
            return;
        }

        if (!authenticatedValid && !(path.equals("/auth/sign-in") || path.equals("/auth/sign-up"))) {
            response.sendRedirect(request.getContextPath() + "/auth/sign-in");
            return;
        }

        filterChain.doFilter(request, response);
    }

}
