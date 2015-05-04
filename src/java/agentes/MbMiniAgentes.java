/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package agentes;

import Message.Mensajes;
import agentes.dao.DaoAgentes;
import agentes.dominio.Agente;
//import agentes.dominio.Agentes;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Named;

/**
 *
 * @author Usuario
 */
@Named(value = "mbMiniAgentes")
@SessionScoped
public class MbMiniAgentes implements Serializable {
    
    private Agente cmbAgentes = new Agente();
    DaoAgentes dao = new DaoAgentes();
    private ArrayList<SelectItem> lstAgentes;

    /**
     * Creates a new instance of MbMiniAgentes
     */
    public MbMiniAgentes() {
    }
    
    public Agente getCmbAgentes() {
        return cmbAgentes;
    }
    
    public void setCmbAgentes(Agente cmbAgentes) {
        this.cmbAgentes = cmbAgentes;
    }
    
    public DaoAgentes getDao() {
        return dao;
    }
    
    public void setDao(DaoAgentes dao) {
        this.dao = dao;
    }
    
    
     public Agente obtenerAgente(int idAgente) {
        Agente a = null;
        try {
            DaoAgentes dao = new DaoAgentes();
            a = dao.dameAgentes(idAgente);
        } catch (SQLException ex) {
            Logger.getLogger(MbAgentes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return a;
    }
    
    public ArrayList<SelectItem> getLstAgentes() {
        if (lstAgentes == null) {
            try {
                lstAgentes = new ArrayList<SelectItem>();
                Agente agentes = new Agente();
                agentes.setIdAgente(0);
                agentes.setAgente("Seleccione un agente");
                lstAgentes.add(new SelectItem(agentes, agentes.getAgente()));
                for (Agente ag : dao.listaAgentes()) {
                    lstAgentes.add(new SelectItem(ag, ag.getAgente()));
                }
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getMessage());
                Logger.getLogger(MbMiniAgentes.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return lstAgentes;
    }
    
    public void setLstAgentes(ArrayList<SelectItem> lstAgentes) {
        this.lstAgentes = lstAgentes;
    }
    
}
