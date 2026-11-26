package com.shopflow.user.service.impl;

import com.shopflow.user.dto.AuthResponse;
import com.shopflow.user.dto.LoginRequest;
import com.shopflow.user.dto.RegisterRequest;
import com.shopflow.user.entity.Role;
import com.shopflow.user.entity.User;
import com.shopflow.user.exception.DuplicateEmailException;
import com.shopflow.user.repository.UserRepository;
import com.shopflow.user.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_shouldNormalizeEmailHashPasswordAndReturnToken() {
        RegisterRequest request = new RegisterRequest("Aarav Sharma", "  Aarav@ShopFlow.com ", "Password123!", "9876543210");

        when(userRepository.existsByEmail("aarav@shopflow.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("mock-jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("mock-jwt-token");
        assertThat(response.email()).isEqualTo("aarav@shopflow.com");
        assertThat(response.role()).isEqualTo("CUSTOMER");
        verify(passwordEncoder).encode("Password123!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowDuplicateEmailException_whenEmailExists() {
        RegisterRequest request = new RegisterRequest("Aarav Sharma", "aarav@shopflow.com", "Password123!", null);
        when(userRepository.existsByEmail("aarav@shopflow.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("aarav@shopflow.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldAuthenticateAndReturnToken() {
        LoginRequest request = new LoginRequest("Aarav@ShopFlow.com", "Password123!");
        User user = new User();
        user.setId(1L);
        user.setEmail("aarav@shopflow.com");
        user.setFullName("Aarav Sharma");
        user.setRole(Role.CUSTOMER);

        when(userRepository.findByEmail("aarav@shopflow.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("login-jwt-token");

        AuthResponse response = authService.login(request);

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("aarav@shopflow.com", "Password123!")
        );
        assertThat(response.accessToken()).isEqualTo("login-jwt-token");
        assertThat(response.userId()).isEqualTo(1L);
    }
}