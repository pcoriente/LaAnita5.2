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
import producto2.dominio.Parte;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author JULIOS
 */
public class DAOPartes {

    private DataSource ds;

    public DAOPartes() throws NamingException {
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

    public Parte obtenerParte(String str) throws SQLException {
        Parte parte = null;
        String strSQL = "SELECT idParte, parte FROM productosPartes WHERE parte='" + parte + "'";
        Connection cn = ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                parte = new Parte(rs.getInt("idparte"), rs.getString("parte"));
            }
        } finally {
            cn.close();
        }
        return parte;
    }

    public Parte obtenerParte(int idParte) throws SQLException {
        Parte parte = null;
        String strSQL = "SELECT idParte, parte FROM productosPartes WHERE idParte=" + idParte;
        Connection cn = ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                parte = new Parte(rs.getInt("idparte"), rs.getString("parte"));
            }
        } finally {
            cn.close();
        }
        return parte;
    }

    public ArrayList<Parte> obtenerPartes() throws SQLException {
        ArrayList<Parte> partes = new ArrayList<>();
        String strSQL = "SELECT idParte, parte FROM productosPartes ORDER BY parte";
        Connection cn = ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                partes.add(new Parte(rs.getInt("idparte"), rs.getString("parte")));
            }
        } finally {
            cn.close();
        }
        return partes;
    }

    public void eliminar(int idParte, int idProducto) throws SQLException {
        try (Connection cn = ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery("SELECT idProducto FROM productos WHERE idParte=" + idParte);
                boolean ban = false;
                String msg = "No se puede eliminar, esta en otro(s) producto(s) !";
                while (rs.next()) {
                    ban = true;
                    msg += "\n " + rs.getString("idProducto");
                }
                if (ban) {
                    throw new SQLException(msg);
                }
                st.executeUpdate("DELETE FROM productosPartes WHERE idParte=" + idParte);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                cn.setAutoCommit(true);
                throw (ex);
            }
            cn.setAutoCommit(true);
        }
    }

    public void modificar(Parte p) throws SQLException {
        Connection cn = ds.getConnection();
        try (Statement st = cn.createStatement()) {
            st.executeUpdate("UPDATE productosPartes SET parte='" + p.getParte() + "' WHERE idParte=" + p.getIdParte());
        } finally {
            cn.close();
        }
    }

    public int agregar(String parte) throws SQLException {
        int idParte = 0;
        Connection cn = ds.getConnection();
        try (Statement st = cn.createStatement()) {
            st.executeUpdate("INSERT INTO productosPartes (parte) VALUES ('" + parte + "')");
            ResultSet rs = st.executeQuery("SELECT MAX(idParte) AS idParte FROM productosPartes");
            if (rs.next()) {
                idParte = rs.getInt("idParte");
            }
        } finally {
            cn.close();
        }
        return idParte;
    }

    public ArrayList<Parte> completePartes(String strParte) throws SQLException {
        ArrayList<Parte> partes = new ArrayList<>();
        String strSQL = "SELECT idParte, parte FROM productosPartes WHERE parte like '%" + strParte + "%' ORDER BY parte";
        Connection cn = ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                partes.add(new Parte(rs.getInt("idparte"), rs.getString("parte")));
            }
        } finally {
            cn.close();
        }
        return partes;
    }
}
