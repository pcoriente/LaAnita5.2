package impuestos.dominio;

/**
 *
 * @author JULIOS
 */
public class ImpuestoGrupo {
    private int idGrupo;
    private String grupo;
    
    public ImpuestoGrupo(int idGrupo, String grupo) {
        this.idGrupo=idGrupo;
        this.grupo=grupo;
    }

    @Override
    public String toString() {
        return grupo;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + this.idGrupo;
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
        final ImpuestoGrupo other = (ImpuestoGrupo) obj;
        if (this.idGrupo != other.idGrupo) {
            return false;
        }
        return true;
    }

    public int getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(int idGrupo) {
        this.idGrupo = idGrupo;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }
}
