package envios.dominio;

import traspasos.dominio.TraspasoProducto;

/**
 *
 * @author jesc
 */
public class EnvioProducto extends TraspasoProducto {
    private int idEnvio;
    private int idSolicitud;
    private double estadistica;
    private double sugerido;
    private double sugerido2;
    private double cantSolicitada2;
    
    public EnvioProducto() {
        super();
    }

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

    public double getSugerido2() {
        return sugerido2;
    }

    public void setSugerido2(double sugerido2) {
        this.sugerido2 = sugerido2;
    }

    public double getCantSolicitada2() {
        return cantSolicitada2;
    }

    public void setCantSolicitada2(double cantSolicitada2) {
        this.cantSolicitada2 = cantSolicitada2;
    }
}
