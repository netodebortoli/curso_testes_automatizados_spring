package com.example.testes.commom;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import com.example.testes.domain.Planet;

public class PlanetConstant {
   public static final Planet PLANET = new Planet("name", "terrain", "climate");
   public static final Planet INVALID_PLANET = new Planet("", "", "");

   public static final Planet TATOOINE = new Planet(1L, "Tatooine", "arid", "desert");
   public static final Planet ALDERAAN = new Planet(2L, "Alderaan", "temperate", "grasslands, mountains");
   public static final Planet YAVINIV = new Planet(3L, "Yavin IV", "temperate, tropical", "jungle, rainforests");
   public static final List<Planet> PLANETS = new ArrayList<>() {
      {
         add(TATOOINE);
         add(ALDERAAN);
         add(YAVINIV);
      }
   };

   public static final Stream<Arguments> invalidPlanets() {
      return Stream.of(
            Arguments.of(new Planet("", "terrain", "climate")),
            Arguments.of(new Planet("name", "", "climate")),
            Arguments.of(new Planet("name", "terrain", "")),
            Arguments.of(new Planet("", "", "")),
            Arguments.of(new Planet(null, "terrain", "climate")),
            Arguments.of(new Planet("name", null, "climate")),
            Arguments.of(new Planet("name", "terrain", null)),
            Arguments.of(new Planet(null, null, null)));
   }

}
