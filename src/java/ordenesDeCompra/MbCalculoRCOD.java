package ordenesDeCompra;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.naming.NamingException;
import ordenesDeCompra.dominio.OrdenCompraDetalle;
import requisiciones.mb.MbRequisiciones;

/**
 *
 * @author arias
 */
@Named(value = "mbCalculoRCOD")
@SessionScoped
public class MbCalculoRCOD implements Serializable {

    private double subtotalGeneral;
    private double sumaDescuentosProductos; //POR PRODUCTO
    // private double sumaDescuentosGenerales; // GENERALES
    private double subtotalBruto;
    private double impuesto;
    private double total;
    private String subtotF;
    private String descF;
    private String subtotalBrutoF;
    private String impF;
    private String totalF;
//    private String sumaDescuentosProductosF;
//    private String sumaDescuentosGeneralesF;
    private double iva = 0.16;
    private double sumaDescuentoTotales; // TODOS LOS DESCUENTOS
    private String sumaDescuentosTotalesF;
    private double descuentoGeneralAplicado;
    private int NumCotizacion;
    private double dc;
    private double dpp;
    //INYECCIONES
//    @ManagedProperty(value = "#{mbOrdenCompra}")
//    private MbOrdenCompra mbOrdenCompra;    
    ArrayList<OrdenCompraDetalle> ordenCompraDetallesDirectas;
    OrdenCompraDetalle ordenCompraDetalleSeleccionado;

    //CONSTRUCTOR
    public MbCalculoRCOD() throws NamingException {
        //     this.mbOrdenCompra = new MbOrdenCompra();
    }

    //GETS AND SETS
    public OrdenCompraDetalle getOrdenCompraDetalleSeleccionado() {
        return ordenCompraDetalleSeleccionado;
    }

    public void setOrdenCompraDetalleSeleccionado(OrdenCompraDetalle ordenCompraDetalleSeleccionado) {
        this.ordenCompraDetalleSeleccionado = ordenCompraDetalleSeleccionado;
    }

    public double getDc() {
        return dc;
    }

    public void setDc(double dc) {
        this.dc = dc;
    }

    public double getDpp() {
        return dpp;
    }

    public void setDpp(double dpp) {
        this.dpp = dpp;
    }

    public ArrayList<OrdenCompraDetalle> getOrdenCompraDetallesDirectas() {
        return ordenCompraDetallesDirectas;
    }

    public void setOrdenCompraDetallesDirectas(ArrayList<OrdenCompraDetalle> ordenCompraDetallesDirectas) {
        this.ordenCompraDetallesDirectas = ordenCompraDetallesDirectas;
    }

//    public double getSumaDescuentosGenerales() {
//        return sumaDescuentosGenerales;
//    }
//
//    public void setSumaDescuentosGenerales(double sumaDescuentosGenerales) {
//        this.sumaDescuentosGenerales = sumaDescuentosGenerales;
//    }
    public double getSubtotalBruto() {
        this.calcularSubtotalBruto();
        return subtotalBruto;
    }

    public void setSubtotalBruto(double subtotalBruto) {
        this.subtotalBruto = subtotalBruto;
    }

    public String getSubtotF() {
        return subtotF;
    }

    public void setSubtotF(String subtotF) {
        this.subtotF = subtotF;
    }

    public String getDescF() {
        return descF;
    }

    public void setDescF(String descF) {
        this.descF = descF;
    }

    public String getSubtotalBrutoF() {
        subtotalBrutoF = utilerias.Utilerias.formatoMonedas(this.getSubtotalBruto());
        return subtotalBrutoF;
    }

    public void setSubtotalBrutoF(String subtotalBrutoF) {
        this.subtotalBrutoF = subtotalBrutoF;
    }

    public String getImpF() {
        impF = utilerias.Utilerias.formatoMonedas(this.getImpuesto());
        return impF;
    }

    public void setImpF(String impF) {
        this.impF = impF;
    }

    public String getTotalF() {
        totalF = utilerias.Utilerias.formatoMonedas(this.getTotal());
        return totalF;
    }

    public void setTotalF(String totalF) {
        this.totalF = totalF;
    }

//    public String getSumaDescuentosProductosF() {
//        return sumaDescuentosProductosF;
//    }
//
//    public void setSumaDescuentosProductosF(String sumaDescuentosProductosF) {
//        this.sumaDescuentosProductosF = sumaDescuentosProductosF;
//    }
//
//    public String getSumaDescuentosGeneralesF() {
//        return sumaDescuentosGeneralesF;
//    }
//
//    public void setSumaDescuentosGeneralesF(String sumaDescuentosGeneralesF) {
//        this.sumaDescuentosGeneralesF = sumaDescuentosGeneralesF;
//    }
    public double getIva() {

        return iva;
    }

    public void setIva(double iva) {
        this.iva = iva;
    }

    public String getSumaDescuentosTotalesF() {
        sumaDescuentosTotalesF = utilerias.Utilerias.formatoMonedas(this.getSumaDescuentoTotales());
        return sumaDescuentosTotalesF;
    }

    public void setSumaDescuentosTotalesF(String sumaDescuentosTotalesF) {
        this.sumaDescuentosTotalesF = sumaDescuentosTotalesF;
    }

    public int getNumCotizacion() {
        return NumCotizacion;
    }

    public void setNumCotizacion(int NumCotizacion) {
        this.NumCotizacion = NumCotizacion;
    }

    ///// METODOS NECESARIOS//
    public double getSubtotalGeneral() {
        this.calculoSubtotalGeneral();
        return subtotalGeneral;
    }

    public void setSubtotalGeneral(double subtotalGeneral) {
        this.subtotalGeneral = subtotalGeneral;
    }

