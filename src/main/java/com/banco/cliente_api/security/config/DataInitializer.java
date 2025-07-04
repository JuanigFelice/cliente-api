package com.banco.cliente_api.security.config;

import com.banco.cliente_api.security.entity.EnumRole;
import com.banco.cliente_api.security.entity.Role;
import com.banco.cliente_api.security.repository.RoleRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

	private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class); // Inicializar Logger

	
    @Bean
    public CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
        	 logger.info("Verificando y creando roles iniciales si no existen...");
        	 
            if (roleRepository.findByName(EnumRole.ROLE_USER).isEmpty()) {
                Role userRole = new Role();
                userRole.setName(EnumRole.ROLE_USER);
                roleRepository.save(userRole);
                System.out.println("Rol ROLE_USER creado.");
            }
            if (roleRepository.findByName(EnumRole.ROLE_MODERATOR).isEmpty()) {
                Role modRole = new Role();
                modRole.setName(EnumRole.ROLE_MODERATOR);
                roleRepository.save(modRole);
                System.out.println("Rol ROLE_MODERATOR creado.");
            }
            if (roleRepository.findByName(EnumRole.ROLE_ADMIN).isEmpty()) {
                Role adminRole = new Role();
                adminRole.setName(EnumRole.ROLE_ADMIN);
                roleRepository.save(adminRole);
                System.out.println("Rol ROLE_ADMIN creado.");
            }
        };
    }
}