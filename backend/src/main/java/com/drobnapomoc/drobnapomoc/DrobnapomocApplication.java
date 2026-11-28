package com.drobnapomoc.drobnapomoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DrobnapomocApplication {

	public static void main(String[] args) {
		SpringApplication.run(DrobnapomocApplication.class, args);
	}

}
