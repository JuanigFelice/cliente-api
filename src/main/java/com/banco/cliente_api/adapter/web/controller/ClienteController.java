package com.banco.cliente_api.adapter.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banco.cliente_api.adapter.web.dto.ClienteRequestDTO;
import com.banco.cliente_api.adapter.web.dto.ClienteResponseDTO;
import com.banco.cliente_api.adapter.web.dto.ClienteTelefonoUpdateDTO;
import com.banco.cliente_api.exception.ClienteNotFoundException;
import com.banco.cliente_api.exception.ClientesPorProductoNotFoundException;
import com.banco.cliente_api.model.Cliente;
import com.banco.cliente_api.service.ClienteService;
import com.banco.cliente_api.util.DtoConverter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ClienteController {

    private static final Logger logger = LoggerFactory.getLogger(ClienteController.class);

    private final ClienteService clienteService;
    private final DtoConverter dtoConverter;

    
    /**
     * Crea un único cliente.
     * Recibe un ClienteRequestDTO en el cuerpo.
     * Requiere el rol 'ADMIN'.
     *
     * @param clienteRequestDTO El DTO del cliente a crear.
     * @return ResponseEntity con el ClienteResponseDTO del cliente creado y HttpStatus.CREATED (201).
     */
    @PostMapping // /api/clientes
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteResponseDTO> crearCliente(@Valid @RequestBody ClienteRequestDTO clienteRequestDTO) {
        logger.info("Recibida la solicitud para crear un nuevo cliente con DNI: {}", clienteRequestDTO.getDni());

        Cliente cliente = dtoConverter.convertToEntity(clienteRequestDTO);
        Set<String> productosBancariosCodigosSet = clienteRequestDTO.getProductosBancariosCodigos()
                .stream()
                .collect(Collectors.toSet());

        Cliente nuevoCliente = clienteService.crearCliente(cliente, productosBancariosCodigosSet);
        logger.info("Cliente con DNI {} creado exitosamente.", nuevoCliente.getDni());

        return new ResponseEntity<>(dtoConverter.convertToDto(nuevoCliente), HttpStatus.CREATED);
    }

    /**
     * Crea múltiples clientes en una sola solicitud.
     * Recibe una lista de ClienteRequestDTO en el cuerpo.
     * Requiere el rol 'ADMIN'.
     *
     * @param clientesRequestDTOs La lista de DTOs de clientes a crear.
     * @return ResponseEntity con la lista de ClienteResponseDTO de los clientes creados y HttpStatus.CREATED (201).
     */
    @PostMapping("/batch") // /api/clientes/batch
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClienteResponseDTO>> crearClientesBatch(@Valid @RequestBody List<ClienteRequestDTO> clientesRequestDTOs) {
        if (clientesRequestDTOs == null || clientesRequestDTOs.isEmpty()) {
            logger.warn("La lista de clientes a crear en el lote está vacía.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Recibida la solicitud para crear múltiples clientes. Cantidad: {}", clientesRequestDTOs.size());

        List<ClienteResponseDTO> responseDTOs = new ArrayList<>();
        for (ClienteRequestDTO dto : clientesRequestDTOs) {
            logger.debug("Procesando cliente con DNI: {}", dto.getDni());
            Cliente cliente = dtoConverter.convertToEntity(dto);
            Set<String> productosBancariosCodigosSet = dto.getProductosBancariosCodigos()
                    .stream()
                    .collect(Collectors.toSet());
            Cliente nuevoCliente = clienteService.crearCliente(cliente, productosBancariosCodigosSet);
            responseDTOs.add(dtoConverter.convertToDto(nuevoCliente));
        }

        logger.info("Creados {} cliente(s) exitosamente.", responseDTOs.size());
        return new ResponseEntity<>(responseDTOs, HttpStatus.CREATED);
    }

   
    /**
     * Recupera todos los clientes.
     * Permite a usuarios con roles 'ADMIN', 'MODERATOR' o 'USER'.
     *
     * @return ResponseEntity con una lista de ClienteResponseDTO.
     */
    @GetMapping // /api/clientes
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or hasRole('USER')")
    public ResponseEntity<List<ClienteResponseDTO>> getAllClientes() {
        logger.info("Recibida solicitud para obtener todos los clientes.");
        List<ClienteResponseDTO> clientes = clienteService.getAllClientes().stream()
                .map(dtoConverter::convertToDto)
                .toList();
        return ResponseEntity.ok(clientes);
    }

    /**
     * Recupera un cliente por su DNI.
     * Permite a usuarios con roles 'ADMIN', 'MODERATOR' o 'USER'.
     *
     * @param dni El DNI del cliente a buscar.
     * @return ResponseEntity con el ClienteResponseDTO encontrado y HttpStatus.OK (200).
     * @throws ClienteNotFoundException si el cliente no es encontrado.
     */
    @GetMapping("/{dni}") // /api/clientes/{dni}
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

    /**
     * Recupera clientes por un código de producto bancario.
     * Permite a usuarios con roles 'ADMIN', 'MODERATOR' o 'USER'.
     *
     * @param codigoProducto El código del producto bancario.
     * @return ResponseEntity con una lista de ClienteResponseDTO.
     * @throws ClientesPorProductoNotFoundException si no se encuentran clientes para el producto.
     */
    @GetMapping("/por-producto/{codigoProducto}") // /api/clientes/por-producto/{codigoProducto}
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

    
    /**
     * Actualiza el teléfono de un único cliente.
     * El DNI del cliente se pasa como variable de ruta y el nuevo teléfono en el cuerpo.
     * Permite a usuarios con roles 'ADMIN', 'MODERATOR' o 'USER'.
     *
     * @param dni El DNI del cliente a actualizar.
     * @param updateDto DTO que contiene el nuevo número de teléfono.
     * @return ResponseEntity con el ClienteResponseDTO actualizado y HttpStatus.OK (200).
     */
    @PatchMapping("/{dni}/telefono") // /api/clientes/{dni}/telefono
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or hasRole('USER')")
    public ResponseEntity<ClienteResponseDTO> updateClienteTelefono(@PathVariable String dni, @Valid @RequestBody ClienteTelefonoUpdateDTO updateDto) {
        
        if (!dni.equals(updateDto.getDni())) {
            logger.warn("Conflicto: DNI en el path ({}) no coincide con el DNI en el cuerpo ({}).", dni, updateDto.getDni());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Recibida solicitud para actualizar teléfono del cliente con DNI {}.", dni);
        Cliente clienteActualizado = clienteService.updateClienteTelefono(dni, updateDto.getNuevoTelefono());
        logger.info("Teléfono del cliente con DNI {} actualizado exitosamente.", dni);
        return ResponseEntity.ok(dtoConverter.convertToDto(clienteActualizado));
    }

    /**
     * Actualiza el teléfono de múltiples clientes en una sola solicitud.
     * Recibe una lista de ClienteTelefonoUpdateDTO en el cuerpo.
     * Permite a usuarios con roles 'ADMIN', 'MODERATOR' o 'USER'.
     *
     * @param updates La lista de DTOs con DNI y el nuevo teléfono para cada cliente.
     * @return ResponseEntity con la lista de ClienteResponseDTO de los clientes actualizados y HttpStatus.OK (200).
     */
    @PatchMapping("/telefono/batch") // /api/clientes/telefono/batch
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or hasRole('USER')")
    public ResponseEntity<List<ClienteResponseDTO>> updateClientesTelefonoBatch(@Valid @RequestBody List<ClienteTelefonoUpdateDTO> updates) {
        if (updates == null || updates.isEmpty()) {
            logger.warn("La lista de actualizaciones de teléfono para el lote está vacía.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Recibida la solicitud para actualizar teléfonos de múltiples clientes. Cantidad: {}", updates.size());

        List<ClienteResponseDTO> updatedClientes = new ArrayList<>();
        for (ClienteTelefonoUpdateDTO updateDto : updates) {
            logger.debug("Actualizando teléfono para DNI: {}", updateDto.getDni());
            Cliente clienteActualizado = clienteService.updateClienteTelefono(updateDto.getDni(), updateDto.getNuevoTelefono());
            updatedClientes.add(dtoConverter.convertToDto(clienteActualizado));
        }

        logger.info("Teléfono(s) de {} cliente(s) actualizado(s) exitosamente.", updatedClientes.size());
        return ResponseEntity.ok(updatedClientes);
    }

  
    /**
     * Elimina un único cliente por su DNI.
     * El DNI se pasa como variable de ruta.
     * Requiere el rol 'ADMIN'.
     *
     * @param dni El DNI del cliente a eliminar.
     * @return ResponseEntity con un mapa de confirmación y HttpStatus.OK (200).
     * @throws ClienteNotFoundException si el cliente no es encontrado.
     */
    @DeleteMapping("/{dni}") // /api/clientes/{dni}
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteClienteByDni(@PathVariable String dni) {
        logger.info("Recibida solicitud para eliminar cliente con DNI: {}", dni);
        clienteService.deleteCliente(dni);
        logger.info("Cliente con DNI {} eliminado exitosamente.", dni);
        // JSON de confirmación
        return ResponseEntity.ok().body(
                Map.of(
                        "message", "Cliente eliminado exitosamente",
                        "dni", dni
                )
        );
    }

    /**
     * Elimina múltiples clientes.
     * Recibe una lista de Strings (DNI's) en el cuerpo de la solicitud.
     * Requiere el rol 'ADMIN'.
     *
     * @param dnisToDelete La lista de DNIs de los clientes a eliminar.
     * @return ResponseEntity con una lista de mapas (DNI, estado) y HttpStatus.OK (200).
     */
    @DeleteMapping("/batch") // /api/clientes/batch
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, String>>> deleteClientesBatch(@RequestBody List<String> dnisToDelete) {
        if (dnisToDelete == null || dnisToDelete.isEmpty()) {
            logger.warn("La lista de DNIs a eliminar en el borrado por lotes está vacía.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Recibida la solicitud para eliminar múltiples clientes (batch). Cantidad: {}", dnisToDelete.size());

        List<Map<String, String>> results = new ArrayList<>();
        for (String dni : dnisToDelete) {
            try {
                clienteService.deleteCliente(dni);
                results.add(Map.of("dni", dni, "status", "eliminado", "message", "Cliente eliminado exitosamente."));
                logger.info("Cliente con DNI {} eliminado exitosamente.", dni);
            } catch (Exception e) {
                results.add(Map.of("dni", dni, "status", "error", "message", e.getMessage()));
                logger.error("Error al eliminar cliente con DNI {}: {}", dni, e.getMessage());
            }
        }

        logger.info("Procesada solicitud de eliminación por lotes para {} cliente(s).", dnisToDelete.size());
        return ResponseEntity.ok(results);
    }
}