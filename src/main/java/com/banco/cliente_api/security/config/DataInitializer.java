package com.banco.cliente_api.security.config;

import com.banco.cliente_api.repository.ProductoBancarioRepository;
import com.banco.cliente_api.security.entity.EnumRole;
import com.banco.cliente_api.security.entity.Role;
import com.banco.cliente_api.security.entity.UsuarioBanco;
import com.banco.cliente_api.security.repository.RoleRepository;
import com.banco.cliente_api.security.repository.UsuarioBancoRepository;
import com.banco.cliente_api.model.ProductoBancario;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

     @Bean
    public CommandLineRunner initData(RoleRepository roleRepository,
                                      UsuarioBancoRepository usuarioBancoRepository,
                                      ProductoBancarioRepository productoBancarioRepository,
                                      PasswordEncoder passwordEncoder) { 
        return args -> {
            logger.info("Iniciando precarga de datos: Roles, Usuarios y Productos Bancarios...");


            /* Lógica de creación de Roles */
            Role userRole = roleRepository.findByName(EnumRole.ROLE_USER)
                                          .orElseGet(() -> {
                                              Role newUserRole = new Role();
                                              newUserRole.setName(EnumRole.ROLE_USER);
                                              return roleRepository.save(newUserRole);
                                          });
            if (userRole.getId() != null) {
                logger.info("Rol ROLE_USER verificado/creado");
            }

            Role modRole = roleRepository.findByName(EnumRole.ROLE_MODERATOR)
                                         .orElseGet(() -> {
                                             Role newModRole = new Role();
                                             newModRole.setName(EnumRole.ROLE_MODERATOR);
                                             return roleRepository.save(newModRole);
                                         });
            if (modRole.getId() != null) {
                logger.info("Rol ROLE_MODERATOR verificado/creado");
            }

            Role adminRole = roleRepository.findByName(EnumRole.ROLE_ADMIN)
                                           .orElseGet(() -> {
                                               Role newAdminRole = new Role();
                                               newAdminRole.setName(EnumRole.ROLE_ADMIN);
                                               return roleRepository.save(newAdminRole);
                                           });
            if (adminRole.getId() != null) {
                logger.info("Rol ROLE_ADMIN verificado/creado");
            }

            // * Lógica de creación de Usuarios */
            logger.info("Verificando y creando usuarios iniciales si no existen...");

            // Crear usuario 'admin'
            if (usuarioBancoRepository.findByUsername("admin").isEmpty()) {
                UsuarioBanco adminUser = new UsuarioBanco("admin", passwordEncoder.encode("adminpass"));
                Set<Role> adminRoles = new HashSet<>();
                adminRoles.add(adminRole);
                adminUser.setRoles(adminRoles);
                usuarioBancoRepository.save(adminUser);
                logger.info("Usuario 'admin' creado con éxito (Username: admin, Password: adminpass).");
            } else {
                logger.info("Usuario 'admin' ya existe.");
            }

            // Crear usuario 'user'
            if (usuarioBancoRepository.findByUsername("user").isEmpty()) {
                UsuarioBanco standardUser = new UsuarioBanco("user", passwordEncoder.encode("userpass"));
                Set<Role> userRoles = new HashSet<>();
                userRoles.add(userRole);
                standardUser.setRoles(userRoles);
                usuarioBancoRepository.save(standardUser);
                logger.info("Usuario 'user' creado con éxito (Username: user, Password: userpass).");
            } else {
                logger.info("Usuario 'user' ya existe.");
            }

            // *** Precarga de Productos Bancarios ***/
            logger.info("Verificando y creando productos bancarios iniciales si no existen...");

            insertProductoIfNotExists(productoBancarioRepository, "PZOF", "Plazo Fijo");
            insertProductoIfNotExists(productoBancarioRepository, "CHEQ", "Cheques");
            insertProductoIfNotExists(productoBancarioRepository, "TJCREDITO", "Tarjeta de Crédito");
            insertProductoIfNotExists(productoBancarioRepository, "CJAHRR", "Caja de Ahorro");
            insertProductoIfNotExists(productoBancarioRepository, "CTACORR", "Cuenta Corriente");
            insertProductoIfNotExists(productoBancarioRepository, "PRESTAMO", "Préstamo");
            insertProductoIfNotExists(productoBancarioRepository, "TJDEBITO", "Tarjeta de Débito");
       
        };
    }
    
    // Método auxiliar para evitar repetición de código
    private void insertProductoIfNotExists(ProductoBancarioRepository repo, String codigo, String descripcion) {
        repo.findByCodigo(codigo).ifPresentOrElse(
            producto -> logger.info("Producto '{}' ({}) ya existe.", descripcion, codigo),
            () -> {
                ProductoBancario nuevoProducto = new ProductoBancario();
                nuevoProducto.setCodigo(codigo);
                nuevoProducto.setDescripcion(descripcion);
                repo.save(nuevoProducto);
                logger.info("Producto '{}' ({}) creado.", descripcion, codigo);
            }
        );
    }
}