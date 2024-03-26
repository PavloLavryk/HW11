package org.example;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

@WebServlet("/time")
public class TimeServlet extends HttpServlet {

    private TemplateEngine templateEngine;

    @Override
    public void init() {
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(getServletContext());
        templateResolver.setPrefix("src/main/resources/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML5");

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String timezoneParam = req.getParameter("timezone");
        String timezone = Optional.ofNullable(timezoneParam)
                .orElseGet(() -> getCookieValue(req, "lastTimezone").orElse("UTC"));

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(timezone));
        String time = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));

        WebContext context = new WebContext(req, resp, req.getServletContext());
        context.setVariable("time", time);

        resp.setContentType("text/html;charset=UTF-8");
        templateEngine.process("time", context, resp.getWriter());

        if (timezoneParam != null) {
            setCookie(resp, "lastTimezone", timezone, 60 * 60 * 24 * 365);  // 1 year
        }
    }

    private Optional<String> getCookieValue(HttpServletRequest req, String name) {
        return Optional.ofNullable(req.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies).filter(c -> c.getName().equals(name)).findFirst())
                .map(Cookie::getValue);
    }

    private void setCookie(HttpServletResponse resp, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        resp.addCookie(cookie);
    }
}
