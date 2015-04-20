package proveedores.dominio;

/**
 *
 * @author jsolis
 */
public class TipoTercero {
    private int idTipoTercero;
    private String tipoTercero;
    private String tercero;

    public TipoTercero() {
        this.idTipoTercero=0;
        this.tipoTercero="00";
        this.tercero="";
    }

    public TipoTercero(int idTipoTercero, String tipoTercero, String tercero) {
        this.idTipoTercero = idTipoTercero;
        this.tipoTercero = tipoTercero;
        this.tercero = tercero;
    }

    @Override
    public String toString() {
        return this.tercero;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.idTipoTercero;
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
        final TipoTercero other = (TipoTercero) obj;
        if (this.idTipoTercero != other.idTipoTercero) {
            return false;
        }
        return true;
    }

    public int getIdTipoTercero() {
        return idTipoTercero;
    }

    public void setIdTipoTercero(int idTipoTercero) {
        this.idTipoTercero = idTipoTercero;
    }

    public String getTipoTercero() {
        return tipoTercero;
    }

    public void setTipoTercero(String tipoTercero) {
        this.tipoTercero = tipoTercero;
    }

    public String getTercero() {
        return tercero;
    }

    public void setTercero(String tercero) {
        this.tercero = tercero;
    }
}
