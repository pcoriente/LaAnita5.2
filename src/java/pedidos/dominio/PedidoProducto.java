package pedidos.dominio;

import movimientos.dominio.ProductoOficina;
import producto2.dominio.Producto;

/**
 *
 * @author jesc
 */
public class PedidoProducto extends ProductoOficina {
    private int idPedido;
    private double cantOrdenada;
    private double cantOrdenadaSinCargo;
    private boolean similar;
//    private double cantOrdenadaTotal;

    public PedidoProducto() {
        super();
    }

    public PedidoProducto(Producto producto) {
        super(producto);
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

//    public double getCantOrdenadaTotal() {
//        return cantOrdenadaTotal;
//    }
//
//    public void setCantOrdenadaTotal(double cantOrdenadaTotal) {
//        this.cantOrdenadaTotal = cantOrdenadaTotal;
//    }

    public boolean isSimilar() {
        return similar;
    }

    public void setSimilar(boolean similar) {
        this.similar = similar;
    }
}
