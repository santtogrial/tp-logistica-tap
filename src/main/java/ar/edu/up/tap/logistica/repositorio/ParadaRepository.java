package ar.edu.up.tap.logistica.repositorio;

import ar.edu.up.tap.logistica.dominio.Parada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParadaRepository extends JpaRepository<Parada, Long> {

    Optional<Parada> findFirstByOrdenIdOrderByIdDesc(Long ordenId);
}
