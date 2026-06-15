package org.isaac.com.ecommers.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${spring.jwt.secret}")
    private String secret;

    public SecretKey getSigninKey(){
        byte[] keyByte = Decoders.BASE64.decode(this.secret);
        return Keys.hmacShaKeyFor(keyByte);
    }

    public String generateToken(String email){
        long timeNowMillis = System.currentTimeMillis();
        Date timeNow = new Date(timeNowMillis);

        long expirationMillis = timeNowMillis + 3600000;
        Date expiration = new Date(expirationMillis);

        return Jwts.builder().subject(email)
                .issuedAt(timeNow)
                .expiration(expiration)
                .signWith(getSigninKey())
                .compact();
    }

    public String extractUsername(String token){
        return extractClaims(token, Claims::getSubject);
    }

    public boolean isExpiredToken(String token){
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token){
        return extractClaims(token, Claims::getExpiration);
    }

    public <T> T extractClaims(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(getSigninKey()) // Se obtiene el secret
                .build()
                .parseSignedClaims(token) // Se valida el firma
                .getPayload(); //Se obtiene el cuerpo del token
    }

    public boolean isValidToken(String token,  UserDetails userDetails){
        try{
            Jwts.parser()
                    .verifyWith(getSigninKey())
                    .build()
                    .parseSignedClaims(token);
            final String userFromToken = extractUsername(token);
            return(userFromToken.equals(userDetails.getUsername()) && !isExpiredToken(token));
        }catch (ExpiredJwtException e){
            System.out.println("The token has expired");
            return false;
        }
    }
}
