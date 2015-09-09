package movimientos.dominio;

import almacenes.to.TOAlmacenJS;
import java.util.Date;

/**
 *
 * @author jesc
 */
public class MovimientoOficina {

    private int idMovto;
    private int idTipo;
    private TOAlmacenJS almacen;
    private int folio;
    private double desctoComercial;
    private double desctoProntoPago;
    private Date fecha;
    private double tipoDeCambio;
    private int idUsuario;
    private int propietario;
    private int estatus;
    private int idMovtoAlmacen;
    
    private double subTotal;
    private double descuento;
    private double impuesto;
    private double total;

    public MovimientoOficina() {
        this.almacen = new TOAlmacenJS();
        this.fecha = new Date();
        this.tipoDeCambio = 1;
    }
    
    public MovimientoOficina(int idTipo) {
        this.idTipo = idTipo;
        this.almacen = new TOAlmacenJS();
        this.fecha = new Date();
        this.tipoDeCambio = 1;
    }

    public MovimientoOficina(TOAlmacenJS almacen) {
        this.almacen = almacen;
        this.fecha = new Date();
        this.tipoDeCambio = 1;
    }
    
    public MovimientoOficina(int idTipo, TOAlmacenJS almacen) {
        this.idTipo = idTipo;
        this.almacen = almacen;
        this.fecha = new Date();
        this.tipoDeCambio = 1;
    }

    public int getIdMovto() {
        return idMovto;
    }

    public void setIdMovto(int idMovto) {
        this.idMovto = idMovto;
    }

    public int getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(int idTipo) {
        this.idTipo = idTipo;
    }

    public TOAlmacenJS getAlmacen() {
        return almacen;
    }

    public void setAlmacen(TOAlmacenJS almacen) {
        this.almacen = almacen;
    }

    public int getFolio() {
        return folio;
    }

    public void setFolio(int folio) {
        this.folio = folio;
    }

//    public int getIdImpuestoZona() {
//        return idImpuestoZona;
//    }
//
//    public void setIdImpuestoZona(int idImpuestoZona) {
//        this.idImpuestoZona = idImpuestoZona;
//    }
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

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public double getTipoDeCambio() {
        return tipoDeCambio;
    }

    public void setTipoDeCambio(double tipoDeCambio) {
        this.tipoDeCambio = tipoDeCambio;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getPropietario() {
        return propietario;
    }

    public void setPropietario(int propietario) {
        this.propietario = propietario;
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
