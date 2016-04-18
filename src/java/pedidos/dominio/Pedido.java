package pedidos.dominio;

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
public class Pedido extends MovimientoOficina {
    private int idEnvio;
    private boolean agregado;
    private double peso;
    private int orden;
    private int orden2;
    private int envioEstatus;
    
    private int idPedido;
    private TOTienda tienda;
    private Comprobante comprobante;
    private int pedidoFolio;
    private Date pedidoFecha;
    private int diasCredito;
    private boolean especial;
    private int pedidoIdUsuario;
    private String canceladoMotivo;
    private boolean directo;
    private int idSolicitud;
    private int pedidoEstatus;
    private String electronico;
    private String ordenDeCompra;
    private Date ordenDeCompraFecha;
    private String entregaFolio;
    private Date entregaFecha;
    private Date entregaFechaMaxima;
    private int idVenta;

    public Pedido() {
        super(new MovimientoTipo(28, "VENTA"));
        this.tienda=new TOTienda();
        this.comprobante=new Comprobante();
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
    
    public Pedido(TOAlmacenJS almacen, TOTienda tienda, Comprobante comprobante) {
        super(new MovimientoTipo(28, "VENTA"), almacen);
        this.tienda=tienda;
        this.comprobante=comprobante;
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
    
    public Pedido(TOAlmacenJS almacen, TOTienda tienda, Moneda moneda) {
        super(new MovimientoTipo(28, "VENTA"), almacen);
        this.tienda=tienda;
        this.comprobante=new Comprobante(super.getTipo().getIdTipo(), almacen.getIdEmpresa(), tienda.getIdTienda(), moneda);
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.idPedido;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pedido other = (Pedido) obj;
        if (this.idPedido != other.idPedido) {
            return false;
        }
        return true;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
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

    public boolean isEspecial() {
        return especial;
    }

    public void setEspecial(boolean especial) {
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

    public boolean isDirecto() {
        return directo;
    }

    public void setDirecto(boolean directo) {
        this.directo = directo;
    }

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public int getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(int idEnvio) {
        this.idEnvio = idEnvio;
    }

    public boolean isAgregado() {
        return agregado;
    }

    public void setAgregado(boolean agregado) {
        this.agregado = agregado;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public int getOrden2() {
        return orden2;
    }

    public void setOrden2(int orden2) {
        this.orden2 = orden2;
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

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }
}
