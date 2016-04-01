package ventas.to;

import java.util.Date;
import movimientos.to.TOMovimientoOficina;

/**
 *
 * @author jesc
 */
public class TOVenta extends TOMovimientoOficina {
    private int idPedidoOC;
    private int pedidoFolio;
    private Date pedidoFecha;
    private int diasCredito;
    private int especial;
    private int pedidoIdUsuario;
    private String canceladoMotivo;
    private int directo;
    private double peso;
    private int idEnvio;
    private int orden;
    private int pedidoEstatus;
    private int envioEstatus;
    private String electronico;
    private String ordenDeCompra;
    private Date ordenDeCompraFecha;
    private String entregaFolio;
    private Date entregaFecha;
    private Date entregaFechaMaxima;
    
    public TOVenta() {
        super();
        this.pedidoFecha=new Date();
        this.canceladoMotivo="";
//        this.canceladoFecha=new Date();
        this.electronico="";
        this.ordenDeCompra="";
        this.ordenDeCompraFecha=new Date();
        this.entregaFolio = "";
        this.entregaFecha = new Date();
        this.entregaFechaMaxima = new Date();
    }
    
    public TOVenta(int idTipo) {
        super(idTipo);
        this.pedidoFecha=new Date();
        this.canceladoMotivo="";
//        this.canceladoFecha=new Date();
        this.electronico="";
        this.ordenDeCompra="";
        this.ordenDeCompraFecha=new Date();
        this.entregaFolio = "";
        this.entregaFecha = new Date();
        this.entregaFechaMaxima = new Date();
        
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

    public String getCanceladoMotivo() {
        return canceladoMotivo;
    }

    public void setCanceladoMotivo(String canceladoMotivo) {
        this.canceladoMotivo = canceladoMotivo;
    }

//    public Date getCanceladoFecha() {
//        return canceladoFecha;
//    }
//
//    public void setCanceladoFecha(Date canceladoFecha) {
//        this.canceladoFecha = canceladoFecha;
//    }
//
    public int getPedidoEstatus() {
        return pedidoEstatus;
    }

    public void setPedidoEstatus(int pedidoEstatus) {
        this.pedidoEstatus = pedidoEstatus;
    }

    public String getElectronico() {
        return electronico;
    }

    public void setElectronico(String electronico) {
        this.electronico = electronico;
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

    public int getDirecto() {
        return directo;
    }

    public void setDirecto(int directo) {
        this.directo = directo;
    }

    public int getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(int idEnvio) {
        this.idEnvio = idEnvio;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public int getEnvioEstatus() {
        return envioEstatus;
    }

    public void setEnvioEstatus(int envioEstatus) {
        this.envioEstatus = envioEstatus;
    }

    public String getEntregaFolio() {
        return entregaFolio;
    }

    public void setEntregaFolio(String entregaFolio) {
        this.entregaFolio = entregaFolio;
    }

    public Date getEntregaFecha() {
        return entregaFecha;
    }

    public void setEntregaFecha(Date entregaFecha) {
        this.entregaFecha = entregaFecha;
    }

    public Date getEntregaFechaMaxima() {
        return entregaFechaMaxima;
    }

    public void setEntregaFechaMaxima(Date entregaFechaMaxima) {
        this.entregaFechaMaxima = entregaFechaMaxima;
    }
}
