package ventas.dominio;

import java.util.Date;

/**
 *
 * @author jesc
 */
public class EmpaqueLote {
    private String lote;
    private Date fechaCaducidad;
    private double disponibles;

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.lote != null ? this.lote.hashCode() : 0);
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
        final EmpaqueLote other = (EmpaqueLote) obj;
        if ((this.lote == null) ? (other.lote != null) : !this.lote.equals(other.lote)) {
            return false;
        }
        return true;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public Date getFechaCaducidad() {
        return fechaCaducidad;
    }

    public void setFechaCaducidad(Date fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }

    public double getDisponibles() {
        return disponibles;
    }

    public void setDisponibles(double disponibles) {
        this.disponibles = disponibles;
    }
}
