package ar.edu.up.tap.logistica.repositorio;

import ar.edu.up.tap.logistica.dominio.EstadoRuta;
import ar.edu.up.tap.logistica.dominio.Ruta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RutaRepository extends JpaRepository<Ruta, Long> {

    Optional<Ruta> findFirstByVehiculoIdAndEstadoInOrderByIdDesc(Long vehiculoId, Collection<EstadoRuta> estados);

    default Optional<Ruta> buscarRutaEnCurso(Long vehiculoId) {
        return findFirstByVehiculoIdAndEstadoInOrderByIdDesc(vehiculoId, List.of(EstadoRuta.EN_CURSO));
    }

    Page<Ruta> findByEstado(EstadoRuta estado, Pageable pageable);
}
