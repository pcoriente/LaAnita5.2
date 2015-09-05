package compras.dominio;

import movimientos.dominio.ProductoOficina;

/**
 *
 * @author jesc
 */
public class ProductoCompra extends ProductoOficina {
    private double cantOrdenada;
    private double cantOrdenadaSinCargo;
    private String cantOrdenadaTotal;
//    private double neto;
    
    public ProductoCompra() {
        super();
        this.cantOrdenadaTotal = "";
    }

    public double getCantOrdenada() {
        return cantOrdenada;
    }

    public void setCantOrdenada(double cantOrdenada) {
        this.cantOrdenada = cantOrdenada;
    }

    public double getCantOrdenadaSinCargo() {
        return cantOrdenadaSinCargo;
    }

    public void setCantOrdenadaSinCargo(double cantOrdenadaSinCargo) {
        this.cantOrdenadaSinCargo = cantOrdenadaSinCargo;
    }

    public String getCantOrdenadaTotal() {
        return cantOrdenadaTotal;
    }

    public void setCantOrdenadaTotal(String cantOrdenadaTotal) {
        this.cantOrdenadaTotal = cantOrdenadaTotal;
    }

//    public double getNeto() {
//        return neto;
//    }
//
//    public void setNeto(double neto) {
//        this.neto = neto;
//    }
}
