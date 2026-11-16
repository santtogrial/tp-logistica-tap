package ar.edu.up.tap.logistica.servicio;

import ar.edu.up.tap.logistica.dominio.EstadoVehiculo;
import ar.edu.up.tap.logistica.dominio.Vehiculo;
import ar.edu.up.tap.logistica.excepcion.RecursoNoEncontradoException;
import ar.edu.up.tap.logistica.repositorio.VehiculoRepository;
import ar.edu.up.tap.logistica.web.dto.PaginaResponse;
import ar.edu.up.tap.logistica.web.dto.VehiculoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehiculoService {

    private static final Logger log = LoggerFactory.getLogger(VehiculoService.class);

    private final VehiculoRepository vehiculoRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository) {
        this.vehiculoRepository = vehiculoRepository;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<VehiculoResponse> listar(EstadoVehiculo estado, Pageable pageable) {
        var pagina = estado == null ? vehiculoRepository.findAll(pageable) : vehiculoRepository.findByEstado(estado, pageable);
        return PaginaResponse.de(pagina.map(VehiculoResponse::desde));
    }

    @Transactional
    public VehiculoResponse cambiarEstado(Long id, EstadoVehiculo nuevo) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("Vehículo", id));
        EstadoVehiculo anterior = vehiculo.getEstado();
        vehiculo.cambiarEstado(nuevo);
        log.info("Vehículo {}: {} -> {}", vehiculo.getPatente(), anterior, nuevo);
        return VehiculoResponse.desde(vehiculo);
    }
}
