package envios.converters;

import Message.Mensajes;
import envios.dao.DAOEnvios;
import envios.to.TOEnvio;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.naming.NamingException;

/**
 *
 * @author jesc
 */
public class EnvioConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        TOEnvio to=null;
        int id = Integer.parseInt(value);
        if (id == 0) {
            to = new TOEnvio();
        } else {
            try {
                DAOEnvios dao=new DAOEnvios();
                to=dao.obtenerEnvio(id);
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
                Logger.getLogger(EnvioConverter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
                Logger.getLogger(EnvioConverter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return to;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        TOEnvio to=(TOEnvio)value;
        return Integer.toString(to.getIdEnvio());
    }
}
