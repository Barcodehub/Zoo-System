package com.nelumbo.zoo_api.unit;

import com.nelumbo.zoo_api.dto.AuthRequestDTO;
import com.nelumbo.zoo_api.dto.AuthResponseDTO;
import com.nelumbo.zoo_api.dto.errors.SuccessResponseDTO;
import com.nelumbo.zoo_api.exception.AuthenticationFailedException;
import com.nelumbo.zoo_api.models.Role;
import com.nelumbo.zoo_api.models.User;
import com.nelumbo.zoo_api.repository.UserRepository;
import com.nelumbo.zoo_api.security.JwtService;
import com.nelumbo.zoo_api.services.AuthService;
import com.nelumbo.zoo_api.services.SessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private SessionService sessionService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void authenticate_WhenValidCredentials_ShouldReturnToken() {
        // Arrange
        String email = "user@example.com";
        String password = "password";
        String deviceId = "device-123";
        String token = "mock.jwt.token";

        AuthRequestDTO request = new AuthRequestDTO(email, password);
        User mockUser = new User(1, "Test User", email, "encodedPass", Role.EMPLOYEE);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(jwtService.generateToken(mockUser, deviceId)).thenReturn(token);

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        // Act
        ResponseEntity<SuccessResponseDTO<AuthResponseDTO>> response =
                authService.authenticate(request, deviceId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        AuthResponseDTO responseBody = Objects.requireNonNull(response.getBody()).data();
        assertEquals(token, responseBody.token());
        assertEquals(email, responseBody.email());
        assertEquals(Role.EMPLOYEE, responseBody.role());

        verify(userRepository).findByEmail(email);
        verify(jwtService).generateToken(mockUser, deviceId);
        verify(authenticationManager).authenticate(any());
        verify(sessionService).createSession(email, deviceId, token);
    }

    @Test
    void authenticate_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        String deviceId = "device-123";
        AuthRequestDTO request = new AuthRequestDTO("nonexistent@example.com", "password");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        AuthenticationFailedException exception = assertThrows(
                AuthenticationFailedException.class,
                () -> authService.authenticate(request, deviceId)
        );

        assertEquals("Usuario NO encontrado para ese E-mail", exception.getMessage());
        assertEquals("email", exception.getField());

        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void authenticate_WhenBadCredentials_ShouldThrowException() {
        // Arrange
        String deviceId = "device-123";
        AuthRequestDTO request = new AuthRequestDTO("user@example.com", "wrongpass");
        User mockUser = new User(1, "Test User", "user@example.com", "encodedPass", Role.EMPLOYEE);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        AuthenticationFailedException exception = assertThrows(
                AuthenticationFailedException.class,
                () -> authService.authenticate(request, deviceId)
        );

        assertEquals("Credenciales inválidas", exception.getMessage());
        assertEquals("password", exception.getField());

        verify(userRepository).findByEmail("user@example.com");
        verify(authenticationManager).authenticate(any());
        verify(jwtService, never()).generateToken(any());
    }
}