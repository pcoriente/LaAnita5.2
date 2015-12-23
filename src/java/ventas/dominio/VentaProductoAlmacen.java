package ventas.dominio;

import java.util.Date;
import movimientos.dominio.ProductoAlmacen;
import producto2.dominio.Producto;

/**
 *
 * @author jesc
 */
public class VentaProductoAlmacen extends ProductoAlmacen {
    private double separados;
    private double disponibles;
    private Date fechaCaducidad;
    
    public VentaProductoAlmacen() {
        super();
        this.fechaCaducidad = new Date();
    }
    
    public VentaProductoAlmacen(Producto producto, String lote) {
        super(producto);
        this.fechaCaducidad = new Date();
    }
    
    public VentaProductoAlmacen(int idMovtoAlmacen, Producto producto) {
        super(idMovtoAlmacen, producto);
        this.fechaCaducidad = new Date();
    }
    
    public String getId() {
        return String.valueOf(super.getProducto().getIdProducto()).concat(super.getLote());
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
}
