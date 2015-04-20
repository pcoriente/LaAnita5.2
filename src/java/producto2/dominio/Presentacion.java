package producto2.dominio;

/**
 *
 * @author jesc
 */
public class Presentacion {
    private int idPresentacion;
    private String presentacion;
    private String abreviatura;
    
    public Presentacion() {
        this.idPresentacion=0;
        this.presentacion="";
        this.abreviatura="";
    }

    public Presentacion(int idPresentacion, String presentacion, String abreviatura) {
        this.idPresentacion = idPresentacion;
        this.presentacion = presentacion;
        this.abreviatura = abreviatura;
    }

    @Override
    public String toString() {
        return this.presentacion;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + this.idPresentacion;
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
        final Presentacion other = (Presentacion) obj;
        if (this.idPresentacion != other.idPresentacion) {
            return false;
        }
        return true;
    }

    public int getIdPresentacion() {
        return idPresentacion;
    }

    public void setIdPresentacion(int idPresentacion) {
        this.idPresentacion = idPresentacion;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        this.presentacion = presentacion;
    }

    public String getAbreviatura() {
        return abreviatura;
    }

    public void setAbreviatura(String abreviatura) {
        this.abreviatura = abreviatura;
    }
}
