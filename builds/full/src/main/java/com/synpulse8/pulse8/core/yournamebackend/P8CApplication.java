package com.synpulse8.pulse8.core.yournamebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class P8CApplication {

	public static void main(String[] args) {
		SpringApplication.run(P8CApplication.class, args);
	}

}
