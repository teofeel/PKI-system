package com.CyberSecurity.security;

import com.CyberSecurity.utils.JWTService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTAuthFilter extends OncePerRequestFilter {
    private JWTService jwtService;
    private UserDetailsService userDetailsService;
    protected final Log LOGGER = LogFactory.getLog(getClass());
    public JWTAuthFilter(UserDetailsService userDetailsService, JWTService jwtService){
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }
        try{
            String token = authHeader.substring(7);
        }catch (Exception e) {
            System.out.println("JWT ERROR: " + e.getMessage());
        }
        String token = jwtService.getToken(request);

        try{
            if(token != null && !token.isEmpty()){
                String email = jwtService.extractEmail(token);
                if (email != null && !email.isEmpty()){
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    if (jwtService.validateToken(token, userDetails)){
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        token,
                                        userDetails.getAuthorities()
                                );

                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        } catch (ExpiredJwtException e) {
            LOGGER.debug("Token expired");
        }
        filterChain.doFilter(request, response);
    }
}
