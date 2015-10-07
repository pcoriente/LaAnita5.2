package salidas;

import java.util.ArrayList;
import movimientos.dominio.ProductoAlmacen;
import movimientos.to1.Lote1;
import producto2.dominio.Producto;

/**
 *
 * @author jesc
 */
public class SalidaAlmacenProducto {
    private Producto producto;
    private double cantidad;
//    private double separados;
    private ArrayList<ProductoAlmacen> lotes;
    
    public SalidaAlmacenProducto() {
        this.producto=new Producto();
        this.lotes=new ArrayList<>();
    }
    
    public SalidaAlmacenProducto(Producto producto) {
        this.producto=producto;
        this.lotes=new ArrayList<>();
    }

    @Override
    public String toString() {
        return this.producto.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.producto != null ? this.producto.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SalidaAlmacenProducto other = (SalidaAlmacenProducto) obj;
        if (this.producto != other.producto && (this.producto == null || !this.producto.equals(other.producto))) {
            return false;
        }
        return true;
    }

//    public double getSeparados() {
//        return separados;
//    }
//
//    public void setSeparados(double separados) {
//        this.separados = separados;
//    }

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
