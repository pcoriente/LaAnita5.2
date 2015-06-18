package ordenesDeCompra;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import javax.naming.NamingException;
import ordenesDeCompra.dao.DAOOrdenDeCompra;
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
    private double subtotalBruto;
    private double impuesto;
    private double total;
    private double iva = 0.16;
    private double sumaDescuentoTotales; // TODOS LOS DESCUENTOS
    private double descuentoGeneralAplicado;
    private int NumCotizacion;
    private double dc;
    private double dpp;

    //INYECCIONES
    ArrayList<OrdenCompraDetalle> ordenCompraDetallesDirectas;
    OrdenCompraDetalle ordenCompraDetalleSeleccionado;

    //CONSTRUCTOR
    public MbCalculoRCOD() throws NamingException {
        this.ordenCompraDetallesDirectas = new ArrayList<>();
        this.ordenCompraDetalleSeleccionado = new OrdenCompraDetalle();
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

    public double getSubtotalBruto() {
        // this.calcularSubtotalBruto();
        return subtotalBruto;
    }

    public void setSubtotalBruto(double subtotalBruto) {
        this.subtotalBruto = subtotalBruto;
    }

    public double getIva() {
        return iva;
    }

    public void setIva(double iva) {
        this.iva = iva;
    }

    public int getNumCotizacion() {
        return NumCotizacion;
    }

    public void setNumCotizacion(int NumCotizacion) {
        this.NumCotizacion = NumCotizacion;
    }

    ///// METODOS NECESARIOS//
    public double getSubtotalGeneral() {
        return subtotalGeneral;
    }

    public void setSubtotalGeneral(double subtotalGeneral) {
        this.subtotalGeneral = subtotalGeneral;
    }

    public double getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(double impuesto) {
        this.impuesto = impuesto;
    }

    public double getSumaDescuentosProductos() {

        return sumaDescuentosProductos;
    }

    public void setSumaDescuentosProductos(double sumaDescuentosProductos) {
        this.sumaDescuentosProductos = sumaDescuentosProductos;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getSumaDescuentoTotales() {
        return sumaDescuentoTotales;
    }

    public void setSumaDescuentoTotales(double sumaDescuentoTotales) {
        this.sumaDescuentoTotales = sumaDescuentoTotales;
    }

    public double getDescuentoGeneralAplicado() {
        return descuentoGeneralAplicado;
    }

    public void setDescuentoGeneralAplicado(double descuentoGeneralAplicado) {
        this.descuentoGeneralAplicado = descuentoGeneralAplicado;
    }

    public void pasoParametros(int idEmp, ArrayList<OrdenCompraDetalle> ordenCompraDetallesDirectas, double dc, double dpp) {
        //PARAMETROS QUE SE ENVIARON DEL XHTML
        if (idEmp != 0 || ordenCompraDetallesDirectas != null) {
            this.calculaPrecioDescuento(idEmp, ordenCompraDetallesDirectas);

        }
        this.setDc(dc);
        this.setDpp(dpp);

    }

    public void calculaPrecioDescuento(int idEmp, ArrayList<OrdenCompraDetalle> ordenCompraDetallesDirectas) {
//        System.out.println("entr");
        subtotalGeneral = 0;

        try {
            for (OrdenCompraDetalle e : ordenCompraDetallesDirectas) {
                if (e.getProducto().getIdProducto() == idEmp) {
                    double neto = e.getCostoOrdenado() - (e.getCostoOrdenado() * (e.getDescuentoProducto() / 100));
                    double neto2 = neto - neto * (e.getDescuentoProducto2() / 100);
                    e.setNeto(neto2);
//                    e.setSubtotal(e.getNeto() * e.getCantOrdenada());
                    e.setSubtotal(e.getCostoOrdenado() * e.getCantOrdenada());
                    break;
                }

            }
        } catch (Exception e) {
            Logger.getLogger(MbRequisiciones.class.getName()).log(Level.SEVERE, null, e);
        }
        this.setOrdenCompraDetallesDirectas(ordenCompraDetallesDirectas);
    }

    public double calculoSubtotalGeneral() {   //Importe :
        subtotalGeneral = 0;
        for (OrdenCompraDetalle e : ordenCompraDetallesDirectas) {
            subtotalGeneral = subtotalGeneral + e.getSubtotal();
        }
        this.setSubtotalGeneral(subtotalGeneral);
        return subtotalGeneral;
    }

    public double calculoDescuentoGeneral() {  //Comercial+Pronto Pago:

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
        return descuentoGA;
    }

    public double calculoDescuentoProducto() {
        sumaDescuentosProductos = 0;
        for (OrdenCompraDetalle e : ordenCompraDetallesDirectas) {
            sumaDescuentosProductos += (e.getCantOrdenada() * (e.getCostoOrdenado() - e.getNeto()));
        }
        this.setSumaDescuentosProductos(sumaDescuentosProductos);
        return sumaDescuentosProductos;
    }

    public double calculoDescuentoTotales() {
        sumaDescuentoTotales = 0;
        sumaDescuentoTotales = this.getSumaDescuentosProductos() + this.getDescuentoGeneralAplicado();
        this.setSumaDescuentoTotales(sumaDescuentoTotales);
        return sumaDescuentoTotales;
    }

    public double calculoSubtotalBruto() {  //Subtotal :
        subtotalBruto = 0.0;
//        subtotalBruto = this.getSubtotalGeneral() - this.getDescuentoGeneralAplicado();
        subtotalBruto = this.getSubtotalGeneral() - this.getSumaDescuentoTotales();
        this.setSubtotalBruto(subtotalBruto); //
        return subtotalBruto;
    }

    public double calculoIVA() {  //Impuestos :
        impuesto = 0;
        double desc = this.subtotalBruto;                           //subtotalGeneral - descuentoGeneralAplicado;
        if (desc > 0) {
            impuesto = (desc) * this.iva;
        } else {
            impuesto = 0;
        }
        this.setImpuesto(impuesto);
        return impuesto;
    }

    public double calculoTotal() { //Total: 
        total = 0;
        double desc = this.subtotalBruto;                                           //subtotalGeneral - descuentoGeneralAplicado;
        if (desc > 0) {
            total = (desc) + this.getImpuesto();
        } else {
            total = 0;
        }
        this.setTotal(total);
        return total;
    }

    

    public void eliminaProducto() {
        ordenCompraDetallesDirectas.remove(ordenCompraDetalleSeleccionado);
        System.out.print("hola... se elimino" + ordenCompraDetalleSeleccionado.getIdOrdenCompra());
    }

    public void guardarOrdenCompra() throws NamingException {
        //   int longOC = 0;
     
        DAOOrdenDeCompra daoOCD = new DAOOrdenDeCompra();
        if (this.total != 0) {
//            daoOCD.guardarOrdenCompraDirecta(this.ordenCompraEncabezadoDirecta,ordenCompraDetallesDirectas);
           
        }


    }

    public String salirMenuCotizaciones() throws NamingException {

        String salir = "menuRequisiciones.xhtml";
        return salir;
    }

    
}
