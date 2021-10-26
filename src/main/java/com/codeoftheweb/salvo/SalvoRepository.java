package com.codeoftheweb.salvo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SalvoRepository extends JpaRepository<Salvo, Long> {
}
//El concepto de Repository como clase que se encarga de gestionar
// todas las operaciones de persistencia contra una tabla de la base de datos