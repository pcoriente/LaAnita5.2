package clientes.converters;

import Message.Mensajes;
import clientes.dao.DAOTiendasFormatos;
import clientes.dominio.TiendaFormato;
import java.sql.SQLException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.naming.NamingException;

/**
 *
 * @author jesc
 */
public class TiendasFormatosConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        TiendaFormato formato=null;
        int idFormato = Integer.parseInt(value);
        if (idFormato == 0) {
            formato = new TiendaFormato();
        } else {
            try {
                DAOTiendasFormatos dao=new DAOTiendasFormatos();
                formato=dao.obtenerFormato(idFormato);
            } catch(SQLException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
        return formato;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        TiendaFormato formato=(TiendaFormato) value;
        return Integer.toString(formato.getIdFormato());
    }
    
}
