package entradas.dominio;

import almacenes.dominio.AlmacenJS;
import java.util.Date;
import monedas.Moneda;

/**
 *
 * @author jsolis
 */
public class Entrada {
    private int idEntrada;
    private AlmacenJS almacen;
    private int folio;
    private Comprobante comprobante;
    private int idImpuestoZona;
    private int idOrdenCompra;
    private Moneda moneda;
    private double tipoCambio;
    private double desctoComercial;
    private double desctoProntoPago;
    private Date fecha;
    private int idUsuario;
    private double subTotal;
    private double descuento;
    private double impuesto;
    private double total;

    public Entrada() {
        this.idEntrada=0;
        this.folio=0;
        this.almacen=new AlmacenJS();
        this.comprobante=new Comprobante();
        this.idImpuestoZona=0;
        this.idOrdenCompra=0;
        this.moneda=new Moneda();
        this.tipoCambio=1.00;
        this.desctoComercial=0.00;
        this.desctoProntoPago=0.00;
        this.fecha=new Date();
        this.idUsuario=0;
        this.subTotal=0.00;
        this.descuento=0.00;
        this.impuesto=0.00;
        this.total=0.00;
    }

    public int getIdEntrada() {
        return idEntrada;
    }

    public void setIdEntrada(int idEntrada) {
        this.idEntrada = idEntrada;
    }

    public Comprobante getComprobante() {
        return comprobante;
    }

    public void setComprobante(Comprobante comprobante) {
        this.comprobante = comprobante;
    }

    public int getIdImpuestoZona() {
        return idImpuestoZona;
    }

    public void setIdImpuestoZona(int idImpuestoZona) {
        this.idImpuestoZona = idImpuestoZona;
    }

    public int getIdOrdenCompra() {
        return idOrdenCompra;
    }

    public void setIdOrdenCompra(int idOrdenCompra) {
        this.idOrdenCompra = idOrdenCompra;
    }

    public Moneda getMoneda() {
        return moneda;
    }

    public void setMoneda(Moneda moneda) {
        this.moneda = moneda;
    }

    public double getTipoCambio() {
        return tipoCambio;
    }

    public void setTipoCambio(double tipoCambio) {
        this.tipoCambio = tipoCambio;
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

    public int getFolio() {
        return folio;
    }

    public void setFolio(int folio) {
        this.folio = folio;
    }

    public AlmacenJS getAlmacen() {
        return almacen;
    }

    public void setAlmacen(AlmacenJS almacen) {
        this.almacen = almacen;
    }
}
