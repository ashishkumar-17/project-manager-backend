package com.projectmanager.security.jwt;

import com.projectmanager.service.Impl.UserDetailsImpl;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    @Value("${spring.security.jwt.secret}")
    private String jwtSecret;

    @Value("${spring.security.jwt.expiration}")
    private long jwtExpiration;

    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if(bearerToken != null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }

    public String generateToken(UserDetailsImpl user) {
        String username = user.getUsername();
        String roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    private Key getSigningKey(){
        return Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtSecret));
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith((SecretKey) getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}