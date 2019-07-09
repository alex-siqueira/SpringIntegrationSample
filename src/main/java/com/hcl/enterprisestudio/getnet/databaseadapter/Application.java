package com.hcl.enterprisestudio.getnet.databaseadapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.config.EnableIntegration;

/**
 * Classe para bootstrap do projeto, habilitando as configurações de 
 * AutoConfiguration do Spring Boot e Spring Integration.
 * 
 * @author Alexandre.Siqueira
 *
 */
@SpringBootApplication
@EnableIntegration
public class Application {

	public static void main (String args[]){
		SpringApplication.run(Application.class, args);		
	}
	
}
