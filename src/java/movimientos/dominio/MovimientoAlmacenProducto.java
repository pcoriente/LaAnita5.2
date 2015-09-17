package movimientos.dominio;

import movimientos.to1.Lote1;
import java.util.ArrayList;
import java.util.Objects;
import producto2.dominio.Producto;

/**
 *
 * @author jesc
 */
public class MovimientoAlmacenProducto {
    private int idMovtoAlmacen;
    private Producto producto;
    private double cantOrdenada;
    private double cantRecibida;
    private double cantidad;
    private ArrayList<Lote1> lotes;
    
    public MovimientoAlmacenProducto() {
        this.producto=new Producto();
    }

    @Override
    public String toString() {
        return this.producto.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.producto);
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
        final MovimientoAlmacenProducto other = (MovimientoAlmacenProducto) obj;
        if (!Objects.equals(this.producto, other.producto)) {
            return false;
        }
        return true;
    }

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

    public double getCantOrdenada() {
        return cantOrdenada;
    }

    public void setCantOrdenada(double cantOrdenada) {
        this.cantOrdenada = cantOrdenada;
    }

    public double getCantRecibida() {
        return cantRecibida;
    }

    public void setCantRecibida(double cantRecibida) {
        this.cantRecibida = cantRecibida;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public ArrayList<Lote1> getLotes() {
        return lotes;
    }

    public void setLotes(ArrayList<Lote1> lotes) {
        this.lotes = lotes;
    }
}
