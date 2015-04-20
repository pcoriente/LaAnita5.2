package envios.dominio;

import producto2.dominio.Empaque;

/**
 *
 * @author jesc
 */
public class EnvioProducto {
    private int idEnvio;
    private int idMovto;
    private Empaque empaque;
    private double cantidad;
    private double peso;

    public EnvioProducto() {}

    public EnvioProducto(Empaque empaque) {
        this.empaque = empaque;
    }

    @Override
    public String toString() {
        return this.empaque.toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + (this.empaque != null ? this.empaque.hashCode() : 0);
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
        final EnvioProducto other = (EnvioProducto) obj;
        if (this.empaque != other.empaque && (this.empaque == null || !this.empaque.equals(other.empaque))) {
            return false;
        }
        return true;
    }

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

    public Empaque getEmpaque() {
        return empaque;
    }

    public void setEmpaque(Empaque empaque) {
        this.empaque = empaque;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }
}
