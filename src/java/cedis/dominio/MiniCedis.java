package cedis.dominio;

/**
 *
 * @author Julio
 */
public class MiniCedis {
    private int idCedis;
    //private String codigo;
    private String cedis;

    public MiniCedis() {
        this.idCedis=0;
        this.cedis="";
    }

    @Override
    public String toString() {
        return "("+ idCedis +") "+cedis;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MiniCedis other = (MiniCedis) obj;
        if (this.idCedis != other.idCedis) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.idCedis;
        return hash;
    }

    public String getCedis() {
        return cedis;
    }

    public void setCedis(String cedis) {
        this.cedis = cedis;
    }
    /*
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
    * */
    public int getIdCedis() {
        return idCedis;
    }

    public void setIdCedis(int idCedis) {
        this.idCedis = idCedis;
    }
}
