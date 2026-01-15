package com.martinmzc.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http,
                                         JwtService jwtService) throws Exception {

    http
      .csrf(csrf -> csrf.disable())
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/ping").permitAll()
        .requestMatchers("/api/auth/login").permitAll()
        // H2 console (dev only)
        .requestMatchers("/h2-console/**").permitAll()
        .anyRequest().authenticated()
      )
      .addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

    // 允许 H2 console iframe
    http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

    // 默认 401/403 处理足够
    http.httpBasic(Customizer.withDefaults());

    return http.build();
  }
}