/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package listaPrecioIdeal.dominio;

import java.io.Serializable;
import producto2.dominio.Empaque;
import producto2.dominio.Producto;

/**
 *
 * @author Usuario
 */
public class ListaPrecioIdeal implements Serializable {

    private Producto producto = new Producto();
    private double precioLista = 0.00;

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public double getPrecioLista() {
        return precioLista;
    }

    public void setPrecioLista(double precioLista) {
        this.precioLista = precioLista;
    }

}
