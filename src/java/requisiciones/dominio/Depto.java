package requisiciones.dominio;

public class Depto {

    private int idDepto;
    private int idEmpresa;
    private String depto;
    private int codigo;

    public Depto() {
    }

    public Depto(int idDepto, String depto) {
        this.idDepto = idDepto;
        this.depto = depto;
       
    }

    @Override
    public String toString() {
        return  depto;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + this.idDepto;
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
        final Depto other = (Depto) obj;
        if (this.idDepto != other.idDepto) {
            return false;
        }
        return true;
    }
    
    

    public int getIdDepto() {
        return idDepto;
    }

    public void setIdDepto(int idDepto) {
        this.idDepto = idDepto;
    }

    public String getDepto() {
        return depto;
    }

    public void setDepto(String depto) {
        this.depto = depto;
    }

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }
    
    
}
