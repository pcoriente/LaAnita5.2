package contactos.converters;

import contactos.dao.DAOTelefonos;
import contactos.dominio.Telefono;
import contactos.dominio.TelefonoTipo;
import java.sql.SQLException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.naming.NamingException;

/**
 *
 * @author jsolis
 */
public class TipoConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "");
        TelefonoTipo tipo = null;
        try {
            int idTipo=Integer.parseInt(value);
            if(idTipo==0) {
                tipo=new TelefonoTipo(false);
            } else {
                DAOTelefonos dao=new DAOTelefonos();
                tipo = dao.obtenerTipo(idTipo);
            }
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
            throw new ConverterException(fMsg);
        }  catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            throw new ConverterException(fMsg);
        }
        return tipo;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        TelefonoTipo t = (TelefonoTipo) value;
        String val = Integer.toString(t.getIdTipo());
        return val;
    }
    
}
