package movimientos.dominio;

import almacenes.to.TOAlmacenJS;
import java.util.Date;

/**
 *
 * @author jesc
 */
public class MovimientoRelacionado {
    private int idMovto;
    private TOAlmacenJS almacenOrigen;
    private int folio;
    private TOAlmacenJS almacenDestino;
    private Date fecha;
    private int idUsuario;
    private int estatus;
    private int idMovtoAlmacen;
    private int folioAlmacen;
    private double subTotal;
    private double descuento;
    private double impuesto;
    private double total;
    
    public MovimientoRelacionado() {
        this.almacenDestino=new TOAlmacenJS();
        this.almacenOrigen=new TOAlmacenJS();
        this.fecha=new Date();
    }

    public int getIdMovto() {
        return idMovto;
    }

    public void setIdMovto(int idMovto) {
        this.idMovto = idMovto;
    }

    public TOAlmacenJS getAlmacenDestino() {
        return almacenDestino;
    }

    public void setAlmacenDestino(TOAlmacenJS almacenDestino) {
        this.almacenDestino = almacenDestino;
    }

    public int getFolio() {
        return folio;
    }

    public void setFolio(int folio) {
        this.folio = folio;
    }

    public TOAlmacenJS getAlmacenOrigen() {
        return almacenOrigen;
    }

    public void setAlmacenOrigen(TOAlmacenJS almacenOrigen) {
        this.almacenOrigen = almacenOrigen;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getEstatus() {
        return estatus;
    }

    public void setEstatus(int estatus) {
        this.estatus = estatus;
    }

    public int getIdMovtoAlmacen() {
        return idMovtoAlmacen;
    }

    public void setIdMovtoAlmacen(int idMovtoAlmacen) {
        this.idMovtoAlmacen = idMovtoAlmacen;
    }

    public int getFolioAlmacen() {
        return folioAlmacen;
    }

    public void setFolioAlmacen(int folioAlmacen) {
        this.folioAlmacen = folioAlmacen;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }

    public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }

    public double getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(double impuesto) {
        this.impuesto = impuesto;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
