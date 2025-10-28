package nl.hva.elections.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Main security configuration for the application.
 * This is where we disable default security and configure our own rules.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * This is the main security filter chain. It tells Spring Security how to handle requests.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF protection. This is common for APIs that use tokens.
                .csrf(csrf -> csrf.disable())

                // We are using JWT, so we don't need sessions.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Define which endpoints are public and which are protected.
                .authorizeHttpRequests(auth -> auth
                        // IMPORTANT: Allow all requests to our API endpoints for now.
                        // This lets our registration and login work without a token.
                        // We will add more specific rules later.
                        .requestMatchers("/api/**").permitAll()
                        // Allow access to the H2 database console.
                        .requestMatchers("/h2-console/**").permitAll()
                        // Any other request that is not specified must be authenticated.
                        .anyRequest().authenticated()
                );

        // Required for the H2 console to work correctly with Spring Security.
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }

    /**
     * Configures the password encoder bean.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * This configures the CORS settings for the entire application.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // This allows our frontend (running on localhost:5173) to
                // make requests to our backend.
                registry.addMapping("/api/**") // Apply this rule to all API endpoints
                        .allowedOrigins("http://localhost:5173") // Allow requests from this origin
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allow these methods
                        .allowedHeaders("*") // Allow all headers
                        .allowCredentials(true); // Allow sending cookies/credentials
            }
        };
    }
}