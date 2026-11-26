package redluz.io;

import redluz.modelo.RedDinamica.Resultado;

/** Texto en español para cada resultado de la red. */
public final class Mensajes {

    private Mensajes() {
    }

    public static String de(Resultado resultado) {
        switch (resultado) {
            case OK: return "OK";
            case ANILLO: return "RECHAZADO: cerraría un anillo (ya están conectados o ambas zonas tienen subestación)";
            case YA_EXISTE: return "ya existe";
            case NO_EXISTE: return "ese tramo no existe";
            case NODO_DESCONOCIDO: return "nodo desconocido";
            case DATO_INVALIDO: return "dato inválido";
            default: return resultado.name();
        }
    }
}
