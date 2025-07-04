package com.banco.cliente_api.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteTelefonoUpdateDTO {
    @NotBlank(message = "El DNI no puede estar vacío.")
    private String dni;
    @NotBlank(message = "El nuevo teléfono no puede estar vacío.")
    private String nuevoTelefono;
}