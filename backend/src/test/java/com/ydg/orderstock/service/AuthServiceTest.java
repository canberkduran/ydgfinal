package com.ydg.orderstock.service;

import com.ydg.orderstock.dto.RegisterRequest;
import com.ydg.orderstock.repository.CartRepository;
import com.ydg.orderstock.repository.UserRepository;
import com.ydg.orderstock.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthServiceTest {
    private UserRepository userRepository;
    private CartRepository cartRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setup() {
        userRepository = Mockito.mock(UserRepository.class);
        cartRepository = Mockito.mock(CartRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        jwtService = Mockito.mock(JwtService.class);
        authService = new AuthService(userRepository, cartRepository, passwordEncoder, jwtService);
    }

    @Test
    void registerCreatesUserWithBcryptPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setName("Test");
        request.setPassword("secret123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken("test@example.com")).thenReturn("token");

        authService.register(request);

        ArgumentCaptor<com.ydg.orderstock.entity.AppUser> captor = ArgumentCaptor.forClass(com.ydg.orderstock.entity.AppUser.class);
        verify(userRepository).save(captor.capture());
        assertThat(passwordEncoder.matches("secret123", captor.getValue().getPasswordHash())).isTrue();
    }
}
