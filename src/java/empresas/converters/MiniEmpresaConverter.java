package empresas.converters;

import empresas.dao.DAOMiniEmpresas;
import empresas.dominio.MiniEmpresa;
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
public class MiniEmpresaConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        MiniEmpresa mini = null;
        try {
            int idMini=Integer.parseInt(value);
            if(idMini==0) {
                mini=new MiniEmpresa();
            } else {
                DAOMiniEmpresas dao=new DAOMiniEmpresas();
                mini=dao.obtenerMiniEmpresa(idMini);
            }
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Pais_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return mini;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            MiniEmpresa mini = (MiniEmpresa) value;
            val = Integer.toString(mini.getIdEmpresa());
        }catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Pedido_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
}
