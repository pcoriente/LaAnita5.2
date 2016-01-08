package ventas.dao;

import comprobantes.to.TOComprobante;
import impuestos.dominio.ImpuestosProducto;
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
import usuarios.dominio.UsuarioSesion;
import ventas.to.TOVenta;
import ventas.to.TOVentaProducto;
import ventas.to.TOVentaProductoAlmacen;

/**
 *
 * @author jesc
 */
public class DAOVentas {

    int idUsuario, idCedis;
    private DataSource ds = null;

    public DAOVentas() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
        this.idUsuario = usuarioSesion.getUsuario().getId();
        this.idCedis = usuarioSesion.getUsuario().getIdCedis();

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
    }

    public void liberarVentaAlmacen(TOVenta toVta) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                movimientos.Movimientos.liberarMovimientoAlmacen(cn, toVta.getIdMovto(), this.idUsuario);
                toVta.setPropietario(0);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void cerrarVentaAlmacen(TOVenta toVta) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                toVta.setIdUsuario(this.idUsuario);
                toVta.setEstatus(7);

                toVta.setFolio(movimientos.Movimientos.obtenMovimientoFolioAlmacen(cn, toVta.getIdAlmacen(), toVta.getIdTipo()));
                movimientos.Movimientos.grabaMovimientoAlmacen(cn, toVta);

                movimientos.Movimientos.actualizaDetalleAlmacen(cn, toVta.getIdMovtoAlmacen(), false);

                strSQL = "UPDATE movimientosAlmacen SET propietario=0 WHERE idMovtoAlmacen=" + toVta.getIdMovtoAlmacen();
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

    public void traspasarLote(int idAlmacen, TOVentaProductoAlmacen toOrigen, TOVentaProductoAlmacen toDestino, double cantTraspasar) throws SQLException {
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

    public ArrayList<TOVentaProductoAlmacen> obtenerLotesDisponibles(int idAlmacen, TOVentaProductoAlmacen toProd) throws SQLException {
        ArrayList<TOVentaProductoAlmacen> lotes = new ArrayList<>();
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

    private TOVentaProductoAlmacen construirProductoAlmacen(ResultSet rs) throws SQLException {
        TOVentaProductoAlmacen toProd = new TOVentaProductoAlmacen();
        toProd.setDisponibles(rs.getDouble("disponibles"));
        toProd.setFechaCaducidad(new java.util.Date(rs.getDate("fechaCaducidad").getTime()));
        movimientos.Movimientos.construirProductoAlmacen(rs, toProd);
        return toProd;
    }

    public ArrayList<TOVentaProductoAlmacen> obtenerDetalleAlmacen(TOVenta toVta) throws SQLException {
        ArrayList<TOVentaProductoAlmacen> productos = new ArrayList<>();
        String strSQL = "SELECT D.*, 0 AS disponibles, A.fechaCaducidad\n"
                + "FROM movimientosDetalleAlmacen D\n"
                + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                + "WHERE D.idMovtoAlmacen=" + toVta.getIdMovtoAlmacen() + "\n"
                + "ORDER BY D.idEmpaque, A.fechaCaducidad";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    productos.add(this.construirProductoAlmacen(rs));
                }
                movimientos.Movimientos.bloquearMovimientoAlmacen(cn, toVta, this.idUsuario);

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

    public ArrayList<TOVenta> obtenerVentasAlmacen(int idAlmacen, int estatus, Date fechaInicial) throws SQLException {
        String condicion = ">=7";
        if (estatus == 5) {
            condicion = "=5";
        }
        if (fechaInicial == null) {
            fechaInicial = new Date();
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TOVenta> ventas = new ArrayList<>();
        String strSQL = "SELECT M.*\n"
                + "     , ISNULL(P.idPedidoOC, 0) AS idPedidoOC, ISNULL(P.idMoneda, 0) AS idMoneda\n"
                + "     , ISNULL(P.canceladoMotivo, '') AS canceladoMotivo, ISNULL(P.canceladoFecha, '1900-01-01') AS canceladoFecha\n"
                + "     , ISNULL(OC.ordenDeCompra, '') AS ordenDeCompra, ISNULL(OC.ordenDeCompraFecha, '1900-01-01') AS ordenDeCompraFecha\n"
                + "FROM movimientos M\n"
                + "INNER JOIN movimientosAlmacen MA ON MA.idMovtoAlmacen=M.idMovtoAlmacen\n"
                + "LEFT JOIN pedidos P ON P.idPedido=M.referencia\n"
                + "LEFT JOIN pedidosOC OC ON OC.idPedidoOC=P.idPedidoOC\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=28 AND M.estatus=7 AND MA.estatus" + condicion + "\n";
        if (estatus != 0) {
            strSQL += "         AND CONVERT(date, M.fecha) >= '" + format.format(fechaInicial) + "'\n";
        }
        strSQL += "ORDER BY M.fecha DESC";
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                ventas.add(this.construir(rs));
            }
        } finally {
            cn.close();
        }
        return ventas;
    }

//    private ArrayList<TOVentaProducto> obtenSimilares(Connection cn, int idMovto, int idProducto) throws SQLException {
//        ArrayList<TOVentaProducto> similares = new ArrayList<>();
//        String strSQL = "SELECT PD.idPedido, PD.cantOrdenada, PD.cantOrdenadaSinCargo, PD.similar, D.*\n"
//                + "FROM movimientosDetalle D\n"
//                + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
//                + "INNER JOIN pedidosDetalle PD ON PD.idPedido=M.referencia AND PD.idEmpaque=D.idEmpaque\n"
//                + "INNER JOIN empaquesSimilares S ON S.idSimilar=D.idEmpaque\n"
//                + "WHERE M.idMovto=" + idMovto + " AND S.idEmpaque=" + idProducto;
//        try (Statement st = cn.createStatement()) {
//            ResultSet rs = st.executeQuery(strSQL);
//            while (rs.next()) {
//                similares.add(this.construirProductoOficina(rs));
//            }
//        }
//        return similares;
//    }
    public ArrayList<TOVentaProducto> surtirFincado(TOVenta toMov) throws SQLException, Exception {
        // Intenta surtir en automatico TODO el pedido fincado
        double cantSolicitada;
        ArrayList<TOVentaProducto> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                detalle = this.obtenDetalleOficina(cn, toMov);
                if (toMov.getIdUsuario() == toMov.getPropietario() && toMov.getEstatus() == 5) {
                    for (TOVentaProducto to : detalle) {
                        if (to.getCantFacturada() < to.getCantOrdenada()) {
                            cantSolicitada = to.getCantOrdenada() - to.getCantFacturada();
                            try {
                                movimientos.Movimientos.separar(cn, toMov, to.getIdProducto(), cantSolicitada, "cantFacturada");
                                to.setCantFacturada(to.getCantOrdenada());
                            } catch (Exception ex) {
                                // Si no se pudieron surtir todos los solicitados, pasa al siguiete producto
                            }
                        }
                        if (to.getCantSinCargo() < to.getCantOrdenadaSinCargo()) {
                            cantSolicitada = to.getCantOrdenadaSinCargo() - to.getCantSinCargo();
                            try {
                                movimientos.Movimientos.separar(cn, toMov, to.getIdProducto(), cantSolicitada, "cantSinCargo");
                                to.setCantSinCargo(to.getCantOrdenadaSinCargo());
                            } catch (Exception ex) {
                                // Si no se pudieron surtir todos los solicitados, pasa al siguiete producto
                            }
                        }
//                        if (to.getCantFacturada() + to.getCantSinCargo() < to.getCantOrdenada() + to.getCantOrdenadaSinCargo()) {
//                            cantSolicitada = to.getCantOrdenada() + to.getCantOrdenadaSinCargo() - (to.getCantFacturada() + to.getCantSinCargo());
//                            try {
//                                movimientos.Movimientos.separar(cn, toMov, to.getIdProducto(), cantSolicitada, "cantFacturada");
//                                to.setCantFacturada(to.getCantOrdenada());
//                                to.setCantSinCargo(to.getCantOrdenadaSinCargo());
//                            } catch (Exception ex) {
//                                // Si no se pudieron surtir todos los solicitados, pasa al siguiete producto
//                                Logger.getLogger(DAOVentas.class.getName()).log(Level.SEVERE, null, ex);
//                            }
//                        }
                    }
                } else {
                    throw new Exception("El pedido no se puede surtir, otro usuario es propietario");
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } catch (Exception ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return detalle;
    }

    public ArrayList<TOVentaProducto> generarPedidoVenta(TOVenta toMov) throws SQLException, Exception {
        String strSQL;
        ArrayList<TOVentaProducto> detalle;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                // Checa que no haya ningun movimiento con estatus=5
                strSQL = "SELECT M.idMovto\n"
                        + "FROM movimientos M\n"
                        + "INNER JOIN pedidos P ON P.idPedido=M.referencia\n"
                        + "WHERE P.idPedido=" + toMov.getReferencia() + " AND M.estatus=5";
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    throw new Exception("El pedido ya tiene una venta pendiente !!!");
                }
                int idMovto;
                // Obtiene el movimiento original (el primer idMovto) del pedido, para con este crear el nuevo
                strSQL = "SELECT M.*, P.idPedidoOC, P.idMoneda, P.canceladoMotivo, P.canceladoFecha\n"
                        + "     , ISNULL(OC.ordenDeCompra, '') AS ordenDeCompra, ISNULL(OC.ordenDeCompraFecha, '1900-01-01') AS ordenDeCompraFecha\n"
                        + "FROM movimientos M\n"
                        + "INNER JOIN pedidos P ON P.idPedido=M.referencia\n"
                        + "LEFT JOIN pedidosOC OC ON OC.idPedidoOC=P.idPedidoOC\n"
                        + "WHERE P.idPedido=" + toMov.getReferencia() + " AND M.estatus=7\n"
                        + "ORDER BY M.idMovto";
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    this.construir(rs, toMov);
                    idMovto = toMov.getIdMovto();
                    toMov.setIdUsuario(this.idUsuario);
                    toMov.setPropietario(0);
                    toMov.setEstatus(5);
                    toMov.setFolio(0);
                } else {
                    throw new Exception("El pedido no tiene una venta facturada !!!");
                }
                TOComprobante to = new TOComprobante(toMov.getIdTipo(), toMov.getIdEmpresa(), toMov.getIdReferencia(), toMov.getIdMoneda());
                to.setTipo(1);
                to.setNumero(String.valueOf(toMov.getReferencia()));
                to.setIdUsuario(this.idUsuario);
                to.setPropietario(0);
                comprobantes.Comprobantes.agregar(cn, to);

                toMov.setIdComprobante(to.getIdComprobante());
                movimientos.Movimientos.agregaMovimientoAlmacen(cn, toMov, false);
                movimientos.Movimientos.agregaMovimientoOficina(cn, toMov, false);

                strSQL = "INSERT INTO movimientosDetalle\n"
                        + "SELECT " + toMov.getIdMovto() + " AS idMovto, D.idEmpaque, 0 AS cantFacturada, 0 AS cantSinCargo, D.costoPromedio, D.costo\n"
                        + ",    D.desctoProducto1, D.desctoProducto2, D.desctoConfidencial, D.unitario, D.idImpuestoGrupo, '', 0\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN pedidosDetalle PD ON PD.idPedido=M.referencia AND PD.idEmpaque=D.idEmpaque\n"
                        + "INNER JOIN pedidos P ON P.idPedido=M.referencia\n"
                        + "WHERE D.idMovto=" + idMovto + " AND (PD.cantSurtida<PD.cantOrdenada OR PD.cantSurtidaSinCargo<PD.cantOrdenadaSinCargo)";
                st.executeUpdate(strSQL);

                strSQL = "INSERT INTO movimientosDetalleImpuestos\n"
                        + "SELECT D.idMovto, I.idEmpaque, I.idImpuesto, I.impuesto, I.valor, I.aplicable, I.modo, I.acreditable, I.importe, I.acumulable\n"
                        + "FROM (SELECT * FROM movimientosDetalleImpuestos WHERE idMovto=" + idMovto + ") I\n"
                        + "INNER JOIN movimientosDetalle D ON D.idEmpaque=I.idEmpaque\n"
                        + "WHERE D.idMovto=" + toMov.getIdMovto();
                st.executeUpdate(strSQL);

                detalle = this.obtenDetalleOficina(cn, toMov);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw (ex);
            } catch (Exception ex) {
                cn.rollback();
                throw (ex);
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return detalle;
    }

    public ArrayList<TOVenta> obtenerPedidos(int idAlmacen) throws SQLException {
        ArrayList<TOVenta> ventas = new ArrayList<>();
        String strSQL = "SELECT P.idPedidoOC, P.idMoneda, P.canceladoMotivo, P.canceladoFecha\n"
                + "	, ISNULL(OC.ordenDeCompra, '') AS ordenDeCompra, ISNULL(OC.ordenDeCompraFecha, '1900-01-01') AS ordenDeCompraFecha\n"
                + "	, 0 AS idMovto, 28 AS idTipo, 0 AS idEmpresa, " + idAlmacen + " AS idAlmacen, 0 AS folio, 0 AS idComprobante, T.idImpuestoZona\n"
                + "	, 0 AS desctoComercial, 0 AS desctoProntoPago, P.fecha, 0 AS idUsuario, 1 AS tipoDeCambio\n"
                + "	, P.idTienda AS idReferencia, P.idPedido AS referencia, 0 AS propietario, 5 AS estatus, 0 AS  idMovtoAlmacen\n"
                + "FROM (SELECT referencia FROM movimientos WHERE idAlmacen=" + idAlmacen + " AND idTipo=28 AND estatus=5) M\n"
                + "RIGHT JOIN pedidos P ON P.idPedido=M.referencia\n"
                + "INNER JOIN clientesTiendas T ON T.idTienda=P.idTienda\n"
                + "LEFT JOIN pedidosOC OC ON OC.idPedidoOC=P.idPedidoOC\n"
                + "WHERE P.estatus=5 AND M.referencia IS NULL\n"
                + "ORDER BY P.fecha DESC";
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                ventas.add(this.construir(rs));
            }
        } finally {
            cn.close();
        }
        return ventas;
    }

    public void liberarVentaOficina(TOVenta toVta) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                movimientos.Movimientos.liberarMovimientoOficina(cn, toVta.getIdMovto(), this.idUsuario);
                toVta.setPropietario(0);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void eliminarVentaOficina(TOVenta toMov) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "UPDATE L\n"
                        + "SET L.separados=L.separados-K.cantidad\n"
                        + "FROM almacenesLotes L\n"
                        + "INNER JOIN movimientosDetalleAlmacen K ON K.idMovtoAlmacen=" + toMov.getIdMovtoAlmacen() + " AND K.idEmpaque=L.idEmpaque AND K.lote=L.lote\n"
                        + "WHERE L.idAlmacen=" + toMov.getIdAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalleAlmacen WHERE idMovtoAlmacen=" + toMov.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosAlmacen WHERE idMovtoAlmacen=" + toMov.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE E\n"
                        + "SET E.separados=E.separados-(D.cantFacturada+D.cantSinCargo)\n"
                        + "FROM almacenesEmpaques E\n"
                        + "INNER JOIN movimientosDetalle D ON D.idMovto=" + toMov.getIdMovto() + " AND D.idEmpaque=E.idEmpaque\n"
                        + "WHERE E.idAlmacen=" + toMov.getIdAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalle WHERE idMovto=" + toMov.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientos WHERE idMovto=" + toMov.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalleImpuestos WHERE idMovto=" + toMov.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM comprobantes WHERE idComprobante=" + toMov.getIdComprobante();
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

    private void generaPedidoVenta(Connection cn, int idPedido) throws SQLException {
        // Obtiene el movimiento original (el primer idMovto) del pedido, para con este crear el nuevo
        String strSQL = "SELECT M.*, P.idPedidoOC, P.idMoneda, P.canceladoMotivo, P.canceladoFecha\n"
                + "     , ISNULL(OC.ordenDeCompra, '') AS ordenDeCompra, ISNULL(OC.ordenDeCompraFecha, '1900-01-01') AS ordenDeCompraFecha\n"
                + "FROM movimientos M\n"
                + "INNER JOIN pedidos P ON P.idPedido=M.referencia\n"
                + "LEFT JOIN pedidosOC OC ON OC.idPedidoOC=P.idPedidoOC\n"
                + "WHERE P.idPedido=" + idPedido + " AND M.estatus=7\n"
                + "ORDER BY M.idMovto";
        try (Statement st = cn.createStatement()) {
            int idMovto;
            ResultSet rs = st.executeQuery(strSQL);
            rs.next();

            TOVenta toMov = this.construir(rs);
            idMovto = toMov.getIdMovto();
            toMov.setIdUsuario(this.idUsuario);
            toMov.setPropietario(0);
            toMov.setEstatus(5);
            toMov.setFolio(0);

            TOComprobante to = new TOComprobante(toMov.getIdTipo(), toMov.getIdEmpresa(), toMov.getIdReferencia(), toMov.getIdMoneda());
            to.setTipo(1);
            to.setNumero(String.valueOf(toMov.getReferencia()));
            to.setIdUsuario(this.idUsuario);
            to.setPropietario(0);
            comprobantes.Comprobantes.agregar(cn, to);

            toMov.setIdComprobante(to.getIdComprobante());
            movimientos.Movimientos.agregaMovimientoAlmacen(cn, toMov, false);
            movimientos.Movimientos.agregaMovimientoOficina(cn, toMov, false);

            strSQL = "INSERT INTO movimientosDetalle\n"
                    + "SELECT " + toMov.getIdMovto() + " AS idMovto, D.idEmpaque, 0 AS cantFacturada, 0 AS cantSinCargo, D.costoPromedio, D.costo\n"
                    + ",    D.desctoProducto1, D.desctoProducto2, D.desctoConfidencial, D.unitario, D.idImpuestoGrupo, '', 0\n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "INNER JOIN pedidosDetalle PD ON PD.idPedido=M.referencia AND PD.idEmpaque=D.idEmpaque\n"
                    + "INNER JOIN pedidos P ON P.idPedido=M.referencia\n"
                    + "WHERE D.idMovto=" + idMovto + " AND (PD.cantSurtida<PD.cantOrdenada OR PD.cantSurtidaSinCargo<PD.cantOrdenadaSinCargo)";
            st.executeUpdate(strSQL);

            strSQL = "INSERT INTO movimientosDetalleImpuestos\n"
                    + "SELECT D.idMovto, I.idEmpaque, I.idImpuesto, I.impuesto, I.valor, I.aplicable, I.modo, I.acreditable, I.importe, I.acumulable\n"
                    + "FROM (SELECT * FROM movimientosDetalleImpuestos WHERE idMovto=" + idMovto + ") I\n"
                    + "INNER JOIN movimientosDetalle D ON D.idEmpaque=I.idEmpaque\n"
                    + "WHERE D.idMovto=" + toMov.getIdMovto();
            st.executeUpdate(strSQL);
        }
    }

    public void cerrarVentaOficina(TOVenta toVta) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                toVta.setIdUsuario(this.idUsuario);
                toVta.setPropietario(0);
                toVta.setEstatus(7);

                toVta.setFolio(movimientos.Movimientos.obtenMovimientoFolioOficina(cn, toVta.getIdAlmacen(), toVta.getIdTipo()));
                movimientos.Movimientos.grabaMovimientoOficina(cn, toVta);

                movimientos.Movimientos.actualizaDetalleOficina(cn, toVta.getIdMovto(), toVta.getIdTipo(), false);

                strSQL = "UPDATE comprobantes\n"
                        + "SET tipo='2', numero='" + String.valueOf(toVta.getFolio()) + "'\n"
                        + "WHERE idComprobante=" + toVta.getIdComprobante();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE PD\n"
                        + "SET cantSurtida=PD.cantSurtida+D.cantFacturada, cantSurtidaSinCargo=PD.cantSurtidaSinCargo+D.cantSinCargo\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN pedidosDetalle PD ON PD.idPedido=M.referencia AND PD.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toVta.getIdMovto();
                st.executeUpdate(strSQL);

                if (toVta.getReferencia() != 0) {
                    strSQL = "SELECT * FROM pedidosDetalle\n"
                            + "WHERE idPedido=" + toVta.getReferencia() + " AND (cantSurtida<cantOrdenada OR cantSurtidaSinCargo<cantOrdenadaSinCargo)";
                    ResultSet rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        this.generaPedidoVenta(cn, toVta.getReferencia());
                    }
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

    public void tranferirSinCargo(TOVenta toVta, TOVentaProducto toProd, TOVentaProducto toSimilar, int idZonaImpuestos, double cantidad) throws SQLException, Exception {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                if (toSimilar.getIdMovto() == 0) {
                    toSimilar.setIdMovto(toProd.getIdMovto());
                    toSimilar.setCantOrdenadaSinCargo(0);
                    this.agregaProducto(cn, toVta, toSimilar);
                }
                movimientos.Movimientos.separar(cn, toVta, toSimilar.getIdProducto(), cantidad, "cantSinCargo");
                toSimilar.setCantSinCargo(toSimilar.getCantSinCargo() + cantidad);

                movimientos.Movimientos.liberar(cn, toVta, toProd.getIdProducto(), cantidad, "cantSinCargo");
                toProd.setCantSinCargo(toProd.getCantSinCargo() - cantidad);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw (ex);
            } catch (Exception ex) {
                cn.rollback();
                throw (ex);
            } finally {
                cn.setAutoCommit(true);
            }
        }
