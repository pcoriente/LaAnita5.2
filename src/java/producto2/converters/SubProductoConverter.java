package producto2.converters;

import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import producto2.dao.DAOSubProductos;
import producto2.dominio.SubProducto;

/**
 *
 * @author jesc
 */
public class SubProductoConverter implements Converter {
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        SubProducto subProducto=null;
        try {
            int idSubProducto=Integer.parseInt(value);
            if(idSubProducto==0) {
                subProducto=new SubProducto();
            } else {
                DAOSubProductos dao=new DAOSubProductos();
                subProducto=dao.obtenerSubProducto(idSubProducto);
            }
        } catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_SubProducto_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return subProducto;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            SubProducto subProducto = (SubProducto) value;
            val = Integer.toString(subProducto.getIdProducto());
        }catch(Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_SubProducto_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
}
