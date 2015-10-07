package movimientos.dominio;

import movimientos.to.TOProductoAlmacen;

/**
 *
 * @author jesc
 */
public class ProductoAlmacen extends TOProductoAlmacen {
    private double separados;
    
    public ProductoAlmacen() {
        super();
    }
    
    public ProductoAlmacen(int idMovtoAlmacen, int idProducto) {
        super(idMovtoAlmacen, idProducto);
    }

    public double getSeparados() {
        return separados;
    }

    public void setSeparados(double separados) {
        this.separados = separados;
    }
}
