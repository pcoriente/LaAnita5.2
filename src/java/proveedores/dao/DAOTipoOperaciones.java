package proveedores.dao;

//import java.sql.Connection;

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
import proveedores.dominio.TipoOperacion;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jsolis
 */
public class DAOTipoOperaciones {
    String tabla="proveedoresTipoOperacion";
    private DataSource ds;
    
    public DAOTipoOperaciones() throws NamingException {
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
    
    public ArrayList<TipoOperacion> obtenerTipoOperaciones() throws SQLException {
        ArrayList<TipoOperacion> operaciones=new ArrayList<TipoOperacion>();
        
        Connection cn=ds.getConnection();
        String strSQL="select * from "+this.tabla+" order by operacion";
        try {
            Statement sentencia = cn.createStatement();
            ResultSet rs = sentencia.executeQuery(strSQL);
            while(rs.next()) {
                operaciones.add(construir(rs));
            }
        } finally {
            cn.close();
        }
        return operaciones;
    }
    
    private TipoOperacion construir(ResultSet rs) throws SQLException {
        TipoOperacion tipoOperacion=new TipoOperacion();
        tipoOperacion.setIdTipoOperacion(rs.getInt("idTipoOperacion"));
        tipoOperacion.setTipoOperacion(rs.getString("tipoOperacion"));
        tipoOperacion.setOperacion(rs.getString("operacion"));
        return tipoOperacion;
    }
    
    public TipoOperacion obtenerTipoOperacion(int idTipoOperacion) throws SQLException {
        TipoOperacion tipoOperacion=null;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        String strSQL="select * from "+this.tabla+" where idTipoOperacion="+idTipoOperacion;
        try {
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                tipoOperacion=construir(rs);
            }
        } finally {
            cn.close();
        }
        return tipoOperacion;
    }
    
    public void eliminar(int idTipoOperacion) throws SQLException, Exception {
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        String strSQL="DELETE FROM "+this.tabla+" WHERE idTipoOperacion=" + idTipoOperacion;
        try {
            int total=0;
            st.executeUpdate("begin transaction");
            ResultSet rs=st.executeQuery("SELECT count(*) AS total FROM proveedores WHERE idTipoOperacion="+idTipoOperacion);
            if(rs.next()) {
                total=rs.getInt("total");
            }
            if(total==0) {
                st.executeUpdate(strSQL);
            } else {
                throw new Exception("El tipo de operación no se puede eliminar, se encuentra en uso !!!");
            }
            st.executeUpdate("commit transaction");
        } catch (SQLException ex) {
            st.executeUpdate("rollback transaction");
            throw (ex);
        } catch (Exception ex) {
            st.executeUpdate("rollback transaction");
            throw (ex);
        } finally {
            cn.close();
        }
    }
    
    public void modificar(TipoOperacion t) throws SQLException {
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        String strSQL="UPDATE "+this.tabla+" "
                    + "SET operacion='" + t.getOperacion() + "', tipoOperacion='"+t.getTipoOperacion()+"' "
                    + "WHERE idTipoOperacion=" + t.getIdTipoOperacion();
        try {
            st.executeUpdate(strSQL);
        } finally {
            cn.close();
        }
    }
    
    public int agregar(TipoOperacion t) throws SQLException {
        int idTipoOperacion = 0;
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        String strSQL="INSERT INTO "+this.tabla+" (tipoOperacion, operacion) VALUES ('" + t.getTipoOperacion() + "', '"+t.getOperacion()+"')";
        try {
            st.executeUpdate("begin Transaction");
            st.executeUpdate(strSQL);
            ResultSet rs = st.executeQuery("SELECT MAX(idTipoOperacion) AS idTipoOperacion FROM "+this.tabla);
            if (rs.next()) {
                idTipoOperacion = rs.getInt("idTipoOperacion");
            }
            st.executeUpdate("commit Transaction");
        } catch (SQLException ex) {
            st.executeUpdate("rollback Transaction");
            throw (ex);
        } finally {
            cn.close();
        }
        return idTipoOperacion;
    }
}
