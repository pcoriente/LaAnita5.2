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
import proveedores.dominio.TipoTercero;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jsolis
 */
public class DAOTipoTerceros {
    String tabla="proveedoresTipoTercero";
    private DataSource ds;
    
    public DAOTipoTerceros() throws NamingException {
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
    
    public ArrayList<TipoTercero> obtenerTipoTerceros() throws SQLException {
        ArrayList<TipoTercero> terceros=new ArrayList<TipoTercero>();
        
        Connection cn=ds.getConnection();
        String strSQL="select * from "+this.tabla+" order by tercero";
        try {
            Statement sentencia = cn.createStatement();
            ResultSet rs = sentencia.executeQuery(strSQL);
            while(rs.next()) {
                terceros.add(construir(rs));
            }
        } finally {
            cn.close();
        }
        return terceros;
    }
    
    public TipoTercero obtenerTipoTercero(int idTipoTercero) throws SQLException {
        TipoTercero tipoTercero=null;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        String strSQL="select * from "+this.tabla+" where idTipoTercero="+idTipoTercero;
        try {
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                tipoTercero=construir(rs);
            }
        } finally {
            cn.close();
        }
        return tipoTercero;
    }
    
    private TipoTercero construir(ResultSet rs) throws SQLException {
        TipoTercero tipoTercero=new TipoTercero();
        tipoTercero.setIdTipoTercero(rs.getInt("idTipoTercero"));
        tipoTercero.setTipoTercero(rs.getString("tipoTercero"));
        tipoTercero.setTercero(rs.getString("tercero"));
        return tipoTercero;
    }
    
    public void eliminar(int idTipoTercero) throws SQLException, Exception {
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        String strSQL="DELETE FROM "+this.tabla+" WHERE idTipoTercero=" + idTipoTercero;
        try {
            int total=0;
            st.executeUpdate("begin transaction");
            ResultSet rs=st.executeQuery("SELECT count(*) AS total FROM proveedores WHERE idTipoTercero="+idTipoTercero);
            if(rs.next()) {
                total=rs.getInt("total");
            }
            if(total==0) {
                st.executeUpdate(strSQL);
            } else {
                throw new Exception("El tipo tercero está en uso, no puede eliminarse !!!");
            }
            st.executeUpdate("commit transaction");
        } catch(SQLException ex) {
            st.executeUpdate("rollback transaction");
            throw (ex);
        } catch (Exception ex) {
            st.executeUpdate("rollback transaction");
            throw (ex);
        } finally {
            cn.close();
        }
    }
    
    public void modificar(TipoTercero t) throws SQLException {
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        String strSQL="UPDATE "+this.tabla+" "
                    + "SET tercero='" + t.getTercero() + "', tipoTercero='"+t.getTipoTercero()+"' "
                    + "WHERE idTipoTercero=" + t.getIdTipoTercero();
        try {
            st.executeUpdate(strSQL);
        } finally {
            cn.close();
        }
    }
    
    public int agregar(TipoTercero t) throws SQLException {
        int idTipoTercero = 0;
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        String strSQL="INSERT INTO "+this.tabla+" (tipoTercero, Tercero) VALUES ('" + t.getTipoTercero() + "', '"+t.getTercero()+"')";
        try {
            st.executeUpdate("begin Transaction");
            st.executeUpdate(strSQL);
            ResultSet rs = st.executeQuery("SELECT MAX(idTipoTercero) AS idTipoTercero FROM "+this.tabla);
            if (rs.next()) {
                idTipoTercero = rs.getInt("idTipoTercero");
            }
            st.executeUpdate("commit Transaction");
        } catch (SQLException ex) {
            st.executeUpdate("rollback Transaction");
            throw (ex);
        } finally {
            cn.close();
        }
        return idTipoTercero;
    }
}
