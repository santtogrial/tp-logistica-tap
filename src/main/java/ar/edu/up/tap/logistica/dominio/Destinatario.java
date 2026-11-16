package ar.edu.up.tap.logistica.dominio;

import jakarta.persistence.*;

@Entity
@Table(name = "destinatario")
public class Destinatario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String telefono;

    @Column(nullable = false)
    private String direccion;

    @Embedded
    private Coordenada ubicacion;

    protected Destinatario() {
    }

    public Destinatario(String nombre, String telefono, String direccion, Coordenada ubicacion) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
        this.ubicacion = ubicacion;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public Coordenada getUbicacion() {
        return ubicacion;
    }
}
