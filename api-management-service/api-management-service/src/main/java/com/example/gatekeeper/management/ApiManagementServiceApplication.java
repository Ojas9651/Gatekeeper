package com.example.gatekeeper.management;

import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.annotation.sql.DataSourceDefinitions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class ApiManagementServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiManagementServiceApplication.class, args);
	}

}
