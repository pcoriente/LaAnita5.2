package movimientos.to;

/**
 *
 * @author jesc
 */
public class TOProductoAlmacen {
    private int idMovtoAlmacen;
    private int idProducto;
    private String lote;
    private double cantidad;

    public TOProductoAlmacen() {
        this.lote = "";
    }

    public TOProductoAlmacen(int idMovtoAlmacen, int idProducto) {
        this.idMovtoAlmacen = idMovtoAlmacen;
        this.idProducto = idProducto;
        this.lote = "";
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

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }
}
