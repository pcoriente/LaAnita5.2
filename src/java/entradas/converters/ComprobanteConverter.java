package entradas.converters;

import entradas.dao.DAOComprobantes;
import entradas.to.TOComprobante;
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
public class ComprobanteConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        TOComprobante comprobante = null;
        try {
            int idComprobante = Integer.parseInt(value);
            if (idComprobante == 0) {
                comprobante = new TOComprobante();
                comprobante.setSerie("");
                comprobante.setNumero("Seleccione");
            } else {
                DAOComprobantes dao = new DAOComprobantes();
                comprobante = dao.obtenerComprobante(idComprobante);
            }
        } catch (Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Comprobante_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return comprobante;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            TOComprobante comprobante = (TOComprobante) value;
            val = Integer.toString(comprobante.getIdComprobante());
        } catch (Exception ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Comprobante_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
    
}
