package ar.edu.up.tap.logistica.servicio;

import ar.edu.up.tap.logistica.Fixtures;
import ar.edu.up.tap.logistica.dominio.*;
import ar.edu.up.tap.logistica.excepcion.RecursoNoEncontradoException;
import ar.edu.up.tap.logistica.excepcion.StockInsuficienteException;
import ar.edu.up.tap.logistica.excepcion.TransicionInvalidaException;
import ar.edu.up.tap.logistica.repositorio.DepositoRepository;
import ar.edu.up.tap.logistica.repositorio.DestinatarioRepository;
import ar.edu.up.tap.logistica.repositorio.OrdenRepository;
import ar.edu.up.tap.logistica.seguridad.UsuarioActual;
import ar.edu.up.tap.logistica.web.dto.CrearOrdenRequest;
import ar.edu.up.tap.logistica.web.dto.ItemRequest;
import ar.edu.up.tap.logistica.web.dto.OrdenDetalleResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdenServiceTest {

    @Mock
    private OrdenRepository ordenRepository;
    @Mock
    private DepositoRepository depositoRepository;
    @Mock
    private DestinatarioRepository destinatarioRepository;
    @Mock
    private StockService stockService;
    @Mock
    private UsuarioActual usuarioActual;

    @InjectMocks
    private OrdenService ordenService;

    private final Deposito deposito = Fixtures.deposito();
    private final Destinatario destinatario = Fixtures.destinatario(5L, -34.60, -58.42);
    private final Producto yerba = Fixtures.producto(10L);

    @BeforeEach
    void setUp() {
        lenient().when(usuarioActual.obtener()).thenReturn(Fixtures.operador());
    }

    @Test
    void crearDescuentaElStockYDejaLaOrdenCreada() {
        when(depositoRepository.findById(1L)).thenReturn(Optional.of(deposito));
        when(destinatarioRepository.findById(5L)).thenReturn(Optional.of(destinatario));
        when(stockService.descontar(1L, 10L, 3)).thenReturn(new StockDeposito(deposito, yerba, 7));

        OrdenDetalleResponse orden = ordenService.crear(
                new CrearOrdenRequest(1L, 5L, List.of(new ItemRequest(10L, 3))));

        assertThat(orden.estado()).isEqualTo(EstadoOrden.CREADA);
        assertThat(orden.codigoSeguimiento()).startsWith("LG-").hasSize(11);
        assertThat(orden.items()).singleElement().satisfies(i -> assertThat(i.cantidad()).isEqualTo(3));
        verify(stockService).descontar(1L, 10L, 3);
        verify(ordenRepository).save(any(Orden.class));
    }

    @Test
    void siUnProductoVieneRepetidoSeDescuentaLaSuma() {
        when(depositoRepository.findById(1L)).thenReturn(Optional.of(deposito));
        when(destinatarioRepository.findById(5L)).thenReturn(Optional.of(destinatario));
        when(stockService.descontar(1L, 10L, 5)).thenReturn(new StockDeposito(deposito, yerba, 5));

        ordenService.crear(new CrearOrdenRequest(1L, 5L, List.of(new ItemRequest(10L, 2), new ItemRequest(10L, 3))));

        verify(stockService, times(1)).descontar(1L, 10L, 5);
    }

    @Test
    void sinStockSuficienteNoSeGuardaLaOrden() {
        when(depositoRepository.findById(1L)).thenReturn(Optional.of(deposito));
        when(destinatarioRepository.findById(5L)).thenReturn(Optional.of(destinatario));
        when(stockService.descontar(1L, 10L, 50)).thenThrow(new StockInsuficienteException("Producto 10", 7, 50));

        assertThatThrownBy(() -> ordenService.crear(
                new CrearOrdenRequest(1L, 5L, List.of(new ItemRequest(10L, 50)))))
                .isInstanceOf(StockInsuficienteException.class);
        verify(ordenRepository, never()).save(any());
    }

    @Test
    void conUnDepositoInexistenteResponde404() {
        when(depositoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordenService.crear(
                new CrearOrdenRequest(99L, 5L, List.of(new ItemRequest(10L, 1)))))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
        verifyNoInteractions(stockService);
    }

    @Test
    void cancelarUnaOrdenCreadaReintegraElStock() {
        Orden orden = Fixtures.orden(1L, "LG-AAAA1111");
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        OrdenDetalleResponse respuesta = ordenService.cancelar(1L);

        assertThat(respuesta.estado()).isEqualTo(EstadoOrden.CANCELADA);
        verify(stockService).reintegrar(orden);
    }

    @Test
    void cancelarUnaOrdenEnTransitoSeRechazaSinTocarElStock() {
        Orden orden = Fixtures.orden(1L, "LG-AAAA1111");
        Usuario chofer = Fixtures.chofer(3L);
        orden.asignar(chofer);
        orden.iniciarTransito(chofer);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        assertThatThrownBy(() -> ordenService.cancelar(1L)).isInstanceOf(TransicionInvalidaException.class);
        verify(stockService, never()).reintegrar(any());
    }

    @Test
    void elCodigoGeneradoNoRepiteUnoExistente() {
        when(depositoRepository.findById(1L)).thenReturn(Optional.of(deposito));
        when(destinatarioRepository.findById(5L)).thenReturn(Optional.of(destinatario));
        when(stockService.descontar(1L, 10L, 1)).thenReturn(new StockDeposito(deposito, yerba, 9));
        when(ordenRepository.existsByCodigoSeguimiento(anyString())).thenReturn(true, false);

        ordenService.crear(new CrearOrdenRequest(1L, 5L, List.of(new ItemRequest(10L, 1))));

        verify(ordenRepository, times(2)).existsByCodigoSeguimiento(anyString());
    }
}
