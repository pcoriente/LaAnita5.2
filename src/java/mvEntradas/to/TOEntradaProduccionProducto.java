package mvEntradas.to;

import java.util.ArrayList;
import movimientos.to.TOMovimientoProductoAlmacen;
import movimientos.to.TOProductoOficina;

/**
 *
 * @author jesc
 */
public class TOEntradaProduccionProducto extends TOProductoOficina {
    ArrayList<TOMovimientoProductoAlmacen> lotes;
    
    public TOEntradaProduccionProducto() {
        super();
        this.lotes = new ArrayList<>();
    }

    public ArrayList<TOMovimientoProductoAlmacen> getLotes() {
        return lotes;
    }

    public void setLotes(ArrayList<TOMovimientoProductoAlmacen> lotes) {
        this.lotes = lotes;
    }
}
