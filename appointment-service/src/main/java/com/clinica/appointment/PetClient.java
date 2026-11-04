package com.clinica.appointment;

import com.clinica.appointment.Dtos.PetDetalleDto;
import com.clinica.appointment.Dtos.PetDto;
import com.clinica.appointment.Errores.NoEncontrado;
import com.clinica.appointment.Errores.ServicioNoDisponible;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/** Cliente HTTP hacia Pet-Service: valida mascotas y obtiene el detalle mascota + propietario. */
@Component
public class PetClient {

    private final RestTemplate restTemplate;
    private final String baseUrl; // por ejemplo http://pet-service:8082

    public PetClient(RestTemplate restTemplate, @Value("${pet-service.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * GET /pets/{id}: comprueba que la mascota exista.
     *
     * @throws NoEncontrado         si la mascota no existe (404)
     * @throws ServicioNoDisponible si Pet-Service no responde
     */
    public PetDto obtenerMascota(Long id) {
        try {
            return restTemplate.getForObject(baseUrl + "/pets/{id}", PetDto.class, id);
        } catch (HttpClientErrorException.NotFound e) {
            throw new NoEncontrado("La mascota con id " + id + " no existe");
        } catch (RestClientException e) {
            throw new ServicioNoDisponible("Pet-Service no está disponible en este momento");
        }
    }

    /**
     * GET /pets/{id}/detalle: mascota + propietario (Pet-Service a su vez consulta a Owner-Service).
     *
     * @throws NoEncontrado         si no existe la mascota o su propietario
     * @throws ServicioNoDisponible si Pet-Service u Owner-Service no responden
     */
    public PetDetalleDto obtenerDetalle(Long id) {
        try {
            return restTemplate.getForObject(baseUrl + "/pets/{id}/detalle", PetDetalleDto.class, id);
        } catch (HttpClientErrorException.NotFound e) {
            throw new NoEncontrado("No se encontró la mascota con id " + id + " o su propietario");
        } catch (RestClientException e) {
            throw new ServicioNoDisponible("Pet-Service u Owner-Service no están disponibles en este momento");
        }
    }
}
