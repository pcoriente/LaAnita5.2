package recepciones.to;

import movimientos.to.TOMovimientoProductoAlmacen;

/**
 *
 * @author jesc
 */
public class TORecepcionProductoAlmacen extends TOMovimientoProductoAlmacen {
//    private double cantSolicitada;
    private double cantTraspasada;
//    private double separados;
    
    public TORecepcionProductoAlmacen() {
        super();
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
//        final TORecepcionProductoAlmacen other = (TORecepcionProductoAlmacen) obj;
        if(!super.equals(obj)) {
            return false;
        }
        return true;
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
//
//    public double getSeparados() {
//        return separados;
//    }
//
//    public void setSeparados(double separados) {
//        this.separados = separados;
//    }
}
