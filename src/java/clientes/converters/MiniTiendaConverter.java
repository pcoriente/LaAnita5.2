package clientes.converters;

import clientes.dao.DAOTiendas;
import clientes.dominio.MiniTienda;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 *
 * @author jesc
 */
public class MiniTiendaConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        MiniTienda tienda = null;
        try {
            int idTienda=Integer.parseInt(value);
            if(idTienda == 0) {
                tienda=new MiniTienda();
            } else {
                DAOTiendas dao=new DAOTiendas();
                tienda=dao.obtenerMiniTienda(idTienda);
            }
        } catch(Throwable ex) {
            System.err.println(ex);
//            ResourceBundle bundle = ResourceBundle.getBundle("messages");
//            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_MiniProveedor_getAsObject"));
//            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
//            throw new ConverterException(msg);
        }
        return tienda;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            MiniTienda tienda = (MiniTienda) value;
            val = Integer.toString(tienda.getIdTienda());
        } catch(Throwable ex) {
         //   ResourceBundle bundle = ResourceBundle.getBundle("messages");
        //    FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_MiniProveedor_getAsString"));
        //    msg.setSeverity(FacesMessage.SEVERITY_ERROR);
        //    throw new ConverterException(msg);
        }
        return val;
    }
    
}
