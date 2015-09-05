package traspasos.to;

import movimientos.to.TOProductoAlmacen;

/**
 *
 * @author jesc
 */
public class TORecepcionProductoAlmacen extends TOProductoAlmacen {
    private double cantEnviada;
    private double separados;
    
    public TORecepcionProductoAlmacen() {
        super();
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
