package clientes.converters;

import Message.Mensajes;
import clientes.dao.DAOClientes;
import clientes.dao.DAOMiniClientes;
import clientes.dominio.MiniCliente;
import clientes.to.TOCliente;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.naming.NamingException;

/**
 *
 * @author jesc
 */
public class MiniClienteConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        TOCliente toCliente = null;
        try {
            int idCliente=Integer.parseInt(value);
            if(idCliente == 0) {
                toCliente=new TOCliente();
            } else {
                DAOClientes dao=new DAOClientes();
                toCliente=dao.obtenerCliente(idCliente);
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
        }
        return toCliente;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            TOCliente toCliente = (TOCliente) value;
            val = Integer.toString(toCliente.getIdCliente());
        } catch(Throwable ex) {
         //   ResourceBundle bundle = ResourceBundle.getBundle("messages");
        //    FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_MiniProveedor_getAsString"));
        //    msg.setSeverity(FacesMessage.SEVERITY_ERROR);
        //    throw new ConverterException(msg);
        }
        return val;
    }
    
}
