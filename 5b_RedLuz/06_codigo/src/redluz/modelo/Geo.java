package redluz.modelo;

/**
 * Distancia haversine entre dos puntos (latitud, longitud) en grados:
 *
 *   a = sin²(Δlat/2) + cos(lat1)·cos(lat2)·sin²(Δlon/2)
 *   d = 2·R·asin(√a)          con R = 6371 km
 *
 * Se usa cuando un tramo del CSV no trae km pero sus dos nodos traen
 * coordenadas (caso real con datos de OpenStreetMap). Es la distancia en
 * línea recta; el cable real puede ser algo más largo.
 */
public final class Geo {

    public static final double RADIO_TIERRA_KM = 6371.0;

    private Geo() {
        // clase de utilidad
    }

    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * RADIO_TIERRA_KM * Math.asin(Math.sqrt(a));
    }
}
