package traspasos.dominio;

import java.util.ArrayList;
import movimientos.dominio.ProductoOficina;
import movimientos.to.TOProductoAlmacen;

/**
 *
 * @author jesc
 */
public class TraspasoProducto extends ProductoOficina {
    private int idSolicitud;
    private double cantSolicitada;
    private ArrayList<TOProductoAlmacen> lotes;
    
    public TraspasoProducto() {
        super();
        this.lotes = new ArrayList<>();
    }

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public double getCantSolicitada() {
        return cantSolicitada;
    }

    public void setCantSolicitada(double cantSolicitada) {
        this.cantSolicitada = cantSolicitada;
    }

    public ArrayList<TOProductoAlmacen> getLotes() {
        return lotes;
    }

    public void setLotes(ArrayList<TOProductoAlmacen> lotes) {
        this.lotes = lotes;
    }
}
