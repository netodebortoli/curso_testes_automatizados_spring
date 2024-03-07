package com.example.testes.domain;

import static com.example.testes.commom.PlanetConstant.INVALID_PLANET;
import static com.example.testes.commom.PlanetConstant.PLANET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;

@ExtendWith(MockitoExtension.class)
public class PlanetServiceTest {

    // @Autowired
    @InjectMocks
    private PlanetService planetService;

    // @MockBean
    @Mock
    private PlanetRepository planetRepository;

    @Test
    public void createPlanet_WithValidData_ReturnsPlanet() {
        /// AAA
        // Arrange
        when(planetRepository.save(PLANET)).thenReturn(PLANET);

        // Act
        // system under test
        Planet sut = planetService.create(PLANET);

        // Assert
        assertThat(sut).isEqualTo(PLANET);
    }

    /**
     * Neste cenário, eu estou dizendo que o meu service não tratará a exceção
     * lançada pelo banco de dados por conta própria,
     * mas sim fará o thrown da exceção para o controlador.
     * Logo, isso define também como é o design de minha aplicação.
     * Além disso, essa função pode fazer o tratamento tanto para dados inválidos de
     * um planeta, quanto para a regra de negócio "Planetas com nomes únicos".
     * Caso eu defina uma constraint no banco de dados,
     * para a coluna name, que faça os nomes de planetas serem UNIQUE,
     * então o banco de dados lançará a exceção do tipo Runtime e como este test já
     * está esperando uma exceção do tipo
     * Runtime, não seria necessário criar um novo método de teste, tendo em vista
     * que este método já estaria fazendo as duas verifições em um só.
     * Além disso, essa abordagem traz mais perfomance para a aplicação, em que não
     * é necessário buscar no banco de dados através de um nome, para
     * verificar se um determinado planeta com um nome já existe. Ao tentar criar, a
     * exceção já seria lançada pelo banco de dados.
     **/
    @Test
    public void createPlanet_WithInvalidData_ThrowsException() {
        // Normalmente a nivel de banco de dados, qnd é lançada uma exceção, ela é do
        // time Runtime
        when(planetRepository.save(INVALID_PLANET)).thenThrow(RuntimeException.class);

        // Verifico se ao criar um planeto inválido, é lançado uma exceção e que seja do
        // tipo Runtime
        assertThatThrownBy(() -> planetService.create(INVALID_PLANET)).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void getPlanet_ByExistingId_ReturnsPlanet() {
        /// AAA - Arrange, Act, Assert
        when(planetRepository.findById(anyLong())).thenReturn(Optional.of(PLANET));

        Optional<Planet> sut = planetService.get(1L);

        assertThat(sut.get()).isEqualTo(PLANET);
        assertNotNull(sut);
        assertThat(sut).isNotEmpty();
    }

    @Test
    public void getPlanet_ByUnexistingId_ReturnsEmpty() {
        /// AAA - Arrange, Act, Assert
        when(planetRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<Planet> sut = planetService.get(1L);

        assertThat(sut).isEmpty();
    }

    @Test
    public void getPlanet_ByExistingName_ReturnsPlanet() {
        when(planetRepository.findByName(PLANET.getName())).thenReturn(Optional.of(PLANET));

        Optional<Planet> sut = planetService.getByName(PLANET.getName());

        assertThat(sut).isNotEmpty();
        assertThat(sut.get()).isEqualTo(PLANET);
        assertThat(sut.get().getName()).isEqualTo(PLANET.getName());
    }

    @Test
    public void getPlanet_ByUnexistingName_ReturnsEmpty() {
        final String planetName = "Unexisting planet";
        when(planetRepository.findByName(anyString())).thenReturn(Optional.empty());

        Optional<Planet> sut = planetService.getByName(planetName);

        assertThat(sut).isEmpty();
    }

    @Test
    public void listPlanets_ReturnsAllPlanets() {
        Example<Planet> query = QueryBuilder.makeQuery(new Planet(PLANET.getClimate(), PLANET.getTerrain()));

        when(planetRepository.findAll(query)).thenReturn(List.of(PLANET));

        List<Planet> sut = planetService.list(PLANET.getClimate(), PLANET.getTerrain());

        assertThat(sut).isNotEmpty();
        assertThat(sut).hasSize(1);
        assertThat(sut.get(0)).isEqualTo(PLANET);
    }

    @Test
    public void listPlanets_ReturnsNoPlanets() {
        when(planetRepository.findAll(any())).thenReturn(List.of());

        List<Planet> sut = planetService.list(PLANET.getClimate(), PLANET.getTerrain());

        assertThat(sut).isEmpty();
    }

    /*
     * Neste cenário, pelo método delete no service se tratar de um método void, não é possível usar o método Mockito.when().
     * Isso porque o When espera algo a ser retornado, logo, métodos voids não são compatíveis com ele. 
     * Dessa forma, não é necessário fazer o Stub, onde surge a necessidade de amarrar comportamentos.
     * Com isso, eu faço uma aferição direta com o método 'assertThatCode', em que o código 'remove' do
     * planetService não lançará nenhuma exceção, passando algum id qualquer.
     */
    @Test
    public void removePlanet_WithExistingId_doesNotThrowAnyException() {
        assertThatCode(() -> planetService.remove(1L)).doesNotThrowAnyException();
    }

    /*
     * Uso do método doThrow, para lançar uma exceção quando planetRepository ter o método deleteById invocado e não conseguir deletar pelo ID passado.
     * Isso é um stub, onde eu estou amarrando o comportamento do planeyRepository
     */
    @Test
    public void removePlanet_WithUnexistingId_ThrowsException() {
        doThrow(new RuntimeException()).when(planetRepository).deleteById(99L);
        assertThatThrownBy(() -> planetService.remove(99L)).isInstanceOf(RuntimeException.class);
    }

}
