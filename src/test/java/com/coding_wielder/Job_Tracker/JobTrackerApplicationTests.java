package com.coding_wielder.Job_Tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.coding_wielder.Job_Tracker.jobs.JobRepository;
import com.coding_wielder.Job_Tracker.lib.TestTokenLib;
import com.coding_wielder.Job_Tracker.security.JwtUtil;
import com.coding_wielder.Job_Tracker.users.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class JobTrackerApplicationTests {

	@Container
	@SuppressWarnings("resource") // container closes automatically but I still get a warning
	public static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
			.withDatabaseName("testdb")
			.withUsername("testuser")
			.withPassword("testpass");

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
		registry.add("spring.datasource.username", postgresContainer::getUsername);
		registry.add("spring.datasource.password", postgresContainer::getPassword);
	}

	@Autowired
	private JobRepository jobRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	JdbcClient jdbcClient;
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	TestTokenLib testTokenLib;

	@Autowired
	private MockMvc mvc;

	private static String token;
	private static String refresh_token;
	private static final UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111112");
	private static final String userName = "TestUserName";
	private static final String password = "TestPassWord";

	@BeforeAll
	static void setUp(@Autowired JwtUtil jwtUtil, @Autowired JdbcClient jdbcClient,
			@Autowired PasswordEncoder passwordEncoder) {
		token = jwtUtil.generateToken(userId);
		refresh_token = jwtUtil.generateRefreshToken(userId);

		// store test user
		jdbcClient
				.sql("INSERT INTO users (id, username, hashed_password) VALUES (:id, :username, :password)")
				.param("id", userId)
				.param("username", userName)
				.param("password", passwordEncoder.encode(password))
				.update();
	}

	/* Auth route test */
	@Test
	void signupTest() throws Exception {
		String responseString = mvc.perform(post("/auth/signup")
				.servletPath("/auth/signup") // needed for filter to work
				.contentType("application/json")
				.content("{\"username\": \"testuser\", \"password\": \"testpass\"}"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		testTokenObjectString(responseString);
	}

	@Test
	void loginTest() throws Exception {
		String responseString = mvc.perform(post("/auth/login")
				.servletPath("/auth/login")
				.contentType("application/json")
				.content("{\"username\": \"" + userName + "\", \"password\": \"" + password + "\"}"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		testTokenObjectString(responseString);
	}

	@Test
	void refreshTest() throws Exception {
		String responseToken = mvc.perform(get("/token/refresh")
				.header("Authorization", "Bearer " + refresh_token))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		testTokenLib.testToken(responseToken, "token", userId);
	}

	void testTokenObjectString(String tokenObjString) throws Exception {
		TokenResponse tokenResponse = objectMapper.readValue(tokenObjString,
				TokenResponse.class);
		String token = tokenResponse.token();
		String refresh_token = tokenResponse.refresh_token();

		Claims tokenClaims = jwtUtil.validateTokenAndReturnClaims(token);
		Claims refreshTokenClaims = jwtUtil.validateTokenAndReturnClaims(refresh_token);

		assertEquals(tokenClaims.getSubject(), refreshTokenClaims.getSubject(),
				"token sub id's do not match");
		assertTrue(tokenClaims.getIssuedAt().before(new java.util.Date()));
		assertTrue(refreshTokenClaims.getIssuedAt().before(new java.util.Date()),
				"Should not be issued in future");
		assertTrue(tokenClaims.getExpiration().after(new java.util.Date()), "Token should not be expired");
		assertTrue(refreshTokenClaims.getExpiration().after(new java.util.Date()),
				"Refresh token should not be expired");
		assertEquals("token", tokenClaims.get("token_use"), "Token use should be'token'");
		assertEquals("refresh_token", refreshTokenClaims.get("token_use"),
				"Refresh token use should be 'refresh_token'");
	}
}

record TokenResponse(String token, String refresh_token) {
}