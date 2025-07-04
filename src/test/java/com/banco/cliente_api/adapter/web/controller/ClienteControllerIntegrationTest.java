package com.banco.cliente_api.adapter.web.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.banco.cliente_api.adapter.web.dto.ClienteRequestDTO;
import com.banco.cliente_api.adapter.web.dto.ClienteTelefonoUpdateDTO;
import com.banco.cliente_api.model.ProductoBancario;
import com.banco.cliente_api.repository.ClienteRepository;
import com.banco.cliente_api.repository.ProductoBancarioRepository;
import com.banco.cliente_api.security.entity.EnumRole;
import com.banco.cliente_api.security.entity.Role;
import com.banco.cliente_api.security.payload.request.LoginRequest;
import com.banco.cliente_api.security.payload.request.SignupRequest;
import com.banco.cliente_api.security.repository.RoleRepository;
import com.banco.cliente_api.security.repository.UsuarioBancoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
//@ActiveProfiles("test") // Descomentar si usas un perfil de test específico
public class ClienteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UsuarioBancoRepository usuarioBancoRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ProductoBancarioRepository productoBancarioRepository;
    @Autowired
    private PasswordEncoder encoder;

    private String adminToken;
    private String moderatorToken;
    private String userToken;

    private final String ADMIN_USERNAME = "testadmin";
    private final String MOD_USERNAME = "testmod";
    private final String USER_USERNAME = "testuser";
    private final String PASSWORD = "password123";

    @BeforeEach
    void setup() throws Exception {
        clienteRepository.deleteAllInBatch();
        usuarioBancoRepository.deleteAllInBatch();
        roleRepository.deleteAllInBatch();
        productoBancarioRepository.deleteAllInBatch();

        if (roleRepository.findByName(EnumRole.ROLE_ADMIN).isEmpty()) {
            roleRepository.save(new Role(null, EnumRole.ROLE_ADMIN));
        }
        if (roleRepository.findByName(EnumRole.ROLE_MODERATOR).isEmpty()) {
            roleRepository.save(new Role(null, EnumRole.ROLE_MODERATOR));
        }
        if (roleRepository.findByName(EnumRole.ROLE_USER).isEmpty()) {
            roleRepository.save(new Role(null, EnumRole.ROLE_USER));
        }

        registerAndObtainToken(ADMIN_USERNAME, PASSWORD, Set.of("admin"));
        adminToken = obtainJwtToken(ADMIN_USERNAME, PASSWORD);

        registerAndObtainToken(MOD_USERNAME, PASSWORD, Set.of("mod"));
        moderatorToken = obtainJwtToken(MOD_USERNAME, PASSWORD);

        registerAndObtainToken(USER_USERNAME, PASSWORD, Set.of("user"));
        userToken = obtainJwtToken(USER_USERNAME, PASSWORD);

        // Asegurarse de que los productos bancarios existen para las pruebas
        if (productoBancarioRepository.findByCodigo("CA").isEmpty()) {
            productoBancarioRepository.save(new ProductoBancario(null, "CA", "Caja de Ahorro"));
        }
        if (productoBancarioRepository.findByCodigo("TC").isEmpty()) {
            productoBancarioRepository.save(new ProductoBancario(null, "TC", "Tarjeta de Crédito"));
        }
        if (productoBancarioRepository.findByCodigo("CJAHRR").isEmpty()) {
            productoBancarioRepository.save(new ProductoBancario(null, "CJAHRR", "Cuenta de Ahorro"));
        }
        if (productoBancarioRepository.findByCodigo("PZOF").isEmpty()) {
            productoBancarioRepository.save(new ProductoBancario(null, "PZOF", "Plazo Fijo"));
        }
        if (productoBancarioRepository.findByCodigo("CHEQ").isEmpty()) {
            productoBancarioRepository.save(new ProductoBancario(null, "CHEQ", "Cuenta Corriente"));
        }
        if (productoBancarioRepository.findByCodigo("TJCREDITO").isEmpty()) {
            productoBancarioRepository.save(new ProductoBancario(null, "TJCREDITO", "Tarjeta de Crédito Adicional"));
        }
    }

    private void registerAndObtainToken(String username, String password, Set<String> roles) throws Exception {
        if (!usuarioBancoRepository.existsByUsername(username)) {
            SignupRequest signupRequest = new SignupRequest(username, password, roles);
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signupRequest)))
                    .andExpect(status().isOk());
        }
    }

    private String obtainJwtToken(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);
        MvcResult result = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String responseContent = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseContent).get("token").asText();
    }

    // --- Tests de CREACIÓN (POST) ---
 
    @Test
    void testCrearCliente_AdminRole_Success_Single() throws Exception {
        ClienteRequestDTO newCliente = new ClienteRequestDTO();
        newCliente.setDni("11111111");
        newCliente.setNombre("Cliente Admin Single");
        newCliente.setApellido("Apellido Admin Single");
        newCliente.setTelefono("1111111111");
        newCliente.setCelular("1511111111");
        newCliente.setCalle("Calle Unica");
        newCliente.setNumero(101);
        newCliente.setCodigoPostal("C1001");
        newCliente.setProductosBancariosCodigos(Set.of("CA", "TC").stream().collect(Collectors.toList()));

        mockMvc.perform(post("/api/clientes") // Endpoint para un solo cliente
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCliente)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dni").value("11111111"))
                .andExpect(jsonPath("$.nombre").value("Cliente Admin Single"))
                .andExpect(jsonPath("$.productosBancarios", hasSize(2)));
    }

    @Test
    void testCrearClientes_AdminRole_Success_Multiple() throws Exception {
        ClienteRequestDTO cliente1 = new ClienteRequestDTO();
        cliente1.setDni("11111112");
        cliente1.setNombre("Multi 1");
        cliente1.setApellido("Apellido Multi 1");
        cliente1.setTelefono("1111111112");
        cliente1.setCelular("1511111112");
        cliente1.setCalle("Calle Multi 1");
        cliente1.setNumero(102);
        cliente1.setCodigoPostal("C1002");
        cliente1.setProductosBancariosCodigos(Set.of("CJAHRR").stream().collect(Collectors.toList()));

        ClienteRequestDTO cliente2 = new ClienteRequestDTO();
        cliente2.setDni("11111113");
        cliente2.setNombre("Multi 2");
        cliente2.setApellido("Apellido Multi 2");
        cliente2.setTelefono("1111111113");
        cliente2.setCelular("1511111113");
        cliente2.setCalle("Calle Multi 2");
        cliente2.setNumero(103);
        cliente2.setCodigoPostal("C1003");
        cliente2.setProductosBancariosCodigos(Set.of("TJCREDITO").stream().collect(Collectors.toList()));

        List<ClienteRequestDTO> clientes = Arrays.asList(cliente1, cliente2);

        mockMvc.perform(post("/api/clientes/batch") // Nuevo endpoint para crear múltiples
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientes)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2))) 
                .andExpect(jsonPath("$[0].dni").value("11111112"))
                .andExpect(jsonPath("$[1].dni").value("11111113"));
    }

    @Test
    void testCrearCliente_UserRole_Forbidden() throws Exception {
        ClienteRequestDTO newCliente = new ClienteRequestDTO();
        newCliente.setDni("22222222");
        newCliente.setNombre("Cliente User");
        newCliente.setApellido("Apellido User");
        newCliente.setTelefono("1234");
        newCliente.setCelular("5678");
        newCliente.setProductosBancariosCodigos(Set.of("CJAHRR").stream().collect(Collectors.toList()));

        mockMvc.perform(post("/api/clientes") // Endpoint para un solo cliente
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCliente)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCrearCliente_NoAuth_Unauthorized() throws Exception {
        ClienteRequestDTO newCliente = new ClienteRequestDTO();
        newCliente.setDni("33333333");
        newCliente.setNombre("Cliente NoAuth");
        newCliente.setApellido("Apellido NoAuth");
        newCliente.setTelefono("1234");
        newCliente.setCelular("5678");
        newCliente.setProductosBancariosCodigos(Set.of("CJAHRR").stream().collect(Collectors.toList()));

        mockMvc.perform(post("/api/clientes") // Endpoint para un solo cliente
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCliente)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCrearClientes_EmptyList_BadRequest() throws Exception {
        mockMvc.perform(post("/api/clientes/batch") // Endpoint para crear múltiples
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Collections.emptyList())))
                .andExpect(status().isBadRequest());
    }

    // --- Tests de Recuperación (GET) ---
   
    @Test
    void testGetAllClientes_ModeratorRole_Success() throws Exception {
        // Asegurar que hay al menos un cliente
        testCrearCliente_AdminRole_Success_Single();

        mockMvc.perform(get("/api/clientes")
                        .header("Authorization", "Bearer " + moderatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].dni").exists());
    }

    @Test
    void testGetAllClientes_UserRole_Success() throws Exception {
        // Asegurar que hay al menos un cliente
        testCrearCliente_AdminRole_Success_Single(); 

        mockMvc.perform(get("/api/clientes")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void testGetClienteByDni_UserRole_Success() throws Exception {
        // Creamos un cliente para la prueba con rol ADMIN
        ClienteRequestDTO clienteToCreate = new ClienteRequestDTO();
        clienteToCreate.setDni("44444444");
        clienteToCreate.setNombre("Cliente DNI");
        clienteToCreate.setApellido("Apellido DNI");
        clienteToCreate.setTelefono("1234");
        clienteToCreate.setCelular("5678");
        clienteToCreate.setProductosBancariosCodigos(Set.of("CJAHRR").stream().collect(Collectors.toList()));

        mockMvc.perform(post("/api/clientes") // Endpoint para un solo cliente
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteToCreate)))
                .andExpect(status().isCreated());

        // Intentamos obtenerlo con rol USER
        mockMvc.perform(get("/api/clientes/44444444")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("44444444"))
                .andExpect(jsonPath("$.nombre").value("Cliente DNI"));
    }

    // --- Tests de Actualización (PATCH) ---
    // (Ahora separados para un solo cliente y para batch)
    
    @Test
    void testUpdateClienteTelefono_ModeratorRole_Success_Single() throws Exception {
        // Creamos un cliente para la prueba
        ClienteRequestDTO clienteToUpdate = new ClienteRequestDTO();
        clienteToUpdate.setDni("55555555");
        clienteToUpdate.setNombre("Cliente Update");
        clienteToUpdate.setApellido("Apellido Update");
        clienteToUpdate.setTelefono("0000000000");
        clienteToUpdate.setCelular("0000000000");
        clienteToUpdate.setProductosBancariosCodigos(Set.of("CJAHRR").stream().collect(Collectors.toList()));

        mockMvc.perform(post("/api/clientes") // Endpoint para un solo cliente
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteToUpdate)))
                .andExpect(status().isCreated());

        // Preparamos el DTO de actualización para un solo cliente
        ClienteTelefonoUpdateDTO updateDto = new ClienteTelefonoUpdateDTO("55555555", "9999999999");

        mockMvc.perform(patch("/api/clientes/{dni}/telefono", "55555555") 
                        .header("Authorization", "Bearer " + moderatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("55555555")) 
                .andExpect(jsonPath("$.telefono").value("9999999999"));
    }

    @Test
    void testUpdateClientesTelefono_ModeratorRole_Success_Multiple() throws Exception {
        // Crear clientes para la prueba (usando el endpoint batch)
        ClienteRequestDTO cliente1 = new ClienteRequestDTO();
        cliente1.setDni("55555556");
        cliente1.setNombre("Update Multi 1");
        cliente1.setApellido("Apellido Multi 1");
        cliente1.setTelefono("111");
        cliente1.setCelular("111");
        cliente1.setProductosBancariosCodigos(Set.of("CJAHRR").stream().collect(Collectors.toList()));

        ClienteRequestDTO cliente2 = new ClienteRequestDTO();
        cliente2.setDni("55555557");
        cliente2.setNombre("Update Multi 2");
        cliente2.setApellido("Apellido Multi 2");
        cliente2.setTelefono("222");
        cliente2.setCelular("222");
        cliente2.setProductosBancariosCodigos(Set.of("TJCREDITO").stream().collect(Collectors.toList()));

        mockMvc.perform(post("/api/clientes/batch") // Endpoint para crear múltiples
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Arrays.asList(cliente1, cliente2))))
                .andExpect(status().isCreated());
        
        List<ClienteTelefonoUpdateDTO> updates = Arrays.asList(
                new ClienteTelefonoUpdateDTO("55555556", "3333333333"),
                new ClienteTelefonoUpdateDTO("55555557", "4444444444")
        );

        mockMvc.perform(patch("/api/clientes/telefono/batch") // Nuevo endpoint para actualizar múltiples
                        .header("Authorization", "Bearer " + moderatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].dni").value("55555556"))
                .andExpect(jsonPath("$[0].telefono").value("3333333333"))
                .andExpect(jsonPath("$[1].dni").value("55555557"))
                .andExpect(jsonPath("$[1].telefono").value("4444444444"));
    }

    @Test
    void testUpdateClienteTelefono_NoExiste_NotFound() throws Exception {
        ClienteTelefonoUpdateDTO updateDto = new ClienteTelefonoUpdateDTO("99999999", "123456789");
        
        mockMvc.perform(patch("/api/clientes/{dni}/telefono", "99999999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("no encontrado"))); 
    }

    @Test
    void testUpdateClientesTelefono_EmptyList_BadRequest() throws Exception {
        mockMvc.perform(patch("/api/clientes/telefono/batch") // Endpoint para batch
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Collections.emptyList())))
                .andExpect(status().isBadRequest());
    }

    // --- Tests de Eliminación (DELETE) ---
   
    @Test
    void testDeleteCliente_AdminRole_Success_Single() throws Exception {
        // Creamos un cliente para la prueba
        ClienteRequestDTO clienteToDelete = new ClienteRequestDTO();
        clienteToDelete.setDni("66666666");
        clienteToDelete.setNombre("Cliente Delete Single");
        clienteToDelete.setApellido("Apellido Delete Single");
        clienteToDelete.setTelefono("123");
        clienteToDelete.setCelular("456");
        clienteToDelete.setProductosBancariosCodigos(Set.of("CJAHRR").stream().collect(Collectors.toList()));

        mockMvc.perform(post("/api/clientes") // Endpoint para un solo cliente
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteToDelete)))
                .andExpect(status().isCreated());

        // Eliminamos el cliente usando el DNI en la URL
        mockMvc.perform(delete("/api/clientes/{dni}", "66666666") // Nuevo endpoint para un solo cliente
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("66666666"))
                .andExpect(jsonPath("$.message").value("Cliente eliminado exitosamente"));

        // Verificamos que el cliente ya no existe
        mockMvc.perform(get("/api/clientes/66666666")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteClientes_AdminRole_Success_Multiple() throws Exception {
        // Crear múltiples clientes para eliminar (usando el endpoint batch)
        ClienteRequestDTO cliente1 = new ClienteRequestDTO();
        cliente1.setDni("66666667");
        cliente1.setNombre("Delete Multi 1");
        cliente1.setApellido("Apellido Delete Multi 1");
        cliente1.setProductosBancariosCodigos(List.of("CA"));
        cliente1.setTelefono("111");
        cliente1.setCelular("111");

        ClienteRequestDTO cliente2 = new ClienteRequestDTO();
        cliente2.setDni("66666668");
        cliente2.setNombre("Delete Multi 2");
        cliente2.setApellido("Apellido Delete Multi 2");
        cliente2.setProductosBancariosCodigos(List.of("TC"));
        cliente2.setTelefono("222");
        cliente2.setCelular("222");


        mockMvc.perform(post("/api/clientes/batch") // Endpoint para crear múltiples
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Arrays.asList(cliente1, cliente2))))
                .andExpect(status().isCreated());

        // DNIs para eliminar (uno existente, uno existente, uno inexistente)
        List<String> dnisToDelete = Arrays.asList("66666667", "66666668", "99999999");

        mockMvc.perform(delete("/api/clientes/batch") // Nuevo endpoint para eliminar múltiples
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dnisToDelete)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3))) // Tres resultados esperados
                .andExpect(jsonPath("$[?(@.dni == '66666667')].status").value("eliminado"))
                .andExpect(jsonPath("$[?(@.dni == '66666668')].status").value("eliminado")) 
                .andExpect(jsonPath("$[?(@.dni == '99999999')].status").value("error")); 
                                                                                            
    }

    @Test
    void testDeleteCliente_UserRole_Forbidden() throws Exception {
        // Creamos un cliente para la prueba
        ClienteRequestDTO clienteToDelete = new ClienteRequestDTO();
        clienteToDelete.setDni("77777777");
        clienteToDelete.setNombre("Cliente Delete Forbidden");
        clienteToDelete.setApellido("Apellido Delete Forbidden");
        clienteToDelete.setTelefono("123");
        clienteToDelete.setCelular("456");
        clienteToDelete.setProductosBancariosCodigos(Set.of("CJAHRR").stream().collect(Collectors.toList()));

        mockMvc.perform(post("/api/clientes") // Endpoint para un solo cliente
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteToDelete)))
                .andExpect(status().isCreated());

        // Intentamos eliminarlo con rol USER (prohibido)
        mockMvc.perform(delete("/api/clientes/{dni}", "77777777")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteClientes_EmptyList_BadRequest() throws Exception {
        mockMvc.perform(delete("/api/clientes/batch") // Endpoint para eliminar múltiples
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Collections.emptyList())))
                .andExpect(status().isBadRequest());
    }

    // --- Otros Tests ---
   
    @Test
    void testCrearCliente_DatosInvalidos_BadRequest() throws Exception {
        ClienteRequestDTO clienteInvalido = new ClienteRequestDTO();
        clienteInvalido.setDni("");
        clienteInvalido.setNombre(""); 
        clienteInvalido.setApellido("");
        clienteInvalido.setTelefono("abc");
        clienteInvalido.setCelular("def"); 
        clienteInvalido.setCalle(""); 
        clienteInvalido.setCodigoPostal(""); 
        clienteInvalido.setNumero(null); 
        clienteInvalido.setProductosBancariosCodigos(List.of()); 

        mockMvc.perform(post("/api/clientes") // Endpoint para un solo cliente
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Error de validación")));
    }

    @Test
    void testCrearCliente_DniDuplicado_BadRequest() throws Exception {
        ClienteRequestDTO cliente = new ClienteRequestDTO();
        cliente.setDni("88888888");
        cliente.setNombre("Cliente Duplicado");
        cliente.setApellido("Apellido Duplicado");
        cliente.setTelefono("1234567890");
        cliente.setCelular("0987654321");
        cliente.setCalle("Calle Duplicada");
        cliente.setNumero(1);
        cliente.setCodigoPostal("C1000");
        cliente.setProductosBancariosCodigos(Set.of("CA").stream().toList());

        // Crear el cliente por primera vez
        mockMvc.perform(post("/api/clientes") // Endpoint para un solo cliente
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isCreated());

        // Intentar crearlo de nuevo
        mockMvc.perform(post("/api/clientes") // Endpoint para un solo cliente
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("ya existe")));
    }

    @Test
    void testGetClienteByDni_NoExiste_NotFound() throws Exception {
        mockMvc.perform(get("/api/clientes/99999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("no encontrado")));
    }

    @Test
    void testGetClientesByProductoBancario_SinResultados() throws Exception {
        mockMvc.perform(get("/api/clientes/por-producto/NOEXISTE")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("No se encontraron clientes")));
    }

    @Test
    void testGetClientesByProductoBancario_ConResultados() throws Exception {
        // Crear dos clientes con productos distintos
        ClienteRequestDTO cliente1 = new ClienteRequestDTO();
        cliente1.setDni("12345678");
        cliente1.setNombre("Cliente Producto 1");
        cliente1.setApellido("Apellido 1");
        cliente1.setTelefono("1111111111");
        cliente1.setCelular("2222222222");
        cliente1.setCalle("Calle Prod 1");
        cliente1.setNumero(1);
        cliente1.setCodigoPostal("C1001");
        cliente1.setProductosBancariosCodigos(List.of("PZOF", "CHEQ"));

        ClienteRequestDTO cliente2 = new ClienteRequestDTO();
        cliente2.setDni("87654321");
        cliente2.setNombre("Cliente Producto 2");
        cliente2.setApellido("Apellido 2");
        cliente2.setTelefono("3333333333");
        cliente2.setCelular("4444444444");
        cliente2.setCalle("Calle Prod 2");
        cliente2.setNumero(2);
        cliente2.setCodigoPostal("C1002");
        cliente2.setProductosBancariosCodigos(List.of("TJCREDITO", "PZOF"));

        // Se usa el endpoint de crear múltiples clientes
        mockMvc.perform(post("/api/clientes/batch") // Nuevo endpoint para crear múltiples
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Arrays.asList(cliente1, cliente2))))
                .andExpect(status().isCreated());

        // Buscar clientes por producto PZOF (debería traer ambos)
        mockMvc.perform(get("/api/clientes/por-producto/PZOF")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.dni == '12345678')]").exists())
                .andExpect(jsonPath("$[?(@.dni == '87654321')]").exists());


        // Buscar clientes por producto CHEQ (solo cliente1)
        mockMvc.perform(get("/api/clientes/por-producto/CHEQ")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].dni").value("12345678"));

        // Buscar clientes por producto TJCREDITO (solo cliente2)
        mockMvc.perform(get("/api/clientes/por-producto/TJCREDITO")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].dni").value("87654321"));
    }

    @Test
    void testCrearCliente_ProductoInexistente_BadRequest() throws Exception {
        ClienteRequestDTO cliente = new ClienteRequestDTO();
        cliente.setDni("99988877");
        cliente.setNombre("Cliente Producto Inexistente");
        cliente.setApellido("Apellido");
        cliente.setTelefono("1234567890");
        cliente.setCelular("0987654321");
        cliente.setCalle("Calle Inexistente");
        cliente.setNumero(1);
        cliente.setCodigoPostal("C0000");
        cliente.setProductosBancariosCodigos(List.of("NOEXISTE"));

        mockMvc.perform(post("/api/clientes") // Endpoint para un solo cliente
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("producto bancario")));
    }
}