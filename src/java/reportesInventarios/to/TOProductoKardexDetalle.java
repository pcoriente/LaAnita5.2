package reportesInventarios.to;

import java.util.Date;

/**
 *
 * @author jesc
 */
public class TOProductoKardexDetalle {
    private Date fecha;
    private String tipo;
    private int folio;
    private String comprobante;
    private String lote;
    private double existenciaAnterior;
    private String operacion;
    private double cantidad;
    private double saldo;
    private double costoPromedio;
    
    public TOProductoKardexDetalle() {
        this.fecha = new Date();
        this.comprobante = "";
        this.operacion = "";
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getFolio() {
        return folio;
    }

    public void setFolio(int folio) {
        this.folio = folio;
    }

    public String getComprobante() {
        return comprobante;
    }

    public void setComprobante(String comprobante) {
        this.comprobante = comprobante;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public double getExistenciaAnterior() {
        return existenciaAnterior;
    }

    public void setExistenciaAnterior(double existenciaAnterior) {
        this.existenciaAnterior = existenciaAnterior;
    }

    public String getOperacion() {
        return operacion;
    }

    public void setOperacion(String operacion) {
        this.operacion = operacion;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }
    
    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public double getCostoPromedio() {
        return costoPromedio;
    }

    public void setCostoPromedio(double costoPromedio) {
        this.costoPromedio = costoPromedio;
    }
}
