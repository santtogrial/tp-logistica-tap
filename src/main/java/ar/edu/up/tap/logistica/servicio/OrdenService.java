package ar.edu.up.tap.logistica.servicio;

import ar.edu.up.tap.logistica.dominio.Deposito;
import ar.edu.up.tap.logistica.dominio.Destinatario;
import ar.edu.up.tap.logistica.dominio.EstadoOrden;
import ar.edu.up.tap.logistica.dominio.Orden;
import ar.edu.up.tap.logistica.dominio.StockDeposito;
import ar.edu.up.tap.logistica.dominio.Usuario;
import ar.edu.up.tap.logistica.excepcion.RecursoNoEncontradoException;
import ar.edu.up.tap.logistica.repositorio.DepositoRepository;
import ar.edu.up.tap.logistica.repositorio.DestinatarioRepository;
import ar.edu.up.tap.logistica.repositorio.OrdenRepository;
import ar.edu.up.tap.logistica.seguridad.UsuarioActual;
import ar.edu.up.tap.logistica.web.dto.CrearOrdenRequest;
import ar.edu.up.tap.logistica.web.dto.ItemRequest;
import ar.edu.up.tap.logistica.web.dto.OrdenDetalleResponse;
import ar.edu.up.tap.logistica.web.dto.OrdenResumenResponse;
import ar.edu.up.tap.logistica.web.dto.PaginaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrdenService {

    private static final Logger log = LoggerFactory.getLogger(OrdenService.class);

    // Sin 0/O ni 1/I para que el código no se confunda al dictarlo por teléfono
    private static final String CARACTERES_CODIGO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LARGO_CODIGO = 8;

    private final OrdenRepository ordenRepository;
    private final DepositoRepository depositoRepository;
    private final DestinatarioRepository destinatarioRepository;
    private final StockService stockService;
    private final UsuarioActual usuarioActual;
    private final SecureRandom random = new SecureRandom();

    public OrdenService(OrdenRepository ordenRepository, DepositoRepository depositoRepository,
                        DestinatarioRepository destinatarioRepository, StockService stockService,
                        UsuarioActual usuarioActual) {
        this.ordenRepository = ordenRepository;
        this.depositoRepository = depositoRepository;
        this.destinatarioRepository = destinatarioRepository;
        this.stockService = stockService;
        this.usuarioActual = usuarioActual;
    }

    @Transactional
    public OrdenDetalleResponse crear(CrearOrdenRequest request) {
        Deposito deposito = depositoRepository.findById(request.depositoId())
                .orElseThrow(() -> RecursoNoEncontradoException.de("Depósito", request.depositoId()));
        Destinatario destinatario = destinatarioRepository.findById(request.destinatarioId())
                .orElseThrow(() -> RecursoNoEncontradoException.de("Destinatario", request.destinatarioId()));
        Usuario usuario = usuarioActual.obtener();

        Orden orden = new Orden(generarCodigo(), deposito, destinatario, usuario);
        agruparPorProducto(request.items()).forEach((productoId, cantidad) -> {
            StockDeposito stock = stockService.descontar(deposito.getId(), productoId, cantidad);
            orden.agregarItem(stock.getProducto(), cantidad);
        });

        ordenRepository.save(orden);
        log.info("Orden {} creada en {} por {}", orden.getCodigoSeguimiento(), deposito.getNombre(), usuario.getEmail());
        return OrdenDetalleResponse.desde(orden);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<OrdenResumenResponse> listar(EstadoOrden estado, Long depositoId, Pageable pageable) {
        Specification<Orden> filtro = (root, query, cb) -> cb.conjunction();
        if (estado != null) {
            filtro = filtro.and((root, query, cb) -> cb.equal(root.get("estado"), estado));
        }
        if (depositoId != null) {
            filtro = filtro.and((root, query, cb) -> cb.equal(root.get("deposito").get("id"), depositoId));
        }
        return PaginaResponse.de(ordenRepository.findAll(filtro, pageable).map(OrdenResumenResponse::desde));
    }

    @Transactional(readOnly = true)
    public OrdenDetalleResponse obtener(Long id) {
        return OrdenDetalleResponse.desde(buscar(id));
    }

    @Transactional
    public OrdenDetalleResponse cancelar(Long id) {
        Orden orden = buscar(id);
        orden.cancelar(usuarioActual.obtener());
        stockService.reintegrar(orden);
        log.info("Orden {} cancelada, stock reintegrado", orden.getCodigoSeguimiento());
        return OrdenDetalleResponse.desde(orden);
    }

    private Orden buscar(Long id) {
        return ordenRepository.findById(id).orElseThrow(() -> RecursoNoEncontradoException.de("Orden", id));
    }

    // Si el mismo producto viene en dos líneas se suman las cantidades
    private Map<Long, Integer> agruparPorProducto(List<ItemRequest> items) {
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (ItemRequest item : items) {
            cantidades.merge(item.productoId(), item.cantidad(), Integer::sum);
        }
        return cantidades;
    }

    private String generarCodigo() {
        String codigo;
        do {
            StringBuilder sb = new StringBuilder("LG-");
            for (int i = 0; i < LARGO_CODIGO; i++) {
                sb.append(CARACTERES_CODIGO.charAt(random.nextInt(CARACTERES_CODIGO.length())));
            }
            codigo = sb.toString();
        } while (ordenRepository.existsByCodigoSeguimiento(codigo));
        return codigo;
    }
}
