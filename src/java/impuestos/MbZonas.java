package impuestos;

import Message.Mensajes;
import impuestos.dao.DAOZonas;
import impuestos.dominio.ImpuestoZona;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.naming.NamingException;
import org.primefaces.context.RequestContext;

/**
 *
 * @author JULIOS
 */
@Named(value = "mbZonas")
@SessionScoped
public class MbZonas implements Serializable {

    private ImpuestoZona zona = new ImpuestoZona(0, "");
    private ArrayList<SelectItem> listaZonas;
    private DAOZonas dao;

    public MbZonas() {
        this.zona = new ImpuestoZona(0, "");
    }

    public boolean eliminar() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        if (this.zona == null || this.zona.getIdZona() == 0) {
            fMsg.setDetail("Se requiere la zona a eliminar !!");
        } else {
            try {
                this.dao = new DAOZonas();
                ok = this.dao.eliminar(this.zona.getIdZona());
                if (ok) {
                    this.zona = new ImpuestoZona(0, "");
                    this.cargarZonas();
                } else {
                    fMsg.setDetail("La zona no puede ser eliminada, actualmente está en uso !!");
                }
            } catch (SQLException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getMessage());
            }
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okEliminarZona", ok);
        return ok;
    }

    public boolean grabar() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        if (this.zona == null || this.zona.getZona().isEmpty()) {
            fMsg.setDetail("Se requiere la descripción de la zona !!");
        } else {
            try {
                this.dao = new DAOZonas();
                if (this.zona.getIdZona() == 0) {
                    this.zona.setIdZona(this.dao.agregar(zona));
                } else {
                    this.dao.modificar(zona);
                }
                this.cargarZonas();
                ok = true;
            } catch (SQLException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getMessage());
            }
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okGrabarZona", ok);
        return ok;
    }
    
    public ImpuestoZona obtenerZona(int idImpuestoZona) {
        ImpuestoZona z=null;
        try {
            this.dao=new DAOZonas();
            z=this.dao.obtenerZona(idImpuestoZona);
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        return z;
    }

    private void cargarZonas() {
        this.listaZonas = new ArrayList<SelectItem>();
        ImpuestoZona zone = new ImpuestoZona(0, "SELECCIONE UNA ZONA");
        this.listaZonas.add(new SelectItem(zone, zone.toString()));
        try {
            this.dao = new DAOZonas();
            ArrayList<ImpuestoZona> lstZonas = this.dao.obtenerZonas();
            for (ImpuestoZona z : lstZonas) {
                this.listaZonas.add(new SelectItem(z, z.toString()));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public ImpuestoZona getZona() {
        return zona;
    }

    public void setZona(ImpuestoZona zona) {
        this.zona = zona;
    }

    public ArrayList<SelectItem> getListaZonas() {
        if (this.listaZonas == null) {
            this.cargarZonas();
        }
        return listaZonas;
    }

    public void setListaZonas(ArrayList<SelectItem> listaZonas) {
        this.listaZonas = listaZonas;
    }

    public boolean validar() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        if (zona.getIdZona() == 0) {
            Mensajes.mensajeAlert("Error! Zona Requerida!!");
        } else {
            ok = true;
        }
        context.addCallbackParam("okEliminarZona", ok);
        return ok;
    }
}
