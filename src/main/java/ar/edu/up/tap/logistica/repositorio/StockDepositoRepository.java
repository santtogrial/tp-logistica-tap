package ar.edu.up.tap.logistica.repositorio;

import ar.edu.up.tap.logistica.dominio.StockDeposito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockDepositoRepository extends JpaRepository<StockDeposito, Long> {

    @EntityGraph(attributePaths = "producto")
    Optional<StockDeposito> findByDepositoIdAndProductoId(Long depositoId, Long productoId);

    @EntityGraph(attributePaths = "producto")
    Page<StockDeposito> findByDepositoId(Long depositoId, Pageable pageable);
}
