package clientes2.dominio;

import clientes.dominio.ClienteSEA;
import bancos.dominio.Banco;

public class Cliente {

    private int idCliente;
    private ClienteSEA clienteSEA;
    private Banco banco;
    //private int idbanco;
    //private int codigoCliente; //Convertimos en entero.
    private String numCtaPago;
    
    private String medioPago;
    private String nombre;

    public ClienteSEA getClienteSEA() {
        return clienteSEA;
    }

    public void setClienteSEA(ClienteSEA clienteSEA) {
        this.clienteSEA = clienteSEA;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

//    public int getIdbanco() {
//        return idbanco;
//    }
//
//    public void setIdbanco(int idbanco) {
//        this.idbanco = idbanco;
//    }
    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public Banco getBanco() {
        return banco;
    }

    public void setBanco(Banco banco) {
        this.banco = banco;
    }

//    public int getCodigoCliente() {
//        return codigoCliente;
//    }
//
//    public void setCodigoCliente(int codigoCliente) {
//        this.codigoCliente = codigoCliente;
//    }
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
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Cliente other = (Cliente) obj;
        if (this.idCliente != other.idCliente) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + this.idCliente;
        return hash;
    }
}
