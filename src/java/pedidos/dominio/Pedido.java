package pedidos.dominio;

import clientes.to.TOCliente;
import formatos.dominio.ClienteFormato;
import java.util.Date;
import tiendas.to.TOTienda;

/**
 *
 * @author jesc
 */
public class Pedido {
    private int idPedido;
    private int idEmpresa;
    private int idAlmacen;
    private TOTienda tienda;
    private ClienteFormato formato;
    private TOCliente cliente;
    private String ordenDeCompra;
    private Date ordenDeCompraFecha;
    private Date fecha;
    private int status;
    private Date cancelacionFecha;
    private String cancelacionMotivo;
    private double cantArticulos;

    public Pedido() {
        this.tienda=new TOTienda();
        this.formato=new ClienteFormato();
        this.cliente=new TOCliente();
        this.ordenDeCompra="";
        this.ordenDeCompraFecha=new Date();
        this.fecha=new Date();
        this.cancelacionFecha=new Date();
        this.cancelacionMotivo="";
    }

    public Pedido(int idAlmacen, TOTienda tienda, ClienteFormato formato, TOCliente cliente) {
        this.idAlmacen = idAlmacen;
        this.tienda = tienda;
        this.formato = formato;
        this.cliente = cliente;
        this.ordenDeCompra="";
        this.ordenDeCompraFecha=new Date();
        this.cancelacionMotivo="";
        this.cancelacionFecha=new Date();
    }

    public double getCantArticulos() {
        return cantArticulos;
    }

    public void setCantArticulos(double cantArticulos) {
        this.cantArticulos = cantArticulos;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getIdAlmacen() {
        return idAlmacen;
    }

    public void setIdAlmacen(int idAlmacen) {
        this.idAlmacen = idAlmacen;
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

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCancelacionFecha() {
        return cancelacionFecha;
    }

    public void setCancelacionFecha(Date cancelacionFecha) {
        this.cancelacionFecha = cancelacionFecha;
    }

    public String getCancelacionMotivo() {
        return cancelacionMotivo;
    }

    public void setCancelacionMotivo(String cancelacionMotivo) {
        this.cancelacionMotivo = cancelacionMotivo;
    }

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }
}
