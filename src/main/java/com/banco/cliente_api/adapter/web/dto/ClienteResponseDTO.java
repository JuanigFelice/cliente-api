package com.banco.cliente_api.adapter.web.dto;

import lombok.Data;
import java.util.Set;

@Data
public class ClienteResponseDTO {
    private Long id;
    private String dni;
    private String nombre;
    private String apellido;
    private String calle;
    private Integer numero;
    private String codigoPostal;
    private String telefono;
    private String celular;
    private Set<ProductoBancarioDTO> productosBancarios;
}