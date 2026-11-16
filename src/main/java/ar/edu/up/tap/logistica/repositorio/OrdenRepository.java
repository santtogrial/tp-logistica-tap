package ar.edu.up.tap.logistica.repositorio;

import ar.edu.up.tap.logistica.dominio.EstadoOrden;
import ar.edu.up.tap.logistica.dominio.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrdenRepository extends JpaRepository<Orden, Long>, JpaSpecificationExecutor<Orden> {

    Optional<Orden> findByCodigoSeguimiento(String codigoSeguimiento);

    boolean existsByCodigoSeguimiento(String codigoSeguimiento);

    @Query("""
            select o from Orden o
            where o.deposito.id = :depositoId
              and (o.estado = :creada or (o.estado = :noEntregada and o.intentosFallidos < :maxIntentos))
            order by o.creadaEn
            """)
    List<Orden> buscarPlanificables(@Param("depositoId") Long depositoId,
                                    @Param("creada") EstadoOrden creada,
                                    @Param("noEntregada") EstadoOrden noEntregada,
                                    @Param("maxIntentos") int maxIntentos);

    default List<Orden> buscarPlanificables(Long depositoId) {
        return buscarPlanificables(depositoId, EstadoOrden.CREADA, EstadoOrden.NO_ENTREGADA, Orden.MAX_INTENTOS);
    }
}
