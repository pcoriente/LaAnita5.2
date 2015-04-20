package producto2.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import producto2.dominio.Grupo;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOGrupos {
    private DataSource ds;
    
    public DAOGrupos() throws NamingException {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
            
            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/"+usuarioSesion.getJndi());
        } catch (NamingException ex) {
            throw(ex);
        }
    }
    
    public int obtenerUltimoCodigoGrupo() throws SQLException {
        int ultimo = 0;
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery("SELECT max(codigoGrupo) as ultimo FROM productosGrupos");
            if (rs.next()) {
                ultimo = rs.getInt("ultimo");
            }
        } finally {
            cn.close();
        }
        return ultimo;
    }
    
    public void eliminar(int idGrupo) throws SQLException {
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");
            
            ResultSet rs=st.executeQuery("SELECT COUNT(*) AS total FROM productos WHERE idGrupo="+idGrupo);
            if(rs.next()) {
                throw new SQLException("Hay productos clasificados en el grupo, no se puede eliminar");
            }
            st.executeUpdate("DELETE FROM productosGrupos WHERE idGrupo=" + idGrupo);
            st.executeUpdate("DELETE FROM productosSubGrupos WHERE idGrupo="+idGrupo);
            
            st.executeUpdate("COMMIT TRANSACTION");
        } catch(SQLException ex) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw (ex);
        } finally {
            cn.close();
        }
    }
    
    public void modificar(Grupo g) throws SQLException {
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate("UPDATE productosGrupos "
                    + "SET codigoGrupo=" + g.getCodigo() + ", grupo='" + g.getGrupo() + "' "
                    + "WHERE idGrupo=" + g.getIdGrupo());
        } finally {
            cn.close();
        }
    }
    
    public int agregar(Grupo g) throws SQLException {
        int idGrupo = 0;
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate("begin Transaction");
            st.executeUpdate("INSERT INTO productosGrupos (codigoGrupo, grupo) VALUES (" + g.getCodigo() + ", '" + g.getGrupo() + "')");
            ResultSet rs = st.executeQuery("SELECT MAX(idGrupo) AS idGrupo FROM productosGrupos");
            if (rs.next()) {
                idGrupo = rs.getInt("idGrupo");
            }
            st.executeUpdate("commit Transaction");
        } catch (SQLException ex) {
            st.executeUpdate("rollback Transaction");
            throw (ex);
        } finally {
            cn.close();
        }
        return idGrupo;
    }
    
    public Grupo obtenerGrupo(int idGrupo) throws SQLException {
        Grupo grupo=null;
        String strSQL="SELECT * FROM productosGrupos WHERE idGrupo="+idGrupo;
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                grupo=new Grupo(rs.getInt("idGrupo"), rs.getInt("codigoGrupo"), rs.getString("grupo"));
            }
        } finally {
            cn.close();
        }
        return grupo;
    }
    
    public ArrayList<Grupo> obtenerGrupos() throws SQLException {
        ArrayList<Grupo> grupos=new ArrayList<Grupo>();
        String strSQL="SELECT * FROM productosGrupos ORDER BY grupo";
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                grupos.add(new Grupo(rs.getInt("idGrupo"), rs.getInt("codigoGrupo"), rs.getString("grupo")));
            }
        } finally {
            cn.close();
        }
        return grupos;
    }
}
