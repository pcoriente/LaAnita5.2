package pedidos.to;

import java.util.Date;

/**
 *
 * @author jesc
 */
public class TOPedido {
    private int idPedido;
    private int idMovto;
    private int idMovtoAlmacen;
    private int idPedidoOC;
    private int idEmpresa;
    private int idAlmacen;
    private int idTienda;
    private double desctoComercial;
    private double desctoProntoPago;
    private int idImpuestoZona;
    private Date fecha;
    private int estatus;
    private String ordenDeCompra;
    private Date ordenDeCompraFecha;
    private boolean cancelado;
    private Date canceladoFecha;
    private String canceladoMotivo;

    public TOPedido() {
        this.ordenDeCompra="";
        this.ordenDeCompraFecha=new Date();
        this.fecha=new Date();
        this.canceladoFecha=new Date();
        this.canceladoMotivo="";
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getIdMovto() {
        return idMovto;
    }

    public void setIdMovto(int idMovto) {
        this.idMovto = idMovto;
    }

    public int getIdMovtoAlmacen() {
        return idMovtoAlmacen;
    }

    public void setIdMovtoAlmacen(int idMovtoAlmacen) {
        this.idMovtoAlmacen = idMovtoAlmacen;
    }
    
    public int getIdPedidoOC() {
        return idPedidoOC;
    }

    public void setIdPedidoOC(int idPedidoOC) {
        this.idPedidoOC = idPedidoOC;
    }

    public int getIdTienda() {
        return idTienda;
    }

    public void setIdTienda(int idTienda) {
        this.idTienda = idTienda;
    }

    public double getDesctoComercial() {
        return desctoComercial;
    }

    public void setDesctoComercial(double desctoComercial) {
        this.desctoComercial = desctoComercial;
    }

    public double getDesctoProntoPago() {
        return desctoProntoPago;
    }

    public void setDesctoProntoPago(double desctoProntoPago) {
        this.desctoProntoPago = desctoProntoPago;
    }

    public int getIdImpuestoZona() {
        return idImpuestoZona;
    }

    public void setIdImpuestoZona(int idImpuestoZona) {
        this.idImpuestoZona = idImpuestoZona;
    }

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public int getIdAlmacen() {
        return idAlmacen;
    }

    public void setIdAlmacen(int idAlmacen) {
        this.idAlmacen = idAlmacen;
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

    public int getEstatus() {
        return estatus;
    }

    public void setEstatus(int estatus) {
        this.estatus = estatus;
    }

    public boolean isCancelado() {
        return cancelado;
    }

    public void setCancelado(boolean cancelado) {
        this.cancelado = cancelado;
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
