package producto2;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import org.primefaces.context.RequestContext;
import producto2.dao.DAOGrupos;
import producto2.dominio.Grupo;

/**
 *
 * @author jesc
 */
@Named(value = "mbGrupo")
@SessionScoped
public class MbGrupo implements Serializable {
    private Grupo grupo;
    private ArrayList<Grupo> grupos;
    private ArrayList<SelectItem> listaGrupos;
    @ManagedProperty(value = "#{mbSubGrupo}")
    private MbSubGrupo mbSubGrupo;
    private DAOGrupos dao;
    
    public MbGrupo() {
        this.mbSubGrupo=new MbSubGrupo();
        this.inicializaLocales();
    }
    
    public void inicializar() {
        this.inicializaLocales();
        this.mbSubGrupo.inicializar(0);
    }
    
    private void inicializaLocales() {
        this.grupo=new Grupo();
        this.setListaGrupos(null);
    }
    
    public void cargaListaSubGrupos() {
        this.mbSubGrupo.cargaListaSubGrupos(this.grupo.getIdGrupo());
    }
    
    private void cargarListaGrupos() {
        boolean ok=false;
        this.listaGrupos=new ArrayList<SelectItem>();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "cargarListaGrupos");
        try {
            this.dao=new DAOGrupos();
            Grupo g0=new Grupo(0, 0, "SELECCCIONE");
            this.listaGrupos.add(new SelectItem(g0, g0.toString()));
            for(Grupo g:this.dao.obtenerGrupos()) {
                this.listaGrupos.add(new SelectItem(g, g.toString()));
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
    
    public boolean eliminar() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "eliminar: mbGrupo");
        try {
            this.dao = new DAOGrupos();
            this.dao.eliminar(this.grupo.getIdGrupo());
//            this.grupos = this.dao.obtenerGrupos();
//            this.grupo = new Grupo();
            ok = true;
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
        context.addCallbackParam("okGrupo", ok);
        return ok;
    }

    public boolean grabar() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "grabar: mbGrupo");
        if (this.grupo.getGrupo().isEmpty()) {
            fMsg.setDetail("Se requiere el grupo");
        } else {
            try {
                this.dao = new DAOGrupos();
                if (this.grupo.getIdGrupo() == 0) {
                    this.grupo.setIdGrupo(this.dao.agregar(this.grupo));
                } else {
                    this.dao.modificar(this.grupo);
                }
                this.grupos = dao.obtenerGrupos();
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
        context.addCallbackParam("okGrupo", ok);
        return ok;
    }
    
    public void nuevo() {
        this.grupo.setIdGrupo(0);
        this.grupo.setCodigo(0);
        this.grupo.setGrupo("");
    }
    
    public void copia(Grupo g) {
        this.grupo.setIdGrupo(g.getIdGrupo());
        this.grupo.setCodigo(g.getCodigo());
        this.grupo.setGrupo(g.getGrupo());
    }

    public int obtenerUltimoGrupo() {
        int ultimo = 0;
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "obtenerUltimoGrupo");
        try {
            this.dao = new DAOGrupos();
            ultimo = this.dao.obtenerUltimoCodigoGrupo();
            ok = true;
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
        //context.addCallbackParam("okGrupo", ok);
        return ultimo;
    }

    public ArrayList<Grupo> obtenerGrupos() {
        boolean ok = false;
        this.grupos = new ArrayList<Grupo>();
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "obtenerGrupos");
        try {
            this.dao = new DAOGrupos();
            this.grupos = this.dao.obtenerGrupos();
            ok = true;
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
        context.addCallbackParam("okGrupo", ok);
        return this.grupos;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public ArrayList<Grupo> getGrupos() {
        return grupos;
    }

    public void setGrupos(ArrayList<Grupo> grupos) {
        this.grupos = grupos;
    }

    public ArrayList<SelectItem> getListaGrupos() {
        if(this.listaGrupos==null) {
            this.cargarListaGrupos();
        }
        return listaGrupos;
    }

    public void setListaGrupos(ArrayList<SelectItem> listaGrupos) {
        this.listaGrupos = listaGrupos;
    }

    public MbSubGrupo getMbSubGrupo() {
        return mbSubGrupo;
    }

    public void setMbSubGrupo(MbSubGrupo mbSubGrupo) {
        this.mbSubGrupo = mbSubGrupo;
    }
}
