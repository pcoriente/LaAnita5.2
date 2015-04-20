/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package requisiciones.converters;

import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import requisiciones.dao.DAODepto;
import requisiciones.dominio.Depto;

/**
 *
 * @author daap
 */
public class DeptoConverter implements Converter {
    
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Depto dep = null;
        try {
            int idDepto=Integer.parseInt(value);
            if(idDepto == 0) {
                dep=new Depto(0, "SELECCIONE UN DEPTO..");
            } else {
                DAODepto dao=new DAODepto();
                dep=dao.obtenerDeptoConverter(idDepto);
            }
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Depto_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return dep;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            Depto dep = (Depto) value;
            val = Integer.toString(dep.getIdDepto());
        }catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Depto_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
    
    
    
    
}
