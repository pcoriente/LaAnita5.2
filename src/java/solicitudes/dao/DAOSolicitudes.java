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
import solicitudes.to.TOSolicitud;
import solicitudes.to.TOSolicitudProducto;
import traspasos.to.TOTraspasoProducto;
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

//    private TOTraspasoProducto construirProducto(ResultSet rs) throws SQLException {
//        TOTraspasoProducto toProd = new TOTraspasoProducto();
//        toProd.setCantSolicitada(rs.getDouble("cantSolicitada"));
//        movimientos.Movimientos.construirProducto(rs, toProd);
//        return toProd;
//    }
//
//    private ArrayList<TOTraspasoProducto> obtenDetalleTraspaso(Connection cn, int idMovto) throws SQLException {
//        String strSQL = "";
//        ArrayList<TOTraspasoProducto> detalle = new ArrayList<>();
//        try (Statement st = cn.createStatement()) {
//            strSQL = "SELECT D.*, S.cantSolicitada\n"
//                    + "FROM movimientosDetalle D\n"
//                    + "LEFT JOIN solicitudes T ON T.idMovto=D.idMovto AND T.idEmpaque=D.idEmpaque\n"
//                    + "WHERE D.idMovto=" + idMovto;
//            ResultSet rs = st.executeQuery(strSQL);
//            while (rs.next()) {
//                detalle.add(this.construirProducto(rs));
//            }
//        }
//        return detalle;
//    }
//
//    public ArrayList<TOTraspasoProducto> obtenerDetalleTraspaso(int idMovto) throws SQLException {
//        ArrayList<TOTraspasoProducto> detalle = new ArrayList<>();
//        try (Connection cn = this.ds.getConnection()) {
//            detalle = this.obtenDetalleTraspaso(cn, idMovto);
//        }
//        return detalle;
//    }
//
//    public ArrayList<TOSolicitud> obtenerSolicitudesTraspaso(int idAlmacenOrigen) throws SQLException {
//        String strSQL = "SELECT *\n"
//                + "FROM solicitudes\n"
//                + "WHERE idAlmacenOrigen=" + idAlmacenOrigen + " AND estatus IN (1, 3)\n"
//                + "ORDER BY fecha DESC";
//        ArrayList<TOSolicitud> solicitudes = new ArrayList<>();
//        try (Connection cn = this.ds.getConnection()) {
//            try (Statement st = cn.createStatement()) {
//                ResultSet rs = st.executeQuery(strSQL);
//                while (rs.next()) {
//                    solicitudes.add(this.construirSolicitud(rs));
//                }
//            }
//        }
//        return solicitudes;
//    }

    public void eliminarSolicitud(TOSolicitud to) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "DELETE FROM solicitudes WHERE idMovto=" + to.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM solicitudesDetalle WHERE idMovto=" + to.getIdMovto();
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

    public void grabarSolicitud(TOSolicitud to) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                to.setEstatus(1);
                to.setIdUsuario(this.idUsuario);
                to.setPropietario(0);
                to.setFolio(movimientos.Movimientos.obtenMovimientoFolioOficina(cn, to.getIdAlmacen(), 53));

                String strSQL = "UPDATE solicitudes\n"
                        + "SET folio=" + to.getFolio() + ", fecha=GETDATE(), idUsuario=" + to.getIdUsuario() + ", propietario=" + to.getPropietario() + ", estatus=" + to.getEstatus() + "\n"
                        + "WHERE idMovto=" + to.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "SELECT fecha FROM solicitudes WHERE idMovto=" + to.getIdMovto();
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    to.setFecha(new java.util.Date(rs.getDate("fecha").getTime()));
                }
//                TOMovimientoAlmacen toAlm = new TOMovimientoAlmacen();
//                toAlm.setIdEmpresa(to.getIdEmpresa());
//                toAlm.setIdAlmacen(to.getIdAlmacenOrigen());
//                toAlm.setIdReferencia(to.getIdAlmacen());
//                toAlm.setReferencia(to.getIdMovto());
//                movimientos.Movimientos.agregaMovimientoAlmacen(cn, toAlm, false);
//                
//                TOMovimientoOficina toOfis = new TOMovimientoOficina();
//                toOfis.setIdEmpresa(to.getIdEmpresa());
//                toOfis.setIdAlmacen(to.getIdAlmacenOrigen());
//                toOfis.setIdReferencia(to.getIdAlmacen());
//                toOfis.setReferencia(to.getIdMovto());
//                toOfis.setIdMovtoAlmacen(toAlm.getIdMovtoAlmacen());
//                movimientos.Movimientos.agregaMovimientoOficina(cn, toOfis, false);

                strSQL = "DELETE FROM solicitudesDetalle WHERE idMovto=" + to.getIdMovto() + " AND cantSolicitada=0";
                st.executeUpdate(strSQL);

