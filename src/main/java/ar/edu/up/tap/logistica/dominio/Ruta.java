package ar.edu.up.tap.logistica.dominio;

import ar.edu.up.tap.logistica.excepcion.TransicionInvalidaException;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "ruta")
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "planificacion_id")
    private Planificacion planificacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRuta estado;

    @Column(name = "distancia_km", nullable = false)
    private double distanciaKm;

    @Column(name = "duracion_min", nullable = false)
    private int duracionMin;

    @Column(name = "iniciada_en")
    private LocalDateTime iniciadaEn;

    @Column(name = "finalizada_en")
    private LocalDateTime finalizadaEn;

    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL)
    @OrderBy("secuencia ASC")
    private List<Parada> paradas = new ArrayList<>();

    protected Ruta() {
    }

    Ruta(Planificacion planificacion, Vehiculo vehiculo, double distanciaKm, int duracionMin) {
        this.planificacion = planificacion;
        this.vehiculo = vehiculo;
        this.distanciaKm = distanciaKm;
        this.duracionMin = duracionMin;
        this.estado = EstadoRuta.PLANIFICADA;
    }

    public void agregarParada(Orden orden, int secuencia, int etaMin) {
        paradas.add(new Parada(this, orden, secuencia, etaMin));
    }

    public void iniciar() {
        validarEstado(EstadoRuta.PLANIFICADA, "iniciar");
        estado = EstadoRuta.EN_CURSO;
        iniciadaEn = LocalDateTime.now();
    }

    public void finalizar() {
        validarEstado(EstadoRuta.EN_CURSO, "finalizar");
        if (!todasLasParadasResueltas()) {
            throw new TransicionInvalidaException("La ruta " + id + " tiene paradas sin resultado registrado");
        }
        estado = EstadoRuta.FINALIZADA;
        finalizadaEn = LocalDateTime.now();
    }

    public void anular() {
        validarEstado(EstadoRuta.PLANIFICADA, "anular");
        estado = EstadoRuta.ANULADA;
    }

    public boolean todasLasParadasResueltas() {
        return paradas.stream().allMatch(Parada::estaResuelta);
    }

    public boolean estaEnCurso() {
        return estado == EstadoRuta.EN_CURSO;
    }

    public Optional<Parada> proximaParada() {
        return paradas.stream().filter(p -> !p.estaResuelta()).findFirst();
    }

    private void validarEstado(EstadoRuta esperado, String accion) {
        if (estado != esperado) {
            throw new TransicionInvalidaException(
                    "No se puede " + accion + " la ruta " + id + " porque está " + estado);
        }
    }

    public Long getId() {
        return id;
    }

    public Planificacion getPlanificacion() {
        return planificacion;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public EstadoRuta getEstado() {
        return estado;
    }

    public double getDistanciaKm() {
        return distanciaKm;
    }

    public int getDuracionMin() {
        return duracionMin;
    }

    public LocalDateTime getIniciadaEn() {
        return iniciadaEn;
    }

    public LocalDateTime getFinalizadaEn() {
        return finalizadaEn;
    }

    public List<Parada> getParadas() {
        return Collections.unmodifiableList(paradas);
    }
}
