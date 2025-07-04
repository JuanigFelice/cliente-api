package com.banco.cliente_api.security.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    private final ObjectMapper objectMapper;

    @Autowired
    public AuthEntryPointJwt(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
  
        logger.error("Error de autenticación: No autorizado. Mensaje: {}. Path: {}", authException.getMessage(), request.getRequestURI());

        // Configuración respuesta
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Crea cuerpo JSON usando Map.of()
        Map<String, Object> errorDetails = Map.of(
            "status", HttpServletResponse.SC_UNAUTHORIZED,
            "error", "Unauthorized",
            "message", "Acceso no autorizado. Credenciales inválidas o token ausente/expirado.",
            "path", request.getRequestURI()
        );

        // Escribir la respuesta JSON
        objectMapper.writeValue(response.getWriter(), errorDetails);
    }
}
