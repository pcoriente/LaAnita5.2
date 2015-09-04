package movimientos.to;

/**
 *
 * @author jesc
 */
public class TOMovimientoAlmacen extends TOMovimiento {
    private int idMovtoAlmacen;
    
    public TOMovimientoAlmacen() {
        super();
    }
    
    public TOMovimientoAlmacen(int idTipo) {
        super(idTipo);
    }

    public int getIdMovtoAlmacen() {
        return idMovtoAlmacen;
    }

    public void setIdMovtoAlmacen(int idMovtoAlmacen) {
        this.idMovtoAlmacen = idMovtoAlmacen;
    }
}