    public double getImpuesto() {
        this.calculoIVA();
        return impuesto;
    }

    public void setImpuesto(double impuesto) {
        this.impuesto = impuesto;
    }

    public double getSumaDescuentosProductos() {

        this.calculoDescuentoProducto();
        return sumaDescuentosProductos;
    }

    public void setSumaDescuentosProductos(double sumaDescuentosProductos) {
        this.sumaDescuentosProductos = sumaDescuentosProductos;
    }

    public double getTotal() {
        this.calculoTotal();
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getSumaDescuentoTotales() {

        this.calculoDescuentoTotales();

        return sumaDescuentoTotales;
    }

    public void setSumaDescuentoTotales(double sumaDescuentoTotales) {
        this.sumaDescuentoTotales = sumaDescuentoTotales;
    }

    public double getDescuentoGeneralAplicado() {
        this.calculoDescuentoGeneral();
        return descuentoGeneralAplicado;
    }

    public void setDescuentoGeneralAplicado(double descuentoGeneralAplicado) {
        this.descuentoGeneralAplicado = descuentoGeneralAplicado;
    }

    ///..........................//
    public void calculaPrecioDescuento(int idEmp, ArrayList<OrdenCompraDetalle> ordenCompraDetallesDirectas, double dc, double dpp) {
        System.out.println("entr");
        subtotalGeneral = 0;

        try {
            for (OrdenCompraDetalle e : ordenCompraDetallesDirectas) {
                if (e.getProducto().getIdProducto() == idEmp) {
                    double neto = e.getCostoOrdenado() - (e.getCostoOrdenado() * (e.getDescuentoProducto() / 100));
                    double neto2 = neto - neto * (e.getDescuentoProducto2() / 100);
                    e.setNeto(neto2);
                    e.setSubtotal(e.getNeto() * e.getCantOrdenada());
                    break;
                }

            }
        } catch (Exception e) {
            Logger.getLogger(MbRequisiciones.class.getName()).log(Level.SEVERE, null, e);
        }
        this.setOrdenCompraDetallesDirectas(ordenCompraDetallesDirectas);
        this.setDc(dc);
        this.setDpp(dpp);
    }

    public void calculoDescuentoProducto() {
        sumaDescuentosProductos = 0;
        for (OrdenCompraDetalle e : ordenCompraDetallesDirectas) {
            sumaDescuentosProductos += (e.getCantOrdenada() * (e.getCostoOrdenado() - e.getNeto()));
        }
    }

    public void calculoDescuentoTotales() {
        sumaDescuentoTotales = 0;
        sumaDescuentoTotales = this.getSumaDescuentosProductos() + this.getDescuentoGeneralAplicado();
    }
    
    public void calculoDescuentoGeneral() {  //Comercial+Pronto Pago:

        descuentoGeneralAplicado = 0;
        double sumaCostoCotizado = 0;
        double descuentoC;
        double descuentoPP;
        double descuentoGA;
        for (OrdenCompraDetalle e : ordenCompraDetallesDirectas) {
            sumaCostoCotizado += (e.getCantOrdenada() * e.getNeto());
        }
        descuentoC = sumaCostoCotizado * (dc / 100);
        sumaCostoCotizado = sumaCostoCotizado - descuentoC;
        descuentoPP = sumaCostoCotizado * (dpp / 100);
        descuentoGA = descuentoC + descuentoPP;
        this.setDescuentoGeneralAplicado(descuentoGA);
    }
    
    public void calculoSubtotalGeneral() {   //Importe :
        subtotalGeneral = 0;
        for (OrdenCompraDetalle e : ordenCompraDetallesDirectas) {
            subtotalGeneral = subtotalGeneral + e.getSubtotal();
        }
       // this.setSubtotalGeneral(subtotalGeneral);

    }
    
    public void calcularSubtotalBruto() {  //Subtotal :
        subtotalBruto = 0.0;
        subtotalBruto = this.getSubtotalGeneral() - this.getDescuentoGeneralAplicado();
        this.setSubtotalBruto(subtotalBruto); //
    }

    public void calculoIVA() {  //Impuestos :
        impuesto = 0;
        double desc = this.subtotalBruto;                           //subtotalGeneral - descuentoGeneralAplicado;
        if (desc > 0) {
            impuesto = (desc) * this.iva;
        } else {
            impuesto = 0;
        }
       this.setImpuesto(impuesto);
    }

    public void calculoTotal() { //Total: 
        total = 0;
        double desc = this.subtotalBruto;                                           //subtotalGeneral - descuentoGeneralAplicado;
        if (desc > 0) {
            total = (desc) + this.getImpuesto();
        } else {
            total = 0;
        }
        this.setTotal(total);
    }

    public void limpiaOrdenCompraDirecta() throws NamingException {
        for (OrdenCompraDetalle d : ordenCompraDetallesDirectas) {
            //    d.setCantidadCotizada(0);
            d.setCostoOrdenado(0);
            d.setDescuentoProducto(0);
            d.setDescuentoProducto2(0);
            d.setNeto(0);
            d.setSubtotal(0);
        }
        this.subtotalGeneral = 0.00;
        this.sumaDescuentosProductos = 0.00;
        this.descuentoGeneralAplicado = 0.00;
        this.sumaDescuentoTotales = 0.00;
        this.subtotalBruto = 0.00;
        this.impuesto = 0.00;
        this.total = 0.00;
        ordenCompraDetallesDirectas.clear();
    }

    public void eliminaProducto() {
        ordenCompraDetallesDirectas.remove(ordenCompraDetalleSeleccionado);
        System.out.print("hola... se elimino" + ordenCompraDetalleSeleccionado.getIdOrdenCompra());
    }
}
