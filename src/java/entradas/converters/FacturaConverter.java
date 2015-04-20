package entradas.converters;

import entradas.dao.DAOFacturas;
import entradas.dominio.Factura;
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
public class FacturaConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Factura factura = null;
        try {
            int idFactura = Integer.parseInt(value);
            if (idFactura == 0) {
                factura = new Factura();
                factura.setSerie("");
                factura.setNumero("Seleccione");
            } else {
                DAOFacturas dao = new DAOFacturas();
                factura = dao.obtenerFactura(idFactura);
            }
        } catch (Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Factura_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return factura;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            Factura factura = (Factura) value;
            val = Integer.toString(factura.getIdFactura());
        } catch (Exception ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Factura_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
    
}
