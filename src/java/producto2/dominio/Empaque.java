package producto2.dominio;

/**
 *
 * @author jesc
 */
public class Empaque {
    private int idEmpaque;
    private String empaque;
    private String abreviatura;
    
    public Empaque() {
        this.empaque="";
        this.abreviatura="";
    }
    
    public Empaque(int idEmpaque, String empaque, String abreviatura) {
        this.idEmpaque = idEmpaque;
        this.empaque = empaque;
        this.abreviatura = abreviatura;
    }

    @Override
    public String toString() {
        return empaque;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + this.idEmpaque;
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
        final Empaque other = (Empaque) obj;
        if (this.idEmpaque != other.idEmpaque) {
            return false;
        }
        return true;
    }

    public int getIdEmpaque() {
        return idEmpaque;
    }

    public void setIdEmpaque(int idEmpaque) {
        this.idEmpaque = idEmpaque;
    }

    public String getEmpaque() {
        return empaque;
    }

    public void setEmpaque(String empaque) {
        this.empaque = empaque;
    }

    public String getAbreviatura() {
        return abreviatura;
    }

    public void setAbreviatura(String abreviatura) {
        this.abreviatura = abreviatura;
    }
}
