/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientes.to;

import empresas.dominio.MiniEmpresa;
import menuClientesGrupos.dominio.ClienteGrupo;
import formatos.dominio.ClienteFormato;
import clientes.dominio.MiniCliente;
/**
 *
 * @author carlos.pat
 */
public class TOClienteLista {

    private int idClienteLista;
    private int idEmpresa;
    private int idGrupoCte;
    private int idFormato;
    private int idCliente;
    private int idTienda;
    private MiniEmpresa miniemp = new MiniEmpresa();
    private ClienteGrupo clientegrupo = new ClienteGrupo();
    private ClienteFormato clienteformato = new ClienteFormato();
    private clientes.dominio.MiniCliente minicliente = new MiniCliente();

    public int getIdClienteLista() {
        return idClienteLista;
    }

    public void setIdClienteLista(int idClienteLista) {
        this.idClienteLista = idClienteLista;
    }
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.idClienteLista;
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
        final TOClienteLista other = (TOClienteLista) obj;
        if (this.idClienteLista != other.idClienteLista) {
            return false;
        }
        return true;
    }
    
    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public int getIdGrupoCte() {
        return idGrupoCte;
    }

    public void setIdGrupoCte(int idGrupoCte) {
        this.idGrupoCte = idGrupoCte;
    }

    public int getIdFormato() {
        return idFormato;
    }

    public void setIdFormato(int idFormato) {
        this.idFormato = idFormato;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdTienda() {
        return idTienda;
    }

    public void setIdTienda(int idTienda) {
        this.idTienda = idTienda;
    }

    public MiniEmpresa getMiniemp() {
        return miniemp;
    }

    public void setMiniemp(MiniEmpresa miniemp) {
        this.miniemp = miniemp;
    }

    public ClienteFormato getClienteformato() {
        return clienteformato;
    }

    public void setClienteformato(ClienteFormato clienteformato) {
        this.clienteformato = clienteformato;
    }


    public ClienteGrupo getClientegrupo() {
        return clientegrupo;
    }

    public void setClientegrupo(ClienteGrupo clientegrupo) {
        this.clientegrupo = clientegrupo;
    }

    public MiniCliente getMinicliente() {
        return minicliente;
    }

    public void setMinicliente(MiniCliente minicliente) {
        this.minicliente = minicliente;
    }

}
