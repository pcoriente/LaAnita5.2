package movimientos.to;

import java.util.ArrayList;

/**
 *
 * @author jesc
 */
public class TOProductoLotes {
    private int idMovtoAlmacen;
    private int idProducto;
    private double cantidad;
    private ArrayList<TOProductoAlmacen> lotes;
    
    public TOProductoLotes() {
        this.lotes=new ArrayList<>();
    }

    public int getIdMovtoAlmacen() {
        return idMovtoAlmacen;
    }

    public void setIdMovtoAlmacen(int idMovtoAlmacen) {
        this.idMovtoAlmacen = idMovtoAlmacen;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public ArrayList<TOProductoAlmacen> getLotes() {
        return lotes;
    }

    public void setLotes(ArrayList<TOProductoAlmacen> lotes) {
        this.lotes = lotes;
    }
}
