package producto2.dominio;

/**
 *
 * @author jesc
 */
public class Marca {
    private int idMarca;
    private String marca;
    private boolean produccion;
    
    public Marca() {
        this.marca="";
        this.produccion=true;
    }
    
    public Marca(int idMarca, String marca, boolean produccion) {
        this.idMarca = idMarca;
        this.marca = marca;
        this.produccion = produccion;
    }

    @Override
    public String toString() {
        return marca;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + this.idMarca;
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
        final Marca other = (Marca) obj;
        if (this.idMarca != other.idMarca) {
            return false;
        }
        return true;
    }

    public int getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(int idMarca) {
        this.idMarca = idMarca;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public boolean isProduccion() {
        return produccion;
    }

    public void setProduccion(boolean produccion) {
        this.produccion = produccion;
    }
}
