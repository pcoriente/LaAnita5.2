package clientes.dominio;

/**
 *
 * @author jesc
 */
public class TiendaFormato {
    private int idFormato;
    private String formato;
    private int idGrupoCte;
    
    public TiendaFormato() {
        this.formato="";
    }
    
    public TiendaFormato(int idGrupoCte) {
        this.formato="";
        this.idGrupoCte=idGrupoCte;
    }

    @Override
    public String toString() {
        return this.formato;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + this.idFormato;
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
        final TiendaFormato other = (TiendaFormato) obj;
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
}
