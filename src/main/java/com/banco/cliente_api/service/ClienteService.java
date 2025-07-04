package com.banco.cliente_api.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banco.cliente_api.model.Cliente;
import com.banco.cliente_api.model.ProductoBancario;
import com.banco.cliente_api.repository.ClienteRepository;
import com.banco.cliente_api.repository.ProductoBancarioRepository;
import com.banco.cliente_api.exception.ClienteNotFoundException;
import com.banco.cliente_api.exception.InvalidInputException;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 

@Service
@RequiredArgsConstructor
public class ClienteService {

	private static final Logger logger = LoggerFactory.getLogger(ClienteService.class);
	
    private final ClienteRepository clienteRepository;
    private final ProductoBancarioRepository productoBancarioRepository;
    
    
    public Cliente crearCliente(Cliente cliente, Set<String> productosBancariosCodigos) {
    	logger.info("Intentando crear cliente con DNI: {}", cliente.getDni());
    	
    	// Validar si el DNI ya existe
        if (clienteRepository.existsByDni(cliente.getDni())) {
            logger.warn("Fallo al crear cliente: Cliente con DNI {} ya existe.", cliente.getDni());
            throw new InvalidInputException("El DNI " + cliente.getDni() + " ya existe.");
        }
        
        // Asocia productos bancarios
        if (productosBancariosCodigos != null && !productosBancariosCodigos.isEmpty()) {
            Set<ProductoBancario> productos = productosBancariosCodigos.stream()
                    .map(productoBancarioRepository::findByCodigo)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            cliente.setProductosBancarios(productos);
        }

        return clienteRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public List<Cliente> getAllClientes() {
        return clienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Cliente> getClienteByDni(String dni) {
    	logger.debug("Buscando cliente por DNI: {}", dni);
        return clienteRepository.findByDni(dni);
    }

    public Cliente updateClienteTelefono(String dni, String nuevoTelefono) {
    	logger.info("Intentando actualizar teléfono para cliente con DNI {}: Nuevo teléfono {}", dni, nuevoTelefono);
        return clienteRepository.findByDni(dni).map(cliente -> {
            cliente.setTelefono(nuevoTelefono);
            logger.info("Cliente con DNI {} encontrado y teléfono actualizado a {}", dni, nuevoTelefono);
            return clienteRepository.save(cliente);
        })
        	.orElseThrow(() -> {
        	logger.warn("Fallo al actualizar teléfono: Cliente con DNI {} no encontrado.", dni);
        	return new ClienteNotFoundException(dni);
        });
    }

    public List<Cliente> getClientesByProductoBancario(String codigoProducto) {
        // Asumiendo que Cliente tiene una relación con ProductoBancario        
        return clienteRepository.findByProductosBancarios_Codigo(codigoProducto);
    }

    public void deleteCliente(String dni) {
    	logger.info("Intentando eliminar cliente con DNI {}", dni);
        clienteRepository.findByDni(dni).ifPresentOrElse(
                cliente -> clienteRepository.delete(cliente),
                () -> { throw new ClienteNotFoundException(dni);
               }
        );
    }
}