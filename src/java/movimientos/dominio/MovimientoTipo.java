package movimientos.dominio;

/**
 *
 * @author jesc
 */
public class MovimientoTipo {
    private int idTipo;
    private String tipo;
    
    public MovimientoTipo() {
        this.tipo="";
    }
    
    public MovimientoTipo(int idTipo, String tipo) {
        this.idTipo=idTipo;
        this.tipo=tipo;
    }

    @Override
    public String toString() {
        return this.tipo;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + this.idTipo;
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
        final MovimientoTipo other = (MovimientoTipo) obj;
        if (this.idTipo != other.idTipo) {
            return false;
        }
        return true;
    }

    public int getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(int idTipo) {
        this.idTipo = idTipo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
