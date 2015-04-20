package producto2.converters;

import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import producto2.dao.DAOUpcs;
import producto2.dominio.Upc;

/**
 *
 * @author jesc
 */
public class UpcConverter implements Converter {
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Upc upc = null;
        try {
            int pos=value.indexOf("|");
            int idProducto=Integer.parseInt(value.substring(pos+1));
            value=value.substring(0, pos);
            if(value.equals("SELECCIONE")) {
                upc=new Upc("SELECCIONE", idProducto, false);
            } else {
                DAOUpcs dao=new DAOUpcs();
                upc=dao.obtenerUpc(value);
            }
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Upc_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return upc;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val=null;
        try {
            Upc upc = (Upc) value;
            val=upc.getUpc()+"|"+Integer.toString(upc.getIdProducto());
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Upc_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
}
