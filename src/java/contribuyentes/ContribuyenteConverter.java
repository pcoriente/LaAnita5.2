package contribuyentes;

import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import proveedores.dao.DAOClasificaciones;
import proveedores.dominio.Clasificacion;

/**
 *
 * @author jsolis
 */
public class ContribuyenteConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Contribuyente contribuyente = null;
        try {
            int idContribuyente=Integer.parseInt(value);
            if(idContribuyente == 0) {
                contribuyente=new Contribuyente();
            } else {
                DAOContribuyentes dao=new DAOContribuyentes();
                contribuyente=dao.obtenerContribuyente(idContribuyente);
            }
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Contribuyente_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return contribuyente;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            Contribuyente contribuyente = (Contribuyente) value;
            val = Integer.toString(contribuyente.getIdContribuyente());
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Contribuyente_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
    
}
