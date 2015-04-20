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
import producto2.dominio.Empaque;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOEmpaques {
    private String tabla="empaquesUnidades";
    private DataSource ds;
    
    public DAOEmpaques() throws NamingException {
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
    
    public void eliminar(int idUnidad) throws SQLException {
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate("DELETE FROM "+this.tabla+" WHERE idUnidad="+idUnidad);
        } finally {
            cn.close();
        }
    }
    
    public void modificar(Empaque e) throws SQLException {
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate("UPDATE "+this.tabla+" "
                    + "SET unidad='"+e.getEmpaque()+"', abreviatura='"+e.getAbreviatura()+"' "
                    + "WHERE idUnidad="+e.getIdEmpaque());
        } finally {
            cn.close();
        }
    }
    
    public int agregar(Empaque e) throws SQLException {
        int idUnidad=0;
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate("begin Transaction");
            st.executeUpdate("INSERT INTO "+this.tabla+" (unidad, abreviatura) "
                    + "VALUES('"+e.getEmpaque()+"', '"+e.getAbreviatura()+"')");
            ResultSet rs=st.executeQuery("SELECT max(idUnidad) AS idUnidad FROM "+this.tabla);
            if(rs.next()) {
                idUnidad=rs.getInt("idUnidad");
            }
            st.executeUpdate("commit Transaction");
        } catch(SQLException ex) {
            st.executeUpdate("rollback Transaction");
            throw(ex);
        } finally {
            cn.close();
        }
        return idUnidad;
    }
    
    public Empaque obtenerEmpaque(int idEmpaque) throws SQLException {
        Empaque empaque=null;
        String strSQL="SELECT * FROM empaquesUnidades WHERE idUnidad="+idEmpaque;
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                empaque=new Empaque(rs.getInt("idUnidad"), rs.getString("unidad"), rs.getString("abreviatura"));
            }
        } finally {
            cn.close();
        }
        return empaque;
    }
    
    public ArrayList<Empaque> obtenerEmpaques() throws SQLException {
        ArrayList<Empaque> empaques=new ArrayList<Empaque>();
        String strSQL="SELECT * FROM empaquesUnidades ORDER BY unidad";
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                empaques.add(new Empaque(rs.getInt("idUnidad"), rs.getString("unidad"), rs.getString("abreviatura")));
            }
        } finally {
            cn.close();
        }
        return empaques;
    }
}
