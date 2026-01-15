package com.martinmzc.backend.auth;

import com.martinmzc.backend.security.JwtService;
import com.martinmzc.backend.user.User;
import com.martinmzc.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerUnitTest {

  @Test
  void login_shouldCreateUserAndReturnToken_whenUserNotExists() {
    UserRepository userRepository = mock(UserRepository.class);
    JwtService jwtService = mock(JwtService.class);

    when(userRepository.findByPhone("13800000000")).thenReturn(Optional.empty());

    User saved = mock(User.class);
    when(saved.getId()).thenReturn(1L);
    when(saved.getPhone()).thenReturn("13800000000");
    when(userRepository.save(any(User.class))).thenReturn(saved);

    when(jwtService.generateToken(1L, "13800000000")).thenReturn("token-abc");

    AuthController controller = new AuthController(userRepository, jwtService);

    var resp = controller.login(new AuthController.LoginRequest("13800000000", "123456"));

    assertThat(resp.token()).isEqualTo("token-abc");
    assertThat(resp.userId()).isEqualTo(1L);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    assertThat(captor.getValue().getPhone()).isEqualTo("13800000000");

    verify(jwtService).generateToken(1L, "13800000000");
    verifyNoMoreInteractions(jwtService);
  }

  @Test
  void login_shouldReuseExistingUser_whenUserExists() {
    UserRepository userRepository = mock(UserRepository.class);
    JwtService jwtService = mock(JwtService.class);

    User existing = mock(User.class);
    when(existing.getId()).thenReturn(99L);
    when(existing.getPhone()).thenReturn("13800000000");
    when(userRepository.findByPhone("13800000000")).thenReturn(Optional.of(existing));

    when(jwtService.generateToken(99L, "13800000000")).thenReturn("token-xyz");

    AuthController controller = new AuthController(userRepository, jwtService);

    var resp = controller.login(new AuthController.LoginRequest("13800000000", "123456"));

    assertThat(resp.userId()).isEqualTo(99L);
    assertThat(resp.token()).isEqualTo("token-xyz");

    verify(userRepository, never()).save(any());
    verify(jwtService).generateToken(99L, "13800000000");
  }

  @Test
  void login_shouldThrow400_whenCodeWrong() {
    UserRepository userRepository = mock(UserRepository.class);
    JwtService jwtService = mock(JwtService.class);

    AuthController controller = new AuthController(userRepository, jwtService);

    assertThatThrownBy(() ->
      controller.login(new AuthController.LoginRequest("13800000000", "000000"))
    )
      .isInstanceOf(ResponseStatusException.class)
      .hasMessageContaining("400")
      .hasMessageContaining("Invalid verification code");

    verifyNoInteractions(userRepository, jwtService);
  }
}