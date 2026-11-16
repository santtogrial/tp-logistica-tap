package ar.edu.up.tap.logistica.dominio;

import ar.edu.up.tap.logistica.excepcion.TransicionInvalidaException;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "orden")
public class Orden {

    public static final int MAX_INTENTOS = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_seguimiento", nullable = false, unique = true)
    private String codigoSeguimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deposito_id")
    private Deposito deposito;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_id")
    private Destinatario destinatario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoOrden estado;

    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemOrden> items = new ArrayList<>();

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL)
    @OrderBy("fecha ASC, id ASC")
    private List<EventoOrden> eventos = new ArrayList<>();

    protected Orden() {
    }

    public Orden(String codigoSeguimiento, Deposito deposito, Destinatario destinatario, Usuario creadaPor) {
        this.codigoSeguimiento = codigoSeguimiento;
        this.deposito = deposito;
        this.destinatario = destinatario;
        this.estado = EstadoOrden.CREADA;
        this.creadaEn = LocalDateTime.now();
        eventos.add(new EventoOrden(this, null, EstadoOrden.CREADA, creadaPor, "Orden creada"));
    }

    public void agregarItem(Producto producto, int cantidad) {
        items.add(new ItemOrden(this, producto, cantidad));
    }

    public void asignar(Usuario usuario) {
        if (agotoIntentos()) {
            throw new TransicionInvalidaException(
                    "La orden " + codigoSeguimiento + " ya tuvo " + MAX_INTENTOS + " intentos fallidos y no se puede replanificar");
        }
        cambiarEstado(EstadoOrden.ASIGNADA, usuario, null);
    }

    public void desasignar(Usuario usuario) {
        cambiarEstado(EstadoOrden.CREADA, usuario, "Ruta anulada antes de salir");
    }

    public void iniciarTransito(Usuario usuario) {
        cambiarEstado(EstadoOrden.EN_TRANSITO, usuario, null);
    }

    public void marcarEntregada(Usuario usuario) {
        cambiarEstado(EstadoOrden.ENTREGADA, usuario, null);
    }

    public void marcarNoEntregada(String motivo, Usuario usuario) {
        cambiarEstado(EstadoOrden.NO_ENTREGADA, usuario, motivo);
        intentosFallidos++;
    }

    public void devolver(Usuario usuario) {
        cambiarEstado(EstadoOrden.DEVUELTA, usuario, "Devuelta al depósito tras " + intentosFallidos + " intentos fallidos");
    }

    public void cancelar(Usuario usuario) {
        cambiarEstado(EstadoOrden.CANCELADA, usuario, null);
    }

    public boolean agotoIntentos() {
        return intentosFallidos >= MAX_INTENTOS;
    }

    public boolean puedePlanificarse() {
        return estado == EstadoOrden.CREADA || (estado == EstadoOrden.NO_ENTREGADA && !agotoIntentos());
    }

    private void cambiarEstado(EstadoOrden nuevo, Usuario usuario, String observacion) {
        if (!estado.puedePasarA(nuevo)) {
            throw new TransicionInvalidaException(
                    "La orden " + codigoSeguimiento + " no puede pasar de " + estado + " a " + nuevo);
        }
        eventos.add(new EventoOrden(this, estado, nuevo, usuario, observacion));
        estado = nuevo;
    }

    public Long getId() {
        return id;
    }

    public String getCodigoSeguimiento() {
        return codigoSeguimiento;
    }

    public Deposito getDeposito() {
        return deposito;
    }

    public Destinatario getDestinatario() {
        return destinatario;
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public LocalDateTime getCreadaEn() {
        return creadaEn;
    }

    public List<ItemOrden> getItems() {
        return Collections.unmodifiableList(items);
    }

    public List<EventoOrden> getEventos() {
        return Collections.unmodifiableList(eventos);
    }
}
