package com.example.testes.domain;

import static com.example.testes.commom.PlanetConstant.PLANET;
import static com.example.testes.commom.PlanetConstant.TATOOINE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Example;
import org.springframework.test.context.jdbc.Sql;

//@SpringBootTest(classes = PlanetRepository.class)
@DataJpaTest
public class PlanetRepositoryTest {

   @Autowired
   private PlanetRepository planetRepository;

   @Autowired
   private TestEntityManager testEntityManager;

   /*
    * Após cada testes ser executado, o seu ID será setado como NULO, para não
    * afetar
    * os testes dos outros métodos no qual o objeto PLANET possa receber um id
    * mediante a operação 'persitAndFlush'.
    * Assim, eu garanto que a cada teste executado, a constante PLANET terá sempre
    * um "id novo"
    */
   @AfterEach
   public void afterEach() {
      PLANET.setId(null);
   }

   @Test
   public void createPlanet_WithValidData_ReturnsPlanet() {
      // Arrange
      Planet planet = planetRepository.save(PLANET);

      // Act
      Planet sut = testEntityManager.find(Planet.class, planet.getId());

      // Assert
      assertThat(sut).isNotNull();
      assertThat(sut).isEqualTo(planet);
   }

   /*
    * No cenário de erro, esse teste testa as validações da API ao salvar um
    * planeta com dados inválidos e vazio.
    * Caso a API nao possui as validações necessárias na entidade planet, essa
    * exceção nunca será lançada pelo banco de dados.
    * Com isso, os testes auxiliam a identificar possíveis problemas de
    * desenvolvimento.
    */
   @Test
   public void createPlanet_WithInvalidData_ReturnsThrowException() {
      Planet invalidPlanet = new Planet();
      Planet emptyPlanet = new Planet("", "", "");

      assertThatThrownBy(() -> planetRepository.save(invalidPlanet)).isInstanceOf(RuntimeException.class);
      assertThatThrownBy(() -> planetRepository.save(emptyPlanet)).isInstanceOf(RuntimeException.class);
   }

   @ParameterizedTest
   @MethodSource("com.example.testes.commom.PlanetConstant#invalidPlanets") // Esse é o caminho onde está o meu stream de dados inválidos
   void createPlanet_WithInvalidData_ReturnsThrowException(Planet invalidPlanet) {
      assertThatThrownBy(() -> planetRepository.save(invalidPlanet)).isInstanceOf(RuntimeException.class);
   }

   /*
    * Esse test testa a API se ela permite ter planetas com nome duplicados.
    * Para realizar tal teste, a preparação dos dados precisa ser feito diretamente
    * com o gerenciador do banco de dados (EM).
    * Para adicionar unicidade ao nome, basta adicionar a clásula unique = true, na
    * anotação @Column do atributo name, na entidade Planeta.
    */
   @Test
   public void createPlanet_WithExistingName_ReturnsThrowException() {
      /*
       * persistFlushFind salva o objeto no banco, atualiza o estado do banco e por
       * fim, garante que o objeto foi salvo retornando o mesmo.
       */
      Planet planet = testEntityManager.persistFlushFind(PLANET);

      /*
       * O método detach remove o objeto 'planet' do contexto(sessão) do entity
       * manager. Dessa forma é possível testar o planet como se fosse um objeto novo
       */
      testEntityManager.detach(planet);

      /*
       * Como os objetos salvos no entitManager ainda ficam sob gereciamento do mesmo,
       * e objeto 'planet' ainda terá o atributo id, é necessário setar seu ID para
       * nulo removê-lo do contexto do EM (feito anteriormente).
       * Portanto, ao salvar o objeto pelo repository, e este não ter o ID, o método
       * será o de criação. Assim, posso testar a funcionalidade de unicidade
       */
      planet.setId(null);

      assertThatThrownBy(() -> planetRepository.save(planet)).isInstanceOf(RuntimeException.class);
   }

   @Test
   public void getPlanet_ByExistingId_ReturnsPlanet() {
      Planet planet = testEntityManager.persistFlushFind(PLANET);

      Planet sut = planetRepository.findById(planet.getId()).get();

      assertThat(sut).isNotNull();
      assertThat(sut).isEqualTo(planet);
   }

   @Test
   public void getPlanet_ByUnexistingId_ReturnsEmpty() {
      Optional<Planet> sut = planetRepository.findById(1L);
      assertThat(sut).isEmpty();
   }

   @Test
   public void getPlanet_ByExistingName_ReturnsPlanet() {
      Planet planet = testEntityManager.persistFlushFind(PLANET);

      Optional<Planet> sut = planetRepository.findByName(planet.getName());

      assertThat(sut).isNotEmpty();
      assertThat(sut.get()).isEqualTo(planet);
   }

   @Test
   public void getPlanet_ByUnexistingName_ReturnsNotFound() {
      Optional<Planet> sut = planetRepository.findByName(PLANET.getName());
      assertThat(sut).isEmpty();
   }

   /*
    * Uso da anotação @Sql, para executar algum script de sql durante os testes
    * Assim, eu consigo realizar alguns testes de 'busca por todos e com filtros'.
    * O script precisa estar no classhPath do projeto, assim, o spring consegue
    * encontrar o arquivo.
    */
   @Sql(scripts = "/import_planets.sql")
   @Test
   public void listPlanets_ReturnsFilteredPlanets() {
      Example<Planet> queryWithoutFilters = QueryBuilder.makeQuery(new Planet());
      Example<Planet> queryWithFilters = QueryBuilder
            .makeQuery(new Planet(TATOOINE.getClimate(), TATOOINE.getTerrain()));

      List<Planet> planets = planetRepository.findAll(queryWithoutFilters);
      List<Planet> filteredPlanets = planetRepository.findAll(queryWithFilters);

      assertThat(planets).hasSize(3);
      assertThat(planets).isNotEmpty();
      assertThat(filteredPlanets).isNotEmpty();
      assertThat(filteredPlanets).hasSize(1);
      assertThat(filteredPlanets.get(0)).isEqualTo(TATOOINE);
   }

   @Test
   public void listPlanets_ReturnsNoPlanets() {
      Example<Planet> queryWithoutFilters = QueryBuilder.makeQuery(new Planet());

      List<Planet> planets = planetRepository.findAll(queryWithoutFilters);

      assertThat(planets).isEmpty();
   }

   @Test
   public void removePlanet_WithExistingId_RemovesPlanetFromDataBase() throws Exception {
      Planet planet = testEntityManager.persistFlushFind(PLANET);
      planetRepository.deleteById(planet.getId());
      Planet removedPlanet = testEntityManager.find(Planet.class, planet.getId());
      assertThat(removedPlanet).isNull();
   }

}