package com.coding_wielder.Job_Tracker;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class JobTrackerApplication {

	private static final Logger logger = LoggerFactory.getLogger(JobTrackerApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(JobTrackerApplication.class, args);
	}

	@Bean
	@Profile("dev")
	public CommandLineRunner commandLineRunner() {
		return args -> {
			logger.info("pgadmin4 web URL: http://localhost:5050/");
		};
	}

}
