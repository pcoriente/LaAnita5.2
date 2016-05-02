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
    private double enviar;          // Captura
//    private double enviar2;         // Captura
    private double enviarSinCargo;  // Captura
//    private double enviarSinCargo2; // Captura
//    private double cantEnviar;          // Grabar
//    private double cantEnviarSinCargo;  // Grabar
    private double ordenada;        // Captura
    private double ordenadaSinCargo;// Captura
//    private double cantOrdenada;        // Grabar
//    private double cantOrdenadaSinCargo;// Grabar
    private double respaldo;
    private double respaldoSinCargo;
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

//    public double getEnviar2() {
//        return enviar2;
//    }
//
//    public void setEnviar2(double enviar2) {
//        this.enviar2 = enviar2;
//    }
//
    public double getEnviarSinCargo() {
        return enviarSinCargo;
    }

    public void setEnviarSinCargo(double enviarSinCargo) {
        this.enviarSinCargo = enviarSinCargo;
    }

//    public double getEnviarSinCargo2() {
//        return enviarSinCargo2;
//    }
//
//    public void setEnviarSinCargo2(double enviarSinCargo2) {
//        this.enviarSinCargo2 = enviarSinCargo2;
//    }
//
//    public double getCantEnviar() {
//        return cantEnviar;
//    }
//
//    public void setCantEnviar(double cantEnviar) {
//        this.cantEnviar = cantEnviar;
//    }
//
//    public double getCantEnviarSinCargo() {
//        return cantEnviarSinCargo;
//    }
//
//    public void setCantEnviarSinCargo(double cantEnviarSinCargo) {
//        this.cantEnviarSinCargo = cantEnviarSinCargo;
//    }
//
    public double getOrdenada() {
        return ordenada;
    }

    public void setOrdenada(double ordenada) {
        this.ordenada = ordenada;
    }

    public double getOrdenadaSinCargo() {
        return ordenadaSinCargo;
    }

    public void setOrdenadaSinCargo(double ordenadaSinCargo) {
        this.ordenadaSinCargo = ordenadaSinCargo;
    }

//    public double getCantOrdenada() {
//        return cantOrdenada;
//    }
//
//    public void setCantOrdenada(double cantOrdenada) {
//        this.cantOrdenada = cantOrdenada;
//    }
//
//    public double getCantOrdenadaSinCargo() {
//        return cantOrdenadaSinCargo;
//    }
//
//    public void setCantOrdenadaSinCargo(double cantOrdenadaSinCargo) {
//        this.cantOrdenadaSinCargo = cantOrdenadaSinCargo;
//    }
//
    public double getRespaldo() {
        return respaldo;
    }

    public void setRespaldo(double respaldo) {
        this.respaldo = respaldo;
    }

    public double getRespaldoSinCargo() {
        return respaldoSinCargo;
    }

    public void setRespaldoSinCargo(double respaldoSinCargo) {
        this.respaldoSinCargo = respaldoSinCargo;
    }
    
    public boolean isSimilar() {
        return similar;
    }

    public void setSimilar(boolean similar) {
        this.similar = similar;
    }
}
