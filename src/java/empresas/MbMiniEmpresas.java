package empresas;

import empresas.dao.DAOMiniEmpresas;
import empresas.dominio.MiniEmpresa;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;

/**
 *
 * @author JULIOS
 */
@ManagedBean(name = "mbMiniEmpresas")
@SessionScoped
public class MbMiniEmpresas implements Serializable {
    private MiniEmpresa empresa;
    private ArrayList<SelectItem> listaEmpresas;
    private DAOMiniEmpresas dao;
    
    public MbMiniEmpresas() {
        this.inicializaLocales();
    }
    
    public void inicializar() {
        this.inicializaLocales();
    }
    
    private void inicializaLocales() {
        this.empresa=new MiniEmpresa();
        this.setListaEmpresas(null);
    }
    
    public MiniEmpresa obtenerEmpresa(int idEmpresa) {
        boolean ok=false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "obtenerEmpresa");
        MiniEmpresa e=null;
        try {
            this.dao=new DAOMiniEmpresas();
            e=this.dao.obtenerMiniEmpresa(idEmpresa);
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
        return e;
    }
    
    private void cargaListaMiniEmpresas() {
        boolean ok=false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "cargaListaMiniEmpresas");
        this.listaEmpresas=new ArrayList<SelectItem>();
        try {
            MiniEmpresa e0=new MiniEmpresa();
            e0.setIdEmpresa(0);
            e0.setCodigoEmpresa("0");
            e0.setNombreComercial("Seleccione una empresa");
            this.listaEmpresas.add(new SelectItem(e0, e0.toString()));
            
            this.dao = new DAOMiniEmpresas();
            for (MiniEmpresa e : this.dao.obtenerMiniEmpresas()) {
                listaEmpresas.add(new SelectItem(e, e.toString()));
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

    public MiniEmpresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(MiniEmpresa empresa) {
        this.empresa = empresa;
    }

    public ArrayList<SelectItem> getListaEmpresas() {
        if(this.listaEmpresas==null) {
            this.cargaListaMiniEmpresas();
        }
        return listaEmpresas;
    }

    public void setListaEmpresas(ArrayList<SelectItem> listaEmpresas) {
        this.listaEmpresas = listaEmpresas;
    }

    public DAOMiniEmpresas getDao() {
        return dao;
    }

    public void setDao(DAOMiniEmpresas dao) {
        this.dao = dao;
    }
}
