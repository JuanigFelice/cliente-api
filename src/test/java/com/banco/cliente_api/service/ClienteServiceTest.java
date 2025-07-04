package com.banco.cliente_api.service;

import com.banco.cliente_api.exception.InvalidInputException;
import com.banco.cliente_api.model.Cliente;
import com.banco.cliente_api.model.ProductoBancario;
import com.banco.cliente_api.repository.ClienteRepository;
import com.banco.cliente_api.repository.ProductoBancarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita la integración de Mockito con JUnit 5
public class ClienteServiceTest {

    @Mock // Mockea ClienteRepository
    private ClienteRepository clienteRepository;

    @Mock // Mockea ProductoBancarioRepository
    private ProductoBancarioRepository productoBancarioRepository;

    @InjectMocks // Inyecta los mocks en esta instancia de ClienteService
    private ClienteService clienteService;

    private Cliente clienteEjemplo;
    private ProductoBancario productoAhorro;
    private ProductoBancario productoCredito;

    @BeforeEach
    void setUp() {
        // Inicializa objetos comunes antes de cada test para evitar repetición
        clienteEjemplo = Cliente.builder()
                .id(1L)
                .dni("12345678")
                .nombre("Juan")
                .apellido("Perez")
                .calle("Calle Falsa")
                .numero(123)
                .telefono("1122334455")
                .celular("1566778899")
                .productosBancarios(new HashSet<>()) // Importante inicializar el set
                .build();

        productoAhorro = new ProductoBancario(1L, "CJAHRR", "Caja de Ahorro");
        productoCredito = new ProductoBancario(2L, "TJCREDITO", "Tarjeta de Crédito");
    }

