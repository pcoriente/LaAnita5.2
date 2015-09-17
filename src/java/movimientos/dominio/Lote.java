package movimientos.dominio;

import java.util.Date;

/**
 *
 * @author jesc
 */
public class Lote {
    private String lote;
    private double cantidad;
    private double separados;
    private Date fechaCaducidad;
    
    public Lote() {
        this.lote="";
        this.fechaCaducidad = new Date();
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public double getSeparados() {
        return separados;
    }

    public void setSeparados(double separados) {
        this.separados = separados;
    }

    public Date getFechaCaducidad() {
        return fechaCaducidad;
    }

    public void setFechaCaducidad(Date fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }
}
