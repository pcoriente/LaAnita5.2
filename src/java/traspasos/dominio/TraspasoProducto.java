package traspasos.dominio;

import movimientos.dominio.ProductoOficina;

/**
 *
 * @author jesc
 */
public class TraspasoProducto extends ProductoOficina {
    private double cantSolicitada;
//    private ArrayList<ProductoAlmacen> lotes;
    
    public TraspasoProducto() {
        super();
//        this.lotes = new ArrayList<>();
    }

    public double getCantSolicitada() {
        return cantSolicitada;
    }

    public void setCantSolicitada(double cantSolicitada) {
        this.cantSolicitada = cantSolicitada;
    }

//    public ArrayList<ProductoAlmacen> getLotes() {
//        return lotes;
//    }
//
//    public void setLotes(ArrayList<ProductoAlmacen> lotes) {
//        this.lotes = lotes;
//    }
}
