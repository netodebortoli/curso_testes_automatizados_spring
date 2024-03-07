package com.example.testes.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "planets")
public class Planet {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @NotBlank
   @Column(nullable = false, unique = true)
   private String name;

   @NotBlank
   @Column(nullable = false)
   private String terrain;

   @NotBlank
   @Column(nullable = false)
   private String climate;

   public Planet() {
   }

   public Planet(String climate, String terrain) {
      this.climate = climate;
      this.terrain = terrain;
   }

   public Planet(String name, String climate, String terrain) {
      this.name = name;
      this.terrain = terrain;
      this.climate = climate;
   }

   public Planet(Long id, String name, String climate, String terrain) {
      this.id = id;
      this.name = name;
      this.terrain = terrain;
      this.climate = climate;
   }

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getTerrain() {
      return terrain;
   }

   public void setTerrain(String terrain) {
      this.terrain = terrain;
   }

   public String getClimate() {
      return climate;
   }

   public void setClimate(String climate) {
      this.climate = climate;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((terrain == null) ? 0 : terrain.hashCode());
      result = prime * result + ((climate == null) ? 0 : climate.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      Planet other = (Planet) obj;
      if (id == null) {
         if (other.id != null)
            return false;
      } else if (!id.equals(other.id))
         return false;
      if (name == null) {
         if (other.name != null)
            return false;
      } else if (!name.equals(other.name))
         return false;
      if (terrain == null) {
         if (other.terrain != null)
            return false;
      } else if (!terrain.equals(other.terrain))
         return false;
      if (climate == null) {
         if (other.climate != null)
            return false;
      } else if (!climate.equals(other.climate))
         return false;
      return true;
   }

   @Override
   public String toString() {
      return "Planet [id=" + id + ", name=" + name + ", terrain=" + terrain + ", climate=" + climate + "]";
   }

}
