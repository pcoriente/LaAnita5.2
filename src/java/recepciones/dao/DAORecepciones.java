package recepciones.dao;

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
import movimientos.to.TOMovimientoOficina;
import movimientos.to.TOProductoAlmacen;
import recepciones.to.TORecepcion;
import recepciones.to.TORecepcionProducto;
import recepciones.to.TORecepcionProductoAlmacen;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAORecepciones {

    int idUsuario, idCedis;
    private DataSource ds = null;

    public DAORecepciones() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
        this.idUsuario = usuarioSesion.getUsuario().getId();
        this.idCedis = usuarioSesion.getUsuario().getIdCedis();

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
    }

    public void cancelar(TORecepcion mov) throws SQLException {
        String strSQL = "";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                mov.setEstatus(6);
                mov.setIdUsuario(this.idUsuario);

                movimientos.Movimientos.grabaMovimientoAlmacen(cn, mov);
                movimientos.Movimientos.grabaMovimientoOficina(cn, mov);

                strSQL = "UPDATE movimientosDetalleAlmacen SET cantidad=0 WHERE idMovtoAlmacen=" + mov.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosDetalle SET cantFacturada=0 WHERE idMovto=" + mov.getIdMovto();
                st.executeUpdate(strSQL);

                this.generaRechazo(cn, mov);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private void generaRechazo(Connection cn, TORecepcion toRecepcion) throws SQLException {
        String strSQL = "SELECT M.idAlmacen, DE.idEmpaque, DE.cantFacturada, DS.cantFacturada\n"
                + "FROM movimientosDetalle DE\n"
                + "INNER JOIN movimientos M ON M.idMovto=DE.idMovto\n"
                + "INNER JOIN movimientosDetalle DS ON DS.idMovto=M.referencia AND DS.idEmpaque=DE.idEmpaque\n"
                + "WHERE DE.idMovto=" + toRecepcion.getIdMovto() + " AND DE.cantFacturada < DS.cantFacturada";
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                TOMovimientoOficina toRechazo = new TOMovimientoOficina();
                toRechazo.setIdTipo(54);
                toRechazo.setIdEmpresa(toRecepcion.getIdEmpresa());
                toRechazo.setIdAlmacen(toRecepcion.getIdReferencia());
                toRechazo.setTipoDeCambio(toRecepcion.getTipoDeCambio());
                toRechazo.setIdReferencia(toRecepcion.getIdAlmacen());
                toRechazo.setIdUsuario(this.idUsuario);
                toRechazo.setPropietario(0);
                toRechazo.setEstatus(7);

                toRechazo.setReferencia(toRecepcion.getIdMovtoAlmacen());
                toRechazo.setFolio(movimientos.Movimientos.obtenMovimientoFolioAlmacen(cn, toRechazo.getIdAlmacen(), toRechazo.getIdTipo()));
                movimientos.Movimientos.agregaMovimientoAlmacen(cn, toRechazo, true);

                toRechazo.setReferencia(toRecepcion.getIdMovto());
                toRechazo.setFolio(movimientos.Movimientos.obtenMovimientoFolioOficina(cn, toRechazo.getIdAlmacen(), toRechazo.getIdTipo()));
                movimientos.Movimientos.agregaMovimientoOficina(cn, toRechazo, true);

                strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantFacturada, cantSinCargo, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior)\n"
                        + "SELECT " + toRechazo.getIdMovto() + ", DS.idEmpaque, DS.cantFacturada-DE.cantFacturada AS cantFacturada, 0, DS.unitario, DS.costo, 0, 0, 0, DS.unitario, DS.idImpuestoGrupo, '', 0\n"
                        + "FROM movimientosDetalle DE\n"
                        + "INNER JOIN movimientos M ON M.idMovto=DE.idMovto\n"
                        + "INNER JOIN movimientosDetalle DS ON DS.idMovto=M.referencia AND DS.idEmpaque=DE.idEmpaque\n"
                        + "WHERE DE.idMovto=" + toRecepcion.getIdMovto() + " AND DE.cantFacturada < DS.cantFacturada";
                int n = st.executeUpdate(strSQL);

                strSQL = "UPDATE D\n"
                        + "SET D.fecha=GETDATE(), D.existenciaAnterior=A.existencia\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toRechazo.getIdMovto();
                if (st.executeUpdate(strSQL) != n) {
                    throw new SQLException("(idMovto=" + toRecepcion.getIdMovto() + ") No se encontro empaque en almacen !!!");
                }
                strSQL = "UPDATE A\n"
                        + "SET existencia=A.existencia+D.cantFacturada\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toRechazo.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE E\n"
                        + "SET E.costoUnitarioPromedio=(E.costoUnitarioPromedio*E.existencia+D.costoPromedio*D.cantFacturada)/(E.existencia+D.cantFacturada)\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toRechazo.getIdMovto();
                if (st.executeUpdate(strSQL) != n) {
                    throw new SQLException("(idMovto=" + toRecepcion.getIdMovto() + ") No se encontro empaque en empresa !!!");
                }
                strSQL = "UPDATE E\n"
                        + "SET existencia=E.existencia+D.cantFacturada\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toRechazo.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "INSERT INTO movimientosDetalleAlmacen (idMovtoAlmacen, idEmpaque, lote, cantidad, fecha, existenciaAnterior)\n"
                        + "SELECT " + toRechazo.getIdMovtoAlmacen() + ", DE.idEmpaque, DE.lote, DS.cantidad-DE.cantidad AS cantidad, '', 0\n"
                        + "FROM movimientosDetalleAlmacen DE\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=DE.idMovtoAlmacen\n"
                        + "LEFT JOIN movimientosDetalleAlmacen DS ON DS.idMovtoAlmacen=M.referencia AND DS.idEmpaque=DE.idEmpaque AND DS.lote=DE.lote\n"
                        + "WHERE DE.idMovtoAlmacen=" + toRecepcion.getIdMovtoAlmacen() + " AND DE.cantidad < DS.cantidad";
                n = st.executeUpdate(strSQL);

                strSQL = "UPDATE D\n"
                        + "SET fecha=GETDATE(), existenciaAnterior=A.existencia\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                        + "WHERE D.idMovtoAlmacen=" + toRechazo.getIdMovtoAlmacen();
                if (st.executeUpdate(strSQL) != n) {
                    throw new SQLException("(idMovtoAlmacen=" + toRechazo.getIdMovtoAlmacen() + ") No se encontro empaque-lote en almacen !!!");
                }
                strSQL = "UPDATE A\n"
                        + "SET existencia=A.existencia+D.cantidad\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                        + "WHERE D.idMovtoAlmacen=" + toRechazo.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalle WHERE idMovto=" + toRecepcion.getIdMovto() + " AND cantFacturada=0";
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalleAlmacen WHERE idMovtoAlmacen=" + toRecepcion.getIdMovtoAlmacen() + " AND cantidad=0";
                st.executeUpdate(strSQL);
            }
        }
    }
    
    public void liberarRecepcion(TORecepcion toRecepcion) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                movimientos.Movimientos.liberarMovimientoOficina(cn, toRecepcion.getIdMovto(), this.idUsuario);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void grabar(TORecepcion mov) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                this.generaRechazo(cn, mov);

                mov.setIdUsuario(this.idUsuario);
                mov.setPropietario(0);
                mov.setEstatus(7);

                mov.setFolio(movimientos.Movimientos.obtenMovimientoFolioAlmacen(cn, mov.getIdAlmacen(), mov.getIdTipo()));
                movimientos.Movimientos.grabaMovimientoAlmacen(cn, mov);
                
                strSQL="UPDATE movimientosAlmacen SET propietario=0 WHERE idMovtoAlmacen=" + mov.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                movimientos.Movimientos.actualizaDetalleAlmacen(cn, mov.getIdMovtoAlmacen(), true);
                
                mov.setFolio(movimientos.Movimientos.obtenMovimientoFolioOficina(cn, mov.getIdAlmacen(), mov.getIdTipo()));
                movimientos.Movimientos.grabaMovimientoOficina(cn, mov);
                
                strSQL="UPDATE movimientos SET propietario=0 WHERE idMovto=" + mov.getIdMovto();
                st.executeUpdate(strSQL);
                
                movimientos.Movimientos.actualizaDetalleOficina(cn, mov.getIdMovto(), mov.getIdTipo(), true);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void liberar(int idMovto, TOProductoAlmacen toProd, double cantLiberar) throws SQLException {
        String strSQL = "UPDATE movimientosDetalle\n"
                + "SET cantFacturada=cantFacturada-" + cantLiberar + "\n"
                + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + toProd.getIdProducto();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);

                toProd.setCantidad(toProd.getCantidad() - cantLiberar);
                movimientos.Movimientos.grabaProductoAlmacen(cn, toProd);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void separar(int idMovto, TOProductoAlmacen toProd, double cantSeparar) throws SQLException {
        String strSQL = "UPDATE movimientosDetalle\n"
                + "SET cantFacturada=cantFacturada+" + cantSeparar + "\n"
                + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + toProd.getIdProducto();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);

                toProd.setCantidad(toProd.getCantidad() + cantSeparar);
                movimientos.Movimientos.grabaProductoAlmacen(cn, toProd);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public TORecepcionProductoAlmacen construirProductoAlmacen(ResultSet rs) throws SQLException {
        TORecepcionProductoAlmacen toProd = new TORecepcionProductoAlmacen();
        toProd.setCantTraspasada(rs.getDouble("cantTraspasada"));
        movimientos.Movimientos.construirProductoAlmacen(rs, toProd);
        toProd.setSeparados(toProd.getCantidad());
        return toProd;
    }

    public ArrayList<TORecepcionProductoAlmacen> obtenerDetalleProducto(int idMovtoAlmacen, int idProducto) throws SQLException {
        String strSQL = "SELECT DT.cantidad AS cantTraspasada, D.*, A.fechaCaducidad\n"
                + "FROM movimientosDetalleAlmacen D\n"
                + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                + "INNER JOIN movimientosAlmacen T ON T.idMovtoAlmacen=M.referencia\n"
                + "LEFT JOIN movimientosDetalleAlmacen DT ON DT.idMovtoAlmacen=T.idMovtoAlmacen AND DT.idEmpaque=D.idEmpaque AND DT.lote=D.lote\n"
                + "INNER JOIN almacenesLotes A ON A.idAlmacen=T.idAlmacen AND A.idEmpaque=DT.idEmpaque AND A.lote=DT.lote\n"
                + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen + " AND D.idEmpaque=" + idProducto;
        ArrayList<TORecepcionProductoAlmacen> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    detalle.add(this.construirProductoAlmacen(rs));
                }
            }
        }
        return detalle;
    }

    private TORecepcionProducto construirProducto(ResultSet rs) throws SQLException {
        TORecepcionProducto to = new TORecepcionProducto();
        to.setCantSolicitada(rs.getInt("cantSolicitada"));
        to.setCantTraspasada(rs.getDouble("cantTraspasada"));
        movimientos.Movimientos.construirProductoOficina(rs, to);
        return to;
    }

    public ArrayList<TORecepcionProducto> obtenerDetalle(TORecepcion toRecepcion) throws SQLException {
        ArrayList<TORecepcionProducto> productos = new ArrayList<>();
        String strSQL = "SELECT D.*, TD.cantFacturada AS cantTraspasada, S.cantSolicitada\n"
                + "FROM movimientosDetalle D\n"
                + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                + "INNER JOIN movimientosDetalle TD ON TD.idMovto=M.referencia AND TD.idEmpaque=D.idEmpaque\n"
                + "INNER JOIN movimientos T ON T.idMovto=TD.idMovto\n"
                + "INNER JOIN solicitudesDetalle S ON S.idSolicitud=T.referencia AND S.idEmpaque=D.idEmpaque\n"
                + "WHERE D.idMovto=" + toRecepcion.getIdMovto();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    productos.add(this.construirProducto(rs));
                }
                movimientos.Movimientos.bloquearMovimientoOficina(cn, toRecepcion, this.idUsuario);
                
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return productos;
    }

    private TORecepcion construir(ResultSet rs) throws SQLException {
        TORecepcion toMov = new TORecepcion();
        toMov.setTraspasoFolio(rs.getInt("traspasoFolio"));
        toMov.setTraspasoFecha(new java.util.Date(rs.getTimestamp("traspasoFecha").getTime()));
        toMov.setSolicitudFolio(rs.getInt("solicitudFolio"));
        toMov.setSolicitudFecha(new java.util.Date(rs.getTimestamp("solicitudFecha").getTime()));
        movimientos.Movimientos.construirMovimientoOficina(rs, toMov);
        return toMov;
    }

    public ArrayList<TORecepcion> obtenerRecepciones(int idAlmacen, int estatus, Date fechaInicial) throws SQLException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TORecepcion> movimientos = new ArrayList<>();
        String strSQL = "SELECT M.*\n"
                + "     , T.folio AS traspasoFolio, T.fecha AS traspasoFecha\n"
                + "     , S.folio AS solicitudFolio, S.fecha AS solicitudFecha\n"
                + "FROM movimientos M\n"
                + "INNER JOIN movimientos T ON T.idMovto=M.referencia\n"
                + "INNER JOIN solicitudes S ON S.idSolicitud=T.referencia\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=9 AND M.estatus=" + estatus + "\n";
        if (estatus != 0) {
            strSQL += "         AND CONVERT(date, M.fecha) >= '" + format.format(fechaInicial) + "'\n";
        }
        strSQL += "ORDER BY M.fecha DESC";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    movimientos.add(this.construir(rs));
                }
            }
        }
        return movimientos;
    }
}
