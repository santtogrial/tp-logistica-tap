package ar.edu.up.tap.logistica.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Coordenada {

    private static final double RADIO_TIERRA_KM = 6371.0;

    @Column(name = "lat", nullable = false)
    private double lat;

    @Column(name = "lng", nullable = false)
    private double lng;

    protected Coordenada() {
    }

    public Coordenada(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    /** Distancia en línea recta (fórmula de Haversine). */
    public double distanciaKm(Coordenada otra) {
        double dLat = Math.toRadians(otra.lat - lat);
        double dLng = Math.toRadians(otra.lng - lng);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(otra.lat))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return RADIO_TIERRA_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}
