package rechazos.to;

import movimientos.to.TOProductoOficina;

/**
 *
 * @author jesc
 */
public class TORechazoProducto extends TOProductoOficina {
    private double cantTraspasada;
    private double cantRecibida;
    
    public TORechazoProducto() {
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
