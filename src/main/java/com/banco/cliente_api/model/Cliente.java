package com.banco.cliente_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder; // Añadido para el patrón Builder
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Habilita el patrón Builder para esta entidad
@Table(name = "clientes") // Especifica el nombre de la tabla en la DB
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String dni; // Asumiendo DNI puede ser String si tiene formato o letras

    @NotBlank(message = "Nombre no puede ser vacío")
    private String nombre;

    @NotBlank(message = "Apellido no puede ser vacío")
    private String apellido;

    private String calle;
    private Integer numero;
    private String codigoPostal;
    private String telefono;
    private String celular;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
        name = "cliente_producto",
        joinColumns = @JoinColumn(name = "cliente_id"),
        inverseJoinColumns = @JoinColumn(name = "producto_id")
    )
    private Set<ProductoBancario> productosBancarios = new HashSet<>();

    // Métodos de ayuda para la gestión de la relación (opcional, pero buena práctica)
    public void addProductoBancario(ProductoBancario producto) {
        this.productosBancarios.add(producto);
        producto.getClientes().add(this);
    }

    public void removeProductoBancario(ProductoBancario producto) {
        this.productosBancarios.remove(producto);
        producto.getClientes().remove(this);
    }
}