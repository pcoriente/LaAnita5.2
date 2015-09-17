package compras.to;

import movimientos.to.TOProductoAlmacen;

/**
 *
 * @author jesc
 */
public class TOProductoCompraAlmacen extends TOProductoAlmacen {
    private double cantOrdenada;
    
    public TOProductoCompraAlmacen() {
        super();
    }

    public double getCantOrdenada() {
        return cantOrdenada;
    }

    public void setCantOrdenada(double cantOrdenada) {
        this.cantOrdenada = cantOrdenada;
    }
}
