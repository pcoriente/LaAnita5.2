package solicitudes.to;

/**
 *
 * @author jesc
 */
public class TOSolicitudProducto {
    private int idMovto;
    private int idProducto;
    private double cantSolicitada;
    
    public TOSolicitudProducto() {}

    public int getIdMovto() {
        return idMovto;
    }

    public void setIdMovto(int idMovto) {
        this.idMovto = idMovto;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public double getCantSolicitada() {
        return cantSolicitada;
    }

    public void setCantSolicitada(double cantSolicitada) {
        this.cantSolicitada = cantSolicitada;
    }
}
