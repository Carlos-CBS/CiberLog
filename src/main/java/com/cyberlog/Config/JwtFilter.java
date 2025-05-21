package com.cyberlog.Config;

import com.cyberlog.Service.JWTService;
import com.cyberlog.Service.MyUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String[] PUBLIC_PATHS = {"/login", "/register", "/logout", "/login-page"};

        String requestPath = request.getServletPath();
        boolean isPublicPath = false;

        for (String path : PUBLIC_PATHS) {
            if (requestPath.equals(path)) {
                isPublicPath = true;
                break;
            }
        }

        if (isPublicPath) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = null;
        String email = null;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // Si no hay token en las cookies, intentar obtenerlo del header Authorization
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        if (token != null) {
            try {
                email = jwtService.extractUserEmail(token);
            } catch (Exception e) {
                logger.error("Error extracting email from token", e);
            }
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (jwtService.validateToken(token)) {
                    // Cargar los detalles del usuario
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    // Autenticar al usuario
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                logger.error("Authentication error", e);
            }
        }


        filterChain.doFilter(request, response);
    }
}