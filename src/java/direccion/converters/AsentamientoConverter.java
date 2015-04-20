package direccion.converters;

import direccion.dao.DAOAsentamiento;
import direccion.dominio.Asentamiento;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/**
 *
 * @author Julio
 */
public class AsentamientoConverter implements Converter {
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Asentamiento asentamiento = null;
        try {
            if(value.equals("0")) {
                asentamiento=new Asentamiento();
                asentamiento.setCodAsentamiento("0");
                asentamiento.setCodigoPostal("");
                asentamiento.setTipo("");
                asentamiento.setAsentamiento("");
                asentamiento.setCodEstado("");
                asentamiento.setEstado("");
                asentamiento.setCodMunicipio("");
                asentamiento.setMunicipio("");
                asentamiento.setCiudad("");
            } else {
                DAOAsentamiento dao=new DAOAsentamiento();
                asentamiento=dao.obtener(value);
            }
        }catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Asentamiento_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return asentamiento;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            Asentamiento asentamiento = (Asentamiento) value;
            val = asentamiento.getCodAsentamiento();
        }catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Asentamiento_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
}
