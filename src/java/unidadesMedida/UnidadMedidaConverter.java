package unidadesMedida;

import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/**
 *
 * @author JULIOS
 */
public class UnidadMedidaConverter implements Converter{

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        UnidadMedida unidad=null;
        try {
            int idUnidad=Integer.parseInt(value);
            if(idUnidad==0) {
                unidad=new UnidadMedida(0, "", "");
            } else {
                DAOUnidadesMedida dao=new DAOUnidadesMedida();
                unidad=dao.obtenerUnidad(idUnidad);
            }
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_UnidadesMedidaConverter_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return unidad;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            UnidadMedida unidad=(UnidadMedida) value;
            val=Integer.toString(unidad.getIdUnidadMedida());
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_UnidadesMedidaConverter_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
    
}
