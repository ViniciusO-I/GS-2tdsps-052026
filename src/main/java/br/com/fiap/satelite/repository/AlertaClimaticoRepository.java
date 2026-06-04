package br.com.fiap.satelite.repository;

import br.com.fiap.satelite.domain.AlertaClimatico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertaClimaticoRepository extends JpaRepository<AlertaClimatico, Long> {
}