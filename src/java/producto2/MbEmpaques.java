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
import producto2.dao.DAOEmpaques;
import producto2.dominio.Empaque;

/**
 *
 * @author jesc
 */
@Named(value = "mbEmpaques")
@SessionScoped
public class MbEmpaques implements Serializable {
    private Empaque empaque;
    private ArrayList<Empaque> empaques;
    private ArrayList<SelectItem> listaEmpaques;
    private DAOEmpaques dao;
    
    public MbEmpaques() {
        this.inicializa();
    }
    
    private void cargaListaEmpaques() {
         boolean ok=false;
        this.listaEmpaques=new ArrayList<SelectItem>();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "");
        try {
            this.dao=new DAOEmpaques();
            Empaque e0=new Empaque(0, "SELECCIONE", "");
            this.listaEmpaques.add(new SelectItem(e0, e0.toString()));
            for(Empaque e:this.dao.obtenerEmpaques()) {
                this.listaEmpaques.add(new SelectItem(e, e.toString()));
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
        boolean ok=false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao=new DAOEmpaques();
            this.dao.eliminar(this.empaque.getIdEmpaque());
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
        context.addCallbackParam("okEmpaque", ok);
        return ok;
    }
    
    public boolean grabar() {
        boolean ok=false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "");
        if(this.empaque.getEmpaque().isEmpty()) {
            fMsg.setDetail("Se requiere la unidad de empaque");
        } else if(this.empaque.getAbreviatura().isEmpty()) {
            fMsg.setDetail("Se requiere la abreviatura de la unidad de empaque");
        } else {
            try {
                this.dao = new DAOEmpaques();
                if(this.empaque.getIdEmpaque()==0) {
                    this.empaque.setIdEmpaque(this.dao.agregar(this.empaque));
                } else {
                    this.dao.modificar(this.empaque);
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
        context.addCallbackParam("okEmpaque", ok);
        return ok;
    }
    
    public void copia(Empaque e) {
        this.empaque.setIdEmpaque(e.getIdEmpaque());
        this.empaque.setEmpaque(e.getEmpaque());
        this.empaque.setAbreviatura(e.getAbreviatura());
    }
    
    public void nuevo() {
        this.empaque.setIdEmpaque(0);
        this.empaque.setEmpaque("");
        this.empaque.setAbreviatura("");
    }
    
    public void inicializar() {
        this.inicializa();
    }
    
    private void inicializa() {
        this.empaque=new Empaque();
        this.setListaEmpaques(null);
    }

    public Empaque getEmpaque() {
        return empaque;
    }

    public void setEmpaque(Empaque empaque) {
        this.empaque = empaque;
    }

    public ArrayList<Empaque> getEmpaques() {
        return empaques;
    }

    public void setEmpaques(ArrayList<Empaque> empaques) {
        this.empaques = empaques;
    }

    public ArrayList<SelectItem> getListaEmpaques() {
        if(this.listaEmpaques==null) {
            this.cargaListaEmpaques();
        }
        return listaEmpaques;
    }

    public void setListaEmpaques(ArrayList<SelectItem> listaEmpaques) {
        this.listaEmpaques = listaEmpaques;
    }
}
