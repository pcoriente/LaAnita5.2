package contactos;

import contactos.dao.DAOTelefonos;
import contactos.dominio.Telefono;
import contactos.dominio.TelefonoTipo;
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

/**
 *
 * @author jsolis
 */
@Named(value = "mbTelefonos")
@SessionScoped
public class MbTelefonos implements Serializable {

    private boolean celular;
    private Telefono telefono = new Telefono();
    private ArrayList<Telefono> telefonos;
    private ArrayList<SelectItem> listaTelefonos;
    private TelefonoTipo tipo = new TelefonoTipo(false);
    private ArrayList<SelectItem> listaTipos;
    private DAOTelefonos dao;

    public MbTelefonos() {
        this.telefono = new Telefono();
        this.tipo = new TelefonoTipo(false);
        telefonos = new ArrayList<Telefono>();
        listaTelefonos = new ArrayList<SelectItem>();
    }

    public boolean eliminarTipo(int idTipo) {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao = new DAOTelefonos();
            this.dao.eliminarTipo(idTipo);
            ok = true;
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        } catch (Exception ex) {
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okTelefonoTipo", ok);
        return ok;
    }

    public boolean grabarTipo() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            if (this.tipo.getTipo().isEmpty()) {
                fMsg.setDetail("Se requiere la descripción del tipo de teléfono");
            } else {
                this.dao = new DAOTelefonos();
                if (this.tipo.getIdTipo() == 0) {
                    tipo.setCelular(celular);
                    this.tipo.setIdTipo(this.dao.agregarTipo(this.tipo));
                } else {
                    this.dao.modificarTipo(this.tipo);
                }
                ok = true;
            }
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okTelefonoTipo", ok);
        return ok;
    }

    public TelefonoTipo copiaTipo(TelefonoTipo t1) {
        TelefonoTipo t = new TelefonoTipo(t1.isCelular());
        t.setIdTipo(t1.getIdTipo());
        t.setTipo(t1.getTipo());
        return t;
    }

    public void cargaTipos() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao = new DAOTelefonos();
            ArrayList<TelefonoTipo> tipos = this.dao.obtenerTipos(this.celular);
            TelefonoTipo t0 = new TelefonoTipo(this.celular);
            t0.setTipo("Nuevo Tipo");
            this.listaTipos = new ArrayList<SelectItem>();
            this.listaTipos.add(new SelectItem(t0, t0.toString()));
            for (TelefonoTipo t : tipos) {
                this.listaTipos.add(new SelectItem(t, t.toString()));
            }
            ok = true;
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }

    public boolean eliminar(int idTelefono) {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao = new DAOTelefonos();
            this.dao.eliminar(idTelefono);
            ok = true;
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okTelefono", ok);
        return ok;
    }

    public boolean grabar(int idContacto) {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            if (this.telefono.getTipo().getIdTipo() == 0) {
                fMsg.setDetail("Se requiere el tipo de telefono !!!");
            } else if (this.telefono.getTelefono().isEmpty()) {
                fMsg.setDetail("Se requiere el número teléfono !!!");
            } else {
                this.dao = new DAOTelefonos();
                if (this.telefono.getIdTelefono() == 0) {
                    this.telefono.setIdTelefono(this.dao.agregar(this.telefono, idContacto));
                } else {
                    this.dao.modificar(this.telefono);
                }
                ok = true;
            }
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okTelefono", ok);
        return ok;
    }

    public boolean validarTelefonos() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        if (this.telefono.getTipo().getIdTipo() == 0) {
            fMsg.setDetail("Se requiere el tipo de telefono !!!");
        } else if (this.telefono.getTelefono().isEmpty()) {
            fMsg.setDetail("Se requiere el número teléfono !!!");
        } else {
            ok = true;
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okTelefono", ok);
        return ok;
    }

    public ArrayList<Telefono> obtenerTelefonos(int idContacto) throws NamingException, SQLException {
//        boolean ok=false;
//        ArrayList<Telefono> tels=new ArrayList<Telefono>();
//        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
//        try {
        this.dao = new DAOTelefonos();
        ArrayList<Telefono> tels = this.dao.obtenerTelefonos(idContacto);
//            ok=true;
//        } catch (SQLException ex) {
//            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
//            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
//        } catch (NamingException ex) {
//            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
//            fMsg.setDetail(ex.getMessage());
//        }
//        if(!ok) {
//            FacesContext.getCurrentInstance().addMessage(null, fMsg);
//        }
        return tels;
    }

    public void cargaTelefonos(int idContacto) {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao = new DAOTelefonos();
            this.telefonos = this.dao.obtenerTelefonos(idContacto);
            Telefono t0 = new Telefono();
            t0.setTelefono("Nuevo Teléfono");
            this.listaTelefonos = new ArrayList<SelectItem>();
            this.listaTelefonos.add(new SelectItem(t0, t0.toString()));
            for (Telefono t : this.telefonos) {
                this.listaTelefonos.add(new SelectItem(t, t.toString()));
            }
            ok = true;
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }

    public Telefono copia(Telefono t1) {
        Telefono t = new Telefono();
        t.setIdTelefono(t1.getIdTelefono());
        t.setLada(t1.getLada());
        t.setTelefono(t1.getTelefono());
        t.setExtension(t1.getExtension());
        t.getTipo().setIdTipo(t1.getTipo().getIdTipo());
        t.getTipo().setTipo(t1.getTipo().getTipo());
        t.getTipo().setCelular(t1.getTipo().isCelular());
        return t;
    }

    public Telefono getTelefono() {
        return telefono;
    }

    public void setTelefono(Telefono telefono) {
        this.telefono = telefono;
    }

    public ArrayList<Telefono> getTelefonos() {
        return telefonos;
    }

    public void setTelefonos(ArrayList<Telefono> telefonos) {
        this.telefonos = telefonos;
    }

    public ArrayList<SelectItem> getListaTelefonos() {
        return listaTelefonos;
    }

    public void setListaTelefonos(ArrayList<SelectItem> listaTelefonos) {
        this.listaTelefonos = listaTelefonos;
    }

    public TelefonoTipo getTipo() {
        return tipo;
    }

    public void setTipo(TelefonoTipo tipo) {
        this.tipo = tipo;
    }

    public ArrayList<SelectItem> getListaTipos() {
        return listaTipos;
    }

    public void setListaTipos(ArrayList<SelectItem> listaTipos) {
        this.listaTipos = listaTipos;
    }

    public boolean isCelular() {
        return celular;
    }

    public void setCelular(boolean celular) {
        this.celular = celular;
    }
}
