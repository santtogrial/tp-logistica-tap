package ar.edu.up.tap.logistica.servicio;

import ar.edu.up.tap.logistica.Fixtures;
import ar.edu.up.tap.logistica.dominio.Orden;
import ar.edu.up.tap.logistica.dominio.StockDeposito;
import ar.edu.up.tap.logistica.excepcion.RecursoNoEncontradoException;
import ar.edu.up.tap.logistica.repositorio.DepositoRepository;
import ar.edu.up.tap.logistica.repositorio.StockDepositoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockDepositoRepository stockRepository;
    @Mock
    private DepositoRepository depositoRepository;

    @InjectMocks
    private StockService stockService;

    @Test
    void descontarActualizaElStockDelDeposito() {
        StockDeposito stock = new StockDeposito(Fixtures.deposito(), Fixtures.producto(10L), 8);
        when(stockRepository.findByDepositoIdAndProductoId(1L, 10L)).thenReturn(Optional.of(stock));

        stockService.descontar(1L, 10L, 3);

        assertThat(stock.getCantidad()).isEqualTo(5);
    }

    @Test
    void unProductoSinStockCargadoEnElDepositoResponde404() {
        when(stockRepository.findByDepositoIdAndProductoId(1L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockService.descontar(1L, 99L, 1))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void reintegrarDevuelveLaCantidadDeCadaItemDeLaOrden() {
        Orden orden = Fixtures.orden(1L, "LG-AAAA1111"); // tiene 2 unidades del producto 10
        StockDeposito stock = new StockDeposito(Fixtures.deposito(), Fixtures.producto(10L), 5);
        when(stockRepository.findByDepositoIdAndProductoId(1L, 10L)).thenReturn(Optional.of(stock));

        stockService.reintegrar(orden);

        assertThat(stock.getCantidad()).isEqualTo(7);
    }

    @Test
    void listarElStockDeUnDepositoInexistenteResponde404() {
        when(depositoRepository.existsById(9L)).thenReturn(false);

        assertThatThrownBy(() -> stockService.listar(9L, Pageable.ofSize(10)))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
