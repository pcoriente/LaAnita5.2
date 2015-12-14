package entradas.dominio;

import java.util.ArrayList;
import movimientos.to.TOMovimientoProductoAlmacen;

/**
 *
 * @author jesc
 */
public class ProductoReporteAlmacen {
    private int folio;
    private int idOrdenCompra;
    private String sku;
    private String empaque;
    private double cantidad;
    private String lote;
    private double loteCantidad;
    private ArrayList<TOMovimientoProductoAlmacen> lotes;
    
    public ProductoReporteAlmacen() {
        this.sku="";
        this.empaque="";
        this.lotes=new ArrayList<>();
    }

    public int getFolio() {
        return folio;
    }

    public void setFolio(int folio) {
        this.folio = folio;
    }

    public int getIdOrdenCompra() {
        return idOrdenCompra;
    }

    public void setIdOrdenCompra(int idOrdenCompra) {
        this.idOrdenCompra = idOrdenCompra;
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

    public ArrayList<TOMovimientoProductoAlmacen> getLotes() {
        return lotes;
    }

    public void setLotes(ArrayList<TOMovimientoProductoAlmacen> lotes) {
        this.lotes = lotes;
    }
}
