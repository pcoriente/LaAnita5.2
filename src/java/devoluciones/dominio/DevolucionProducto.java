package devoluciones.dominio;

import movimientos.dominio.ProductoOficina;

/**
 *
 * @author jesc
 */
public class DevolucionProducto extends ProductoOficina {
    private double cantVendida;
    private double cantVendidaSinCargo;
    private double cantDevuelta;
    private double cantDevueltaSinCargo;
    
    public DevolucionProducto() {
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
