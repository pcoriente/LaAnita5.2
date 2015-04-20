/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientesListas.formatosDetalleDominio;

import java.io.Serializable;
import java.util.Date;
import producto2.dominio.Producto;

/**
 *
 * @author Usuario
 */
public class ClienteListasDetalle implements Serializable {

    private int idClientesLista;
    private Producto producto = new Producto();
    private double descuentos;
    private Date finVigencia;
     private Date inicioVigencia;
    private double precioVenta;


    public double getDescuentos() {
        return descuentos;
    }

    public void setDescuentos(double descuentos) {
        this.descuentos = descuentos;
    }

    public Date getFinVigencia() {
        return finVigencia;
    }

    public void setFinVigencia(Date finVigencia) {
        this.finVigencia = finVigencia;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + this.idClientesLista;
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
        final ClienteListasDetalle other = (ClienteListasDetalle) obj;
        if (this.idClientesLista != other.idClientesLista) {
            return false;
        }
        return true;
    }

    public int getIdClientesLista() {
        return idClientesLista;
    }

    public void setIdClientesLista(int idClientesLista) {
        this.idClientesLista = idClientesLista;
    }

    public Date getInicioVigencia() {
        return inicioVigencia;
    }

    public void setInicioVigencia(Date inicioVigencia) {
        this.inicioVigencia = inicioVigencia;
    }

    public double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(double precioVenta) {
        this.precioVenta = precioVenta;
    }

}
