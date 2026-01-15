package com.martinmzc.backend.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  public JwtAuthFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {

    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header == null || !header.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = header.substring("Bearer ".length()).trim();
    try {
      Claims claims = jwtService.parse(token);
      long userId = Long.parseLong(claims.getSubject());

      var principal = new UserPrincipal(userId);
      var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(auth);
    } catch (Exception ex) {
      // token 无效：清空上下文，继续走后续（最终会被鉴权拦截返回 401）
      SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
  }
}