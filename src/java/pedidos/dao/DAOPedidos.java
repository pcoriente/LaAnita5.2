package pedidos.dao;

import envios.Envios;
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
import movimientos.Movimientos;
import pedidos.to.TOPedido;
import pedidos.to.TOPedidoProducto;
import pedidos.Pedidos;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOPedidos {

    int idUsuario, idCedis;
    private DataSource ds = null;

    public DAOPedidos() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
        this.idUsuario = usuarioSesion.getUsuario().getId();
        this.idCedis = usuarioSesion.getUsuario().getIdCedis();

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
    }

    public ArrayList<TOPedidoProducto> cerrarFincadoDirecto(TOPedido toPed, int recepcionIdMovto) throws SQLException {
        String strSQL;
        ArrayList<TOPedidoProducto> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                toPed.setIdUsuario(this.idUsuario);
                toPed.setPropietario(0);
                toPed.setEstatus(5);

                strSQL = "UPDATE D\n"
                        + "SET cantFacturada=EPD.cantEnviar, cantSinCargo=EPD.cantEnviarSinCargo\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN enviosPedidosDetalle EPD ON EPD.idVenta=M.referencia AND EPD.idEmpaque=D.idEmpaque\n"
                        + "WHERE M.idMovto=" + toPed.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "SELECT *\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "LEFT JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toPed.getIdMovto() + " AND (A.idEmpaque IS NULL OR A.existencia - A.separados < D.cantFacturada + D.cantSinCargo)";
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    throw new SQLException("No hay existencia suficiente !!");
                }
                strSQL = "UPDATE A\n"
                        + "SET separados=A.separados+(D.cantFacturada+D.cantSinCargo)\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toPed.getIdMovto();
                st.executeUpdate(strSQL);

                Pedidos.cerrarVenta(cn, toPed);

                toPed.setIdUsuario(this.idUsuario);
                toPed.setPropietario(0);
                toPed.setEstatus(7);

                int recepcionIdMovtoAlmacen = 0;
                strSQL = "SELECT idMovtoAlmacen FROM movimientos WHERE idMovto=" + recepcionIdMovto;
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    recepcionIdMovtoAlmacen = rs.getInt("idMovtoAlmacen");
                } else {
                    throw new SQLException("No se encontró el movimiento de la recepción !!!");
                }
                strSQL = "SELECT D.idEmpaque\n"
                        + "FROM (SELECT idEmpaque, SUM(cantidad) AS cantidad\n"
                        + "	FROM movimientosDetalleAlmacen\n"
                        + "	WHERE idMovtoAlmacen=" + recepcionIdMovtoAlmacen + "\n"
                        + "	GROUP BY idEmpaque) A\n"
                        + "INNER JOIN movimientosDetalle D ON D.idEmpaque=A.idEmpaque\n"
                        + "WHERE D.idMovto=" + toPed.getIdMovto() + " AND A.cantidad != D.cantFacturada + D.cantSinCargo";
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    throw new SQLException("Cantidades recibidas no coinciden con lotes !!");
                }
                strSQL = "INSERT INTO movimientosDetalleAlmacen (idMovtoAlmacen, idEmpaque, lote, cantidad, fecha, existenciaAnterior)\n"
                        + "SELECT " + toPed.getIdMovtoAlmacen() + " AS idMovtoAlmacen, idEmpaque, lote, cantidad, '' AS fecha, 0 AS existenciaAnterior\n"
                        + "FROM movimientosDetalleAlmacen\n"
                        + "WHERE idMovtoAlmacen=" + recepcionIdMovtoAlmacen;
                st.executeUpdate(strSQL);

                strSQL = "SELECT *\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "LEFT JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                        + "WHERE D.idMovtoAlmacen=" + toPed.getIdMovtoAlmacen() + " AND (A.lote IS NULL OR A.existencia - A.separados < D.cantidad)";
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    throw new SQLException("No hay existencia de lotes suficiente !!!");
                }
                strSQL = "UPDATE A\n"
                        + "SET separados=A.separados+D.cantidad\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                        + "WHERE D.idMovtoAlmacen=" + toPed.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                Pedidos.cierraVentaAlmacen(cn, toPed);
                detalle = this.obtenDetalle(cn, toPed);

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

    public void obtenerEstatusTraspaso(int idSolicitud) throws SQLException, Exception {
        try (Connection cn = this.ds.getConnection()) {
            Envios.obtenerEstatusTraspaso(cn, idSolicitud);
        }
    }

    public int obtenerIdMovtoRecepcion(int idSolicitud) throws SQLException, Exception {
        int idMovtoRecepcion = 0;
        try (Connection cn = this.ds.getConnection()) {
            idMovtoRecepcion = Envios.obtenerIdMovtoRecepcion(cn, idSolicitud);
        }
        return idMovtoRecepcion;
    }
//
//    public int obtenerEstatusRecepcion(int idSolicitud) throws SQLException {
//        int estatus = 0;
//        int idMovto;
//        try (Connection cn = this.ds.getConnection()) {
//            try {
//                Envios.obtenerEstatusTraspaso(cn, idSolicitud);
//            } catch (Exception ex) {
//                Mensajes.mensajeAlert(ex.getMessage());
//            }
//            estatus = Envios.obtenerEstatusRecepcion(cn, idSolicitud);
//        }
//        return estatus;
//    }
//    
//    public int obtenerEstatusTraspasoDirecto1(int idEnvio, int idAlmacen, int idSolicitud) throws SQLException {
//        int estatus = 0;
//        try (Connection cn = this.ds.getConnection()) {
//            estatus = Envios.obtenerEstatusEnvioTraspaso(cn, idEnvio, idAlmacen);
//            if (estatus == 1) {
//                estatus = Envios.obtenerEstatusTraspasoDirecto(cn, idSolicitud);
//            }
//        }
//        return estatus;
//    }

    public void cancelarPedido(TOPedido toPed) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "SELECT estatus FROM pedidos WHERE idPedido=" + toPed.getReferencia();
                ResultSet rs = st.executeQuery(strSQL);
                rs.next();
                toPed.setPedidoEstatus(rs.getInt("estatus"));
                if (toPed.getPedidoEstatus() != 1) {
                    throw new SQLException("El pedido ya no puede ser cancelado !!!");
                }
                toPed.setPedidoIdUsuario(this.idUsuario);
                toPed.setIdUsuario(this.idUsuario);
                toPed.setPropietario(0);
                toPed.setPedidoEstatus(6);
                toPed.setEstatus(6);

                strSQL = "UPDATE A\n"
                        + "SET separados=A.separados-D.cantidad\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                        + "WHERE D.idMovtoAlmacen=" + toPed.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosDetalleAlmacen SET cantidad=0 WHERE idMovtoAlmacen=" + toPed.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosAlmacen\n"
                        + "SET propietario=" + toPed.getPropietario() + ", estatus=" + toPed.getEstatus() + "\n"
                        + "WHERE idMovtoAlmacen=" + toPed.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE A\n"
                        + "SET separados=A.separados-(D.cantFacturada+D.cantSinCargo)\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toPed.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosDetalle SET cantFacturada=0, cantSinCargo=0 WHERE idMovto=" + toPed.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientos\n"
                        + "SET propietario=" + toPed.getPropietario() + ", estatus=" + toPed.getEstatus() + "\n"
                        + "WHERE idMovto=" + toPed.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE pedidos\n"
                        + "SET fecha=GETDATE(), idUsuario=" + this.idUsuario + "\n"
                        + "WHERE idPedido=" + toPed.getReferencia();
                st.executeUpdate(strSQL);

//                strSQL = "SELECT fecha FROM pedidos WHERE idPedido=" + toPed.getReferencia();
//                rs = st.executeQuery(strSQL);
//                rs.next();
//                toPed.setPedidoFecha(new java.util.Date(rs.getTimestamp("fecha").getTime()));

                strSQL = "UPDATE pedidosOrdenes\n"
                        + "SET canceladoMotivo='" + toPed.getCanceladoMotivo() + "'\n"
                        + "WHERE idOrden=" + toPed.getIdOrden();
                st.executeUpdate(strSQL);

                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw (e);
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

