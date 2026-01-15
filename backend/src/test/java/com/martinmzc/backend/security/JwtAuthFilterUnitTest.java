package com.martinmzc.backend.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthFilterUnitTest {

  @AfterEach
  void cleanup() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void doFilter_shouldSkip_whenNoAuthorizationHeader() throws Exception {
    JwtService jwtService = mock(JwtService.class);
    JwtAuthFilter filter = new JwtAuthFilter(jwtService);

    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse res = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);

    when(req.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

    filter.doFilter(req, res, chain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(chain).doFilter(req, res);
    verifyNoInteractions(jwtService);
  }

  @Test
  void doFilter_shouldAuthenticate_whenTokenValid() throws Exception {
    JwtService jwtService = mock(JwtService.class);
    JwtAuthFilter filter = new JwtAuthFilter(jwtService);

    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse res = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);

    when(req.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer good.token");

    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("123");
    when(jwtService.parse("good.token")).thenReturn(claims);

    filter.doFilter(req, res, chain);

    var auth = SecurityContextHolder.getContext().getAuthentication();
    assertThat(auth).isNotNull();
    assertThat(auth.getPrincipal()).isInstanceOf(UserPrincipal.class);
    assertThat(((UserPrincipal) auth.getPrincipal()).getUserId()).isEqualTo(123L);

    verify(jwtService).parse("good.token");
    verify(chain).doFilter(req, res);
  }

  @Test
  void doFilter_shouldClearContext_whenTokenInvalid() throws Exception {
    JwtService jwtService = mock(JwtService.class);
    JwtAuthFilter filter = new JwtAuthFilter(jwtService);

    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse res = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);

    when(req.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer bad.token");
    when(jwtService.parse("bad.token")).thenThrow(new RuntimeException("bad"));

    filter.doFilter(req, res, chain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(chain).doFilter(req, res);
  }
}