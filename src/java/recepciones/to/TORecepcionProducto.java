package recepciones.to;

import movimientos.to.TOProductoOficina;

/**
 *
 * @author jesc
 */
public class TORecepcionProducto extends TOProductoOficina {
    private double cantSolicitada;
    private double cantTraspasada;
    private double separados;
    
    public TORecepcionProducto() {
        super();
    }

    public double getCantSolicitada() {
        return cantSolicitada;
    }

    public void setCantSolicitada(double cantSolicitada) {
        this.cantSolicitada = cantSolicitada;
    }

    public double getCantTraspasada() {
        return cantTraspasada;
    }

    public void setCantTraspasada(double cantTraspasada) {
        this.cantTraspasada = cantTraspasada;
    }

    public double getSeparados() {
        return separados;
    }

    public void setSeparados(double separados) {
        this.separados = separados;
    }
}
