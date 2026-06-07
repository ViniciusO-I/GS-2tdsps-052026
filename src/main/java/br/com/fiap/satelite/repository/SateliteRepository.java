package br.com.fiap.satelite.repository;

import br.com.fiap.satelite.domain.Satelite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SateliteRepository extends JpaRepository<Satelite, Long> {

    Optional<Satelite> findByCodigoSatelite(String codigoSatelite);

    boolean existsByCodigoSatelite(String codigoSatelite);
}
