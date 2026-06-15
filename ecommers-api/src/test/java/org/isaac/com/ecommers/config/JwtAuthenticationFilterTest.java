package org.isaac.com.ecommers.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.isaac.com.ecommers.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {
    @Mock
    private JwtService jwtService;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private FilterChain filterChain;
    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private JwtAuthenticationFilter jwtFilter;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp(){
        request = new MockHttpServletRequest();
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_ShouldContinueChain_WhenHeaderIsMissing() throws Exception{
        jwtFilter.doFilter(request, response, filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    //Header sin bearer
    @Test
    void doFilterInternal_ShoulContinueChain_WhenHeaderIsInvalid() throws Exception{
        request.addHeader("Authorization", "Basic abs123");
        jwtFilter.doFilter(request, response, filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldAuthenticateUser_WhenTokenIsValid() throws Exception{
        request.addHeader("Authorization", "Bearer TOKEN");
        UserDetails userDetails = User.withUsername("raul@gmail.com")
                .password("12345")
                .authorities("ROLE_USER")
                .build();

        when(jwtService.extractUsername("TOKEN")).thenReturn("raul@gmail.com");
        when(userDetailsService.loadUserByUsername("raul@gmail.com")).thenReturn(userDetails);
        when(jwtService.isValidToken("TOKEN", userDetails)).thenReturn(true);

        jwtFilter.doFilter(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(auth);
        assertEquals("raul@gmail.com", userDetails.getUsername());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFIlterInternal_ShouldNotAuthenticate_WhenTokenIsInvalid() throws Exception{
        request.addHeader("Authorization", "Bearer TOKEN");
        UserDetails userDetails = User.withUsername("raul@gmail.com")
                .password("12345")
                .authorities("ROLE_USER")
                .build();

        when(jwtService.extractUsername("TOKEN")).thenReturn("raul@gmail.com");
        when(userDetailsService.loadUserByUsername("raul@gmail.com")).thenReturn(userDetails);
        when(jwtService.isValidToken("TOKEN", userDetails)).thenReturn(false);

        jwtFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
