package com.martinmzc.backend.user;

import jakarta.persistence.*;

@Entity
@Table(
  name = "users",
  uniqueConstraints = @UniqueConstraint(name = "uk_users_phone", columnNames = {"phone"})
)
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 32)
  private String phone;

  protected User() {}

  public User(String phone) {
    this.phone = phone;
  }

  public Long getId() {
    return id;
  }

  public String getPhone() {
    return phone;
  }
}