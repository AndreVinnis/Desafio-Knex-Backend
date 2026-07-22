package com.uepb.DesafioKnex.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uepb.DesafioKnex.dto.request.AuthRequest;
import com.uepb.DesafioKnex.dto.request.UserRequest;
import com.uepb.DesafioKnex.dto.response.UserResponse;
import com.uepb.DesafioKnex.model.User;
import com.uepb.DesafioKnex.model.enums.Role;
import com.uepb.DesafioKnex.security.JwtService;
import com.uepb.DesafioKnex.security.SecurityConfiguration;
import com.uepb.DesafioKnex.services.AuthService;
import com.uepb.DesafioKnex.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfiguration.class)
class AuthControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtService jwtService;

    private User loggedUser;
    private UserRequest request;
    private UserResponse response;

    @BeforeEach
    void setUp() {
        loggedUser = new User();
        loggedUser.setId(1L);
        loggedUser.setEmail("joao.silva@email.com");
        loggedUser.setRole(Role.CLIENT);

        request = new UserRequest(
                "João Silva",
                "joao.silva@email.com",
                "senha123",
                Role.CLIENT
        );

        response = new UserResponse(
                "João Silva",
                "joao.silva@email.com",
                Role.CLIENT,
                Instant.parse("2026-07-22T10:00:00Z")
        );
    }

    // ---------------------- POST /auth/register ----------------------

    @Test
    void register_deveRetornar200_quandoDadosValidos() throws Exception {
       when(userService.create(any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("joao.silva@email.com"));

        verify(userService).create(any(UserRequest.class));
    }

    // ---------------------- POST /auth/login ----------------------

    @Test
    void login_deveRetornar200ComToken_quandoCredenciaisValidas() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken(loggedUser, null);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(loggedUser)).thenReturn("fake-jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        mockMvc.perform(post("/auth/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.expiresIn").value(3600));

        verify(jwtService).generateToken(loggedUser);
    }

    @Test
    void login_devePropagarErro_quandoCredenciaisInvalidas() throws Exception {
        AuthRequest request = new AuthRequest("user@test.com", "senha-errada");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        mockMvc.perform(post("/auth/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}