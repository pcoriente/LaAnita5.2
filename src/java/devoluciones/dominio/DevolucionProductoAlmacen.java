package devoluciones.dominio;

import movimientos.dominio.ProductoAlmacen;
import producto2.dominio.Producto;

/**
 *
 * @author jesc
 */
public class DevolucionProductoAlmacen extends ProductoAlmacen {
    private double cantVendida;
    private double cantDevuelta;
    
    public DevolucionProductoAlmacen() {
        super();
    }
    
    public DevolucionProductoAlmacen(Producto producto) {
        super(producto);
    }
    
    public DevolucionProductoAlmacen(int idMovtoAlmacen, Producto producto) {
        super(idMovtoAlmacen, producto);
    }
    
    public String getId() {
        return String.valueOf(super.getProducto().getIdProducto()).concat(super.getLote());
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
