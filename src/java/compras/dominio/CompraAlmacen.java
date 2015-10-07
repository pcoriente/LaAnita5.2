package compras.dominio;

import almacenes.to.TOAlmacenJS;
import comprobantes.dominio.Comprobante;
import movimientos.dominio.MovimientoAlmacen;
import movimientos.dominio.MovimientoTipo;
import proveedores.dominio.MiniProveedor;

/**
 *
 * @author jesc
 */
public class CompraAlmacen extends MovimientoAlmacen {
    private Comprobante comprobante;
    private MiniProveedor proveedor;
    private int idOrdenCompra;
    
    public CompraAlmacen() {
        super(new MovimientoTipo(1, "COMPRA OFICINA"));
        this.proveedor=new MiniProveedor();
        this.comprobante=new Comprobante();
    }
    
    public CompraAlmacen(TOAlmacenJS almacen, MiniProveedor proveedor, Comprobante comprobante) {
        super(new MovimientoTipo(1, "COMPRA OFICINA"), almacen);
        this.proveedor=proveedor;
        this.comprobante=comprobante;
    }

    public MiniProveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(MiniProveedor proveedor) {
        this.proveedor = proveedor;
    }

    public Comprobante getComprobante() {
        return comprobante;
    }

    public void setComprobante(Comprobante comprobante) {
        this.comprobante = comprobante;
    }

    public int getIdOrdenCompra() {
        return idOrdenCompra;
    }

    public void setIdOrdenCompra(int idOrdenCompra) {
        this.idOrdenCompra = idOrdenCompra;
    }
}
