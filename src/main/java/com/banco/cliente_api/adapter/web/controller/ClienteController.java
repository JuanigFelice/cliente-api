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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor // Inyección de dependencias para ClienteService y DtoConverter
public class ClienteController {

    private final ClienteService clienteService;
    private final DtoConverter dtoConverter;

    // Alta de un cliente (Create)
    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crearCliente(@Valid @RequestBody ClienteRequestDTO clienteRequestDTO) {
        Cliente cliente = dtoConverter.convertToEntity(clienteRequestDTO);
        Cliente nuevoCliente = clienteService.crearCliente(cliente, clienteRequestDTO.getProductosBancariosCodigos());
        return new ResponseEntity<>(dtoConverter.convertToDto(nuevoCliente), HttpStatus.CREATED);
    }

    // Recuperar todos los clientes (Read All)
    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> getAllClientes() {
        List<ClienteResponseDTO> clientes = clienteService.getAllClientes().stream()
                .map(dtoConverter::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(clientes);
    }

    // Recuperar un cliente por DNI (Read One)
    @GetMapping("/{dni}")
    public ResponseEntity<ClienteResponseDTO> getClienteByDni(@PathVariable String dni) {
        return clienteService.getClienteByDni(dni)
                .map(dtoConverter::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Modificación de teléfono de un cliente (Update)
    @PatchMapping("/{dni}/telefono")
    public ResponseEntity<ClienteResponseDTO> updateClienteTelefono(@PathVariable String dni, @RequestParam String nuevoTelefono) {
        try {
            Cliente clienteActualizado = clienteService.updateClienteTelefono(dni, nuevoTelefono);
            return ResponseEntity.ok(dtoConverter.convertToDto(clienteActualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Recuperar clientes por producto bancario
    @GetMapping("/por-producto/{codigoProducto}")
    public ResponseEntity<List<ClienteResponseDTO>> getClientesByProductoBancario(@PathVariable String codigoProducto) {
        List<ClienteResponseDTO> clientes = clienteService.getClientesByProductoBancario(codigoProducto).stream()
                .map(dtoConverter::convertToDto)
                .collect(Collectors.toList());
        if (clientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clientes);
    }

    // Eliminar un cliente (Delete)
    @DeleteMapping("/{dni}")
    public ResponseEntity<Void> deleteCliente(@PathVariable String dni) {
        try {
            clienteService.deleteCliente(dni);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}