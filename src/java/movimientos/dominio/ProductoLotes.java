package movimientos.dominio;

import java.util.ArrayList;
import producto2.dominio.Producto;

/**
 *
 * @author jesc
 */
public class ProductoLotes {
    private int idMovtoAlmacen;
    private Producto producto;
    private double cantidad;
    private ArrayList<Lote> lotes;
    
    public ProductoLotes() {}

    public int getIdMovtoAlmacen() {
        return idMovtoAlmacen;
    }

    public void setIdMovtoAlmacen(int idMovtoAlmacen) {
        this.idMovtoAlmacen = idMovtoAlmacen;
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

    public ArrayList<Lote> getLotes() {
        return lotes;
    }

    public void setLotes(ArrayList<Lote> lotes) {
        this.lotes = lotes;
    }
}
