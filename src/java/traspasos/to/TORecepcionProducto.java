package traspasos.to;

import movimientos.to.TOProductoOficina;

/**
 *
 * @author jesc
 */
public class TORecepcionProducto extends TOProductoOficina {
    private double cantSolicitada;
    private double cantEnviada;
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

    public double getCantEnviada() {
        return cantEnviada;
    }

    public void setCantEnviada(double cantEnviada) {
        this.cantEnviada = cantEnviada;
    }

    public double getSeparados() {
        return separados;
    }

    public void setSeparados(double separados) {
        this.separados = separados;
    }
}
