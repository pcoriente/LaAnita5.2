package almacenes;

import Message.Mensajes;
import almacenes.dao.DAOMiniAlmacenes;
import almacenes.dominio.MiniAlmacen;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;

/**
 *
 * @author jesc
 */
@Named(value = "mbMiniAlmacenes")
@SessionScoped
public class MbMiniAlmacenes implements Serializable {
    private MiniAlmacen almacen;
    private ArrayList<SelectItem> listaAlmacenes;
    private boolean sinEmpresa;
    private DAOMiniAlmacenes dao;
    
    public MbMiniAlmacenes() {
        this.almacen=new MiniAlmacen();
    }
    
    public MiniAlmacen obtenerAlmacen(int idAlmacen) {
        MiniAlmacen mini=null;
        try {
            this.dao=new DAOMiniAlmacenes();
            mini=this.dao.obtenerAlmacen(idAlmacen);
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
        }
        return mini;
    }
    
    public void cargaListaAlmacenesCedis() {
        this.listaAlmacenes=new ArrayList<SelectItem>();
        this.listaAlmacenes.add(new SelectItem(new MiniAlmacen(), "Seleccione un almacen"));
        try {
            this.dao=new DAOMiniAlmacenes();
            for(MiniAlmacen a: this.dao.obtenerAlmacenesCedis()) {
                this.listaAlmacenes.add(new SelectItem(a, a.toString()));
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
        }
    }
    
    public void cargaListaAlmacenesCedis(int idCedis) {
        boolean ok=false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        this.listaAlmacenes=new ArrayList<SelectItem>();
        try {
            MiniAlmacen a0=new MiniAlmacen();
            a0.setIdAlmacen(0);
            a0.setAlmacen("Seleccione un almacen");
            this.listaAlmacenes.add(new SelectItem(a0, a0.toString()));
            
            this.dao = new DAOMiniAlmacenes();
            for (MiniAlmacen a: this.dao.obtenerAlmacenesCedis(idCedis)) {
                listaAlmacenes.add(new SelectItem(a, a.toString()));
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
    
    public void cargaListaAlmacenes(int idEmpresa, int idCedis) {
        boolean ok=false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        this.listaAlmacenes=new ArrayList<SelectItem>();
        try {
            MiniAlmacen a0=new MiniAlmacen();
            a0.setIdAlmacen(0);
            a0.setAlmacen("Seleccione un almacen");
            this.listaAlmacenes.add(new SelectItem(a0, a0.toString()));
            
            this.dao = new DAOMiniAlmacenes();
            for (MiniAlmacen a: this.dao.obtenerAlmacenes(idEmpresa, idCedis)) {
                listaAlmacenes.add(new SelectItem(a, a.toString()));
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
    
    public void nuevoAlmacen() {
        this.almacen=new MiniAlmacen();
    }

    public MiniAlmacen getAlmacen() {
        return almacen;
    }

    public void setAlmacen(MiniAlmacen almacen) {
        this.almacen = almacen;
    }

    public ArrayList<SelectItem> getListaAlmacenes() {
        if(this.listaAlmacenes==null) {
            if(this.sinEmpresa) {
                this.cargaListaAlmacenesCedis();
            } else {
                this.listaAlmacenes=new ArrayList<SelectItem>();
                MiniAlmacen a0=new MiniAlmacen();
                a0.setIdAlmacen(0);
                a0.setAlmacen("Seleccione un almacen");
                this.listaAlmacenes.add(new SelectItem(a0, a0.toString()));
            }
        }
        return listaAlmacenes;
    }

    public void setListaAlmacenes(ArrayList<SelectItem> listaAlmacenes) {
        this.listaAlmacenes = listaAlmacenes;
    }

    public boolean isSinEmpresa() {
        return sinEmpresa;
    }

    public void setSinEmpresa(boolean sinEmpresa) {
        this.sinEmpresa = sinEmpresa;
    }
}
