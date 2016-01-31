package traspasos.to;

import movimientos.to.TOProductoOficina;

/**
 *
 * @author jesc
 */
public class TOTraspasoProducto extends TOProductoOficina {
//    private int idSolicitud;
    private double cantSolicitada;
    private double cantTraspasada;
    
    public TOTraspasoProducto() {
        super();
    }

    public double getCantSolicitada() {
        return cantSolicitada;
    }

    public void setCantSolicitada(double cantSolicitada) {
        this.cantSolicitada = cantSolicitada;
    }

    public double getCantTraspasada() {
        return cantTraspasada;
    }

    public void setCantTraspasada(double cantTraspasada) {
        this.cantTraspasada = cantTraspasada;
    }
}
