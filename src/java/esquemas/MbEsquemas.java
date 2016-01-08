/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esquemas;

import Message.Mensajes;
import esquemas.DAO.DAOEsquemas;
import esquemas.Dominio.EsquemaNegociacion;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;

/**
 *
 * @author Torres
 */
@Named(value = "mbEsquemas")
@SessionScoped
public class MbEsquemas implements Serializable {

    private ArrayList<SelectItem> lst = null;
    private int valorEsquema;

    public MbEsquemas() {
    }

    public ArrayList<SelectItem> getLst() {
        if (lst == null) {
            lst = new ArrayList<>();
            try {
                DAOEsquemas dao = new DAOEsquemas();
                EsquemaNegociacion esquema = new EsquemaNegociacion();
                esquema.setIdEsquema(0);
                esquema.setEsquema("Seleccione un esquema");
                lst.add(new SelectItem(esquema.getIdEsquema(), esquema.getEsquema()));
                for (EsquemaNegociacion lst1 : dao.dameEsquemas()) {
                    EsquemaNegociacion esq = new EsquemaNegociacion();
                    esq.setIdEsquema(lst1.getIdEsquema());
                    esq.setEsquema(lst1.getEsquema());
                    lst.add(new SelectItem(esq.getIdEsquema(), esq.getEsquema()));
                }
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
                Logger.getLogger(MbEsquemas.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getMessage());
                Logger.getLogger(MbEsquemas.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return lst;
    }

    public void setLst(ArrayList<SelectItem> lst) {
        this.lst = lst;
    }

    public int getValorEsquema() {
        return valorEsquema;
    }

    public void setValorEsquema(int valorEsquema) {
        this.valorEsquema = valorEsquema;
    }

}
