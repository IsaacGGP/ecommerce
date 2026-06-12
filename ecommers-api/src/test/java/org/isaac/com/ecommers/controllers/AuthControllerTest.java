package org.isaac.com.ecommers.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.units.qual.A;
import org.isaac.com.ecommers.config.JwtAuthenticationFilter;
import org.isaac.com.ecommers.exception.EmailAlreadyExistsException;
import org.isaac.com.ecommers.exception.InvalidCredentialsException;
import org.isaac.com.ecommers.models.dto.*;
import org.isaac.com.ecommers.security.JwtService;
import org.isaac.com.ecommers.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createUser_ShouldCreateUser_WhenDatesAreValid() throws Exception{
        CreateUserRequest request = new CreateUserRequest("Kim", "Kim@gmail.com", "12345");
        UserResponse response = new UserResponse(1L, "Kim", "Kim@gmail.com", "USER");
        String json = objectMapper.writeValueAsString(request);

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Kim"))
                .andExpect(jsonPath("$.email").value("Kim@gmail.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).createUser(any(CreateUserRequest.class));
    }

    @Test
    void createUser_ShouldThrowException_WhenEmailAlreadyExist() throws Exception{
        CreateUserRequest request = new CreateUserRequest("Kim", "Kim@gmail.com", "12345");
        String json = objectMapper.writeValueAsString(request);

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("Email already exists"));
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // Verifica que devuelva un JSON
                .andExpect(jsonPath("$.message").value("Email already exists"));
        verify(userService).createUser(any(CreateUserRequest.class));
    }

    @Test
    void loginUser_ShouldLoginUser_WhenCredentialsAreValid() throws Exception{
        LoginRequest request = new LoginRequest("Kim@gmail.com", "12345");
        LoginResponse response = new LoginResponse("JWT_TOKEN", "Login successful");
        String json = objectMapper.writeValueAsString(request);

        when(userService.loginUser(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("JWT_TOKEN"))
                .andExpect(jsonPath("$.message").value("Login successful"));

        verify(userService).loginUser(any(LoginRequest.class));
    }

    @Test
    void loginUser_ShouldReturn401_WhenCredentialsAreNotValid() throws Exception{
        LoginRequest request = new LoginRequest("Kim@gmail.com", "12345");
        String json = objectMapper.writeValueAsString(request);

        when(userService.loginUser(any(LoginRequest.class))).thenThrow(new InvalidCredentialsException("Invalid Credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid Credentials"));

        verify(userService).loginUser(any(LoginRequest.class));
    }
}
