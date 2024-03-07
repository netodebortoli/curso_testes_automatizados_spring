package com.example.testes.web;

import static com.example.testes.commom.PlanetConstant.PLANET;
import static com.example.testes.commom.PlanetConstant.PLANETS;
import static com.example.testes.commom.PlanetConstant.TATOOINE;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.testes.domain.Planet;
import com.example.testes.domain.PlanetService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(PlanetController.class)
public class PlanetControllerTest {

   @Autowired
   private MockMvc mockMvc;

   @Autowired
   private ObjectMapper objectMapper;

   @MockBean
   private PlanetService planetService;

   @Test
   public void createPlanet_WithValidData_RetunrsStatusCreated() throws Exception {

      when(planetService.create(PLANET)).thenReturn(PLANET);

      /*
       * 1) Uso do método stático 'post()', disponibilizado pela biblioteca do
       * MockMvcRequestBuilders. Além disso, essa bibioteca dispõe de todos os outros
       * métodos HTTP, para ser usados para test.
       * 2) O conteúdo da requisição 'content' deve ser mandado como um string, uma
       * vez que o servidor espera receber esse valor serializado.
       * Dessa forma, utilizo a classe ObjectMapper, para fazer a conversão do valor
       * do objeto para um string de formato JSON.
       * 3) Toda requisição pelo lado do cliente precisa informar o tipo do dado que
       * está sendo transportado na requisição.
       * Dito isso, é necessário informar que o tipo de mídia na requisão é JSON.
       * 4) Usa do método statico 'status()'. Assim, consigo aferir qual a resposta da
       * requisição
       * 5) Usa do método statico 'jsonPath()', no qual eu informo a raíz do json
       * ($), e vejo se ele é igual ao valor de Planet
       */
      mockMvc
            .perform(
                  post("/planets")
                        .content(objectMapper.writeValueAsString(PLANET))
                        .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$").value(PLANET));
   }

   @Test
   public void createPlanet_WithInvalidData_ReturnsException() throws Exception {
      Planet invalidPlanet = new Planet();
      Planet emptyPlanet = new Planet("", "", "");

      mockMvc
            .perform(
                  post("/planets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPlanet)))
            .andExpect(status().isUnprocessableEntity());

      mockMvc
            .perform(
                  post("/planets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyPlanet)))
            .andExpect(status().isUnprocessableEntity());
   }

   /*
    * A estratégia para a regra de negócio no contexto é lidar com o conflito de
    * nomes.
    * Ao tentar inserir no banco de dados um planet com nome repetido, é lançada a
    * exceção genérica RuntimeException.
    * No entanto, a exceção que herda de RuntimeException e que também foi lançada
    * é a DataIntegrityViolationException.
    * Dessa forma, para lidar com o tratamento de exceções, é necessário criar um
    * método handler da exceção em questão.
    * Assim, a API vai conseguir lidar com esse comportamento e lançará para o
    * cliente o devido status correspondente.
    */
   @Test
   public void createPlanet_WithExistingName_ReturnsConflict() throws Exception {
      when(planetService.create(any())).thenThrow(DataIntegrityViolationException.class);

      mockMvc
            .perform(
                  post("/planets")
                        .content(objectMapper.writeValueAsString(PLANET))
                        .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
   }

   @Test
   public void getPlanet_ByExistingId_ReturnsPlanet() throws JsonProcessingException, Exception {
      when(planetService.get(anyLong())).thenReturn(Optional.of(PLANET));

      mockMvc
            .perform(
                  get("/planets/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(PLANET));
   }

   @Test
   public void getPlanet_ByUnexistingId_ReturnsNotFound() throws Exception {
      mockMvc
            .perform(
                  get("/planets/{id}", 1L))
            .andExpect(status().isNotFound());
   }

   @Test
   public void getPlanet_ByExistingName_ReturnsPlanet() throws Exception {
      when(planetService.getByName(PLANET.getName())).thenReturn(Optional.of(PLANET));

      mockMvc
            .perform(
                  get("/planets/name/{name}", PLANET.getName()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(PLANET));
   }

   @Test
   public void getPlanet_ByUnexistingName_ReturnsNotFound() throws Exception {
      mockMvc
            .perform(
                  get("/planets/name/{name}", PLANET.getName()))
            .andExpect(status().isNotFound());
   }

   @Test
   public void listPlanets_ReturnsFilteredPlanets() throws Exception {
      // Cenário 1: listagem sem filtros
      when(planetService.list(null, null)).thenReturn(PLANETS);

      // Cenário 2: listagem com filtros
      when(planetService.list(TATOOINE.getClimate(), TATOOINE.getTerrain())).thenReturn(List.of(TATOOINE));

      mockMvc
            .perform(
                  get("/planets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)));
      // Uso do método estático hasSize, da biblioteca hamcrest. Esse método
      // implementa o Matcher, sendo o segundo parâmetro do jsonPath
      // Muito comum seu uso em testes para lidar com coleções

      mockMvc
            .perform(
                  get("/planets")
                        .param("climate", TATOOINE.getClimate())
                        .param("terrain", TATOOINE.getTerrain()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0]").value(TATOOINE));
      // Uso do método 'param', para definir quais são os parâmetros da requisição
      // Uma outra abordagem seria contacenar a URI string diretamente dentro do método.
      // Exemplo: "/planets? " +
      // String.format("terrain=%s&climate=%s",TATOOINE.getClimate(),TATOOINE.getTerrain());
      // Nota: como o caminho raíz do json ($) é um array, eu preciso acessar a
      // primeira posição deste para obter o item da lista
   }

   @Test
   public void listPlanets_ReturnsNoPlanets() throws Exception {
      when(planetService.list(anyString(), anyString())).thenReturn(List.of());

      mockMvc
            .perform(
                  get("/planets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
   }

   @Test
   public void removePlanet_WithExistingId_ReturnsNoContent() throws Exception {
      mockMvc
            .perform(
                  delete("/planets/{id}", 1L))
            .andExpect(status().isNoContent());
   }

   @Test
   public void removePlanet_WithUnexistingId_ReturnsNotFound() throws Exception {
      final Long id = 1L;
      doThrow(new EmptyResultDataAccessException(1)).when(planetService).remove(id);
      
      mockMvc
            .perform(
                  delete("/planets/{id}", id))
            .andExpect(status().isNotFound());
   }

}
