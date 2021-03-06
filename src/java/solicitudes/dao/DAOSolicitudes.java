package solicitudes.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import solicitudes.Solicitudes;
import solicitudes.to.TOSolicitud;
import solicitudes.to.TOSolicitudProducto;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOSolicitudes {

    int idUsuario, idCedis;
    private DataSource ds = null;

    public DAOSolicitudes() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
        this.idUsuario = usuarioSesion.getUsuario().getId();
        this.idCedis = usuarioSesion.getUsuario().getIdCedis();

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
    }

    public void eliminar(TOSolicitud to) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "DELETE FROM solicitudes WHERE idSolicitud=" + to.getIdSolicitud();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM solicitudesDetalle WHERE idSolicitud=" + to.getIdSolicitud();
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

    public void liberar(int idSolicitud) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                solicitudes.Solicitudes.liberarSolicitud(cn, idSolicitud, this.idUsuario);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void grabar(TOSolicitud to) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                to.setIdUsuario(this.idUsuario);
                to.setPropietario(this.idUsuario);

                Solicitudes.grabar(cn, to);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private TOSolicitudProducto construirProducto(ResultSet rs) throws SQLException {
        TOSolicitudProducto toProd = new TOSolicitudProducto();
        toProd.setIdSolicitud(rs.getInt("idSolicitud"));
        toProd.setIdProducto(rs.getInt("idEmpaque"));
        toProd.setCantSolicitada(rs.getDouble("cantSolicitada"));
        return toProd;
    }

    public ArrayList<TOSolicitudProducto> obtenerDetalle(TOSolicitud toSolicitud) throws SQLException {
        String strSQL = "";
        ArrayList<TOSolicitudProducto> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "SELECT * FROM solicitudesDetalle WHERE idSolicitud=" + toSolicitud.getIdSolicitud();
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    detalle.add(this.construirProducto(rs));
                }
                int propietario = 0;
                strSQL = "SELECT propietario, estatus, idUsuarioOrigen FROM solicitudes WHERE idSolicitud=" + toSolicitud.getIdSolicitud();
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    toSolicitud.setIdUsuario(this.idUsuario);
                    toSolicitud.setIdUsuarioOrigen(rs.getInt("idUsuarioOrigen"));
                    toSolicitud.setEstatus(rs.getInt("estatus"));
                    propietario = rs.getInt("propietario");
                    if (propietario != 0) {
                        toSolicitud.setPropietario(propietario);
                    } else {
                        strSQL = "UPDATE solicitudes SET propietario=" + this.idUsuario + "\n"
                                + "WHERE idSolicitud=" + toSolicitud.getIdSolicitud();
                        st.executeUpdate(strSQL);
                        toSolicitud.setPropietario(this.idUsuario);
                    }
                } else {
                    throw new SQLException("La solicitud ya no existe !!!");
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return detalle;
    }

    public void modificarProducto(TOSolicitudProducto to) throws SQLException {
        String strSQL = "UPDATE solicitudesDetalle\n"
                + "SET cantSolicitada=" + to.getCantSolicitada() + "\n"
                + "WHERE idSolicitud=" + to.getIdSolicitud() + " AND idEmpaque=" + to.getIdProducto();
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);
            }
        }
    }

    public void agregarProducto(TOSolicitudProducto to) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            Solicitudes.agregarProducto(cn, to);
        }
    }

    private TOSolicitud construir(ResultSet rs) throws SQLException {
        TOSolicitud toSolicitud = new TOSolicitud();
        solicitudes.Solicitudes.construir(toSolicitud, rs);
        return toSolicitud;
    }

    public ArrayList<TOSolicitud> obtenerSolicitudes(int idAlmacen, int estatus, Date fechaInicial) throws SQLException {
        String condicion = ">=1";
        if (estatus == 0) {
            condicion = "=0";
        }
        if (fechaInicial == null) {
            fechaInicial = new Date();
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TOSolicitud> solicitudes = new ArrayList<>();
        String strSQL = "SELECT *\n"
                + "FROM solicitudes\n"
                + "WHERE idAlmacen=" + idAlmacen + " AND estatus" + condicion + " AND envio=0\n";
        if (estatus != 0) {
            strSQL += "         AND CONVERT(date, fecha) >= '" + format.format(fechaInicial) + "'\n";
        }
        strSQL += "ORDER BY fecha DESC";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    solicitudes.add(this.construir(rs));
                }
            }
        }
        return solicitudes;
    }

    public void agregar(TOSolicitud toMov) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                toMov.setEstatus(0);
                toMov.setIdUsuario(this.idUsuario);
                toMov.setPropietario(this.idUsuario);

                solicitudes.Solicitudes.agrega(cn, toMov);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }
}
