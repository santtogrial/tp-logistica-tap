package ar.edu.up.tap.logistica.dominio;

import ar.edu.up.tap.logistica.Fixtures;
import ar.edu.up.tap.logistica.excepcion.StockInsuficienteException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockDepositoTest {

    @Test
    void descontarRestaLaCantidadPedida() {
        StockDeposito stock = new StockDeposito(Fixtures.deposito(), Fixtures.producto(10L), 10);

        stock.descontar(4);

        assertThat(stock.getCantidad()).isEqualTo(6);
    }

    @Test
    void sePuedeDescontarTodoElStockDisponible() {
        StockDeposito stock = new StockDeposito(Fixtures.deposito(), Fixtures.producto(10L), 5);

        stock.descontar(5);

        assertThat(stock.getCantidad()).isZero();
    }

    @Test
    void siNoAlcanzaLanzaExcepcionYNoModificaElStock() {
        StockDeposito stock = new StockDeposito(Fixtures.deposito(), Fixtures.producto(10L), 3);

        assertThatThrownBy(() -> stock.descontar(4))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("hay 3");
        assertThat(stock.getCantidad()).isEqualTo(3);
    }

    @Test
    void reintegrarDevuelveLaCantidad() {
        StockDeposito stock = new StockDeposito(Fixtures.deposito(), Fixtures.producto(10L), 3);

        stock.reintegrar(2);

        assertThat(stock.getCantidad()).isEqualTo(5);
    }
}
