package movimientos.dominio;

import java.util.ArrayList;
import producto2.dominio.Producto;

/**
 *
 * @author jesc
 */
public class ProductoLotes {
    private Producto producto;
    private double cantidad;
    private ArrayList<ProductoAlmacen> lotes;
    
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

    public ArrayList<ProductoAlmacen> getLotes() {
        return lotes;
    }

    public void setLotes(ArrayList<ProductoAlmacen> lotes) {
        this.lotes = lotes;
    }
}
