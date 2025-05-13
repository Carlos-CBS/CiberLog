package com.cyberlog.Config;

import com.cyberlog.Service.JWTService;
import com.cyberlog.Service.MyUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
        // Rutas públicas que no necesitan autenticación
        final String[] PUBLIC_PATHS = {"/login", "/register", "/logout"};

        String requestPath = request.getServletPath();
        boolean isPublicPath = false;

        for (String path : PUBLIC_PATHS) {
            if (requestPath.equals(path)) {
                isPublicPath = true;
                break;
            }
        }

        if (isPublicPath) {
            // Si es una ruta pública, continúa sin verificar autenticación
            filterChain.doFilter(request, response);
            return;
        }

        // Extraer y validar token para rutas protegidas
        String authHeader = request.getHeader("Authorization");

        String token = null;
        String email = null;

        // Verificar formato del header de autorización
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);

            try {
                email = jwtService.extractUserEmail(token);
            } catch (Exception e) {
                // Error en la extracción del email
            }
        }

        // Autenticar al usuario si el token es válido
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtService.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                // Manejo de errores
            }
        }

        filterChain.doFilter(request, response);
    }
}
