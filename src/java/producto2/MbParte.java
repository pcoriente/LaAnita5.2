package producto2;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
//import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import org.primefaces.context.RequestContext;
import producto2.dao.DAOPartes;
import producto2.dominio.Parte;

/**
 *
 * @author jesc
 */
@Named(value = "mbParte")
@SessionScoped
public class MbParte implements Serializable {
    private Parte parte;
//    private ArrayList<SelectItem> listaPartes;
    private DAOPartes dao;
    
    public MbParte() {
        this.parte=new Parte();
    }
    
    public void nueva() {
        if(this.parte!=null) {
            this.parte.setIdParte(0);
            this.parte.setParte("");
        } else {
            this.parte=new Parte();
        }
    }
    
    public ArrayList<Parte> completePartes(String query) {
        boolean ok=false;
        ArrayList<Parte> partes=null;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "completePartes");
        try {
            this.dao = new DAOPartes();
             partes = this.dao.completePartes(query);
            ok=true;
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if(!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        return partes;
    }
    
    public boolean eliminar(int idProducto) {
        boolean ok=false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "eliminar: mbParte");
        try {
            if(this.parte.getIdParte()!=0) {
                this.dao=new DAOPartes();
                this.dao.eliminar(this.parte.getIdParte(), idProducto);
            }
            this.parte=new Parte();
            ok=true;
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if(!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okParte", ok);
        return ok;
    }
    
    public boolean grabar() {
        boolean ok=false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "grabar: mbParte");
        if(this.parte==null || this.parte.getParte().equals("")) {
            fMsg.setDetail("Se requiere identificar la parte");
        } else {
            try {
                this.dao = new DAOPartes();
                if(this.parte.getIdParte()==0) {
                    this.parte.setIdParte(this.dao.agregar(this.parte.getParte()));
                } else {
                    this.dao.modificar(this.parte);
                }
                ok=true;
            } catch (NamingException ex) {
                fMsg.setDetail(ex.getMessage());
            } catch (SQLException ex) {
                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
        if(!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okParte", ok);
        return ok;
    }

    public Parte getParte() {
        return parte;
    }

    public void setParte(Parte parte) {
        this.parte = parte;
    }
//
//    public ArrayList<SelectItem> getListaPartes() {
//        return listaPartes;
//    }
//
//    public void setListaPartes(ArrayList<SelectItem> listaPartes) {
//        this.listaPartes = listaPartes;
//    }
}
