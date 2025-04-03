package com.example.filters;

import com.example.dao.AuthDao;
import com.example.models.Sessions;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component("userFilter")
@RequiredArgsConstructor
public class UserFilter implements Filter {

    private final AuthDao authDao;


    /**
     * Фильтр для проверки аутентификации юзера
     * Данный фильтр выполняет следующие действия:
     * -> Проверяет наличие cookie с именем "SESSIONID" и валидирует сессию
     * -> Не дает юзеру зайти на страницу регестрации или авторизации если тот аутентифицирован
     * -> Не дает юзера зайти на на страници пока тот не аутентифицирован
     *
     * @param servletRequest  запрос
     * @param servletResponse ответ
     * @param filterChain     цепочка фильтров через которую передается запрос
     * @throws IOException      в случае ошибки ввода/вывода
     * @throws ServletException в случае ошибки сервлета
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = request.getRequestURI().substring(request.getContextPath().length());

        if (path.startsWith("/css/") || path.startsWith("/images/")) {
            filterChain.doFilter(request, response);
            return;
        }

        Cookie[] cookies = request.getCookies();
        boolean authenticatedValid = false;
        String sessionId = null;

        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("SESSIONID".equals(c.getName())) {
                    sessionId = c.getValue();
                    if (authDao.findByUUID(sessionId)) {
                        authenticatedValid = true;
                        // Восстанавливаем атрибуты сессии
                        Sessions session = authDao.findSessionByUUID(UUID.fromString(sessionId.toString())); // Предполагается, что у тебя есть такой метод
                        if (session != null && session.getUser() != null) {
                            request.getSession().setAttribute("id", session.getUser().getId());
                            request.getSession().setAttribute("login", session.getUser().getLogin());
                        }
                        break;
                    } else {
                        Cookie cookie = new Cookie("SESSIONID", null);
                        cookie.setPath("/");
                        cookie.setMaxAge(0);
                        response.addCookie(cookie);
                    }
                }
            }
        }

        if (authenticatedValid && (path.equals("/auth/sign-in") || path.equals("/auth/sign-up"))) {
            response.sendRedirect(request.getContextPath() + "/weather");
            return;
        }

        if (authenticatedValid && path.equals("/")) {
            response.sendRedirect(request.getContextPath() + "/weather");
            return;
        }

        if (!authenticatedValid && !(path.equals("/auth/sign-in") || path.equals("/auth/sign-up"))) {
            response.sendRedirect(request.getContextPath() + "/auth/sign-in");
            return;
        }


        if (!response.isCommitted()) {
            filterChain.doFilter(request, response);
        }
    }

}
