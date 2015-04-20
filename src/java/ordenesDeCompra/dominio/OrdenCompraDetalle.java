package ordenesDeCompra.dominio;

import cotizaciones.dominio.CotizacionDetalle;
import java.io.Serializable;
import producto2.dominio.Producto;

public class OrdenCompraDetalle implements Serializable {

 //   private Empaque producto;
    private Producto producto;
    private CotizacionDetalle cotizacionDetalle;
    private int idOrdenCompra;
    private int idEmpaque;
//    private String sku;
    private double cantOrdenada;
    private double cantidadSolicitada;
    private double cantRecibida;
    private double costoOrdenado;
    private double descuentoProducto;
    private double descuentoProducto2;
    private double desctoConfidencial;
    private int sinCargoBase;
    private int sinCargoCant;
    private double ptjeOferte;
    private double margen;
    private int IdImpuestosGrupo;
    private int idMarca;
    private double neto;
    private double subtotal;
    private String netoF;
    private String subtotalF;
    private String costoOrdenadoF;
    private String nombreProducto;
   

    public OrdenCompraDetalle() {
    }

    public OrdenCompraDetalle(Producto producto, CotizacionDetalle cotizacionDetalle, int idOrdenCompra, int idEmpaque, String sku, double cantOrdenada, double cantidadSolicitada, double costoOrdenado, double descuentoProducto, double descuentoProducto2, double desctoConfidencial, int sinCargoBase, int sinCargoCant, double ptjeOferte, double margen, int IdImpuestosGrupo, int idMarca, double neto, double subtotal) {
     //   this.producto = producto;
        this.producto=producto;
        this.cotizacionDetalle = cotizacionDetalle;
        this.idOrdenCompra = 0;
        this.idEmpaque = 0;
//        this.sku = "";
        this.cantOrdenada = 0.00;
        this.cantidadSolicitada=0.00;
        this.cantRecibida=0;
        this.costoOrdenado = 0.00;
        this.descuentoProducto = 0.00;
        this.descuentoProducto2 = 0.00;
        this.desctoConfidencial = 0.00;
        this.sinCargoBase = 0;
        this.sinCargoCant = 0;
        this.ptjeOferte = 0.00;
        this.margen = 0.00;
        this.IdImpuestosGrupo = 0;
        this.idMarca = 0;
        this.neto=0;
        this.subtotal=0;
        
    }

//   

    public CotizacionDetalle getCotizacionDetalle() {
        return cotizacionDetalle;
    }

    public void setCotizacionDetalle(CotizacionDetalle cotizacionDetalle) {
        this.cotizacionDetalle = cotizacionDetalle;
    }

    public int getIdOrdenCompra() {
        return idOrdenCompra;
    }

    public void setIdOrdenCompra(int idOrdenCompra) {
        this.idOrdenCompra = idOrdenCompra;
    }

    public int getIdEmpaque() {
        return idEmpaque;
    }

    public void setIdEmpaque(int idEmpaque) {
        this.idEmpaque = idEmpaque;
    }

//    public String getSku() {
//        return sku;
//    }
//
//    public void setSku(String sku) {
//        this.sku = sku;
//    }

    public double getCantOrdenada() {
        return cantOrdenada;
    }

    public void setCantOrdenada(double cantOrdenada) {
        this.cantOrdenada = cantOrdenada;
    }

    public double getCantidadSolicitada() {
        return cantidadSolicitada;
    }

    public void setCantidadSolicitada(double cantidadSolicitada) {
        this.cantidadSolicitada = cantidadSolicitada;
    }
    
    public double getCostoOrdenado() {
        return costoOrdenado;
    }

    public void setCostoOrdenado(double costoOrdenado) {
        this.costoOrdenado = costoOrdenado;
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

    public double getDesctoConfidencial() {
        return desctoConfidencial;
    }

    public void setDesctoConfidencial(double desctoConfidencial) {
        this.desctoConfidencial = desctoConfidencial;
    }

    public int getSinCargoBase() {
        return sinCargoBase;
    }

    public void setSinCargoBase(int sinCargoBase) {
        this.sinCargoBase = sinCargoBase;
    }

    public int getSinCargoCant() {
        return sinCargoCant;
    }

    public void setSinCargoCant(int sinCargoCant) {
        this.sinCargoCant = sinCargoCant;
    }

    public double getPtjeOferte() {
        return ptjeOferte;
    }

    public void setPtjeOferte(double ptjeOferte) {
        this.ptjeOferte = ptjeOferte;
    }

    public double getMargen() {
        return margen;
    }

    public void setMargen(double margen) {
        this.margen = margen;
    }

    public int getIdImpuestosGrupo() {
        return IdImpuestosGrupo;
    }

    public void setIdImpuestosGrupo(int IdImpuestosGrupo) {
        this.IdImpuestosGrupo = IdImpuestosGrupo;
    }

    public int getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(int idMarca) {
        this.idMarca = idMarca;
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

   
    
    public String getCostoOrdenadoF() {
        costoOrdenadoF=   utilerias.Utilerias.formatoMonedas(this.getCostoOrdenado());
        return costoOrdenadoF;
    }

    public void setCostoOrdenadoF(String costoOrdenadoF) {
        this.costoOrdenadoF = costoOrdenadoF;
    }

    public String getNetoF() {
        netoF = utilerias.Utilerias.formatoMonedas(this.getNeto());
        return netoF;
    }

    public String getSubtotalF() {
         subtotalF = utilerias.Utilerias.formatoMonedas(this.subtotal);
        return subtotalF;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public double getCantRecibida() {
        return cantRecibida;
    }

    public void setCantRecibida(double cantRecibida) {
        this.cantRecibida = cantRecibida;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }
}
