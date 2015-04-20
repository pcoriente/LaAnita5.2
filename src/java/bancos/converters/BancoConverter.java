package bancos.converters;

import bancos.dao.DAOBancos;
import bancos.dominio.Banco;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

public class BancoConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Banco banco = null;
        try {
            int idBanco = Integer.parseInt(value);
            //System.err.println("El entero convertido de BANCO es " + idBanco);
            if (idBanco == 0) {
                banco = new Banco();
                banco.setIdBanco(0);
            } else {
                DAOBancos dao = new DAOBancos();
                banco = dao.obtener(idBanco);
                //System.err.println("El BANCO  de la clase converter es " + banco);
            }
        } catch (Throwable ex) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages");
            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Pais_getAsObject"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        return banco;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = null;
        try {
            Banco banco = (Banco) value;
            val = Integer.toString(banco.getIdBanco());
        } catch (Exception ex) {
//            ResourceBundle bundle = ResourceBundle.getBundle("messages");
//            FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_Pais_getAsString"));
//            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
//            throw new ConverterException(msg);
        }
        return val;
    }
}
