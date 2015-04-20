package mvEntradas;

/**
 *
 * @author jesc
 */
public class TOEntradaOficinaProducto {
    private int idProducto;
    private double cantidad;
    private double costo;
    
    public TOEntradaOficinaProducto() {}

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }
}
