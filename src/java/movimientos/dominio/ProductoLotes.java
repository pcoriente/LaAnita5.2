package movimientos.dominio;

import java.util.ArrayList;
import movimientos.to.TOMovimientoProductoAlmacen;
import producto2.dominio.Producto;

/**
 *
 * @author jesc
 */
public class ProductoLotes {
    private Producto producto;
    private double cantidad;
    private ArrayList<TOMovimientoProductoAlmacen> lotes;
    
    public ProductoLotes() {
        this.producto=new Producto();
        this.lotes = new ArrayList<>();
    }
    
    public ProductoLotes(Producto producto) {
        this.producto = producto;
        this.lotes = new ArrayList<>();
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public ArrayList<TOMovimientoProductoAlmacen> getLotes() {
        return lotes;
    }

    public void setLotes(ArrayList<TOMovimientoProductoAlmacen> lotes) {
        this.lotes = lotes;
    }
}
