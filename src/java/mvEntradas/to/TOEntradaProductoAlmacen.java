package mvEntradas.to;

import java.util.Date;
import movimientos.to.TOProductoAlmacen;

/**
 *
 * @author jesc
 */
public class TOEntradaProductoAlmacen extends TOProductoAlmacen {
    private double separados;
    private Date fechaCaducidad;
    
    public TOEntradaProductoAlmacen() {
        super();
        this.fechaCaducidad = new Date();
    }
    
    public TOEntradaProductoAlmacen(int idMovtoAlmacen, int idProducto) {
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
