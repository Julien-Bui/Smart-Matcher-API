package com.smartmatcher.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import java.util.Arrays;

@Configuration
public class WebConfig {

    @Value("${cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(Arrays.asList("*"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }

    @Bean
    public org.springframework.web.filter.OncePerRequestFilter securityHeadersFilter() {
        return new org.springframework.web.filter.OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request,
                                            jakarta.servlet.http.HttpServletResponse response,
                                            jakarta.servlet.FilterChain filterChain)
                    throws jakarta.servlet.ServletException, java.io.IOException {
                
                // Anti-Clickjacking
                response.setHeader("X-Frame-Options", "DENY");
                
                // Anti-MIME Sniffing
                response.setHeader("X-Content-Type-Options", "nosniff");
                
                // Force HTTPS (HSTS) - 1 year
                response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
                
                // Content Security Policy
                // Autorise le chargement depuis le même domaine, et Google Fonts
                String csp = "default-src 'self'; " +
                             "connect-src 'self' https://api.adzuna.com https://smart-matcher-api-production.up.railway.app; " +
                             "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                             "font-src 'self' https://fonts.gstatic.com; " +
                             "script-src 'self' 'unsafe-inline'; " +
                             "img-src 'self' data:;";
                response.setHeader("Content-Security-Policy", csp);

                filterChain.doFilter(request, response);
            }
        };
    }
}
