package ar.edu.up.tap.logistica.dominio;

import ar.edu.up.tap.logistica.excepcion.TransicionInvalidaException;
import jakarta.persistence.*;

@Entity
@Table(name = "vehiculo")
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String patente;

    @Column(nullable = false)
    private String modelo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deposito_base_id")
    private Deposito depositoBase;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chofer_id")
    private Usuario chofer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoVehiculo estado;

    protected Vehiculo() {
    }

    public Vehiculo(String patente, String modelo, Deposito depositoBase, Usuario chofer) {
        this.patente = patente;
        this.modelo = modelo;
        this.depositoBase = depositoBase;
        this.chofer = chofer;
        this.estado = EstadoVehiculo.DISPONIBLE;
    }

    public boolean estaDisponible() {
        return estado == EstadoVehiculo.DISPONIBLE;
    }

    public boolean esConducidoPor(Usuario usuario) {
        return chofer != null && chofer.getId().equals(usuario.getId());
    }

    public void salirARuta() {
        if (!estaDisponible()) {
            throw new TransicionInvalidaException("El vehículo " + patente + " no está disponible (estado " + estado + ")");
        }
        estado = EstadoVehiculo.EN_RUTA;
    }

    public void volverDeRuta() {
        if (estado != EstadoVehiculo.EN_RUTA) {
            throw new TransicionInvalidaException("El vehículo " + patente + " no está en ruta");
        }
        estado = EstadoVehiculo.DISPONIBLE;
    }

    public void cambiarEstado(EstadoVehiculo nuevo) {
        if (!estado.admiteCambioManualA(nuevo)) {
            throw new TransicionInvalidaException(
                    "El vehículo " + patente + " no puede pasar de " + estado + " a " + nuevo);
        }
        estado = nuevo;
    }

    public Long getId() {
        return id;
    }

    public String getPatente() {
        return patente;
    }

    public String getModelo() {
        return modelo;
    }

    public Deposito getDepositoBase() {
        return depositoBase;
    }

    public Usuario getChofer() {
        return chofer;
    }

    public EstadoVehiculo getEstado() {
        return estado;
    }
}
