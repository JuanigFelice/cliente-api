package com.banco.cliente_api.security.repository;

import com.banco.cliente_api.security.entity.EnumRole;
import com.banco.cliente_api.security.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(EnumRole name);
}