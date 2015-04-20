package unidadesMedida;

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
 * @author JULIOS
 */
public class DAOUnidadesMedida {
    private DataSource ds;
    
    public DAOUnidadesMedida() throws NamingException {
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
    
    public void modificar(UnidadMedida unidadMedida) throws SQLException {
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate("UPDATE unidadesMedida "
                    + "SET unidadMedida='"+unidadMedida.getUnidadMedida()+"', abreviatura='"+unidadMedida.getAbreviatura()+"' "
                    + "WHERE idUnidadMedida="+unidadMedida.getIdUnidadMedida());
        } finally {
            cn.close();
        }
    }
    
    public int agregar(UnidadMedida unidadMedida) throws SQLException {
        int idUnidadMedida=0;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate("begin Transaction");
            st.executeUpdate("INSERT INTO unidadesMedida (unidadMedida, abreviatura) "
                    + "VALUES ('"+unidadMedida.getUnidadMedida()+"', '"+unidadMedida.getAbreviatura()+"')");
            
            ResultSet rs=st.executeQuery("SELECT MAX(idUnidadMedida) AS idUnidadMedida FROM unidadesMedida");
            if(rs.next()) idUnidadMedida=rs.getInt("idUnidadMedida");
            st.executeUpdate("commit Transaction");
        } catch (SQLException ex) {
            st.executeUpdate("rollback Transaction");
            throw(ex);
        } finally {
            cn.close();
        }
        return idUnidadMedida;
    }
    
    public ArrayList<UnidadMedida> obtenerUnidades() throws SQLException {
        ArrayList<UnidadMedida> unidades=new ArrayList<UnidadMedida>();
        String strSQL="SELECT * FROM unidadesMedida ORDER BY idUnidadMedida desc";
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                unidades.add(new UnidadMedida(rs.getInt("idUnidadMedida"), rs.getString("unidadMedida"), rs.getString("abreviatura")));
            }
        } finally {
            cn.close();
        }
        return unidades;
    }
    
    public UnidadMedida obtenerUnidad(int idUnidad) throws SQLException {
        UnidadMedida unidad=null;
        String strSQL="SELECT * FROM unidadesMedida WHERE idUnidadMedida="+idUnidad;
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                unidad=new UnidadMedida(rs.getInt("idUnidadMedida"), rs.getString("unidadMedida"), rs.getString("abreviatura"));
            }
        } finally {
            cn.close();
        }
        return unidad;
    }
}
