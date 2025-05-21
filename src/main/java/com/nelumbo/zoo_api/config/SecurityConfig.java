package com.nelumbo.zoo_api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nelumbo.zoo_api.dto.errors.ErrorDetailDTO;
import com.nelumbo.zoo_api.dto.errors.ErrorResponseDTO;
import com.nelumbo.zoo_api.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        return http
                .csrf(csrf ->
                        csrf.disable())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler(customAccessDeniedHandler())
                        .authenticationEntryPoint(authenticationEntryPoint())
                )
                .authorizeHttpRequests(authRequest ->
                        authRequest
                                .requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers(
                                        "/docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/v3/api-docs/**",
                                        "/v3/api-docs",
                                        "/swagger-resources/**",
                                        "/webjars/**"
                                ).permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/users").hasAuthority("ROLE_ADMIN") //hasAnyAuthority


                                .requestMatchers(HttpMethod.GET, "/api/animals/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLOYEE")
                                .requestMatchers(HttpMethod.POST, "/api/animals/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/animals/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/animals/**").hasAuthority("ROLE_ADMIN")

                                .requestMatchers(HttpMethod.GET, "/api/species/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLOYEE")
                                .requestMatchers(HttpMethod.POST, "/api/species/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/species/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/species/**").hasAuthority("ROLE_ADMIN")

                                .requestMatchers(HttpMethod.GET, "/api/zones/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLOYEE")
                                .requestMatchers(HttpMethod.POST, "/api/zones/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/zones/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/zones/**").hasAuthority("ROLE_ADMIN")

                                .requestMatchers(HttpMethod.GET, "/api/comments/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLOYEE")
                                .requestMatchers(HttpMethod.DELETE, "/api/comments/**").hasAuthority("ROLE_ADMIN")

                                .requestMatchers(HttpMethod.GET, "/api/stats/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLOYEE")
                                .requestMatchers(HttpMethod.POST, "/api/stats/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLOYEE")
                                .requestMatchers(HttpMethod.PUT, "/api/stats/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLOYEE")
                                .requestMatchers(HttpMethod.DELETE, "/api/stats/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLOYEE")
                                .anyRequest().authenticated()
                )
                .sessionManagement(sessionManager->
                        sessionManager
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();


    }


    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setContentType("application/json");
            response.setStatus(HttpStatus.FORBIDDEN.value());

            ErrorDetailDTO errorDetail = new ErrorDetailDTO(
                    "403",
                    "No tienes permisos para realizar esta acción",
                    null
            );

            ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                    null,
                    List.of(errorDetail)
            );

            response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
        };
    }
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setContentType("application/json");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());

            ErrorDetailDTO errorDetail = new ErrorDetailDTO(
                    "401",
                    "Autenticación requerida",
                    null
            );

            ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                    null,
                    List.of(errorDetail)
            );

            response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
        };
    }

}