package movimientos.to;

import java.util.Date;

/**
 *
 * @author jesc
 */
public class TOProductoAlmacen {    // Esta clase se usuara para grabar y compras (solo un lote por empaque)

    private int idMovtoAlmacen;
    private int idProducto;
    private String lote;
    private double cantidad;
    private Date fechaCaducidad;    // Pendiente por eliminar, solo queda por ahora por compatibilidad

    public TOProductoAlmacen() {
        this.lote = "";
        this.fechaCaducidad = new Date();
    }

    public TOProductoAlmacen(int idMovtoAlmacen, int idProducto) {
        this.idMovtoAlmacen = idMovtoAlmacen;
        this.idProducto = idProducto;
        this.lote = "";
        this.fechaCaducidad = new Date();
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

    public Date getFechaCaducidad() {
        return fechaCaducidad;
    }

    public void setFechaCaducidad(Date fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }
}
