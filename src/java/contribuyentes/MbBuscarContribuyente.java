package contribuyentes;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import org.primefaces.context.RequestContext;

/**
 *
 * @author jsolis
 */
@Named(value = "mbBuscarContribuyente")
@SessionScoped
public class MbBuscarContribuyente implements Serializable {

    private String tipoBuscar;
    private String strBuscar;
    private Contribuyente contribuyente;
    private ArrayList<Contribuyente> contribuyentes;
    private ArrayList<Contribuyente> filtrados;

    public MbBuscarContribuyente() {
        inicializa();
    }

    public void inicializar() {
        inicializa();
    }

    private void inicializa() {
        this.tipoBuscar = "2";
        this.strBuscar = "";
        this.contribuyentes = null;
        this.filtrados = null;
    }

    public void verCambio() {

        this.strBuscar = "";
    }

    public void obtenerContribuyente(int idContribuyente) {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            DAOContribuyentes dao = new DAOContribuyentes();
            this.contribuyente = dao.obtenerContribuyente(idContribuyente);
            ok = true;
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okBuscarContribuyente", ok);
    }

    public void buscar() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            DAOContribuyentes dao = new DAOContribuyentes();
            if (this.tipoBuscar.equals("1")) {
                this.contribuyentes = dao.obtenerContribuyentesRFC(this.strBuscar);
                if (this.contribuyentes.isEmpty()) {
                    fMsg.setDetail("No se encontró contribuyente con el RFC proporcionado");
                    FacesContext.getCurrentInstance().addMessage(null, fMsg);
                } else {
                    this.contribuyente = null;
                }
            } else {
                this.contribuyente = null;
                this.contribuyentes = dao.obtenerContribuyentes(this.strBuscar);
                if (this.contribuyentes.isEmpty()) {
                    fMsg.setSeverity(FacesMessage.SEVERITY_INFO);
                    fMsg.setDetail("No se encontraron contribuyentes en la busqueda");
                    FacesContext.getCurrentInstance().addMessage(null, fMsg);
                }
            }
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okBuscarContribuyente", ok);
      
    }

    public Contribuyente buscarRfc() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            DAOContribuyentes dao = new DAOContribuyentes();
            if (this.tipoBuscar.equals("1")) {
                this.contribuyentes = dao.obtenerContribuyentesRFC(this.strBuscar);
                if (this.contribuyentes.isEmpty()) {
                    fMsg.setDetail("No se encontró contribuyente con el RFC proporcionado");
                    FacesContext.getCurrentInstance().addMessage(null, fMsg);
                    contribuyente= new Contribuyente();
                    contribuyente.setRfc(strBuscar);
                } else {
                    for (Contribuyente c : contribuyentes) {
                        this.setContribuyente(c);
                        break;
                    }
                }
            } else {
                this.contribuyentes = dao.obtenerContribuyentes(this.strBuscar);
                if (this.contribuyentes.isEmpty()) {
                    fMsg.setSeverity(FacesMessage.SEVERITY_INFO);
                    fMsg.setDetail("No se encontraron contribuyentes en la busqueda");
                    FacesContext.getCurrentInstance().addMessage(null, fMsg);
                }
            }
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okBuscarContribuyente", ok);
        return contribuyente;
    }

    public String getTipoBuscar() {
        return tipoBuscar;
    }

    public void setTipoBuscar(String tipoBuscar) {
        this.tipoBuscar = tipoBuscar;
    }

    public String getStrBuscar() {
        return strBuscar;
    }

    public void setStrBuscar(String strBuscar) {
        this.strBuscar = strBuscar;
    }

    public Contribuyente getContribuyente() {
        return contribuyente;
    }

    public void setContribuyente(Contribuyente contribuyente) {
        this.contribuyente = contribuyente;
    }

    public ArrayList<Contribuyente> getContribuyentes() {
        return contribuyentes;
    }

    public void setContribuyentes(ArrayList<Contribuyente> contribuyentes) {
        this.contribuyentes = contribuyentes;
    }

    public ArrayList<Contribuyente> getFiltrados() {
        return filtrados;
    }

    public void setFiltrados(ArrayList<Contribuyente> filtrados) {
        this.filtrados = filtrados;
    }
}
