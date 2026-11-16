package ar.edu.up.tap.logistica.servicio;

import ar.edu.up.tap.logistica.dominio.ItemOrden;
import ar.edu.up.tap.logistica.dominio.Orden;
import ar.edu.up.tap.logistica.dominio.StockDeposito;
import ar.edu.up.tap.logistica.excepcion.RecursoNoEncontradoException;
import ar.edu.up.tap.logistica.repositorio.DepositoRepository;
import ar.edu.up.tap.logistica.repositorio.StockDepositoRepository;
import ar.edu.up.tap.logistica.web.dto.PaginaResponse;
import ar.edu.up.tap.logistica.web.dto.StockResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final StockDepositoRepository stockRepository;
    private final DepositoRepository depositoRepository;

    public StockService(StockDepositoRepository stockRepository, DepositoRepository depositoRepository) {
        this.stockRepository = stockRepository;
        this.depositoRepository = depositoRepository;
    }

    @Transactional
    public StockDeposito descontar(Long depositoId, Long productoId, int cantidad) {
        StockDeposito stock = buscar(depositoId, productoId);
        stock.descontar(cantidad);
        return stock;
    }

    @Transactional
    public void reintegrar(Orden orden) {
        for (ItemOrden item : orden.getItems()) {
            buscar(orden.getDeposito().getId(), item.getProducto().getId()).reintegrar(item.getCantidad());
        }
    }

    @Transactional(readOnly = true)
    public PaginaResponse<StockResponse> listar(Long depositoId, Pageable pageable) {
        if (!depositoRepository.existsById(depositoId)) {
            throw RecursoNoEncontradoException.de("Depósito", depositoId);
        }
        return PaginaResponse.de(stockRepository.findByDepositoId(depositoId, pageable).map(StockResponse::desde));
    }

    private StockDeposito buscar(Long depositoId, Long productoId) {
        return stockRepository.findByDepositoIdAndProductoId(depositoId, productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El producto " + productoId + " no tiene stock cargado en el depósito " + depositoId));
    }
}
