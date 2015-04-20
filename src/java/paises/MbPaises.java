/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package paises;

import Message.Mensajes;
import direccion.dao.DAOPais;
import direccion.dominio.Pais;
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
@Named(value = "mbPaises")
@SessionScoped
public class MbPaises implements Serializable {

    /**
     * Creates a new instance of MbPaises
     */
    private ArrayList<Pais> lstPais = null;
    private ArrayList<Pais> filterPais;
    private Pais pais = new Pais();

    public MbPaises() {
    }

    public ArrayList<Pais> getLstPais() {
        if (lstPais == null) {
            try {
                DAOPais dao = new DAOPais();
                lstPais = dao.obtenerPaises();
            } catch (NamingException ex) {
                Message.Mensajes.mensajeError(ex.getMessage());
                Logger.getLogger(MbPaises.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getMessage());
                Logger.getLogger(MbPaises.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return lstPais;
    }

    public boolean validarPais() {
        boolean ok = false;
        if (pais.getPais().equals("")) {
            Mensajes.mensajeAlert("Se requiere el nombre del Pais");
        } else {
            ok = true;
        }
        return ok;

    }

//    public void guardar() {
//        boolean ok = validarPais();
//        if (ok == true) {
//            DAOPais dao;
//            try {
//                dao = new DAOPais();
//                dao.guardarPais(pais);
//            } catch (NamingException ex) {
//                Logger.getLogger(MbPaises.class.getName()).log(Level.SEVERE, null, ex);
//            }
//
//        }
//
//    }

    public void setLstPais(ArrayList<Pais> lstPais) {
        this.lstPais = lstPais;
    }

    public ArrayList<Pais> getFilterPais() {
        return filterPais;
    }

    public void setFilterPais(ArrayList<Pais> filterPais) {
        this.filterPais = filterPais;
    }

    public Pais getPais() {
        return pais;
    }

    public void setPais(Pais pais) {
        this.pais = pais;
    }
}
