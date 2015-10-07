package movimientos.dominio;

import almacenes.to.TOAlmacenJS;

/**
 *
 * @author jesc
 */
public class MovimientoOficina extends MovimientoAlmacen {
    private int idMovto;
    private double desctoComercial;
    private double desctoProntoPago;
    private double tipoDeCambio;
    private double subTotal;
    private double descuento;
    private double impuesto;
    private double total;

    public MovimientoOficina() {
        super();
        this.tipoDeCambio = 1;
    }

    public MovimientoOficina(MovimientoTipo tipo) {
        super(tipo);
        this.tipoDeCambio = 1;
    }

    public MovimientoOficina(MovimientoTipo tipo, TOAlmacenJS almacen) {
        super(tipo, almacen);
        this.tipoDeCambio = 1;
    }

    public int getIdMovto() {
        return idMovto;
    }

    public void setIdMovto(int idMovto) {
        this.idMovto = idMovto;
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

    public double getTipoDeCambio() {
        return tipoDeCambio;
    }

    public void setTipoDeCambio(double tipoDeCambio) {
        this.tipoDeCambio = tipoDeCambio;
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
