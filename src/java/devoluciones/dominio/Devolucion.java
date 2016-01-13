package devoluciones.dominio;

import almacenes.to.TOAlmacenJS;
import comprobantes.dominio.Comprobante;
import movimientos.dominio.MovimientoOficina;
import movimientos.dominio.MovimientoTipo;
import tiendas.to.TOTienda;

/**
 *
 * @author jesc
 */
public class Devolucion extends MovimientoOficina {
    private TOTienda tienda;
    private Comprobante comprobante;
    private int idMovtoVenta;
    
    public Devolucion() {
        super(new MovimientoTipo(2, "Devolucion"));
        this.tienda = new TOTienda();
        this.comprobante = new Comprobante();
    }
    
    public Devolucion(TOAlmacenJS almacen, TOTienda tienda, Comprobante comprobante) {
        super(new MovimientoTipo(2, "Devolucion"), almacen);
        this.tienda = tienda;
        this.comprobante = comprobante;
    }

    public TOTienda getTienda() {
        return tienda;
    }

    public void setTienda(TOTienda tienda) {
        this.tienda = tienda;
    }

    public Comprobante getComprobante() {
        return comprobante;
    }

    public void setComprobante(Comprobante comprobante) {
        this.comprobante = comprobante;
    }

    public int getIdMovtoVenta() {
        return idMovtoVenta;
    }

    public void setIdMovtoVenta(int idMovtoVenta) {
        this.idMovtoVenta = idMovtoVenta;
    }
}
