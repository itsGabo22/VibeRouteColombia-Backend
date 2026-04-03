package com.routeoptimizer.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

/**
 * Value Object inmutable que representa una coordinate geográfica.
 * Usado en: Pedido (ubicacion), Repartidor (ubicacion), MapService
 */
@Embeddable
public class Coordinate implements Serializable {

    private Double lat;
    private Double lng;

    public Coordinate() {
    }

    public Coordinate(Double lat, Double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}
