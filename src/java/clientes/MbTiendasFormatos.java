package clientes;

import Message.Mensajes;
import clientes.dao.DAOTiendasFormatos;
import clientes.dominio.TiendaFormato;
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
 * @author jesc
 */
@Named(value = "mbTiendasFormatos")
@SessionScoped
public class MbTiendasFormatos implements Serializable {
    private TiendaFormato formato;
    private ArrayList<SelectItem> listaFormatos;
    
    public MbTiendasFormatos() {
        this.formato=new TiendaFormato();
    }
    
    public boolean validar() {
        boolean ok = true;
        if (this.formato.getFormato().equals("")) {
            Mensajes.mensajeAlert("Formato Requerido");
            ok=false;
        }
        return ok;
    }
    
    public void guardarFormato() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", "");
        RequestContext context = RequestContext.getCurrentInstance();
        if (validar()) {
            try {
                DAOTiendasFormatos dao = new DAOTiendasFormatos();
                if (this.formato.getIdFormato()==0) {
                    this.formato.setIdFormato(dao.agregar(formato));
                } else {
                    dao.modificar(formato);
                }
                this.setListaFormatos(null);
                fMsg.setDetail("El formato se grabo correctamente");
                ok=true;
            } catch (NamingException ex) {
                fMsg.setDetail(ex.getMessage());
            } catch (SQLException ex) {
                fMsg.setDetail(ex.getMessage());
            }
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
            context.addCallbackParam("ok", ok);
        }
    }
    
    public void inicializaFormato(int idGrupoCte) {
        this.formato.setIdFormato(0);
        this.formato.setFormato("");
        this.formato.setIdGrupoCte(idGrupoCte);
    }
    
    public void nuevoFormato(int idGrupoCte) {
        this.formato=new TiendaFormato(idGrupoCte);
    }
    
    public void cargarListaCombo(int idGrupoClte) {
        try {
            listaFormatos = new ArrayList<SelectItem>();
            DAOTiendasFormatos dao = new DAOTiendasFormatos();
            TiendaFormato fmto0 = new TiendaFormato();
            fmto0.setIdFormato(0);
            fmto0.setFormato("Nuevo Formato");
            listaFormatos.add(new SelectItem(fmto0, fmto0.getFormato()));
            for (TiendaFormato fmto : dao.obtenerFormatos(idGrupoClte)) {
                listaFormatos.add(new SelectItem(fmto, fmto.getFormato()));
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException e) {
            Mensajes.mensajeError(e.getMessage());
        }
    }
    
    public TiendaFormato getFormato() {
        return formato;
    }

    public void setFormato(TiendaFormato formato) {
        this.formato = formato;
    }

    public ArrayList<SelectItem> getListaFormatos() {
        return listaFormatos;
    }

    public void setListaFormatos(ArrayList<SelectItem> listaFormatos) {
        this.listaFormatos = listaFormatos;
    }
}
