package ar.edu.up.tap.logistica.repositorio;

import ar.edu.up.tap.logistica.dominio.Destinatario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DestinatarioRepository extends JpaRepository<Destinatario, Long> {
}
