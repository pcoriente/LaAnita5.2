/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mbMenuClientesGrupos.Converter;

import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.naming.NamingException;
import menuClientesGrupos.dao.DAOClientesGrupo;
import menuClientesGrupos.dominio.ClienteGrupo;
import org.codehaus.groovy.tools.shell.ParseCode;

/**
 *
 * @author Usuario
 */
public class MenuClientesConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        ClienteGrupo cg = null;
        try {
            int id = Integer.parseInt(value);
            DAOClientesGrupo dao = new DAOClientesGrupo();
            if (id == 0) {
                cg = new ClienteGrupo();
                cg.setIdGrupoCte(0);
                cg.setGrupoCte("Nuevo Cliente");
            } else {
                try {
                    cg = dao.dameClientesGrupo(id);
                } catch (SQLException ex) {
                    ResourceBundle bundle = ResourceBundle.getBundle("messages");
                    FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Pais_getAsObject"));
                    msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                    Logger.getLogger(MenuClientesConverter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (NamingException ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Pais_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            Logger.getLogger(MenuClientesConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cg;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        ClienteGrupo clientesGrupos = (ClienteGrupo) value;
        String dato = Integer.toString(clientesGrupos.getIdGrupoCte());
        return dato;
    }
}
