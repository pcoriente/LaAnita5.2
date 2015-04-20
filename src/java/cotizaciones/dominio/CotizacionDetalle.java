package cotizaciones.dominio;

import java.io.Serializable;
import producto2.dominio.Producto;
import proveedores.dominio.Proveedor;

public class CotizacionDetalle implements Serializable {

    //  private RequisicionDetalle requisicionDetalle;
    private CotizacionEncabezado cotizacionEncabezado;
    private Proveedor proveedor = new Proveedor();
    private int idCotizacion;  //??
    private int idRequisicion; //??
    private Producto producto;  //??
    private String sku;
    private double cantidadAutorizada;
    private double cantidadCotizada;
    private double costoCotizado;
    private double descuentoProducto;
    private double descuentoProducto2; //Solicitud del Dr.
    private double neto;
    private double subtotal;
    private double iva;
    private double total;
   
    public CotizacionEncabezado getCotizacionEncabezado() {
        return cotizacionEncabezado;
    }

    public void setCotizacionEncabezado(CotizacionEncabezado cotizacionEncabezado) {
        this.cotizacionEncabezado = cotizacionEncabezado;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public double getCantidadAutorizada() {
        return cantidadAutorizada;
    }

    public void setCantidadAutorizada(double cantidadAutorizada) {
        this.cantidadAutorizada = cantidadAutorizada;
    }

    public double getCantidadCotizada() {
        return cantidadCotizada;
    }

    public void setCantidadCotizada(double cantidadCotizada) {
        this.cantidadCotizada = cantidadCotizada;
    }

    public double getCostoCotizado() {
        return costoCotizado;
    }

    public void setCostoCotizado(double costoCotizado) {
        this.costoCotizado = costoCotizado;
    }

    public double getDescuentoProducto() {
        return descuentoProducto;
    }

    public void setDescuentoProducto(double descuentoProducto) {
        this.descuentoProducto = descuentoProducto;
    }

    public double getDescuentoProducto2() {
        return descuentoProducto2;
    }

    public void setDescuentoProducto2(double descuentoProducto2) {
        this.descuentoProducto2 = descuentoProducto2;
    }

    public double getNeto() {
        return neto;
    }

    public void setNeto(double neto) {
        this.neto = neto;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getIva() {
        return iva;
    }

    public void setIva(double iva) {
        this.iva = iva;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public int getIdCotizacion() {
        return idCotizacion;
    }

    public void setIdCotizacion(int idCotizacion) {
        this.idCotizacion = idCotizacion;
    }

    public int getIdRequisicion() {
        return idRequisicion;
    }

    public void setIdRequisicion(int idRequisicion) {
        this.idRequisicion = idRequisicion;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }
}
