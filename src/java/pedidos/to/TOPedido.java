package pedidos.to;

import java.util.Date;
import movimientos.to.TOMovimientoOficina;

/**
 *
 * @author jesc
 */
public class TOPedido extends TOMovimientoOficina {
    private int idPedidoOC;
    private int pedidoFolio;
    private Date pedidoFecha;
    private int diasCredito;
    private int especial;
    private int pedidoIdUsuario;
    private String canceladoMotivo;
//    private Date canceladoFecha;
    private int pedidoEstatus;
    private String electronico;
    private String ordenDeCompra;
    private Date ordenDeCompraFecha;

    public TOPedido() {
        super();
        this.pedidoFecha=new Date();
        this.ordenDeCompra="";
        this.ordenDeCompraFecha=new Date();
//        this.canceladoFecha=new Date();
        this.canceladoMotivo="";
        this.electronico="";
    }
    
    public TOPedido(int idTipo) {
        super(idTipo);
        this.pedidoFecha=new Date();
        this.ordenDeCompra="";
        this.ordenDeCompraFecha=new Date();
//        this.canceladoFecha=new Date();
        this.canceladoMotivo="";
        this.electronico="";
    }
    
    public int getIdPedidoOC() {
        return idPedidoOC;
    }

    public void setIdPedidoOC(int idPedidoOC) {
        this.idPedidoOC = idPedidoOC;
    }

    public int getPedidoFolio() {
        return pedidoFolio;
    }

    public void setPedidoFolio(int pedidoFolio) {
        this.pedidoFolio = pedidoFolio;
    }
    
    public Date getPedidoFecha() {
        return pedidoFecha;
    }

    public void setPedidoFecha(Date pedidoFecha) {
        this.pedidoFecha = pedidoFecha;
    }

    public int getDiasCredito() {
        return diasCredito;
    }

    public void setDiasCredito(int diasCredito) {
        this.diasCredito = diasCredito;
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

//    public Date getCanceladoFecha() {
//        return canceladoFecha;
//    }
//
//    public void setCanceladoFecha(Date canceladoFecha) {
//        this.canceladoFecha = canceladoFecha;
//    }

    public String getCanceladoMotivo() {
        return canceladoMotivo;
    }

    public void setCanceladoMotivo(String canceladoMotivo) {
        this.canceladoMotivo = canceladoMotivo;
    }

    public int getPedidoEstatus() {
        return pedidoEstatus;
    }

    public void setPedidoEstatus(int pedidoEstatus) {
        this.pedidoEstatus = pedidoEstatus;
    }

    public int getEspecial() {
        return especial;
    }

    public void setEspecial(int especial) {
        this.especial = especial;
    }

    public int getPedidoIdUsuario() {
        return pedidoIdUsuario;
    }

    public void setPedidoIdUsuario(int pedidoIdUsuario) {
        this.pedidoIdUsuario = pedidoIdUsuario;
    }

    public String getElectronico() {
        return electronico;
    }

    public void setElectronico(String electronico) {
        this.electronico = electronico;
    }
}
