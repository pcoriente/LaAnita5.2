package envios.dominio;

import traspasos.dominio.TraspasoProducto;

/**
 *
 * @author jesc
 */
public class EnvioProducto extends TraspasoProducto {
    private int idEnvio;
    private int idSolicitud;
    private double estadistica;
    private double existencia;
    private double sugerido;
    private double sugerido2;
    private int diasInventario;
    private int diasInventario2;
    private boolean banCajas;
    private double cantFincada;
    private double cantDirecta;
    private double solicitada;
    private double solicitada2;
    
    public EnvioProducto() {
        super();
    }

    public int getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(int idEnvio) {
        this.idEnvio = idEnvio;
    }

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public double getEstadistica() {
        return estadistica;
    }

    public void setEstadistica(double estadistica) {
        this.estadistica = estadistica;
    }

    public double getExistencia() {
        return existencia;
    }

    public void setExistencia(double existencia) {
        this.existencia = existencia;
    }

    public double getSugerido() {
        return sugerido;
    }

    public void setSugerido(double sugerido) {
        this.sugerido = sugerido;
    }

    public double getSugerido2() {
        return sugerido2;
    }

    public void setSugerido2(double sugerido2) {
        this.sugerido2 = sugerido2;
    }

    public int getDiasInventario() {
        return diasInventario;
    }

    public void setDiasInventario(int diasInventario) {
        this.diasInventario = diasInventario;
    }

    public int getDiasInventario2() {
        return diasInventario2;
    }

    public void setDiasInventario2(int diasInventario2) {
        this.diasInventario2 = diasInventario2;
    }

    public boolean isBanCajas() {
        return banCajas;
    }

    public void setBanCajas(boolean banCajas) {
        this.banCajas = banCajas;
    }

    public double getCantFincada() {
        return cantFincada;
    }

    public void setCantFincada(double cantFincada) {
        this.cantFincada = cantFincada;
    }

    public double getCantDirecta() {
        return cantDirecta;
    }

    public void setCantDirecta(double cantDirecta) {
        this.cantDirecta = cantDirecta;
    }

    public double getSolicitada() {
        return solicitada;
    }

    public void setSolicitada(double solicitada) {
        this.solicitada = solicitada;
    }

    public double getSolicitada2() {
        return solicitada2;
    }

    public void setSolicitada2(double solicitada2) {
        this.solicitada2 = solicitada2;
    }
}