//    public void cancelarVenta(TOPedido toPed) throws SQLException {
//        String strSQL;
//        try (Connection cn = this.ds.getConnection()) {
//            cn.setAutoCommit(false);
//            try (Statement st = cn.createStatement()) {
//                strSQL = "SELECT estatus FROM pedidos WHERE idPedido=" + toPed.getIdPedido();
//                ResultSet rs = st.executeQuery(strSQL);
//                rs.next();
//                toPed.setPedidoEstatus(7);
//                if (rs.getInt("estatus") == 3) {
//                    toPed.setPedidoEstatus(6);
//                }
//                toPed.setIdUsuario(this.idUsuario);
//                toPed.setPropietario(0);
//                toPed.setEstatus(6);
//
//                strSQL = "UPDATE A\n"
//                        + "SET separados=A.separados-D.cantidad\n"
//                        + "FROM movimientosDetalleAlmacen D\n"
//                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
//                        + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
//                        + "WHERE D.idMovtoAlmacen=" + toPed.getIdMovtoAlmacen();
//                st.executeUpdate(strSQL);
//
//                strSQL = "UPDATE movimientosDetalleAlmacen SET cantidad=0 WHERE idMovtoAlmacen=" + toPed.getIdMovtoAlmacen();
//                st.executeUpdate(strSQL);
//
//                strSQL = "UPDATE movimientosAlmacen\n"
//                        + "SET fecha=GETDATE(), idUsuario=" + toPed.getIdUsuario() + ", propietario=" + toPed.getPropietario() + ", estatus=" + toPed.getEstatus() + "\n"
//                        + "WHERE idMovtoAlmacen=" + toPed.getIdMovtoAlmacen();
//                st.executeUpdate(strSQL);
//
//                strSQL = "UPDATE A\n"
//                        + "SET separados=A.separados-(D.cantFacturada+D.cantSinCargo)\n"
//                        + "FROM movimientosDetalle D\n"
//                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
//                        + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
//                        + "WHERE D.idMovto=" + toPed.getIdMovto();
//                st.executeUpdate(strSQL);
//
//                strSQL = "UPDATE movimientosDetalle SET cantFacturada=0, cantSinCargo=0 WHERE idMovto=" + toPed.getIdMovto();
//                st.executeUpdate(strSQL);
//
//                strSQL = "UPDATE movimientos\n"
//                        + "SET fecha=GETDATE(), idUsuario=" + toPed.getIdUsuario() + ", propietario=" + toPed.getPropietario() + ", estatus=" + toPed.getEstatus() + "\n"
//                        + "WHERE idMovto=" + toPed.getIdMovto();
//                st.executeUpdate(strSQL);
//                
//                strSQL="UPDATE ventas\n"
//                        + "SET fecha=GETDATE(), idUsuario=" + this.idUsuario + "\n"
//                        + "WHERE idVenta=" + toPed.getReferencia();
//                st.executeUpdate(strSQL);
//
//                strSQL = "SELECT fecha FROM ventas WHERE idVenta=" + toPed.getReferencia();
//                rs = st.executeQuery(strSQL);
//                rs.next();
//                toPed.setPedidoFecha(new java.util.Date(rs.getTimestamp("fecha").getTime()));
//
//                strSQL = "UPDATE pedidos\n"
//                        + "SET canceladoMotivo='" + toPed.getCanceladoMotivo() + "', estatus=" + toPed.getPedidoEstatus() + "\n"
//                        + "WHERE idPedido=" + toPed.getIdPedido();
//                st.executeUpdate(strSQL);
//
//                cn.commit();
//            } catch (SQLException e) {
//                cn.rollback();
//                throw (e);
//            } finally {
//                cn.setAutoCommit(true);
//            }
//        }
//    }
//
    public ArrayList<TOPedidoProducto> surtirFincado(TOPedido toPed) throws SQLException, Exception {
        // Intenta surtir en automatico TODO el pedido fincado
        double cantSolicitada;
        ArrayList<TOPedidoProducto> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                detalle = this.obtenDetalle(cn, toPed);
                if (toPed.getIdUsuario() == toPed.getPropietario() && toPed.getEstatus() == 0) {
                    for (TOPedidoProducto to : detalle) {
                        if (to.getCantFacturada() < to.getCantOrdenada()) {
                            cantSolicitada = to.getCantOrdenada() - to.getCantFacturada();
                            try {
                                if (this.idCedis == 1) {
                                    Movimientos.separar(cn, toPed.getIdMovto(), to.getIdProducto(), cantSolicitada, "cantFacturada");
                                } else {
                                    movimientos.Movimientos.separar(cn, toPed, to.getIdProducto(), cantSolicitada, "cantFacturada");
                                }
                                to.setCantFacturada(to.getCantOrdenada());
                            } catch (Exception ex) {
                                // Si no se pudieron surtir todos los solicitados, pasa al siguiete producto
                            }
                        }
                        if (to.getCantSinCargo() < to.getCantOrdenadaSinCargo()) {
                            cantSolicitada = to.getCantOrdenadaSinCargo() - to.getCantSinCargo();
                            try {
                                if (this.idCedis == 1) {
                                    Movimientos.separar(cn, toPed.getIdMovto(), to.getIdProducto(), cantSolicitada, "cantSinCargo");
                                } else {
                                    movimientos.Movimientos.separar(cn, toPed, to.getIdProducto(), cantSolicitada, "cantSinCargo");
                                }
                                to.setCantSinCargo(to.getCantOrdenadaSinCargo());
                            } catch (Exception ex) {
                                // Si no se pudieron surtir todos los solicitados, pasa al siguiete producto
                            }
                        }
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

    public void cerrarPedido(TOPedido toPed) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                toPed.setIdUsuario(this.idUsuario);
                toPed.setPropietario(this.idUsuario);
                toPed.setIdUsuario(this.idUsuario);
                toPed.setPedidoEstatus(1);
                toPed.setEstatus(0);

                toPed.setPedidoFolio(movimientos.Movimientos.obtenMovimientoFolioOficina(cn, toPed.getIdAlmacen(), 54));
                strSQL = "UPDATE pedidos\n"
                        + "SET folio=" + toPed.getPedidoFolio() + ", estatus=" + toPed.getPedidoEstatus() + "\n"
                        + "WHERE idPedido=" + toPed.getReferencia();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM pedidosDetalle\n"
                        + "WHERE idPedido=" + toPed.getReferencia() + " AND cantOrdenada+cantOrdenadaSinCargo=0";
                st.executeUpdate(strSQL);

                strSQL = "UPDATE ventas\n"
                        + "SET diasCredito=" + toPed.getDiasCredito() + "\n"
                        + "WHERE idMovto=" + toPed.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE D\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "LEFT JOIN pedidosDetalle PD ON PD.idPedido=M.referencia AND PD.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toPed.getIdMovto() + " AND PD.idEmpaque IS NULL\n";
                st.executeUpdate(strSQL);

                strSQL = "DELETE I\n"
                        + "FROM movimientosDetalleImpuestos I\n"
                        + "LEFT JOIN movimientosDetalle D ON D.idMovto=I.idMovto AND D.idEmpaque=I.idEmpaque\n"
                        + "WHERE I.idMovto=" + toPed.getIdMovto() + " AND D.idMovto IS NULL";
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientos\n"
                        + "SET desctoComercial=" + toPed.getDesctoComercial() + "\n"
                        + "     , fecha=GETDATE(), idUsuario=" + this.idUsuario + ", propietario=" + toPed.getPropietario() + ", estatus=" + toPed.getEstatus() + "\n"
                        + "WHERE idMovto=" + toPed.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosAlmacen\n"
                        + "SET fecha=GETDATE(), idUsuario=" + this.idUsuario + ", propietario=" + toPed.getPropietario() + ", estatus=" + toPed.getEstatus() + "\n"
                        + "WHERE idMovtoAlmacen=" + toPed.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public ArrayList<TOPedidoProducto> generarPedidoVenta(TOPedido toPed) throws SQLException, Exception {
        ArrayList<TOPedidoProducto> detalle;
        String strSQL = "SELECT M.idMovto\n"
                + "FROM movimientos M\n"
                + "INNER JOIN pedidos P ON P.idPedido=M.referencia\n"
                + "WHERE P.idPedido=" + toPed.getReferencia() + " AND M.estatus=5";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                // Checa que no haya ningun movimiento con estatus=5
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    throw new Exception("El pedido ya tiene una venta pendiente !!!");
                }
                Pedidos.generarPedidoVenta(cn, toPed.getReferencia(), this.idUsuario);
                detalle = this.obtenDetalle(cn, toPed);

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

//    private void generaPedidoVenta(Connection cn, int idPedido) throws SQLException {
//        // Obtiene el movimiento original (el primer idMovto) del pedido, para con este crear el nuevo
//        String strSQL;
//        int idMovto;
//        TOPedido toPed;
//        try {
//            Pedidos.generarPedidoVenta(cn, idPedido, this.idUsuario);
//        }
//    }
//
    public void cerrarVenta(TOPedido toPed) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                toPed.setIdUsuario(this.idUsuario);
                toPed.setPropietario(0);
                toPed.setEstatus(5);

                Pedidos.cerrarVenta(cn, toPed);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void eliminarPedido(TOPedido toPed) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "DELETE FROM pedidosOrdenes WHERE idOrden=" + toPed.getIdOrden();
                st.executeUpdate(strSQL);

                this.eliminaVenta(cn, toPed);

                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw (e);
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private void eliminaVenta(Connection cn, TOPedido toPed) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            strSQL = "UPDATE L\n"
                    + "SET L.separados=L.separados-K.cantidad\n"
                    + "FROM almacenesLotes L\n"
                    + "INNER JOIN movimientosDetalleAlmacen K ON K.idMovtoAlmacen=" + toPed.getIdMovtoAlmacen() + " AND K.idEmpaque=L.idEmpaque AND K.lote=L.lote\n"
                    + "WHERE L.idAlmacen=" + toPed.getIdAlmacen();
            st.executeUpdate(strSQL);

            strSQL = "DELETE FROM movimientosDetalleAlmacen WHERE idMovtoAlmacen=" + toPed.getIdMovtoAlmacen();
            st.executeUpdate(strSQL);

            strSQL = "DELETE FROM movimientosAlmacen WHERE idMovtoAlmacen=" + toPed.getIdMovtoAlmacen();
            st.executeUpdate(strSQL);

            strSQL = "UPDATE E\n"
                    + "SET E.separados=E.separados-(D.cantFacturada+D.cantSinCargo)\n"
                    + "FROM almacenesEmpaques E\n"
                    + "INNER JOIN movimientosDetalle D ON D.idMovto=" + toPed.getIdMovto() + " AND D.idEmpaque=E.idEmpaque\n"
                    + "WHERE E.idAlmacen=" + toPed.getIdAlmacen();
            st.executeUpdate(strSQL);

            strSQL = "DELETE FROM movimientosDetalle WHERE idMovto=" + toPed.getIdMovto();
            st.executeUpdate(strSQL);

            strSQL = "DELETE FROM movimientos WHERE idMovto=" + toPed.getIdMovto();
            st.executeUpdate(strSQL);

            strSQL = "DELETE FROM movimientosDetalleImpuestos WHERE idMovto=" + toPed.getIdMovto();
            st.executeUpdate(strSQL);

            strSQL = "DELETE FROM comprobantes WHERE idComprobante=" + toPed.getIdComprobante();
            st.executeUpdate(strSQL);
            
            strSQL = "DELETE FROM ventas WHERE idMovto="+toPed.getIdMovto();
            st.executeUpdate(strSQL);

            strSQL = "DELETE FROM pedidos WHERE idPedido=" + toPed.getReferencia();
            st.executeUpdate(strSQL);

            strSQL = "DELETE FROM pedidosDetalle WHERE idPedido=" + toPed.getReferencia();
            st.executeUpdate(strSQL);
        }
    }

    public void eliminarVenta(TOPedido toPed) throws SQLException {

        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                this.eliminaVenta(cn, toPed);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void liberarPedido(TOPedido toPed) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                Pedidos.liberarPedido(cn, toPed, this.idUsuario);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public ArrayList<TOPedidoProducto> eliminarProductoPedido(TOPedido toPed, TOPedidoProducto toProd) throws SQLException {
        String strSQL;
        ArrayList<TOPedidoProducto> similares = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "DELETE FROM pedidosDetalle\n"
                        + "WHERE idPedido=" + toProd.getIdPedido() + " AND idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalle\n"
                        + "WHERE idMovto=" + toProd.getIdMovto() + " AND idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalleImpuestos\n"
                        + "WHERE idMovto=" + toProd.getIdMovto() + " AND idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);

                if (toPed.getEspecial() == 0) {
                    this.actualizaProductoCantidadPedido(cn, toPed, toProd);
                    similares = this.obtenSimilares(cn, toProd.getIdMovto(), toProd.getIdProducto());
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.close();
            }
        }
        return similares;
    }

    public ArrayList<TOPedidoProducto> eliminarProducto(TOPedido toPed, TOPedidoProducto toProd, double separados) throws SQLException, Exception {
        String strSQL;
        ArrayList<TOPedidoProducto> similares = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                similares = this.actualizaProductoCantidad(cn, toPed, toProd, separados);

                if (toPed.getReferencia() == 0 && toProd.getCantSinCargo() == 0) {
                    strSQL = "DELETE FROM movimientosDetalle\n"
                            + "WHERE idMovto=" + toProd.getIdMovto() + " AND idEmpaque=" + toProd.getIdProducto();
                    st.executeUpdate(strSQL);

                    strSQL = "DELETE FROM movimientosDetalleImpuestos\n"
                            + "WHERE idMovto=" + toProd.getIdMovto() + " AND idEmpaque=" + toProd.getIdProducto();
                    st.executeUpdate(strSQL);

                    strSQL = "DELETE FROM movimientosDetalleAlmacen\n"
                            + "WHERE idMovtoAlmacen=" + toPed.getIdMovtoAlmacen() + " AND idEmpaque=" + toProd.getIdProducto() + " AND cantidad=0";
                    st.executeUpdate(strSQL);
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } catch (Exception ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.close();
            }
        }
        return similares;
    }

    public void transferirSinCargoVenta(TOPedido toPed, TOPedidoProducto toProd, TOPedidoProducto toSimilar, double cantidad) throws SQLException, Exception {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                if (toSimilar.getIdMovto() == 0) {
                    toSimilar.setIdMovto(toProd.getIdMovto());
                    toSimilar.setCantOrdenada(0);
                    toSimilar.setCantOrdenadaSinCargo(0);
//                    this.agregaProducto(cn, toPed, toSimilar);
//                    Movimientos.agregarProductoVenta(cn, toPed, toSimilar, toPed.getOrdenDeCompraFecha());
                    Pedidos.agregaProductoPedido(cn, toPed, toProd);
                }
                if (this.idCedis == 1) {
                    Movimientos.separar(cn, toPed.getIdMovto(), toSimilar.getIdProducto(), cantidad, "cantSinCargo");
                    Movimientos.liberar(cn, toPed.getIdMovto(), toPed.getIdAlmacen(), toProd.getIdProducto(), cantidad, "cantSinCargo");
                } else {
                    movimientos.Movimientos.separar(cn, toPed, toSimilar.getIdProducto(), cantidad, "cantSinCargo");
                    movimientos.Movimientos.liberar(cn, toPed, toProd.getIdProducto(), cantidad, "cantSinCargo");
                }
                toSimilar.setCantSinCargo(toSimilar.getCantSinCargo() + cantidad);
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

    public ArrayList<TOPedidoProducto> transferirSinCargo(TOPedido toPed, TOPedidoProducto toProd, TOPedidoProducto toSimilar, double cantidad) throws SQLException {
        String strSQL;
        ArrayList<TOPedidoProducto> similares = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                if (toSimilar.getIdPedido() == 0) {
                    toSimilar.setIdPedido(toPed.getReferencia());
                    toSimilar.setIdMovto(toPed.getIdMovto());

                    Pedidos.agregaProductoPedido(cn, toPed, toSimilar);
                }
                toSimilar.setCantOrdenadaSinCargo(toSimilar.getCantOrdenadaSinCargo() + cantidad);
                toSimilar.setCantSinCargo(toSimilar.getCantSinCargo() + cantidad);

                strSQL = "UPDATE pedidosDetalle\n"
                        + "SET cantOrdenadaSinCargo=cantOrdenadaSinCargo+" + cantidad + "\n"
                        + "WHERE idPedido=" + toPed.getReferencia() + " AND idEmpaque=" + toSimilar.getIdProducto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE pedidosDetalle\n"
                        + "SET cantOrdenadaSinCargo=cantOrdenadaSinCargo-" + cantidad + "\n"
                        + "WHERE idPedido=" + toPed.getReferencia() + " AND idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);

                similares = this.obtenSimilares(cn, toProd.getIdMovto(), toProd.getIdProducto());
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.close();
            }
        }
        return similares;
    }

    private String sqlSimilares(int idMovto, int idProducto, int piezas) {
        return "SELECT ISNULL(D.idEnvio, 0) AS idEnvio, ISNULL(D.cantEnviada, 0) AS cantEnviada\n"
                + "     , ISNULL(D.idPedido, 0) AS idPedido, E.piezas\n"
                + "     , ISNULL(D.cantOrdenada, 0) AS cantOrdenada, ISNULL(D.cantOrdenadaSinCargo, 0) AS cantOrdenadaSinCargo\n"
                + "     , ISNULL(D.idMovto, 0) AS idMovto, ISNULL(D.idEmpaque, S.idSimilar) AS idEmpaque\n"
                + "     , ISNULL(D.cantFacturada, 0) AS cantFacturada, ISNULL(D.cantSinCargo, 0) AS cantSinCargo\n"
                + "	, ISNULL(D.costoPromedio, 0) AS costoPromedio, ISNULL(D.costo, 0) AS costo\n"
                + "	, ISNULL(D.desctoProducto1, 0) AS desctoProducto1, ISNULL(D.desctoProducto2, 0) AS desctoProducto2\n"
                + "	, ISNULL(D.desctoConfidencial, 0) AS desctoConfidencial, ISNULL(D.unitario, 0) AS unitario\n"
                + "	, ISNULL(D.idImpuestoGrupo, 0) AS idImpuestoGrupo\n"
                + "	, ISNULL(D.fecha, '1900-01-01') AS fecha, ISNULL(D.existenciaAnterior, 0) AS existenciaAnterior\n"
                + "FROM (" + Pedidos.sqlObtenProducto() + "\n"
                + "         WHERE D.idMovto=" + idMovto + ") D\n"
                + "RIGHT JOIN empaquesSimilares S ON S.idSimilar=D.idEmpaque\n"
                + "INNER JOIN empaques E ON E.idEmpaque=S.idSimilar\n"
                + "WHERE S.idEmpaque=" + idProducto + " AND S.idSimilar!=S.idEmpaque AND E.piezas=" + piezas;
    }

    public ArrayList<TOPedidoProducto> obtenerSimilares(int idMovto, int idProducto, int piezas) throws SQLException {
        ArrayList<TOPedidoProducto> productos = new ArrayList<>();
        String strSQL = this.sqlSimilares(idMovto, idProducto, piezas);
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    productos.add(Pedidos.construirProducto(rs));
                }
            }
        }
        return productos;
    }

    public double obtenerImpuestosProducto(int idMovto, int idEmpaque, ArrayList<ImpuestosProducto> impuestos) throws SQLException {
        double importeImpuestos = 0;
        try (Connection cn = this.ds.getConnection()) {
            importeImpuestos = movimientos.Movimientos.obtenImpuestosProducto(cn, idMovto, idEmpaque, impuestos);
        }
        return importeImpuestos;
    }

    private void actualizaProductoCantidadPedido(Connection cn, TOPedido toPed, TOPedidoProducto toProd) throws SQLException {
        String strSQL;
        ArrayList<Double> boletin = movimientos.Movimientos.obtenerBoletinSinCargo(cn, toPed.getIdEmpresa(), toPed.getIdReferencia(), toPed.getOrdenDeCompraFecha(), toProd.getIdProducto());
        try (Statement st = cn.createStatement()) {
            strSQL = "UPDATE pedidosDetalle\n"
                    + "SET cantOrdenada=" + toProd.getCantOrdenada() + "\n"
                    + "WHERE idPedido=" + toPed.getReferencia() + " AND idEmpaque=" + toProd.getIdProducto();
            st.executeUpdate(strSQL);

            if (boletin.get(0) > 0) {
                strSQL = "SELECT SUM(D.cantOrdenada) AS cantOrdenada, SUM(D.cantOrdenadaSinCargo) AS cantOrdenadaSinCargo\n"
                        + "FROM empaquesSimilares S\n"
                        + "INNER JOIN pedidosDetalle D ON D.idPedido=" + toPed.getReferencia() + " AND D.idEmpaque=S.idSimilar\n"
                        + "WHERE S.idEmpaque=" + toProd.getIdProducto() + "\n"
                        + "GROUP BY S.idEmpaque\n"
                        + "HAVING COUNT(*) > 1";
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    // Si hay mas de un similar en el pedido
                    double cantOrdenada = rs.getDouble("cantOrdenada") / toProd.getPiezas();
                    double cantOrdenadaSinCargo = rs.getDouble("cantOrdenadaSinCargo") / toProd.getPiezas();
                    double cantSimilaresSinCargo = (int) (cantOrdenada / boletin.get(0)) * boletin.get(1);
                    if (cantSimilaresSinCargo > cantOrdenadaSinCargo) {
                        toProd.setCantOrdenadaSinCargo(toProd.getCantOrdenadaSinCargo() + (cantSimilaresSinCargo - cantOrdenadaSinCargo) * toProd.getPiezas());
                        strSQL = "UPDATE pedidosDetalle\n"
                                + "SET cantOrdenadaSinCargo=" + toProd.getCantOrdenadaSinCargo() + "\n"
                                + "WHERE idPedido=" + toPed.getReferencia() + " AND idEmpaque=" + toProd.getIdProducto();
                        st.executeUpdate(strSQL);
                    } else if (cantSimilaresSinCargo < cantOrdenadaSinCargo) {
                        double disponibles;
                        double cantSinCargo;
                        Statement st1 = cn.createStatement();
                        double cantLiberar = cantOrdenadaSinCargo - cantSimilaresSinCargo;
                        strSQL = "SELECT D.*\n"
                                + "FROM empaquesSimilares S\n"
                                + "INNER JOIN pedidosDetalle D ON D.idPedido=" + toPed.getReferencia() + " AND D.idEmpaque=S.idSimilar\n"
                                + "INNER JOIN empaques E ON E.idEmpaque=D.idEmpaque\n"
                                + "WHERE S.idEmpaque=" + toProd.getIdProducto() + " AND E.piezas=" + toProd.getPiezas() + "\n"
                                + "ORDER BY CASE WHEN S.idEmpaque=S.idSimilar THEN 0 ELSE 1 END, D.cantOrdenadaSinCargo DESC";
                        rs = st.executeQuery(strSQL);
                        while (rs.next() && cantLiberar > 0) {
                            cantSinCargo = (int) (rs.getDouble("cantOrdenada") / (toProd.getPiezas() * boletin.get(0))) * boletin.get(1);
                            if (rs.getDouble("cantOrdenadaSinCargo") / toProd.getPiezas() > cantSinCargo) {
                                disponibles = rs.getDouble("cantOrdenadaSinCargo") / toProd.getPiezas() - cantSinCargo;
                                if (disponibles >= cantLiberar) {
                                    disponibles = cantLiberar;
                                }
                                strSQL = "UPDATE pedidosDetalle\n"
                                        + "SET cantOrdenadaSinCargo=cantOrdenadaSinCargo-" + disponibles * toProd.getPiezas() + "\n"
                                        + "WHERE idPedido=" + toPed.getReferencia() + " AND idEmpaque=" + rs.getInt("idEmpaque");
                                st1.executeUpdate(strSQL);
                                cantLiberar -= disponibles;
                            }
                        }
                    }
                } else {
                    // Si es el unico similar en el pedido o si no tiene similares pero tiene boletin
                    toProd.setCantOrdenadaSinCargo((int) (toProd.getCantOrdenada() / (toProd.getPiezas() * boletin.get(0))) * boletin.get(1) * toProd.getPiezas());
                    strSQL = "UPDATE pedidosDetalle\n"
                            + "SET cantOrdenadaSinCargo=" + toProd.getCantOrdenadaSinCargo() + "\n"
                            + "WHERE idPedido=" + toPed.getReferencia() + " AND idEmpaque=" + toProd.getIdProducto();
                    st.executeUpdate(strSQL);
                }
            } else {
                // Si no tiene similares y no tiene boletin
                toProd.setCantOrdenadaSinCargo(0);
                strSQL = "UPDATE pedidosDetalle\n"
                        + "SET cantOrdenadaSinCargo=" + toProd.getCantOrdenadaSinCargo() + "\n"
                        + "WHERE idPedido=" + toPed.getReferencia() + " AND idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);
            }
        }
    }

    private ArrayList<TOPedidoProducto> obtenSimilares(Connection cn, int idMovto, int idProducto) throws SQLException {
        ArrayList<TOPedidoProducto> similares = new ArrayList<>();
        String strSQL = Pedidos.sqlObtenProducto() + "\n"
                + "INNER JOIN empaquesSimilares S ON S.idSimilar=D.idEmpaque\n"
                + "WHERE D.idMovto=" + idMovto + " AND S.idEmpaque=" + idProducto;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                similares.add(Pedidos.construirProducto(rs));
            }
        }
        return similares;
    }

    public ArrayList<TOPedidoProducto> obtenerSimilares(int idMovto, int idProducto) throws SQLException {
        ArrayList<TOPedidoProducto> productos = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            this.obtenSimilares(cn, idMovto, idProducto);
        }
        return productos;
    }

    public void grabarProductoCantidadSinCargo(TOPedido toPed, TOPedidoProducto toProd) throws SQLException {
        String strSQL = "UPDATE pedidosDetalle\n"
                + "SET cantOrdenadaSinCargo=" + toProd.getCantOrdenadaSinCargo() + "\n"
                + "WHERE idPedido=" + toPed.getReferencia() + " AND idEmpaque=" + toProd.getIdProducto();
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);
            }
        }
    }

    public ArrayList<TOPedidoProducto> grabarProductoCantidad(TOPedido toPed, TOPedidoProducto toProd) throws SQLException {
        ArrayList<TOPedidoProducto> similares = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                this.actualizaProductoCantidadPedido(cn, toPed, toProd);
                similares = this.obtenSimilares(cn, toPed.getIdMovto(), toProd.getIdProducto());

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return similares;
    }

