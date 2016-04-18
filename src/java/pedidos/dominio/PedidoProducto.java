package pedidos.dominio;

import movimientos.dominio.ProductoOficina;
import producto2.dominio.Producto;

/**
 *
 * @author jesc
 */
public class PedidoProducto extends ProductoOficina {
    private int idEnvio;
    private int idVenta;
    private double enviar;
    private double enviar2;
    private double enviarSinCargo;
    private double enviarSinCargo2;
    private double cantEnviar;
    private double cantEnviarSinCargo;
    private double cantOrdenada;
    private double cantOrdenadaSinCargo;
    private boolean similar;

    public PedidoProducto() {
        super();
    }

    public PedidoProducto(Producto producto) {
        super(producto);
    }

    public int getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(int idEnvio) {
        this.idEnvio = idEnvio;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public double getEnviar() {
        return enviar;
    }

    public void setEnviar(double enviar) {
        this.enviar = enviar;
    }

    public double getEnviar2() {
        return enviar2;
    }

    public void setEnviar2(double enviar2) {
        this.enviar2 = enviar2;
    }

    public double getEnviarSinCargo() {
        return enviarSinCargo;
    }

    public void setEnviarSinCargo(double enviarSinCargo) {
        this.enviarSinCargo = enviarSinCargo;
    }

    public double getEnviarSinCargo2() {
        return enviarSinCargo2;
    }

    public void setEnviarSinCargo2(double enviarSinCargo2) {
        this.enviarSinCargo2 = enviarSinCargo2;
    }

    public double getCantEnviar() {
        return cantEnviar;
    }

    public void setCantEnviar(double cantEnviar) {
        this.cantEnviar = cantEnviar;
    }

    public double getCantEnviarSinCargo() {
        return cantEnviarSinCargo;
    }

    public void setCantEnviarSinCargo(double cantEnviarSinCargo) {
        this.cantEnviarSinCargo = cantEnviarSinCargo;
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

    public boolean isSimilar() {
        return similar;
    }

    public void setSimilar(boolean similar) {
        this.similar = similar;
    }
}
