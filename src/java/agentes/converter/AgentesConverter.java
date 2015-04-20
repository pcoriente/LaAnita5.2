/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentes.converter;

import Message.Mensajes;
import agentes.dao.DaoAgentes;
import agentes.dominio.Agente;
import java.sql.SQLException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.naming.NamingException;

/**
 *
 * @author Usuario
 */
public class AgentesConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        int idAgente = Integer.parseInt(value);
        Agente agente = null;
        if (idAgente == 0) {
            agente = new Agente();
        } else {
            try {
                DaoAgentes dao = new DaoAgentes();
                agente = dao.obtenerAgente(idAgente);
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
        return agente;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        Agente agentes = (Agente) value;
        return Integer.toString(agentes.getIdAgente());
    }
}
