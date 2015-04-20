/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rutas.dominio;

import java.io.Serializable;

/**
 *
 * @author Usuario
 */
public class Ruta implements Serializable{

    private int idRuta;
    private String ruta;
    
    public Ruta() {
        this.ruta="";
    }
    
    @Override
    public String toString() {
        return this.ruta;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + this.idRuta;
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
        final Ruta other = (Ruta) obj;
        if (this.idRuta != other.idRuta) {
            return false;
        }
        return true;
    }

    public int getIdRuta() {
        return idRuta;
    }

    public void setIdRuta(int idRuta) {
        this.idRuta = idRuta;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }
}
