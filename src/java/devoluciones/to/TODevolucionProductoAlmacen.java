package devoluciones.to;

import movimientos.to.TOProductoAlmacen;

/**
 *
 * @author jesc
 */
public class TODevolucionProductoAlmacen extends TOProductoAlmacen {
    private double cantVendida;
    private double cantDevuelta;
    
    public TODevolucionProductoAlmacen() {
        super();
    }

    public double getCantVendida() {
        return cantVendida;
    }

    public void setCantVendida(double cantVendida) {
        this.cantVendida = cantVendida;
    }

    public double getCantDevuelta() {
        return cantDevuelta;
    }

    public void setCantDevuelta(double cantDevuelta) {
        this.cantDevuelta = cantDevuelta;
    }
}
