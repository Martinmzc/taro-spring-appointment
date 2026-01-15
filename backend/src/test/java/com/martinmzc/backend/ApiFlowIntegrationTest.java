package com.martinmzc.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiFlowIntegrationTest {

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @Test
  void login_shouldReturnToken_whenCodeIsCorrect() throws Exception {
    var body = """
      {"phone":"13800000000","code":"123456"}
      """;

    var res = mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.token").isNotEmpty())
      .andExpect(jsonPath("$.userId").isNumber())
      .andReturn();

    JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
    assertThat(json.get("token").asText()).isNotBlank();
    assertThat(json.get("userId").asLong()).isPositive();
  }

  @Test
  void login_shouldReturn400_whenCodeIsWrong() throws Exception {
    var body = """
      {"phone":"13800000000","code":"000000"}
      """;

    mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
      .andExpect(status().isBadRequest());
  }

  @Test
  void login_shouldReturn400_whenPhoneInvalid() throws Exception {
    var body = """
      {"phone":"abc","code":"123456"}
      """;

    mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
      .andExpect(status().isBadRequest());
  }

  @Test
  void protectedEndpoint_shouldReturn401_whenNoToken() throws Exception {
    mockMvc.perform(get("/api/appointments/me"))
      .andExpect(status().isUnauthorized());
  }

  @Test
  void protectedEndpoint_shouldReturn401_whenTokenInvalid() throws Exception {
    mockMvc.perform(get("/api/appointments/me")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + "not-a-jwt"))
      .andExpect(status().isUnauthorized());
  }

  @Test
  void createAppointment_shouldBeIdempotent_forSameUserAndSamePayload() throws Exception {
    String token = loginAndGetToken("13800000000");

    var payload = """
      {"serviceName":"meeting","date":"2026-01-13","timeSlot":"10:00-11:00"}
      """;

    long id1 = createAndGetAppointmentId(token, payload);
    long id2 = createAndGetAppointmentId(token, payload);

    assertThat(id2).isEqualTo(id1);

    JsonNode listJson = myList(token);
    assertThat(listJson.size()).isEqualTo(1);
    assertThat(listJson.get(0).get("id").asLong()).isEqualTo(id1);
  }

  @Test
  void createAppointment_shouldAllowDifferentUsers_samePayload() throws Exception {
    String tokenA = loginAndGetToken("13800000001");
    String tokenB = loginAndGetToken("13800000002");

    var payload = """
      {"serviceName":"meeting","date":"2026-01-13","timeSlot":"10:00-11:00"}
      """;

    long a1 = createAndGetAppointmentId(tokenA, payload);
    long b1 = createAndGetAppointmentId(tokenB, payload);

    assertThat(a1).isNotEqualTo(b1);

    assertThat(myList(tokenA).size()).isEqualTo(1);
    assertThat(myList(tokenB).size()).isEqualTo(1);
  }

  @Test
  void createAppointment_shouldReturn400_whenMissingField() throws Exception {
    String token = loginAndGetToken("13800000000");

    var payloadMissing = """
      {"serviceName":"","date":"2026-01-13","timeSlot":"10:00-11:00"}
      """;

    mockMvc.perform(post("/api/appointments")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(payloadMissing))
      .andExpect(status().isBadRequest());
  }

  private String loginAndGetToken(String phone) throws Exception {
    var body = """
      {"phone":"%s","code":"123456"}
      """.formatted(phone);

    var res = mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
      .andExpect(status().isOk())
      .andReturn();

    return objectMapper.readTree(res.getResponse().getContentAsString()).get("token").asText();
  }

  private long createAndGetAppointmentId(String token, String payload) throws Exception {
    var res = mockMvc.perform(post("/api/appointments")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").isNumber())
      .andReturn();

    return objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();
  }

  private JsonNode myList(String token) throws Exception {
    var res = mockMvc.perform(get("/api/appointments/me")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
      .andExpect(status().isOk())
      .andReturn();

    JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
    assertThat(json.isArray()).isTrue();
    return json;
  }
}