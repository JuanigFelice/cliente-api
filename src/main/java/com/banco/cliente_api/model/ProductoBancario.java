package com.banco.cliente_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "productos_bancarios")
@EqualsAndHashCode(exclude = "clientes")
@ToString(exclude = "clientes")
public class ProductoBancario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Código no puede ser vacío")
    private String codigo; // por ejemplo: PZOF, CHEQ, TJCREDITO, TJDEBITO, CJAHRR, CTACORR

    @NotBlank(message = "Descripción no puede ser vacía")
    private String descripcion; // por ejemplo: Plazo Fijo, Cheques, Tarjeta de Crédito, Tarjeta de Débito, Caja de Ahorro, Cuenta Corriente

    @ManyToMany(mappedBy = "productosBancarios", fetch = FetchType.LAZY)
    private Set<Cliente> clientes = new HashSet<>();

    public ProductoBancario(Long id, String codigo, String descripcion) {
        this.id = id;
        this.codigo = codigo;
        this.descripcion = descripcion;
    }
}