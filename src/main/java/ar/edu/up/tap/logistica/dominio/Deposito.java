package ar.edu.up.tap.logistica.dominio;

import jakarta.persistence.*;

@Entity
@Table(name = "deposito")
public class Deposito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String direccion;

    @Embedded
    private Coordenada ubicacion;

    protected Deposito() {
    }

    public Deposito(String nombre, String direccion, Coordenada ubicacion) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.ubicacion = ubicacion;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public Coordenada getUbicacion() {
        return ubicacion;
    }
}
