package proveedores.dominio;

import java.io.Serializable;

/**
 *
 * @author jsolis
 */
public class TipoOperacion implements Serializable {
    private int idTipoOperacion;
    private String tipoOperacion;
    private String operacion;

    public TipoOperacion() {
        this.idTipoOperacion=0;
        this.tipoOperacion="00";
        this.operacion="";
    }

    public TipoOperacion(int idTipoOperacion, String tipoOperacion, String operacion) {
        this.idTipoOperacion = idTipoOperacion;
        this.tipoOperacion = tipoOperacion;
        this.operacion = operacion;
    }

    @Override
    public String toString() {
        return this.operacion;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + this.idTipoOperacion;
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
        final TipoOperacion other = (TipoOperacion) obj;
        if (this.idTipoOperacion != other.idTipoOperacion) {
            return false;
        }
        return true;
    }

    public int getIdTipoOperacion() {
        return idTipoOperacion;
    }

    public void setIdTipoOperacion(int idTipoOperacion) {
        this.idTipoOperacion = idTipoOperacion;
    }

    public String getTipoOperacion() {
        return tipoOperacion;
    }

    public void setTipoOperacion(String tipoOperacion) {
        this.tipoOperacion = tipoOperacion;
    }

    public String getOperacion() {
        return operacion;
    }

    public void setOperacion(String operacion) {
        this.operacion = operacion;
    }
}
