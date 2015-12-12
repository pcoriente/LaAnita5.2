package movimientos.dominio;

import producto2.dominio.Producto;

/**
 *
 * @author jesc
 */
public class ProductoAlmacen {
    private int idMovtoAlmacen;
    private Producto producto;
    private String lote;
    private double cantidad;
    
    public ProductoAlmacen() {
        this.producto=new Producto();
        this.lote="";
    }
    
    public ProductoAlmacen(Producto producto) {
        this.producto = producto;
        this.lote = "";
    }
    
    public ProductoAlmacen(int idMovtoAlmacen, Producto producto) {
        this.idMovtoAlmacen = idMovtoAlmacen;
        this.producto = producto;
        this.lote = "";
    }

    public int getIdMovtoAlmacen() {
        return idMovtoAlmacen;
    }

    public void setIdMovtoAlmacen(int idMovtoAlmacen) {
        this.idMovtoAlmacen = idMovtoAlmacen;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
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
}
