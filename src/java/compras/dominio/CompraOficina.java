package compras.dominio;

import almacenes.to.TOAlmacenJS;
import comprobantes.dominio.Comprobante;
import movimientos.dominio.MovimientoOficina;
import movimientos.dominio.MovimientoTipo;
import proveedores.dominio.MiniProveedor;

/**
 *
 * @author jsolis
 */
public class CompraOficina extends MovimientoOficina {
    private Comprobante comprobante;
    private MiniProveedor proveedor;
    private int idOrdenCompra;

    public CompraOficina() {
        super(new MovimientoTipo(1, "COMPRA OFICINA"));
        this.proveedor=new MiniProveedor();
        this.comprobante=new Comprobante();
    }
    
    public CompraOficina(TOAlmacenJS almacen, MiniProveedor proveedor, Comprobante comprobante) {
        super(new MovimientoTipo(1, "COMPRA OFICINA"), almacen);
        this.proveedor=proveedor;
        this.comprobante=comprobante;
    }

    public Comprobante getComprobante() {
        return comprobante;
    }

    public void setComprobante(Comprobante comprobante) {
        this.comprobante = comprobante;
    }

    public MiniProveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(MiniProveedor proveedor) {
        this.proveedor = proveedor;
    }

    public int getIdOrdenCompra() {
        return idOrdenCompra;
    }

    public void setIdOrdenCompra(int idOrdenCompra) {
        this.idOrdenCompra = idOrdenCompra;
    }
}
