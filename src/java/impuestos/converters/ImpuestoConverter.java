package impuestos.converters;

import impuestos.dao.DAOImpuestos;
import impuestos.dominio.Impuesto;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

public class ImpuestoConverter implements Converter{
   
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Impuesto impuesto=null;
        try {
            int idImpuesto=Integer.parseInt(value);
            if(idImpuesto==0) {
                impuesto=new Impuesto(0, "", false, 0, false, false);
            } else {
                DAOImpuestos dao = new DAOImpuestos();
                impuesto=dao.obtenerImpuesto(idImpuesto);
            }
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Impuesto_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return impuesto;
    }


    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            Impuesto impuesto=(Impuesto) value;
            val=Integer.toString(impuesto.getIdImpuesto());
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Impuesto_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
    
    
    
}
