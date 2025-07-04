package com.banco.cliente_api.adapter.web.controller;

import com.banco.cliente_api.adapter.web.dto.ClienteRequestDTO;
import com.banco.cliente_api.adapter.web.dto.ClienteResponseDTO;
import com.banco.cliente_api.model.Cliente;
import com.banco.cliente_api.service.ClienteService;
import com.banco.cliente_api.util.DtoConverter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.banco.cliente_api.exception.ClienteNotFoundException;
import com.banco.cliente_api.exception.ClientesPorProductoNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ClienteController {

	private static final Logger logger = LoggerFactory.getLogger(ClienteController.class);
	
    private final ClienteService clienteService;
    private final DtoConverter dtoConverter;

    // Alta de un cliente
    // Solo ADMIN puede crear clientes.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteResponseDTO> crearCliente(@Valid @RequestBody ClienteRequestDTO clienteRequestDTO) {
    	logger.info("Recibida la solicitud para crear un nuevo cliente.");
    	
        Cliente cliente = dtoConverter.convertToEntity(clienteRequestDTO);
        
        Set<String> productosBancariosCodigosSet = clienteRequestDTO.getProductosBancariosCodigos()
        																.stream()
        																.collect(Collectors.toSet());
    
        Cliente nuevoCliente = clienteService.crearCliente(cliente, productosBancariosCodigosSet);
        logger.info("Cliente con DNI {} creado exitosamente.", nuevoCliente.getDni());

        return new ResponseEntity<>(dtoConverter.convertToDto(nuevoCliente), HttpStatus.CREATED);
    }

    // Recuperar todos los clientes
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or hasRole('USER')")
    public ResponseEntity<List<ClienteResponseDTO>> getAllClientes() {
    	logger.info("Recibida solicitud para obtener todos los clientes.");
    	
        List<ClienteResponseDTO> clientes = clienteService.getAllClientes().stream()
                .map(dtoConverter::convertToDto)
                .toList();
        return ResponseEntity.ok(clientes);
    }

    // Recuperar un cliente por DNI
    // ADMIN, MODERATOR, o incluso USER, pueden ver UN cliente.
    // Si USER puede ver CUALQUIER cliente, usa hasRole('USER').
    // Si solo los logueados pueden ver, usa isAuthenticated().
    @GetMapping("/{dni}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or hasRole('USER')")
    public ResponseEntity<ClienteResponseDTO> getClienteByDni(@PathVariable String dni) {
    	logger.info("Recibida solicitud para obtener cliente con DNI: {}", dni);
    	
        return clienteService.getClienteByDni(dni)
                .map(dtoConverter::convertToDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> {
                    logger.warn("Cliente con DNI {} no encontrado en el controlador.", dni);
                    return new ClienteNotFoundException(dni);
                });
    }

    // Modificación del teléfono de un cliente
    // Todos pueden actualizar clientes
    // Acá importa que tenga roles y no que sea otro tipo de usuario logueado
    @PatchMapping("/{dni}/telefono")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or hasRole('USER')")
    public ResponseEntity<ClienteResponseDTO> updateClienteTelefono(@PathVariable String dni, @RequestParam String nuevoTelefono) {
    	logger.info("Recibida solicitud para actualizar teléfono del cliente con DNI {}.", dni);
    	
        Cliente clienteActualizado = clienteService.updateClienteTelefono(dni, nuevoTelefono);
        logger.info("Teléfono del cliente con DNI {} actualizado exitosamente.", dni);
        
        return ResponseEntity.ok(dtoConverter.convertToDto(clienteActualizado));
               
    }
    
    // Recuperar clientes por producto bancario 
    // Todos pueden realizar esta búsqueda.
    @GetMapping("/por-producto/{codigoProducto}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or hasRole('USER')")
    public ResponseEntity<List<ClienteResponseDTO>> getClientesByProductoBancario(@PathVariable String codigoProducto) {
    	logger.info("Recibida solicitud para obtener clientes por producto bancario con código: {}", codigoProducto);
    	
        List<ClienteResponseDTO> clientes = clienteService.getClientesByProductoBancario(codigoProducto).stream()
                                                        .map(dtoConverter::convertToDto)
                                                        .toList();
        if (clientes.isEmpty()) {
            logger.warn("No se encontraron clientes para el producto bancario {}.", codigoProducto);
            throw new ClientesPorProductoNotFoundException(codigoProducto);
        }
        logger.debug("Se encontraron {} clientes para el producto bancario {}.", clientes.size(), codigoProducto);
        return ResponseEntity.ok(clientes);
    }

    // Eliminar un cliente
    // Solo ADMIN puede eliminar clientes.
    @DeleteMapping("/{dni}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCliente(@PathVariable String dni) {
        logger.info("Recibida solicitud para eliminar cliente con DNI: {}", dni);
        clienteService.deleteCliente(dni);
        logger.info("Cliente con DNI {} eliminado exitosamente.", dni);
        // JSON de confirmación
        return ResponseEntity.ok().body(
            java.util.Map.of(
                "message", "Cliente eliminado exitosamente",
                "dni", dni
            )
        );
    }
}