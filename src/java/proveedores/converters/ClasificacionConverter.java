package proveedores.converters;

import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import proveedores.dao.DAOClasificaciones;
import proveedores.dominio.Clasificacion;

/**
 *
 * @author jsolis
 */
public class ClasificacionConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Clasificacion clasificacion = null;
        try {
            int idClasificacion=Integer.parseInt(value);
            if(idClasificacion == 0) {
                clasificacion=new Clasificacion();
            } else {
                DAOClasificaciones dao=new DAOClasificaciones();
                clasificacion=dao.obtenerClasificacion(idClasificacion);
            }
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Clasificacion_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return clasificacion;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            Clasificacion clasificacion = (Clasificacion) value;
            val = Integer.toString(clasificacion.getIdClasificacion());
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Clasificacion_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
    
}
