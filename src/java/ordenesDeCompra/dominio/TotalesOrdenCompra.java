/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ordenesDeCompra.dominio;

/**
 *
 * @author comod_000
 */
public class TotalesOrdenCompra {
    private String subtoF;
    private String sumaDescuentosGeneralesF;
    private String sumaDescuentsoProductosF;
    private String sumaDescuentosTotalesF;
    private String subTotalBrutoF;
    private String impF;
    private String totalF;
    private String Letras;

    
//    ------------------------------------------
    
     private double subtotalGeneral;
    private double descuentoGeneralAplicado;
    private double sumaDescuentosProductos;
    private double sumaDescuentoTotales;
    private double subtotalBruto;
    private double impuesto;
    private double total;
    
    
    public String getLetras() {
        return Letras;
    }

    public void setLetras(String Letras) {
        this.Letras = Letras;
    }
    

    public String getSubtoF() {
        return subtoF;
    }

    public void setSubtoF(String subtoF) {
        this.subtoF = subtoF;
    }

    public String getSumaDescuentosGeneralesF() {
        return sumaDescuentosGeneralesF;
    }

    public void setSumaDescuentosGeneralesF(String sumaDescuentosGeneralesF) {
        this.sumaDescuentosGeneralesF = sumaDescuentosGeneralesF;
    }

    public String getSumaDescuentsoProductosF() {
        return sumaDescuentsoProductosF;
    }

    public void setSumaDescuentsoProductosF(String sumaDescuentsoProductosF) {
        this.sumaDescuentsoProductosF = sumaDescuentsoProductosF;
    }

    public String getSumaDescuentosTotalesF() {
        return sumaDescuentosTotalesF;
    }

    public void setSumaDescuentosTotalesF(String sumaDescuentosTotalesF) {
        this.sumaDescuentosTotalesF = sumaDescuentosTotalesF;
    }

    public String getSubTotalBrutoF() {
        return subTotalBrutoF;
    }

    public void setSubTotalBrutoF(String subTotalBrutoF) {
        this.subTotalBrutoF = subTotalBrutoF;
    }

    public String getImpF() {
        return impF;
    }

    public void setImpF(String impF) {
        this.impF = impF;
    }

    public String getTotalF() {
        return totalF;
    }

    public void setTotalF(String totalF) {
        this.totalF = totalF;
    }

    public double getSubtotalGeneral() {
        return subtotalGeneral;
    }

    public void setSubtotalGeneral(double subtotalGeneral) {
        this.subtotalGeneral = subtotalGeneral;
    }

    public double getDescuentoGeneralAplicado() {
        return descuentoGeneralAplicado;
    }

    public void setDescuentoGeneralAplicado(double descuentoGeneralAplicado) {
        this.descuentoGeneralAplicado = descuentoGeneralAplicado;
    }

    public double getSumaDescuentosProductos() {
        return sumaDescuentosProductos;
    }

    public void setSumaDescuentosProductos(double sumaDescuentosProductos) {
        this.sumaDescuentosProductos = sumaDescuentosProductos;
    }

    public double getSumaDescuentoTotales() {
        return sumaDescuentoTotales;
    }

    public void setSumaDescuentoTotales(double sumaDescuentoTotales) {
        this.sumaDescuentoTotales = sumaDescuentoTotales;
    }

    public double getSubtotalBruto() {
        return subtotalBruto;
    }

    public void setSubtotalBruto(double subtotalBruto) {
        this.subtotalBruto = subtotalBruto;
    }

    public double getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(double impuesto) {
        this.impuesto = impuesto;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
    
    
    
    
}
