  package com.martinmzc.backend.appointment;

import com.martinmzc.backend.user.User;
import com.martinmzc.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class AppointmentRepositoryTest {

  @Autowired UserRepository userRepository;
  @Autowired AppointmentRepository appointmentRepository;

  @Test
  void uniqueConstraint_shouldRejectDuplicateAppointment_forSameUserSamePayload() {
    User u = userRepository.save(new User("13800000000"));

    Appointment a1 = appointmentRepository.save(
      new Appointment(u, "meeting", LocalDate.parse("2026-01-13"), "10:00-11:00")
    );
    assertThat(a1.getId()).isNotNull();

    assertThatThrownBy(() ->
      appointmentRepository.save(
        new Appointment(u, "meeting", LocalDate.parse("2026-01-13"), "10:00-11:00")
      )
    ).isInstanceOf(DataIntegrityViolationException.class);
  }
}