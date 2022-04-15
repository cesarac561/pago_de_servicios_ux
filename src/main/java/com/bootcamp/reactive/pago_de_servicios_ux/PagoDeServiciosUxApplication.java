package com.bootcamp.reactive.pago_de_servicios_ux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
@EnableCaching
public class PagoDeServiciosUxApplication {

	public static void main(String[] args) {
		SpringApplication.run(PagoDeServiciosUxApplication.class, args);
	}

}
