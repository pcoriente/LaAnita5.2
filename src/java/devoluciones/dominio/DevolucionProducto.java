package devoluciones.dominio;

import movimientos.dominio.ProductoOficina;

/**
 *
 * @author jesc
 */
public class DevolucionProducto extends ProductoOficina {
    private double cantVendida;
    private double cantDevuelta;
    
    public DevolucionProducto() {
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
