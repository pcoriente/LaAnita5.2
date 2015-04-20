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
import producto2.dao.DAOPresentaciones;
import producto2.dominio.Presentacion;

/**
 *
 * @author jesc
 */
@Named(value = "mbPresentacion")
@SessionScoped
public class MbPresentacion implements Serializable {
    private Presentacion presentacion;
    private ArrayList<Presentacion> presentaciones;
    private ArrayList<SelectItem> listaPresentaciones;
    private DAOPresentaciones dao;
    
    public MbPresentacion() {
        this.inicializa();
    }
    
    public void inicializar() {
        this.inicializa();
    }
    
    private void inicializa() {
        this.presentacion=new Presentacion();
        this.setListaPresentaciones(null);
    }
    
    public boolean eliminar() {
        boolean ok=false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao=new DAOPresentaciones();
            this.dao.eliminar(this.presentacion.getIdPresentacion());
            ok=true;
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if(!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okPresentacion", ok);
        return ok;
    }
    
    public boolean grabar() {
        boolean ok=false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        if(this.presentacion.getPresentacion().isEmpty()) {
            fMsg.setDetail("Se requiere la presentación");
        } else if(this.presentacion.getAbreviatura().isEmpty()) {
            fMsg.setDetail("Se requiere la abreviatura de la presentación");
        } else {
            try {
                this.dao = new DAOPresentaciones();
                if(this.presentacion.getIdPresentacion()==0) {
                    this.presentacion.setIdPresentacion(this.dao.agregar(this.presentacion));
                } else {
                    this.dao.modificar(this.presentacion);
                }
                ok=true;
            } catch (NamingException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getMessage());
            } catch (SQLException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
        if(!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okPresentacion", ok);
        return ok;
    }
    
    public void nueva() {
        this.presentacion.setIdPresentacion(0);
        this.presentacion.setPresentacion("");
        this.presentacion.setAbreviatura("");
    }
    
    public void copia(Presentacion presentacion) {
        this.presentacion.setIdPresentacion(presentacion.getIdPresentacion());
        this.presentacion.setPresentacion(presentacion.getPresentacion());
        this.presentacion.setAbreviatura(presentacion.getAbreviatura());
    }
    
    private void cargaPresentaciones() {
        boolean ok=false;
        this.listaPresentaciones = new ArrayList<SelectItem>();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "");
        try {
            Presentacion p0 = new Presentacion(0, "SELECCIONE", "");
            this.listaPresentaciones.add(new SelectItem(p0, p0.toString()));
            
            this.dao = new DAOPresentaciones();
            for (Presentacion p : this.dao.obtenerPresentaciones()) {
                this.listaPresentaciones.add(new SelectItem(p, p.toString()));
            }
            ok=true;
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if(!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }

    public Presentacion getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(Presentacion presentacion) {
        this.presentacion = presentacion;
    }

    public ArrayList<Presentacion> getPresentaciones() {
        return presentaciones;
    }

    public void setPresentaciones(ArrayList<Presentacion> presentaciones) {
        this.presentaciones = presentaciones;
    }

    public ArrayList<SelectItem> getListaPresentaciones() {
        if(this.listaPresentaciones==null) {
            this.cargaPresentaciones();
        }
        return listaPresentaciones;
    }

    public void setListaPresentaciones(ArrayList<SelectItem> listaPresentaciones) {
        this.listaPresentaciones = listaPresentaciones;
    }
}
