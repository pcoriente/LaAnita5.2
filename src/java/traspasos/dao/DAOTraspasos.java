package traspasos.dao;

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
import movimientos.to.TOMovimientoOficina;
import traspasos.to.TOTraspaso;
import traspasos.to.TOTraspasoProducto;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOTraspasos {

    int idUsuario, idCedis;
    private DataSource ds = null;

    public DAOTraspasos() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
        this.idUsuario = usuarioSesion.getUsuario().getId();
        this.idCedis = usuarioSesion.getUsuario().getIdCedis();

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
    }

    public void grabarTraspaso(TOMovimientoOficina toTraspaso) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                toTraspaso.setIdUsuario(this.idUsuario);
                toTraspaso.setPropietario(0);
                toTraspaso.setEstatus(5);

                toTraspaso.setFolio(movimientos.Movimientos.obtenMovimientoFolioAlmacen(cn, toTraspaso.getIdAlmacen(), toTraspaso.getIdTipo()));
                movimientos.Movimientos.grabaMovimientoAlmacen(cn, toTraspaso);

                toTraspaso.setFolio(movimientos.Movimientos.obtenMovimientoFolioOficina(cn, toTraspaso.getIdAlmacen(), toTraspaso.getIdTipo()));
                movimientos.Movimientos.grabaMovimientoOficina(cn, toTraspaso);

                strSQL = "UPDATE solicitudes SET idUsuario=" + this.idUsuario + ", estatus=5 WHERE idMovto=" + toTraspaso.getReferencia();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalleAlmacen WHERE idMovtoAlmacen=" + toTraspaso.getIdMovtoAlmacen() + " AND cantidad=0";
                st.executeUpdate(strSQL);

                strSQL = "UPDATE D\n"
                        + "SET fecha=GETDATE(), existenciaAnterior=L.existencia\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes L ON L.idAlmacen=M.idAlmacen AND L.idEmpaque=D.idEmpaque AND L.lote=D.lote\n"
                        + "WHERE D.idMovtoAlmacen=" + toTraspaso.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE L\n"
                        + "SET existencia=L.existencia-D.cantidad, separados=L.separados-D.cantidad\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes L ON L.idAlmacen=M.idAlmacen AND L.idEmpaque=D.idEmpaque AND L.lote=D.lote\n"
                        + "WHERE D.idMovtoAlmacen=" + toTraspaso.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalle WHERE idMovto=" + toTraspaso.getIdMovto() + " AND cantFacturada=0";
                st.executeUpdate(strSQL);

                strSQL = "UPDATE D\n"
                        + "SET fecha=GETDATE(), existenciaAnterior=E.existencia\n"
                        + "     , costoPromedio=EE.costoUnitarioPromedio, costo=EE.costoUnitarioPromedio, unitario=EE.costoUnitarioPromedio\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN almacenesEmpaques E ON E.idAlmacen=M.idAlmacen AND E.idEmpaque=D.idEmpaque\n"
                        + "INNER JOIN empresasEmpaques EE ON EE.idEmpresa=M.idEmpresa AND EE.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toTraspaso.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE A\n"
                        + "SET existencia=A.existencia-D.cantFacturada, separados=A.separados-D.cantFacturada\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toTraspaso.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE E\n"
                        + "SET existencia=E.existencia-D.cantFacturada\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toTraspaso.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE E\n"
                        + "SET costoUnitarioPromedio=0\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toTraspaso.getIdMovto() + " AND E.existencia=0";
                st.executeUpdate(strSQL);

                // ------------------------- SECCION: CREAR RECEPCION ---------------------

                TOMovimientoOficina toRecepcion = new TOMovimientoOficina();
                toRecepcion.setIdTipo(9);
                toRecepcion.setIdEmpresa(toTraspaso.getIdEmpresa());
                toRecepcion.setIdAlmacen(toTraspaso.getIdReferencia());
                toRecepcion.setIdUsuario(toTraspaso.getIdUsuario());
                toRecepcion.setTipoDeCambio(toTraspaso.getTipoDeCambio());
                toRecepcion.setIdReferencia(toTraspaso.getIdAlmacen());
                toRecepcion.setEstatus(4);

                toRecepcion.setReferencia(toTraspaso.getIdMovtoAlmacen());
                movimientos.Movimientos.agregaMovimientoAlmacen(cn, toRecepcion, false);
                
                toRecepcion.setReferencia(toTraspaso.getIdMovto());
                movimientos.Movimientos.agregaMovimientoOficina(cn, toRecepcion, false);

                strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantFacturada, cantSinCargo, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior)\n"
                        + "SELECT " + toRecepcion.getIdMovto() + ", idEmpaque, cantFacturada, cantSinCargo, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, '', 0\n"
                        + "FROM movimientosDetalle WHERE idMovto=" + toTraspaso.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "INSERT INTO movimientosDetalleAlmacen (idMovtoAlmacen, idEmpaque, lote, cantidad, fecha, existenciaAnterior)\n"
                        + "SELECT " + toRecepcion.getIdMovtoAlmacen() + ", MD.idEmpaque, MD.lote, MD.cantidad, '', 0\n"
                        + "FROM movimientosDetalleAlmacen MD\n"
                        + "WHERE MD.idMovtoAlmacen=" + toTraspaso.getIdMovtoAlmacen();
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

    public void cancelar(int idMovto, int idMovtoAlmacen) throws SQLException {
//        int idMovtoAlmacen = 0;
//        String strSQL = "SELECT idMovtoAlmacen FROM movimientos WHERE idMovto=" + idMovto;
        String strSQL = "";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
//                ResultSet rs = st.executeQuery(strSQL);
//                if (rs.next()) {
//                    idMovtoAlmacen = rs.getInt("idMovtoAlmacen");
//                } else {
//                    throw new SQLException("No se encontró movimiento de almacen !!!");
//                }
                strSQL = "UPDATE S\n"
                        + "SET S.idUsuarioOrigen=" + this.idUsuario + ", S.estatus=6\n"
                        + "FROM movimientos M\n"
                        + "INNER JOIN solicitudes S ON S.idMovto=M.referencia\n"
                        + "WHERE M.idMovto=" + idMovto;
                st.executeUpdate(strSQL);
                
                strSQL = "UPDATE A\n"
                        + "SET separados=A.separados-D.cantidad\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                        + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen;
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalleAlmacen WHERE idMovtoAlmacen=" + idMovtoAlmacen;
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosAlmacen WHERE idMovtoAlmacen=" + idMovtoAlmacen;
                st.executeUpdate(strSQL);

                strSQL = "UPDATE A\n"
                        + "SET separados=A.separados-D.cantFacturada\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + idMovto;
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalle WHERE idMovto=" + idMovto;
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientos WHERE idMovto=" + idMovto;
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

    public void procesar(TOTraspaso toMov) throws SQLException {
        String strSQL = "";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                toMov.setFolio(0);
                toMov.setIdUsuario(this.idUsuario);
                toMov.setPropietario(this.idUsuario);
                toMov.setEstatus(4);

                movimientos.Movimientos.agregaMovimientoAlmacen(cn, toMov, false);
                movimientos.Movimientos.agregaMovimientoOficina(cn, toMov, false);

                strSQL = "INSERT INTO movimientosDetalle\n"
                        + "SELECT " + toMov.getIdMovto() + " AS idMovto, S.idEmpaque, 0 AS cantFacturada, 0 AS cantSinCargo, 0 AS costoPromedio, 0 AS costo\n"
                        + "	, 0 AS desctoProducto1, 0 AS desctoProducto2, 0 AS desctoConfidencial, 0 AS unitario\n"
                        + "	, P.idImpuesto AS idImpuestoGrupo, '' AS fecha, 0 AS existentencioAnterior\n"
                        + "FROM solicitudesDetalle S\n"
                        + "INNER JOIN empaques E ON E.idEmpaque=S.idEmpaque\n"
                        + "INNER JOIN productos P ON P.idProducto=E.idProducto\n"
                        + "WHERE S.idMovto=" + toMov.getReferencia();
                st.executeUpdate(strSQL);
                
                strSQL="UPDATE solicitudes SET estatus=4, idUsuarioOrigen="+this.idUsuario+" WHERE idMovto="+toMov.getReferencia();
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

    public void rechazar(int idSolicitud) throws SQLException {
        String strSQL = "UPDATE solicitudes SET idUsuarioOrigen=" + this.idUsuario + ", estatus=2 WHERE idMovto=" + idSolicitud;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);
            }
        }
    }

    public ArrayList<TOTraspasoProducto> obtenerDetalleTraspaso(int idMovto) throws SQLException {
        ArrayList<TOTraspasoProducto> detalle = new ArrayList<>();
        String strSQL = "SELECT S.cantSolicitada, D.*\n"
                + "FROM movimientosDetalle D\n"
                + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                + "INNER JOIN solicitudesDetalle S ON S.idMovto=M.referencia AND S.idEmpaque=D.idEmpaque\n"
                + "WHERE D.idMovto=" + idMovto;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    detalle.add(this.construir(rs));
                }
            }
        }
        return detalle;
    }

    private TOTraspasoProducto construir(ResultSet rs) throws SQLException {
        TOTraspasoProducto toProd = new TOTraspasoProducto();
        toProd.setCantSolicitada(rs.getDouble("cantSolicitada"));
        movimientos.Movimientos.construirProducto(rs, toProd);
        return toProd;
    }

    public ArrayList<TOTraspasoProducto> obtenerDetalleSolicitud(int idSolicitud) throws SQLException {
        ArrayList<TOTraspasoProducto> detalle = new ArrayList<>();
        String strSQL = "SELECT 0 AS idMovto, S.idEmpaque, S.cantSolicitada, 0 AS cantFacturada, 0 AS cantSinCargo, 0 AS costoPromedio, 0 AS costo\n"
                + "	, 0 AS desctoProducto1, 0 AS desctoProducto2, 0 AS desctoConfidencial, 0 AS unitario, P.idImpuesto AS idImpuestoGrupo\n"
                + "FROM solicitudesDetalle S\n"
                + "INNER JOIN empaques E ON E.idEmpaque=S.idEmpaque\n"
                + "INNER JOIN productos P ON P.idProducto=E.idProducto\n"
                + "WHERE S.idMovto=" + idSolicitud;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    detalle.add(this.construir(rs));
                }
            }
        }
        return detalle;
    }

    public ArrayList<TOTraspaso> obtenerTraspasos(int idAlmacen) throws SQLException {
        ArrayList<TOTraspaso> traspasos = new ArrayList<>();
        String strSQL = "SELECT S.folio AS solicitudFolio, S.fecha AS solicitudFecha, S.idUsuario AS solicitudIdUsuario\n"
                + "     , S.estatus AS solicitudEstatus, M.*\n"
                + "FROM movimientos M\n"
                + "INNER JOIN solicitudes S ON S.idMovto=M.referencia\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=35 AND S.estatus>1\n"
                + "ORDER BY S.fecha";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    traspasos.add(this.construirTraspaso(rs));
                }
            }
        }
        return traspasos;
    }

    private TOTraspaso construirTraspaso(ResultSet rs) throws SQLException {
        TOTraspaso toMov = new TOTraspaso();
        toMov.setSolicitudFolio(rs.getInt("solicitudFolio"));
        toMov.setSolicitudFecha(new java.util.Date(rs.getTimestamp("solicitudFecha").getTime()));
        toMov.setSolicitudIdUsuario(rs.getInt("solicitudIdUsuario"));
        toMov.setSolicitudEstatus(rs.getInt("solicitudEstatus"));
        movimientos.Movimientos.construirMovimientoOficina(rs, toMov);
        return toMov;
    }

    public ArrayList<TOTraspaso> obtenerSolicitudes(int idAlmacenOrigen) throws SQLException {
        ArrayList<TOTraspaso> solicitudes = new ArrayList<>();
        String strSQL = "SELECT S.folio AS solicitudFolio, S.fecha AS solicitudFecha, S.idUsuario AS solicitudIdUsuario\n"
                + "     , S.estatus AS solicitudEstatus\n"
                + "     , 0 AS idMovto, 35 AS idTipo, S.idEmpresa, S.idAlmacenOrigen AS idAlmacen, 0 AS folio\n"
                + "     , 0 AS idComprobante, 0 AS idImpuestoZona, 0 AS desctoComercial, 0 AS desctoProntoPago, GETDATE() AS fecha\n"
                + "     , " + this.idUsuario + " AS idUsuario, 1 AS tipoDeCambio, S.idAlmacen AS idReferencia, S.idMovto AS referencia\n"
                + "     , 0 AS propietario, 0 AS estatus, 0 AS idMovtoAlmacen\n"
                + "FROM solicitudes S\n"
                + "WHERE S.idAlmacenOrigen=" + idAlmacenOrigen + " AND S.estatus=1\n"
                + "ORDER BY S.fecha";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    solicitudes.add(this.construirTraspaso(rs));
                }
            }
        }
        return solicitudes;
    }
}
