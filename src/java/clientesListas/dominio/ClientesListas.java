/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package clientesListas.dominio;

import empresas.dominio.Empresa;
import empresas.dominio.MiniEmpresa;
import formatos.dominio.ClienteFormato;
import menuClientesGrupos.dominio.ClienteGrupo;

/**
 *
 * @author Usuario
 */
public class ClientesListas {
    private int idClientesListas;
    private MiniEmpresa empresa = new MiniEmpresa();
    private ClienteGrupo grupoCliente =  new ClienteGrupo();
    private ClienteFormato formato = new ClienteFormato();
    private String descuetos;
    private double mercanciaConCargo;
    private double mercanciaSinCargo;
    private double porcentajaBoletin;
    private String numeroProveedor;
    private double boletin;

    public int getIdClientesListas() {
        return idClientesListas;
    }

    public void setIdClientesListas(int idClientesListas) {
        this.idClientesListas = idClientesListas;
    }

    public MiniEmpresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(MiniEmpresa empresa) {
        this.empresa = empresa;
    }

   

    public ClienteGrupo getGrupoCliente() {
        return grupoCliente;
    }

    public void setGrupoCliente(ClienteGrupo grupoCliente) {
        this.grupoCliente = grupoCliente;
    }

    public ClienteFormato getFormato() {
        return formato;
    }

    public void setFormato(ClienteFormato formato) {
        this.formato = formato;
    }

    public String getDescuetos() {
        return descuetos;
    }

    public void setDescuetos(String descuetos) {
        this.descuetos = descuetos;
    }

    public double getMercanciaConCargo() {
        return mercanciaConCargo;
    }

    public void setMercanciaConCargo(double mercanciaConCargo) {
        this.mercanciaConCargo = mercanciaConCargo;
    }

    public double getMercanciaSinCargo() {
        return mercanciaSinCargo;
    }

    public void setMercanciaSinCargo(double mercanciaSinCargo) {
        this.mercanciaSinCargo = mercanciaSinCargo;
    }

    public double getPorcentajaBoletin() {
        return porcentajaBoletin;
    }

    public void setPorcentajaBoletin(double porcentajaBoletin) {
        this.porcentajaBoletin = porcentajaBoletin;
    }

    public String getNumeroProveedor() {
        return numeroProveedor;
    }

    public void setNumeroProveedor(String numeroProveedor) {
        this.numeroProveedor = numeroProveedor;
    }

  
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.idClientesListas;
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
        final ClientesListas other = (ClientesListas) obj;
        if (this.idClientesListas != other.idClientesListas) {
            return false;
        }
        return true;
    }

    public double getBoletin() {
        return boletin;
    }

    public void setBoletin(double boletin) {
        this.boletin = boletin;
    }
    
    
}
