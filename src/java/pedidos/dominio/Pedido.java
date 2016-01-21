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
    private TOTienda tienda;
    private Comprobante comprobante;
    private int idPedidoOC;
    private String ordenDeCompra;
    private Date ordenDeCompraFecha;
    private String canceladoMotivo;
    private Date canceladoFecha;
    private boolean especial;
    private String electronico;
    private int idPedido;

    public Pedido() {
        super(new MovimientoTipo(28, "VENTA"));
        this.tienda=new TOTienda();
        this.comprobante=new Comprobante();
        this.ordenDeCompra="";
        this.ordenDeCompraFecha=new Date();
        this.canceladoMotivo="";
        this.canceladoFecha=new Date();
        this.electronico="";
    }
    
    public Pedido(TOAlmacenJS almacen, TOTienda tienda, Comprobante comprobante) {
        super(new MovimientoTipo(28, "VENTA"), almacen);
        this.tienda=tienda;
        this.comprobante=comprobante;
        this.ordenDeCompra="";
        this.ordenDeCompraFecha=new Date();
        this.canceladoMotivo="";
        this.canceladoFecha=new Date();
        this.electronico="";
    }
    
    public Pedido(TOAlmacenJS almacen, TOTienda tienda, Moneda moneda) {
        super(new MovimientoTipo(28, "VENTA"), almacen);
        this.tienda=tienda;
        this.comprobante=new Comprobante(super.getTipo().getIdTipo(), almacen.getIdEmpresa(), tienda.getIdTienda(), moneda);
        this.ordenDeCompra="";
        this.ordenDeCompraFecha=new Date();
        this.canceladoMotivo="";
        this.canceladoFecha=new Date();
        this.electronico="";
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

    public int getIdPedidoOC() {
        return idPedidoOC;
    }

    public void setIdPedidoOC(int idPedidoOC) {
        this.idPedidoOC = idPedidoOC;
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

    public boolean isEspecial() {
        return especial;
    }

    public void setEspecial(boolean especial) {
        this.especial = especial;
    }

    public String getElectronico() {
        return electronico;
    }

    public void setElectronico(String electronico) {
        this.electronico = electronico;
    }
}
