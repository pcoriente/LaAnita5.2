package compras.dominio;

import almacenes.to.TOAlmacenJS;
import entradas.dominio.Comprobante;
import movimientos.dominio.MovimientoAlmacen;
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
        super(1);
        this.proveedor=new MiniProveedor();
        this.comprobante=new Comprobante();
    }
    
    public CompraAlmacen(TOAlmacenJS almacen, MiniProveedor proveedor, Comprobante comprobante) {
        super(1, almacen);
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
