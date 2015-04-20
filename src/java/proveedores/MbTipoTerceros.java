package proveedores;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import org.primefaces.context.RequestContext;
import proveedores.dao.DAOTipoTerceros;
import proveedores.dominio.TipoTercero;

/**
 *
 * @author jsolis
 */
@Named(value = "mbTipoTerceros")
@SessionScoped
public class MbTipoTerceros implements Serializable {
    private TipoTercero tipoTercero;
    private ArrayList<TipoTercero> tipoTerceros;
    private DAOTipoTerceros dao;
    
    public MbTipoTerceros() {
        this.tipoTercero=new TipoTercero();
    }
    
    public boolean eliminar() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao = new DAOTipoTerceros();
            this.dao.eliminar(this.tipoTercero.getIdTipoTercero());
            this.tipoTerceros = this.dao.obtenerTipoTerceros();
            this.tipoTercero=new TipoTercero();
            ok = true;
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (Exception ex) {
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okTipoTercero", ok);
        return ok;
    }
    
    public boolean grabar() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        if (this.tipoTercero.getTercero().isEmpty()) {
            fMsg.setDetail("Se requiere la descripcion del tipo de tercero");
        } else {
            try {
                this.dao = new DAOTipoTerceros();
                if (this.tipoTercero.getIdTipoTercero() == 0) {
                    this.tipoTercero.setIdTipoTercero(this.dao.agregar(this.tipoTercero));
                } else {
                    this.dao.modificar(this.tipoTercero);
                }
                this.tipoTerceros = dao.obtenerTipoTerceros();
                ok = true;
            } catch (NamingException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getMessage());
            } catch (SQLException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okTipoTercero", ok);
        return ok;
    }
    
    public TipoTercero copia(TipoTercero t) {
        TipoTercero tt=new TipoTercero();
        tt.setIdTipoTercero(t.getIdTipoTercero());
        tt.setTipoTercero(t.getTipoTercero());
        tt.setTercero(t.getTercero());
        return tt;
    }
    
    public ArrayList<TipoTercero> obtenerTipoTerceros() {
        ArrayList<TipoTercero> lstTipoTerceros=new ArrayList<TipoTercero>();
        try {
            this.dao=new DAOTipoTerceros();
            lstTipoTerceros=this.dao.obtenerTipoTerceros();
        } catch (NamingException ex) {
            Logger.getLogger(MbClasificaciones.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(MbClasificaciones.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lstTipoTerceros;
    }
    
    public TipoTercero obtenerTipoTercero(int idTipoTercero) {
        TipoTercero t=new TipoTercero();
        try {
            this.dao=new DAOTipoTerceros();
            t=this.dao.obtenerTipoTercero(idTipoTercero);
        } catch (NamingException ex) {
            Logger.getLogger(MbClasificaciones.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(MbClasificaciones.class.getName()).log(Level.SEVERE, null, ex);
        }
        return t;
    }

    public TipoTercero getTipoTercero() {
        return tipoTercero;
    }

    public void setTipoTercero(TipoTercero tipoTercero) {
        this.tipoTercero = tipoTercero;
    }

    public ArrayList<TipoTercero> getTipoTerceros() {
        return tipoTerceros;
    }

    public void setTipoTerceros(ArrayList<TipoTercero> tipoTerceros) {
        this.tipoTerceros = tipoTerceros;
    }
}
