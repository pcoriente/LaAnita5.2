package producto2.converters;

import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import producto2.dao.DAOTipos;
import producto2.dominio.Tipo;

/**
 *
 * @author jesc
 */
public class TipoConverter implements Converter {
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Tipo tipo = null;
        try {
            int idTipo=Integer.parseInt(value);
            if(idTipo==0) {
                tipo=new Tipo();
            } else {
                DAOTipos dao=new DAOTipos();
                tipo=dao.obtenerTipo(idTipo);
            }
       } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_ProductoTipo_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return tipo;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            Tipo tipo = (Tipo) value;
            val = Integer.toString(tipo.getIdTipo());
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_ProductoTipo_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
}
