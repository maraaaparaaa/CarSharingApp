package com.carsharing.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value; //annotation for immutable classes, final fields
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service for JWT tokens:
 * - Token generation
 * - Token validation
 * - Info extraction from token
 */

@Service
public class JwtService {

    // Inject application properties
    @Value("${jwt.expiration}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Extracts email (subject) from token
     */
    public String extractEmail(String token){
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts any claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates token for user
     */
    public String generateToken(UserDetails userDetails){
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates token with custom claims
     * @param extraClaims - extra info (id, role , .. )
     * @param userDetails - user info from Spring Security
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails){
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Builds JWT token
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ){
        return Jwts
                .builder()
                .setClaims(extraClaims)              // Claims custom (id, role)
                .setSubject(userDetails.getUsername()) // Subject = email
                .setIssuedAt(new Date(System.currentTimeMillis())) // when was it created
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // when does it expire
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // signature
                .compact();
    }

    /**
     * Validates token:
     * 1. Does the email from token correspond with the user?
     * 2. Is token expired?
     */
    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username = extractEmail(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Converts secret key from String in Key object for signature
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
