package direccion.dao;

import direccion.dominio.Direccion;
import direccion.to.TODireccion;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
 * @author Julio
 */
public class DAODireccion {
    private DataSource ds;
    
    public DAODireccion() throws NamingException {
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
    
    public int agregar(String calle, String numeroExterior, String numeroInterior, String referencia, int idPais, String codigoPostal, String estado, String municipio, String localidad, String colonia, String numeroLocalizacion) throws SQLException {
        int idDireccion=0;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            // Hay que agregar todos los campos, ya que ninguno acepta nulos
            cn.setAutoCommit(false);
            st.executeUpdate("INSERT INTO direcciones (calle, numeroExterior, numeroInterior, referencia, idPais, codigoPostal, estado, municipio, localidad, colonia, numeroLocalizacion) "
                    + "VALUES('"+calle+"', '"+numeroExterior+"', '"+numeroInterior+"', '"+referencia+"', "+idPais+", '"+codigoPostal+"', '"+estado+"', '"+municipio+"', '"+localidad+"', '"+colonia+"', '"+numeroLocalizacion+"')");
            ResultSet rs=st.executeQuery("SELECT MAX(idDireccion) as idDireccion FROM direcciones");
            if(rs.next()) idDireccion=rs.getInt("idDireccion");
          //  cn.commit();
        } catch (SQLException ex) {
            cn.rollback();
            throw(ex);
        } finally {
            cn.close();
        }
        return idDireccion;
    }
    
    public void modificar(int idDireccion, String calle, String numeroExterior, String numeroInterior, String referencia, int idPais, String codigoPostal, String estado, String municipio, String localidad, String colonia, String numeroLocalizacion) throws SQLException {
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            String sql = "UPDATE direcciones SET calle='"+calle.trim()+"', numeroExterior='"+numeroExterior.trim()+"', numeroInterior='"+numeroInterior.trim()+"', referencia='"+referencia.trim()+"', idPais="+idPais+", codigoPostal='"+codigoPostal.trim()+"', estado='"+estado.trim()+"', municipio='"+municipio.trim()+"', localidad='"+localidad.trim()+"', colonia='"+colonia.trim()+"', numeroLocalizacion='"+numeroLocalizacion.trim()+ "' WHERE idDireccion="+idDireccion;
            st.executeUpdate(sql);
        } finally {
            cn.close();
        }
    }
    
    public void eliminar(int idDireccion) throws SQLException {
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        st.executeUpdate("DELETE FROM direcciones WHERE idDireccion="+idDireccion);
        cn.close();
    }
    
    public TODireccion obtener(int idDireccion) throws SQLException {
        TODireccion toDir=null;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery("SELECT * FROM direcciones WHERE idDireccion="+idDireccion);
            if(rs.next()) toDir=construir(rs);
        } finally {
            cn.close();
        }
        return toDir;
    }
    
    private TODireccion construir(ResultSet rs) throws SQLException {
        TODireccion toDir=new TODireccion();
        toDir.setIdDireccion(rs.getInt("idDireccion"));
        toDir.setCalle(rs.getString("calle"));
        toDir.setNumeroExterior(rs.getString("numeroExterior"));
        toDir.setNumeroInterior(rs.getString("numeroInterior"));
        toDir.setReferencia(rs.getString("referencia"));
        toDir.setIdPais(rs.getInt("idPais"));
        toDir.setCodigoPostal(rs.getString("codigoPostal"));
        toDir.setEstado(rs.getString("estado"));
        toDir.setMunicipio(rs.getString("municipio"));
        toDir.setLocalidad(rs.getString("localidad"));
        toDir.setColonia(rs.getString("colonia"));
        toDir.setNumeroLocalizacion(rs.getString("numeroLocalizacion"));
        return toDir;
    }
    public Direccion obtenerDireccion(int idDireccion) throws SQLException, NamingException {
        Direccion toDir = null;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery("SELECT * FROM direcciones WHERE idDireccion=" + idDireccion);
            if (rs.next()) {
                toDir = construirDireccion(rs);
            }
        } finally {
            cn.close();
        }
        return toDir;
    }

    private Direccion construirDireccion(ResultSet rs) throws SQLException, NamingException {
        Direccion toDir = new Direccion();
        DAOPais daoP = new DAOPais();
        toDir.setIdDireccion(rs.getInt("idDireccion"));
        toDir.setCalle(rs.getString("calle"));
        toDir.setNumeroExterior(rs.getString("numeroExterior"));
        toDir.setNumeroInterior(rs.getString("numeroInterior"));
        toDir.setReferencia(rs.getString("referencia"));
        toDir.setPais(daoP.obtener(rs.getInt("idPais")));
        toDir.setCodigoPostal(rs.getString("codigoPostal"));
        toDir.setEstado(rs.getString("estado"));
        toDir.setMunicipio(rs.getString("municipio"));
        toDir.setLocalidad(rs.getString("localidad"));
        toDir.setColonia(rs.getString("colonia"));
        toDir.setNumeroLocalizacion(rs.getString("numeroLocalizacion"));
        return toDir;
    }

}
