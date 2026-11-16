package ar.edu.up.tap.logistica.servicio;

import ar.edu.up.tap.logistica.dominio.Coordenada;
import ar.edu.up.tap.logistica.dominio.Destinatario;
import ar.edu.up.tap.logistica.repositorio.DepositoRepository;
import ar.edu.up.tap.logistica.repositorio.DestinatarioRepository;
import ar.edu.up.tap.logistica.repositorio.ProductoRepository;
import ar.edu.up.tap.logistica.web.dto.CrearDestinatarioRequest;
import ar.edu.up.tap.logistica.web.dto.DepositoResponse;
import ar.edu.up.tap.logistica.web.dto.DestinatarioResponse;
import ar.edu.up.tap.logistica.web.dto.PaginaResponse;
import ar.edu.up.tap.logistica.web.dto.ProductoResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consultas de datos maestros (depósitos, productos, destinatarios). */
@Service
public class CatalogoService {

    private final DepositoRepository depositoRepository;
    private final ProductoRepository productoRepository;
    private final DestinatarioRepository destinatarioRepository;

    public CatalogoService(DepositoRepository depositoRepository, ProductoRepository productoRepository,
                           DestinatarioRepository destinatarioRepository) {
        this.depositoRepository = depositoRepository;
        this.productoRepository = productoRepository;
        this.destinatarioRepository = destinatarioRepository;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<DepositoResponse> listarDepositos(Pageable pageable) {
        return PaginaResponse.de(depositoRepository.findAll(pageable).map(DepositoResponse::desde));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<ProductoResponse> listarProductos(Pageable pageable) {
        return PaginaResponse.de(productoRepository.findAll(pageable).map(ProductoResponse::desde));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<DestinatarioResponse> listarDestinatarios(Pageable pageable) {
        return PaginaResponse.de(destinatarioRepository.findAll(pageable).map(DestinatarioResponse::desde));
    }

    @Transactional
    public DestinatarioResponse crearDestinatario(CrearDestinatarioRequest request) {
        Destinatario destinatario = new Destinatario(request.nombre(), request.telefono(), request.direccion(),
                new Coordenada(request.lat(), request.lng()));
        return DestinatarioResponse.desde(destinatarioRepository.save(destinatario));
    }
}
