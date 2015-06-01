/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package monedas;

import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.naming.NamingException;

@FacesConverter("monedaConverterDaap")
public class MonedaConverterDaap implements Converter {

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {

        if (value != null && value.trim().length() > 0) {

            Moneda moneda;
            try {
                int idMoneda = Integer.parseInt(value);
                DAOMonedas dao;
                dao = new DAOMonedas();
                moneda = dao.obtenerMoneda(idMoneda);
                return moneda;

            } catch (NumberFormatException e) {
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not a valid theme."));
            } catch (SQLException ex) {
                Logger.getLogger(MonedaConverterDaap.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NamingException ex) {
                Logger.getLogger(MonedaConverterDaap.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return null;
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object object) {
        if (object != null) {

            String Mon = String.valueOf(((Moneda) object).getIdMoneda());
            return Mon;
        } else {
            return null;
        }
    }
}
//            } catch (Throwable ex) {
//                ResourceBundle bundle = ResourceBundle.getBundle("messages");
//                FacesMessage msg = new FacesMessage(bundle.getString("Mensaje_conversion_MiniProveedor_getAsObject"));
//                msg.setSeverity(FacesMessage.SEVERITY_ERROR);
//                throw new ConverterException(msg);