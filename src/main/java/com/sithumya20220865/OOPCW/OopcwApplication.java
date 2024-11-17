package com.sithumya20220865.OOPCW;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OopcwApplication {

	public static void main(String[] args) {
		var context = SpringApplication.run(OopcwApplication.class, args);

		try {
			// Initialize the database through GlobalUtil
			GlobalUtil.initializeDatabase();
			System.out.println("Application is up and running!");
		} catch (RuntimeException e) {
			System.err.println("Application failed to start: " + e.getMessage());
			System.exit(1);
		}
	}

}
