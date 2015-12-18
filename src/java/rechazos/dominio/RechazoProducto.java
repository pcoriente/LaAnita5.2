package rechazos.dominio;

import java.util.ArrayList;
import movimientos.dominio.ProductoOficina;
import rechazos.to.TORechazoProductoAlmacen;

/**
 *
 * @author jesc
 */
public class RechazoProducto extends ProductoOficina {
    private double cantTraspasada;
    private double cantRecibida;
    private ArrayList<TORechazoProductoAlmacen> lotes;
    private double sumaLotes;
    
    public RechazoProducto() {
        super();
        this.lotes = new ArrayList<>();
    }

    public double getCantTraspasada() {
        return cantTraspasada;
    }

    public void setCantTraspasada(double cantTraspasada) {
        this.cantTraspasada = cantTraspasada;
    }

    public double getCantRecibida() {
        return cantRecibida;
    }

    public void setCantRecibida(double cantRecibida) {
        this.cantRecibida = cantRecibida;
    }

    public ArrayList<TORechazoProductoAlmacen> getLotes() {
        return lotes;
    }

    public void setLotes(ArrayList<TORechazoProductoAlmacen> lotes) {
        this.lotes = lotes;
    }

    public double getSumaLotes() {
        return sumaLotes;
    }

    public void setSumaLotes(double sumaLotes) {
        this.sumaLotes = sumaLotes;
    }
}
