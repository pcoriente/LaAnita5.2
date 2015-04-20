package tiendas.converters;

import Message.Mensajes;
import java.sql.SQLException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.naming.NamingException;
import tiendas.dao.DAOTiendas;
import tiendas.to.TOTienda;

/**
 *
 * @author jesc
 */
public class MiniTiendaConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        int idTienda=Integer.parseInt(value);
        TOTienda to=null;
        if(idTienda==0) {
            to=new TOTienda();
        } else {
            try {
                DAOTiendas dao = new DAOTiendas();
                to=dao.obtenerTienda(idTienda);
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
            }
        }
        return to;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        TOTienda to=(TOTienda)value;
        return Integer.toString(to.getIdTienda());
    }
}
