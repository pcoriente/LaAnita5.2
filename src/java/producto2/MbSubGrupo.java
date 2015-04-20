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
import producto2.dao.DAOSubGrupos;
import producto2.dominio.SubGrupo;

/**
 *
 * @author jesc
 */
@Named(value = "mbSubGrupo")
@SessionScoped
public class MbSubGrupo implements Serializable {
    private SubGrupo subGrupo;
    private ArrayList<SelectItem> listaSubGrupos;
    private ArrayList<SubGrupo> subGrupos;
    private DAOSubGrupos dao;
    
    public MbSubGrupo() {
        this.inicializaLocales(0);
    }
    
    public void nuevo() {
        this.subGrupo.setIdSubGrupo(0);
        this.subGrupo.setSubGrupo("");
    }
    
    public void inicializar(int idGrupo) {
        this.inicializaLocales(idGrupo);
    }
    
    private void inicializaLocales(int idGrupo) {
        this.subGrupo = new SubGrupo();
        this.cargaListaSubGrupos(idGrupo);
    }
    
    public boolean eliminar(int idSubGrupo) {
        boolean ok=false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao = new DAOSubGrupos();
            this.dao.eliminar(this.subGrupo.getIdSubGrupo());
            this.subGrupos=this.dao.obtenerSubGrupos(idSubGrupo);
            this.subGrupo=new SubGrupo(0, "");
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
        context.addCallbackParam("okSubGrupo", ok);
        return ok;
    }
    
    public boolean grabar(int idGrupo) {
        boolean ok=false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        if(this.subGrupo.getSubGrupo().isEmpty()) {
            fMsg.setDetail("Se requiere el SubGrupo");
        } else {
            try {
                this.dao = new DAOSubGrupos();
                if(this.subGrupo.getIdSubGrupo()==0) {
                    this.subGrupo.setIdSubGrupo(this.dao.agregar(this.subGrupo, idGrupo));
                } else {
                    this.dao.modificar(this.subGrupo);
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
        context.addCallbackParam("okSubGrupo", ok);
        return ok;
    }
    
    public void copia(SubGrupo s) {
        this.subGrupo.setIdSubGrupo(s.getIdSubGrupo());
        this.subGrupo.setSubGrupo(s.getSubGrupo());
    }
    
    public void cargaListaSubGrupos(int idGrupo) {
        boolean ok=false;
        this.listaSubGrupos=new ArrayList<SelectItem>();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "");
        try {
            this.dao=new DAOSubGrupos();
            SubGrupo sg0=new SubGrupo(0,"SELECCIONE");
            this.listaSubGrupos.add(new SelectItem(sg0, sg0.toString()));
            for(SubGrupo sg:this.dao.obtenerSubGrupos(idGrupo)) {
                this.listaSubGrupos.add(new SelectItem(sg, sg.toString()));
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
    
    public SubGrupo getSubGrupo() {
        return subGrupo;
    }

    public void setSubGrupo(SubGrupo subGrupo) {
        this.subGrupo = subGrupo;
    }

    public ArrayList<SubGrupo> getSubGrupos() {
        return subGrupos;
    }

    public void setSubGrupos(ArrayList<SubGrupo> subGrupos) {
        this.subGrupos = subGrupos;
    }

    public ArrayList<SelectItem> getListaSubGrupos() {
        return listaSubGrupos;
    }

    public void setListaSubGrupos(ArrayList<SelectItem> listaSubGrupos) {
        this.listaSubGrupos = listaSubGrupos;
    }
}
