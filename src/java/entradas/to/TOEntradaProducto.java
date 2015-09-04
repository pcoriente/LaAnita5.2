package entradas.to;

import java.util.ArrayList;
import movimientos.dominio.Lote;
import movimientos.to1.TOMovimientoProducto;

/**
 *
 * @author jesc
 */
public class TOEntradaProducto extends TOMovimientoProducto {
    ArrayList<Lote> lotes;
    
    public TOEntradaProducto() {
        super();
        this.lotes=new ArrayList<>();
    }

    public ArrayList<Lote> getLotes() {
        return lotes;
    }

    public void setLotes(ArrayList<Lote> lotes) {
        this.lotes = lotes;
    }
}
