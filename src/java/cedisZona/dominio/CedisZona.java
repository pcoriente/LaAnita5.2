/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cedisZona.dominio;

import java.io.Serializable;

/**
 *
 * @author PJGT
 */
public class CedisZona implements Serializable {

    private int idZona;
    private String zona;
    private int eliminable;

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

    public int getEliminable() {
        return eliminable;
    }

    public void setEliminable(int eliminable) {
        this.eliminable = eliminable;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.idZona;
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
        final CedisZona other = (CedisZona) obj;
        if (this.idZona != other.idZona) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return zona;
    }
}
