package formulas.to;

/**
 *
 * @author jesc
 */
public class TOInsumo {
    private int idEmpaque;
    private double cantidad;
    private double costoUnitarioPromedio;
    private double porcentVariacion;
    private double costoUnitario;

    public int getIdEmpaque() {
        return idEmpaque;
    }

    public void setIdEmpaque(int idEmpaque) {
        this.idEmpaque = idEmpaque;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public double getCostoUnitarioPromedio() {
        return costoUnitarioPromedio;
    }

    public void setCostoUnitarioPromedio(double costoUnitarioPromedio) {
        this.costoUnitarioPromedio = costoUnitarioPromedio;
    }

    public double getPorcentVariacion() {
        return porcentVariacion;
    }

    public void setPorcentVariacion(double porcentVariacion) {
        this.porcentVariacion = porcentVariacion;
    }

    public double getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(double costoUnitario) {
        this.costoUnitario = costoUnitario;
    }
}
