package proveedores.converters;

import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import proveedores.dao.DAOTipoTerceros;
import proveedores.dominio.TipoTercero;

/**
 *
 * @author jsolis
 */
public class TipoTerceroConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        TipoTercero tipoTercero = null;
        try {
            int idTipoTercero=Integer.parseInt(value);
            if(idTipoTercero == 0) {
                tipoTercero=new TipoTercero();
            } else {
                DAOTipoTerceros dao=new DAOTipoTerceros();
                tipoTercero=dao.obtenerTipoTercero(idTipoTercero);
            }
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_TipoTercero_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return tipoTercero;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            TipoTercero tipoTercero = (TipoTercero) value;
            val = Integer.toString(tipoTercero.getIdTipoTercero());
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_TipoTercero_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
    
}
