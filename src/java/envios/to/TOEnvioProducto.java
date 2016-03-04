package envios.to;

import traspasos.to.TOTraspasoProducto;

/**
 *
 * @author jesc
 */
public class TOEnvioProducto extends TOTraspasoProducto {
    private int idEnvio;
    private int idSolicitud;
    private double estadistica;
    private double sugerido;

    public int getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(int idEnvio) {
        this.idEnvio = idEnvio;
    }

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public double getEstadistica() {
        return estadistica;
    }

    public void setEstadistica(double estadistica) {
        this.estadistica = estadistica;
    }

    public double getSugerido() {
        return sugerido;
    }

    public void setSugerido(double sugerido) {
        this.sugerido = sugerido;
    }
}