//    private void agregaProductoPedido(Connection cn, TOPedido toPed, TOProductoPedido toProd) throws SQLException {
//        String strSQL = "INSERT INTO pedidosDetalle (idPedido, idEmpaque, cantOrdenada, cantOrdenadaSinCargo, cantSurtida, cantSurtidaSinCargo)\n"
//                + "VALUES (" + toProd.getIdPedido() + ", " + toProd.getIdProducto() + ", " + toProd.getCantOrdenada() + ", " + toProd.getCantOrdenadaSinCargo() + ", 0, 0)";
//        try (Statement st = cn.createStatement()) {
//            movimientos.Movimientos.agregaProductoOficina(cn, toProd, toPed.getIdImpuestoZona());
//            movimientos.Movimientos.actualizaProductoPrecio(cn, toPed, toProd);
//
//            st.executeUpdate(strSQL);
//        }
//    }
//
    public void agregarProductoPedido(TOPedido toPed, TOPedidoProducto toProd) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                Pedidos.agregaProductoPedido(cn, toPed, toProd);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

//    public TOProductoPedido construirProducto(ResultSet rs) throws SQLException {
//        TOProductoPedido to = new TOProductoPedido();
//        to.setIdEnvio(rs.getInt("idEnvio"));
//        to.setCantEnviada(rs.getDouble("cantEnviada"));
//        to.setIdPedido(rs.getInt("idPedido"));
//        to.setCantOrdenada(rs.getDouble("cantOrdenada"));
//        to.setCantOrdenadaSinCargo(rs.getDouble("cantOrdenadaSinCargo"));
//        to.setPiezas(rs.getInt("piezas"));
//        movimientos.Movimientos.construirProductoOficina(rs, to);
//        return to;
//    }
//    
    public void actualizarProductoSinCargo(TOPedido toPed, TOPedidoProducto toProd, double cantSeparada) throws SQLException {
        // Solo para capturar la cantidad sin cargo, cuando la venta es con pedido
        double cantSolicitada;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                if (toProd.getCantFacturada() + toProd.getCantSinCargo() > cantSeparada) {
                    cantSolicitada = toProd.getCantFacturada() + toProd.getCantSinCargo() - cantSeparada;
                    try {
                        if (this.idCedis == 1) {
                            Movimientos.separar(cn, toPed.getIdMovto(), toProd.getIdProducto(), cantSolicitada, "cantSinCargo");
                            cantSeparada = cantSolicitada;
                        } else {
                            movimientos.Movimientos.separar(cn, toPed, toProd.getIdProducto(), cantSolicitada, "cantSinCargo");
                        }
                    } catch (Exception ex) {
                        throw new SQLException(ex.getMessage());
                    }
                } else {
                    cantSolicitada = cantSeparada - toProd.getCantFacturada() - toProd.getCantSinCargo();
                    if (this.idCedis == 1) {
                        Movimientos.liberar(cn, toPed.getIdMovto(), toPed.getIdAlmacen(), toProd.getIdProducto(), cantSolicitada, "cantSinCargo");
                    } else {
                        movimientos.Movimientos.liberar(cn, toPed, toProd.getIdProducto(), cantSolicitada, "cantSinCargo");
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

    private void liberaSimilaresSinCargo(Connection cn, TOPedido toPed, int idProducto, double cantSolicitada) throws SQLException {
        double cantLiberar;
        String strSQL = "SELECT M.idAlmacen, D.idEmpaque, D.cantSinCargo\n"
                + "FROM empaquesSimilares S\n"
                + "INNER JOIN movimientosDetalle D ON D.idEmpaque=S.idSimilar\n"
                + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                + "WHERE S.idEmpaque=" + idProducto + " AND D.idMovto=" + toPed.getIdMovto() + " AND D.cantSinCargo > 0\n"
                + "ORDER BY IIF(S.idEmpaque=S.idSimilar, 0, 1)";
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next() && cantSolicitada != 0) {
                cantLiberar = rs.getDouble("cantSinCargo");
                if (cantSolicitada < cantLiberar) {
                    cantLiberar = cantSolicitada;
                }
                Movimientos.liberar(cn, toPed.getIdMovto(), toPed.getIdAlmacen(), idProducto, cantLiberar, "cantSinCargo");
                cantSolicitada -= cantLiberar;
            }
            if (cantSolicitada != 0) {
                throw new SQLException("No se pudo liberar el total de similares !!!");
            }
        }
    }

    private void liberaSimilaresSinCargo(Connection cn, TOPedido toPed, TOPedidoProducto toProd, double cantSolicitada) throws SQLException, Exception {
        double cantLiberada;
        String strSQL = "SELECT D.idEmpaque, D.cantSinCargo, L.lote, L.fechaCaducidad, L.separados\n"
                + "FROM empaquesSimilares S\n"
                + "INNER JOIN movimientosDetalle D ON D.idEmpaque=S.idSimilar\n"
                + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                + "INNER JOIN movimientosDetalleAlmacen DA ON DA.idMovtoAlmacen=M.idMovtoAlmacen AND DA.idEmpaque=D.idEmpaque\n"
                + "INNER JOIN almacenesLotes L ON L.idAlmacen=M.idAlmacen AND L.idEmpaque=DA.idEmpaque AND L.lote=DA.lote\n"
                + "WHERE D.idMovto=" + toPed.getIdMovto() + " AND S.idEmpaque=" + toProd.getIdProducto() + " AND D.cantSinCargo > 0\n"
                + "ORDER BY IIF(S.idEmpaque=S.idSimilar, 0, 1), L.fechaCaducidad DESC";
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next() && cantSolicitada != 0) {
                cantLiberada = movimientos.Movimientos.liberarLote(cn, toPed, rs.getInt("idEmpaque"), rs.getString("lote"), cantSolicitada, "cantSinCargo");
                cantSolicitada -= cantLiberada;
            }
        }
    }

    private void separaSimilaresSinCargo(Connection cn, TOPedido toPed, int idProducto, double cantSolicitada) throws SQLException {
        double disponibles;
        TOPedidoProducto toSimilar = new TOPedidoProducto();
        String strSQL = "SELECT ISNULL(SUM(A.existencia-A.separados), 0) AS disponibles\n"
                + "FROM empaquesSimilares S\n"
                + "RIGHT JOIN almacenesEmpaques A ON A.idEmpaque=S.idSimilar\n"
                + "WHERE A.idAlmacen=" + toPed.getIdAlmacen() + " AND S.idEmpaque=" + idProducto;
        try (Statement st = cn.createStatement(); Statement st1 = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            rs.next();
            if (rs.getDouble("disponibles") < cantSolicitada) {
                throw new SQLException("No hay existencia suficiente. Disponibles=" + rs.getDouble("disponibles"));
            }
            strSQL = "SELECT ISNULL(D.idPedido, 0) AS idPedido, ISNULL(D.idMovto, 0) AS idMovto, E.idEmpaque, P.idImpuesto, ISNULL(A.disponibles, 0) AS disponibles\n"
                    + "FROM empaquesSimilares S\n"
                    + "INNER JOIN empaques E ON E.idEmpaque=S.idSimilar\n"
                    + "INNER JOIN productos P ON P.idProducto=E.idProducto\n"
                    + "LEFT JOIN (SELECT idEmpaque, existencia-separados AS disponibles\n"
                    + "             FROM almacenesEmpaques\n"
                    + "             WHERE idAlmacen=" + toPed.getIdAlmacen() + " AND existencia-separados > 0) A ON S.idSimilar=A.idEmpaque\n"
                    + "LEFT JOIN (SELECT P.idPedido, D.idMovto, D.idEmpaque\n"
                    + "             FROM movimientosDetalle D\n"
                    + "             INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "             INNER JOIN pedidosDetalle P ON P.idPedido=M.referencia AND P.idEmpaque=D.idEmpaque\n"
                    + "             WHERE D.idMovto=" + toPed.getIdMovto() + ") D ON S.idSimilar=D.idEmpaque\n"
                    + "WHERE S.idEmpaque=" + idProducto + " AND ISNULL(A.disponibles, 0) > 0\n"
                    + "ORDER BY IIF(S.idSimilar=S.idEmpaque, 0, 1), ISNULL(D.idMovto, 0) DESC, ISNULL(D.idPedido, 0) DESC";
            rs = st.executeQuery(strSQL);
            while (rs.next() && cantSolicitada > 0) {
                toSimilar.setIdPedido(0);
                toSimilar.setIdMovto(toPed.getIdMovto());
                toSimilar.setIdProducto(rs.getInt("idEmpaque"));
                toSimilar.setCantOrdenada(0);
                toSimilar.setCantOrdenadaSinCargo(0);
                toSimilar.setCantFacturada(0);
                toSimilar.setCantSinCargo(0);
                toSimilar.setCostoPromedio(0);
                toSimilar.setIdImpuestoGrupo(rs.getInt("idImpuesto"));
                disponibles = rs.getDouble("disponibles");
//                if (rs.getInt("idVenta") != 0) {
//                    if (rs.getInt("idMovto") == 0) {
//                        Movimientos.agregarProductoVenta(cn, toPed, toSimilar, toPed.getOrdenDeCompraFecha());
//                    }
//                    strSQL = "UPDATE ventasDetalle\n"
//                            + "SET cantOrdenadaSinCargo=cantOrdenadaSinCargo+" + disponibles + "\n"
//                            + "WHERE idVenta=" + toPed.getReferencia() + " AND idEmpaque=" + idProducto;
//                    st1.executeUpdate(strSQL);
//                } else if (toPed.getIdPedido() != 0) {
//                    if (rs.getInt("idMovto") == 0) {
//                        Pedidos.agregaProductoPedido(cn, toPed, toSimilar);
//                    } else {
//                        Pedidos.agregaProducto(cn, toSimilar);
//                    }
//                    strSQL = "UPDATE ventasDetalle\n"
//                            + "SET cantOrdenadaSinCargo=cantOrdenadaSinCargo+" + disponibles + "\n"
//                            + "WHERE idVenta=" + toPed.getReferencia() + " AND idEmpaque=" + idProducto;
//                    st1.executeUpdate(strSQL);
//                } else if (rs.getInt("idMovto") == 0) {
//                    Movimientos.agregarProductoVenta(cn, toPed, toSimilar, toPed.getOrdenDeCompraFecha());
//                }
                if (rs.getInt("idPedido") == 0) {
                    Pedidos.agregaProductoPedido(cn, toPed, toSimilar);

                    strSQL = "UPDATE pedidosDetalle\n"
                            + "SET cantOrdenadaSinCargo=cantOrdenadaSinCargo+" + disponibles + "\n"
                            + "WHERE idPedido=" + toPed.getReferencia() + " AND idEmpaque=" + idProducto;
                    st1.executeUpdate(strSQL);
                }
                strSQL = "UPDATE almacenesEmpaques\n"
                        + "SET separados=separados+" + disponibles + "\n"
                        + "WHERE idAlmacen=" + toPed.getIdAlmacen() + " AND idEmpaque=" + idProducto;
                st1.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosDetalle\n"
                        + "SET cantSinCargo=cantSinCargo+" + disponibles + "\n"
                        + "WHERE idMovto=" + toPed.getIdMovto() + " AND idEmpaque=" + idProducto;
                st1.executeUpdate(strSQL);

                cantSolicitada -= disponibles;
            }
        }
    }

    private void separaSimilaresSinCargo(Connection cn, TOPedido toPed, TOPedidoProducto toProd, double cantSolicitada) throws SQLException, Exception {
        int idEmpaque;
        ArrayList<String> empaques = new ArrayList<>();
        TOPedidoProducto toSimilar = new TOPedidoProducto();
        // Calcula los disponibles entre todos los similares
        String strSQL = "SELECT ISNULL(SUM(CASE WHEN L.disponibles <= E.existencia-E.separados THEN L.disponibles\n"
                + "			ELSE E.existencia-E.separados END), 0) AS disponibles\n"
                + "FROM (SELECT L.idEmpaque, SUM(L.existencia-L.separados) AS disponibles\n"
                + "	  FROM empaquesSimilares S\n"
                + "	  INNER JOIN almacenesLotes L ON L.idAlmacen=" + toPed.getIdAlmacen() + " AND L.idEmpaque=S.idSimilar\n"
                + "	  WHERE S.idEmpaque=" + toProd.getIdProducto() + "\n"
                + "	  GROUP BY L.idEmpaque) L\n"
                + "INNER JOIN almacenesEmpaques E ON E.idEmpaque=L.idEmpaque\n"
                + "WHERE E.idAlmacen=" + toPed.getIdAlmacen();
        try (Statement st = cn.createStatement(); Statement st1 = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                // Calculo disponibles (minimo disponible entre almacen y oficina) de los empaques similares
                if (rs.getDouble("disponibles") < cantSolicitada) {
                    // Si no hay suficientes disponibles se aborta la transaccion
                    throw new Exception("No hay existencia suficiente entre similares !!!");
                }
            }
            // Obteniendo todos los lotes en empaques similares con disponibles
            strSQL = "SELECT ISNULL(D.idPedido, 0) AS idPedido, ISNULL(D.idMovto, 0) AS idMovto, L.idEmpaque, L.lote, P.idImpuesto\n"
                    + "FROM (SELECT M.idMovto, M.referencia AS idPedido, D.idEmpaque\n"
                    + "      FROM movimientosDetalle D\n"
                    + "      INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "      WHERE D.idMovto=" + toPed.getIdMovto() + ") D\n"
                    + "RIGHT JOIN empaquesSimilares S ON S.idSimilar=D.idEmpaque\n"
                    + "INNER JOIN almacenesLotes L ON L.idAlmacen=" + toPed.getIdAlmacen() + " AND L.idEmpaque=S.idSimilar\n"
                    + "INNER JOIN empaques E ON E.idEmpaque=L.idEmpaque\n"
                    + "INNER JOIN productos P ON P.idProducto=E.idProducto\n"
                    + "WHERE S.idEmpaque=" + toProd.getIdProducto() + " AND L.existencia-L.separados > 0\n"
                    + "ORDER BY IIF(S.idSimilar=S.idEmpaque, 0, 1), ISNULL(D.idPedido, 0) DESC, ISNULL(D.idMovto, 0) DESC, L.idEmpaque, L.fechaCaducidad";
            rs = st.executeQuery(strSQL);
            while (rs.next() && cantSolicitada != 0) {
                // Para todos los lotes con existencia disponible en los empaques similares
                // Sin importar si el empaque esta o no en el movimiento
                idEmpaque = rs.getInt("idEmpaque");
                if (rs.getInt("idMovto") == 0 && empaques.indexOf(String.valueOf(idEmpaque)) == -1) {
                    toSimilar.setIdPedido(0);
                    toSimilar.setIdMovto(toPed.getIdMovto());
                    toSimilar.setIdProducto(idEmpaque);
                    toSimilar.setCantOrdenada(0);
                    toSimilar.setCantOrdenadaSinCargo(0);
                    toSimilar.setCantFacturada(0);
                    toSimilar.setCantSinCargo(0);
                    toSimilar.setCostoPromedio(0);
                    toSimilar.setIdImpuestoGrupo(rs.getInt("idImpuesto"));
                    Pedidos.agregaProductoPedido(cn, toPed, toSimilar);
                    empaques.add(String.valueOf(idEmpaque));
                } else {
                    strSQL = "UPDATE pedidosDetalle\n"
                            + "SET cantOrdenadaSinCargo=cantOrdenadaSinCargo+disponibles\n"
                            + "WHERE idPedido=" + toPed.getReferencia() + " AND idEmpaque=" + idEmpaque;
                    st1.executeUpdate(strSQL);
                }
                cantSolicitada -= movimientos.Movimientos.separarLote(cn, toPed, idEmpaque, rs.getString("lote"), cantSolicitada, "cantSinCargo");
            }
        }
    }

    private ArrayList<TOPedidoProducto> actualizaProductoCantidad(Connection cn, TOPedido toPed, TOPedidoProducto toProd, double separados) throws SQLException, Exception {
        // Con la cantidad facturada checa los boletines y ajusta la cantidad sin cargo en su caso
        String strSQL;
        double cantSolicitada, cantSeparada;
        double cantFacturadaOrig = separados - toProd.getCantSinCargo();
        double cantSinCargoOrig = toProd.getCantSinCargo();
        ArrayList<TOPedidoProducto> similares = new ArrayList<>();
        if (toProd.getCantFacturada() > cantFacturadaOrig) {
            cantSolicitada = toProd.getCantFacturada() - cantFacturadaOrig;
            if (this.idCedis == 1) {
                Movimientos.separar(cn, toPed.getIdMovto(), toProd.getIdProducto(), cantSolicitada, "cantFacturada");
                cantSeparada = cantSolicitada;
            } else {
                cantSeparada = movimientos.Movimientos.separar(cn, toPed, toProd.getIdProducto(), cantSolicitada, "cantFacturada");
            }
            toProd.setCantFacturada(cantFacturadaOrig + cantSeparada);
        } else if (toProd.getCantFacturada() < cantFacturadaOrig) {
            cantSolicitada = cantFacturadaOrig - toProd.getCantFacturada();
            if (this.idCedis == 1) {
                Movimientos.liberar(cn, toPed.getIdMovto(), toPed.getIdAlmacen(), toProd.getIdProducto(), cantSolicitada, "cantFacturada");
            } else {
                movimientos.Movimientos.liberar(cn, toPed, toProd.getIdProducto(), cantSolicitada, "cantFacturada");
            }
            toProd.setCantFacturada(cantFacturadaOrig - cantSolicitada);
        }
        ArrayList<Double> boletin;
        if (toPed.getReferencia() != 0) {
            boletin = new ArrayList<>();
            boletin.add(1.0);
            boletin.add(0.0);
        } else {
            boletin = movimientos.Movimientos.obtenerBoletinSinCargo(cn, toPed.getIdEmpresa(), toPed.getIdReferencia(), toPed.getOrdenDeCompraFecha(), toProd.getIdProducto());
            try (Statement st = cn.createStatement()) {
                // Necesito saber cuantos sin cargo tengo y debo tener entre los similares
                // Aqui obtengo la suma de cantFacturada de todos los similares y del producto original
                strSQL = "SELECT SUM(D.cantFacturada) AS cantFacturada, SUM(D.cantSinCargo) AS cantSinCargo\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN empaquesSimilares S ON S.idSimilar=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toPed.getIdMovto() + " AND S.idEmpaque=" + toProd.getIdProducto() + "\n"
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
                                if (this.idCedis == 1) {
                                    this.separaSimilaresSinCargo(cn, toPed, toProd.getIdProducto(), cantSolicitada);
                                } else {
                                    this.separaSimilaresSinCargo(cn, toPed, toProd, cantSolicitada);
                                }
                            } else if (cantSinCargoOrig < rs.getDouble("cantSinCargo")) {
                                cantSolicitada = rs.getDouble("cantSinCargo") - cantSinCargoOrig;
                                if (this.idCedis == 1) {
                                    this.liberaSimilaresSinCargo(cn, toPed, toProd.getIdProducto(), cantSolicitada);
                                } else {
                                    this.liberaSimilaresSinCargo(cn, toPed, toProd, cantSolicitada);
                                }
                            }
                            similares = this.obtenSimilares(cn, toPed.getIdMovto(), toProd.getIdProducto());
                            toProd.setCantSinCargo(similares.get(0).getCantSinCargo());
                            similares.remove(0);
                        }
                    } else {
                        // Si no hay similares, atiende solamente las piezas sin cargo del producto en cuestion
                        toProd.setCantSinCargo((int) (toProd.getCantFacturada() / boletin.get(0)) * boletin.get(1));
                        if (toProd.getCantSinCargo() > cantSinCargoOrig) {
                            cantSolicitada = toProd.getCantSinCargo() - cantSinCargoOrig;
                            if (this.idCedis == 1) {
                                Movimientos.separar(cn, toPed.getIdMovto(), toProd.getIdProducto(), cantSolicitada, "cantSinCargo");
                                cantSeparada = cantSolicitada;
                            } else {
                                cantSeparada = movimientos.Movimientos.separar(cn, toPed, toProd.getIdProducto(), cantSolicitada, "cantSinCargo");
                            }
                            toProd.setCantSinCargo(cantSinCargoOrig + cantSeparada);
                        } else if (toProd.getCantSinCargo() < cantSinCargoOrig) {
                            cantSolicitada = cantSinCargoOrig - toProd.getCantSinCargo();
                            if (this.idCedis == 1) {
                                Movimientos.liberar(cn, toPed.getIdMovto(), toPed.getIdAlmacen(), toProd.getIdProducto(), cantSolicitada, "cantSinCargo");
                            } else {
                                movimientos.Movimientos.liberar(cn, toPed, toProd.getIdProducto(), cantSolicitada, "cantSinCargo");
                            }
                            toProd.setCantSinCargo(cantSinCargoOrig - cantSolicitada);
                        }
                    }
                } else if (rs.next()) {
                    cantSolicitada = rs.getDouble("cantSinCargo");
                    if (cantSolicitada > 0) {
                        if (this.idCedis == 1) {
                            this.liberaSimilaresSinCargo(cn, toPed, toProd.getIdProducto(), cantSolicitada);
                        } else {
                            this.liberaSimilaresSinCargo(cn, toPed, toProd, cantSolicitada);
                        }
                        similares = this.obtenSimilares(cn, toPed.getIdMovto(), toProd.getIdProducto());
                        toProd.setCantSinCargo(similares.get(0).getCantSinCargo());
                        similares.remove(0);
                    }
                } else if (cantSinCargoOrig > 0) {
                    if (this.idCedis == 1) {
                        Movimientos.liberar(cn, toPed.getIdMovto(), toPed.getIdAlmacen(), toProd.getIdProducto(), cantSinCargoOrig, "cantSinCargo");
                    } else {
                        movimientos.Movimientos.liberar(cn, toPed, toProd.getIdProducto(), cantSinCargoOrig, "cantSinCargo");
                    }
                    toProd.setCantSinCargo(0);
                }
            }
        }
        return similares;
    }

    public ArrayList<TOPedidoProducto> actualizarProductoCantidad(TOPedido toPed, TOPedidoProducto toProd, double cantSeparada) throws SQLException, Exception {
        // Cuando esto capturando en pantalla de edicion, en una venta nueva
        ArrayList<TOPedidoProducto> similares = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                similares = this.actualizaProductoCantidad(cn, toPed, toProd, cantSeparada);
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

    public TOPedidoProducto obtenerProductoOficina(Connection cn, int idMovto, int idProducto) throws SQLException {
        TOPedidoProducto toProd = null;
        String strSQL = Pedidos.sqlObtenProducto() + "\n"
                + "WHERE D.idMovto=" + idMovto + " AND D.idEmpaque=" + idProducto;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                toProd = Pedidos.construirProducto(rs);
            }
        }
        return toProd;
    }

    private ArrayList<TOPedidoProducto> obtenDetalleOficina(Connection cn, TOPedido toPed) throws SQLException {
        ArrayList<TOPedidoProducto> detalle = new ArrayList<>();
        String strSQL = Pedidos.sqlObtenProducto() + "\n"
                + "WHERE D.idMovto=" + toPed.getIdMovto();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                detalle.add(Pedidos.construirProducto(rs));
            }
            movimientos.Movimientos.bloquearMovimientoOficina(cn, toPed, this.idUsuario);
        }
        return detalle;
    }

    public ArrayList<TOPedidoProducto> obtenerDetalleOficina(TOPedido toPed) throws SQLException {
        // Al cargar una venta no cerrada, actualiza precios y verifica boletines de productos
        String aviso = "";
        String strSQL;
        double separados;
        ArrayList<TOPedidoProducto> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                detalle = this.obtenDetalleOficina(cn, toPed);
                if (toPed.getIdUsuario() == toPed.getPropietario()) {
                    if (toPed.getReferencia() != 0) {
                        if (toPed.getPedidoEstatus() == 0) {
                            movimientos.Movimientos.liberarMovimientoOficina(cn, toPed.getIdMovto(), this.idUsuario);
                            toPed.setPropietario(0);
//                        } else if (toPed.getDirecto() != 0) {
//                            try {
//                                Envios.obtenerEstatusTraspaso(cn, toPed.getIdSolicitud());
//                            } catch (Exception ex) {
//                                movimientos.Movimientos.liberarMovimientoOficina(cn, toPed.getIdMovto(), this.idUsuario);
//                                toPed.setPropietario(0);
//                            }
                        } else {
                            if (toPed.getDirecto() != 0) {
                                try {
                                    Envios.obtenerEstatusTraspaso(cn, toPed.getIdSolicitud());
                                } catch (Exception ex) {
                                    movimientos.Movimientos.liberarMovimientoOficina(cn, toPed.getIdMovto(), this.idUsuario);
                                    toPed.setPropietario(0);
                                }
                            }
                            strSQL = "SELECT estatus FROM pedidos WHERE idPedido=" + toPed.getReferencia();
                            ResultSet rs = st.executeQuery(strSQL);
                            if (rs.next()) {
                                toPed.setPedidoEstatus(rs.getInt("estatus"));
                                if (toPed.getPedidoEstatus() == 1) {
                                    toPed.setPedidoEstatus(3);
                                    strSQL = "UPDATE pedidos SET estatus=" + toPed.getPedidoEstatus() + " WHERE idPedido=" + toPed.getReferencia();
                                    st.executeUpdate(strSQL);
//                                } else {
//                                    toPed.setPropietario(0);
                                }
                            } else {
                                throw new SQLException("No se encontró el pedido !!!");
                            }
                        }
                    } else if (toPed.getEstatus() == 0) {
                        TOPedidoProducto toProd;
                        for (TOPedidoProducto to : detalle) {
                            toProd = this.obtenerProductoOficina(cn, toPed.getIdMovto(), to.getIdProducto());
                            movimientos.Movimientos.actualizaProductoPrecio(cn, toPed, toProd, toPed.getOrdenDeCompraFecha());
                            try {
                                separados = toProd.getCantFacturada() + toProd.getCantSinCargo();
                                this.actualizaProductoCantidad(cn, toPed, toProd, separados);
                            } catch (Exception ex) {
                                if (!aviso.isEmpty()) {
                                    aviso += ", ";
                                }
                                aviso += toProd.getCod_pro();
                            }
                        }
                        detalle = this.obtenDetalleOficina(cn, toPed);
                        if (!aviso.isEmpty()) {
                            movimientos.Movimientos.liberarMovimientoOficina(cn, toPed.getIdMovto(), this.idUsuario);
                            toPed.setPropietario(0);
                        }
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

    private ArrayList<TOPedidoProducto> obtenDetalle(Connection cn, TOPedido toPed) throws SQLException {
        ArrayList<TOPedidoProducto> detalle = new ArrayList<>();
        String strSQL = Pedidos.sqlObtenProducto() + "\n"
                + "WHERE D.idMovto=" + toPed.getIdMovto();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                detalle.add(Pedidos.construirProducto(rs));
            }
            movimientos.Movimientos.bloquearMovimientoOficina(cn, toPed, this.idUsuario);
        }
        return detalle;
    }

    public ArrayList<TOPedidoProducto> obtenerDetallePedido(TOPedido toPed) throws SQLException {
        ArrayList<TOPedidoProducto> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                detalle = this.obtenDetalle(cn, toPed);
                if (toPed.getIdUsuario() == toPed.getPropietario() && toPed.getEstatus() == 0 && toPed.getEspecial() == 0) {
                    for (TOPedidoProducto toProd : detalle) {
                        movimientos.Movimientos.actualizaProductoPrecio(cn, toPed, toProd, toPed.getOrdenDeCompraFecha());
                        this.actualizaProductoCantidadPedido(cn, toPed, toProd);
                    }
                }
                cn.commit();
            } catch (SQLException ex) {
                toPed.setPropietario(0);
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return detalle;
    }

    public void agregarPedido(TOPedido toPed, int idMoneda) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                toPed.setEstatus(0);
                toPed.setIdUsuario(this.idUsuario);
                toPed.setPropietario(this.idUsuario);
                toPed.setPedidoIdUsuario(this.idUsuario);

                Pedidos.agregarPedidoConOrden(cn, toPed, idMoneda);

                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw (e);
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void agregarVenta(TOPedido toPed, int idMoneda) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                toPed.setEstatus(0);
                toPed.setIdUsuario(this.idUsuario);
                toPed.setPropietario(this.idUsuario);
                toPed.setIdUsuario(this.idUsuario);

                Pedidos.agregarPedido(cn, toPed, idMoneda);

                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw (e);
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

//    private TOPedido construirPedido(ResultSet rs) throws SQLException {
//        TOPedido to = new TOPedido();
//        Pedidos.construyePedido(to, rs);
//        return to;
//    }
//
    public ArrayList<TOPedido> obtenerPedidos(int idAlmacen) throws SQLException {
        ArrayList<TOPedido> ventas = new ArrayList<>();
        String strSQL = "SELECT M.*, P.idPedidoOC, P.folio AS pedidoFolio, P.fecha AS pedidoFecha, P.diasCredito, P.especial, P.canceladoMotivo, P.canceladoFecha\n"
                + "     , P.directo, P.idEnvio, P.peso, P.orden, P.estatus AS pedidoEstatus, ISNULL(V.estatus, 0) AS envioEstatus, T.idImpuestoZona\n"
                + "     , ISNULL(OC.electronico, '') AS electronico, ISNULL(OC.ordenDeCompra, '') AS ordenDeCompra, ISNULL(OC.ordenDeCompraFecha, '1900-01-01') AS ordenDeCompraFecha\n"
                + "FROM movimientos M\n"
                + "RIGHT JOIN pedidos P ON P.idPedido=M.referencia\n"
                + "INNER JOIN clientesTiendas T ON T.idTienda=M.idReferencia\n"
                + "LEFT JOIN pedidosOC ON OC.idPedidoOC=P.idPedidoOC\n"
                + "LEFT JOIN envios V ON V.idEnvio=P.idEnvio\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=28 AND M.estatus=0 AND P.estatus=1\n"
                + "ORDER BY P.fecha DESC";
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                ventas.add(Pedidos.construirPedido(rs));
            }
        } finally {
            cn.close();
        }
        return ventas;
    }

    public ArrayList<TOPedido> obtenerPedidos(int idAlmacen, int estatus, Date fechaInicial, boolean isVenta) throws SQLException {
        String condicion = "";
        if (isVenta) {
            if (estatus != 0) {
                condicion += "M.estatus>=5";
            } else {
                condicion += "((M.referencia=0 OR P.estatus>=1) AND M.estatus=0)";
            }
        } else if (estatus != 0) {
            condicion = "M.referencia!=0 AND P.estatus>=1";
        } else {
            condicion += "M.referencia!=0 AND P.estatus=0";
        }
        ArrayList<TOPedido> pedidos = new ArrayList<>();
        String strSQL = "SELECT " + Pedidos.sqlPedidos() + "\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=28 AND " + condicion + "\n";
        if (estatus != 0) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            strSQL += "                 AND CONVERT(date, M.fecha) >= '" + format.format(fechaInicial) + "'\n";
        }
        strSQL += "ORDER BY M.fecha";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    pedidos.add(Pedidos.construirPedido(rs));
                }
            }
        }
        return pedidos;
    }
}
