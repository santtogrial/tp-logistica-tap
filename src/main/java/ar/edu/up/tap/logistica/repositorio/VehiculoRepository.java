package ar.edu.up.tap.logistica.repositorio;

import ar.edu.up.tap.logistica.dominio.EstadoVehiculo;
import ar.edu.up.tap.logistica.dominio.Vehiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    Optional<Vehiculo> findByChoferId(Long choferId);

    Page<Vehiculo> findByEstado(EstadoVehiculo estado, Pageable pageable);
}
