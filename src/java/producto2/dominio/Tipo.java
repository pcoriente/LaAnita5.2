package producto2.dominio;

/**
 *
 * @author jesc
 */
public class Tipo {
    private int idTipo;
    private String tipo;
    
    public Tipo() {
        this.tipo="";
    }
    
    public Tipo(int idTipo, String tipo) {
        this.idTipo=idTipo;
        this.tipo=tipo;
    }
    
    @Override
    public String toString() {
        return tipo;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + this.idTipo;
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
        final Tipo other = (Tipo) obj;
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
