/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package formatos.dominio;

import java.io.Serializable;

/**
 *
 * @author Usuario
 */
public class ClienteFormato implements Serializable {

    private int idFormato;
    private String formato;
    private int idGrupoCte;
    private int idCliente;
    
    public ClienteFormato() {
        this.formato="";
    }
    
    @Override
    public String toString() {
        return this.formato;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + this.idFormato;
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
        final ClienteFormato other = (ClienteFormato) obj;
        if (this.idFormato != other.idFormato) {
            return false;
        }
        return true;
    }

    public int getIdFormato() {
        return idFormato;
    }

    public void setIdFormato(int idFormato) {
        this.idFormato = idFormato;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public int getIdGrupoCte() {
        return idGrupoCte;
    }

    public void setIdGrupoCte(int idGrupoCte) {
        this.idGrupoCte = idGrupoCte;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }
}
