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
import recepciones.Recepciones;
import recepciones.to.TORecepcion;
import recepciones.to.TORecepcionProducto;
import recepciones.to.TORecepcionProductoAlmacen;
import traspasos.Traspasos;
import traspasos.to.TOTraspaso;
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

    public TOTraspaso obtenerTraspaso(int idMovtoTraspaso) throws SQLException {
        String strSQL;
        TOTraspaso toTraspaso = null;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                int idSolicitud = 0;
                strSQL = "SELECT referencia AS idSolicitud FROM movimientos WHERE idMovto=" + idMovtoTraspaso;
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    idSolicitud = rs.getInt("idSolicitud");
                } else {
                    throw new SQLException("No se encontró idMovto del traspaso !!!");
                }
                toTraspaso = Traspasos.obtenerTraspaso(cn, idSolicitud);
            }
        }
        return toTraspaso;
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

                Recepciones.generaRechazo(cn, mov);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
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
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                mov.setIdUsuario(this.idUsuario);
//                Recepciones.generaRechazo(cn, mov);
                mov.setPropietario(0);
                mov.setEstatus(7);

                Recepciones.grabar(cn, mov);

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
        String strSQL = "SELECT ISNULL(DT.cantidad, 0) AS cantTraspasada, D.*, ISNULL(A.fechaCaducidad, DATEADD(DAY, E.diasCaducidad, L.fecha)) AS fechaCaducidad\n"
                + "FROM movimientosDetalleAlmacen D\n"
                + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                + "INNER JOIN empaques E ON E.idEmpaque=D.idEmpaque\n"
                + "INNER JOIN lotes L ON L.lote=SUBSTRING(D.lote, 1, 4)\n"
                + "LEFT JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                + "INNER JOIN movimientosAlmacen T ON T.idMovtoAlmacen=M.referencia\n"
                + "LEFT JOIN movimientosDetalleAlmacen DT ON DT.idMovtoAlmacen=T.idMovtoAlmacen AND DT.idEmpaque=D.idEmpaque AND DT.lote=D.lote\n"
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
        toMov.setIdEnvio(rs.getInt("idEnvio"));
        toMov.setEnvioFolio(rs.getInt("envioFolio"));
        toMov.setTraspasoFolio(rs.getInt("traspasoFolio"));
        toMov.setTraspasoFecha(new java.util.Date(rs.getTimestamp("traspasoFecha").getTime()));
        toMov.setIdSolicitud(rs.getInt("idSolicitud"));
        toMov.setSolicitudFolio(rs.getInt("solicitudFolio"));
        toMov.setSolicitudFecha(new java.util.Date(rs.getTimestamp("solicitudFecha").getTime()));
        toMov.setPedidoFolio(rs.getInt("pedidoFolio"));
        toMov.setPedidoFecha(new java.util.Date(rs.getTimestamp("pedidoFecha").getTime()));
        movimientos.Movimientos.construirMovimientoOficina(rs, toMov);
        return toMov;
    }

    public ArrayList<TORecepcion> obtenerRecepciones(int idAlmacen, int estatus, Date fechaInicial) throws SQLException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TORecepcion> movimientos = new ArrayList<>();
        String strSQL = "SELECT ISNULL(E1.idEnvio, ISNULL(E2.idEnvio, 0)) AS idEnvio, ISNULL(E1.folio, ISNULL(E2.folio, 0)) AS envioFolio\n"
                + "	, T.folio AS traspasoFolio, T.fecha AS traspasoFecha"
                + "     , S.idSolicitud, S.folio AS solicitudFolio, S.fecha AS solicitudFecha\n"
                + "	, ISNULL(P.folio, 0) AS pedidoFolio, ISNULL(V.fecha, '1900-01-01') AS pedidoFecha, M.*\n"
                + "FROM movimientos M\n"
                + "INNER JOIN movimientos T ON T.idMovto=M.referencia INNER JOIN solicitudes S ON S.idSolicitud=T.referencia\n"
                + "LEFT JOIN enviosSolicitudes ES ON ES.idSolicitud=S.idSolicitud LEFT JOIN envios E1 ON E1.idEnvio=ES.idEnvio\n"
                + "LEFT JOIN enviosPedidos EP ON S.idSolicitud=EP.idSolicitud LEFT JOIN envios E2 ON E2.idEnvio=EP.idEnvio\n"
                + "LEFT JOIN movimientos V ON V.idMovto=EP.idVenta LEFT JOIN pedidos P ON P.idPedido=V.referencia\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=9 AND M.estatus=" + estatus + "\n";
        if (estatus != 5) {
            strSQL += "         AND CONVERT(date, M.fecha) >= '" + format.format(fechaInicial) + "'\n";
        }
        strSQL += "ORDER BY M.fecha";
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
