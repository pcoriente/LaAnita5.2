package contactos.dominio;

/**
 *
 * @author jsolis
 */
public class ContactoTipo {
    private int idTipo;
    private String tipo;

    public ContactoTipo() {
        this.idTipo=0;
        this.tipo="";
    }

    @Override
    public String toString() {
        return this.tipo;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + this.idTipo;
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
        final ContactoTipo other = (ContactoTipo) obj;
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
}
