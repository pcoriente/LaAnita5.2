package impuestos.converters;

import impuestos.dao.DAOZonas;
import impuestos.dominio.ImpuestoZona;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

public class ImpuestoZonaConverter implements Converter {
    
     @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        ImpuestoZona zona=null;
        try {
            int idZona=Integer.parseInt(value);
            if(idZona==0) {
                zona=new ImpuestoZona(0, "");
            } else {
                DAOZonas dao = new DAOZonas();
                zona=dao.obtenerZona(idZona);
            }
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_ImpuestoZona_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return zona;
    }


    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            ImpuestoZona zona=(ImpuestoZona) value;
            val=Integer.toString(zona.getIdZona());
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_ImpuestoZona_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
    
    
    
    
}
