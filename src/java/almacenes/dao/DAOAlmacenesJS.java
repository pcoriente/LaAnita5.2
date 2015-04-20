package almacenes.dao;

import almacenes.to.TOAlmacenJS;
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
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOAlmacenesJS {
    private DataSource ds = null;

    public DAOAlmacenesJS() throws NamingException {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");

            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
        } catch (NamingException ex) {
            throw (ex);
        }
    }
    
    public void modificar(TOAlmacenJS almacen) throws SQLException {
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            String strSQL="UPDATE almacenes "
                    + "SET almacen='"+almacen.getAlmacen()+"', idDireccion="+almacen.getIdDireccion()+" "
                    + "WHERE idAlmacen="+almacen.getIdAlmacen();
            st.executeUpdate(strSQL);
        } finally {
            cn.close();
        } 
    }
    
    public int agregar(TOAlmacenJS almacen) throws SQLException {
        int idAlmacen=0;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            String strSQL="INSERT INTO almacenes (almacen, idDireccion, idEmpresa, idCedis) "
                    + "VALUES ('"+almacen.getAlmacen()+"', "+almacen.getIdDireccion()+", "+almacen.getIdEmpresa()+", "+almacen.getIdCedis()+")";
            st.executeUpdate("begin Transaction");
            st.executeUpdate(strSQL);
            ResultSet rs=st.executeQuery("SELECT @@IDENTITY AS idAlmacen");
            if(rs.next()) {
                idAlmacen=rs.getInt("idAlmacen");
            }
            st.executeUpdate("commit Transaction");
        } catch(SQLException ex) {
            st.executeUpdate("rollback transaction");
            throw(ex);
        } finally {
            cn.close();
        }
        return idAlmacen;
    }
    
    public TOAlmacenJS obtenerAlmacen(int idAlmacen) throws SQLException {
        TOAlmacenJS almacen=null;
        String stringSQL = "SELECT a.idAlmacen, a.almacen, a.idCedis, a.idEmpresa, e.nombreComercial, a.idDireccion "
                    + "FROM almacenes a "
                    + "INNER JOIN empresasGrupo e ON e.idEmpresa=a.idEmpresa "
                    + "WHERE idAlmacen="+idAlmacen;
        Connection cn=this.ds.getConnection();
        try {
            Statement st=cn.createStatement();
            ResultSet rs=st.executeQuery(stringSQL);
            if(rs.next()) almacen=construir(rs);
        } finally {
            cn.close();
        }
        return almacen;
    }
    
    public ArrayList<TOAlmacenJS> obtenerAlmacenesEmpresa(int idCedis, int idEmpresa) throws SQLException {
        ArrayList<TOAlmacenJS> lista = new ArrayList<TOAlmacenJS>();
        String stringSQL = "SELECT a.idAlmacen, a.almacen, a.idCedis, a.idEmpresa, e.nombreComercial, a.idDireccion "
                    + "FROM almacenes a "
                    + "INNER JOIN empresasGrupo e ON e.idEmpresa=a.idEmpresa "
                    + "WHERE a.idCedis="+idCedis+" AND a.idEmpresa="+idEmpresa+" "
                    + "ORDER BY e.empresa";
        Connection cn = ds.getConnection();
        try {
            Statement sentencia = cn.createStatement();
            ResultSet rs = sentencia.executeQuery(stringSQL);
            while (rs.next()) {
                lista.add(construir(rs));
            }
        } finally {
            cn.close();
        }
        return lista;
    }
    
    public ArrayList<TOAlmacenJS> obtenerAlmacenes(int idCedis) throws SQLException {
        ArrayList<TOAlmacenJS> lista = new ArrayList<TOAlmacenJS>();
        String stringSQL = "SELECT a.idAlmacen, a.almacen, a.idCedis, a.idEmpresa, e.nombreComercial, a.idDireccion "
                    + "FROM almacenes a "
                    + "INNER JOIN empresasGrupo e ON e.idEmpresa=a.idEmpresa "
                    + "WHERE a.idCedis="+idCedis+" "
                    + "ORDER BY e.empresa";
        Connection cn = ds.getConnection();
        try {
            Statement sentencia = cn.createStatement();
            ResultSet rs = sentencia.executeQuery(stringSQL);
            while (rs.next()) {
                lista.add(construir(rs));
            }
        } finally {
            cn.close();
        }
        return lista;
    }
    
    private TOAlmacenJS construir(ResultSet rs) throws SQLException {
        TOAlmacenJS to = new TOAlmacenJS();
        to.setIdAlmacen(rs.getInt("idAlmacen"));
        to.setAlmacen(rs.getString("almacen"));
        to.setIdCedis(rs.getInt("idCedis"));
        to.setIdEmpresa(rs.getInt("idEmpresa"));
        to.setEmpresa(rs.getString("nombreComercial"));
        to.setIdDireccion(rs.getInt("idDireccion"));
        return to;
    }
}
