package movimientos.dominio;

import movimientos.to1.Lote1;
import entradas.dominio.MovimientoProducto;
import java.util.ArrayList;

/**
 *
 * @author jesc
 */
public class SalidaProducto extends MovimientoProducto {
    private double separados;
    ArrayList<Lote1> lotes;
    
    public SalidaProducto() {
        super();
        this.separados=0;
        this.lotes=new ArrayList<>();
    }

    public ArrayList<Lote1> getLotes() {
        return lotes;
    }

    public void setLotes(ArrayList<Lote1> lotes) {
        this.lotes = lotes;
    }

    public double getSeparados() {
        return separados;
    }

    public void setSeparados(double separados) {
        this.separados = separados;
    }
}
