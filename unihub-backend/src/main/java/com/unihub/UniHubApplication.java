package com.unihub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;
import org.springframework.scheduling.annotation.EnableAsync;

@Modulithic
@SpringBootApplication
@EnableAsync
public class UniHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(UniHubApplication.class, args);
	}

}
