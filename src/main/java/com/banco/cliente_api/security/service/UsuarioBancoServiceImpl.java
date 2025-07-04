package com.banco.cliente_api.security.service;

import com.banco.cliente_api.security.entity.UsuarioBanco;
import com.banco.cliente_api.security.repository.UsuarioBancoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioBancoServiceImpl implements UserDetailsService {

    @Autowired
    UsuarioBancoRepository usuarioBancoRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    	UsuarioBanco user = usuarioBancoRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return UsuarioBancoDetailsImpl.build(user);
    }
}