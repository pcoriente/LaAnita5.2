package clasificaciones.to;

/**
 *
 * @author Julio
 */
public class TOSubGrupo {
    private int idSubGrupo;
    private int codigoSubGrupo;
    private String subGrupo;
    private int idGrupo;

    public TOSubGrupo(int idSubGrupo, int codigoSubGrupo, String subGrupo, int idGrupo) {
        this.idSubGrupo = idSubGrupo;
        this.codigoSubGrupo = codigoSubGrupo;
        this.subGrupo = subGrupo;
        this.idGrupo = idGrupo;
    }

    public int getCodigoSubGrupo() {
        return codigoSubGrupo;
    }

    public void setCodigoSubGrupo(int codigoSubGrupo) {
        this.codigoSubGrupo = codigoSubGrupo;
    }

    public int getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(int idGrupo) {
        this.idGrupo = idGrupo;
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
