package usuarios.dominio;

import java.io.Serializable;

/**
 *
 * @author Julio
 */
public class Usuario implements Serializable {
    private int id;
    private String usuario;
    private String correo;
    private int idPerfil;
    private int idCedis;
    private int idCedisZona;
    
    public Usuario() {
        this.id=0;
        this.usuario="";
        this.correo="";
        this.idPerfil=0;
        this.idCedis=0;
        this.idCedisZona=0;
    }
    
    public Usuario(int id, String usuario) {
        this.id=id;
        this.usuario=usuario;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Usuario other = (Usuario) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + this.id;
        return hash;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public int getIdPerfil() {
        return idPerfil;
    }

    public void setIdPerfil(int idPerfil) {
        this.idPerfil = idPerfil;
    }
    
     @Override
    public String toString() {
        return  usuario;
    }

    public int getIdCedis() {
        return idCedis;
    }

    public void setIdCedis(int idCedis) {
        this.idCedis = idCedis;
    }

    public int getIdCedisZona() {
        return idCedisZona;
    }

    public void setIdCedisZona(int idCedisZona) {
        this.idCedisZona = idCedisZona;
    }
}
