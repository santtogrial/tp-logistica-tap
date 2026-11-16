package ar.edu.up.tap.logistica.repositorio;

import ar.edu.up.tap.logistica.dominio.Planificacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanificacionRepository extends JpaRepository<Planificacion, Long> {
}
