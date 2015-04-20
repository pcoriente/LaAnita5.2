package producto2.converters;

import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import producto2.dao.DAOEmpaques;
import producto2.dominio.Empaque;

/**
 *
 * @author jesc
 */
public class EmpaqueConverter implements Converter {
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Empaque empaque=null;
        try {
            int idEmpaque=Integer.parseInt(value);
            if(idEmpaque==0) {
                empaque=new Empaque(0, "", "");
            } else {
                DAOEmpaques dao=new DAOEmpaques();
                empaque=dao.obtenerEmpaque(idEmpaque);
            }
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Empaque_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return empaque;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            Empaque empaque=(Empaque) value;
            val=Integer.toString(empaque.getIdEmpaque());
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Empaque_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
}
