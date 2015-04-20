/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clientes.dominio;

import contactos.dominio.Contacto;
import contribuyentes.Contribuyente;
import direccion.dominio.Direccion;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import leyenda.dominio.ClienteBanco;
import menuClientesGrupos.dominio.ClienteGrupo;

/**
 *
 * @author Pjgt
 */
public class Cliente implements Serializable {

    private int idCliente;
    private ClienteGrupo grupo;
    private TiendaFormato formato;
    private Contribuyente contribuyente;
    private Direccion direccion;
    private int idEsquema;
    private Date fechaAlta;
    private int diasCredito;
    private double limiteCredito;
    private double descuentoComercial;
    private int diasBloqueo;
    private ArrayList<ClienteBanco> bancos;
    private ArrayList<Contacto> contactos;

    public Cliente() {
        this.grupo=new ClienteGrupo();
        this.formato=new TiendaFormato();
        this.contribuyente=new Contribuyente();
        this.direccion=new Direccion();
        this.bancos=new ArrayList<ClienteBanco>();
        this.contactos=new ArrayList<Contacto>();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + this.idCliente;
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
        final Cliente other = (Cliente) obj;
        if (this.idCliente != other.idCliente) {
            return false;
        }
        return true;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public ClienteGrupo getGrupo() {
        return grupo;
    }

    public void setGrupo(ClienteGrupo grupo) {
        this.grupo = grupo;
    }

    public TiendaFormato getFormato() {
        return formato;
    }

    public void setFormato(TiendaFormato formato) {
        this.formato = formato;
    }

    public int getIdEsquema() {
        return idEsquema;
    }

    public void setIdEsquema(int idEsquema) {
        this.idEsquema = idEsquema;
    }

    public Contribuyente getContribuyente() {
        return contribuyente;
    }

    public void setContribuyente(Contribuyente contribuyente) {
        this.contribuyente = contribuyente;
    }

    public ArrayList<ClienteBanco> getBancos() {
        return bancos;
    }

    public void setBancos(ArrayList<ClienteBanco> bancos) {
        this.bancos = bancos;
    }

    public ArrayList<Contacto> getContactos() {
        return contactos;
    }

    public void setContactos(ArrayList<Contacto> contactos) {
        this.contactos = contactos;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
    }

    public Date getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(Date fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public int getDiasCredito() {
        return diasCredito;
    }

    public void setDiasCredito(int diasCredito) {
        this.diasCredito = diasCredito;
    }

    public double getLimiteCredito() {
        return limiteCredito;
    }

    public void setLimiteCredito(double limiteCredito) {
        this.limiteCredito = limiteCredito;
    }

    public double getDescuentoComercial() {
        return descuentoComercial;
    }

    public void setDescuentoComercial(double descuentoComercial) {
        this.descuentoComercial = descuentoComercial;
    }

    public int getDiasBloqueo() {
        return diasBloqueo;
    }

    public void setDiasBloqueo(int diasBloqueo) {
        this.diasBloqueo = diasBloqueo;
    }
}
