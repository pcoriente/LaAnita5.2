package clasificaciones.dominio;

/**
 *
 * @author Julio
 */
public class SubGrupo {
    private int idSubGrupo;
    private int codigoSubGrupo;
    private String subGrupo;
    private Grupo grupo;

    public int getCodigoSubGrupo() {
        return codigoSubGrupo;
    }

    public void setCodigoSubGrupo(int codigoSubGrupo) {
        this.codigoSubGrupo = codigoSubGrupo;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
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
