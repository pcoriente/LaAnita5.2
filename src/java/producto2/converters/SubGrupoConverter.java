package producto2.converters;

import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import producto2.dao.DAOSubGrupos;
import producto2.dominio.SubGrupo;

/**
 *
 * @author jesc
 */
public class SubGrupoConverter implements Converter {
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        SubGrupo subGrupo = null;
        try {
            int idSubGrupo=Integer.parseInt(value);
            if(idSubGrupo == 0) {
                subGrupo=new SubGrupo(0, "SELECCIONE UN GRUPO");
            } else {
                DAOSubGrupos dao=new DAOSubGrupos();
                subGrupo=dao.obtenerSubGrupo(idSubGrupo);
            }
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_SubGrupo_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return subGrupo;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            SubGrupo subGrupo = (SubGrupo) value;
            val = Integer.toString(subGrupo.getIdSubGrupo());
        }catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_SubGrupo_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
}
