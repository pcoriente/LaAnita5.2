package ventas.dominio;

import almacenes.to.TOAlmacenJS;
import comprobantes.dominio.Comprobante;
import java.util.Date;
import monedas.Moneda;
import movimientos.dominio.MovimientoOficina;
import movimientos.dominio.MovimientoTipo;
import tiendas.to.TOTienda;

/**
 *
 * @author jesc
 */
public class Venta extends MovimientoOficina {
    private TOTienda tienda;
    private Comprobante comprobante;
    private int idPedidoOC;
    private int idMoneda;
    private String ordenDeCompra;
    private Date ordenDeCompraFecha;
    private String canceladoMotivo;
    private Date canceladoFecha;
    private int idPedido;
    
    public Venta() {
        super(new MovimientoTipo(28, "VENTA"));
        this.tienda=new TOTienda();
        this.comprobante=new Comprobante();
        this.ordenDeCompra="";
        this.ordenDeCompraFecha=new Date();
        this.canceladoMotivo="";
        this.canceladoFecha=new Date();
    }
    
    public Venta(TOAlmacenJS almacen, TOTienda tienda, Comprobante comprobante) {
        super(new MovimientoTipo(28, "VENTA"), almacen);
        this.tienda=tienda;
        this.comprobante=comprobante;
        this.ordenDeCompra="";
        this.ordenDeCompraFecha=new Date();
        this.canceladoMotivo="";
        this.canceladoFecha=new Date();
    }
    
    public Venta(TOAlmacenJS almacen, TOTienda tienda, Moneda moneda) {
        super(new MovimientoTipo(28, "VENTA"), almacen);
        this.tienda=tienda;
        this.comprobante=new Comprobante(super.getTipo().getIdTipo(), almacen.getIdEmpresa(), tienda.getIdTienda(), moneda);
        this.ordenDeCompra="";
        this.ordenDeCompraFecha=new Date();
        this.canceladoMotivo="";
        this.canceladoFecha=new Date();
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

    public int getIdPedidoOC() {
        return idPedidoOC;
    }

    public void setIdPedidoOC(int idPedidoOC) {
        this.idPedidoOC = idPedidoOC;
    }

    public int getIdMoneda() {
        return idMoneda;
    }

    public void setIdMoneda(int idMoneda) {
        this.idMoneda = idMoneda;
    }

    public String getOrdenDeCompra() {
        return ordenDeCompra;
    }

    public void setOrdenDeCompra(String ordenDeCompra) {
        this.ordenDeCompra = ordenDeCompra;
    }

    public Date getOrdenDeCompraFecha() {
        return ordenDeCompraFecha;
    }

    public void setOrdenDeCompraFecha(Date ordenDeCompraFecha) {
        this.ordenDeCompraFecha = ordenDeCompraFecha;
    }

    public String getCanceladoMotivo() {
        return canceladoMotivo;
    }

    public void setCanceladoMotivo(String canceladoMotivo) {
        this.canceladoMotivo = canceladoMotivo;
    }

    public Date getCanceladoFecha() {
        return canceladoFecha;
    }

    public void setCanceladoFecha(Date canceladoFecha) {
        this.canceladoFecha = canceladoFecha;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }
}
