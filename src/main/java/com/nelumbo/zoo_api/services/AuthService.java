package com.nelumbo.zoo_api.services;


import com.nelumbo.zoo_api.dto.AuthRequestDTO;
import com.nelumbo.zoo_api.dto.AuthResponseDTO;
import com.nelumbo.zoo_api.dto.errors.SuccessResponseDTO;
import com.nelumbo.zoo_api.exception.AuthenticationFailedException;
import com.nelumbo.zoo_api.models.User;
import com.nelumbo.zoo_api.repository.UserRepository;
import com.nelumbo.zoo_api.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final SessionService sessionService;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<SuccessResponseDTO<AuthResponseDTO>> authenticate(@Valid AuthRequestDTO request, @NotBlank String deviceId) {
        try {

            User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new AuthenticationFailedException("Usuario NO encontrado para ese E-mail", "email"));

            if (deviceId == null || deviceId.trim().isEmpty()) {
                throw new IllegalArgumentException("Device ID es requerido");
            }

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );

            String jwtToken = jwtService.generateToken(user, deviceId);

            // Crear nueva sesión
            sessionService.createSession(user.getEmail(), deviceId, jwtToken);

            AuthResponseDTO authResponse = new AuthResponseDTO(
                    jwtToken,
                    user.getEmail(),
                    user.getRole()
            );



            return ResponseEntity.ok(new SuccessResponseDTO<>(authResponse));


        } catch (BadCredentialsException e) {
            throw new AuthenticationFailedException("Credenciales inválidas", "password");
        } catch (AuthenticationFailedException e) {
            throw e;
        }
    }



    public ResponseEntity<SuccessResponseDTO<String>> logout(String token, @NotBlank String deviceId) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token no proporcionado");
        }

        if (deviceId == null || deviceId.isBlank()) {
            throw new IllegalArgumentException("Device ID es requerido");
        }

        String username = jwtService.extractUsername(token);

        if (!sessionService.hasActiveSession(username, deviceId)) {
            throw new IllegalStateException("No existe sessión para este dispositivo");
        }

        sessionService.invalidateSessionForDevice(username, deviceId);

        return ResponseEntity.ok(new SuccessResponseDTO<>("Logout exitoso para este dispositivo"));
    }

}
