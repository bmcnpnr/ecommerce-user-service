package com.ecommerce.user.controller;

import com.ecommerce.user.dto.*;
import com.ecommerce.user.exception.InvalidCredentialsException;
import com.ecommerce.user.exception.UserAlreadyExistsException;
import com.ecommerce.user.exception.UserNotFoundException;
import com.ecommerce.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private UserService userService;

    @Test
    void register_returns201() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser").email("test@example.com").password("password123").build();
        UserDTO response = UserDTO.builder()
                .id(1L).username("testuser").email("test@example.com").role("CUSTOMER").status("ACTIVE")
                .createdAt(LocalDateTime.now()).build();

        when(userService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void register_duplicateUsername_returns409() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("existing").email("email@example.com").password("password123").build();

        when(userService.register(any())).thenThrow(new UserAlreadyExistsException("Username already taken"));

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_returns200WithToken() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "password123");
        AuthResponse response = AuthResponse.builder()
                .token("jwt.token.here").expiresIn(3600000L).username("testuser").role("CUSTOMER").build();

        when(userService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.here"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        LoginRequest request = new LoginRequest("user", "wrongpass");
        when(userService.login(any())).thenThrow(new InvalidCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserById_found_returns200() throws Exception {
        UserDTO response = UserDTO.builder().id(1L).username("testuser").role("CUSTOMER").status("ACTIVE")
                .createdAt(LocalDateTime.now()).build();
        when(userService.getUserById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getUserById_notFound_returns404() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new UserNotFoundException("User not found with id: 99"));

        mockMvc.perform(get("/api/v1/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void register_invalidInput_returns400() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("ab") // too short
                .email("not-an-email")
                .password("short") // too short
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
