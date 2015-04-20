/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package agentes;

import Message.Mensajes;
import agentes.dao.DaoAgentes;
import agentes.dominio.Agente;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.naming.NamingException;

/**
 *
 * @author Usuario
 */
@Named(value = "mbMiniAgentes")
@SessionScoped
public class MbMiniAgentes implements Serializable {
    
    private Agente cmbAgentes = new Agente();
    private ArrayList<SelectItem> lstAgentes;
    private DaoAgentes dao;

    public MbMiniAgentes() {
    }
    
    public Agente getCmbAgentes() {
        return cmbAgentes;
    }
    
    public void setCmbAgentes(Agente cmbAgentes) {
        this.cmbAgentes = cmbAgentes;
    }
    
    public Agente obtenerAgente(int idAgente) {
        Agente agente=null;
        try {
            this.dao=new DaoAgentes();
            agente=this.dao.obtenerAgente(idAgente);
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        return agente;
    }
    
    private void cargaAgentes() {
        this.lstAgentes = new ArrayList<SelectItem>();
        Agente agentes = new Agente();
        agentes.setIdAgente(0);
        agentes.setAgente("Seleccione un agente");
        lstAgentes.add(new SelectItem(agentes, agentes.getAgente()));
        try {
            this.dao=new DaoAgentes();
            for (Agente ag : dao.listaAgentes()) {
                this.lstAgentes.add(new SelectItem(ag, ag.getAgente()));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }
    
    public ArrayList<SelectItem> getLstAgentes() {
        if (this.lstAgentes == null) {
            this.cargaAgentes();
        }
        return lstAgentes;
    }
    
    public void setLstAgentes(ArrayList<SelectItem> lstAgentes) {
        this.lstAgentes = lstAgentes;
    }
    
}