    @Test
    void testCrearClienteSinProductos() {
        // Verificar que la excepción InvalidInputException sea lanzada
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            clienteService.crearCliente(clienteEjemplo, Collections.emptySet());
        });

        // Aserciones
        assertEquals("Debe especificar al menos un producto bancario válido para el cliente.", exception.getMessage());
        verify(clienteRepository, never()).save(any(Cliente.class)); // El save no debería ocurrir si lanza la excepción
        verifyNoInteractions(productoBancarioRepository); // No debería interactuar con productoBancarioRepository
    }

    @Test
    void testCrearClienteConProductosExistentes() {
        Set<String> codigosProductos = new HashSet<>();
        codigosProductos.add("CJAHRR");
        codigosProductos.add("TJCREDITO");

        // Configurar el comportamiento de los mocks de productoBancarioRepository
        when(productoBancarioRepository.findByCodigo("CJAHRR")).thenReturn(Optional.of(productoAhorro));
        when(productoBancarioRepository.findByCodigo("TJCREDITO")).thenReturn(Optional.of(productoCredito));

        // Configurar el comportamiento del mock de clienteRepository.save()
        // El clienteEjemplo debe reflejar los productos asignados después del save
        Cliente clienteConProductos = Cliente.builder().id(1L).dni("12345678").nombre("Juan").apellido("Perez").build();
        clienteConProductos.setProductosBancarios(new HashSet<>());
        clienteConProductos.addProductoBancario(productoAhorro);
        clienteConProductos.addProductoBancario(productoCredito);

        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteConProductos);

        // Ejecutar el método a probar
        Cliente clienteGuardado = clienteService.crearCliente(clienteEjemplo, codigosProductos);

        // Aserciones
        assertNotNull(clienteGuardado);
        assertEquals("12345678", clienteGuardado.getDni());
        assertFalse(clienteGuardado.getProductosBancarios().isEmpty());
        assertEquals(2, clienteGuardado.getProductosBancarios().size());
        assertTrue(clienteGuardado.getProductosBancarios().contains(productoAhorro));
        assertTrue(clienteGuardado.getProductosBancarios().contains(productoCredito));

        // Verificar interacciones con los mocks
        verify(productoBancarioRepository, times(1)).findByCodigo("CJAHRR");
        verify(productoBancarioRepository, times(1)).findByCodigo("TJCREDITO");
        verify(clienteRepository, times(1)).save(any(Cliente.class)); 
    }

    @Test
    void testCrearClienteConProductosAlgunosNoExistentes() {
        Set<String> codigosProductos = new HashSet<>();
        codigosProductos.add("CJAHRR");
        codigosProductos.add("PROD_NO_EXISTE"); // Este producto no existirá

        // Configurar el comportamiento de los mocks de productoBancarioRepository
        when(productoBancarioRepository.findByCodigo("CJAHRR")).thenReturn(Optional.of(productoAhorro));
        when(productoBancarioRepository.findByCodigo("PROD_NO_EXISTE")).thenReturn(Optional.empty());

        // Verificar que la excepción InvalidInputException sea lanzada
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            clienteService.crearCliente(clienteEjemplo, codigosProductos);
        });

        // Aserciones
        assertEquals("El producto bancario con código 'PROD_NO_EXISTE' no existe.", exception.getMessage());
        verify(productoBancarioRepository, times(1)).findByCodigo("CJAHRR");
        verify(productoBancarioRepository, times(1)).findByCodigo("PROD_NO_EXISTE");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void testGetAllClientes() {
        List<Cliente> clientes = List.of(clienteEjemplo, new Cliente());
        when(clienteRepository.findAll()).thenReturn(clientes);

        List<Cliente> result = clienteService.getAllClientes();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(clienteEjemplo.getDni(), result.get(0).getDni());
        verify(clienteRepository, times(1)).findAll(); // Verifica que se llamó al repositorio
    }

    @Test
    void testGetClienteByDniExistente() {
        String dni = "12345678";
        when(clienteRepository.findByDni(dni)).thenReturn(Optional.of(clienteEjemplo));

        Optional<Cliente> resultado = clienteService.getClienteByDni(dni);

        assertTrue(resultado.isPresent());
        assertEquals(dni, resultado.get().getDni());
        verify(clienteRepository, times(1)).findByDni(dni);
    }

    @Test
    void testGetClienteByDniNoExistente() {
        String dni = "99999999";
        when(clienteRepository.findByDni(dni)).thenReturn(Optional.empty());

        Optional<Cliente> resultado = clienteService.getClienteByDni(dni);

        assertFalse(resultado.isPresent());
        verify(clienteRepository, times(1)).findByDni(dni);
    }

    @Test
    void testUpdateClienteTelefonoExistente() {
        String dni = "12345678";
        String nuevoTelefono = "1198765432";
        
        // Simular que findByDni encuentra al cliente y luego save lo guarda actualizado
        when(clienteRepository.findByDni(dni)).thenReturn(Optional.of(clienteEjemplo));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente clienteArg = invocation.getArgument(0);
            assertEquals(nuevoTelefono, clienteArg.getTelefono()); // Verificar que el teléfono se actualizó antes de guardar
            return clienteArg; // Retornar el mismo cliente modificado
        });

        Cliente clienteActualizado = clienteService.updateClienteTelefono(dni, nuevoTelefono);

        assertNotNull(clienteActualizado);
        assertEquals(nuevoTelefono, clienteActualizado.getTelefono());
        verify(clienteRepository, times(1)).findByDni(dni);
        verify(clienteRepository, times(1)).save(clienteEjemplo); // Verifica que save fue llamado con el cliente modificado
    }

    @Test
    void testUpdateClienteTelefonoNoExistenteThrowsException() {
        String dni = "99999999";
        String nuevoTelefono = "1198765432";

        when(clienteRepository.findByDni(dni)).thenReturn(Optional.empty());

        // Asegurarse de que lanza la excepción esperada
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                clienteService.updateClienteTelefono(dni, nuevoTelefono));

        assertEquals("Cliente no encontrado con DNI: " + dni, exception.getMessage());
        verify(clienteRepository, times(1)).findByDni(dni);
        verify(clienteRepository, never()).save(any(Cliente.class)); // save nunca debe ser llamado
    }

    @Test
    void testGetClientesByProductoBancario() {
        String codigoProducto = "CJAHRR";
        List<Cliente> clientesPorProducto = List.of(clienteEjemplo); // Un cliente que tiene ese producto
        when(clienteRepository.findByProductosBancarios_Codigo(codigoProducto)).thenReturn(clientesPorProducto);

        List<Cliente> result = clienteService.getClientesByProductoBancario(codigoProducto);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(clienteEjemplo.getDni(), result.get(0).getDni());
        verify(clienteRepository, times(1)).findByProductosBancarios_Codigo(codigoProducto);
    }

    @Test
    void testGetClientesByProductoBancarioNoEncontrado() {
        String codigoProducto = "NON_EXISTENT_PROD";
        when(clienteRepository.findByProductosBancarios_Codigo(codigoProducto)).thenReturn(Collections.emptyList());

        List<Cliente> result = clienteService.getClientesByProductoBancario(codigoProducto);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(clienteRepository, times(1)).findByProductosBancarios_Codigo(codigoProducto);
    }

    @Test
    void testDeleteClienteExistente() {
        String dni = "12345678";
        when(clienteRepository.findByDni(dni)).thenReturn(Optional.of(clienteEjemplo));
        doNothing().when(clienteRepository).delete(any(Cliente.class)); // Configura el mock para un método void

        assertDoesNotThrow(() -> clienteService.deleteCliente(dni)); // No debe lanzar ninguna excepción
        verify(clienteRepository, times(1)).findByDni(dni);
        verify(clienteRepository, times(1)).delete(clienteEjemplo); // Verifica que delete fue llamado
    }

    @Test
    void testDeleteClienteNoExistenteThrowsException() {
        String dni = "99999999";
        when(clienteRepository.findByDni(dni)).thenReturn(Optional.empty());

        // Asegurarse de que lanza la excepción esperada
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                clienteService.deleteCliente(dni));

        assertEquals("Cliente no encontrado con DNI: " + dni, exception.getMessage());
        verify(clienteRepository, times(1)).findByDni(dni);
        verify(clienteRepository, never()).delete(any(Cliente.class)); // delete nunca debe ser llamado
    }
}