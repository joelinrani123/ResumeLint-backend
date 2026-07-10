package com.resumelint.config;

import com.resumelint.filter.AuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers CORS (equivalent to Express's {@code app.use(cors())}) and the
 * {@link AuthInterceptor} on exactly the route groups that used
 * {@code requireAuth} in the Node backend: {@code /auth/me}, all of
 * {@code /resumes/**}, and all of {@code /dashboard/**}.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(
                        "/api/auth/me",
                        "/api/resumes",
                        "/api/resumes/**",
                        "/api/dashboard/**");
    }
}
