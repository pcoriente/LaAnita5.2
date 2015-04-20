package proveedores.converters;

import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import proveedores.dao.DAOMiniProveedores;
import proveedores.dominio.MiniProveedor;

/**
 *
 * @author jsolis
 */
public class MiniProveedorConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        MiniProveedor proveedor = null;
        try {
            int idProveedor=Integer.parseInt(value);
            if(idProveedor == 0) {
                proveedor=new MiniProveedor();
            } else {
                DAOMiniProveedores dao=new DAOMiniProveedores();
                proveedor=dao.obtenerProveedor(idProveedor);
            }
        } catch(Throwable ex) {
            System.err.println(ex);
//            ResourceBundle bundle = ResourceBundle.getBundle("messages");
//            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_MiniProveedor_getAsObject"));
//            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
//            throw new ConverterException(msg);
        }
        return proveedor;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            MiniProveedor proveedor = (MiniProveedor) value;
            val = Integer.toString(proveedor.getIdProveedor());
        } catch(Throwable ex) {
         //   ResourceBundle bundle = ResourceBundle.getBundle("messages");
        //    FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_MiniProveedor_getAsString"));
        //    msg.setSeverity(FacesMessage.SEVERITY_ERROR);
        //    throw new ConverterException(msg);
        }
        return val;
    }
}
