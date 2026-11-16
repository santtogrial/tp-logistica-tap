package ar.edu.up.tap.logistica.optimizacion;

import ar.edu.up.tap.logistica.dominio.Coordenada;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OptimizadorVecinoMasCercanoTest {

    private final OptimizadorRutas optimizador = new OptimizadorVecinoMasCercano(25, 5);
    private final Coordenada deposito = new Coordenada(-34.5885, -58.4320);

    private final List<PuntoEntrega> puntos = List.of(
            new PuntoEntrega(1L, new Coordenada(-34.5600, -58.4560)),
            new PuntoEntrega(2L, new Coordenada(-34.5450, -58.4610)),
            new PuntoEntrega(3L, new Coordenada(-34.6030, -58.4200)),
            new PuntoEntrega(4L, new Coordenada(-34.6180, -58.4370)),
            new PuntoEntrega(5L, new Coordenada(-34.6200, -58.3720)));

    @Test
    void cadaOrdenAparecePorUnaSolaVez() {
        List<RecorridoPlanificado> recorridos = optimizador.optimizar(deposito, List.of(7L, 8L), puntos);

        List<Long> visitadas = recorridos.stream()
                .flatMap(r -> r.paradas().stream())
                .map(ParadaPlanificada::ordenId)
                .toList();
        assertThat(visitadas).containsExactlyInAnyOrder(1L, 2L, 3L, 4L, 5L);
    }

    @Test
    void repartePorPartesParejasEntreLosVehiculos() {
        List<RecorridoPlanificado> recorridos = optimizador.optimizar(deposito, List.of(7L, 8L), puntos);

        assertThat(recorridos).hasSize(2);
        assertThat(recorridos).extracting(r -> r.paradas().size()).containsExactlyInAnyOrder(3, 2);
    }

    @Test
    void conMasVehiculosQueParadasNoArmaRutasVacias() {
        List<RecorridoPlanificado> recorridos = optimizador.optimizar(deposito, List.of(7L, 8L, 9L), puntos.subList(0, 2));

        assertThat(recorridos).hasSize(2);
        assertThat(recorridos).allSatisfy(r -> assertThat(r.paradas()).isNotEmpty());
    }

    @Test
    void visitaPrimeroLaParadaMasCercanaYLasSecuenciasSonConsecutivas() {
        // Tres puntos alineados hacia el norte del depósito, cargados en desorden
        List<PuntoEntrega> alineados = List.of(
                new PuntoEntrega(30L, new Coordenada(-34.5585, -58.4320)),
                new PuntoEntrega(10L, new Coordenada(-34.5785, -58.4320)),
                new PuntoEntrega(20L, new Coordenada(-34.5685, -58.4320)));

        RecorridoPlanificado recorrido = optimizador.optimizar(deposito, List.of(7L), alineados).get(0);

        assertThat(recorrido.paradas()).extracting(ParadaPlanificada::ordenId).containsExactly(10L, 20L, 30L);
        assertThat(recorrido.paradas()).extracting(ParadaPlanificada::secuencia).containsExactly(1, 2, 3);
    }

    @Test
    void elEtaCreceALoLargoDelRecorridoYLaDuracionIncluyeLaVuelta() {
        RecorridoPlanificado recorrido = optimizador.optimizar(deposito, List.of(7L), puntos).get(0);

        List<Integer> etas = recorrido.paradas().stream().map(ParadaPlanificada::etaMin).toList();
        assertThat(etas).isSorted();
        assertThat(recorrido.duracionMin()).isGreaterThan(etas.get(etas.size() - 1));
        assertThat(recorrido.distanciaKm()).isPositive();
    }
}
