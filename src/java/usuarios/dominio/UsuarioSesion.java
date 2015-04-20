package usuarios.dominio;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import usuarios.dominio.Usuario;

/**
 *
 * @author Julio
 */
@ManagedBean(name = "usuarioSesion")
@SessionScoped
public class UsuarioSesion {
    private Usuario usuario;
    private String jndi;
    /*
    public UsuarioSesion() {
        usuario=new Usuario();
        jndi="";
    }
    */
    public String getJndi() {
        return jndi;
    }

    public void setJndi(String jndi) {
        this.jndi = jndi;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
