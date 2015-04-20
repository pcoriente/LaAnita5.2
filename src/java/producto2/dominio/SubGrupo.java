package producto2.dominio;

/**
 *
 * @author jesc
 */
public class SubGrupo {
    private int idSubGrupo;
    private String subGrupo;
    
    public SubGrupo() {
        this.subGrupo = "";
    }

    public SubGrupo(int idSubGrupo, String subGrupo) {
        this.idSubGrupo = idSubGrupo;
        this.subGrupo = subGrupo;
    }

    @Override
    public String toString() {
        return subGrupo;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + this.idSubGrupo;
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
        final SubGrupo other = (SubGrupo) obj;
        if (this.idSubGrupo != other.idSubGrupo) {
            return false;
        }
        return true;
    }

    public int getIdSubGrupo() {
        return idSubGrupo;
    }

    public void setIdSubGrupo(int idSubGrupo) {
        this.idSubGrupo = idSubGrupo;
    }

    public String getSubGrupo() {
        return subGrupo;
    }

    public void setSubGrupo(String subGrupo) {
        this.subGrupo = subGrupo;
    }
}
