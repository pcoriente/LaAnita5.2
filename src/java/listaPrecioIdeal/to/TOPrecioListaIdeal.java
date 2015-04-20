/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package listaPrecioIdeal.to;

/**
 *
 * @author Usuario
 */
public class TOPrecioListaIdeal {

    private int idProducto;
    private double precioLista;

    public TOPrecioListaIdeal() {
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public double getPrecioLista() {
        return precioLista;
    }

    public void setPrecioLista(double precioLista) {
        this.precioLista = precioLista;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.idProducto;
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
        final TOPrecioListaIdeal other = (TOPrecioListaIdeal) obj;
        if (this.idProducto != other.idProducto) {
            return false;
        }
        return true;
    }

}
