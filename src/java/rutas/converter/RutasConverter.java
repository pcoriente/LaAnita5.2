/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rutas.converter;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.naming.NamingException;
import rutas.daoRutas.DAORutas;
import rutas.dominio.Ruta;

/**
 *
 * @author Usuario
 */
public class RutasConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        int id = 0;
        id = Integer.parseInt(value);
        Ruta ruta = null;
        if (id == 0) {
            ruta = new Ruta();
            ruta.setIdRuta(0);
            ruta.setRuta("Nueva Ruta");
        } else {
            try {
                DAORutas dao = new DAORutas();
                ruta = dao.dameRuta(id);
            } catch (NamingException ex) {
                Logger.getLogger(RutasConverter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(RutasConverter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return ruta;

    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        Ruta ruta = (Ruta) value;
        return Integer.toString(ruta.getIdRuta());
    }
}
