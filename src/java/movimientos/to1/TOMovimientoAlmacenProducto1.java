package movimientos.to1;

import java.util.ArrayList;

/**
 *
 * @author jesc
 */
public class TOMovimientoAlmacenProducto1 {
    private int idMovtoAlmacen;
    private int idProducto;
    private double cantidad;
    private ArrayList<Lote1> lotes;
    
    public TOMovimientoAlmacenProducto1() {}

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

    public ArrayList<Lote1> getLotes() {
        return lotes;
    }

    public void setLotes(ArrayList<Lote1> lotes) {
        this.lotes = lotes;
    }
}
