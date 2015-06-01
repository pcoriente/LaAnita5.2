package pedidos.to;

import java.util.Date;

/**
 *
 * @author jesc
 */
public class TOPedido {
    private int idPedido;
    private int idEmpresa;
    private int idAlmacen;
    private int idTienda;
    private String ordenDeCompra;
    private Date ordenDeCompraFecha;
    private Date fecha;
    private int status;
    private Date cancelacionFecha;
    private String cancelacionMotivo;

    public TOPedido() {
        this.ordenDeCompra="";
        this.ordenDeCompraFecha=new Date();
        this.fecha=new Date();
        this.cancelacionFecha=new Date();
        this.cancelacionMotivo="";
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

    public int getIdTienda() {
        return idTienda;
    }

    public void setIdTienda(int idTienda) {
        this.idTienda = idTienda;
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
