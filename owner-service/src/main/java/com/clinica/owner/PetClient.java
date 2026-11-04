package com.clinica.owner;

import com.clinica.owner.Errores.ServicioNoDisponible;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente HTTP hacia Pet-Service.
 * Owner-Service solo lo necesita para la regla "no eliminar propietarios con mascotas".
 */
@Component
public class PetClient {

    private final RestTemplate restTemplate;
    private final String baseUrl; // por ejemplo http://pet-service:8082

    public PetClient(RestTemplate restTemplate, @Value("${pet-service.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * Llama a GET /pets/owner/{id} y dice si la lista de mascotas del propietario trae algo.
     *
     * @throws ServicioNoDisponible si Pet-Service no responde (se rechaza el borrado por seguridad)
     */
    public boolean tieneMascotas(Long ownerId) {
        try {
            Object[] mascotas = restTemplate.getForObject(baseUrl + "/pets/owner/{id}", Object[].class, ownerId);
            return mascotas != null && mascotas.length > 0;
        } catch (RestClientException e) {
            throw new ServicioNoDisponible(
                    "Pet-Service no está disponible; no se puede verificar si el propietario tiene mascotas");
        }
    }
}
