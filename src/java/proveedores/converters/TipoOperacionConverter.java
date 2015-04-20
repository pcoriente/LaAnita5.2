package proveedores.converters;

import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import proveedores.dao.DAOTipoOperaciones;
import proveedores.dominio.TipoOperacion;

/**
 *
 * @author jsolis
 */
public class TipoOperacionConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        TipoOperacion tipoOperacion = null;
        try {
            int idTipoOperacion=Integer.parseInt(value);
            if(idTipoOperacion == 0) {
                tipoOperacion=new TipoOperacion();
            } else {
                DAOTipoOperaciones dao=new DAOTipoOperaciones();
                tipoOperacion=dao.obtenerTipoOperacion(idTipoOperacion);
            }
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_TipoOperacion_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return tipoOperacion;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            TipoOperacion tipoOperacion = (TipoOperacion) value;
            val = Integer.toString(tipoOperacion.getIdTipoOperacion());
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_TipoOperacion_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
    
}
