package traspasos.dao;

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
import traspasos.to.TOTraspaso;
import traspasos.to.TOTraspasoProducto;
import traspasos.to.TOTraspasoProductoAlmacen;
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

    public void cerrarAlmacen(TOTraspaso toTraspaso) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                toTraspaso.setIdUsuario(this.idUsuario);
                toTraspaso.setPropietario(0);
                toTraspaso.setEstatus(7);

                toTraspaso.setFolio(movimientos.Movimientos.obtenMovimientoFolioAlmacen(cn, toTraspaso.getIdAlmacen(), toTraspaso.getIdTipo()));
                movimientos.Movimientos.grabaMovimientoAlmacen(cn, toTraspaso);
                
                strSQL="UPDATE movimientosAlmacen SET propietario=0 WHERE idMovtoAlmacen=" + toTraspaso.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);
                
                movimientos.Movimientos.actualizaDetalleAlmacen(cn, toTraspaso.getIdMovtoAlmacen(), false);
                
                strSQL = "UPDATE solicitudes SET estatus=7 WHERE idSolicitud=" + toTraspaso.getReferencia();
                st.executeUpdate(strSQL);

                // ------------------------- SECCION: CREAR RECEPCION ---------------------

                TOTraspaso toRecepcion = new TOTraspaso();
                toRecepcion.setIdTipo(9);
                toRecepcion.setIdEmpresa(toTraspaso.getIdEmpresa());
                toRecepcion.setIdAlmacen(toTraspaso.getIdReferencia());
                toRecepcion.setIdUsuario(toTraspaso.getIdUsuario());
                toRecepcion.setTipoDeCambio(toTraspaso.getTipoDeCambio());
                toRecepcion.setIdReferencia(toTraspaso.getIdAlmacen());
                toRecepcion.setEstatus(5);

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

    public void traspasarLote(int idAlmacen, TOTraspasoProductoAlmacen toOrigen, TOTraspasoProductoAlmacen toDestino, double cantTraspasar) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                movimientos.Movimientos.separar(cn, idAlmacen, toDestino, cantTraspasar, true);
                if (toDestino.getIdMovtoAlmacen() == 0) {
                    toDestino.setIdMovtoAlmacen(toOrigen.getIdMovtoAlmacen());
                    movimientos.Movimientos.agregaProductoAlmacen(cn, toDestino);
                }
                movimientos.Movimientos.liberar(cn, idAlmacen, toOrigen, cantTraspasar);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public ArrayList<TOTraspasoProductoAlmacen> obtenerLotesDisponibles(int idAlmacen, TOTraspasoProductoAlmacen toProd) throws SQLException {
        ArrayList<TOTraspasoProductoAlmacen> lotes = new ArrayList<>();
        String strSQL = "SELECT ISNULL(D.idMovtoAlmacen, 0) AS idMovtoAlmacen, L.idEmpaque, L.lote, ISNULL(D.cantidad, 0) AS cantidad, L.existencia-L.separados AS disponibles, L.fechaCaducidad\n"
                + "FROM almacenesLotes L\n"
                + "LEFT JOIN (SELECT * FROM movimientosDetalleAlmacen WHERE idMovtoAlmacen=" + toProd.getIdMovtoAlmacen() + " ) D ON D.idEmpaque=L.idEmpaque AND D.lote=L.lote\n"
                + "WHERE L.idAlmacen=" + idAlmacen + " AND L.idEmpaque=" + toProd.getIdProducto() + " AND L.lote!='" + toProd.getLote() + "' AND L.existencia-L.separados > 0\n"
                + "ORDER BY L.fechaCaducidad";
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                lotes.add(this.construirProductoAlmacen(rs));
            }
        } finally {
            cn.close();
        }
        return lotes;
    }

    private TOTraspasoProductoAlmacen construirProductoAlmacen(ResultSet rs) throws SQLException {
        TOTraspasoProductoAlmacen toProd = new TOTraspasoProductoAlmacen();
        toProd.setDisponibles(rs.getDouble("disponibles"));
        toProd.setFechaCaducidad(new java.util.Date(rs.getDate("fechaCaducidad").getTime()));
        movimientos.Movimientos.construirProductoAlmacen(rs, toProd);
        return toProd;
    }

    public ArrayList<TOTraspasoProductoAlmacen> obtenerDetalleAlmacen(TOTraspaso toMov) throws SQLException, NamingException {
        String strSQL = "SELECT D.*, 0 AS disponibles, A.fechaCaducidad\n"
                + "FROM movimientosDetalleAlmacen D\n"
                + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                + "WHERE D.idMovtoAlmacen=" + toMov.getIdMovtoAlmacen() + "\n"
                + "ORDER BY D.idEmpaque, A.fechaCaducidad";
        ArrayList<TOTraspasoProductoAlmacen> productos = new ArrayList<>();
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                productos.add(this.construirProductoAlmacen(rs));
            }
            int propietario = 0;
            strSQL = "SELECT propietario, estatus FROM movimientosAlmacen WHERE idMovtoAlmacen=" + toMov.getIdMovtoAlmacen();
            rs = st.executeQuery(strSQL);
            if (rs.next()) {
                toMov.setEstatus(rs.getInt("estatus"));
                propietario = rs.getInt("propietario");
                if (propietario == 0) {
                    strSQL = "UPDATE movimientosAlmacen SET propietario=" + this.idUsuario + "\n"
                            + "WHERE idMovtoAlmacen=" + toMov.getIdMovtoAlmacen();
                    st.executeUpdate(strSQL);
                    toMov.setPropietario(this.idUsuario);
                } else {
                    toMov.setPropietario(propietario);
                }
                toMov.setIdUsuario(this.idUsuario);
            } else {
                throw new SQLException("No se encontro el movimiento !!!");
            }
        } finally {
            cn.close();
        }
        return productos;
    }

    public ArrayList<TOTraspaso> obtenerTraspasosAlmacen(int idAlmacen, int estatus, Date fechaInicial) throws SQLException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TOTraspaso> traspasos = new ArrayList<>();
        String strSQL = "SELECT S.folio AS solicitudFolio, S.fecha AS solicitudFecha, S.idUsuario AS solicitudIdUsuario\n"
                + "     , S.estatus AS solicitudEstatus, M.*\n"
                + "FROM movimientos M\n"
                + "INNER JOIN movimientosAlmacen MA ON MA.idMovtoAlmacen=M.idMovtoAlmacen\n"
                + "INNER JOIN solicitudes S ON S.idSolicitud=M.referencia\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=35 AND M.estatus=7 AND S.estatus=" + estatus + " AND S.envio=0\n";
        if (estatus != 5) {
            strSQL += "         AND CONVERT(date, M.fecha) >= '" + format.format(fechaInicial) + "'\n";
        }
        strSQL += "ORDER BY M.fecha DESC";
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                traspasos.add(this.construir(rs));
            }
        } finally {
            cn.close();
        }
        return traspasos;
    }

    public ArrayList<TOTraspasoProducto> obtenerDetalleTraspaso(TOTraspaso toTraspaso) throws SQLException {
        ArrayList<TOTraspasoProducto> detalle = new ArrayList<>();
        String strSQL = "SELECT S.cantSolicitada, D.*\n"
                + "FROM movimientosDetalle D\n"
                + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                + "INNER JOIN solicitudesDetalle S ON S.idSolicitud=M.referencia AND S.idEmpaque=D.idEmpaque\n"
                + "WHERE D.idMovto=" + toTraspaso.getIdMovto();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    detalle.add(this.construirProducto(rs));
                }
                movimientos.Movimientos.bloquearMovimientoOficina(cn, toTraspaso, this.idUsuario);
                
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

    public ArrayList<TOTraspaso> obtenerTraspasos(int idAlmacen, int estatus, Date fechaInicial) throws SQLException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TOTraspaso> traspasos = new ArrayList<>();
        String strSQL = "SELECT S.folio AS solicitudFolio, S.fecha AS solicitudFecha, S.idUsuario AS solicitudIdUsuario\n"
                + "     , S.estatus AS solicitudEstatus, M.*\n"
                + "FROM movimientos M\n"
                + "INNER JOIN solicitudes S ON S.idSolicitud=M.referencia\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=35 AND M.estatus=" + estatus + " AND S.envio=0\n";
        if (estatus == 7) {
            strSQL += "         AND CONVERT(date, M.fecha) >= '" + format.format(fechaInicial) + "'\n";
        }
        strSQL += "ORDER BY S.fecha";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    traspasos.add(this.construir(rs));
                }
            }
        }
        return traspasos;
    }
    
    public void liberarSolicitud(TOTraspaso toTraspaso) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                solicitudes.Solicitudes.liberarSolicitud(cn, toTraspaso.getReferencia(), this.idUsuario);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void liberarTraspaso(TOTraspaso toTraspaso) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                movimientos.Movimientos.liberarMovimientoOficina(cn, toTraspaso.getIdMovto(), this.idUsuario);
                toTraspaso.setPropietario(0);
                
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
        String strSQL = "";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "UPDATE S\n"
                        + "SET S.idUsuarioOrigen=" + this.idUsuario + ", S.estatus=6\n"
                        + "FROM movimientos M\n"
                        + "INNER JOIN solicitudes S ON S.idSolicitud=M.referencia\n"
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

    public void cerrar(TOTraspaso toTraspaso) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                toTraspaso.setIdUsuario(this.idUsuario);
                toTraspaso.setPropietario(0);
                toTraspaso.setEstatus(7);

                toTraspaso.setFolio(movimientos.Movimientos.obtenMovimientoFolioOficina(cn, toTraspaso.getIdAlmacen(), toTraspaso.getIdTipo()));
                movimientos.Movimientos.grabaMovimientoOficina(cn, toTraspaso);
                
                strSQL = "UPDATE movimientos SET propietario=0 WHERE idMovto=" + toTraspaso.getIdMovto();
                st.executeUpdate(strSQL);
                
                movimientos.Movimientos.actualizaDetalleOficina(cn, toTraspaso.getIdMovto(), toTraspaso.getIdTipo(), false);
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
                toMov.setPropietario(0);
                toMov.setEstatus(5);

                movimientos.Movimientos.agregaMovimientoAlmacen(cn, toMov, false);
                
                toMov.setPropietario(this.idUsuario);
                movimientos.Movimientos.agregaMovimientoOficina(cn, toMov, false);

                strSQL = "INSERT INTO movimientosDetalle\n"
                        + "SELECT " + toMov.getIdMovto() + " AS idMovto, S.idEmpaque, 0 AS cantFacturada, 0 AS cantSinCargo, 0 AS costoPromedio, 0 AS costo\n"
                        + "	, 0 AS desctoProducto1, 0 AS desctoProducto2, 0 AS desctoConfidencial, 0 AS unitario\n"
                        + "	, P.idImpuesto AS idImpuestoGrupo, '' AS fecha, 0 AS existentencioAnterior\n"
                        + "FROM solicitudesDetalle S\n"
                        + "INNER JOIN empaques E ON E.idEmpaque=S.idEmpaque\n"
                        + "INNER JOIN productos P ON P.idProducto=E.idProducto\n"
                        + "WHERE S.idSolicitud=" + toMov.getReferencia();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE solicitudes\n"
                        + "SET propietario=0, idUsuarioOrigen=" + this.idUsuario + "\n"
                        + "WHERE idSolicitud=" + toMov.getReferencia();
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
        String strSQL = "UPDATE solicitudes\n"
                + "SET idUsuarioOrigen=" + this.idUsuario + ", estatus=6\n"
                + "WHERE idSolicitud=" + idSolicitud;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);
            }
        }
    }

    public void salir(TOTraspaso toMov) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private TOTraspasoProducto construirProducto(ResultSet rs) throws SQLException {
        TOTraspasoProducto toProd = new TOTraspasoProducto();
        toProd.setCantSolicitada(rs.getDouble("cantSolicitada"));
        movimientos.Movimientos.construirProductoOficina(rs, toProd);
        return toProd;
    }

    public ArrayList<TOTraspasoProducto> obtenerDetalleSolicitud(TOTraspaso toTraspaso) throws SQLException {
        ArrayList<TOTraspasoProducto> detalle = new ArrayList<>();
        String strSQL = "SELECT S.cantSolicitada\n"
                + "     , " + toTraspaso.getIdMovto() + " AS idMovto, S.idEmpaque, 0 AS cantFacturada, 0 AS cantSinCargo, 0 AS costoPromedio, 0 AS costo\n"
                + "	, 0 AS desctoProducto1, 0 AS desctoProducto2, 0 AS desctoConfidencial, 0 AS unitario, P.idImpuesto AS idImpuestoGrupo\n"
                + "FROM solicitudesDetalle S\n"
                + "INNER JOIN empaques E ON E.idEmpaque=S.idEmpaque\n"
                + "INNER JOIN productos P ON P.idProducto=E.idProducto\n"
                + "WHERE S.idSolicitud=" + toTraspaso.getReferencia();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    detalle.add(this.construirProducto(rs));
                }
                solicitudes.Solicitudes.bloquearSolicitud(cn, toTraspaso, this.idUsuario, 5);
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

    private TOTraspaso construir(ResultSet rs) throws SQLException {
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
        String strSQL = "SELECT S.folio AS solicitudFolio, S.fecha AS solicitudFecha\n"
                + "     , S.idUsuario AS solicitudIdUsuario, S.estatus AS solicitudEstatus\n"
                + "     , 0 AS idMovto, 35 AS idTipo, S.idEmpresa, S.idAlmacenOrigen AS idAlmacen, 0 AS folio\n"
                + "     , 0 AS idComprobante, 0 AS idImpuestoZona, 0 AS desctoComercial, 0 AS desctoProntoPago, GETDATE() AS fecha\n"
                + "     , 0 AS idUsuario, 1 AS tipoDeCambio, S.idAlmacen AS idReferencia, S.idSolicitud AS referencia\n"
                + "     , 0 AS propietario, 0 AS estatus, 0 AS idMovtoAlmacen\n"
                + "FROM solicitudes S\n"
                + "WHERE S.idAlmacenOrigen=" + idAlmacenOrigen + " AND S.idUsuarioOrigen=0 AND S.estatus=1 AND envio=0\n"
                + "ORDER BY S.fecha";
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
}
