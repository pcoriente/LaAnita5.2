package pedidos.to;

import movimientos.to.TOProductoOficina;

/**
 *
 * @author jesc
 */
public class TOPedidoProducto extends TOProductoOficina {
    private int idEnvio;
    private int idPedido;
    private double cantEnviar;
    private double cantEnviarSinCargo;
    private double cantOrdenada;
    private double cantOrdenadaSinCargo;
    private int piezas;
    private String cod_pro;

    public TOPedidoProducto() {
        super();
        this.cod_pro="";
    }

    public int getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(int idEnvio) {
        this.idEnvio = idEnvio;
    }
    
    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
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

    public String getCod_pro() {
        return cod_pro;
    }

    public void setCod_pro(String cod_pro) {
        this.cod_pro = cod_pro;
    }
}
