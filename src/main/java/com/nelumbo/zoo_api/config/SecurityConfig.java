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
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.List;
import static com.nelumbo.zoo_api.constants.ApiPaths.*;
import static com.nelumbo.zoo_api.constants.Roles.ADMIN;
import static com.nelumbo.zoo_api.constants.Roles.EMPLOYEE;


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
                .csrf(AbstractHttpConfigurer::disable)
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
                                .requestMatchers(HttpMethod.POST, REGISTRO).hasAuthority(ADMIN) //hasAnyAuthority


                                .requestMatchers(HttpMethod.GET, ANIMALES).hasAnyAuthority(ADMIN, EMPLOYEE)
                                .requestMatchers(HttpMethod.POST, ANIMALES).hasAuthority(ADMIN)
                                .requestMatchers(HttpMethod.PUT, ANIMALES).hasAuthority(ADMIN)
                                .requestMatchers(HttpMethod.DELETE, ANIMALES).hasAuthority(ADMIN)

                                .requestMatchers(HttpMethod.GET, ESPECIES).hasAnyAuthority(ADMIN, EMPLOYEE)
                                .requestMatchers(HttpMethod.POST, ESPECIES).hasAuthority(ADMIN)
                                .requestMatchers(HttpMethod.PUT, ESPECIES).hasAuthority(ADMIN)
                                .requestMatchers(HttpMethod.DELETE, ESPECIES).hasAuthority(ADMIN)

                                .requestMatchers(HttpMethod.GET, ZONAS).hasAnyAuthority(ADMIN, EMPLOYEE)
                                .requestMatchers(HttpMethod.POST, ZONAS).hasAuthority(ADMIN)
                                .requestMatchers(HttpMethod.PUT, ZONAS).hasAuthority(ADMIN)
                                .requestMatchers(HttpMethod.DELETE, ZONAS).hasAuthority(ADMIN)

                                .requestMatchers(HttpMethod.GET, COMENTARIO).hasAnyAuthority(ADMIN, EMPLOYEE)
                                .requestMatchers(HttpMethod.POST, COMENTARIOS).hasAnyAuthority(ADMIN, EMPLOYEE)
                                .requestMatchers(HttpMethod.DELETE, COMENTARIOS).hasAnyAuthority(ADMIN, EMPLOYEE)

                                .requestMatchers(HttpMethod.GET, ESTADISTICAS).hasAuthority(ADMIN)
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