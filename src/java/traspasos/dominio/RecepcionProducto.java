package traspasos.dominio;

import java.util.ArrayList;
import movimientos.dominio.ProductoOficina;
import traspasos.to.TORecepcionProductoAlmacen;

/**
 *
 * @author jesc
 */
public class RecepcionProducto extends ProductoOficina {
    private double cantSolicitada;
    private double cantEnviada;
    private ArrayList<TORecepcionProductoAlmacen> lotes;
    
    public RecepcionProducto() {
        super();
        this.lotes = new ArrayList<>();
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

    public ArrayList<TORecepcionProductoAlmacen> getLotes() {
        return lotes;
    }

    public void setLotes(ArrayList<TORecepcionProductoAlmacen> lotes) {
        this.lotes = lotes;
    }
}
