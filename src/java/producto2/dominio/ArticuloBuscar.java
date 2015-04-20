package producto2.dominio;

/**
 *
 * @author jesc
 */
public class ArticuloBuscar {
    private int idArticulo;
    private String tipo;
    private String grupo;
    private String subGrupo;
    private String articulo;
    
    public ArticuloBuscar(int idArticulo, String tipo, String grupo, String subGrupo, String articulo) {
        this.idArticulo = idArticulo;
        this.tipo = tipo;
        this.grupo = grupo;
        this.subGrupo = subGrupo;
        this.articulo = articulo;
    }

    public int getIdArticulo() {
        return idArticulo;
    }

    public void setIdArticulo(int idArticulo) {
        this.idArticulo = idArticulo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public String getSubGrupo() {
        return subGrupo;
    }

    public void setSubGrupo(String subGrupo) {
        this.subGrupo = subGrupo;
    }

    public String getArticulo() {
        return articulo;
    }

    public void setArticulo(String articulo) {
        this.articulo = articulo;
    }
}
