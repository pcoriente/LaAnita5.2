package ventas.dominio;

import movimientos.dominio.ProductoOficina;
import producto2.dominio.Producto;

/**
 *
 * @author jesc
 */
public class VentaProducto extends ProductoOficina {
    private int idPedido;
    private double cantOrdenada;
    private double cantOrdenadaSinCargo;
    
    public VentaProducto() {
        super();
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }
    
    public VentaProducto(Producto producto) {
        super(producto);
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
}
