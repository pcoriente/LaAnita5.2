package rechazos.dominio;

import movimientos.dominio.ProductoAlmacen;

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
    
    public RechazoProductoAlmacen(int idMovtoAlmacen, int idProducto) {
        super(idMovtoAlmacen, idProducto);
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
