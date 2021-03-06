package traspasos.dominio;

import java.util.ArrayList;
import rechazos.to.TORechazoProductoAlmacen;

/**
 *
 * @author jesc
 */
public class TraspasoProductoReporte {
    private String sku;
    private String empaque;
    private double cantFacturada;
    private double cantSinCargo;
    private int piezas;
    private double unitario;
    private String lote;
    private double loteCantidad;
    private ArrayList<TORechazoProductoAlmacen> lotes;
    
    public TraspasoProductoReporte() {
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

    public double getCantSinCargo() {
        return cantSinCargo;
    }

    public void setCantSinCargo(double cantSinCargo) {
        this.cantSinCargo = cantSinCargo;
    }

    public int getPiezas() {
        return piezas;
    }

    public void setPiezas(int piezas) {
        this.piezas = piezas;
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
