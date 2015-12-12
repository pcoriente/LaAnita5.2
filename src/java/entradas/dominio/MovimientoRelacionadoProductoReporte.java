package entradas.dominio;

import java.util.ArrayList;
import rechazos.to.TORechazoProductoAlmacen;

/**
 *
 * @author jesc
 */
public class MovimientoRelacionadoProductoReporte {
    private String sku;
    private String empaque;
    private double cantFacturada;
    private double unitario;
    private String lote;
    private double loteCantidad;
    private ArrayList<TORechazoProductoAlmacen> lotes;
    
    public MovimientoRelacionadoProductoReporte() {
        this.sku="";
        this.empaque="";
        this.lote="";
        this.lotes=new ArrayList<>();
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

    public double getCantFacturada() {
        return cantFacturada;
    }

    public void setCantFacturada(double cantFacturada) {
        this.cantFacturada = cantFacturada;
    }

    public double getUnitario() {
        return unitario;
    }

    public void setUnitario(double unitario) {
        this.unitario = unitario;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public double getLoteCantidad() {
        return loteCantidad;
    }

    public void setLoteCantidad(double loteCantidad) {
        this.loteCantidad = loteCantidad;
    }

    public ArrayList<TORechazoProductoAlmacen> getLotes() {
        return lotes;
    }

    public void setLotes(ArrayList<TORechazoProductoAlmacen> lotes) {
        this.lotes = lotes;
    }
}
