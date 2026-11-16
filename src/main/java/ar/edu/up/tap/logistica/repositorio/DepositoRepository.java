package ar.edu.up.tap.logistica.repositorio;

import ar.edu.up.tap.logistica.dominio.Deposito;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositoRepository extends JpaRepository<Deposito, Long> {
}
