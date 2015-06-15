package compras.dominio;

import almacenes.to.TOAlmacenJS;
import entradas.to.TOComprobante;
import java.util.Date;
import proveedores.dominio.MiniProveedor;

/**
 *
 * @author jesc
 */
public class CompraAlmacen {
    private int idCompra;
    private TOAlmacenJS almacen;
    private MiniProveedor proveedor;
    private TOComprobante comprobante;
    private int folio;
    private int idOrdenCompra;
    private Date fecha;
    private int idUsuario;
    private int estatus;
    
    public CompraAlmacen() {
        this.almacen=new TOAlmacenJS();
        this.proveedor=new MiniProveedor();
        this.comprobante=new TOComprobante();
        this.fecha=new Date();
    }
    
    public CompraAlmacen(TOAlmacenJS almacen, MiniProveedor proveedor, TOComprobante comprobante) {
        this.almacen=almacen;
        this.proveedor=proveedor;
        this.comprobante=comprobante;
        this.fecha=new Date();
    }

    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    public TOAlmacenJS getAlmacen() {
        return almacen;
    }

    public void setAlmacen(TOAlmacenJS almacen) {
        this.almacen = almacen;
    }

    public MiniProveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(MiniProveedor proveedor) {
        this.proveedor = proveedor;
    }

    public TOComprobante getComprobante() {
        return comprobante;
    }

    public void setComprobante(TOComprobante comprobante) {
        this.comprobante = comprobante;
    }

    public int getFolio() {
        return folio;
    }

    public void setFolio(int folio) {
        this.folio = folio;
    }

    public int getIdOrdenCompra() {
        return idOrdenCompra;
    }

    public void setIdOrdenCompra(int idOrdenCompra) {
        this.idOrdenCompra = idOrdenCompra;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getEstatus() {
        return estatus;
    }

    public void setEstatus(int estatus) {
        this.estatus = estatus;
    }
}
