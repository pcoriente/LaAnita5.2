package impuestos.converters;

import impuestos.dao.DAOGrupos;
import impuestos.dominio.ImpuestoGrupo;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/**
 *
 * @author JULIOS
 */
public class ImpuestoGrupoConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        ImpuestoGrupo grupo=null;
        try {
            int idGrupo=Integer.parseInt(value);
            if(idGrupo==0) {
                grupo=new ImpuestoGrupo(0, "");
            } else {
                DAOGrupos dao = new DAOGrupos();
                grupo=dao.obtenerGrupo(idGrupo);
            }
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_ImpuestoGrupo_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return grupo;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            ImpuestoGrupo grupo=(ImpuestoGrupo) value;
            val=Integer.toString(grupo.getIdGrupo());
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_ImpuestoGrupo_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
}
