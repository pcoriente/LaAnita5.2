
package leyenda.dominio;

import java.io.Serializable;

public class ClienteBanco implements Serializable {
    private int idClienteBanco;
    private int codigoCliente;
    private int idBanco;
    private String numCtaPago;
    private String medioPago;
    private BancoLeyenda bancoLeyenda = new BancoLeyenda();
   

    public int getCodigoCliente() {
        return codigoCliente;
    }

    public void setCodigoCliente(int codigoCliente) {
        this.codigoCliente = codigoCliente;
    }

    public int getIdBanco() {
        return idBanco;
    }

    public void setIdBanco(int idBanco) {
        this.idBanco = idBanco;
    }

    public int getIdClienteBanco() {
        return idClienteBanco;
    }

    public void setIdClienteBanco(int idClienteBanco) {
        this.idClienteBanco = idClienteBanco;
    }

    public BancoLeyenda getBancoLeyenda() {
        return bancoLeyenda;
    }

    public void setBancoLeyenda(BancoLeyenda bancoLeyenda) {
        this.bancoLeyenda = bancoLeyenda;
    }

   

    public String getMedioPago() {
        return medioPago;
    }

    public void setMedioPago(String medioPago) {
        this.medioPago = medioPago;
    }

    public String getNumCtaPago() {
        return numCtaPago;
    }

    public void setNumCtaPago(String numCtaPago) {
        this.numCtaPago = numCtaPago;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + this.idClienteBanco;
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
        final ClienteBanco other = (ClienteBanco) obj;
        if (this.idClienteBanco != other.idClienteBanco) {
            return false;
        }
        return true;
    }
    
    
    
}
