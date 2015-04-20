package clientes.dominio;

import direccion.dominio.Direccion;

/**
 *
 * @author jesc
 */
public class Tienda {
    private int idTienda;
    private String codigoTienda;
    private String tienda;
    private Direccion direccion;
    
    public Tienda() {
        this.codigoTienda="";
        this.tienda="";
        this.direccion=new Direccion();
    }

    @Override
    public String toString() {
        return this.tienda;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.idTienda;
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
        final Tienda other = (Tienda) obj;
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

    public Direccion getDireccion() {
        return direccion;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
    }
}
