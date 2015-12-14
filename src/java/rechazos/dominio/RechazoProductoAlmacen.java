package rechazos.dominio;

import movimientos.dominio.ProductoAlmacen;
import producto2.dominio.Producto;

/**
 *
 * @author jesc
 */
public class RechazoProductoAlmacen extends ProductoAlmacen {
    private double cantTraspasada;
    private double cantRecibida;
    
    public RechazoProductoAlmacen() {
        super();
    }
    
    public RechazoProductoAlmacen(int idMovtoAlmacen, Producto producto) {
        super(idMovtoAlmacen, producto);
    }

    public double getCantTraspasada() {
        return cantTraspasada;
    }

    public void setCantTraspasada(double cantTraspasada) {
        this.cantTraspasada = cantTraspasada;
    }

    public double getCantRecibida() {
        return cantRecibida;
    }

    public void setCantRecibida(double cantRecibida) {
        this.cantRecibida = cantRecibida;
    }
}
