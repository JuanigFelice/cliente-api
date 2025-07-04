package com.banco.cliente_api.security.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banco.cliente_api.security.entity.EnumRole;
import com.banco.cliente_api.security.entity.Role;
import com.banco.cliente_api.security.entity.UsuarioBanco;
import com.banco.cliente_api.security.jwt.JwtUtils;
import com.banco.cliente_api.security.payload.request.LoginRequest;
import com.banco.cliente_api.security.payload.request.SignupRequest;
import com.banco.cliente_api.security.payload.response.JwtResponse;
import com.banco.cliente_api.security.payload.response.MessageResponse;
import com.banco.cliente_api.security.repository.RoleRepository;
import com.banco.cliente_api.security.repository.UsuarioBancoRepository;
import com.banco.cliente_api.security.service.UsuarioBancoDetailsImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor; 

@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600) // Permite solicitudes CORS desde cualquier origen (para desarrollo)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private static final Logger logger = LoggerFactory.getLogger(AuthController.class); // Logger

	private final AuthenticationManager authenticationManager;
    private final UsuarioBancoRepository usuarioBancoRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    
    /**
     * Endpoint para iniciar sesión (login) de un usuario.
     * Recibe un usuario y contraseña, los autentica y si son correctos, devuelve un JWT.
     * @param loginRequest DTO con username y password.
     * @return ResponseEntity con el JWT y detalles del usuario, o un error.
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

    	logger.info("Solicitud de autenticación para el usuario: {}", loginRequest.getUsername());

    	
        // Realiza la autenticación usando el AuthenticationManager
        // Esto usa internamente UsuarioBancoServiceImpl y PasswordEncoder
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        // Si la autenticación es exitosa, establece la autenticación en el contexto de seguridad de Spring
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Genera el token JWT para el usuario autenticado
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Obtiene los detalles del usuario autenticado para construir la respuesta JWT
        UsuarioBancoDetailsImpl usuarioBancoDetails = (UsuarioBancoDetailsImpl) authentication.getPrincipal();

        // Extrae los roles del usuario para incluirlos en la respuesta
        List<String> roles = usuarioBancoDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        // Devuelve la respuesta JWT con el token y detalles del usuario
        return ResponseEntity.ok(new JwtResponse(jwt,
                                                 usuarioBancoDetails.getId(),
                                                 usuarioBancoDetails.getUsername(),
                                                 roles));
    }

    /**
     * Endpoint para registrar un nuevo usuario.
     * @param signUpRequest DTO con username, email, password y roles (opcional).
     * @return ResponseEntity con un mensaje de éxito o error.
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    	logger.info("Solicitud de registro para el usuario: {}", signUpRequest.getUsername());
    	
        // Verifica si el nombre de usuario ya existe
        if (usuarioBancoRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: El nombre de usuario ya está en uso!"));
        }

        // Crea una nueva instancia de UsuarioBanco
        UsuarioBanco usuario = new UsuarioBanco(signUpRequest.getUsername(), encoder.encode(signUpRequest.getPassword())); // Hashea la contraseña

        // Obtiene los roles del request
        Set<String> strRoles = signUpRequest.getRole();
        
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) { // Si no se especifican roles, asigna el rol por defecto (USER)
            Role userRole = roleRepository.findByName(EnumRole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Rol USER no encontrado."));
            roles.add(userRole);
        } else {
        	strRoles.forEach(roleStr -> {
        	    EnumRole enumRole;
        	    switch (roleStr.toLowerCase()) {
        	        case "admin":
        	            enumRole = EnumRole.ROLE_ADMIN;
        	            break;
        	        case "mod": // moderador o empleado del banco
        	            enumRole = EnumRole.ROLE_MODERATOR;
        	            break;
        	        default:
        	            enumRole = EnumRole.ROLE_USER;
        	            break;
        	    }

        	    Role role = roleRepository.findByName(enumRole)
        	            .orElseThrow(() -> new RuntimeException("Error: Rol " + enumRole.name() + " no encontrado."));
        	    roles.add(role);
        	});
        }

        usuario.setRoles(roles);
        usuarioBancoRepository.save(usuario);

        return ResponseEntity.ok(new MessageResponse("Usuario registrado exitosamente!"));
    }
}