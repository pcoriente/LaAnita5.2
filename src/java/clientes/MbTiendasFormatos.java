package clientes;

import Message.Mensajes;
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
import formatos.dao.DAOFormatos;
import formatos.dominio.ClienteFormato;

/**
 *
 * @author jesc
 */
@Named(value = "mbTiendasFormatos")
@SessionScoped
public class MbTiendasFormatos implements Serializable {

    private ClienteFormato formato;
    private ArrayList<SelectItem> listaFormatos;

    public MbTiendasFormatos() {
        this.formato = new ClienteFormato();
    }

    public boolean validar() {
        boolean ok = true;
        if (this.formato.getFormato().equals("")) {
            Mensajes.mensajeAlert("Formato Requerido");
            ok = false;
        }
        return ok;
    }

    public void guardarFormato() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", "");
        RequestContext context = RequestContext.getCurrentInstance();
        if (validar()) {
            try {
                DAOFormatos dao = new DAOFormatos();
                if (this.formato.getIdFormato() == 0) {
//                    this.formato.setIdFormato(dao.agregar(formato));
                    dao.agregar(formato);
                } else {
//                    dao.modificar(formato);
                }
                this.setListaFormatos(null);
                fMsg.setDetail("El formato se grabo correctamente");
                ok = true;
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
        this.formato = new ClienteFormato();
        formato.setIdGrupoCte(idGrupoCte);
    }

    public void cargarListaCombo(int idGrupoClte) {
        try {
            listaFormatos = new ArrayList<SelectItem>();
            DAOFormatos dao = new DAOFormatos();
            ClienteFormato fmto0 = new ClienteFormato();
            fmto0.setIdFormato(0);
            fmto0.setFormato("Nuevo Formato");
            listaFormatos.add(new SelectItem(fmto0, fmto0.getFormato()));
            for (ClienteFormato fmto : dao.dameFormatos(idGrupoClte)) {
                listaFormatos.add(new SelectItem(fmto, fmto.getFormato()));
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException e) {
            Mensajes.mensajeError(e.getMessage());
        }
    }
        public ClienteFormato obtenerFormato(int idFormato) {
        ClienteFormato f = new ClienteFormato();
        try {
            DAOFormatos dao = new DAOFormatos();
            f = dao.obtenerClientesFormato(idFormato);
        } catch (NamingException | SQLException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        return f;
    }



    public ArrayList<SelectItem> getListaFormatos() {
        return listaFormatos;
    }

    public void setListaFormatos(ArrayList<SelectItem> listaFormatos) {
        this.listaFormatos = listaFormatos;
    }
}
