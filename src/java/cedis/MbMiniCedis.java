package cedis;

import Message.Mensajes;
import cedis.dao.DAOMiniCedis;
import cedis.dominio.MiniCedis;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;

/**
 *
 * @author jsolis
 */
@Named(value = "mbMiniCedis")
@SessionScoped
public class MbMiniCedis implements Serializable {
    private MiniCedis cedis;
    private ArrayList<SelectItem> listaMiniCedis;
    private DAOMiniCedis dao;
    
    public MbMiniCedis() {
        this.inicializaLocales();
    }
    
    public void inicializar() {
        this.inicializaLocales();
    }
    
    private void inicializaLocales() {
        this.cedis=new MiniCedis();
        this.setListaMiniCedis(null);
    }
    
    public void obtenerCedisPlanta(Boolean planta) {
        this.listaMiniCedis = new ArrayList<>();
        try {
            this.cedis.setIdCedis(0);
            this.cedis.setCedis("Seleccione un cedis");
            this.listaMiniCedis.add(new SelectItem(this.cedis, this.cedis.toString()));
            
            this.dao=new DAOMiniCedis();
            for(MiniCedis mc : this.dao.obtenerCedisPlanta(planta)) {
                this.listaMiniCedis.add(new SelectItem(mc, mc.toString()));
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }
    
    public void obtenerDefaultCedis() {
        boolean ok=false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "obtenerCedis");
        try {
            this.dao=new DAOMiniCedis();
            this.cedis=this.dao.obtenerDefaultMiniCedis();
            ok=true;
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }
    
    public MiniCedis obtenerCedis(int idCedis) {
        boolean ok=false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "obtenerCedis");
        MiniCedis c=null;
        try {
            this.dao=new DAOMiniCedis();
            c=this.dao.obtenerMiniCedis(idCedis);
            ok=true;
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        return c;
    }
    
    public void cargaMiniCedisTodos() {
        this.listaMiniCedis=new ArrayList<>();
        try {
            MiniCedis p0 = new MiniCedis();
            p0.setIdCedis(0);
            p0.setCedis("Seleccione un CEDIS");
            SelectItem cero = new SelectItem(p0, p0.toString());
            this.listaMiniCedis.add(cero);
            
            this.dao=new DAOMiniCedis();
            for (MiniCedis m : this.dao.obtenerListaMiniCedisTodos()) {
                this.listaMiniCedis.add(new SelectItem(m, m.toString()));
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }
    
    public void cargaMiniCedisZona() {
        boolean ok=false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        this.listaMiniCedis=new ArrayList<>();
        try {
            MiniCedis p0 = new MiniCedis();
            p0.setIdCedis(0);
            p0.setCedis("Seleccione un CEDIS");
            SelectItem cero = new SelectItem(p0, p0.toString());
            listaMiniCedis.add(cero);
            
            this.dao=new DAOMiniCedis();
            for (MiniCedis m : this.dao.obtenerListaMiniCedisZona()) {
                listaMiniCedis.add(new SelectItem(m, m.toString()));
            }
            ok=true;
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }

    public MiniCedis getCedis() {
        return cedis;
    }

    public void setCedis(MiniCedis cedis) {
        this.cedis = cedis;
    }

    public ArrayList<SelectItem> getListaMiniCedis() {
        if(this.listaMiniCedis==null) {
            this.cargaMiniCedisZona();
        }
        return listaMiniCedis;
    }

    public void setListaMiniCedis(ArrayList<SelectItem> listaMiniCedis) {
        this.listaMiniCedis = listaMiniCedis;
    }
}
