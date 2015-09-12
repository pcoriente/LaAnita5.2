package movimientos.to;

import java.util.ArrayList;
import movimientos.dominio.Lote;

/**
 *
 * @author jesc
 */
public class TOProductoLotes {
    private int idMovtoAlmacen;
    private int idProducto;
    private ArrayList<Lote> lotes;
    
    public TOProductoLotes() {
        this.lotes=new ArrayList<>();
    }

    public int getIdMovtoAlmacen() {
        return idMovtoAlmacen;
    }

    public void setIdMovtoAlmacen(int idMovtoAlmacen) {
        this.idMovtoAlmacen = idMovtoAlmacen;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public ArrayList<Lote> getLotes() {
        return lotes;
    }

    public void setLotes(ArrayList<Lote> lotes) {
        this.lotes = lotes;
    }
}
