package org.isaac.com.ecommers.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.isaac.com.ecommers.config.JwtAuthenticationFilter;
import org.isaac.com.ecommers.exception.EmailAlreadyExistsException;
import org.isaac.com.ecommers.exception.UserNotFoundException;
import org.isaac.com.ecommers.models.dto.UpdateUserRequest;
import org.isaac.com.ecommers.models.dto.UserResponse;
import org.isaac.com.ecommers.security.JwtService;
import org.isaac.com.ecommers.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {
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
    void findById_ShouldReturnUser_WhenUserExists() throws Exception{
        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setName("Carina");
        response.setEmail("Cari@gmail.com");
        response.setRole("USER");
        when(userService.findById(1L)).thenReturn(response);
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Carina"))
                .andExpect(jsonPath("$.email").value("Cari@gmail.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).findById(1L);
    }

    @Test
    void findById_ShouldReturn404_WhenUserDoesNotExist() throws Exception{
        when(userService.findById(2L)).thenThrow(new UserNotFoundException("User not found"));
        mockMvc.perform(get("/api/users/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenUserExists() throws Exception{
        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setName("Carina");
        response.setEmail("Cari@gmail.com");
        response.setRole("USER");
        when(userService.findByEmail("Cari@gmail.com")).thenReturn(response);
        mockMvc.perform(get("/api/users/email/Cari@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Carina"))
                .andExpect(jsonPath("$.email").value("Cari@gmail.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).findByEmail("Cari@gmail.com");
    }

    @Test
    void findByEmail_ShouldReturn404_WhenUserDoesNotExist() throws Exception{
        when(userService.findByEmail("Ramon@gmail.com")).thenThrow(new UserNotFoundException("User not found"));
        mockMvc.perform(get("/api/users/email/Ramon@gmail.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
        verify(userService).findByEmail("Ramon@gmail.com");
    }

    @Test
    void getAllUsers_ShouldReturnUsers_WhenUsersExist() throws Exception{
        List<UserResponse> usersMocks = List.of(
                new UserResponse(1L, "Ramon", "Ramon@gmail.com", "USER"),
                new UserResponse(2L, "Carina", "Cari@gmail.com", "USER"));
        when(userService.getAllUsers()).thenReturn(usersMocks);
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Ramon"))
                .andExpect(jsonPath("$[1].name").value("Carina"));
        verify(userService).getAllUsers();
    }

    @Test
    void getAllUsers_ShouldReturnEmptyList_WhenNoUsersExist() throws Exception{
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(userService).getAllUsers();
    }

    @Test
    void updateUser_ShouldReturnUpdateUser_WhenTheUpdateIsCorrect() throws Exception{
        UpdateUserRequest request = new UpdateUserRequest("Carina OC", "CariOC1@gmail.com");
        UserResponse response = new UserResponse(2L, "Carina OC", "CariOC1@gmail.com", "USER");

        String json = objectMapper.writeValueAsString(request);

        when(userService.updateUser(eq(2L), any(UpdateUserRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/users/2")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Carina OC"))
                .andExpect(jsonPath("$.email"). value("CariOC1@gmail.com"));
        verify(userService).updateUser(eq(2L), any(UpdateUserRequest.class));
    }

    @Test
    void updateUser_ShouldThrowException_WheUserDoesNotExist() throws Exception{
        UpdateUserRequest request = new UpdateUserRequest("Carina OC", "CariOC1@gmail.com");
        String json = objectMapper.writeValueAsString(request);
        when(userService.updateUser(eq(2L), any(UpdateUserRequest.class))).thenThrow(new UserNotFoundException("User not found"));
        mockMvc.perform(put("/api/users/2").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void updateUser_ShouldThrowException_WhenEmailAlreadyExists() throws Exception{
        UpdateUserRequest request = new UpdateUserRequest("Carina OC", "CariOC1@gmail.com");
        String json = objectMapper.writeValueAsString(request);
        when(userService.updateUser(eq(2L), any(UpdateUserRequest.class))).thenThrow(new EmailAlreadyExistsException("Email Already Exists"));
        mockMvc.perform(put("/api/users/2")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email Already Exists"));
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExist() throws Exception{
        doNothing().when(userService).deleteUser(1L);
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserDoesNotExist() throws Exception{
        doThrow(new UserNotFoundException("User not found")).when(userService).deleteUser(1L);
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(userService).deleteUser(1L);
    }
}
