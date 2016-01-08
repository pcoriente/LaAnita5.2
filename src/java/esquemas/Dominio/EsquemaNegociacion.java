/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esquemas.Dominio;

/**
 *
 * @author Torres
 */
public class EsquemaNegociacion {

    private int idEsquema;
    private String esquema;

    public int getIdEsquema() {
        return idEsquema;
    }

    public void setIdEsquema(int idEsquema) {
        this.idEsquema = idEsquema;
    }

    public String getEsquema() {
        return esquema;
    }

    public void setEsquema(String esquema) {
        this.esquema = esquema;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.idEsquema;
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
        final EsquemaNegociacion other = (EsquemaNegociacion) obj;
        if (this.idEsquema != other.idEsquema) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "EsquemaNegociacion{" + "idEsquema=" + idEsquema + ", esquema=" + esquema + '}';
    }

}
