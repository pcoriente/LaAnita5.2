package tiendas.dao;

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
import tiendas.Tiendas;
import tiendas.to.TOTienda;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOTiendas {

    private DataSource ds;
    private int idCedis;

    public DAOTiendas() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
        this.idCedis = usuarioSesion.getUsuario().getIdCedis();
    }

    public void modificar(TOTienda to) throws SQLException {
        String strSQL = "UPDATE clientesTiendasCodigos SET codigoTienda=" + to.getCodigoTienda() + " "
                + "WHERE idCliente=" + to.getIdCliente() + " AND idFormato=" + to.getIdFormato() + " AND idTienda=" + to.getIdTienda();
        try (Connection cn = ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                int nRegs = st.executeUpdate(strSQL);
                if (nRegs == 0) {
                    strSQL = this.agregarCodigo(to);
                    st.executeUpdate(strSQL);
                }
                strSQL = "UPDATE clientesTiendas "
                        + "SET tienda='" + to.getTienda() + "', idAgente=" + to.getIdAgente() + ", idRuta=" + to.getIdRuta() + ", idImpuestoZona=" + to.getIdImpuestoZona()
                        + "WHERE idTienda=" + to.getIdTienda();
                st.executeUpdate(strSQL);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw (ex);
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private String agregarCodigo(TOTienda to) {
        return "INSERT INTO clientesTiendasCodigos (idCliente, idFormato, idTienda, codigoTienda) "
                + "VALUES(" + to.getIdCliente() + ", " + to.getIdFormato() + ", " + to.getIdTienda() + ", " + to.getCodigoTienda() + ")";
    }

    public int agregar(TOTienda to) throws SQLException {
        String strSQL = "INSERT INTO clientesTiendas (tienda, idDireccion, idCliente, idFormato, idAgente, idRuta, idImpuestoZona, codigoCliente, estado) "
                + "VALUES('" + to.getTienda() + "', " + to.getIdDireccion() + ", " + to.getIdCliente() + ", " + to.getIdFormato() + ", " + to.getIdAgente() + ", " + to.getIdRuta() + ", " + to.getIdImpuestoZona() + ", " + to.getCodigoTienda() + ", " + to.getEstado() + ")";
        try (Connection cn = ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);
                ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idTienda");
                if (rs.next()) {
                    to.setIdTienda(rs.getInt("idTienda"));
                }
                if (to.getCodigoTienda() != 0) {
                    st.executeUpdate(this.agregarCodigo(to));
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw (ex);
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return to.getIdTienda();
    }

    public ArrayList<TOTienda> obtenerTiendasCedisCliente(int idCliente) throws SQLException {
        ArrayList<TOTienda> tos = new ArrayList<>();
        String strSQL = Tiendas.sqlTienda() + "\n"
                + "INNER JOIN agentes A ON A.idAgente=T.idAgente\n"
                + "WHERE A.idCedis=" + this.idCedis + " AND T.idCliente=" + idCliente + "\n"
                + "ORDER BY T.tienda";
        Connection cn = ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                tos.add(Tiendas.construir(rs));
            }
        } finally {
            cn.close();
        }
        return tos;
    }

    public ArrayList<TOTienda> obtenerTiendasFormato(int idFormato) throws SQLException {
        ArrayList<TOTienda> tos = new ArrayList<>();
        String strSQL = Tiendas.sqlTienda() + " WHERE T.idFormato=" + idFormato;
        Connection cn = ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                tos.add(Tiendas.construir(rs));
            }
        } finally {
            cn.close();
        }
        return tos;
    }

    public TOTienda obtenerTienda(int idTienda) throws SQLException {
        TOTienda to = null;
        Connection cn = ds.getConnection();
        try {
            to = Tiendas.obtenerTienda(cn, idTienda);
        } finally {
            cn.close();
        }
        return to;
    }
}
