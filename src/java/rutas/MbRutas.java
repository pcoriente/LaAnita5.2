/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rutas;

import Message.Mensajes;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.naming.NamingException;
import org.primefaces.context.RequestContext;
import rutas.daoRutas.DAORutas;
import rutas.dominio.Ruta;

/**
 *
 * @author Usuario
 */
@Named(value = "mbRutas")
@SessionScoped
public class MbRutas implements Serializable {

    private ArrayList<SelectItem> lstRuta = null;
    private Ruta ruta = new Ruta();
    private Ruta cmbRuta = new Ruta();
    private DAORutas dao;

    /**
     * Creates a new instance of MbRutas
     */
    public MbRutas() {
    }

    public boolean validar() {
        boolean ok = true;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        if (ruta.getRuta().equals("")) {
            ok = false;
            fMsg.setDetail("Se requiere la calle !!");
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("ok", ok);
        return ok;
    }
    
    public Ruta obtenerRuta(int idRuta) {
        Ruta r=null;
        try {
            this.dao=new DAORutas();
            r=this.dao.dameRuta(idRuta);
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
        }
        return r;
    }
    
    private void cargaRutas() {
        try {
            this.lstRuta = new ArrayList<SelectItem>();
            Ruta r0 = new Ruta();
            r0.setRuta("Nueva Ruta");
            this.lstRuta.add(new SelectItem(r0, r0.getRuta()));

            this.dao = new DAORutas();
            for (Ruta r : this.dao.dameListaRutas()) {
                lstRuta.add(new SelectItem(r, r.getRuta()));
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
        }
    }

    public ArrayList<SelectItem> getLstRuta() {
        if (this.lstRuta == null) {
            this.cargaRutas();
        }
        return lstRuta;
    }

    public void setLstRuta(ArrayList<SelectItem> lstRuta) {
        this.lstRuta = lstRuta;
    }

    public Ruta getRuta() {
        return ruta;
    }

    public void setRuta(Ruta ruta) {
        this.ruta = ruta;
    }

    public Ruta getCmbRuta() {
        return cmbRuta;
    }

    public void setCmbRuta(Ruta cmbRuta) {
        this.cmbRuta = cmbRuta;
    }

}
