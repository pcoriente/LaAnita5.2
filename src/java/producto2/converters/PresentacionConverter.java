package producto2.converters;

import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import producto2.dao.DAOPresentaciones;
import producto2.dominio.Presentacion;

/**
 *
 * @author jesc
 */
public class PresentacionConverter implements Converter {
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Presentacion presentacion=null;
        try {
            int idPresentacion=Integer.parseInt(value);
            if(idPresentacion==0) {
                presentacion=new Presentacion(0, "", "");
            } else {
                DAOPresentaciones dao=new DAOPresentaciones();
                presentacion=dao.obtenerPresentacion(idPresentacion);
            }
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_PresentacionConverter_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return presentacion;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            Presentacion presentacion=(Presentacion) value;
            val=Integer.toString(presentacion.getIdPresentacion());
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_PresentacionConverter_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
}
