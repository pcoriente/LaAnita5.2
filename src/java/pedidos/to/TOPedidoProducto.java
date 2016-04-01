package pedidos.to;

import movimientos.to.TOProductoOficina;

/**
 *
 * @author jesc
 */
public class TOPedidoProducto extends TOProductoOficina {
    private int idEnvio;
    private double cantEnviada;
    private int idVenta;
    private double cantOrdenada;
    private double cantOrdenadaSinCargo;
    private int piezas;
    private String cod_pro;
//    private boolean similar;

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

    public double getCantEnviada() {
        return cantEnviada;
    }

    public void setCantEnviada(double cantEnviada) {
        this.cantEnviada = cantEnviada;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
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
