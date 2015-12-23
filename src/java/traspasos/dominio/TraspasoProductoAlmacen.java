package traspasos.dominio;

import java.util.Date;
import movimientos.dominio.ProductoAlmacen;
import producto2.dominio.Producto;

/**
 *
 * @author jesc
 */
public class TraspasoProductoAlmacen extends ProductoAlmacen {
    private double separados;
    private double disponibles;
    private Date fechaCaducidad;
     
    public TraspasoProductoAlmacen() {
        super();
        this.fechaCaducidad = new Date();
    }
        
    public TraspasoProductoAlmacen(Producto producto, String lote) {
        super(producto);
        this.fechaCaducidad = new Date();
    }
    
    public TraspasoProductoAlmacen(int idMovtoAlmacen, Producto producto) {
        super(idMovtoAlmacen, producto);
        this.fechaCaducidad = new Date();
    }

    public double getSeparados() {
        return separados;
    }

    public void setSeparados(double separados) {
        this.separados = separados;
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

    public String getId() {
        return String.valueOf(super.getProducto().getIdProducto()).concat(super.getLote());
    }
}
