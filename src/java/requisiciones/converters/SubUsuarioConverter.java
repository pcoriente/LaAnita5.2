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
import requisiciones.dao.DAOUsuarioRequisiciones;
import usuarios.dominio.Usuario;

/**
 *
 * @author daap
 */
public class SubUsuarioConverter implements Converter  {
    
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Usuario usu = null;
        try {
            int idUsuario=Integer.parseInt(value);
            if(idUsuario == 0) {
                usu=new Usuario(0, "SELECCIONE UN USUARO..");
            } else {
                DAOUsuarioRequisiciones dao=new DAOUsuarioRequisiciones();
                usu=dao.obtenerUsuarioConverter(idUsuario);
            }
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_SubUsuario_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return usu;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            Usuario usu = (Usuario) value;
            val = Integer.toString(usu.getId());
        }catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_SubUsuario_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
    
    
    
}
