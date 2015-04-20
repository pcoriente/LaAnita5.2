package contactos.dominio;

import java.io.Serializable;

/**
 *
 * @author jsolis
 */
public class TelefonoTipo implements Serializable{
    private int idTipo;
    private String tipo;
    private boolean celular;

    public TelefonoTipo(boolean celular) {
        this.idTipo=0;
        this.tipo="";
        this.celular=celular;
    }

    @Override
    public String toString() {
        return (this.getTipo().isEmpty() ? "" : (this.celular ? "Cel. ": "Tel. ")+this.tipo+" ");
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.idTipo;
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
        final TelefonoTipo other = (TelefonoTipo) obj;
        if (this.idTipo != other.idTipo) {
            return false;
        }
        return true;
    }

    public int getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(int idTipo) {
        this.idTipo = idTipo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isCelular() {
        return celular;
    }

    public void setCelular(boolean celular) {
        this.celular = celular;
    }
}
