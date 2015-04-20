package formulas.dominio;

import java.io.Serializable;

/**
 *
 * @author jesc
 */
public class Insumo implements Serializable {
    private boolean nuevo;
    private int idEmpaque;
    private String cod_pro;
    private String empaque;
    private double cantidad;
    private double variacion;
    private double costoPromedio;
    private double ptjeCantParticipacion;
    private double ptjeCtoParticipacion;
    
    public Insumo() {
        this.nuevo=true;
        this.cod_pro="";
        this.empaque="";
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + this.idEmpaque;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Insumo other = (Insumo) obj;
        if (this.idEmpaque != other.idEmpaque) {
            return false;
        }
        return true;
    }

    public boolean isNuevo() {
        return nuevo;
    }

    public void setNuevo(boolean nuevo) {
        this.nuevo = nuevo;
    }

    public int getIdEmpaque() {
        return idEmpaque;
    }

    public void setIdEmpaque(int idEmpaque) {
        this.idEmpaque = idEmpaque;
    }

    public String getCod_pro() {
        return cod_pro;
    }

    public void setCod_pro(String cod_pro) {
        this.cod_pro = cod_pro;
    }

    public String getEmpaque() {
        return empaque;
    }

    public void setEmpaque(String empaque) {
        this.empaque = empaque;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public double getVariacion() {
        return variacion;
    }

    public void setVariacion(double variacion) {
        this.variacion = variacion;
    }

    public double getCostoPromedio() {
        return costoPromedio;
    }

    public void setCostoPromedio(double costoPromedio) {
        this.costoPromedio = costoPromedio;
    }

    public double getPtjeCantParticipacion() {
        return ptjeCantParticipacion;
    }

    public void setPtjeCantParticipacion(double ptjeCantParticipacion) {
        this.ptjeCantParticipacion = ptjeCantParticipacion;
    }

    public double getPtjeCtoParticipacion() {
        return ptjeCtoParticipacion;
    }

    public void setPtjeCtoParticipacion(double ptjeCtoParticipacion) {
        this.ptjeCtoParticipacion = ptjeCtoParticipacion;
    }
}
