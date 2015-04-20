package proveedores.dominio;

/**
 *
 * @author jsolis
 */
public class Clasificacion {
    private int idClasificacion;
    private String clasificacion;

    public Clasificacion() {
        this.idClasificacion=0;
        this.clasificacion="";
    }

    public Clasificacion(int idClasificacion, String clasificacion) {
        this.idClasificacion = idClasificacion;
        this.clasificacion = clasificacion;
    }

    @Override
    public String toString() {
        return this.clasificacion;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.idClasificacion;
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
        final Clasificacion other = (Clasificacion) obj;
        if (this.idClasificacion != other.idClasificacion) {
            return false;
        }
        return true;
    }

    public int getIdClasificacion() {
        return idClasificacion;
    }

    public void setIdClasificacion(int idClasificacion) {
        this.idClasificacion = idClasificacion;
    }

    public String getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(String clasificacion) {
        this.clasificacion = clasificacion;
    }
}
