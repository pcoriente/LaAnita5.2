package almacenes.converters;

import almacenes.dao.DAOAlmacenes;
import almacenes.dominio.Almacen;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/**
 *
 * @author jsolis
 */
public class AlmacenConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Almacen almacen = null;
        try {
            int idAlmacen = Integer.parseInt(value);
            if (idAlmacen == 0) {
                almacen = new Almacen();
                almacen.setAlmacen("Seleccione un Almacén");
            } else {
                DAOAlmacenes dao = new DAOAlmacenes();
                almacen = dao.obtenerAlmacen(idAlmacen);
            }
        } catch (Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Almacen_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return almacen;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            Almacen almacen = (Almacen) value;
            val = Integer.toString(almacen.getIdAlmacen());
        } catch (Exception ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Almacen_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
    
}
