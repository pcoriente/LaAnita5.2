package devoluciones.to;

import movimientos.to.TOProductoOficina;

/**
 *
 * @author jesc
 */
public class TODevolucionProducto extends TOProductoOficina {
    private double cantVendida;
    private double cantVendidaSinCargo;
    private double cantDevuelta;
    private double cantDevueltaSinCargo;
    
    public TODevolucionProducto() {
        super();
    }

    public double getCantVendida() {
        return cantVendida;
    }

    public void setCantVendida(double cantVendida) {
        this.cantVendida = cantVendida;
    }

    public double getCantVendidaSinCargo() {
        return cantVendidaSinCargo;
    }

    public void setCantVendidaSinCargo(double cantVendidaSinCargo) {
        this.cantVendidaSinCargo = cantVendidaSinCargo;
    }

    public double getCantDevuelta() {
        return cantDevuelta;
    }

    public void setCantDevuelta(double cantDevuelta) {
        this.cantDevuelta = cantDevuelta;
    }

    public double getCantDevueltaSinCargo() {
        return cantDevueltaSinCargo;
    }

    public void setCantDevueltaSinCargo(double cantDevueltaSinCargo) {
        this.cantDevueltaSinCargo = cantDevueltaSinCargo;
    }
}
