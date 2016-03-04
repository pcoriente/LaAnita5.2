package pedidos.to;

import movimientos.to.TOProductoOficina;

/**
 *
 * @author jesc
 */
public class TOProductoPedido extends TOProductoOficina {
    private int idEnvio;
    private double cantEnviada;
    private int idPedido;
    private double cantOrdenada;
    private double cantOrdenadaSinCargo;
    private int piezas;
//    private boolean similar;

    public TOProductoPedido() {
        super();
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
//    
    public int getPiezas() {
        return piezas;
    }

    public void setPiezas(int piezas) {
        this.piezas = piezas;
    }
}
