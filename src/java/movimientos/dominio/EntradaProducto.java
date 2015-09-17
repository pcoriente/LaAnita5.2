package movimientos.dominio;

import movimientos.to1.Lote1;
import entradas.dominio.MovimientoProducto;
import java.util.ArrayList;

/**
 *
 * @author jesc
 */
public class EntradaProducto extends MovimientoProducto {
    ArrayList<Lote1> lotes;
    
    public EntradaProducto() {
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
