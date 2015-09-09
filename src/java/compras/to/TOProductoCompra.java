package compras.to;

import movimientos.to.TOProductoOficina;

/**
 *
 * @author jesc
 */
public class TOProductoCompra extends TOProductoOficina {
    private double cantOrdenada;
    private double cantOrdenadaSinCargo;
    private double costoOrdenado;
    private String cantOrdenadaTotal;
//    private double cantRecibida;
//    private double cantRecibidaSinCargo;
//    private double neto;
    
    public TOProductoCompra() {
        super();
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

    public double getCostoOrdenado() {
        return costoOrdenado;
    }

    public void setCostoOrdenado(double costoOrdenado) {
        this.costoOrdenado = costoOrdenado;
    }

    public String getCantOrdenadaTotal() {
        return cantOrdenadaTotal;
    }

    public void setCantOrdenadaTotal(String cantOrdenadaTotal) {
        this.cantOrdenadaTotal = cantOrdenadaTotal;
    }

//    public double getCantRecibida() {
//        return cantRecibida;
//    }
//
//    public void setCantRecibida(double cantRecibida) {
//        this.cantRecibida = cantRecibida;
//    }
//
//    public double getCantRecibidaSinCargo() {
//        return cantRecibidaSinCargo;
//    }
//
//    public void setCantRecibidaSinCargo(double cantRecibidaSinCargo) {
//        this.cantRecibidaSinCargo = cantRecibidaSinCargo;
//    }

//    public double getNeto() {
//        return neto;
//    }
//
//    public void setNeto(double neto) {
//        this.neto = neto;
//    }
}
