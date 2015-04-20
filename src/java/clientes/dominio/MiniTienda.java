package clientes.dominio;

/**
 *
 * @author jesc
 */
public class MiniTienda {
    private int idTienda;
    private String codigoTienda;
    private String tienda;
    
    public MiniTienda() {
        this.codigoTienda="";
        this.tienda="";
    }

    @Override
    public String toString() {
        return this.tienda;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + this.idTienda;
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
        final MiniTienda other = (MiniTienda) obj;
        if (this.idTienda != other.idTienda) {
            return false;
        }
        return true;
    }

    public int getIdTienda() {
        return idTienda;
    }

    public void setIdTienda(int idTienda) {
        this.idTienda = idTienda;
    }

    public String getCodigoTienda() {
        return codigoTienda;
    }

    public void setCodigoTienda(String codigoTienda) {
        this.codigoTienda = codigoTienda;
    }

    public String getTienda() {
        return tienda;
    }

    public void setTienda(String tienda) {
        this.tienda = tienda;
    }
}
