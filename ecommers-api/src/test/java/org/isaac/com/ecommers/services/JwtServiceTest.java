package org.isaac.com.ecommers.services;

import org.isaac.com.ecommers.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {
    private JwtService jwtService;

    @BeforeEach
    void setUp(){
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService,
                "secret",
                "c2VjcmV0S2V5MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkw");
    }

    @Test
    void extractUsername_ShouldReturnEmailFromToken(){
        String token = jwtService.generateToken("julieta@gmail.com");
        String username = jwtService.extractUsername(token);

        assertEquals("julieta@gmail.com", username);
    }

    //Valida un token que fue recientemente generado
    @Test
    void isExpiredToken_ShouldReturnFalse_WhenTokenIsNew(){
        String token = jwtService.generateToken("julieta@gmail.com");
        assertFalse(jwtService.isExpiredToken(token));
    }

    @Test
    void isValidToken_ShouldReturnTrue_WhenTokenBelongsToUser(){
        UserDetails user = User.withUsername("julieta@gmail.com")
                .password("12345")
                .authorities("USER")
                .build();

        String token = jwtService.generateToken("julieta@gmail.com");
        assertTrue(jwtService.isValidToken(token, user));
    }

    @Test
    void isValidToken_ShouldReturnFalse_WhenTokenBelongsToAnotherUser(){
        UserDetails user = User.withUsername("yuli@gmail.com")
                .password("12345")
                .authorities("USER")
                .build();
        String token = jwtService.generateToken("julieta@gmail.com");
        assertFalse(jwtService.isValidToken(token, user));
    }

    //Test que valida que la fecha de expiracion este realmente en el futuro
    @Test
    void extractExpiration_ShouldReturnFutureDate(){
        String token = jwtService.generateToken("julieta@gmail.com");
        Date expiration = jwtService.extractExpiration(token);

        assertTrue(expiration.after(new Date()));
    }

}
