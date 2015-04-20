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
import proveedores.dao.DAOClasificaciones;
import proveedores.dominio.Clasificacion;
import proveedores.dominio.SubClasificacion;

/**
 *
 * @author jsolis
 */
@Named(value = "mbClasificaciones")
@SessionScoped
public class MbClasificaciones implements Serializable {
    private Clasificacion clasificacion;
    private ArrayList<Clasificacion> clasificaciones;
    //private ArrayList<Clasificacion> filtrados;
    //private SelectItem[] listaClasificaciones;
    private SubClasificacion subClasificacion;
    private ArrayList<SubClasificacion> subClasificaciones;
    private DAOClasificaciones dao;
    
    public MbClasificaciones() {
        this.clasificacion=new Clasificacion();
        this.subClasificacion=new SubClasificacion();
    }
    
    public boolean eliminar() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao = new DAOClasificaciones();
            this.dao.eliminar(this.clasificacion.getIdClasificacion());
            this.clasificaciones = this.dao.obtenerClasificaciones();
            this.clasificacion=new Clasificacion(0, "");
            ok = true;
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }catch (Exception ex) {
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okClasificacion", ok);
        return ok;
    }
    
    public boolean grabar() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        if (this.clasificacion.getClasificacion().isEmpty()) {
            fMsg.setDetail("Se requiere la descripcion de la clasificacion");
        } else {
            try {
                this.dao = new DAOClasificaciones();
                if (this.clasificacion.getIdClasificacion() == 0) {
                    this.clasificacion.setIdClasificacion(this.dao.agregar(this.clasificacion));
                } else {
                    this.dao.modificar(this.clasificacion);
                }
                this.clasificaciones = dao.obtenerClasificaciones();
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
        context.addCallbackParam("okClasificacion", ok);
        return ok;
    }

    public Clasificacion copia(Clasificacion c) {
        Clasificacion cl=new Clasificacion();
        cl.setIdClasificacion(c.getIdClasificacion());
        cl.setClasificacion(c.getClasificacion());
        return cl;
    }
    
    public ArrayList<Clasificacion> obtenerClasificaciones() {
        boolean ok = false;
        ArrayList<Clasificacion> lstClasificaciones=new ArrayList<Clasificacion>();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao=new DAOClasificaciones();
            lstClasificaciones=this.dao.obtenerClasificaciones();
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
        return lstClasificaciones;
    }
    
    public Clasificacion obtenerClasificacion(int idClasificacion) {
        boolean ok = false;
        Clasificacion c=new Clasificacion();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao=new DAOClasificaciones();
            c=this.dao.obtenerClasificacion(idClasificacion);
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
    
    public boolean eliminarSubClasificacion(int idClasificacion) {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao = new DAOClasificaciones();
            this.dao.eliminarSubClasificacion(this.subClasificacion.getIdSubClasificacion());
            this.subClasificaciones = this.dao.obtenerSubClasificaciones(idClasificacion);
            this.subClasificacion=new SubClasificacion(0, "");
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
        context.addCallbackParam("okSubClasificacion", ok);
        return ok;
    }
    
    public boolean grabarSubClasificacion(int idClasificacion) {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        if (this.subClasificacion.getSubClasificacion().isEmpty()) {
            fMsg.setDetail("Se requiere la descripcion de la subClasificacion");
        } else {
            try {
                this.dao = new DAOClasificaciones();
                if (this.subClasificacion.getIdSubClasificacion() == 0) {
                    this.subClasificacion.setIdSubClasificacion(this.dao.agregarSubClasificacion(this.subClasificacion, idClasificacion));
                } else {
                    this.dao.modificarSubClasificacion(this.subClasificacion);
                }
                this.subClasificaciones = this.dao.obtenerSubClasificaciones(idClasificacion);
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
        context.addCallbackParam("okSubClasificacion", ok);
        return ok;
    }
    
    public SubClasificacion copia(SubClasificacion c) {
        SubClasificacion sc=new SubClasificacion();
        sc.setIdSubClasificacion(c.getIdSubClasificacion());
        sc.setSubClasificacion(c.getSubClasificacion());
        return sc;
    }
    
    public ArrayList<SubClasificacion> obtenerSubClasificaciones(int idClasificacion) {
        boolean ok = false;
        ArrayList<SubClasificacion> lstSubClasificaciones=new ArrayList<SubClasificacion>();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao=new DAOClasificaciones();
            lstSubClasificaciones=this.dao.obtenerSubClasificaciones(idClasificacion);
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
        return lstSubClasificaciones;
    }
    
    public SubClasificacion obtenerSubClasificacion(int idSubClasificacion) {
        boolean ok = false;
        SubClasificacion s=new SubClasificacion();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao=new DAOClasificaciones();
            s=this.dao.obtenerSubClasificacion(idSubClasificacion);
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
        return s;
    }

    public Clasificacion getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(Clasificacion clasificacion) {
        this.clasificacion = clasificacion;
    }

    public ArrayList<Clasificacion> getClasificaciones() {
        return clasificaciones;
    }

    public void setClasificaciones(ArrayList<Clasificacion> clasificaciones) {
        this.clasificaciones = clasificaciones;
    }

    public SubClasificacion getSubClasificacion() {
        return subClasificacion;
    }

    public void setSubClasificacion(SubClasificacion subClasificacion) {
        this.subClasificacion = subClasificacion;
    }

    public ArrayList<SubClasificacion> getSubClasificaciones() {
        return subClasificaciones;
    }

    public void setSubClasificaciones(ArrayList<SubClasificacion> subClasificaciones) {
        this.subClasificaciones = subClasificaciones;
    }
}
