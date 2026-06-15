package org.isaac.com.ecommers.services;

import org.isaac.com.ecommers.exception.InvalidCredentialsException;
import org.isaac.com.ecommers.exception.UserNotFoundException;
import org.isaac.com.ecommers.models.UserEntity;
import org.isaac.com.ecommers.repositories.UserRepository;
import org.isaac.com.ecommers.security.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {
    @Mock
    UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists(){
        UserEntity user = new UserEntity();
        user.setEmail("Carla@gmail.com");
        user.setPassword("HASHED_PASSWORD");
        user.setRole("USER");

        when(userRepository.findByEmail("Carla@gmail.com")).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername("Carla@gmail.com");

        assertEquals("Carla@gmail.com", result.getUsername());
        assertEquals("HASHED_PASSWORD", result.getPassword());

        assertTrue(result.getAuthorities().stream()
                .anyMatch(autority -> autority.getAuthority().equals("ROLE_USER")));
        verify(userRepository).findByEmail("Carla@gmail.com");
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserDoesNotExists(){
        when(userRepository.findByEmail("Carla@gmail.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername("Carla@gmail.com"));
        verify(userRepository).findByEmail("Carla@gmail.com");
    }
}
