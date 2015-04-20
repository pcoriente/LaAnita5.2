package dbs;

import dbs.dao.DAODbs;
import dbs.dominio.Dbs;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import main.MbMenu;
import usuarios.MbAcciones;
import usuarios.dominio.UsuarioSesion;
import usuarios.dominio.Usuario;

@ManagedBean(name = "mbDbs")
@RequestScoped
public class MbDbs implements Serializable {
    private Long id;
    private String login;
    private String password;
    @ManagedProperty(value = "#{usuarioSesion}")
    private UsuarioSesion usuarioSesion;
    //@ManagedProperty(value = "#{mbMenu}")
    //private MbMenu mbMenu;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    private Dbs dbs;
    private List<SelectItem> listaDbs;
    DAODbs dao;

    public MbDbs() throws NamingException {
        dao=new DAODbs();
        //this.mbAcciones=new MbAcciones(0);
    }

    public Dbs getDbs() {
        return dbs;
    }

    public void setDbs(Dbs dbs) {
        this.dbs = dbs;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UsuarioSesion getUsuarioSesion() {
        return usuarioSesion;
    }

    public void setUsuarioSesion(UsuarioSesion usuarioSesion) {
        this.usuarioSesion = usuarioSesion;
    }

    public List<SelectItem> getListaDbs() {
        try {
            this.listaDbs = obtenerBases();
        } catch (NamingException ex) {
            Logger.getLogger(MbDbs.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(MbDbs.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listaDbs;
    }

    public List<SelectItem> obtenerBases() throws NamingException, SQLException {
        List<SelectItem> bases = new ArrayList<SelectItem>();
        
        Dbs db = new Dbs();
        db.setIdDbs(0);
        db.setNombreBds("Seleccione la Base: ");
        SelectItem p0 = new SelectItem(db, db.getNombreBds());
        bases.add(p0);

        DAODbs daoDbs = new DAODbs();
        Dbs[] rDbs = daoDbs.obtenerDbs();

        for (Dbs po : rDbs) {
            bases.add(new SelectItem(po, po.getNombreBds()));
        }
        return bases;
    }
    
    public String doLogin() {
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        String outcome=null;
        try {
            Usuario usuario = this.dao.login(this.login, this.password, this.dbs.getJndiDbs(), this.dbs.getIdDbs());
            if (usuario == null) {
                fMsg.setDetail("Usuario no válido !!!");
            } else if(usuario.getId()==0) {
                fMsg.setDetail("Clave incorrecta !!!");
            } else if(usuario.getIdPerfil()==0) {
                fMsg.setDetail("El usuario no tiene permisos para ingresar al sistema !!!");
            } else {
                this.usuarioSesion.setUsuario(usuario);
                this.usuarioSesion.setJndi(this.dbs.getJndiDbs());
                //this.mbMenu.actualizarMenu();
                outcome = "exito.login";
            }
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (Exception ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail("Error al encriptar el password !!!");
        }
        if(outcome==null) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        return outcome;
    }

    public MbAcciones getMbAcciones() {
        return mbAcciones;
    }

    public void setMbAcciones(MbAcciones mbAcciones) {
        this.mbAcciones = mbAcciones;
    }
    /*
    public MbMenu getMbMenu() {
        return mbMenu;
    }

    public void setMbMenu(MbMenu mbMenu) {
        this.mbMenu = mbMenu;
    }
    * */
}
