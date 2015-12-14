package compras.dominio;

import movimientos.dominio.ProductoAlmacen;
import producto2.dominio.Producto;

/**
 *
 * @author jesc
 */
public class ProductoCompraAlmacen extends ProductoAlmacen {
    private double cantOrdenada;
    private double separados;
    
    public ProductoCompraAlmacen() {
        super();
    }
    
    public ProductoCompraAlmacen(Producto producto) {
        super(producto);
    }
    
    public ProductoCompraAlmacen(int idMovtoAlmacen, Producto producto) {
        super(idMovtoAlmacen, producto);
    }
    
    public double getCantOrdenada() {
        return cantOrdenada;
    }

    public void setCantOrdenada(double cantOrdenada) {
        this.cantOrdenada = cantOrdenada;
    }

    public double getSeparados() {
        return separados;
    }

    public void setSeparados(double separados) {
        this.separados = separados;
    }
}
