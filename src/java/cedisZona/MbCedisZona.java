/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cedisZona;

import Message.Mensajes;
import cedisZona.dao.DAOCedisZona;
import cedisZona.dominio.CedisZona;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;

/**
 *
 * @author PJGT
 */
@Named(value = "mbCedisZona")
@SessionScoped
public class MbCedisZona implements Serializable {

    /**
     * Creates a new instance of MbCedisZona
     */
    private CedisZona cedisZona = new CedisZona();
    private ArrayList<CedisZona> lstCedisZona = null;
    private CedisZona seleccionCedisZona = null;
    private ArrayList<CedisZona> filterCedisZona;

    public MbCedisZona() {
    }

    public CedisZona getCedisZona() {
        return cedisZona;
    }

    public void setCedisZona(CedisZona cedisZona) {
        this.cedisZona = cedisZona;
    }

    public boolean validar() {
        boolean ok = false;
        if (cedisZona.getZona().equals("")) {
            Mensajes.mensajeAlert("Se requiere una zona");
        } else {
            ok = true;
        }
        return ok;
    }

    public void guardar() {
        boolean ok = validar();
        if (ok == true) {
            try {
                DAOCedisZona dao = new DAOCedisZona();
                dao.guardarCedisZona(cedisZona);
                Mensajes.mensajeSucces("Exito! Nueva zona disponible");
                lstCedisZona = null;
            } catch (NamingException ex) {
                Mensajes.mensajeAlert(ex.getMessage());
                Logger.getLogger(MbCedisZona.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getMessage());
                Logger.getLogger(MbCedisZona.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public ArrayList<CedisZona> getLstCedisZona() {
        if (lstCedisZona == null) {
            lstCedisZona = new ArrayList<CedisZona>();
            try {
                DAOCedisZona dao = new DAOCedisZona();
                lstCedisZona = dao.dameListaCedisZona();
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
                Logger.getLogger(MbCedisZona.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getMessage());
                Logger.getLogger(MbCedisZona.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return lstCedisZona;
    }

    public void setLstCedisZona(ArrayList<CedisZona> lstCedisZona) {
        this.lstCedisZona = lstCedisZona;
    }

    public CedisZona getSeleccionCedisZona() {
        return seleccionCedisZona;
    }

    public void setSeleccionCedisZona(CedisZona seleccionCedisZona) {
        this.seleccionCedisZona = seleccionCedisZona;
    }

    public ArrayList<CedisZona> getFilterCedisZona() {
        return filterCedisZona;
    }

    public void setFilterCedisZona(ArrayList<CedisZona> filterCedisZona) {
        this.filterCedisZona = filterCedisZona;
    }
}
