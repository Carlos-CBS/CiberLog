package com.cyberlog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.cyberlog.Models")
@EnableJpaRepositories("com.cyberlog.Repositories")
public class CiberLogApplication {

	public static void main(String[] args) {
		SpringApplication.run(CiberLogApplication.class, args);
	}

}