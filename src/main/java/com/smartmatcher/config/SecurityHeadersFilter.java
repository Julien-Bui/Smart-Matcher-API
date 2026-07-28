package com.smartmatcher.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            httpResponse.setHeader("X-Content-Type-Options", "nosniff");
            httpResponse.setHeader("X-Frame-Options", "DENY");
            httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
            httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

            String csp = "default-src 'self'; " +
                         "connect-src 'self' https://api.adzuna.com https://smart-matcher-api-production.up.railway.app; " +
                         "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                         "font-src 'self' https://fonts.gstatic.com; " +
                         "script-src 'self' 'unsafe-inline'; " +
                         "img-src 'self' data:;";
            httpResponse.setHeader("Content-Security-Policy", csp);
        }

        chain.doFilter(request, response);
    }
}
