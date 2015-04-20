package clasificaciones.converters;

import clasificaciones.dao.DAOClasificacion;
import clasificaciones.dominio.Grupo;
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
public class GrupoConverter implements Converter {
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Grupo grupo = null;
        try {
            int idGrupo=Integer.parseInt(value);
            if(idGrupo == 0) grupo=new Grupo(0, 0, "SELECCIONE UN GRUPO");
            else {
                DAOClasificacion dao=new DAOClasificacion();
                grupo=dao.obtenerGrupo(idGrupo);
            }
        }catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Grupo_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return grupo;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            Grupo grupo = (Grupo) value;
            val = Integer.toString(grupo.getIdGrupo());
        }catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Grupo_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
}
