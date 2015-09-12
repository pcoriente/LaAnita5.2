package entradas.to;

import java.util.ArrayList;
import movimientos.to1.Lote1;
import movimientos.to1.TOMovimientoProducto;

/**
 *
 * @author jesc
 */
public class TOEntradaProducto extends TOMovimientoProducto {
    ArrayList<Lote1> lotes;
    
    public TOEntradaProducto() {
        super();
        this.lotes=new ArrayList<>();
    }

    public ArrayList<Lote1> getLotes() {
        return lotes;
    }

    public void setLotes(ArrayList<Lote1> lotes) {
        this.lotes = lotes;
    }
}
