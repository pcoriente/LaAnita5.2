package salidas.to;

import java.util.Date;
import movimientos.to.TOProductoAlmacen;

/**
 *
 * @author jesc
 */
public class TOSalidaProductoAlmacen extends TOProductoAlmacen {
    private double separados;
    private Date fechaCaducidad;
    
    public TOSalidaProductoAlmacen() {
        super();
        this.fechaCaducidad = new Date();
    }
    
    public TOSalidaProductoAlmacen(int idMovtoAlmacen, int idProducto) {
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
