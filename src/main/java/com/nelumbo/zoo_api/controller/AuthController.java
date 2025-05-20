package com.nelumbo.zoo_api.controller;


import com.nelumbo.zoo_api.dto.AuthRequestDTO;
import com.nelumbo.zoo_api.dto.AuthResponseDTO;
import com.nelumbo.zoo_api.dto.errors.SuccessResponseDTO;
import com.nelumbo.zoo_api.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<SuccessResponseDTO<AuthResponseDTO>> login(
            @Valid @RequestBody AuthRequestDTO request) {
        return authService.authenticate(request);
    }
}