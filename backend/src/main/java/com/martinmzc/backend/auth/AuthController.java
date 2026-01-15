package com.martinmzc.backend.auth;

import com.martinmzc.backend.security.JwtService;
import com.martinmzc.backend.user.User;
import com.martinmzc.backend.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserRepository userRepository;
  private final JwtService jwtService;

  public AuthController(UserRepository userRepository, JwtService jwtService) {
    this.userRepository = userRepository;
    this.jwtService = jwtService;
  }

  // DTO
  public record LoginRequest(
    @NotBlank
    @Pattern(regexp = "^[0-9]{6,20}$", message = "phone must be digits")
    String phone,

    @NotBlank
    String code
  ) {}

  public record LoginResponse(String token, long userId) {}

  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest req) {
    if (!"123456".equals(req.code())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification code");
    }

    User user = userRepository.findByPhone(req.phone())
      .orElseGet(() -> userRepository.save(new User(req.phone())));

    String token = jwtService.generateToken(user.getId(), user.getPhone());
    return new LoginResponse(token, user.getId());
  }
}