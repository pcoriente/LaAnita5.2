/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruposBancos;

import Message.Mensajes;
import gruposBancos.DAO.DAOGruposBancos;
import gruposBancos.Dominio.GruposBancos;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.Dependent;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.naming.NamingException;
import producto2.dao.DAOGrupos;

/**
 *
 * @author Usuario
 */
@Named(value = "mbGruposBancos")
@Dependent
public class MbGruposBancos {

    /**
     * Creates a new instance of MbGruposBancos
     */
    private ArrayList<SelectItem> lstGruposBancos;
    private GruposBancos grupos = new GruposBancos();

    public MbGruposBancos() {
    }

    public GruposBancos getGrupos() {
        return grupos;
    }

    public void setGrupos(GruposBancos grupos) {
        this.grupos = grupos;
    }

    public ArrayList<SelectItem> getLstGruposBancos() {
        if (lstGruposBancos == null) {
            lstGruposBancos = new ArrayList<SelectItem>();
            GruposBancos gruposBancos = new GruposBancos();
            gruposBancos.setIdBanco(0);
            gruposBancos.setMedioPago("Seleccione el pago");
            lstGruposBancos.add(new SelectItem(gruposBancos, gruposBancos.getMedioPago()));
        }
        return lstGruposBancos;
    }

    public void setLstGruposBancos(ArrayList<SelectItem> lstGruposBancos) {
        this.lstGruposBancos = lstGruposBancos;
    }

    public void cargarGruposBancos(int idGrupoCte) {
        if (lstGruposBancos == null) {
            lstGruposBancos = new ArrayList<SelectItem>();
            try {
                GruposBancos grupo = new GruposBancos();
                grupo.setIdBanco(0);
                grupo.setMedioPago("Seleccione el pago");
                lstGruposBancos.add(new SelectItem(grupo, grupo.getMedioPago()));
                DAOGruposBancos dao = new DAOGruposBancos();
                for (GruposBancos gruposBancos : dao.dameGruposBancos(idGrupoCte)) {
                    lstGruposBancos.add(new SelectItem(gruposBancos, gruposBancos.toString()));
                }
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
                Logger.getLogger(MbGruposBancos.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getMessage());
                Logger.getLogger(MbGruposBancos.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
