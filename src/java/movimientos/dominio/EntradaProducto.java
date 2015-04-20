package movimientos.dominio;

import entradas.dominio.MovimientoProducto;
import java.util.ArrayList;

/**
 *
 * @author jesc
 */
public class EntradaProducto extends MovimientoProducto {
    ArrayList<Lote> lotes;
    
    public EntradaProducto() {
        super();
        this.lotes=new ArrayList<Lote>();
    }

    public ArrayList<Lote> getLotes() {
        return lotes;
    }

    public void setLotes(ArrayList<Lote> lotes) {
        this.lotes = lotes;
    }
}
