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
import producto2.dominio.Marca;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOMarcas {
    private String tabla="productosMarcas";
    private DataSource ds;
    
    public DAOMarcas() throws NamingException {
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
    
    public void eliminar(int idMarca) throws SQLException {
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate("DELETE FROM "+this.tabla+" WHERE idMarca="+idMarca);
        } finally {
            cn.close();
        }
    }
    
    public int agregar(Marca marca) throws SQLException {
        int idMarca=0;
        
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate("begin Transaction");
            
            st.executeUpdate("INSERT INTO "+this.tabla+" (marca, produccion) "
                    + "VALUES ('"+marca.getMarca()+"', "+(marca.isProduccion()?1:0)+")");
            
            ResultSet rs=st.executeQuery("SELECT MAX(idMarca) as idMarca FROM "+this.tabla);
            if(rs.next()) {
                idMarca=rs.getInt("idMarca");
            }
            st.executeUpdate("commit Transaction");
        } catch(SQLException ex) {
            st.executeUpdate("rollback Transaction");
            throw(ex);
        } finally {
            cn.close();
        }
        return idMarca;
    }
    
    public void modificar(Marca marca) throws SQLException {
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate("UPDATE "+this.tabla+" "
                    + "SET marca='"+marca.getMarca()+"' "
                    + "WHERE idMarca="+marca.getIdMarca());
        } finally {
            cn.close();
        }
    }
    
    public Marca obtenerMarca(int idMarca) throws SQLException {
        Marca mca=null;
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery("SELECT * FROM "+this.tabla+" WHERE idMarca="+idMarca);
            if(rs.next()) {
                mca=new Marca(rs.getInt("idMarca"), rs.getString("marca"), rs.getBoolean("produccion"));
            }
        } finally {
            cn.close();
        }
        return mca;
    }
    
    public ArrayList<Marca> obtenerMarcas() throws SQLException {
        ArrayList<Marca> lista=new ArrayList<Marca>();
        String strSQL="SELECT * FROM "+this.tabla+" ORDER BY marca";
        
        Connection cn=ds.getConnection();
        Statement sentencia = cn.createStatement();
        try {
            ResultSet rs = sentencia.executeQuery(strSQL);
            while(rs.next()) {
                lista.add(new Marca(rs.getInt("idMarca"), rs.getString("marca"), rs.getBoolean("produccion")));
            }
        } finally {
            cn.close();
        }
        return lista;
    }
    
    public int ultimoCodigo() throws SQLException {
        int ultimo=0;
        
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery("SELECT MAX(codigoMarca) as ultimo FROM "+this.tabla);
            if(rs.next()) {
                ultimo=rs.getInt("ultimo");
            }
        } finally {
            cn.close();
        }
        return ultimo;
    }
}
