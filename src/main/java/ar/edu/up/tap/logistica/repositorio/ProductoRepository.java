package ar.edu.up.tap.logistica.repositorio;

import ar.edu.up.tap.logistica.dominio.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
