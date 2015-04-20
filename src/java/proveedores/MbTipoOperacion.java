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
import proveedores.dao.DAOTipoOperaciones;
import proveedores.dao.DAOTipoTerceros;
import proveedores.dominio.TipoOperacion;
import proveedores.dominio.TipoTercero;

/**
 *
 * @author jsolis
 */
@Named(value = "mbTipoOperacion")
@SessionScoped
public class MbTipoOperacion implements Serializable {
    private TipoOperacion tipoOperacion;
    private ArrayList<TipoOperacion> tipoOperaciones;
    private DAOTipoOperaciones dao;
    
    public MbTipoOperacion() {
        this.tipoOperacion=new TipoOperacion();
    }
    
    public boolean eliminar() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao = new DAOTipoOperaciones();
            this.dao.eliminar(this.tipoOperacion.getIdTipoOperacion());
            this.tipoOperaciones = this.dao.obtenerTipoOperaciones();
            this.tipoOperacion=new TipoOperacion();
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
        context.addCallbackParam("okTipoOperacion", ok);
        return ok;
    }
    
    public boolean grabar() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        if (this.tipoOperacion.getOperacion().isEmpty()) {
            fMsg.setDetail("Se requiere la descripcion del tipo de operación");
        } else {
            try {
                this.dao = new DAOTipoOperaciones();
                if (this.tipoOperacion.getIdTipoOperacion() == 0) {
                    this.tipoOperacion.setIdTipoOperacion(this.dao.agregar(this.tipoOperacion));
                } else {
                    this.dao.modificar(this.tipoOperacion);
                }
                this.tipoOperaciones = dao.obtenerTipoOperaciones();
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
        context.addCallbackParam("okTipoOperacion", ok);
        return ok;
    }
    
    public TipoOperacion copia(TipoOperacion t) {
        TipoOperacion tt=new TipoOperacion();
        tt.setIdTipoOperacion(t.getIdTipoOperacion());
        tt.setTipoOperacion(t.getTipoOperacion());
        tt.setOperacion(t.getOperacion());
        return tt;
    }
    
    public ArrayList<TipoOperacion> obtenerTipoOperaciones() {
        ArrayList<TipoOperacion> lstTipoOperaciones=new ArrayList<TipoOperacion>();
        try {
            this.dao=new DAOTipoOperaciones();
            lstTipoOperaciones=this.dao.obtenerTipoOperaciones();
        } catch (NamingException ex) {
            Logger.getLogger(MbClasificaciones.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(MbClasificaciones.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lstTipoOperaciones;
    }
    
    public TipoOperacion obtenerTipoOperacion(int idTipoOperacion) {
        TipoOperacion t=new TipoOperacion();
        try {
            this.dao=new DAOTipoOperaciones();
            t=this.dao.obtenerTipoOperacion(idTipoOperacion);
        } catch (NamingException ex) {
            Logger.getLogger(MbClasificaciones.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(MbClasificaciones.class.getName()).log(Level.SEVERE, null, ex);
        }
        return t;
    }

    public TipoOperacion getTipoOperacion() {
        return tipoOperacion;
    }

    public void setTipoOperacion(TipoOperacion tipoOperacion) {
        this.tipoOperacion = tipoOperacion;
    }

    public ArrayList<TipoOperacion> getTipoOperaciones() {
        return tipoOperaciones;
    }

    public void setTipoOperaciones(ArrayList<TipoOperacion> tipoOperaciones) {
        this.tipoOperaciones = tipoOperaciones;
    }
}
