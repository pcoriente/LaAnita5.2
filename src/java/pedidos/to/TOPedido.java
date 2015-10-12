package pedidos.to;

import java.util.Date;
import movimientos.to.TOMovimientoOficina;

/**
 *
 * @author jesc
 */
public class TOPedido extends TOMovimientoOficina {
    private int idPedidoOC;
    private String ordenDeCompra;
    private Date ordenDeCompraFecha;
    private Date canceladoFecha;
    private String canceladoMotivo;

    public TOPedido() {
        super();
        this.ordenDeCompra="";
        this.ordenDeCompraFecha=new Date();
        this.canceladoFecha=new Date();
        this.canceladoMotivo="";
    }
    
    public TOPedido(int idTipo) {
        super(idTipo);
        this.ordenDeCompra="";
        this.ordenDeCompraFecha=new Date();
        this.canceladoFecha=new Date();
        this.canceladoMotivo="";
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

    public Date getCanceladoFecha() {
        return canceladoFecha;
    }

    public void setCanceladoFecha(Date canceladoFecha) {
        this.canceladoFecha = canceladoFecha;
    }

    public String getCanceladoMotivo() {
        return canceladoMotivo;
    }

    public void setCanceladoMotivo(String canceladoMotivo) {
        this.canceladoMotivo = canceladoMotivo;
    }
}
