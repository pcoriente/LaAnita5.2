package producto2;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import org.primefaces.context.RequestContext;
import producto2.dao.DAOTipos;
import producto2.dominio.Tipo;

/**
 *
 * @author jesc
 */
@Named(value = "mbTipo")
@SessionScoped
public class MbTipo implements Serializable {
    private Tipo tipo;
    private ArrayList<Tipo> tipos;
    private ArrayList<SelectItem> listaTipos;
    private DAOTipos dao;
    
    public MbTipo() {
        this.inicializa();
    }
    
    public void inicializar() {
        this.inicializa();
    }
    
    private void inicializa() {
        this.tipo=new Tipo();
        this.setListaTipos(null);
    }
    
    public ArrayList<Tipo> obtenerTipos() {
        boolean ok = false;
        this.tipos=new ArrayList<Tipo>();
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "");
        try {
            this.dao = new DAOTipos();
            this.tipos = this.dao.obtenerTipos();
            ok = true;
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if(!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okTipo", ok);
        return this.tipos;
    }
    
    private void cargaListaTipos() {
        boolean ok=false;
        this.listaTipos=new ArrayList<SelectItem>();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "");
        try {
            this.dao = new DAOTipos();
            Tipo t0=new Tipo(0, "SELECCIONE");
            this.listaTipos.add(new SelectItem(t0, t0.toString()));
            for(Tipo t: this.dao.obtenerTipos()) {
                this.listaTipos.add(new SelectItem(t, t.toString()));
            }
            ok = true;
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if(!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public ArrayList<Tipo> getTipos() {
        return tipos;
    }

    public void setTipos(ArrayList<Tipo> tipos) {
        this.tipos = tipos;
    }

    public ArrayList<SelectItem> getListaTipos() {
        if(this.listaTipos==null) {
            this.cargaListaTipos();
        }
        return listaTipos;
    }

    public void setListaTipos(ArrayList<SelectItem> listaTipos) {
        this.listaTipos = listaTipos;
    }
}
