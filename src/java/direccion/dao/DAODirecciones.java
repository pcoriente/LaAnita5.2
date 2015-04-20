package direccion.dao;

import direccion.dominio.Direccion;
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
import utilerias.Utilerias;
/**
 *
 * @author Julio
 */
public class DAODirecciones {
    private DataSource ds;
    
    public DAODirecciones() throws NamingException {
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
    
    private Direccion construir(ResultSet rs) throws SQLException {
        Direccion d=new Direccion();
        d.setIdDireccion(rs.getInt("idDireccion"));
        d.setCalle(Utilerias.Acentos(rs.getString("calle")).trim());
        d.setNumeroExterior(Utilerias.Acentos(rs.getString("numeroExterior")).trim());
        d.setNumeroInterior(Utilerias.Acentos(rs.getString("numeroInterior")).trim());
        d.setReferencia(Utilerias.Acentos(rs.getString("referencia")).trim());
        d.getPais().setIdPais(rs.getInt("idPais"));
        d.getPais().setPais(rs.getString("pais"));
        d.setCodigoPostal(Utilerias.Acentos(rs.getString("codigoPostal")).trim());
        d.setEstado(Utilerias.Acentos(rs.getString("estado")).trim());
        d.setMunicipio(Utilerias.Acentos(rs.getString("municipio")).trim());
        d.setLocalidad(Utilerias.Acentos(rs.getString("localidad")).trim());
        d.setColonia(Utilerias.Acentos(rs.getString("colonia")).trim());
        d.setNumeroLocalizacion(Utilerias.Acentos(rs.getString("numeroLocalizacion")).trim());
        return d;
    }
    
    public Direccion obtener(int idDireccion) throws SQLException {
        Direccion d;
        String strSQL="SELECT D.*, P.pais "
                + "FROM direcciones D "
                + "INNER JOIN paises P on P.idPais=D.idPais "
                + "WHERE idDireccion="+idDireccion;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                d=construir(rs);
            } else {
                d=new Direccion();
            }
        } finally {
            st.close();
            cn.close();
        }
        return d;
    }
    
    public void eliminar(int idDireccion) throws SQLException {
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate("DELETE FROM direcciones WHERE idDireccion="+idDireccion);
        } finally {
            st.close();
            cn.close();
        }
    }

//    public void modificar(int idDireccion, String calle, String numeroExterior, String numeroInterior, String referencia, int idPais, String codigoPostal, String estado, String municipio, String localidad, String colonia, String numeroLocalizacion) throws SQLException {
    public void modificar(Direccion d) throws SQLException {    
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            String sql = "UPDATE direcciones "
                    + "SET calle='"+d.getCalle()+"', numeroExterior='"+d.getNumeroExterior()+"', numeroInterior='"+d.getNumeroInterior()+"', referencia='"+d.getReferencia()+"', idPais="+d.getPais().getIdPais()+", codigoPostal='"+d.getCodigoPostal()+"', estado='"+d.getEstado()+"', municipio='"+d.getMunicipio()+"', localidad='"+d.getLocalidad()+"', colonia='"+d.getColonia()+"', numeroLocalizacion='"+d.getNumeroLocalizacion()+ "' "
                    + "WHERE idDireccion="+d.getIdDireccion();
            st.executeUpdate(sql);
        } finally {
            st.close();
            cn.close();
        }
    }
    
//    public int agregar(String calle, String numeroExterior, String numeroInterior, String referencia, int idPais, String codigoPostal, String estado, String municipio, String localidad, String colonia, String numeroLocalizacion) throws SQLException {
    public int agregar(Direccion d) throws SQLException {    
        int idDireccion=0;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            // Hay que agregar todos los campos, ya que ninguno acepta nulos
            st.executeUpdate("BEGIN TRANSACTION");
            st.executeUpdate("INSERT INTO direcciones (calle, numeroExterior, numeroInterior, referencia, idPais, codigoPostal, estado, municipio, localidad, colonia, numeroLocalizacion) "
                    + "VALUES('"+d.getCalle()+"', '"+d.getNumeroExterior()+"', '"+d.getNumeroInterior()+"', '"+d.getReferencia()+"', "+d.getPais().getIdPais()+", '"+d.getCodigoPostal()+"', '"+d.getEstado()+"', '"+d.getMunicipio()+"', '"+d.getLocalidad()+"', '"+d.getColonia()+"', '"+d.getNumeroLocalizacion()+"')");
            ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idDireccion");
            if(rs.next()) {
                idDireccion=rs.getInt("idDireccion");
            }
            st.executeUpdate("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw(ex);
        } finally {
            st.close();
            cn.close();
        }
        return idDireccion;
    }
}
