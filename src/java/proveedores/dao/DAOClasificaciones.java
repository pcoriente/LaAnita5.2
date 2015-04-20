package proveedores.dao;

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
import proveedores.dominio.Clasificacion;
import proveedores.dominio.SubClasificacion;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jsolis
 */
public class DAOClasificaciones {
    String tabla="proveedoresClasificacion";
    String tabla1="proveedoresSubClasificacion";
    private DataSource ds;
    
    public DAOClasificaciones() throws NamingException {
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
    
    public ArrayList<SubClasificacion> obtenerSubClasificaciones(int idClasificacion) throws SQLException {
        ArrayList<SubClasificacion> sc=new ArrayList<SubClasificacion>();
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        String strSQL="SELECT * FROM "+this.tabla1+" WHERE idClasificacion="+idClasificacion+" ORDER BY subClasificacion";
        try {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                sc.add(construirSub(rs));
            }
        } finally {
            cn.close();
        }
        return sc;
    }
    
    public SubClasificacion obtenerSubClasificacion(int idSubClasificacion) throws SQLException {
        SubClasificacion subClasificacion=null;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        String strSQL="select * from "+this.tabla1+" where idSubClasificacion="+idSubClasificacion;
        try {
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                subClasificacion=construirSub(rs);
            }
        } finally {
            cn.close();
        }
        return subClasificacion;
    }
    
    private SubClasificacion construirSub(ResultSet rs) throws SQLException {
        SubClasificacion subClasificacion=new SubClasificacion();
        subClasificacion.setIdSubClasificacion(rs.getInt("idSubClasificacion"));
        subClasificacion.setSubClasificacion(rs.getString("subClasificacion"));
        return subClasificacion;
    }
    
    public void eliminarSubClasificacion(int idSubClasificacion) throws SQLException, Exception {
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        String strSQL="DELETE FROM "+this.tabla1+" WHERE idSubClasificacion=" + idSubClasificacion;
        try {
            int total=0;
            st.executeUpdate("begin transaction");
            ResultSet rs=st.executeQuery("SELECT count(*) AS total FROM proveedores WHERE idSubClasificacion="+idSubClasificacion);
            if(rs.next()) {
                total=rs.getInt("total");
            }
            if(total==0) {
                st.executeUpdate(strSQL);
            } else {
                throw(new Exception("La subclasificacion esta en uso, no puede eliminarse !!!"));
            }
            st.executeUpdate("commit transaction");
        } catch(SQLException ex) {
            st.executeUpdate("rollback transaction");
            throw (ex);
        } catch(Exception ex) {
            st.executeUpdate("rollback transaction");
            throw (ex);
        } finally {
            cn.close();
        }
    }
    
    public void modificarSubClasificacion(SubClasificacion s) throws SQLException {
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        String strSQL="UPDATE "+this.tabla1+" "
                    + "SET subClasificacion='" + s.getSubClasificacion() + "' "
                    + "WHERE idSubClasificacion=" + s.getIdSubClasificacion();
        try {
            st.executeUpdate(strSQL);
        } finally {
            cn.close();
        }
    }
    
    public int agregarSubClasificacion(SubClasificacion s, int idClasificacion) throws SQLException {
        int idSubClasificacion = 0;
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        String strSQL="INSERT INTO "+this.tabla1+" (subClasificacion, idClasificacion) VALUES ('" + s.getSubClasificacion() + "', "+idClasificacion+")";
        try {
            st.executeUpdate("begin Transaction");
            st.executeUpdate(strSQL);
            ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idSubClasificacion");
            if (rs.next()) {
                idSubClasificacion = rs.getInt("idSubClasificacion");
            }
            st.executeUpdate("commit Transaction");
        } catch (SQLException ex) {
            st.executeUpdate("rollback Transaction");
            throw (ex);
        } finally {
            cn.close();
        }
        return idSubClasificacion;
    }
    
    public ArrayList<Clasificacion> obtenerClasificaciones() throws SQLException {
        ArrayList<Clasificacion> clasificaciones=new ArrayList<Clasificacion>();
        
        Connection cn=ds.getConnection();
        String strSQL="select * from "+this.tabla+" order by clasificacion";
        try {
            Statement sentencia = cn.createStatement();
            ResultSet rs = sentencia.executeQuery(strSQL);
            while(rs.next()) {
                clasificaciones.add(construir(rs));
            }
        } finally {
            cn.close();
        }
        return clasificaciones;
    }
    
    public Clasificacion obtenerClasificacion(int idClasificacion) throws SQLException {
        Clasificacion clasificacion=null;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        String strSQL="select * from "+this.tabla+" where idClasificacion="+idClasificacion;
        try {
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                clasificacion=construir(rs);
            }
        } finally {
            cn.close();
        }
        return clasificacion;
    }
    
    private Clasificacion construir(ResultSet rs) throws SQLException {
        Clasificacion clasificacion=new Clasificacion();
        clasificacion.setIdClasificacion(rs.getInt("idClasificacion"));
        clasificacion.setClasificacion(rs.getString("clasificacion"));
        return clasificacion;
    }
    
    public void eliminar(int idClasificacion) throws SQLException, Exception {
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        String strSQL="DELETE FROM "+this.tabla+" WHERE idClasificacion=" + idClasificacion;
        try {
            int total=0;
            st.executeUpdate("begin transaction");
            ResultSet rs=st.executeQuery("SELECT count(*) AS total FROM proveedores WHERE idClasificacion="+idClasificacion);
            if(rs.next()) {
                total=rs.getInt("total");
            }
            if(total==0) {
                st.executeUpdate(strSQL);
            } else {
                throw new Exception("No se puede eliminar, la clasificación se encuentra en uso !!!");
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
    
    public void modificar(Clasificacion c) throws SQLException {
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        String strSQL="UPDATE "+this.tabla+" "
                    + "SET clasificacion='" + c.getClasificacion() + "' "
                    + "WHERE idClasificacion=" + c.getIdClasificacion();
        try {
            st.executeUpdate(strSQL);
        } finally {
            cn.close();
        }
    }
    
    public int agregar(Clasificacion c) throws SQLException {
        int idClasificacion = 0;
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        String strSQL="INSERT INTO "+this.tabla+" (clasificacion) VALUES ('" + c.getClasificacion() + "')";
        try {
            st.executeUpdate("begin Transaction");
            st.executeUpdate(strSQL);
            ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idClasificacion");
            if (rs.next()) {
                idClasificacion = rs.getInt("idClasificacion");
            }
            st.executeUpdate("commit Transaction");
        } catch (SQLException ex) {
            st.executeUpdate("rollback Transaction");
            throw (ex);
        } finally {
            cn.close();
        }
        return idClasificacion;
    }
}
