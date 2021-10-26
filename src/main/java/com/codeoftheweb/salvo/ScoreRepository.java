package com.codeoftheweb.salvo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface ScoreRepository extends JpaRepository <Score, Long> {
}
//El concepto de Repository como clase que se encarga de gestionar
// todas las operaciones de persistencia contra una tabla de la base de datos