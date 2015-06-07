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
import producto2.dominio.Upc;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author JULIOS
 */
public class DAOUpcs {

    private DataSource ds;
    private String tabla = "empaquesUpcs";

    public DAOUpcs() throws NamingException {
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

    public void Eliminar(String upc) throws SQLException {
        Connection cn = ds.getConnection();
        try (Statement st = cn.createStatement()) {
            st.executeUpdate("DELETE FROM " + this.tabla + " WHERE upc='" + upc + "'");
        } finally {
            cn.close();
        }
    }

    public void modificar(Upc u) throws SQLException {
        try (Connection cn = ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery("SELECT upc FROM " + this.tabla + " WHERE idProducto=" + u.getIdProducto() + " AND actual=1");
                if (rs.next()) {
                    st.executeUpdate("UPDATE " + this.tabla + " SET actual=0 WHERE upc='" + rs.getString("upc") + "'");
                }
                st.executeUpdate("UPDATE " + this.tabla + " SET actual=1 WHERE upc='" + u.getUpc() + "'");
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                cn.setAutoCommit(true);
                throw ex;
            }
        }
    }

    public void agregar(Upc u) throws SQLException {
        try (Connection cn = ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                int actual = 1;
                ResultSet rs = st.executeQuery("SELECT upc FROM " + this.tabla + " WHERE idProducto=" + u.getIdProducto() + " AND actual=1");
                if (rs.next()) {
                    if (u.isActual()) {
                        st.executeUpdate("UPDATE " + this.tabla + " SET actual=0 WHERE idProducto=" + u.getIdProducto());
                    } else {
                        actual = 0;
                    }
                }
                st.executeUpdate("INSERT INTO " + this.tabla + " (upc, idProducto, actual) "
                        + "VALUES ('" + u.getUpc() + "', " + u.getIdProducto() + ", " + actual + ")");
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                cn.setAutoCommit(true);
                if (ex.getErrorCode() == 2601) {
                    throw new SQLException("UPC ya existe");
                } else {
                    throw ex;
                }
            }
            cn.setAutoCommit(true);
        }
    }

    public Upc obtenerUpc(int idProducto) throws SQLException {
        Upc upc = null;
        Connection cn = ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT upc, idProducto, actual FROM " + this.tabla + " WHERE idProducto=" + idProducto + " AND actual=1");
            if (rs.next()) {
                upc = new Upc(rs.getString("upc"), rs.getInt("idProducto"), rs.getBoolean("actual"));
            } else {
                rs = st.executeQuery("SELECT upc, idProducto, actual FROM " + this.tabla + " WHERE idProducto=" + idProducto);
                if (rs.next()) {
                    upc = new Upc(rs.getString("upc"), rs.getInt("idProducto"), rs.getBoolean("actual"));
                } else {
                    upc = new Upc("SELECCIONE", idProducto, false);
                }
            }
        } finally {
            cn.close();
        }
        return upc;
    }

    public ArrayList<Upc> obtenerUpcs(int idProducto) throws SQLException {
        ArrayList<Upc> upcs = new ArrayList<>();
        Connection cn = ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT upc, idProducto, actual FROM " + this.tabla + " WHERE idProducto=" + idProducto);
            while (rs.next()) {
                upcs.add(new Upc(rs.getString("upc"), rs.getInt("idProducto"), rs.getBoolean("actual")));
            }
        } finally {
            cn.close();
        }
        return upcs;
    }

    public Upc obtenerUpc(String upc) throws SQLException {
        Upc u = null;
        Connection cn = ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT upc, idProducto, actual FROM " + this.tabla + " WHERE upc='" + upc + "'");
            if (rs.next()) {
                u = new Upc(rs.getString("upc"), rs.getInt("idProducto"), rs.getBoolean("actual"));
            }
        } finally {
            cn.close();
        }
        return u;
    }
}
