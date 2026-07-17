package com.uepb.DesafioKnex.services;

import com.uepb.DesafioKnex.dto.request.UserRequest;
import com.uepb.DesafioKnex.dto.response.UserResponse;
import com.uepb.DesafioKnex.exceptions.UserAlreadyExist;
import com.uepb.DesafioKnex.model.User;
import com.uepb.DesafioKnex.model.enums.Role;
import com.uepb.DesafioKnex.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserService userService;

    private UserRequest request;

    @BeforeEach
    void setup(){
        request = new UserRequest("João", "joao@email.com", "123456", Role.CLIENT);
    }

    @Test
    @DisplayName("Deve criar um usuário com sucesso quando os dados forem válidos")
    void create_WithValidData_ReturnsUserResponse() {
        // Arrange
        User savedUser = User.builder()
                .id(1L)
                .name("João")
                .email("joao@email.com")
                .hashPassword("hashed_123456")
                .role(Role.CLIENT)
                .createdAt(Instant.now())
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(null);
        when(encoder.encode(request.password())).thenReturn("hashed_123456");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserResponse response = userService.create(request);

        // Assert
        assertNotNull(response);
        assertEquals(savedUser.getId(), response.id());
        assertEquals(savedUser.getName(), response.name());
        assertEquals(savedUser.getEmail(), response.email());
        assertEquals(savedUser.getRole(), response.role());
        verify(userRepository, times(1)).findByEmail(request.email());
        verify(encoder, times(1)).encode(request.password());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar UserAlreadyExist quando o email já estiver cadastrado")
    void create_WithExistingEmail_ThrowsUserAlreadyExist() {
        // Arrange
        User existingUser = new User();

        when(userRepository.findByEmail(request.email())).thenReturn(existingUser);

        // Act & Assert
        assertThrows(UserAlreadyExist.class, () -> userService.create(request));

        // Verifica que não tentou salvar nem encodar a senha
        verify(encoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando o formato do email for inválido")
    void create_WithInvalidEmailFormat_ThrowsIllegalArgumentException() {
        // Arrange
        UserRequest request = new UserRequest("Pedro", "email-invalido.com", "senha123", Role.CLIENT);

        when(userRepository.findByEmail(request.email())).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.create(request));
        assertEquals("Email inválido", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando o email for nulo ou vazio")
    void create_WithNullOrBlankEmail_ThrowsIllegalArgumentException() {
        // Arrange (Testando com string vazia)
        UserRequest request = new UserRequest("Ana", "   ", "senha123", Role.CLIENT);

        when(userRepository.findByEmail(request.email())).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.create(request));
        assertEquals("Email inválido", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }
}