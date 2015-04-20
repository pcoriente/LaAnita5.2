/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientesBancos.dominio;

import bancos.dominio.Banco;
import java.io.Serializable;

/**
 *
 * @author Usuario
 */
public class ClientesBancos implements Serializable{

    private int idClienteBanco;
    private int codigoCliente;
    private Banco banco = new Banco();
    private String numCtaPago;
    private String medioPago;

    public int getIdClienteBanco() {
        return idClienteBanco;
    }

    public void setIdClienteBanco(int idClienteBanco) {
        this.idClienteBanco = idClienteBanco;
    }

    public int getCodigoCliente() {
        return codigoCliente;
    }

    public void setCodigoCliente(int codigoCliente) {
        this.codigoCliente = codigoCliente;
    }

    public Banco getBanco() {
        return banco;
    }

    public void setBanco(Banco banco) {
        this.banco = banco;
    }

    public String getNumCtaPago() {
        return numCtaPago;
    }

    public void setNumCtaPago(String numCtaPago) {
        this.numCtaPago = numCtaPago;
    }

    public String getMedioPago() {
        return medioPago;
    }

    public void setMedioPago(String medioPago) {
        this.medioPago = medioPago;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + this.idClienteBanco;
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
        final ClientesBancos other = (ClientesBancos) obj;
        if (this.idClienteBanco != other.idClienteBanco) {
            return false;
        }
        return true;
    }

}
