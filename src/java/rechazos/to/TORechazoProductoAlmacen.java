package rechazos.to;

import movimientos.to.TOMovimientoProductoAlmacen;

/**
 *
 * @author jesc
 */
public class TORechazoProductoAlmacen extends TOMovimientoProductoAlmacen {
    private double cantTraspasada;
    private double cantRecibida;
    private int piezas;
    
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

    public int getPiezas() {
        return piezas;
    }

    public void setPiezas(int piezas) {
        this.piezas = piezas;
    }
}
