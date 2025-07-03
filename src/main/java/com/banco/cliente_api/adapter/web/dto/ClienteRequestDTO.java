package com.banco.cliente_api.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ClienteRequestDTO {
    @NotNull(message = "DNI no puede ser nulo")
    private Long dni;

    @NotBlank(message = "Nombre no puede ser vacío")
    @Size(max = 100, message = "Nombre no puede exceder los 100 caracteres")
    private String nombre;

    @NotBlank(message = "Apellido no puede ser vacío")
    @Size(max = 100, message = "Apellido no puede exceder los 100 caracteres")
    private String apellido;

    private String calle;
    private Integer numero;
    private String codigoPostal;
    private String telefono;

    @Pattern(regexp = "^[0-9\\s()-]+$", message = "Formato de celular inválido")
    private String celular;

    @NotNull(message = "Se debe especificar al menos un producto bancario")
    @Size(min = 1, message = "El cliente debe tener al menos un producto bancario")
    private List<String> productosBancariosCodigos;
}