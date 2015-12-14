package recepciones.dominio;

import java.util.ArrayList;
import movimientos.dominio.ProductoOficina;
import recepciones.to.TORecepcionProductoAlmacen;
import rechazos.to.TORechazoProductoAlmacen;

/**
 *
 * @author jesc
 */
public class RecepcionProducto extends ProductoOficina {
    private double cantSolicitada;
    private double cantTraspasada;
    private ArrayList<TORecepcionProductoAlmacen> lotes;
    private double sumaLotes;
    
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

    public double getCantTraspasada() {
        return cantTraspasada;
    }

    public void setCantTraspasada(double cantTraspasada) {
        this.cantTraspasada = cantTraspasada;
    }

    public ArrayList<TORecepcionProductoAlmacen> getLotes() {
        return lotes;
    }

    public void setLotes(ArrayList<TORecepcionProductoAlmacen> lotes) {
        this.lotes = lotes;
    }

    public double getSumaLotes() {
        return sumaLotes;
    }

    public void setSumaLotes(double sumaLotes) {
        this.sumaLotes = sumaLotes;
    }
}
