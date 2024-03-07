package com.example.testes;

import com.example.testes.domain.Planet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.util.List;

import static com.example.testes.commom.PlanetConstant.*;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("it")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"/import_planets.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"/remove_planets_after_test.sql"}, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class PlanetIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createPlanets_ReturnsCreated() {

        /*
         * Faço a requisição pelo verbo POST.
         * Pelos parâmetros, eu informo a URI do recurso (controlador),
         * o objeto da request e por fim, o tipo da resposta esperado.
         */
        ResponseEntity<Planet> sut = restTemplate.postForEntity(
                "/planets",
                PLANET,
                Planet.class);

        assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(sut.getBody()).isNotNull();
        assertThat(sut.getBody().getName()).isEqualTo(PLANET.getName());
        assertThat(sut.getBody().getClimate()).isEqualTo(PLANET.getClimate());
        assertThat(sut.getBody().getTerrain()).isEqualTo(PLANET.getTerrain());
    }

    @Test
    void getPlanet_ReturnsPlanet() {

        ResponseEntity<Planet> sut = restTemplate.getForEntity(
                "/planets/{id}",
                Planet.class,
                1L);

        assertThat(sut.getBody()).isNotNull();
        assertThat(sut.getBody()).isEqualTo(TATOOINE);
        assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getPlanetByName_ReturnsPlanet() {
        ResponseEntity<Planet> sut = restTemplate.getForEntity(
                "/planets/name/{name}",
                Planet.class,
                TATOOINE.getName());

        assertThat(sut.getBody()).isNotNull();
        assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sut.getBody()).isEqualTo(TATOOINE);
    }

    @Test
    void listPlanets_ReturnsAllPlanets() {
        ResponseEntity<Planet[]> sut = restTemplate.getForEntity(
                "/planets",
                Planet[].class);

        List<Planet> planets = List.of(sut.getBody());

        assertThat(sut.getBody()).isNotNull();
        assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(planets).isEqualTo(PLANETS);
        assertThat(sut.getBody()).hasSize(3);
    }

    @Test
    void listPlanets_ByClimate_ReturnsPlanets() {
        ResponseEntity<Planet[]> sut = restTemplate.getForEntity(
                "/planets?climate={climate}",
                Planet[].class,
                ALDERAAN.getClimate());

        assertThat(sut.getBody()).isNotNull();
        assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sut.getBody()[0].getClimate()).isEqualTo(ALDERAAN.getClimate());
        assertThat(sut.getBody()).hasSize(1);
    }

    @Test
    void listPlanets_ByTerrain_ReturnsPlanets() {
        ResponseEntity<Planet[]> sut = restTemplate.getForEntity(
                "/planets?terrain={terrain}",
                Planet[].class,
                TATOOINE.getTerrain());

        assertThat(sut.getBody()).isNotNull();
        assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sut.getBody()[0].getTerrain()).isEqualTo(TATOOINE.getTerrain());
        assertThat(sut.getBody()).hasSize(1);
    }

    @Test
    void removePlanet_ReturnsNoContent() {
        ResponseEntity<Void> sut = restTemplate.exchange(
                "/planets/{id}",
                HttpMethod.DELETE,
                null,
                Void.class,
                TATOOINE.getId());

        assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

}
