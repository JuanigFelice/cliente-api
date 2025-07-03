package com.banco.cliente_api.repository;

import com.banco.cliente_api.model.ProductoBancario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductoBancarioRepository extends JpaRepository<ProductoBancario, Long> {
    Optional<ProductoBancario> findByCodigo(String codigo);
}