package proveedores.converters;

import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import proveedores.dao.DAOClasificaciones;
import proveedores.dominio.SubClasificacion;

/**
 *
 * @author jsolis
 */
public class SubClasificacionConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        SubClasificacion subClasificacion = null;
        try {
            int idSubClasificacion=Integer.parseInt(value);
            if(idSubClasificacion == 0) {
                subClasificacion=new SubClasificacion();
            } else {
                DAOClasificaciones dao=new DAOClasificaciones();
                subClasificacion=dao.obtenerSubClasificacion(idSubClasificacion);
            }
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_SubClasificacion_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return subClasificacion;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            SubClasificacion subClasificacion = (SubClasificacion) value;
            val = Integer.toString(subClasificacion.getIdSubClasificacion());
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_SubClasificacion_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
    
}
