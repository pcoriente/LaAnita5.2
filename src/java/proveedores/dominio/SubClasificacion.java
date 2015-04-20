package proveedores.dominio;

/**
 *
 * @author jsolis
 */
public class SubClasificacion {
    private int idSubClasificacion;
    private String subClasificacion;

    public SubClasificacion() {
        this.idSubClasificacion=0;
        this.subClasificacion="";
    }

    public SubClasificacion(int idSubClasificacion, String subClasificacion) {
        this.idSubClasificacion = idSubClasificacion;
        this.subClasificacion = subClasificacion;
    }

    @Override
    public String toString() {
        return this.subClasificacion;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + this.idSubClasificacion;
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
        final SubClasificacion other = (SubClasificacion) obj;
        if (this.idSubClasificacion != other.idSubClasificacion) {
            return false;
        }
        return true;
    }

    public int getIdSubClasificacion() {
        return idSubClasificacion;
    }

    public void setIdSubClasificacion(int idSubClasificacion) {
        this.idSubClasificacion = idSubClasificacion;
    }

    public String getSubClasificacion() {
        return subClasificacion;
    }

    public void setSubClasificacion(String subClasificacion) {
        this.subClasificacion = subClasificacion;
    }
}
