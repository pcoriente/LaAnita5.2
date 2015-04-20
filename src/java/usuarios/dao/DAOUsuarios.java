package usuarios.dao;

import com.sun.faces.config.WebConfiguration.WebEnvironmentEntry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import usuarios.dominio.Usuario;

/**
 *
 * @author Julio
 */
public class DAOUsuarios {
    private DataSource ds;
    String value = null;
    
    public DAOUsuarios() throws NamingException {
        Context cI = null;
        try {
            cI = new InitialContext();
        } catch (NamingException ex) {
            Logger.getLogger(DAOUsuarios.class.getName()).log(Level.SEVERE, null, ex);
            throw (ex);
        }
        for (WebEnvironmentEntry entry : WebEnvironmentEntry.values()) {
            String entryName = entry.getQualifiedName();

            try {
                value = (String) cI.lookup(entryName);
            } catch (NamingException ex) {
                Logger.getLogger(DAOUsuarios.class.getName()).log(Level.SEVERE, null, ex);
                throw (ex);
            }
        }
        ds = (DataSource) cI.lookup("java:comp/env/"+value);
        
    }
    
    public Usuario obtenerUsuario(String login) throws SQLException {
        ResultSet rs=null;
        Usuario usuario = null;
        
        Connection cn=ds.getConnection();
        String strSQL="SELECT * FROM usuarios WHERE login=?";
        try {
            PreparedStatement sentencia = cn.prepareStatement(strSQL);
            sentencia.setString(1, login);
            rs = sentencia.executeQuery();
            if(rs.next()) usuario=convertirAUsuario(rs);
        } finally {
            cn.close();
        }
        return usuario;
    }
    
    private Usuario convertirAUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("idUsuario"));
        usuario.setUsuario(rs.getString("usuario"));
        usuario.setCorreo(rs.getString("email"));
        usuario.setIdPerfil(rs.getInt("idPerfil"));
        return usuario;
    }
}
