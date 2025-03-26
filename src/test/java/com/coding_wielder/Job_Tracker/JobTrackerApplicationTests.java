package com.coding_wielder.Job_Tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
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

import com.coding_wielder.Job_Tracker.jobs.Job;
import com.coding_wielder.Job_Tracker.jobs.JobRepository;
import com.coding_wielder.Job_Tracker.lib.TestTokenLib;
import com.coding_wielder.Job_Tracker.security.JwtUtil;
import com.coding_wielder.Job_Tracker.users.User;
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
	private JdbcClient jdbcClient;
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
	private static final UUID jobId = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final Job testJob = new Job(jobId, "TestJobTitle", "TestCompany", "TestStatus", LocalDateTime.now(),
			userId);
	private static final UUID oldJobId = UUID.fromString("21111111-1111-1111-1111-111111111111");
	private static final UUID oldUserId = UUID.fromString("22111111-1111-1111-1111-111111111111");
	private static final User oldUser = new User(oldUserId, "OldUserName", "password");
	private static final Job oldJob = new Job(oldJobId, "OldJobTitle", "OldCompany", "OldStatus", LocalDateTime.now(),
			oldUserId);

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

		// Store test Job
		jdbcClient
				.sql("INSERT INTO jobs (id, job_title, company, status, applied_date, user_id) " +
						"VALUES (:id, :jobTitle, :company, :status, :appliedDate, :userId)")
				.param("id", jobId)
				.param("jobTitle", testJob.jobTitle())
				.param("company", testJob.company())
				.param("status", testJob.status())
				.param("appliedDate", testJob.appliedDate())
				.param("userId", testJob.userId())
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

	/* Job Controller Test */
	@Test
	void addNewJobTest() throws Exception {
		RequestJob requestJob = new RequestJob("Software Engineer", "TechCorp", "Applied");

		mvc.perform(post("/job")
				.header("Authorization", "Bearer " + token)
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(requestJob)))
				.andExpect(status().isOk());

		assertTrue(jobRepository.findAll().stream()
				.anyMatch(job -> job.jobTitle().equals("Software Engineer")
						&& job.company().equals("TechCorp")
						&& job.status().equals("Applied")),
				"Job was not Saved to Database");
	}

	@Test
	void getJobsTest() throws Exception {
		mvc.perform(get("/jobs")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());

		assertTrue(jobRepository.findAll().size() > 0);
	}

	@Test
	void getJobByIdTest() throws Exception {
		String responseJob = mvc.perform(get("/job/{jobId}", jobId)
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		Job job = objectMapper.readValue(responseJob, Job.class);

		assertEquals(jobId, job.id(), "Job ID does not match");
		assertEquals(testJob.jobTitle(), job.jobTitle(), "Job title does not match");
		assertEquals(testJob.company(), job.company(), "Job company does not match");
		assertEquals(testJob.status(), job.status(), "Job status does not match");
		assertEquals(testJob.userId(), job.userId(), "Job user ID does not match");
	}

	@Test
	void updateJobTest() throws Exception {
		saveUserWithClient(oldUser);

		RequestJob updatedRequestJob = new RequestJob("UpdatedJobTitle", "UpdatedCompany", "Updatedstatus");

		saveJobWithClient(oldJob);

		mvc.perform(put("/job/{jobId}", oldJobId)
				.header("Authorization", "Bearer " + jwtUtil.generateToken(oldUserId))
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(updatedRequestJob)))
				.andExpect(status().isOk());

		Job updatedJob = jobRepository.findById(oldJobId).orElseThrow(() -> new RuntimeException("Job not found"));

		assertEquals(updatedRequestJob.jobTitle(), updatedJob.jobTitle(), "Job title was not updated");
		assertEquals(updatedRequestJob.company(), updatedJob.company(), "Job company was not updated");
		assertEquals(updatedRequestJob.status(), updatedJob.status(), "Job status was not updated");

		userRepository.delete(oldUser);
	}

	@Test
	void deleteJobTest() throws Exception {
		saveUserWithClient(oldUser);

		saveJobWithClient(oldJob);

		mvc.perform(delete("/job/{jobId}", oldJobId)
				.header("Authorization", "Bearer " + jwtUtil.generateToken(oldUserId)))
				.andExpect(status().isOk());

		assertTrue(jobRepository.findById(oldJobId).isEmpty(), "Job Was not deleted");
		userRepository.delete(oldUser);
	}

	/* User Controller Test */
	@Test
	void getUser() throws Exception {
		String responseUser = mvc.perform(get("/user")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		ResponseUser user = objectMapper.readValue(responseUser, ResponseUser.class);

		assertEquals(userId, user.id(), "User ID does not match");
		assertEquals(userName, user.username(), "Username does not match");
	}

	@Test
	void deleteUserAccountTest() throws Exception {
		saveUserWithClient(oldUser);
		saveJobWithClient(oldJob);

		mvc.perform(delete("/user")
				.header("Authorization", "Bearer " + jwtUtil.generateToken(oldUserId)))
				.andExpect(status().isOk());

		assertTrue(jobRepository.findById(oldJobId).isEmpty(), "Job Was not deleted");
		assertTrue(userRepository.findById(oldUserId).isEmpty(), "User was not deleted");
		userRepository.delete(oldUser);
	}

	private void saveJobWithClient(Job job) {
		jdbcClient
				.sql("INSERT INTO jobs (id, job_title, company, status, applied_date, user_id) " +
						"VALUES (:id, :jobTitle, :company, :status, :appliedDate, :userId)")
				.param("id", job.id())
				.param("jobTitle", job.jobTitle())
				.param("company", job.company())
				.param("status", job.status())
				.param("appliedDate", job.appliedDate())
				.param("userId", job.userId())
				.update();
	}

	private void saveUserWithClient(User user) {
		jdbcClient
				.sql("INSERT INTO users (id, username, hashed_password) VALUES (:id, :username, :password)")
				.param("id", user.id())
				.param("username", user.username())
				.param("password", user.hashedPassword())
				.update();
	}
}

record TokenResponse(String token, String refresh_token) {
}

record ResponseUser(UUID id, String username) {
}

record RequestJob(
		String jobTitle,
		String company,
		String status) {
}