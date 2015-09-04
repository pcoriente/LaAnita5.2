package traspasos.to;

import movimientos.to.TOProductoOficina;

/**
 *
 * @author jesc
 */
public class TOTraspasoProducto extends TOProductoOficina {
    private int idSolicitud;
    private double cantSolicitada;
    
    public TOTraspasoProducto() {
        super();
    }

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public double getCantSolicitada() {
        return cantSolicitada;
    }

    public void setCantSolicitada(double cantSolicitada) {
        this.cantSolicitada = cantSolicitada;
    }
}
