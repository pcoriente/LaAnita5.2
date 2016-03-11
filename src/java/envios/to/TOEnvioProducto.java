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
    private double existencia;
    private double sugerido;
    private int diasInventario;
    private int banCajas;
    private double solicitada;

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

    public double getExistencia() {
        return existencia;
    }

    public void setExistencia(double existencia) {
        this.existencia = existencia;
    }

    public double getSugerido() {
        return sugerido;
    }

    public void setSugerido(double sugerido) {
        this.sugerido = sugerido;
    }

    public int getDiasInventario() {
        return diasInventario;
    }

    public void setDiasInventario(int diasInventario) {
        this.diasInventario = diasInventario;
    }

    public int getBanCajas() {
        return banCajas;
    }

    public void setBanCajas(int banCajas) {
        this.banCajas = banCajas;
    }

    public double getSolicitada() {
        return solicitada;
    }

    public void setSolicitada(double solicitada) {
        this.solicitada = solicitada;
    }
}
