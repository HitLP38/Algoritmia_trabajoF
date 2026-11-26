package fibranet.modelo;

/**
 * Cálculos geográficos para la Red Dorsal Nacional.
 *
 * Distancia haversine: la distancia más corta entre dos puntos sobre la
 * superficie de la Tierra (arco de círculo máximo), a partir de su latitud
 * y longitud en grados.
 *
 *   a = sin²(Δlat/2) + cos(lat1)·cos(lat2)·sin²(Δlon/2)
 *   d = 2·R·asin(√a)          con R = 6371 km (radio medio de la Tierra)
 *
 * Es una cota inferior del cable real: la fibra sigue carreteras y
 * relieve, por eso el plan reporta km "en línea recta".
 */
public final class Geo {

    public static final double RADIO_TIERRA_KM = 6371.0;

    private Geo() {
        // clase de utilidad: no se crean objetos
    }

    /** Distancia en km entre (lat1, lon1) y (lat2, lon2), en grados decimales. */
    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * RADIO_TIERRA_KM * Math.asin(Math.min(1.0, Math.sqrt(a)));   // min: evita NaN por redondeo en puntos opuestos
    }
}
