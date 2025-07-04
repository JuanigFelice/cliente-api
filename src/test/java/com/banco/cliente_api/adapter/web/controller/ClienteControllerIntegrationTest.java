package com.banco.cliente_api.adapter.web.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.banco.cliente_api.adapter.web.dto.ClienteRequestDTO;
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
//@ActiveProfiles("test")
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

        if (productoBancarioRepository.findByCodigo("CA").isEmpty()) {
            productoBancarioRepository.save(new ProductoBancario(null, "CA", "Caja de Ahorro"));
        }
        if (productoBancarioRepository.findByCodigo("TC").isEmpty()) {
            productoBancarioRepository.save(new ProductoBancario(null, "TC", "Tarjeta de Crédito"));
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

    @Test
    void testCrearCliente_AdminRole_Success() throws Exception {
        ClienteRequestDTO newCliente = new ClienteRequestDTO();
        newCliente.setDni("11111111");
        newCliente.setNombre("Cliente Admin");
        newCliente.setApellido("Apellido Admin");
        newCliente.setTelefono("1111111111");
        newCliente.setCelular("1511111111");
        newCliente.setCalle("Calle Falsa");
        newCliente.setNumero(123);
        newCliente.setCodigoPostal("C1000");
        newCliente.setProductosBancariosCodigos(Set.of("CA", "TC").stream().collect(Collectors.toList()));

        mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCliente)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dni").value(11111111L))
                .andExpect(jsonPath("$.nombre").value("Cliente Admin"))
                .andExpect(jsonPath("$.productosBancarios", hasSize(2)));
    }

    @Test
    void testCrearCliente_UserRole_Forbidden() throws Exception {
        ClienteRequestDTO newCliente = new ClienteRequestDTO();
        newCliente.setDni("22222222");
        newCliente.setNombre("Cliente User");
        newCliente.setApellido("Apellido User");
        newCliente.setProductosBancariosCodigos(Set.of("CA").stream().collect(Collectors.toList()));

        mockMvc.perform(post("/api/clientes")
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
        newCliente.setProductosBancariosCodigos(Set.of("CA").stream().collect(Collectors.toList()));

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCliente)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetAllClientes_ModeratorRole_Success() throws Exception {
        testCrearCliente_AdminRole_Success();

        mockMvc.perform(get("/api/clientes")
                        .header("Authorization", "Bearer " + moderatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].dni").exists());
    }

    @Test
    void testGetAllClientes_UserRole_Forbidden() throws Exception {
        mockMvc.perform(get("/api/clientes")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetClienteByDni_UserRole_Success() throws Exception {
        ClienteRequestDTO clienteToCreate = new ClienteRequestDTO();
        clienteToCreate.setDni("44444444");
        clienteToCreate.setNombre("Cliente DNI");
        clienteToCreate.setApellido("Apellido DNI");
        clienteToCreate.setTelefono("1234");
        clienteToCreate.setCelular("5678");
        clienteToCreate.setProductosBancariosCodigos(Set.of("CJAHRR")
        		.stream().collect(Collectors.toList()));

        

        mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteToCreate)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/clientes/44444444")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value(44444444L))
                .andExpect(jsonPath("$.nombre").value("Cliente DNI"));
    }

    @Test
    void testUpdateClienteTelefono_ModeratorRole_Success() throws Exception {
        ClienteRequestDTO clienteToUpdate = new ClienteRequestDTO();
        clienteToUpdate.setDni("55555555");
        clienteToUpdate.setNombre("Cliente Update");
        clienteToUpdate.setApellido("Apellido Update");
        clienteToUpdate.setTelefono("0000000000");
        clienteToUpdate.setCelular("0000000000");
        clienteToUpdate.setProductosBancariosCodigos(Set.of("CJAHRR")
        		.stream().collect(Collectors.toList()));

        mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteToUpdate)))
                .andExpect(status().isCreated());

        String nuevoTelefono = "9999999999";
        mockMvc.perform(patch("/api/clientes/55555555/telefono")
                        .header("Authorization", "Bearer " + moderatorToken)
                        .param("nuevoTelefono", nuevoTelefono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefono").value(nuevoTelefono));
    }

    @Test
    void testDeleteCliente_AdminRole_Success() throws Exception {
        ClienteRequestDTO clienteToDelete = new ClienteRequestDTO();
        clienteToDelete.setDni("66666666");
        clienteToDelete.setNombre("Cliente Delete");
        clienteToDelete.setApellido("Apellido Delete");
        clienteToDelete.setTelefono("123");
        clienteToDelete.setCelular("456");
        clienteToDelete.setProductosBancariosCodigos(Set.of("CJAHRR")
        		.stream().collect(Collectors.toList()));

        mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteToDelete)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/clientes/66666666")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/clientes/66666666")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteCliente_UserRole_Forbidden() throws Exception {
        ClienteRequestDTO clienteToDelete = new ClienteRequestDTO();
        clienteToDelete.setDni("77777777");
        clienteToDelete.setNombre("Cliente Delete Forbidden");
        clienteToDelete.setApellido("Apellido Delete Forbidden");
        clienteToDelete.setTelefono("123");
        clienteToDelete.setCelular("456");
        clienteToDelete.setProductosBancariosCodigos(Set.of("CJAHRR")
        		.stream().collect(Collectors.toList()));

        mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteToDelete)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/clientes/77777777")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}