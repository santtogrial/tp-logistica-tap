package ar.edu.up.tap.logistica.dominio;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "evento_orden")
public class EventoOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orden_id")
    private Orden orden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior")
    private EstadoOrden estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false)
    private EstadoOrden estadoNuevo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    private String observacion;

    protected EventoOrden() {
    }

    EventoOrden(Orden orden, EstadoOrden estadoAnterior, EstadoOrden estadoNuevo, Usuario usuario, String observacion) {
        this.orden = orden;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.usuario = usuario;
        this.observacion = observacion;
        this.fecha = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public EstadoOrden getEstadoAnterior() {
        return estadoAnterior;
    }

    public EstadoOrden getEstadoNuevo() {
        return estadoNuevo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getObservacion() {
        return observacion;
    }
}
