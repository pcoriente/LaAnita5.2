package solicitudes.to;

/**
 *
 * @author jesc
 */
public class TOSolicitudProducto {
    private int idSolicitud;
    private int idProducto;
    private double cantSolicitada;
    
    public TOSolicitudProducto() {}

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public double getCantSolicitada() {
        return cantSolicitada;
    }

    public void setCantSolicitada(double cantSolicitada) {
        this.cantSolicitada = cantSolicitada;
    }
}
