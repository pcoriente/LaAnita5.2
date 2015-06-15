package entradas.dominio;

import java.util.ArrayList;
import movimientos.dominio.Lote;

/**
 *
 * @author jesc
 */
public class MovimientoAlmacenProductoReporte {
    private String sku;
    private String empaque;
    private double cantidad;
    private ArrayList<Lote> lotes;
    
    public MovimientoAlmacenProductoReporte() {
        this.sku="";
        this.empaque="";
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getEmpaque() {
        return empaque;
    }

    public void setEmpaque(String empaque) {
        this.empaque = empaque;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public ArrayList<Lote> getLotes() {
        return lotes;
    }

    public void setLotes(ArrayList<Lote> lotes) {
        this.lotes = lotes;
    }
}
