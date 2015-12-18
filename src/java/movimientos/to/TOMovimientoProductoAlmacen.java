package movimientos.to;

import java.util.Date;

/**
 *
 * @author jesc
 */
public class TOMovimientoProductoAlmacen extends TOProductoAlmacen {
    private double separados;
    private Date fechaCaducidad;
    
    public TOMovimientoProductoAlmacen() {
        super();
        this.fechaCaducidad = new Date();
    }
    
    public TOMovimientoProductoAlmacen(int idMovtoAlmacen, int idProducto) {
        super(idMovtoAlmacen, idProducto);
        this.fechaCaducidad = new Date();
    }

    public double getSeparados() {
        return separados;
    }

    public void setSeparados(double separados) {
        this.separados = separados;
    }

    public Date getFechaCaducidad() {
        return fechaCaducidad;
    }

    public void setFechaCaducidad(Date fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }
}
