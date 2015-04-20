package producto2.dominio;

/**
 *
 * @author jesc
 */
public class Grupo {
    private int idGrupo;
    private int codigo;
    private String grupo;
    
    public Grupo() {
        this.grupo="";
    }

    public Grupo(int idGrupo, int codigo, String grupo) {
        this.idGrupo = idGrupo;
        this.codigo = codigo;
        this.grupo = grupo;
    }

    @Override
    public String toString() {
        return grupo;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + this.idGrupo;
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
        final Grupo other = (Grupo) obj;
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

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }
}
