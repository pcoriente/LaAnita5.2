package mvEntradas.dominio;

import java.util.ArrayList;
import movimientos.dominio.ProductoOficina;
import movimientos.to.TOMovimientoProductoAlmacen;
import producto2.dominio.Producto;

/**
 *
 * @author jesc
 */
public class EntradaProduccionProducto extends ProductoOficina {
    ArrayList<TOMovimientoProductoAlmacen> lotes;
    
    public EntradaProduccionProducto() {
        super();
        this.lotes = new ArrayList<>();
    }
    
    public EntradaProduccionProducto(Producto producto) {
        super(producto);
        this.lotes = new ArrayList<>();
    }

    public ArrayList<TOMovimientoProductoAlmacen> getLotes() {
        return lotes;
    }

    public void setLotes(ArrayList<TOMovimientoProductoAlmacen> lotes) {
        this.lotes = lotes;
    }
}
