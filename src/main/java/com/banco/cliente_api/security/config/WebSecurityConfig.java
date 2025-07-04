package com.banco.cliente_api.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.banco.cliente_api.security.jwt.AuthEntryPointJwt;
import com.banco.cliente_api.security.jwt.AuthTokenFilter;
import com.banco.cliente_api.security.service.UsuarioBancoServiceImpl;

import lombok.RequiredArgsConstructor;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true) // Permite usar anotaciones como @PreAuthorize en los controladores
public class WebSecurityConfig {
   
    @Autowired
    private UsuarioBancoServiceImpl usuarioBancoService;
    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;
    // Este bean define el codificador de contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return usuarioBancoService;
    }

    @SuppressWarnings("deprecation")
	@Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(usuarioBancoService); // Establece UserDetailsService (UsuarioBancoServiceImpl)
        authProvider.setPasswordEncoder(passwordEncoder());     // Establece PasswordEncoder
        return authProvider;
    }

    // Este bean expone el AuthenticationManager, que se usará para autenticar a los usuarios
    // en el controlador de autenticación (login).
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }


    /*** Aquí definimos las reglas de autorización y los filtros de seguridad. ***/
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // Deshabilita CSRF para APIs RESTful que usan JWT
            // Configura el manejador para excepciones de autenticación (401 Unauthorized)
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth ->
                // Permite acceso público a los endpoints de autenticación (login, registro de usuarios)
                auth.requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/test/**").permitAll()
                    // Cualquier otra solicitud debe ser autenticada (requiere un JWT válido)
                    .anyRequest().authenticated()
            );

        // Registra el proveedor de autenticación personalizado
        http.authenticationProvider(authenticationProvider());

        // Agrego un filtro JWT (AuthTokenFilter) antes del filtro estándar de autenticación de Spring Security.
        // Esto asegura que JWT sea validado antes.
        http.addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    // Es un bean de inyeccion de dependencias dentro de AuthTokenFilter.
    @Bean
    public AuthTokenFilter authTokenFilter() {
        return new AuthTokenFilter();
    }

}