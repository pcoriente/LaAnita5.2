package comprobantes.dao;

import comprobantes.to.TOComprobante;
import java.sql.Connection;
import java.sql.Date;
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
 * @author jesc
 */
public class DAOComprobantes {

    int idUsuario;
    private DataSource ds = null;

    public DAOComprobantes() throws NamingException {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
            this.idUsuario = usuarioSesion.getUsuario().getId();

            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
        } catch (NamingException ex) {
            throw (ex);
        }
    }

    public void eliminar(int idComprobante) throws SQLException {
        String strSQL = "SELECT M.idMovto\n"
                + "FROM movimientos M\n"
                + "INNER JOIN comprobantes C ON C.idComprobante=M.idComprobante\n"
                + "WHERE C.idComprobante=" + idComprobante;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    throw new SQLException("El comprobante no puede ser eliminado, esta siendo utilizado !!!");
                }
                strSQL = "SELECT M.idMovtoAlmacen\n"
                        + "FROM movimientosAlmacen M\n"
                        + "INNER JOIN comprobantes C ON C.idComprobante=M.idComprobante\n"
                        + "WHERE C.idComprobante=" + idComprobante;
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    throw new SQLException("El comprobante no puede ser eliminado, utilizado en movimientos de almacen !!!");
                }
                strSQL = "DELETE FROM comprobantes WHERE idComprobante=" + idComprobante;
                st.executeUpdate(strSQL);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void cerrarComprobanteAlmacen(int idComprobante) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery("SELECT * FROM movimientosAlmacen WHERE idTipo=1 AND idComprobante=" + idComprobante + " AND estatus=0");
                if (rs.next()) {
                    throw new SQLException("El comprobante tiene movimientos pendientes !!!");
                }
                st.executeUpdate("UPDATE comprobantes SET cerradoAlmacen=1, estatus=7 WHERE idComprobante=" + idComprobante);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void cerrarComprobanteOficina(int idComprobante) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery("SELECT * FROM movimientos WHERE idTipo=1 AND idComprobante=" + idComprobante + " AND estatus=0");
                if (rs.next()) {
                    throw new SQLException("El comprobante tiene movimientos pendientes !!!");
                }
                st.executeUpdate("UPDATE comprobantes SET cerradoOficina=1, estatus=7 WHERE idComprobante=" + idComprobante);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void liberaComprobante(int idComprobante) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                st.executeUpdate("UPDATE comprobantes SET propietario=0 WHERE idComprobante=" + idComprobante);
            }
        }
    }

    public boolean asegurarComprobante(int idComprobante) throws SQLException {
        boolean ok = true;
        int propietario = 0;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery("SELECT propietario FROM comprobantes WHERE idComprobante=" + idComprobante);
                if (rs.next()) {
                    propietario = rs.getInt("propietario");
                    if (propietario == 0) {
                        st.executeUpdate("UPDATE comprobantes SET propietario=" + this.idUsuario + " WHERE idComprobante=" + idComprobante);
                    } else if (propietario != this.idUsuario) {
                        ok = false;
                    }
                } else {
                    throw new SQLException("No se encotro el comprobante");
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return ok;
    }

    public void modificar(TOComprobante to) throws SQLException {
        to.setIdUsuario(this.idUsuario);
        Date fechaFactura = new java.sql.Date(to.getFechaFactura().getTime());
        String strSQL = "UPDATE comprobantes\n"
                + "SET tipo=" + to.getTipo() + ", serie='" + to.getSerie() + "', numero='" + to.getNumero() + "', fechaFactura='" + fechaFactura.toString() + "', idMoneda=" + to.getIdMoneda() + ", fecha=GETDATE(), idUsuario=" + to.getIdUsuario() + "\n"
                + "WHERE idComprobante=" + to.getIdComprobante();
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);
            }
        }
    }

    public void agregar(TOComprobante to) throws SQLException {
        to.setIdUsuario(this.idUsuario);
        to.setPropietario(this.idUsuario);
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                comprobantes.Comprobantes.agregar(cn, to);
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                if (e.getErrorCode() == 2601) {
                    throw new SQLException("El comprobante ya existe !!!");
                } else {
                    throw (e);
                }
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public ArrayList<TOComprobante> completeComprobantes(int idTipoMovto, int idEmpresa, int idReferencia, String strComprobante) throws SQLException {
        ArrayList<TOComprobante> comprobantes = new ArrayList<>();
        String strSQL = "SELECT *\n"
                + "FROM comprobantes\n"
                + "WHERE idTipoMovto=" + idTipoMovto + " AND idEmpresa=" + idEmpresa + " AND idReferencia=" + idReferencia + " AND numero like '%" + strComprobante + "%'\n"
                + "ORDER BY numero";
        try (Connection cn = ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    comprobantes.add(this.construir(rs));
                }
            }
        }
        return comprobantes;
    }

    public ArrayList<TOComprobante> obtenerComprobantes(int idTipoMovto, int idEmpresa, int idReferencia) throws SQLException {
        ArrayList<TOComprobante> comprobantes = new ArrayList<>();
        String strSQL = "SELECT * FROM comprobantes\n"
                + "WHERE idTipoMovto=" + idTipoMovto + " AND idEmpresa=" + idEmpresa + " AND idReferencia=" + idReferencia + "\n"
                + "ORDER BY comprobante";
        try (Connection cn = ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    comprobantes.add(this.construir(rs));
                }
            }
        }
        return comprobantes;
    }

    public TOComprobante obtenerComprobante(int idComprobante) throws SQLException {
        TOComprobante f = null;
        String strSQL = "SELECT * FROM comprobantes WHERE idComprobante=" + idComprobante;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    f = construir(rs);
                }
            }
        }
        return f;
    }

    private TOComprobante construir(ResultSet rs) throws SQLException {
        TOComprobante to = new TOComprobante();
        to.setIdComprobante(rs.getInt("idComprobante"));
        to.setIdTipoMovto(rs.getInt("idTipoMovto"));
        to.setIdEmpresa(rs.getInt("idEmpresa"));
        to.setIdReferencia(rs.getInt("idReferencia"));
        to.setTipo(rs.getInt("tipo"));
        to.setSerie(rs.getString("serie"));
        to.setNumero(rs.getString("numero"));
        to.setFechaFactura(new java.util.Date(rs.getTimestamp("fechaFactura").getTime()));
        to.setIdMoneda(rs.getInt("idMoneda"));
        to.setIdUsuario(this.idUsuario);
        to.setPropietario(rs.getInt("propietario"));
        to.setCerradoOficina(rs.getBoolean("cerradoOficina"));
        to.setCerradoAlmacen(rs.getBoolean("cerradoAlmacen"));
        to.setEstatus(rs.getInt("estatus"));
        return to;
    }

    public int getIdUsuario() {
        return idUsuario;
    }
}
