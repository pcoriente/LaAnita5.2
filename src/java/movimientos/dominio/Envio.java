package movimientos.dominio;

import almacenes.to.TOAlmacenJS;
import java.util.Date;

/**
 *
 * @author jesc
 */
public class Envio {
    private int idMovto;
    private int idMovtoAlmacen;
    private TOAlmacenJS almacen;
    private int folio;
//    private TOComprobante comprobante;
//    private int idImpuestoZona;
//    private Moneda moneda;
//    private double tipoCambio;
//    private double desctoComercial;
//    private double desctoProntoPago;
    private Date fecha;
    private int idUsuario;
    private TOAlmacenJS almacenDestino;
    private int folioAlmacen;
    private double subTotal;
    private double descuento;
    private double impuesto;
    private double total;
    
    public Envio() {
        this.almacen=new TOAlmacenJS();
//        this.comprobante=new TOComprobante();
//        this.moneda=new Moneda();
//        this.tipoCambio=1;
        this.fecha=new Date();
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

//    public TOComprobante getComprobante() {
//        return comprobante;
//    }
//
//    public void setComprobante(TOComprobante comprobante) {
//        this.comprobante = comprobante;
//    }

//    public int getIdImpuestoZona() {
//        return idImpuestoZona;
//    }
//
//    public void setIdImpuestoZona(int idImpuestoZona) {
//        this.idImpuestoZona = idImpuestoZona;
//    }
//
//    public Moneda getMoneda() {
//        return moneda;
//    }
//
//    public void setMoneda(Moneda moneda) {
//        this.moneda = moneda;
//    }
//
//    public double getTipoCambio() {
//        return tipoCambio;
//    }
//
//    public void setTipoCambio(double tipoCambio) {
//        this.tipoCambio = tipoCambio;
//    }
//
//    public double getDesctoComercial() {
//        return desctoComercial;
//    }
//
//    public void setDesctoComercial(double desctoComercial) {
//        this.desctoComercial = desctoComercial;
//    }
//
//    public double getDesctoProntoPago() {
//        return desctoProntoPago;
//    }
//
//    public void setDesctoProntoPago(double desctoProntoPago) {
//        this.desctoProntoPago = desctoProntoPago;
//    }

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

    public TOAlmacenJS getAlmacenDestino() {
        return almacenDestino;
    }

    public void setAlmacenDestino(TOAlmacenJS almacenDestino) {
        this.almacenDestino = almacenDestino;
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
