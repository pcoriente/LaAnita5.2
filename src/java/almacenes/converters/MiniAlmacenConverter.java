package almacenes.converters;

import almacenes.dao.DAOMiniAlmacenes;
import almacenes.dominio.MiniAlmacen;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/**
 *
 * @author jesc
 */
public class MiniAlmacenConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        MiniAlmacen mini = null;
        try {
            int idAlmacen = Integer.parseInt(value);
            if (idAlmacen == 0) {
                mini = new MiniAlmacen();
                mini.setAlmacen("Seleccione un Almacén");
            } else {
                DAOMiniAlmacenes dao = new DAOMiniAlmacenes();
                mini = dao.obtenerAlmacen(idAlmacen);
            }
        } catch (Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_MiniAlmacen_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return mini;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            MiniAlmacen mini = (MiniAlmacen) value;
            val = Integer.toString(mini.getIdAlmacen());
        } catch (Exception ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_MiniAlmacen_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
    
}
