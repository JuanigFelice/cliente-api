package com.banco.cliente_api.security.jwt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.banco.cliente_api.security.service.UsuarioBancoServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Este filtro se ejecuta una vez por cada solicitud HTTP que llega al servidor.
// Es el encargado de interceptar y validar el token JWT.
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UsuarioBancoServiceImpl usuarioBancoService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            logger.info("AuthTokenFilter - Token extraído: {}", jwt != null ? jwt.substring(0, Math.min(jwt.length(), 20)) + "..." : "NULL");

            // Si se encontró un token y es válido
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
            	logger.info("AuthTokenFilter - Token JWT es válido.");
                // Extrae el nombre de usuario (subject) del token JWT
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                logger.info("AuthTokenFilter - Nombre de usuario del token: {}", username);

                // Carga los detalles completos del usuario usando UsuarioBancoServiceImpl
                // Esto también verifica si el usuario existe en la base de datos.
                UserDetails userDetails = usuarioBancoService.loadUserByUsername(username);
                
                // Representa un usuario autenticado con sus credenciales y roles
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                // Establece detalles adicionales de la autenticación (como la IP del cliente, sesión, etc.)
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Establece la autenticación en el SecurityContextHolder.
                // Le dice a Spring Security que el usuario actual está autenticado
                // y con qué permisos. Los controladores y las anotaciones @PreAuthorize/@PostAuthorize
                // se usaran para la autorización.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            
	            logger.info("AuthTokenFilter - Autenticación establecida para el usuario: {}", username);
	        } else {
	            logger.warn("AuthTokenFilter - No se encontró JWT o el JWT no es válido.");
	            if (jwt != null) {
	                 logger.warn("AuthTokenFilter - Motivo de la falla de validación (JwtUtils): " + (jwtUtils.validateJwtToken(jwt) ? "Validó" : "No validó"));
	            }
	        }
        } catch (Exception e) {
            logger.error("No se pudo establecer la autenticación del usuario: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * Método auxiliar para extraer el token JWT de la cabecera "Authorization" de la solicitud.
     * Los tokens JWT suelen enviarse en el formato "Bearer <token>"
     * @param request La solicitud HTTP.
     * @return El token JWT (solo la cadena del token) o null si no se encuentra.
     */
    private String parseJwt(HttpServletRequest request) {
        // Obtiene el valor completo de la cabecera "Authorization"
        String headerAuth = request.getHeader("Authorization");
        logger.info("AuthTokenFilter - Cabecera Authorization recibida: {}", headerAuth);
        
        // Verifica si la cabecera existe y comienza con "Bearer "
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            // Si es así, devuelve la parte del token sin el prefijo "Bearer " (que tiene 7 caracteres)
            return headerAuth.substring(7);
        }
        return null; // No se encontró un token válido en la cabecera
    }
}