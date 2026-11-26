package redluz.modelo;

/** Tipos de nodo de la red eléctrica. */
public final class Tipos {

    public static final String SUBESTACION = "SUBESTACION";
    public static final String POSTE = "POSTE";
    public static final String CLIENTE = "CLIENTE";

    private Tipos() {
    }

    public static boolean esValido(String tipo) {
        return SUBESTACION.equals(tipo) || POSTE.equals(tipo) || CLIENTE.equals(tipo);
    }
}
