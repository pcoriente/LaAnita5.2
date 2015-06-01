package envios.to;

/**
 *
 * @author jesc
 */
public class TOEnvioProducto {
    private int idEnvio;
    private int idMovto;
    private int idEmpaque;
    private double enviados;
    private double pendientes;
    private double peso;

    public int getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(int idEnvio) {
        this.idEnvio = idEnvio;
    }

    public int getIdMovto() {
        return idMovto;
    }

    public void setIdMovto(int idMovto) {
        this.idMovto = idMovto;
    }

    public int getIdEmpaque() {
        return idEmpaque;
    }

    public void setIdEmpaque(int idEmpaque) {
        this.idEmpaque = idEmpaque;
    }

    public double getEnviados() {
        return enviados;
    }

    public void setEnviados(double enviados) {
        this.enviados = enviados;
    }

    public double getPendientes() {
        return pendientes;
    }

    public void setPendientes(double pendientes) {
        this.pendientes = pendientes;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }
}
