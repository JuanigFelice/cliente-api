package com.banco.cliente_api.security.repository;

import com.banco.cliente_api.security.entity.UsuarioBanco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioBancoRepository extends JpaRepository<UsuarioBanco, Long> {
    Optional<UsuarioBanco> findByUsername(String username);
    Boolean existsByUsername(String username);
}