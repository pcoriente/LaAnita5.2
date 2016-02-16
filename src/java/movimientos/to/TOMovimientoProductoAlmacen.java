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

    @Override
    public int hashCode() {
        int hash = 3;
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
//        final TOMovimientoProductoAlmacen other = (TOMovimientoProductoAlmacen) obj;
        if(!super.equals(obj)) {
            return false;
        }
        return true;
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
