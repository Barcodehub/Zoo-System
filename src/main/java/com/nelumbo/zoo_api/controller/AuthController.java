package com.nelumbo.zoo_api.controller;


import com.nelumbo.zoo_api.dto.AuthRequestDTO;
import com.nelumbo.zoo_api.dto.AuthResponseDTO;
import com.nelumbo.zoo_api.dto.errors.SuccessResponseDTO;
import com.nelumbo.zoo_api.security.JwtService;
import com.nelumbo.zoo_api.services.AuthService;
import com.nelumbo.zoo_api.services.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SessionService sessionService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<SuccessResponseDTO<AuthResponseDTO>> login(
            @Valid @RequestBody AuthRequestDTO request, @RequestHeader("X-Device-Id") String deviceId) {
        return authService.authenticate(request, deviceId);
    }

    @PostMapping("/logout")
    public ResponseEntity<SuccessResponseDTO<String>> logout(
            HttpServletRequest request,
            @RequestHeader("X-Device-Id") String deviceId) {

        String authHeader = request.getHeader("Authorization");
        String token = authHeader != null && authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : null;

        return authService.logout(token, deviceId);
    }
}