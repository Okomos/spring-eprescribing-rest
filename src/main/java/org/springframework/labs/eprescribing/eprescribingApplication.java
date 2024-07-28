package org.springframework.labs.eprescribing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class eprescribingApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(eprescribingApplication.class, args);
	}
}
