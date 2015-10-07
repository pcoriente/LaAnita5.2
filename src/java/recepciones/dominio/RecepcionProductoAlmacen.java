package recepciones.dominio;

import movimientos.dominio.ProductoAlmacen;

/**
 *
 * @author jesc
 */
public class RecepcionProductoAlmacen extends ProductoAlmacen {
//    private double cantSolicitada;
    private double cantTraspasada;
    
    public RecepcionProductoAlmacen() {
        super();
    }
    
    public RecepcionProductoAlmacen(int idMovtoAlmacen, int idProducto) {
        super(idMovtoAlmacen, idProducto);
    }

//    public double getCantSolicitada() {
//        return cantSolicitada;
//    }
//
//    public void setCantSolicitada(double cantSolicitada) {
//        this.cantSolicitada = cantSolicitada;
//    }

    public double getCantTraspasada() {
        return cantTraspasada;
    }

    public void setCantTraspasada(double cantTraspasada) {
        this.cantTraspasada = cantTraspasada;
    }
}
