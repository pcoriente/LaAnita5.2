package contactos.converters;

import cedis.dominio.MiniCedis;
import contactos.dao.DAOContactos;
import contactos.dominio.Contacto;
import java.sql.SQLException;
import java.util.ResourceBundle;
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
public class ContactoConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "");
        Contacto contacto = null;
        try {
            int idContacto=Integer.parseInt(value);
            if(idContacto==0) {
                contacto=new Contacto();
            } else {
                DAOContactos dao=new DAOContactos();
                contacto = dao.obtenerContacto(idContacto);
            }
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
            throw new ConverterException(fMsg);
        }  catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            throw new ConverterException(fMsg);
        }
        return contacto;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
//        String val = null;
//        try {
            Contacto c = (Contacto) value;
            String val = Integer.toString(c.getIdContacto());
//        } catch(Throwable ex) {
//            ResourceBundle bundle = ResourceBundle.getBundle("messages");
//            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_MiniCedis_getAsString"));
//            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
//            throw new ConverterException(msg);
//        }
        return val;
    }
    
}
