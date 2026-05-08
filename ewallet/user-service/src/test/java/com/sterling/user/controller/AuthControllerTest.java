package com.sterling.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sterling.user.dto.AuthResponse;
import com.sterling.user.dto.LoginRequest;
import com.sterling.user.dto.RegisterRequest;
import com.sterling.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("Auth Controller Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    // MockBean the security beans required by the auto-configuration
    @MockBean
    private com.sterling.user.service.JwtService jwtService;

    @MockBean
    private com.sterling.user.repository.UserRepository userRepository;

    @Test
    @DisplayName("POST /api/auth/register - 201 Created with valid input")
    @WithMockUser
    void register_Returns201() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Test User")
                .email("test@sterling.com")
                .password("Password@123")
                .phone("9876543299")
                .build();

        AuthResponse mockResponse = AuthResponse.builder()
                .token("mock.jwt.token")
                .userId(1L)
                .email("test@sterling.com")
                .role("USER")
                .build();

        when(userService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mock.jwt.token"))
                .andExpect(jsonPath("$.email").value("test@sterling.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("POST /api/auth/register - 400 Bad Request when email is invalid")
    @WithMockUser
    void register_Returns400_WhenEmailInvalid() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Test User")
                .email("not-an-email")
                .password("Password@123")
                .phone("9876543299")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - 200 OK with valid credentials")
    @WithMockUser
    void login_Returns200() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@sterling.com")
                .password("Password@123")
                .build();

        AuthResponse mockResponse = AuthResponse.builder()
                .token("mock.jwt.token")
                .userId(1L)
                .email("test@sterling.com")
                .role("USER")
                .build();

        when(userService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock.jwt.token"));
    }

    @Test
    @DisplayName("POST /api/auth/register - 400 Bad Request when password too short")
    @WithMockUser
    void register_Returns400_WhenPasswordTooShort() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Test User")
                .email("test@sterling.com")
                .password("short")
                .phone("9876543299")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
