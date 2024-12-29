package com.example.LikeLink.Config.Security.Jwt;

import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.util.Date;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.example.LikeLink.Config.Security.Service.UserDetailsImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtTokenProvider {
	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${jwt.accessToken.expiration}")  // Changed from jwt.expiration
	private int accessTokenExpiration;       // Changed variable name

	@Value("${jwt.refreshToken.expiration}") // Changed from jwt.refresh-token.expiration
	private int refreshTokenExpiration;

    public String generateAccessToken(UserDetailsImpl userPrincipal) {
        return generateToken(userPrincipal, accessTokenExpiration);
    }

    public String generateRefreshToken(UserDetailsImpl userPrincipal) {
        return generateToken(userPrincipal, refreshTokenExpiration);
    }

    private String generateToken(UserDetailsImpl userPrincipal, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("roles", userPrincipal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .claim("userId", userPrincipal.getId())
                .signWith(getSigningKey())
                .compact();
    }
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }
}
