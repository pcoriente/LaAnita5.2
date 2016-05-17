package envios.to;

/**
 *
 * @author jesc
 */
public class TOEnvioProductoReporte {
    private String sku;
    private String empaque;
    private double enviar;
    private double peso;
    private double estadistica;
    private double existencia;
    private int diasInventario;
    
    public TOEnvioProductoReporte() {
        this.sku = "";
        this.empaque = "";
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getEmpaque() {
        return empaque;
    }

    public void setEmpaque(String empaque) {
        this.empaque = empaque;
    }

    public double getEnviar() {
        return enviar;
    }

    public void setEnviar(double enviar) {
        this.enviar = enviar;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
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

    public int getDiasInventario() {
        return diasInventario;
    }

    public void setDiasInventario(int diasInventario) {
        this.diasInventario = diasInventario;
    }
}
