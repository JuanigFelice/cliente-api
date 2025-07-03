package com.banco.cliente_api.service;

import com.banco.cliente_api.model.Cliente;
import com.banco.cliente_api.model.ProductoBancario;
import com.banco.cliente_api.repository.ClienteRepository;
import com.banco.cliente_api.repository.ProductoBancarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor // Inyección de dependencias para ClienteRepository y ProductoBancarioRepository
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ProductoBancarioRepository productoBancarioRepository;

    @Transactional
    public Cliente crearCliente(Cliente cliente, List<String> productosCodigos) {
        if (productosCodigos == null || productosCodigos.isEmpty()) {
            throw new IllegalArgumentException("Un cliente debe tener al menos un producto bancario.");
        }

        Set<ProductoBancario> productos = new HashSet<>();
        for (String codigo : productosCodigos) {
            ProductoBancario producto = productoBancarioRepository.findByCodigo(codigo)
                    .orElseThrow(() -> new IllegalArgumentException("Producto bancario con código " + codigo + " no encontrado."));
            productos.add(producto);
        }
        cliente.setProductosBancarios(productos);

        return clienteRepository.save(cliente);
    }

    public List<Cliente> getAllClientes() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> getClienteByDni(String dni) {
        return clienteRepository.findByDni(dni);
    }

    @Transactional
    public Cliente updateClienteTelefono(String dni, String nuevoTelefono) {
        return clienteRepository.findByDni(dni).map(cliente -> {
            cliente.setTelefono(nuevoTelefono);
            return clienteRepository.save(cliente);
        }).orElseThrow(() -> new RuntimeException("Cliente con DNI " + dni + " no encontrado."));
    }

    public List<Cliente> getClientesByProductoBancario(String codigoProducto) {
        return clienteRepository.findByProductosBancarios_Codigo(codigoProducto);
    }

    @Transactional
    public void deleteCliente(String dni) {
        Cliente cliente = clienteRepository.findByDni(dni)
                .orElseThrow(() -> new RuntimeException("Cliente con DNI " + dni + " no encontrado para eliminar."));
        clienteRepository.delete(cliente);
    }
}