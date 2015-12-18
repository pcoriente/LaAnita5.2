package ventas.to;

import java.util.Date;
import movimientos.to.TOProductoAlmacen;

/**
 *
 * @author jesc
 */
public class TOVentaProductoAlmacen extends TOProductoAlmacen {
    private double disponibles;
    private Date fechaCaducidad;
    
    public TOVentaProductoAlmacen() {
        super();
        this.fechaCaducidad = new Date();
    }
    
    public TOVentaProductoAlmacen(int idMovtoAlmacen, int idProducto) {
        super(idMovtoAlmacen, idProducto);
        this.fechaCaducidad = new Date();
    }

    public double getDisponibles() {
        return disponibles;
    }

    public void setDisponibles(double disponibles) {
        this.disponibles = disponibles;
    }

    public Date getFechaCaducidad() {
        return fechaCaducidad;
    }

    public void setFechaCaducidad(Date fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }
}
