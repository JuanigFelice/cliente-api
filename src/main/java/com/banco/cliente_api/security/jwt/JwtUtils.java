package com.banco.cliente_api.security.jwt;

import com.banco.cliente_api.security.service.UsuarioBancoDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
	
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    // Inyecta el valor de la clave secreta para la firma del JWT desde application.properties
    @Value("${banco.app.jwtSecret}")
    private String jwtSecret;

    // Inyecta el tiempo de vida (en milisegundos) del token JWT desde application.properties
    @Value("${banco.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    /**
     * Genera un token JWT para un usuario autenticado.
     * @param authentication Objeto de autenticación de Spring Security que contiene los detalles del usuario.
     * @return El token JWT generado como String.
     */
    public String generateJwtToken(Authentication authentication) {
        // Obtiene los detalles del usuario autenticado, casteándolo a la implementación de UserDetails
        UsuarioBancoDetailsImpl usuarioBancoPrincipal = (UsuarioBancoDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((usuarioBancoPrincipal.getUsername())) // Establece el nombre de usuario como "subject" (sujeto) del token
                .setIssuedAt(new Date()) // Establece la fecha de emisión del token (cuando fue creado)
                // Establece la fecha de expiración del token (fecha actual + tiempo de expiración configurado)
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256) // Firma el token con la clave secreta usando el algoritmo HS256
                .compact(); // Construye el JWT en una cadena compacta y segura para URL
    }

    /**
     * Genera una clave segura a partir de la clave secreta (jwtSecret) para la firma.
     * La clave secreta se decodifica de Base64 para ser usada por JJWT.
     * @return Un objeto Key seguro.
     */
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret)); // Decodifica y genera la clave HMAC
    }

    /**
     * Extrae el nombre de usuario (subject) de un token JWT.
     * @param token El token JWT del cual extraer el nombre de usuario.
     * @return El nombre de usuario contenido en el token.
     */
    public String getUserNameFromJwtToken(String token) {
        // Parsea el token usando la clave de firma y extrae el cuerpo (claims), luego el subject
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * Valida la integridad y validez de un token JWT.
     * Verifica la firma, la expiración y otras posibles inconsistencias.
     * @param authToken El token JWT a validar.
     * @return true si el token es válido, false en caso contrario.
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken); // Intenta parsear y validar la firma del token
            logger.info("JwtUtils - Token validado correctamente.");
            
            return true; // Si no se lanza ninguna excepción, el token es válido
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false; // Si ocurre alguna de las excepciones anteriores, el token no es válido
    }
}