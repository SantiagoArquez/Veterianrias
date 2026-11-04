package com.clinica.pet;

import com.clinica.pet.Dtos.OwnerDto;
import com.clinica.pet.Errores.NoEncontrado;
import com.clinica.pet.Errores.ServicioNoDisponible;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/** Cliente HTTP hacia Owner-Service: aquí se consulta si un propietario existe. */
@Component
public class OwnerClient {

    private final RestTemplate restTemplate;
    private final String baseUrl; // por ejemplo http://owner-service:8081

    public OwnerClient(RestTemplate restTemplate, @Value("${owner-service.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * Llama a GET /owners/{id}.
     *
     * @throws NoEncontrado         si Owner-Service responde 404 (el propietario no existe)
     * @throws ServicioNoDisponible si Owner-Service no responde (caído, timeout o error 5xx)
     */
    public OwnerDto obtenerPropietario(Long id) {
        try {
            return restTemplate.getForObject(baseUrl + "/owners/{id}", OwnerDto.class, id);
        } catch (HttpClientErrorException.NotFound e) {
            throw new NoEncontrado("Propietario con id " + id + " no encontrado");
        } catch (RestClientException e) {
            throw new ServicioNoDisponible("Owner-Service no está disponible en este momento");
        }
    }
}
