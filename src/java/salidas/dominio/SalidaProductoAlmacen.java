package salidas.dominio;

import java.util.Date;
import movimientos.dominio.ProductoAlmacen;
import producto2.dominio.Producto;

/**
 *
 * @author jesc
 */
public class SalidaProductoAlmacen extends ProductoAlmacen {
    private Date fechaCaducidad;
    
    public SalidaProductoAlmacen() {
        super();
        this.fechaCaducidad = new Date();
    }
    
    public SalidaProductoAlmacen(Producto producto) {
        super(producto);
        this.fechaCaducidad = new Date();
    }

    public Date getFechaCaducidad() {
        return fechaCaducidad;
    }

    public void setFechaCaducidad(Date fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }
}
