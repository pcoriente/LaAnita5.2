package ventas.to;

import movimientos.to.TOProductoOficina;

/**
 *
 * @author jesc
 */
public class TOVentaProducto extends TOProductoOficina {
    private int idPedido;
    private double cantOrdenada;
    private double cantOrdenadaSinCargo;
//    private boolean similar;
    
    public TOVentaProducto() {
        super();
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
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

//    public boolean isSimilar() {
//        return similar;
//    }
//
//    public void setSimilar(boolean similar) {
//        this.similar = similar;
//    }
}
