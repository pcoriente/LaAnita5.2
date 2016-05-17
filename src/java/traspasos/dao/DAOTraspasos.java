package traspasos.dao;

import envios.Envios;
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
import traspasos.Traspasos;
import traspasos.dominio.Traspaso;
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

    public int obtenerEstatusEnvioTraspaso(int idEnvio, int idAlmacenDestino) throws SQLException {
        int estatus = 0;
        try (Connection cn = this.ds.getConnection()) {
            estatus = Envios.obtenerEstatusEnvioTraspaso(cn, idEnvio, idAlmacenDestino);
        }
        return estatus;
    }

    public Date obtenerFechaProduccion(int idSolicitud, boolean envio) throws SQLException {
        Date fechaProduccion = null;
        try (Connection cn = this.ds.getConnection()) {
            fechaProduccion = Envios.obtenerFechaProduccion(cn, idSolicitud, envio);
        }
        return fechaProduccion;
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
                movimientos.Movimientos.actualizaDetalleAlmacen(cn, toTraspaso.getIdMovtoAlmacen(), false);
                movimientos.Movimientos.liberarMovimientoAlmacen(cn, toTraspaso.getIdMovtoAlmacen(), this.idUsuario);

                movimientos.Movimientos.actualizaDetalleOficina(cn, toTraspaso.getIdMovto(), toTraspaso.getIdTipo(), false);
                strSQL = "UPDATE movimientos SET estatus=" + toTraspaso.getEstatus() + " WHERE idMovto=" + toTraspaso.getIdMovto();
                st.executeUpdate(strSQL);
                movimientos.Movimientos.liberarMovimientoOficina(cn, toTraspaso.getIdMovto(), this.idUsuario);

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
                
//                strSQL = "INSERT INTO recepciones (idMovto, idEnvio, folioTraspaso, folioRecepcion)\n"
//                        + "VALUES ("+toRecepcion.getIdMovto()+", "+toTraspaso.getIdEnvio()+", "+toTraspaso.getFolio()+", "+toTraspaso.getPedidoFolio()+")";
//                st.executeUpdate(strSQL);

                strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantFacturada, cantSinCargo, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior, ctoPromAnterior)\n"
                        + "SELECT " + toRecepcion.getIdMovto() + ", idEmpaque, cantFacturada, cantSinCargo, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, '', 0, 0\n"
                        + "FROM movimientosDetalle WHERE idMovto=" + toTraspaso.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "INSERT INTO movimientosDetalleAlmacen (idMovtoAlmacen, idEmpaque, lote, cantidad, fecha, existenciaAnterior)\n"
                        + "SELECT " + toRecepcion.getIdMovtoAlmacen() + ", MD.idEmpaque, MD.lote, MD.cantidad, '', 0\n"
                        + "FROM movimientosDetalleAlmacen MD\n"
                        + "WHERE MD.idMovtoAlmacen=" + toTraspaso.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                // ---------------------------- CREAR COMPLEMENTO DE SOLICITUD -------------------------

                strSQL = "SELECT SD.idSolicitud, SD.idEmpaque, SD.cantSolicitada, ISNULL(SS.cantTraspasada, 0) AS cantTraspasada\n"
                        + "FROM (SELECT S.idSolicitud, D.idEmpaque, SUM(D.cantFacturada) AS cantTraspasada\n"
                        + "	FROM movimientos M\n"
                        + "	INNER JOIN solicitudes S ON S.idSolicitud=M.referencia\n"
                        + "	INNER JOIN movimientosDetalle D ON D.idMovto=M.idMovto\n"
                        + "	WHERE S.idSolicitud=" + toTraspaso.getReferencia() + " AND M.idTipo=35 AND M.estatus=7\n"
                        + "	GROUP BY S.idSolicitud, D.idEmpaque) SS\n"
                        + "RIGHT JOIN solicitudesDetalle SD ON SD.idSolicitud=SS.idSolicitud AND SD.idEmpaque=SS.idEmpaque\n"
                        + "WHERE SD.idSolicitud=" + toTraspaso.getReferencia() + " AND SD.cantSolicitada > ISNULL(SS.cantTraspasada, 0)";
                ResultSet rs = st.executeQuery(strSQL);
                if(rs.next() && (toTraspaso.getIdEnvio()==0 || toTraspaso.getPedidoFolio()!=0)) {
                    TOTraspaso toComplemento = new TOTraspaso();
                    toComplemento.setIdEmpresa(toTraspaso.getIdEmpresa());
                    toComplemento.setIdAlmacen(toTraspaso.getIdAlmacen());
                    toComplemento.setIdTipo(35);
                    toComplemento.setIdImpuestoZona(toTraspaso.getIdImpuestoZona());
                    toComplemento.setTipoDeCambio(toTraspaso.getTipoDeCambio());
                    toComplemento.setIdReferencia(toTraspaso.getIdReferencia());
                    toComplemento.setReferencia(toTraspaso.getReferencia());
                    this.procesa(cn, toComplemento);
                } else {
                    strSQL = "UPDATE solicitudes SET estatus=7 WHERE idSolicitud=" + toTraspaso.getReferencia();
                    st.executeUpdate(strSQL);
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
        String condicion = ">=7";
        if (estatus == 5) {
            condicion = "=5";
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TOTraspaso> traspasos = new ArrayList<>();
        String strSQL = "SELECT " + Traspasos.sqlTraspasoYY() + "\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=35 AND M.estatus" + condicion + "\n";
        if (estatus != 5) {
            strSQL += "         AND CONVERT(date, M.fecha) >= '" + format.format(fechaInicial) + "'\n";
        }
        strSQL += "ORDER BY M.fecha";
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                traspasos.add(Traspasos.construir(rs));
            }
        } finally {
            cn.close();
        }
        return traspasos;
    }

    public ArrayList<TOTraspasoProducto> obtenerDetalleTraspaso(TOTraspaso toTraspaso) throws SQLException {
        ArrayList<TOTraspasoProducto> detalle = new ArrayList<>();
//        String strSQL = "SELECT SS.cantSolicitada, SS.cantTraspasada, D.*\n"
//                + "FROM (SELECT SD.idEmpaque, SD.cantSolicitada, ISNULL(SS.cantTraspasada, 0) AS cantTraspasada\n"
//                + "     FROM (SELECT S.idSolicitud, D.idEmpaque, SUM(D.cantFacturada) AS cantTraspasada\n"
//                + "         FROM movimientos M\n"
//                + "         INNER JOIN solicitudes S ON S.idSolicitud=M.referencia\n"
//                + "         INNER JOIN movimientosDetalle D ON D.idMovto=M.idMovto\n"
//                + "         WHERE S.idSolicitud=" + toTraspaso.getReferencia() + " AND M.idTipo=35 AND M.estatus=7\n"
//                + "         GROUP BY S.idSolicitud, D.idEmpaque) SS\n"
//                + "	RIGHT JOIN solicitudesDetalle SD ON SD.idSolicitud=SS.idSolicitud AND SD.idEmpaque=SS.idEmpaque\n"
//                + "	WHERE SD.idSolicitud=" + toTraspaso.getReferencia() + ") SS\n"
//                + "INNER JOIN movimientosDetalle D ON D.idEmpaque=SS.idEmpaque\n"
//                + "WHERE D.idMovto=" + toTraspaso.getIdMovto();
        String strSQL = Traspasos.sqlTraspasoDetalle(toTraspaso, 0);
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    detalle.add(Traspasos.construirProducto(rs));
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

    public TOTraspaso obtenerTraspaso(int idSolicitud) throws SQLException {
        TOTraspaso toTraspaso = new TOTraspaso();
        try (Connection cn = this.ds.getConnection()) {
            Traspasos.obtenerTraspaso(cn, idSolicitud);
        }
        return toTraspaso;
    }
    

    public ArrayList<TOTraspaso> obtenerTraspasos(int idAlmacen, int estatus, Date fechaInicial) throws SQLException {
        String condicion = ">=5";
        if (estatus == 0) {
            condicion = "=0";
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TOTraspaso> traspasos = new ArrayList<>();
        String strSQL = "SELECT " + Traspasos.sqlTraspasoYY() + "\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=35 AND S.estatus >= 3 AND M.estatus" + condicion + "\n";
        if (estatus == 7) {
            strSQL += "         AND CONVERT(date, M.fecha) >= '" + format.format(fechaInicial) + "'\n";
        }
        strSQL += "ORDER BY M.fecha";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    traspasos.add(Traspasos.construir(rs));
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
                Traspasos.liberarTraspaso(cn, toTraspaso, this.idUsuario);
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
                strSQL = "UPDATE A\n"
                        + "SET separados=A.separados-D.cantidad\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                        + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen;
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosDetalleAlmacen SET cantidad=0 WHERE idMovtoAlmacen=" + idMovtoAlmacen;
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosAlmacen SET propietario=0, estatus=6 WHERE idMovtoAlmacen=" + idMovtoAlmacen;
                st.executeUpdate(strSQL);

                strSQL = "UPDATE A\n"
                        + "SET separados=A.separados-D.cantFacturada\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + idMovto;
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosDetalle SET cantFacturada=0 WHERE idMovto=" + idMovto;
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientos SET propietario=0, estatus=6 WHERE idMovto=" + idMovto;
                st.executeUpdate(strSQL);

                strSQL = "UPDATE S\n"
                        + "SET idUsuarioOrigen=" + this.idUsuario + ", estatus=CASE WHEN S.estatus=3 THEN 6 ELSE 7 END\n"
                        + "FROM movimientos M\n"
                        + "INNER JOIN solicitudes S ON S.idSolicitud=M.referencia\n"
                        + "WHERE M.idMovto=" + idMovto;
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
                toTraspaso.setEstatus(5);

                strSQL = "UPDATE movimientosAlmacen SET estatus=" + toTraspaso.getEstatus() + "\n"
                        + "WHERE idMovtoAlmacen=" + toTraspaso.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);
                movimientos.Movimientos.liberarMovimientoAlmacen(cn, toTraspaso.getIdMovtoAlmacen(), this.idUsuario);

                toTraspaso.setFolio(movimientos.Movimientos.obtenMovimientoFolioOficina(cn, toTraspaso.getIdAlmacen(), toTraspaso.getIdTipo()));
                movimientos.Movimientos.grabaMovimientoOficina(cn, toTraspaso);
                movimientos.Movimientos.liberarMovimientoOficina(cn, toTraspaso.getIdMovto(), this.idUsuario);

                strSQL = "UPDATE solicitudes SET estatus=5 WHERE idSolicitud=" + toTraspaso.getReferencia();
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

    private void procesa(Connection cn, TOTraspaso toMov) throws SQLException {
        String strSQL = "";
        try (Statement st = cn.createStatement()) {
            toMov.setFolio(0);
            toMov.setIdUsuario(this.idUsuario);
            toMov.setPropietario(0);
            toMov.setEstatus(0);

            movimientos.Movimientos.agregaMovimientoAlmacen(cn, toMov, false);
            movimientos.Movimientos.agregaMovimientoOficina(cn, toMov, false);

            strSQL = "INSERT INTO movimientosDetalle\n"
                    + "SELECT " + toMov.getIdMovto() + " AS idMovto, SD.idEmpaque, 0 AS cantFacturada, 0 AS cantSinCargo\n"
                    + "     , 0 AS costoPromedio, 0 AS costo, 0 AS desctoProducto1, 0 AS desctoProducto2, 0 AS desctoConfidencial\n"
                    + "     , 0 AS unitario, P.idImpuesto AS idImpuestoGrupo, '' AS fecha, 0 AS existentencioAnterior, 0 AS ctoPromAnterior\n"
                    + "FROM (SELECT S.idSolicitud, D.idEmpaque, SUM(D.cantFacturada) AS cantTraspasada\n"
                    + "     FROM movimientos M\n"
                    + "     INNER JOIN solicitudes S ON S.idSolicitud=M.referencia\n"
                    + "     INNER JOIN movimientosDetalle D ON D.idMovto=M.idMovto\n"
                    + "     WHERE S.idSolicitud=" + toMov.getIdReferencia() + " AND M.idTipo=35 AND M.estatus=7\n"
                    + "     GROUP BY S.idSolicitud, D.idEmpaque) SS\n"
                    + "RIGHT JOIN solicitudesDetalle SD ON SD.idSolicitud=SS.idSolicitud AND SD.idEmpaque=SS.idEmpaque\n"
                    + "INNER JOIN empaques E ON E.idEmpaque=SD.idEmpaque\n"
                    + "INNER JOIN productos P ON P.idProducto=E.idProducto\n"
                    + "WHERE SD.idSolicitud=" + toMov.getReferencia() + " AND SD.cantSolicitada > ISNULL(SS.cantTraspasada, 0)";
            st.executeUpdate(strSQL);
        }
    }

    public void procesar(TOTraspaso toMov) throws SQLException {
        String strSQL = "";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                if (toMov.getIdEnvio() != 0 && toMov.getPedidoFolio()==0) {
                    strSQL = "SELECT M.idMovto, M.idMovtoAlmacen\n"
                            + "FROM movimientos M INNER JOIN solicitudes S ON S.idSolicitud=M.referencia\n"
                            + "WHERE M.idAlmacen=" + toMov.getIdAlmacen() + " AND M.idTipo=35 AND S.idSolicitud=" + toMov.getReferencia();
                    ResultSet rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        toMov.setIdMovto(rs.getInt("idMovto"));
                        toMov.setIdMovtoAlmacen(rs.getInt("idMovtoAlmacen"));
                    } else {
                        throw new SQLException("No se encontró movimiento de la solicitud !!!");
                    }
                } else {
                    this.procesa(cn, toMov);
                }
                movimientos.Movimientos.bloquearMovimientoOficina(cn, toMov, this.idUsuario);

                strSQL = "UPDATE solicitudes\n"
                        + "SET idUsuarioOrigen=" + this.idUsuario + ", estatus=3, propietario=0\n"
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
                + "SET idUsuarioOrigen=" + this.idUsuario + ", estatus=2, propietario=0\n"
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

//    private TOTraspasoProducto construirProducto(ResultSet rs) throws SQLException {
//        TOTraspasoProducto toProd = new TOTraspasoProducto();
//        toProd.setCantSolicitada(rs.getDouble("cantSolicitada"));
//        toProd.setCantTraspasada(rs.getDouble("cantTraspasada"));
//        movimientos.Movimientos.construirProductoOficina(rs, toProd);
//        return toProd;
//    }
    public ArrayList<TOTraspasoProducto> obtenerDetalleSolicitud(TOTraspaso toTraspaso) throws SQLException {
        ArrayList<TOTraspasoProducto> detalle = new ArrayList<>();
        String strSQL = "SELECT S.cantSolicitada, 0 AS cantTraspasada, " + toTraspaso.getIdMovto() + " AS idMovto, S.idEmpaque\n"
                + "     , 0 AS cantFacturada, 0 AS cantSinCargo, 0 AS costoPromedio, 0 AS costo, 0 AS desctoProducto1\n"
                + "	, 0 AS desctoProducto2, 0 AS desctoConfidencial, 0 AS unitario, P.idImpuesto AS idImpuestoGrupo\n"
                + "FROM solicitudesDetalle S\n"
                + "INNER JOIN empaques E ON E.idEmpaque=S.idEmpaque\n"
                + "INNER JOIN productos P ON P.idProducto=E.idProducto\n"
                + "WHERE S.idSolicitud=" + toTraspaso.getReferencia();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    detalle.add(Traspasos.construirProducto(rs));
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

//    private TOTraspaso construir1(ResultSet rs) throws SQLException {
//        TOTraspaso toMov = new TOTraspaso();
//        toMov.setSolicitudFolio(rs.getInt("solicitudFolio"));
//        toMov.setSolicitudFecha(new java.util.Date(rs.getTimestamp("solicitudFecha").getTime()));
//        toMov.setSolicitudIdUsuario(rs.getInt("solicitudIdUsuario"));
//        toMov.setSolicitudEstatus(rs.getInt("solicitudEstatus"));
//        movimientos.Movimientos.construirMovimientoOficina(rs, toMov);
//        return toMov;
//    }
//
    public ArrayList<TOTraspaso> obtenerSolicitudes(int idAlmacenOrigen) throws SQLException {
        ArrayList<TOTraspaso> solicitudes = new ArrayList<>();
        String strSQL = "SELECT ISNULL(ES.idEnvio, ISNULL(EP.idEnvio, 0)) AS idEnvio, ISNULL(E1.folio, ISNULL(E2.folio, 0)) AS envioFolio\n"
                + "	, ISNULL(P.folio, 0) AS pedidoFolio\n"
                + "     , S.folio AS solicitudFolio, S.fecha AS solicitudFecha, S.envio\n"
                + "     , S.idUsuario AS solicitudIdUsuario, S.estatus AS solicitudEstatus\n"
                + "     , 0 AS idMovto, 35 AS idTipo, A.idEmpresa, S.idAlmacenOrigen AS idAlmacen, 0 AS folio\n"
                + "     , 0 AS idComprobante, 0 AS idImpuestoZona, 0 AS desctoComercial, 0 AS desctoProntoPago, GETDATE() AS fecha\n"
                + "     , 0 AS idUsuario, 1 AS tipoDeCambio, S.idAlmacen AS idReferencia, S.idSolicitud AS referencia\n"
                + "     , 0 AS propietario, 0 AS estatus, 0 AS idMovtoAlmacen\n"
                + "FROM solicitudes S\n"
                + "INNER JOIN almacenes A ON A.idAlmacen=S.idAlmacen\n"
                + "LEFT JOIN enviosSolicitudes ES ON S.idSolicitud=ES.idSolicitud LEFT JOIN envios E1 ON E1.idEnvio=ES.idEnvio\n"
                + "LEFT JOIN enviosPedidos EP ON S.idSolicitud=EP.idSolicitud LEFT JOIN envios E2 ON E2.idEnvio=EP.idEnvio\n"
                + "LEFT JOIN ventas V ON V.idVenta=EP.idVenta LEFT JOIN pedidos P ON P.idPedido=V.idPedido\n"
                + "WHERE S.idAlmacenOrigen=" + idAlmacenOrigen + " AND S.estatus=1\n"
                + "ORDER BY S.fecha";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    solicitudes.add(Traspasos.construir(rs));
                }
            }
        }
        return solicitudes;
    }
}
