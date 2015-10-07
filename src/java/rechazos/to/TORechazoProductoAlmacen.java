package rechazos.to;

import movimientos.to.TOProductoAlmacen;

/**
 *
 * @author jesc
 */
public class TORechazoProductoAlmacen extends TOProductoAlmacen {
    private double cantTraspasada;
    private double cantRecibida;
    
    public TORechazoProductoAlmacen() {
        super();
    }

    public double getCantTraspasada() {
        return cantTraspasada;
    }

    public void setCantTraspasada(double cantTraspasada) {
        this.cantTraspasada = cantTraspasada;
    }

    public double getCantRecibida() {
        return cantRecibida;
    }

    public void setCantRecibida(double cantRecibida) {
        this.cantRecibida = cantRecibida;
    }
}
