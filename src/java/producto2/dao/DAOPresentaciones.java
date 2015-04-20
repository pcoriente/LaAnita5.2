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
import producto2.dominio.Presentacion;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOPresentaciones {
    private DataSource ds;
    private String tabla="productosPresentaciones";
    
    public DAOPresentaciones() throws NamingException {
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
    
    public void eliminar(int idPresentacion) throws SQLException {
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate("DELETE FROM "+this.tabla+" WHERE idPresentacion="+idPresentacion);
        } finally {
            cn.close();
        }
    }
    
    public void modificar(Presentacion presentacion) throws SQLException {
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate("UPDATE "+this.tabla+" "
                    + "SET presentacion='"+presentacion.getPresentacion()+"', abreviatura='"+presentacion.getAbreviatura()+"' "
                    + "WHERE idPresentacion="+presentacion.getIdPresentacion());
        } finally {
            cn.close();
        }
    }
    
    public int agregar(Presentacion presentacion) throws SQLException {
        int idUnidad=0;
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate("begin Transaction");
            st.executeUpdate("INSERT INTO "+this.tabla+" (presentacion, abreviatura) "
                    + "VALUES('"+presentacion.getPresentacion()+"', '"+presentacion.getAbreviatura()+"')");
            ResultSet rs=st.executeQuery("SELECT max(idPresentacion) AS idPresentacion FROM "+this.tabla);
            if(rs.next()) {
                idUnidad=rs.getInt("idPresentacion");
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
    
    public ArrayList<Presentacion> obtenerPresentaciones() throws SQLException {
        ArrayList<Presentacion> unidades=new ArrayList<Presentacion>();
        String strSQL="SELECT * FROM "+this.tabla+" ORDER BY presentacion";
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                unidades.add(new Presentacion(rs.getInt("idPresentacion"), rs.getString("presentacion"), rs.getString("abreviatura")));
            }
        } finally {
            cn.close();
        }
        return unidades;
    }
    
    public Presentacion obtenerPresentacion(int idPresentacion) throws SQLException {
        Presentacion unidad=null;
        String strSQL="SELECT * FROM "+this.tabla+" WHERE idPresentacion="+idPresentacion;
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                unidad=new Presentacion(rs.getInt("idPresentacion"), rs.getString("presentacion"), rs.getString("abreviatura"));
            }
        } finally {
            cn.close();
        }
        return unidad;
    }
}
