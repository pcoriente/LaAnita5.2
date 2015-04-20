package impuestos.dao;

import impuestos.dominio.ImpuestoZona;
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
public class DAOZonas {
    private DataSource ds;
    
    public DAOZonas() throws NamingException {
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
    
    public boolean eliminar(int idZona) throws SQLException {
        boolean ok=false;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("begin Transaction");
            ResultSet rs=st.executeQuery("SELECT * FROM impuestosDetalle WHERE idZona="+idZona);
            if(!rs.next()) {
                st.executeUpdate("DELETE FROM impuestosZonas WHERE idZona="+idZona);
                ok=true;
            }
            st.executeUpdate("commit Transaction");
        } catch (SQLException ex) {
            st.executeUpdate("rollback Transaction");
            throw (ex);
        } finally {
            cn.close();
        }
        return ok;
    }
    
    public int agregar(ImpuestoZona zona) throws SQLException {
        int idZona=0;
        String strSQL="INSERT INTO impuestosZonas (zona) VALUES ('"+zona.getZona()+"')";
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("begin Transaction");
            st.executeUpdate(strSQL);
            ResultSet rs=st.executeQuery("SELECT MAX(idZona) AS idZona FROM impuestosZonas");
            if(rs.next()) {
                idZona=rs.getInt("idZona");
            }
            st.executeUpdate("commit Transaction");
        } catch (SQLException ex) {
            st.executeUpdate("rollback Transaction");
            throw (ex);
        } finally {
            cn.close();
        }
        return idZona;
    }
    
    public void modificar(ImpuestoZona zona) throws SQLException {
        String strSQL="UPDATE impuestosZonas SET zona='"+zona.getZona()+"' WHERE idZona="+zona.getIdZona();
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate(strSQL);
        } finally {
            cn.close();
        }
    }
    
    public ImpuestoZona obtenerZona(int idZona) throws SQLException {
        ImpuestoZona zona = null;
        String strSQL = "SELECT * FROM impuestosZonas WHERE idZona=" + idZona;
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                zona = new ImpuestoZona(rs.getInt("idZona"), rs.getString("zona"));
            }
        } finally {
            cn.close();
        }
        return zona;
    }

    public ArrayList<ImpuestoZona> obtenerZonas() throws SQLException {
        ArrayList<ImpuestoZona> zonas = new ArrayList<ImpuestoZona>();
        String strSQL = "SELECT * FROM impuestosZonas ORDER BY zona";
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                zonas.add(new ImpuestoZona(rs.getInt("idZona"), rs.getString("zona")));
            }
        } finally {
            cn.close();
        }
        return zonas;
    }
}
