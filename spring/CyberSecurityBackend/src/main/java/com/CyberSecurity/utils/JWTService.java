package com.CyberSecurity.utils;

import com.CyberSecurity.model.Account;
import com.CyberSecurity.model.RefreshToken;
import com.CyberSecurity.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JWTService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access_expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh_expiration}")
    private long refreshExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    private Key getSigningKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractAccountType(String token){
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    public String extractEmail(String token){
        return extractClaim(token, Claims::getSubject);
    }

    public String generateAccessToken(Account account){
        return generateToken(account, accessExpiration);
    }

    @Transactional
    public String generateRefreshToken(Account account) {
        refreshTokenRepository.deleteByAccount(account);
        refreshTokenRepository.flush();
        String token = generateToken(account, refreshExpiration);
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .expiryDate(new Date(System.currentTimeMillis() + refreshExpiration))
                .account(account)
                .build();
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    public String generateToken(Account account, long tokenExpiration){
        return Jwts.builder()
                .setSubject(account.getEmail())
                .claim("id", account.getId())
                .claim("type", account.getAccountType())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails){
        try {
            Account account = (Account) userDetails;
            Claims claims = extractAllClaims(token);
            String email = claims.getSubject();

            if (email == null || !email.equals(account.getEmail())) {
                return false;
            }

            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getToken(HttpServletRequest request){
        if (request.getCookies() != null){
            for (Cookie cookie : request.getCookies()){
                if ("jwt".equals(cookie.getName())){
                    return cookie.getValue();
                }
            }
        }
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")){
            return authHeader.substring(7);
        }

        return null;
    }
}
