package ar.edu.up.tap.logistica.dominio;

import ar.edu.up.tap.logistica.excepcion.ReglaNegocioException;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "parada")
public class Parada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ruta_id")
    private Ruta ruta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orden_id")
    private Orden orden;

    @Column(nullable = false)
    private int secuencia;

    @Column(name = "eta_min", nullable = false)
    private int etaMin;

    @Column(name = "llegada_real")
    private LocalDateTime llegadaReal;

    @Enumerated(EnumType.STRING)
    private ResultadoEntrega resultado;

    private String motivo;

    protected Parada() {
    }

    Parada(Ruta ruta, Orden orden, int secuencia, int etaMin) {
        this.ruta = ruta;
        this.orden = orden;
        this.secuencia = secuencia;
        this.etaMin = etaMin;
    }

    public void registrarResultado(ResultadoEntrega resultado, String motivo) {
        if (estaResuelta()) {
            throw new ReglaNegocioException("La parada " + id + " ya tiene resultado: " + this.resultado);
        }
        if (resultado == ResultadoEntrega.NO_ENTREGADA && (motivo == null || motivo.isBlank())) {
            throw new ReglaNegocioException("Para una entrega fallida hay que indicar el motivo");
        }
        this.resultado = resultado;
        this.motivo = motivo;
        this.llegadaReal = LocalDateTime.now();
    }

    public boolean estaResuelta() {
        return resultado != null;
    }

    public Long getId() {
        return id;
    }

    public Ruta getRuta() {
        return ruta;
    }

    public Orden getOrden() {
        return orden;
    }

    public int getSecuencia() {
        return secuencia;
    }

    public int getEtaMin() {
        return etaMin;
    }

    public LocalDateTime getLlegadaReal() {
        return llegadaReal;
    }

    public ResultadoEntrega getResultado() {
        return resultado;
    }

    public String getMotivo() {
        return motivo;
    }
}
