package cedis.converters;

import cedis.dao.DAOMiniCedis;
import cedis.dominio.MiniCedis;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/**
 *
 * @author Julio
 */
public class MiniCedisConverter implements Converter {
    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        MiniCedis cedis = null;
        try {
            int idCedis=Integer.parseInt(value);
            if(idCedis==0) {
                cedis=new MiniCedis();
                cedis.setIdCedis(0);
                //cedis.setCodigo("00");
                cedis.setCedis("Seleccione una bodega");
            } else {
                DAOMiniCedis dao=new DAOMiniCedis();
                cedis=dao.obtenerMiniCedis(idCedis);
            }
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_MiniCedis_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return cedis;
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object value) {
        String val = null;
        try {
            MiniCedis cedis = (MiniCedis) value;
            val = Integer.toString(cedis.getIdCedis());
        }catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_MiniCedis_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
    
}
