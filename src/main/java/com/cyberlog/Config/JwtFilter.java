package com.cyberlog.Config;

import com.cyberlog.Repositories.UserRepo;
import com.cyberlog.Service.JWTService;
import com.cyberlog.Service.MyUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// check the filter only once for request
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
        System.out.println("Auth Header: " + authHeader); // Log para debugging

        String token = null;
        String email = null;

        // Verificar formato del header de autorización
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            System.out.println("Token extraído: " + token.substring(0, Math.min(10, token.length())) + "..."); // Log solo el inicio del token

            try {
                email = jwtService.extractUserEmail(token);
                System.out.println("Email extraído: " + email);
            } catch (Exception e) {
                System.out.println("Error al extraer email del token: " + e.getMessage());
                // No establecer autenticación, continuar al filtro siguiente
            }
        } else {
            System.out.println("Header de autorización no presente o formato incorrecto");
        }

        // Autenticar al usuario si el token es válido
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                System.out.println("Usuario cargado: " + userDetails.getUsername());

                if (jwtService.validateToken(token, userDetails)) {
                    System.out.println("Token validado correctamente");
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("Autenticación establecida en el contexto de seguridad");
                } else {
                    System.out.println("Validación de token falló");
                }
            } catch (Exception e) {
                System.out.println("Error al autenticar usuario: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}