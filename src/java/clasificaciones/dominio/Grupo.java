package clasificaciones.dominio;

/**
 *
 * @author Julio
 */
public class Grupo {
    private int idGrupo;
    private int codigoGrupo;
    private String grupo;

    public Grupo(int idGrupo, int codigoGrupo, String grupo) {
        this.idGrupo = idGrupo;
        this.codigoGrupo = codigoGrupo;
        this.grupo = grupo;
    }

    @Override
    public String toString() {
        return grupo;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Grupo other = (Grupo) obj;
        if (this.idGrupo != other.idGrupo) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + this.idGrupo;
        return hash;
    }

    public int getCodigoGrupo() {
        return codigoGrupo;
    }

    public void setCodigoGrupo(int codigoGrupo) {
        this.codigoGrupo = codigoGrupo;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public int getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(int idGrupo) {
        this.idGrupo = idGrupo;
    }
}
