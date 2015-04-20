/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package requisiciones.mb;

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
import requisiciones.dao.DAOUsuarioRequisiciones;
import usuarios.dominio.Usuario;

/**
 *
 * @author daap
 */
@Named(value = "mbUsuarios")
@SessionScoped
public class MbUsuarios implements Serializable {

    private Usuario usuario;
    private ArrayList<SelectItem> listaUsuarios;
    private DAOUsuarioRequisiciones dao;

    private ArrayList<Usuario> subUsuario;
    
    public MbUsuarios() {
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public ArrayList<SelectItem> getListaUsuarios() throws SQLException {
        if (this.listaUsuarios == null) {
//            this.cargarUsuarios();
        }
        return listaUsuarios;
    }

    public void setListaUsuarios(ArrayList<SelectItem> listaUsuarios) {
        this.listaUsuarios = listaUsuarios;
    }

    public ArrayList<Usuario> getSubUsuario() {
        return subUsuario;
    }

    public void setSubUsuario(ArrayList<Usuario> subUsuario) {
        this.subUsuario = subUsuario;
    }
    
//    private void cargarUsuarios() throws SQLException {
//        this.listaUsuarios = new ArrayList<SelectItem>();
//        Usuario usu = new Usuario(0, "Seleccione un usuario");
//        this.listaUsuarios.add(new SelectItem(usu, usu.toString()));
//        try {
//            this.dao = new DAOUsuarioRequisiciones();
//            ArrayList<Usuario> lstUsuarios = this.dao.obtenerUsuarios();
//            for (Usuario z : lstUsuarios) {
//                this.listaUsuarios.add(new SelectItem(z, z.toString()));
//            }
//
//        } catch (NamingException ex) {
//            Logger.getLogger(MbUsuarios.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

    public ArrayList<Usuario> obtenerSubUsuarios(int idDepto) throws SQLException {
        boolean ok = false;
        this.subUsuario=new ArrayList<Usuario>();
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao=new DAOUsuarioRequisiciones();
            this.subUsuario = this.dao.obtenerSubUsuario(idDepto);
            ok = true;
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        } 
        if(!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okSubUsuario", ok);
        return this.subUsuario;
    }

}
