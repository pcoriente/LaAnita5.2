package compras.dominio;

import almacenes.to.TOAlmacenJS;
import entradas.to.TOComprobante;
import java.util.Date;
import monedas.Moneda;
import proveedores.dominio.MiniProveedor;

/**
 *
 * @author jsolis
 */
public class CompraOficina {
    private int idEntrada;
    private TOAlmacenJS almacen;
    private MiniProveedor proveedor;
    private TOComprobante comprobante;
    private int folio;
//    private int idImpuestoZona;
    private int idOrdenCompra;
    private Moneda moneda;
    private double tipoDeCambio;
    private double desctoComercial;
    private double desctoProntoPago;
    private Date fecha;
    private int idUsuario;
    private int estatus;
    private double subTotal;
    private double descuento;
    private double impuesto;
    private double total;

    public CompraOficina() {
//        this.idEntrada=0;
//        this.folio=0;
        this.almacen=new TOAlmacenJS();
        this.proveedor=new MiniProveedor();
        this.comprobante=new TOComprobante();
//        this.idImpuestoZona=0;
//        this.idOrdenCompra=0;
        this.moneda=new Moneda();
        this.tipoDeCambio=1.00;
//        this.desctoComercial=0.00;
//        this.desctoProntoPago=0.00;
        this.fecha=new Date();
//        this.idUsuario=0;
//        this.subTotal=0.00;
//        this.descuento=0.00;
//        this.impuesto=0.00;
//        this.total=0.00;
    }
    
    public CompraOficina(TOAlmacenJS almacen, MiniProveedor proveedor, TOComprobante comprobante) {
        this.almacen=almacen;
        this.proveedor=proveedor;
        this.comprobante=comprobante;
        this.moneda=new Moneda();
        this.tipoDeCambio=1;
        this.fecha=new Date();
    }

    public int getIdEntrada() {
        return idEntrada;
    }

    public void setIdEntrada(int idEntrada) {
        this.idEntrada = idEntrada;
    }

    public TOAlmacenJS getAlmacen() {
        return almacen;
    }

    public void setAlmacen(TOAlmacenJS almacen) {
        this.almacen = almacen;
    }

    public MiniProveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(MiniProveedor proveedor) {
        this.proveedor = proveedor;
    }

    public TOComprobante getComprobante() {
        return comprobante;
    }

    public void setComprobante(TOComprobante comprobante) {
        this.comprobante = comprobante;
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

    public double getTipoDeCambio() {
        return tipoDeCambio;
    }

    public void setTipoDeCambio(double tipoDeCambio) {
        this.tipoDeCambio = tipoDeCambio;
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

    public int getEstatus() {
        return estatus;
    }

    public void setEstatus(int estatus) {
        this.estatus = estatus;
    }
}
