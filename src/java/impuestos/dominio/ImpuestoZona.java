package impuestos.dominio;

import java.io.Serializable;

public class ImpuestoZona implements Serializable {

    private int idZona;
    private String zona;

    public ImpuestoZona() {
    }

    public ImpuestoZona(int idZona, String zona) {
        this.idZona = idZona;
        this.zona = zona;
    }

    @Override
    public String toString() {
        return zona;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.idZona;
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
        final ImpuestoZona other = (ImpuestoZona) obj;
        if (this.idZona != other.idZona) {
            return false;
        }
        return true;
    }

    public int getIdZona() {
        return idZona;
    }

    public void setIdZona(int idZona) {
        this.idZona = idZona;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }
}
