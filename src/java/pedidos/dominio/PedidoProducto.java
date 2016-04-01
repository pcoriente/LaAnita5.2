package pedidos.dominio;

import movimientos.dominio.ProductoOficina;
import producto2.dominio.Producto;

/**
 *
 * @author jesc
 */
public class PedidoProducto extends ProductoOficina {
    private int idEnvio;
    private double cantEnviada;
    private double cantEnviada2;
    private int idVenta;
    private double cajas;
    private double cajasSinCargo;
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

    public int getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(int idEnvio) {
        this.idEnvio = idEnvio;
    }

    public double getCantEnviada() {
        return cantEnviada;
    }

    public void setCantEnviada(double cantEnviada) {
        this.cantEnviada = cantEnviada;
    }

    public double getCantEnviada2() {
        return cantEnviada2;
    }

    public void setCantEnviada2(double cantEnviada2) {
        this.cantEnviada2 = cantEnviada2;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public double getCajas() {
        return cajas;
    }

    public void setCajas(double cajas) {
        this.cajas = cajas;
    }

    public double getCajasSinCargo() {
        return cajasSinCargo;
    }

    public void setCajasSinCargo(double cajasSinCargo) {
        this.cajasSinCargo = cajasSinCargo;
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
