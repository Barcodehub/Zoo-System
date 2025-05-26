package com.nelumbo.zoo_api.services;


import com.nelumbo.zoo_api.dto.AuthRequestDTO;
import com.nelumbo.zoo_api.dto.AuthResponseDTO;
import com.nelumbo.zoo_api.dto.errors.SuccessResponseDTO;
import com.nelumbo.zoo_api.exception.AuthenticationFailedException;
import com.nelumbo.zoo_api.models.User;
import com.nelumbo.zoo_api.repository.UserRepository;
import com.nelumbo.zoo_api.security.JwtService;
import jakarta.validation.Valid;
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
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<SuccessResponseDTO<AuthResponseDTO>> authenticate(@Valid AuthRequestDTO request) {
        try {

            User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new AuthenticationFailedException("Usuario NO encontrado para ese E-mail", "email"));

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );

            String jwtToken = jwtService.generateToken(user);

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
}
