package com.martinmzc.backend.appointment;

import com.martinmzc.backend.security.UserPrincipal;
import com.martinmzc.backend.user.User;
import com.martinmzc.backend.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

  private final AppointmentRepository appointmentRepository;
  private final UserRepository userRepository;

  public AppointmentController(AppointmentRepository appointmentRepository, UserRepository userRepository) {
    this.appointmentRepository = appointmentRepository;
    this.userRepository = userRepository;
  }

  public record CreateAppointmentRequest(
    @NotBlank String serviceName,
    @NotNull LocalDate date,
    @NotBlank String timeSlot
  ) {}

  public record AppointmentResponse(
    long id,
    String serviceName,
    LocalDate date,
    String timeSlot,
    Instant createdAt
  ) {
    static AppointmentResponse from(Appointment a) {
      return new AppointmentResponse(a.getId(), a.getServiceName(), a.getDate(), a.getTimeSlot(), a.getCreatedAt());
    }
  }

  private long currentUserId(Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal p)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
    }
    return p.getUserId();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.OK)
  public AppointmentResponse create(@Valid @RequestBody CreateAppointmentRequest req, Authentication authentication) {
    long userId = currentUserId(authentication);

    User user = userRepository.findById(userId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    // 幂等：先查
    return appointmentRepository
      .findByUserIdAndServiceNameAndDateAndTimeSlot(userId, req.serviceName(), req.date(), req.timeSlot())
      .map(AppointmentResponse::from)
      .orElseGet(() -> {
        try {
          Appointment saved = appointmentRepository.save(new Appointment(user, req.serviceName(), req.date(), req.timeSlot()));
          return AppointmentResponse.from(saved);
        } catch (DataIntegrityViolationException dup) {
          // 并发下可能同时插入：靠唯一约束兜底，然后查回已存在记录
          return appointmentRepository
            .findByUserIdAndServiceNameAndDateAndTimeSlot(userId, req.serviceName(), req.date(), req.timeSlot())
            .map(AppointmentResponse::from)
            .orElseThrow(() -> dup);
        }
      });
  }

  @GetMapping("/me")
  public List<AppointmentResponse> my(Authentication authentication) {
    long userId = currentUserId(authentication);
    return appointmentRepository.findByUserIdOrderByCreatedAtDesc(userId)
      .stream()
      .map(AppointmentResponse::from)
      .toList();
  }
}