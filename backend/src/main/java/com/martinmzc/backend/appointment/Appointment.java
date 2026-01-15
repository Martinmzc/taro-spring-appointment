package com.martinmzc.backend.appointment;

import com.martinmzc.backend.user.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
  name = "appointments",
  uniqueConstraints = @UniqueConstraint(
    name = "uk_appointments_user_service_date_slot",
    columnNames = {"user_id", "service_name", "date", "time_slot"}
  )
)
public class Appointment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "service_name", nullable = false, length = 100)
  private String serviceName;

  @Column(nullable = false)
  private LocalDate date;

  @Column(name = "time_slot", nullable = false, length = 50)
  private String timeSlot;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  protected Appointment() {}

  public Appointment(User user, String serviceName, LocalDate date, String timeSlot) {
    this.user = user;
    this.serviceName = serviceName;
    this.date = date;
    this.timeSlot = timeSlot;
  }

  public Long getId() { return id; }
  public User getUser() { return user; }
  public String getServiceName() { return serviceName; }
  public LocalDate getDate() { return date; }
  public String getTimeSlot() { return timeSlot; }
  public Instant getCreatedAt() { return createdAt; }
}