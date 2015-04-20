package movimientos.dominio;

import entradas.dominio.MovimientoProducto;
import java.util.ArrayList;

/**
 *
 * @author jesc
 */
public class SalidaProducto extends MovimientoProducto {
    private double separados;
    ArrayList<Lote> lotes;
    
    public SalidaProducto() {
        super();
        this.separados=0;
        this.lotes=new ArrayList<Lote>();
    }

    public ArrayList<Lote> getLotes() {
        return lotes;
    }

    public void setLotes(ArrayList<Lote> lotes) {
        this.lotes = lotes;
    }

    public double getSeparados() {
        return separados;
    }

    public void setSeparados(double separados) {
        this.separados = separados;
    }
}
