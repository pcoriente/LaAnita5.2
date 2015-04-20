package movimientos.converters;

import entradas.dao.DAOMovimientos;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import movimientos.dominio.MovimientoTipo;

/**
 *
 * @author jesc
 */
public class MovimientoTipoConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        MovimientoTipo tipo=null;
        try {
            int idTipo=Integer.parseInt(value);
            if(idTipo == 0) {
                tipo=new MovimientoTipo();
            } else {
                DAOMovimientos dao=new DAOMovimientos();
                tipo=dao.obtenerMovimientoTipo(idTipo);
            }
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_MovimientoTipo_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return tipo;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            MovimientoTipo tipo = (MovimientoTipo) value;
            val = Integer.toString(tipo.getIdTipo());
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Clasificacion_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
    
}
