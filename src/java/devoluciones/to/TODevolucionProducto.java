package devoluciones.to;

import movimientos.to.TOProductoOficina;

/**
 *
 * @author jesc
 */
public class TODevolucionProducto extends TOProductoOficina {
    private double cantVendida;
    private double cantDevuelta;
    
    public TODevolucionProducto() {
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
