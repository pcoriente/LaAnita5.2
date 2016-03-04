package envios.converters;

import envios.dao.DAOEnvios;
import envios.dominio.EnvioTraspaso;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/**
 *
 * @author jesc
 */
public class EnvioTraspasoConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        EnvioTraspaso envioTraspaso = null;
        try {
            int idSolicitud = Integer.parseInt(value);
            if (idSolicitud == 0) {
                envioTraspaso = new EnvioTraspaso();
//                envioTraspaso.setIdEnvio(0);
                envioTraspaso.setIdSolicitud(0);
                envioTraspaso.setAlmacen("Seleccione un almacén");
            } else {
                DAOEnvios dao = new DAOEnvios();
                envioTraspaso = dao.obtenerTraspaso(idSolicitud);
            }
        } catch (Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Factura_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return envioTraspaso;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            EnvioTraspaso envioTraspaso = (EnvioTraspaso) value;
            val = Integer.toString(envioTraspaso.getIdSolicitud());
        } catch (Exception ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Factura_getAsString"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return val;
    }
}
