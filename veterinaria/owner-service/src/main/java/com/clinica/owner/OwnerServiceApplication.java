package com.clinica.owner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Punto de entrada del microservicio de PROPIETARIOS (puerto 8081).
 *
 * También define el RestTemplate, que es la herramienta con la que este servicio
 * "llama" a otros servicios por HTTP (en este caso, a Pet-Service).
 */
@SpringBootApplication
public class OwnerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OwnerServiceApplication.class, args);
    }

    /**
     * RestTemplate con tiempos máximos de espera: si el otro servicio no responde,
     * fallamos rápido en lugar de quedarnos colgados. Los valores se configuran en application.yml.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder,
                                     @Value("${clients.connect-timeout-ms}") long conexionMs,
                                     @Value("${clients.read-timeout-ms}") long lecturaMs) {
        return builder
                .setConnectTimeout(Duration.ofMillis(conexionMs))
                .setReadTimeout(Duration.ofMillis(lecturaMs))
                .build();
    }
}
