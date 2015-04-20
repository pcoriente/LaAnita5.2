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
import producto2.dominio.SubGrupo;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOSubGrupos {
    private DataSource ds;
    
    public DAOSubGrupos() throws NamingException {
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
    
    public void eliminar(int idSubGrupo) throws SQLException {
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate("DELETE FROM productosSubGrupos WHERE idSubGrupo=" + idSubGrupo);
        } finally {
            cn.close();
        }
    }
    
    public void modificar(SubGrupo s) throws SQLException {
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate("UPDATE productosSubGrupos "
                    + "SET subGrupo='" + s.getSubGrupo() + "' "
                    + "WHERE idSubGrupo=" + s.getIdSubGrupo());
        } finally {
            cn.close();
        }
    }
    
    public int agregar(SubGrupo s, int idGrupo) throws SQLException {
        int idSubGrupo=0;
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate("begin Transaction");
            st.executeUpdate("INSERT INTO productosSubGrupos (subGrupo, idGrupo) VALUES ('" + s.getSubGrupo() + "', " + idGrupo + ")");
            ResultSet rs = st.executeQuery("SELECT max(idSubGrupo) AS idSubGrupo FROM productosSubGrupos");
            if (rs.next()) {
                idSubGrupo = rs.getInt("idSubGrupo");
            }
            st.executeUpdate("commit Transaction");
        } catch (SQLException ex) {
            st.executeUpdate("rollback Transaction");
            throw (ex);
        } finally {
            cn.close();
        }
        return idSubGrupo;
    }
    
    public SubGrupo obtenerSubGrupo(int idSubGrupo) throws SQLException {
        SubGrupo subGrupo=null;
        String strSQL=""
                + "SELECT sg.idSubGrupo, sg.subGrupo "
                + "FROM productosSubGrupos sg "
                + "WHERE sg.idSubGrupo="+idSubGrupo;
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                subGrupo=new SubGrupo(rs.getInt("idSubGrupo"), rs.getString("subGrupo"));
            }
        } finally {
            cn.close();
        }
        return subGrupo;
    }
    
    public ArrayList<SubGrupo> obtenerSubGrupos(int idGrupo) throws SQLException {
        ArrayList<SubGrupo> subGrupos=new ArrayList<SubGrupo>();
        String strSQL=""
                + "SELECT sg.idSubGrupo, sg.subGrupo "
                + "FROM productosSubGrupos sg "
                + "WHERE sg.idGrupo="+idGrupo+" "
                + "ORDER BY sg.subGrupo";
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                subGrupos.add(new SubGrupo(rs.getInt("idSubGrupo"), rs.getString("subGrupo")));
            }
        } finally {
            cn.close();
        }
        return subGrupos;
    }
}
