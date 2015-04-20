package producto2.dominio;

/**
 *
 * @author JULIOS
 */
public class Upc {
    private String upc;
    private int idProducto;
    private boolean actual;
    
    public Upc() {
        this.upc="";
    }

    public Upc(String upc, int idProducto, boolean actual) {
        this.upc = upc;
        this.idProducto = idProducto;
        this.actual=actual;
    }

    @Override
    public String toString() {
        return this.upc+(this.actual?"*":"");
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + (this.upc != null ? this.upc.hashCode() : 0);
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
        final Upc other = (Upc) obj;
        if ((this.upc == null) ? (other.upc != null) : !this.upc.equals(other.upc)) {
            return false;
        }
        return true;
    }

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public boolean isActual() {
        return actual;
    }

    public void setActual(boolean actual) {
        this.actual = actual;
    }
}
