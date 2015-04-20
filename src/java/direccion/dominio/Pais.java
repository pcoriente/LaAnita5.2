package direccion.dominio;

import java.io.Serializable;

/**
 *
 * @author Julio
 */
public class Pais implements Serializable{
    private int idPais;
    private String pais;
    
    public Pais() {
        this.idPais=1;
        this.pais="";
    }

    @Override
    public String toString() {
        return pais;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pais other = (Pais) obj;
        if (this.idPais != other.idPais) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + this.idPais;
        return hash;
    }

    public int getIdPais() {
        return idPais;
    }

    public void setIdPais(int idPais) {
        this.idPais = idPais;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }
}
