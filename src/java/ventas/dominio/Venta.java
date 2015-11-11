package ventas.dominio;

import almacenes.to.TOAlmacenJS;
import clientes.to.TOCliente;
import comprobantes.dominio.Comprobante;
import formatos.dominio.ClienteFormato;
import java.util.Date;
import movimientos.dominio.MovimientoOficina;
import movimientos.dominio.MovimientoTipo;
import tiendas.to.TOTienda;

/**
 *
 * @author jesc
 */
public class Venta extends MovimientoOficina {
    private int idPedidoOC;
    private String ordenDeCompra;
    private Date ordenDeCompraFecha;
    private String canceladoMotivo;
    private Date canceladoFecha;
    private Comprobante comprobante;
    private TOTienda tienda;
    private ClienteFormato formato;
    private TOCliente cliente;
    private int idPedido;
    
    public Venta() {
        super(new MovimientoTipo(28, "VENTA"));
        this.ordenDeCompra="";
        this.ordenDeCompraFecha=new Date();
        this.canceladoMotivo="";
        this.canceladoFecha=new Date();
        this.comprobante=new Comprobante();
        this.tienda=new TOTienda();
        this.formato=new ClienteFormato();
        this.cliente=new TOCliente();
    }
    
    public Venta(TOAlmacenJS almacen, TOTienda tienda) {
        super(new MovimientoTipo(28, "VENTA"), almacen);
        this.ordenDeCompra="";
        this.ordenDeCompraFecha=new Date();
        this.canceladoMotivo="";
        this.canceladoFecha=new Date();
        this.comprobante=new Comprobante();
        this.tienda=tienda;
        this.formato=new ClienteFormato();
        this.cliente=new TOCliente();
    }
    
    public Venta(TOAlmacenJS almacen, TOTienda tienda, ClienteFormato formato, TOCliente cliente) {
        super(new MovimientoTipo(28, "VENTA"), almacen);
        this.ordenDeCompra="";
        this.ordenDeCompraFecha=new Date();
        this.canceladoMotivo="";
        this.canceladoFecha=new Date();
        this.comprobante=new Comprobante();
        this.tienda=tienda;
        this.formato=formato;
        this.cliente=cliente;
    }
    
    public Venta(TOAlmacenJS almacen, Comprobante comprobante, TOTienda tienda) {
        super(new MovimientoTipo(28, "VENTA"), almacen);
        this.ordenDeCompra="";
        this.ordenDeCompraFecha=new Date();
        this.canceladoMotivo="";
        this.canceladoFecha=new Date();
        this.comprobante=comprobante;
        this.tienda=tienda;
        this.formato=new ClienteFormato();
        this.cliente=new TOCliente();
    }

    public int getIdPedidoOC() {
        return idPedidoOC;
    }

    public void setIdPedidoOC(int idPedidoOC) {
        this.idPedidoOC = idPedidoOC;
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

    public Comprobante getComprobante() {
        return comprobante;
    }

    public void setComprobante(Comprobante comprobante) {
        this.comprobante = comprobante;
    }

    public TOTienda getTienda() {
        return tienda;
    }

    public void setTienda(TOTienda tienda) {
        this.tienda = tienda;
    }

    public ClienteFormato getFormato() {
        return formato;
    }

    public void setFormato(ClienteFormato formato) {
        this.formato = formato;
    }

    public TOCliente getCliente() {
        return cliente;
    }

    public void setCliente(TOCliente cliente) {
        this.cliente = cliente;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }
}
