package com.sithumya20220865.OOPCW;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OopcwApplication {

	public static void main(String[] args) {
		var context = SpringApplication.run(OopcwApplication.class, args);

		GlobalUtil.serverSetupJob();
	}

}
