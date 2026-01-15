package com.martinmzc.backend.appointment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
  Optional<Appointment> findByUserIdAndServiceNameAndDateAndTimeSlot(
    Long userId, String serviceName, LocalDate date, String timeSlot
  );

  List<Appointment> findByUserIdOrderByCreatedAtDesc(Long userId);
}