//                strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantFacturada, cantSinCargo, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior)\n"
//                        + "SELECT " + toOfis.getIdMovto() + ", SD.idEmpaque, 0, 0, 0, 0, 0, 0, 0, 0, P.idImpuesto, '', 0 \n"
//                        + "FROM solicitudesDetalle SD\n"
//                        + "INNER JOIN empaques E ON E.idEmpaque=SD.idEmpaque\n"
//                        + "INNER JOIN productos P ON P.idProducto=E.idProducto\n"
//                        + "WHERE SD.idMovto=" + to.getIdMovto();
//                st.executeUpdate(strSQL);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private TOSolicitudProducto construirSolicitudProducto(ResultSet rs) throws SQLException {
        TOSolicitudProducto toProd = new TOSolicitudProducto();
        toProd.setIdMovto(rs.getInt("idMovto"));
        toProd.setIdProducto(rs.getInt("idEmpaque"));
        toProd.setCantSolicitada(rs.getDouble("cantSolicitada"));
        return toProd;
    }

    private ArrayList<TOSolicitudProducto> obtenDetalleSolicitud(Connection cn, int idMovto) throws SQLException {
        String strSQL = "";
        ArrayList<TOSolicitudProducto> detalle = new ArrayList<>();
        try (Statement st = cn.createStatement()) {
            strSQL = "SELECT * FROM solicitudesDetalle D WHERE D.idMovto=" + idMovto;
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                detalle.add(this.construirSolicitudProducto(rs));
            }
        }
        return detalle;
    }

    public ArrayList<TOSolicitudProducto> obtenerDetalleSolicitud(int idMovto) throws SQLException {
        ArrayList<TOSolicitudProducto> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            detalle = this.obtenDetalleSolicitud(cn, idMovto);
        }
        return detalle;
    }

    public void modificarSolicitudProducto(TOSolicitudProducto to) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                strSQL = "UPDATE solicitudesDetalle\n"
                        + "SET cantSolicitada=" + to.getCantSolicitada() + "\n"
                        + "WHERE idMovto=" + to.getIdMovto() + " AND idEmpaque=" + to.getIdProducto();
                st.executeUpdate(strSQL);
            }
        }
    }

    public void agregarSolicitudProducto(TOSolicitudProducto to) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                strSQL = "INSERT INTO solicitudesDetalle (idMovto, idEmpaque, cantSolicitada)\n"
                        + "VALUES (" + to.getIdMovto() + ", " + to.getIdProducto() + ", " + to.getCantSolicitada() + ")";
                st.executeUpdate(strSQL);
            }
        }
    }

    private TOSolicitud construirSolicitud(ResultSet rs) throws SQLException {
        TOSolicitud toMov = new TOSolicitud();
        toMov.setIdMovto(rs.getInt("idMovto"));
        toMov.setIdEmpresa(rs.getInt("idEmpresa"));
        toMov.setIdAlmacen(rs.getInt("idAlmacen"));
        toMov.setFolio(rs.getInt("folio"));
        toMov.setFecha(new java.util.Date(rs.getDate("fecha").getTime()));
        toMov.setIdAlmacenOrigen(rs.getInt("idAlmacenOrigen"));
        toMov.setIdUsuarioOrigen(rs.getInt("idUsuarioOrigen"));
        toMov.setIdUsuario(rs.getInt("idUsuario"));
        toMov.setPropietario(rs.getInt("propietario"));
        toMov.setEstatus(rs.getInt("estatus"));
        return toMov;
    }

    public ArrayList<TOSolicitud> obtenerSolicitudes(int idAlmacen, int estatus, Date fechaInicial) throws SQLException {
        String condicion=" = 0";
        if(estatus>=1) {
            condicion=" >= 1";
        }
        ArrayList<TOSolicitud> solicitudes = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String strSQL = "SELECT *\n"
                + "FROM solicitudes\n"
                + "WHERE idAlmacen=" + idAlmacen + " AND estatus" + condicion + "\n"
                + "         AND CONVERT(date, fecha) <= '" + format.format(fechaInicial) + "'\n"
                + "ORDER BY fecha DESC";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    solicitudes.add(this.construirSolicitud(rs));
                }
            }
        }
        return solicitudes;
    }

    public void agregarSolicitud(TOSolicitud toMov) throws SQLException {
        String strSQL = "";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                toMov.setEstatus(0);
                toMov.setIdUsuario(this.idUsuario);
                toMov.setPropietario(this.idUsuario);
                strSQL = "INSERT INTO solicitudes (idEmpresa, idAlmacen, folio, fecha, idAlmacenOrigen, idUsuarioOrigen, idUsuario, propietario, estatus)\n"
                        + "VALUES (" + toMov.getIdEmpresa() + " ," + toMov.getIdAlmacen() + ", 0, GETDATE(), " + toMov.getIdAlmacenOrigen() + ", 0, " + toMov.getIdUsuario() + ", " + toMov.getPropietario() + ", " + toMov.getEstatus() + ")";
                st.executeUpdate(strSQL);

                ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
                if (rs.next()) {
                    toMov.setIdMovto(rs.getInt("idMovto"));
                }
                strSQL = "SELECT fecha FROM solicitudes WHERE idMovto=" + toMov.getIdMovto();
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    toMov.setFecha(new java.util.Date(rs.getTimestamp("fecha").getTime()));
                }
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