//        return similares;
    }

    public ArrayList<TOVentaProducto> obtenerSimilares(int idMovto, int idProducto) throws SQLException {
        ArrayList<TOVentaProducto> productos = new ArrayList<>();
        String strSQL = "SELECT ISNULL(D.cod_pro, '') AS cod_pro\n"
                + "     , ISNULL(D.cantOrdenada, 0) AS cantOrdenada, ISNULL(D.cantOrdenadaSinCargo, 0) AS cantOrdenadaSinCargo\n"
                + "     , ISNULL(D.idMovto, 0) AS idMovto, ISNULL(D.idPedido, 0) AS idPedido, ISNULL(D.idEmpaque, S.idSimilar) AS idEmpaque\n"
                + "     , ISNULL(D.cantFacturada, 0) AS cantFacturada, ISNULL(D.cantSinCargo, 0) AS cantSinCargo\n"
                + "	, ISNULL(D.costoPromedio, 0) AS costoPromedio, ISNULL(D.costo, 0) AS costo\n"
                + "	, ISNULL(D.desctoProducto1, 0) AS desctoProducto1, ISNULL(D.desctoProducto2, 0) AS desctoProducto2\n"
                + "	, ISNULL(D.desctoConfidencial, 0) AS desctoConfidencial, ISNULL(D.unitario, 0) AS unitario\n"
                + "	, ISNULL(D.idImpuestoGrupo, 0) AS idImpuestoGrupo\n"
                + "	, ISNULL(D.fecha, '1900-01-01') AS fecha, ISNULL(D.existenciaAnterior, 0) AS existenciaAnterior\n"
                + "FROM (SELECT E.cod_pro, PD.idPedido, PD.cantOrdenada, PD.cantOrdenadaSinCargo, D.*\n"
                + "	FROM movimientosDetalle D\n"
                + "	INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                + "     INNER JOIN empaques E ON E.idEmpaque=D.idEmpaque\n"
                + "	LEFT JOIN pedidosDetalle PD ON PD.idPedido=M.referencia AND PD.idEmpaque=D.idEmpaque\n"
                + "	WHERE M.idMovto=" + idMovto + ") D\n"
                + "RIGHT JOIN empaquesSimilares S ON S.idSimilar=D.idEmpaque\n"
                + "WHERE S.idEmpaque=" + idProducto + " AND S.idSimilar!=S.idEmpaque";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    productos.add(this.construirProductoOficina(rs));
                }
            }
        }
        return productos;
    }

    public void actualizarPedidoProductoSinCargo(TOVenta toMov, TOVentaProducto toProd, double cantSeparada) throws SQLException {
        // Cuando esto capturando en pantalla de edicion, en una venta nueva
        double cantSolicitada;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                if (toProd.getCantFacturada() + toProd.getCantSinCargo() > cantSeparada) {
                    cantSolicitada = toProd.getCantFacturada() + toProd.getCantSinCargo() - cantSeparada;
                    try {
                        movimientos.Movimientos.separar(cn, toMov, toProd.getIdProducto(), cantSolicitada, "cantSinCargo");
                    } catch (Exception ex) {
                        throw new SQLException(ex.getMessage());
                    }
                } else {
                    cantSolicitada = cantSeparada - toProd.getCantFacturada() - toProd.getCantSinCargo();
                    movimientos.Movimientos.liberar(cn, toMov, toProd.getIdProducto(), cantSolicitada, "cantSinCargo");
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

    private void liberaSimilaresSinCargo(Connection cn, TOVenta toMov, TOVentaProducto toProd, double cantSolicitada) throws SQLException {
        String strSQL;
        int idEmpaque;
        double disponibles, cantSeparar;
        try (Statement st = cn.createStatement(); Statement st1 = cn.createStatement(); Statement st2 = cn.createStatement()) {
            ResultSet rs1;
            // Obtiene los productos con cantSinCargo > 0 en el movimiento
            strSQL = "SELECT D.idEmpaque, D.cantSinCargo\n"
                    + "FROM (SELECT * FROM empaquesSimilares WHERE idEmpaque=" + toProd.getIdProducto() + ") S\n"
                    + "INNER JOIN movimientosDetalle D ON D.idEmpaque=S.idSimilar\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "WHERE M.idMovto=" + toMov.getIdMovto() + " AND D.cantSinCargo > 0\n"
                    + "ORDER BY CASE WHEN S.idSimilar=S.idEmpaque THEN 1 ELSE 0 END, M.referencia, D.cantFacturada, D.cantSinCargo";
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next() && cantSolicitada != 0) {
                idEmpaque = rs.getInt("idEmpaque");
                disponibles = rs.getInt("cantSinCargo");
                strSQL = "SELECT M.idMovtoAlmacen, M.idMovto, D.idEmpaque, D.lote, L.fechaCaducidad, ISNULL(P.idPedido, 0) AS idPedido\n"
                        + "FROM movimientos M\n"
                        + "INNER JOIN movimientosDetalleAlmacen D ON D.idMovtoAlmacen=M.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes L ON L.idAlmacen=M.idAlmacen AND L.idEmpaque=D.idEmpaque AND L.lote=D.lote\n"
                        + "INNER JOIN (SELECT * FROM empaquesSimilares WHERE idEmpaque=" + toProd.getIdProducto() + ") S ON S.idSimilar=D.idEmpaque\n"
                        + "LEFT JOIN pedidosDetalle P ON P.idPedido=M.referencia AND P.idEmpaque=D.idEmpaque\n"
                        + "WHERE M.idMovto=" + toMov.getIdMovto() + " AND D.idEmpaque=" + idEmpaque + "\n"
                        + "ORDER BY L.fechaCaducidad DESC";
                rs1 = st1.executeQuery(strSQL);
                while (rs1.next() && cantSolicitada != 0) {
                    if (disponibles < cantSolicitada) {
                        cantSeparar = disponibles;
                    } else {
                        cantSeparar = cantSolicitada;
                    }
                    disponibles -= cantSeparar;
                    cantSolicitada -= cantSeparar;

                    strSQL = "UPDATE movimientosDetalleAlmacen\n"
                            + "SET cantidad=cantidad-" + cantSeparar + "\n"
                            + "WHERE idMovtoAlmacen=" + toMov.getIdMovtoAlmacen() + " AND idEmpaque=" + idEmpaque + " AND lote='" + rs1.getString("lote") + "'";
                    st2.executeUpdate(strSQL);

                    strSQL = "UPDATE almacenesLotes\n"
                            + "SET separados=separados-" + cantSeparar + "\n"
                            + "WHERE idAlmacen=" + toMov.getIdAlmacen() + " AND idEmpaque=" + idEmpaque + " AND lote='" + rs1.getString("lote") + "'";
                    st2.executeUpdate(strSQL);

                    strSQL = "UPDATE movimientosDetalle\n"
                            + "SET cantSinCargo=cantSinCargo-" + cantSeparar + "\n"
                            + "WHERE idMovto=" + toMov.getIdMovto() + " AND idEmpaque=" + idEmpaque;
                    st2.executeUpdate(strSQL);

                    strSQL = "UPDATE almacenesEmpaques\n"
                            + "SET separados=separados-" + cantSeparar + "\n"
                            + "WHERE idAlmacen=" + toMov.getIdAlmacen() + " AND idEmpaque=" + idEmpaque;
                    st2.executeUpdate(strSQL);
                }
            }
        }
    }

    private void separaSimilaresSinCargo(Connection cn, TOVenta toMov, TOVentaProducto toProd, double cantSolicitada) throws SQLException, Exception {
        String strSQL;
        int idEmpaque;
        double disponibles, cantSeparar, cantSeparada;
        TOVentaProducto toSimilar = new TOVentaProducto();
        try (Statement st = cn.createStatement(); Statement st1 = cn.createStatement()) {
            ResultSet rs1;
            // Calcula los disponibles entre todos los similares
            strSQL = "SELECT ISNULL(SUM(CASE WHEN L.disponibles <= E.existencia-E.separados THEN L.disponibles\n"
                    + "			ELSE E.existencia-E.separados END), 0) AS disponibles\n"
                    + "FROM (SELECT L.idEmpaque, SUM(L.existencia-L.separados) AS disponibles\n"
                    + "	  FROM empaquesSimilares S\n"
                    + "	  INNER JOIN almacenesLotes L ON L.idAlmacen=" + toMov.getIdAlmacen() + " AND L.idEmpaque=S.idSimilar\n"
                    + "	  WHERE S.idEmpaque=" + toProd.getIdProducto() + "\n"
                    + "	  GROUP BY L.idEmpaque) L\n"
                    + "INNER JOIN almacenesEmpaques E ON E.idEmpaque=L.idEmpaque\n"
                    + "WHERE E.idAlmacen=" + toMov.getIdAlmacen();
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                // Calculo disponibles (minimo disponible entre almacen y oficina) de los empaques similares
                if (rs.getDouble("disponibles") < cantSolicitada) {
                    // Si no hay suficientes disponibles se aborta la transaccion
                    throw new Exception("No hay existencia suficiente entre similares !!!");
                }
            }
            idEmpaque = 0;
            cantSeparada = 0;
            // Obteniendo todos los similares con disponibles
            strSQL = "SELECT ISNULL(D.idPedido, 0) AS idPedido, ISNULL(D.idMovto, 0) AS idMovto, L.idEmpaque, L.lote\n"
                    + "FROM (SELECT  M.idMovto, M.referencia AS idPedido, D.idEmpaque\n"
                    + "      FROM movimientosDetalle D\n"
                    + "      INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "      WHERE D.idMovto=" + toMov.getIdMovto() + ") D\n"
                    + "RIGHT JOIN empaquesSimilares S ON S.idSimilar=D.idEmpaque\n"
                    + "INNER JOIN almacenesLotes L ON L.idAlmacen=" + toMov.getIdAlmacen() + " AND L.idEmpaque=S.idSimilar\n"
                    + "WHERE S.idEmpaque=" + toProd.getIdProducto() + " AND L.existencia-L.separados > 0\n"
                    + "ORDER BY IIF(S.idSimilar=S.idEmpaque, 0, 1), ISNULL(D.idPedido, 0) DESC, ISNULL(D.idMovto, 0) DESC, L.idEmpaque, L.fechaCaducidad";
            rs = st.executeQuery(strSQL);
            while (rs.next() && cantSolicitada != 0) {
                // Para todos los lotes con existencia disponible en los empaques similares
                // Sin importar si el empaque esta o no en el movimiento
                if (idEmpaque != rs.getInt("idEmpaque")) {
                    if (idEmpaque != 0) {
                        // Suma los separados a la cantidad sin cargo del empaque en cuestion
                        strSQL = "UPDATE movimientosDetalle\n"
                                + "SET cantSinCargo=cantSinCargo+" + cantSeparada + "\n"
                                + "WHERE idMovto=" + toMov.getIdMovto() + " AND idEmpaque=" + idEmpaque;
                        st1.executeUpdate(strSQL);

                        // Separa los empaques en el almacen correspondiente
                        strSQL = "UPDATE almacenesEmpaques set separados=separados+" + cantSeparada + "\n"
                                + "WHERE idAlmacen" + toMov.getIdAlmacen() + " AND idEmpaque=" + idEmpaque;
                        st1.executeUpdate(strSQL);
                    }
                    idEmpaque = rs.getInt("idEmpaque");
                    cantSeparada = 0;
                    if (rs.getInt("idMovto") == 0) {
                        toSimilar.setIdPedido(0);
                        toSimilar.setIdMovto(toMov.getIdMovto());
                        toSimilar.setIdProducto(idEmpaque);
                        toSimilar.setCantOrdenada(0);
                        toSimilar.setCantOrdenadaSinCargo(0);
                        toSimilar.setCantFacturada(0);
                        toSimilar.setCantSinCargo(0);
                        toSimilar.setCostoPromedio(0);
                        this.agregaProducto(cn, toMov, toSimilar);
                    }
                }
                // Calcula los disponibles de un lote del empaque de la consulta anterior
                strSQL = "SELECT ISNULL(D.idMovtoAlmacen, 0) AS idMovtoAlmacen\n"
                        + "     , CASE WHEN L.existencia-L.separados <= E.existencia-E.separados\n"
                        + "             THEN L.existencia-L.separados\n"
                        + "             ELSE E.existencia-E.separados END AS disponibles\n"
                        + "FROM almacenesLotes L\n"
                        + "INNER JOIN almacenesEmpaques E ON E.idAlmacen=L.idAlmacen AND E.idEmpaque=L.idEmpaque\n"
                        + "LEFT JOIN movimientosDetalleAlmacen D ON D.idMovtoAlmacen=" + toMov.getIdMovtoAlmacen() + " AND D.idEmpaque=L.idEmpaque AND D.lote=L.lote\n"
                        + "WHERE L.idAlmacen=" + toMov.getIdAlmacen() + " AND L.idEmpaque=" + idEmpaque + " AND L.lote='" + rs.getString("lote") + "'";
                rs1 = st1.executeQuery(strSQL);
                if (rs1.next() && cantSolicitada != 0) {
                    // Si tiene registro en almacenesEmpaques
                    disponibles = rs1.getDouble("disponibles");
                    if (disponibles > 0) {
                        // Si tiene disponibles
                        if (disponibles < cantSolicitada) {
                            cantSeparar = disponibles;
                        } else {
                            cantSeparar = cantSolicitada;
                        }
                        cantSeparada += cantSeparar;
                        cantSolicitada -= cantSeparar;

                        if (rs1.getInt("idMovtoAlmacen") == 0) {
                            // Si el lote no existe
                            strSQL = "INSERT INTO movimientosDetalleAlmacen (idMovtoAlmacen, idEmpaque, lote, cantidad, fecha, existenciaAnterior)\n"
                                    + "VALUES (" + toMov.getIdMovtoAlmacen() + ", " + idEmpaque + ", '" + rs.getString("lote") + "', 0, '', 0)";
                            st1.executeUpdate(strSQL);
                        }
                        strSQL = "UPDATE movimientosDetalleAlmacen\n"
                                + "SET cantidad=cantidad+" + cantSeparar + "\n"
                                + "WHERE idMovtoAlmacen=" + toMov.getIdMovtoAlmacen() + " AND idEmpaque=" + idEmpaque + " AND lote='" + rs.getString("lote") + "'";
                        st1.executeUpdate(strSQL);

                        // Separa los lotes en el almacen correspondiente
                        strSQL = "UPDATE almacenesLotes\n"
                                + "SET separados=separados+" + cantSeparar + "\n"
                                + "WHERE idAlmacen=" + toMov.getIdAlmacen() + " AND idEmpaque=" + idEmpaque + " AND lote='" + rs.getString("lote") + "'";
                        st1.executeUpdate(strSQL);
                    }
                }
            }
            if (cantSolicitada == 0) {
                // Suma los separados a la cantidad sin cargo del empaque en cuestion
                strSQL = "UPDATE movimientosDetalle\n"
                        + "SET cantSinCargo=cantSinCargo+" + cantSeparada + "\n"
                        + "WHERE idMovto=" + toMov.getIdMovto() + " AND idEmpaque=" + idEmpaque;
                st1.executeUpdate(strSQL);

                // Separa los empaques en el almacen correspondiente
                strSQL = "UPDATE almacenesEmpaques set separados=separados+" + cantSeparada + "\n"
                        + "WHERE idAlmacen=" + toMov.getIdAlmacen() + " AND idEmpaque=" + idEmpaque;
                st1.executeUpdate(strSQL);
            } else {
                throw new SQLException("Error en la logica de los separados, algo anda mal !!!");
            }
        }
    }

    private ArrayList<TOVentaProducto> obtenDetalleSimilares(Connection cn, int idMovto, int idProducto) throws SQLException {
        ArrayList<TOVentaProducto> similares = new ArrayList<>();
        String strSQL = "SELECT E.cod_pro, D.*, ISNULL(PD.idPedido, 0) AS idPedido\n"
                + "        , ISNULL(PD.cantOrdenada-PD.cantSurtida, 0) AS cantOrdenada\n"
                + "        , ISNULL(PD.cantOrdenadaSinCargo-cantSurtidaSinCargo, 0) AS cantOrdenadaSinCargo\n"
                + "FROM movimientosDetalle D\n"
                + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                + "INNER JOIN empaquesSimilares S ON S.idSimilar=D.idEmpaque\n"
                + "INNER JOIN empaques E ON E.idEmpaque=D.idEmpaque\n"
                + "LEFT JOIN pedidosDetalle PD ON PD.idPedido=M.referencia AND PD.idEmpaque=D.idEmpaque\n"
                + "WHERE D.idMovto=" + idMovto + " AND S.idEmpaque=" + idProducto;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                similares.add(this.construirProductoOficina(rs));
            }
        }
        return similares;
    }

    private ArrayList<TOVentaProducto> actualizaProductoCantidad(Connection cn, TOVenta toVta, TOVentaProducto toProd, double separados) throws SQLException, Exception {
        // Con la cantidad facturada checa los boletines y ajusta la cantidad sin cargo en su caso
        String strSQL;
        double cantSolicitada, cantSeparada;
        double cantFacturadaOrig = separados - toProd.getCantSinCargo();
        double cantSinCargoOrig = toProd.getCantSinCargo();
        ArrayList<TOVentaProducto> similares = new ArrayList<>();
        if (toProd.getCantFacturada() > cantFacturadaOrig) {
            cantSolicitada = toProd.getCantFacturada() - cantFacturadaOrig;
            cantSeparada = movimientos.Movimientos.separar(cn, toVta, toProd.getIdProducto(), cantSolicitada, "cantFacturada");
            toProd.setCantFacturada(cantFacturadaOrig + cantSeparada);
        } else if (toProd.getCantFacturada() < cantFacturadaOrig) {
            cantSolicitada = cantFacturadaOrig - toProd.getCantFacturada();
            movimientos.Movimientos.liberar(cn, toVta, toProd.getIdProducto(), cantSolicitada, "cantFacturada");
            toProd.setCantFacturada(cantFacturadaOrig - cantSolicitada);
        }
        ArrayList<Double> boletin;
        if (toVta.getReferencia() != 0) {
            boletin = new ArrayList<>();
            boletin.add(1.0);
            boletin.add(0.0);
        } else {
            boletin = movimientos.Movimientos.obtenerBoletinSinCargo(cn, toVta.getIdEmpresa(), toVta.getIdReferencia(), toProd.getIdProducto());
            try (Statement st = cn.createStatement()) {
                // Necesito saber cuantos sin cargo tengo y debo tener entre los similares
                // Aqui obtengo la suma de cantFacturada de todos los similares y del producto original
                strSQL = "SELECT SUM(D.cantFacturada) AS cantFacturada, SUM(D.cantSinCargo) AS cantSinCargo\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN empaquesSimilares S ON S.idSimilar=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toVta.getIdMovto() + " AND S.idEmpaque=" + toProd.getIdProducto() + "\n"
                        + "GROUP BY S.idEmpaque";
                ResultSet rs = st.executeQuery(strSQL);
                if (boletin.get(0) > 0 && boletin.get(1) >= 0) {
                    if (rs.next()) {
                        // Si hay similares (por INNER JOIN empaquesSimilares en consulta anterior)
                        // Calculo cuantos sinCargo requiero incluyendo el producto original
                        cantSinCargoOrig = (int) (rs.getDouble("cantFacturada") / boletin.get(0)) * boletin.get(1);
                        if (cantSinCargoOrig != rs.getDouble("cantSinCargo")) {
                            if (cantSinCargoOrig > rs.getDouble("cantSinCargo")) {
                                cantSolicitada = cantSinCargoOrig - rs.getDouble("cantSinCargo");
                                this.separaSimilaresSinCargo(cn, toVta, toProd, cantSolicitada);
                            } else if (cantSinCargoOrig < rs.getDouble("cantSinCargo")) {
                                cantSolicitada = rs.getDouble("cantSinCargo") - cantSinCargoOrig;
                                this.liberaSimilaresSinCargo(cn, toVta, toProd, cantSolicitada);
                            }
                            similares = this.obtenDetalleSimilares(cn, toVta.getIdMovto(), toProd.getIdProducto());
                            if (similares.size() == 1) {
                                toProd.setCantSinCargo(cantSinCargoOrig);
                                similares.clear();
                            }
                        }
                    } else {
                        // Si no hay similares, atiende solamente las piezas sin cargo del producto en cuestion
                        toProd.setCantSinCargo((int) (toProd.getCantFacturada() / boletin.get(0)) * boletin.get(1));
                        if (toProd.getCantSinCargo() > cantSinCargoOrig) {
                            cantSolicitada = toProd.getCantSinCargo() - cantSinCargoOrig;
                            cantSeparada = movimientos.Movimientos.separar(cn, toVta, toProd.getIdProducto(), cantSolicitada, "cantSinCargo");
                            toProd.setCantSinCargo(cantSinCargoOrig + cantSeparada);
                        } else if (toProd.getCantSinCargo() < cantSinCargoOrig) {
                            cantSolicitada = cantSinCargoOrig - toProd.getCantSinCargo();
                            movimientos.Movimientos.liberar(cn, toVta, toProd.getIdProducto(), cantSolicitada, "cantSinCargo");
                            toProd.setCantSinCargo(cantSinCargoOrig - cantSolicitada);
                        }
                    }
                } else if (rs.next()) {
                    cantSolicitada = rs.getDouble("cantSinCargo");
                    if (cantSolicitada > 0) {
                        this.liberaSimilaresSinCargo(cn, toVta, toProd, cantSolicitada);
                        similares = this.obtenDetalleSimilares(cn, toVta.getIdMovto(), toProd.getIdProducto());
                        if (similares.size() == 1) {
                            toProd.setCantSinCargo(0);
                            similares.clear();
                        }
                    }
                } else if (cantSinCargoOrig > 0) {
                    movimientos.Movimientos.liberar(cn, toVta, toProd.getIdProducto(), cantSinCargoOrig, "cantSinCargo");
                    toProd.setCantSinCargo(0);
                }
            }
        }
//        return incompleta;
        return similares;
    }

    public ArrayList<TOVentaProducto> actualizarProductoCantidad(TOVenta toMov, TOVentaProducto toProd, double cantSeparada) throws SQLException, Exception {
        // Cuando esto capturando en pantalla de edicion, en una venta nueva
        ArrayList<TOVentaProducto> similares = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                similares = this.actualizaProductoCantidad(cn, toMov, toProd, cantSeparada);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } catch (Exception ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return similares;
    }

    public double obtenerImpuestosProducto(int idMovto, int idEmpaque, ArrayList<ImpuestosProducto> impuestos) throws SQLException {
        double importeImpuestos = 0;
        try (Connection cn = this.ds.getConnection()) {
            importeImpuestos = movimientos.Movimientos.obtenImpuestosProducto(cn, idMovto, idEmpaque, impuestos);
        }
        return importeImpuestos;
    }

    private void agregaProducto(Connection cn, TOVenta toVta, TOVentaProducto toProd) throws SQLException {
        movimientos.Movimientos.agregaProductoOficina(cn, toProd, toVta.getIdImpuestoZona());
        movimientos.Movimientos.actualizaProductoPrecio(cn, toVta, toProd);
    }

    public void agregarProducto(TOVenta toVta, TOVentaProducto toProd) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                this.agregaProducto(cn, toVta, toProd);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public TOVentaProducto obtenerProductoOficina(Connection cn, int idMovto, int idProducto) throws SQLException {
        TOVentaProducto toProd = null;
        String strSQL = "SELECT E.cod_pro, MD.*, ISNULL(PD.idPedido, 0) AS idPedido\n"
                + "         , ISNULL(PD.cantOrdenada-PD.cantSurtida, 0) AS cantOrdenada\n"
                + "         , ISNULL(PD.cantOrdenadaSinCargo-cantSurtidaSinCargo, 0) AS cantOrdenadaSinCargo\n"
                + "FROM movimientosDetalle MD\n"
                + "INNER JOIN movimientos M ON M.idMovto=MD.idMovto\n"
                + "INNER JOIN empaques E ON E.idEmpaque=MD.idEmpaque\n"
                + "LEFT JOIN pedidosDetalle PD ON PD.idPedido=M.referencia AND PD.idEmpaque=MD.idEmpaque\n"
                + "WHERE MD.idMovto=" + idMovto + " AND MD.idEmpaque=" + idProducto;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                toProd = this.construirProductoOficina(rs);
            }
        }
        return toProd;
    }

    public TOVentaProducto construirProductoOficina(ResultSet rs) throws SQLException {
        TOVentaProducto toProd = new TOVentaProducto();
        toProd.setIdPedido(rs.getInt("idPedido"));
        toProd.setCantOrdenada(rs.getDouble("cantOrdenada"));
        toProd.setCantOrdenadaSinCargo(rs.getDouble("cantOrdenadaSinCargo"));
        toProd.setCod_pro(rs.getString("cod_pro"));
        movimientos.Movimientos.construirProductoOficina(rs, toProd);
        return toProd;
    }

    private ArrayList<TOVentaProducto> obtenDetalleOficina(Connection cn, TOVenta toVta) throws SQLException {
        ArrayList<TOVentaProducto> detalle = new ArrayList<>();
        String strSQL = "SELECT E.cod_pro, MD.*, ISNULL(PD.idPedido, 0) AS idPedido\n"
                + "         , ISNULL(PD.cantOrdenada-PD.cantSurtida, 0) AS cantOrdenada\n"
                + "         , ISNULL(PD.cantOrdenadaSinCargo-cantSurtidaSinCargo, 0) AS cantOrdenadaSinCargo\n"
                + "FROM movimientosDetalle MD\n"
                + "INNER JOIN movimientos M ON M.idMovto=MD.idMovto\n"
                + "INNER JOIN empaques E ON E.idEmpaque=MD.idEmpaque\n"
                + "LEFT JOIN pedidosDetalle PD ON PD.idPedido=M.referencia AND PD.idEmpaque=MD.idEmpaque\n"
                + "WHERE MD.idMovto=" + toVta.getIdMovto();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                detalle.add(this.construirProductoOficina(rs));
            }
            movimientos.Movimientos.bloquearMovimientoOficina(cn, toVta, this.idUsuario);
        }
        return detalle;
    }

    public ArrayList<TOVentaProducto> obtenerDetalleOficina(TOVenta toVta, String aviso) throws SQLException {
        // Al cargar una venta no cerrada, actualiza precios y verifica boletines de productos
        aviso = "";
        double separados;
        ArrayList<TOVentaProducto> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                detalle = this.obtenDetalleOficina(cn, toVta);
                if (toVta.getIdUsuario() == toVta.getPropietario() && toVta.getReferencia() == 0 && toVta.getEstatus() == 0) {
                    TOVentaProducto toProd;
                    for (TOVentaProducto to : detalle) {
                        toProd = this.obtenerProductoOficina(cn, toVta.getIdMovto(), to.getIdProducto());
                        movimientos.Movimientos.actualizaProductoPrecio(cn, toVta, toProd);
                        try {
                            separados = toProd.getCantFacturada() + toProd.getCantSinCargo();
                            this.actualizaProductoCantidad(cn, toVta, toProd, separados);
                        } catch (Exception ex) {
                            if (!aviso.isEmpty()) {
                                aviso += ", ";
                            }
                            aviso += toProd.getCod_pro();
                        }
                    }
                    detalle = this.obtenDetalleOficina(cn, toVta);
                    if (!aviso.isEmpty()) {
                        movimientos.Movimientos.liberarMovimientoOficina(cn, toVta.getIdMovto(), this.idUsuario);
                        toVta.setPropietario(0);
                    }
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

    public void agregarVenta(TOVenta toVta, int idMoneda) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                toVta.setIdUsuario(this.idUsuario);
                toVta.setPropietario(this.idUsuario);

                TOComprobante to = new TOComprobante(toVta.getIdTipo(), toVta.getIdEmpresa(), toVta.getIdReferencia(), idMoneda);
                to.setTipo('1');
                to.setNumero(String.valueOf(toVta.getReferencia()));
                to.setIdUsuario(this.idUsuario);
                to.setPropietario(0);
                comprobantes.Comprobantes.agregar(cn, to);

                toVta.setIdComprobante(to.getIdComprobante());
                movimientos.Movimientos.agregaMovimientoAlmacen(cn, toVta, false);
                movimientos.Movimientos.agregaMovimientoOficina(cn, toVta, false);

                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw (e);
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private void construir(ResultSet rs, TOVenta toVta) throws SQLException {
        toVta.setIdPedidoOC(rs.getInt("idPedidoOC"));
        toVta.setIdMoneda(rs.getInt("idMoneda"));
        toVta.setOrdenDeCompra(rs.getString("ordenDeCompra"));
        toVta.setOrdenDeCompraFecha(new java.util.Date(rs.getTimestamp("ordenDeCompraFecha").getTime()));
        toVta.setCanceladoMotivo(rs.getString("canceladoMotivo"));
        toVta.setCanceladoFecha(new java.util.Date(rs.getDate("canceladoFecha").getTime()));
        movimientos.Movimientos.construirMovimientoOficina(rs, toVta);
    }

    private TOVenta construir(ResultSet rs) throws SQLException {
        TOVenta toMov = new TOVenta(28);
        this.construir(rs, toMov);
        return toMov;
    }

    public ArrayList<TOVenta> obtenerVentasOficina(int idAlmacen, int estatus, Date fechaInicial) throws SQLException {
        String condicion = ">=7";
        if (estatus == 0) {
            condicion = "<7";
        }
        if (fechaInicial == null) {
            fechaInicial = new Date();
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TOVenta> ventas = new ArrayList<>();
        String strSQL = "SELECT M.*\n"
                + "     , ISNULL(P.idPedidoOC, 0) AS idPedidoOC, ISNULL(P.idMoneda, 0) AS idMoneda\n"
                + "     , ISNULL(P.canceladoMotivo, '') AS canceladoMotivo, ISNULL(P.canceladoFecha, '1900-01-01') AS canceladoFecha\n"
                + "     , ISNULL(OC.ordenDeCompra, '') AS ordenDeCompra, ISNULL(OC.ordenDeCompraFecha, '1900-01-01') AS ordenDeCompraFecha\n"
                + "FROM movimientos M\n"
                + "LEFT JOIN pedidos P ON P.idPedido=M.referencia\n"
                + "LEFT JOIN pedidosOC OC ON OC.idPedidoOC=P.idPedidoOC\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=28 AND M.estatus" + condicion + "\n";
        if (estatus != 0) {
            strSQL += "         AND CONVERT(date, M.fecha) >= '" + format.format(fechaInicial) + "'\n";
        }
        strSQL += "ORDER BY M.fecha DESC";
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                ventas.add(this.construir(rs));
            }
        } finally {
            cn.close();
        }
        return ventas;
    }
}