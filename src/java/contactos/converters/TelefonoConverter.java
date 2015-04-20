package contactos.converters;

import contactos.dao.DAOContactos;
import contactos.dao.DAOTelefonos;
import contactos.dominio.Contacto;
import contactos.dominio.Telefono;
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
public class TelefonoConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "");
        Telefono telefono = null;
        try {
            int idTelefono = Integer.parseInt(value);
            if (idTelefono == 0) {
                telefono = new Telefono();
            } else {
                DAOTelefonos dao = new DAOTelefonos();
                telefono = dao.obtenerTelefono(idTelefono);
            }
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
            throw new ConverterException(fMsg);
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            throw new ConverterException(fMsg);
        }
        return telefono;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = "";
        try {
            Telefono t = (Telefono) value;
            if (t != null) {
                val = Integer.toString(t.getIdTelefono());
            }
        } catch (Exception e) {
//            System.err.println(e);
        }
        return val;
    }
}
