package com.challenge.api.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

@Configuration
public class SecurityConfiguration {

    @Bean
    @Profile("!test")
    ApplicationRunner validateWebhookCredentials(Environment environment) {
        return arguments -> {
            requireNonBlank(environment, "spring.security.user.name");
            requireNonBlank(environment, "spring.security.user.password");
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/api/v1/employee/**")
                        .authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/employee")
                        .authenticated()
                        .anyRequest()
                        .denyAll())
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    private void requireNonBlank(Environment environment, String propertyName) {
        if (!StringUtils.hasText(environment.getProperty(propertyName))) {
            throw new IllegalStateException(propertyName + " must be configured");
        }
    }
}