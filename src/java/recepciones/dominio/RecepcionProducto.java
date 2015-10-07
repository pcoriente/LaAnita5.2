package recepciones.dominio;

import java.util.ArrayList;
import movimientos.dominio.ProductoOficina;

/**
 *
 * @author jesc
 */
public class RecepcionProducto extends ProductoOficina {
    private double cantSolicitada;
    private double cantTraspasada;
    private ArrayList<RecepcionProductoAlmacen> lotes;
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

    public ArrayList<RecepcionProductoAlmacen> getLotes() {
        return lotes;
    }

    public void setLotes(ArrayList<RecepcionProductoAlmacen> lotes) {
        this.lotes = lotes;
    }

    public double getSumaLotes() {
        return sumaLotes;
    }

    public void setSumaLotes(double sumaLotes) {
        this.sumaLotes = sumaLotes;
    }
}
