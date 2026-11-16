package ar.edu.up.tap.logistica.dominio;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "planificacion")
public class Planificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deposito_id")
    private Deposito deposito;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPlanificacion estado;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn;

    @OneToMany(mappedBy = "planificacion", cascade = CascadeType.ALL)
    @OrderBy("id ASC")
    private List<Ruta> rutas = new ArrayList<>();

    protected Planificacion() {
    }

    public Planificacion(Deposito deposito) {
        this.deposito = deposito;
        this.estado = EstadoPlanificacion.PENDIENTE;
        this.creadaEn = LocalDateTime.now();
    }

    public Ruta agregarRuta(Vehiculo vehiculo, double distanciaKm, int duracionMin) {
        Ruta ruta = new Ruta(this, vehiculo, distanciaKm, duracionMin);
        rutas.add(ruta);
        return ruta;
    }

    public void completar() {
        estado = EstadoPlanificacion.COMPLETADA;
    }

    public Long getId() {
        return id;
    }

    public Deposito getDeposito() {
        return deposito;
    }

    public EstadoPlanificacion getEstado() {
        return estado;
    }

    public LocalDateTime getCreadaEn() {
        return creadaEn;
    }

    public List<Ruta> getRutas() {
        return Collections.unmodifiableList(rutas);
    }
}
