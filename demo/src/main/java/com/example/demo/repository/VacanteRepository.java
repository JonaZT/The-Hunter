package com.example.demo.repository;

import com.example.demo.model.Vacante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VacanteRepository extends JpaRepository<Vacante, Long> {

    // Spring genera el SQL automáticamente por el nombre del método
    boolean existsByExternalId(String externalId);
}