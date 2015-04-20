package monedas;

import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

public class MonedasConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Moneda moneda = null;
        try {
            int idMoneda=Integer.parseInt(value);
            if(idMoneda == 0) {
                moneda=new Moneda();
            } else {
                DAOMonedas dao=new DAOMonedas();
                moneda=dao.obtenerMoneda(idMoneda);
            }
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_MiniProveedor_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return moneda;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            Moneda moneda = (Moneda) value;
            val = Integer.toString(moneda.getIdMoneda());
        } catch(Throwable ex) {
//            ResourceBundle bundle = ResourceBundle.getBundle("messages");
//            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_MiniProveedor_getAsString"));
//            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
//            throw new ConverterException(msg);
        }
        return val;
    }
}