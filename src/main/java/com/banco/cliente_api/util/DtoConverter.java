package com.banco.cliente_api.util;

import com.banco.cliente_api.adapter.web.dto.ClienteRequestDTO;
import com.banco.cliente_api.adapter.web.dto.ClienteResponseDTO;
import com.banco.cliente_api.adapter.web.dto.ProductoBancarioDTO;
import com.banco.cliente_api.model.Cliente;
import com.banco.cliente_api.model.ProductoBancario;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class DtoConverter {

    public Cliente convertToEntity(ClienteRequestDTO dto) {
        // Builder de Lombok para una creación de objeto más limpia
        return Cliente.builder()
                .dni(dto.getDni())
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .calle(dto.getCalle())
                .numero(dto.getNumero())
                .codigoPostal(dto.getCodigoPostal())
                .telefono(dto.getTelefono())
                .celular(dto.getCelular())
                .build();
    }

    public ClienteResponseDTO convertToDto(Cliente cliente) {
       
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setId(cliente.getId());
        dto.setDni(cliente.getDni());
        dto.setNombre(cliente.getNombre());
        dto.setApellido(cliente.getApellido());
        dto.setCalle(cliente.getCalle());
        dto.setNumero(cliente.getNumero());
        dto.setCodigoPostal(cliente.getCodigoPostal());
        dto.setTelefono(cliente.getTelefono());
        dto.setCelular(cliente.getCelular());
        if (cliente.getProductosBancarios() != null) {
            dto.setProductosBancarios(cliente.getProductosBancarios().stream()
                    .map(this::convertProductoToDto)
                    .collect(Collectors.toSet()));
        } else {
            dto.setProductosBancarios(new HashSet<>());
        }
        return dto;
    }

    public ProductoBancarioDTO convertProductoToDto(ProductoBancario producto) {
        ProductoBancarioDTO dto = new ProductoBancarioDTO();
        dto.setCodigo(producto.getCodigo());
        dto.setDescripcion(producto.getDescripcion());
        return dto;
    }
}