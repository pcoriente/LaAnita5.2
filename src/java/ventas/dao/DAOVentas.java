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
import java.util.logging.Level;
import java.util.logging.Logger;
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

    public void liberarVenta(int idMovto) throws SQLException, Exception {
        String strSQL = "SELECT propietario FROM movimientos WHERE idMovto=" + idMovto;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    if (rs.getInt("propietario") == this.idUsuario) {
                        strSQL = "UPDATE movimientos SET propietario=0 WHERE idMovto=" + idMovto;
                        st.executeUpdate(strSQL);
                    }
                } else {
                    throw new SQLException("No se encontro el movimiento !!!");
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

    public ArrayList<TOVentaProducto> surtirFincado(TOVenta toMov) throws SQLException, Exception {
        // Intenta surtir en automatico TODO el pedido fincado
        double cantSolicitada;
        ArrayList<TOVentaProducto> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                detalle = this.obtenDetalle(cn, toMov);
                if (toMov.getIdUsuario() == toMov.getPropietario() && toMov.getEstatus() == 5) {
                    for (TOVentaProducto to : detalle) {
                        if (to.getCantFacturada() + to.getCantSinCargo() < to.getCantOrdenada() + to.getCantOrdenadaSinCargo()) {
                            cantSolicitada = to.getCantOrdenada() + to.getCantOrdenadaSinCargo() - (to.getCantFacturada() + to.getCantSinCargo());
                            try {
                                movimientos.Movimientos.separar(cn, toMov, to.getIdProducto(), cantSolicitada, true);
                                to.setCantFacturada(to.getCantOrdenada());
                                to.setCantSinCargo(to.getCantOrdenadaSinCargo());
                            } catch (Exception ex) {
                                // Si no se pudieron surtir todos los solicitados, pasa al siguiete producto
                                Logger.getLogger(DAOVentas.class.getName()).log(Level.SEVERE, null, ex);
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

    public void eliminarVenta(TOVenta toMov) throws SQLException {
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
                
                strSQL="DELETE FROM comprobantes WHERE idComproobante="+toMov.getIdComprobante();
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

    public void cerrarVenta(TOVenta toMov) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                toMov.setIdUsuario(this.idUsuario);
                toMov.setPropietario(0);
                toMov.setEstatus(7);

                toMov.setFolio(movimientos.Movimientos.obtenMovimientoFolioAlmacen(cn, toMov.getIdAlmacen(), toMov.getIdTipo()));
                movimientos.Movimientos.grabaMovimientoAlmacen(cn, toMov);

                toMov.setFolio(movimientos.Movimientos.obtenMovimientoFolioOficina(cn, toMov.getIdAlmacen(), toMov.getIdTipo()));
                movimientos.Movimientos.grabaMovimientoOficina(cn, toMov);

                strSQL = "UPDATE comprobantes\n"
                        + "SET tipo='2', numero='" + String.valueOf(toMov.getFolio()) + "'\n"
                        + "WHERE idComprobante=" + toMov.getIdComprobante();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE D\n"
                        + "SET fecha=GETDATE(), existenciaAnterior=L.existencia\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes L ON L.idAlmacen=M.idAlmacen AND L.idEmpaque=D.idEmpaque AND L.lote=D.lote\n"
                        + "WHERE D.idMovtoAlmacen=" + toMov.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE L\n"
                        + "SET existencia=L.existencia-D.cantidad, separados=L.separados-D.cantidad\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes L ON L.idAlmacen=M.idAlmacen AND L.idEmpaque=D.idEmpaque AND L.lote=D.lote\n"
                        + "WHERE D.idMovtoAlmacen=" + toMov.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE D\n"
                        + "SET fecha=GETDATE(), existenciaAnterior=E.existencia\n"
                        + "     , costoPromedio=EE.costoUnitarioPromedio, costo=EE.costoUnitarioPromedio, unitario=EE.costoUnitarioPromedio\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN almacenesEmpaques E ON E.idAlmacen=M.idAlmacen AND E.idEmpaque=D.idEmpaque\n"
                        + "INNER JOIN empresasEmpaques EE ON EE.idEmpresa=M.idEmpresa AND EE.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toMov.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE A\n"
                        + "SET existencia=A.existencia-D.cantFacturada, separados=A.separados-D.cantFacturada\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toMov.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE E\n"
                        + "SET existencia=E.existencia-D.cantFacturada\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toMov.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE E\n"
                        + "SET costoUnitarioPromedio=0\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toMov.getIdMovto() + " AND E.existencia=0";
                st.executeUpdate(strSQL);

                strSQL = "UPDATE PD\n"
                        + "SET cantSurtida=PD.cantSurtida+D.cantFacturada, cantSurtidaSinCargo=PD.cantSurtidaSinCargo+D.cantSinCargo\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN pedidosDetalle PD ON PD.idPedido=M.referencia AND PD.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toMov.getIdMovto();
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

    public ArrayList<TOVentaProducto> obtenerDetalleFincado(TOVenta toMov) throws SQLException {
        // Al cargar un pedido fincado no se modifica ni precios ni cantidades separadas
        ArrayList<TOVentaProducto> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                detalle = this.obtenDetalle(cn, toMov);
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return detalle;
    }

    public TOVentaProducto construirProducto(ResultSet rs) throws SQLException {
        TOVentaProducto to = new TOVentaProducto();
        to.setIdPedido(rs.getInt("idPedido"));
        to.setCantOrdenada(rs.getDouble("cantOrdenada"));
        to.setCantOrdenadaSinCargo(rs.getDouble("cantOrdenadaSinCargo"));
        movimientos.Movimientos.construirProductoOficina(rs, to);
        return to;
    }

    private ArrayList<TOVentaProducto> obtenDetalle(Connection cn, TOVenta toMov) throws SQLException {
        String strSQL;
        int propietario = 0;
        ArrayList<TOVentaProducto> detalle = new ArrayList<>();
//        try (Connection cn = this.ds.getConnection()) {
//            cn.setAutoCommit(false);
        try (Statement st = cn.createStatement()) {
            strSQL = "SELECT MD.*, M.referencia AS idPedido\n"
                    + "         , ISNULL(PD.cantOrdenada-PD.cantSurtida, 0) AS cantOrdenada"
                    + "         , ISNULL(PD.cantOrdenadaSinCargo-cantSurtidaSinCargo, 0) AS cantOrdenadaSinCargo\n"
                    + "FROM movimientosDetalle MD\n"
                    + "INNER JOIN movimientos M ON M.idMovto=MD.idMovto\n"
                    + "LEFT JOIN pedidosDetalle PD ON PD.idPedido=M.referencia AND PD.idEmpaque=MD.idEmpaque\n"
                    + "WHERE MD.idMovto=" + toMov.getIdMovto();
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                detalle.add(this.construirProducto(rs));
            }
            strSQL = "SELECT propietario, estatus FROM movimientos WHERE idMovto=" + toMov.getIdMovto();
            rs = st.executeQuery(strSQL);
            if (rs.next()) {
                toMov.setEstatus(rs.getInt("estatus"));
                propietario = rs.getInt("propietario");
                if (propietario == 0) {
                    strSQL = "UPDATE movimientos SET propietario=" + this.idUsuario + "\n"
                            + "WHERE idMovto=" + toMov.getIdMovto();
                    st.executeUpdate(strSQL);
                    toMov.setPropietario(this.idUsuario);
                } else {
                    toMov.setPropietario(propietario);
                }
                toMov.setIdUsuario(this.idUsuario);
            } else {
                throw new SQLException("No se encontro el movimiento !!!");
            }
//                cn.commit();
//            } catch (SQLException ex) {
//                cn.rollback();
//                throw ex;
//            } finally {
//                cn.setAutoCommit(true);
        }
//        }
        return detalle;
    }

    public ArrayList<TOVentaProducto> obtenerDetalleVenta(TOVenta toMov) throws SQLException {
        // Al cargar una venta no cerrada, actualiza precios y verifica boletines de productos
        double cantSeparada;
        ArrayList<TOVentaProducto> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                detalle = this.obtenDetalle(cn, toMov);
                if (toMov.getIdUsuario() == toMov.getPropietario() && toMov.getEstatus() == 0) {
                    for (TOVentaProducto toProd : detalle) {
                        movimientos.Movimientos.actualizaProductoPrecio(cn, toMov, toProd);
                        cantSeparada = toProd.getCantFacturada() + toProd.getCantSinCargo();
                        this.actualizaProductoCantidad(cn, toMov, toProd, cantSeparada, true);
                    }
                }
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return detalle;
    }

    public void actualizarProductoSinCargo(TOVenta toMov, TOVentaProducto toProd, double cantSeparada) throws SQLException {
        // Cuando esto capturando en pantalla de edicion, en una venta nueva
        double cantSolicitada;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                if (toProd.getCantFacturada() + toProd.getCantSinCargo() > cantSeparada) {
                    cantSolicitada = toProd.getCantFacturada() + toProd.getCantSinCargo() - cantSeparada;
                    try {
                        movimientos.Movimientos.separar(cn, toMov, toProd.getIdProducto(), cantSolicitada, true);
                    } catch (Exception ex) {
                        throw new SQLException(ex.getMessage());
                    }
                } else {
                    cantSolicitada = cantSeparada - toProd.getCantFacturada() - toProd.getCantSinCargo();
                    movimientos.Movimientos.liberar(cn, toMov, toProd.getIdProducto(), cantSolicitada);
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

    private void actualizaProductoCantidad(Connection cn, TOVenta toMov, TOVentaProducto toProd, double cantSeparada, boolean reintentar) throws SQLException {
        // Con la cantidad facturada checa los boletines y ajusta la cantidad sin cargo en su caso
        boolean repetir;
        double cantSolicitada;
        ArrayList<Double> boletin;
        if (toMov.getReferencia() != 0) {
            boletin = new ArrayList<>();
            boletin.add(1.0);
            boletin.add(0.0);
        } else {
            boletin = movimientos.Movimientos.obtenerBoletinSinCargo(cn, toMov.getIdEmpresa(), toMov.getIdReferencia(), toProd.getIdProducto());
            if (boletin.get(0) > 0) {
                toProd.setCantSinCargo((int) (toProd.getCantFacturada() / boletin.get(0)) * boletin.get(1));
            } else {
                toProd.setCantSinCargo(0);
            }
        }
        if (toProd.getCantFacturada() + toProd.getCantSinCargo() > cantSeparada) {
            do {
                cantSolicitada = toProd.getCantFacturada() + toProd.getCantSinCargo() - cantSeparada;
                try {
                    if (cantSolicitada != 0) {
                        if (cantSolicitada > 0) {
                            movimientos.Movimientos.separar(cn, toMov, toProd.getIdProducto(), cantSolicitada, true);
                        } else {
                            cantSolicitada = cantSeparada - toProd.getCantFacturada() - toProd.getCantSinCargo();
                            movimientos.Movimientos.liberar(cn, toMov, toProd.getIdProducto(), cantSolicitada);
                        }
                    }
                    repetir = false;
                } catch (Exception ex) {
                    if (!reintentar) {
                        repetir = false;
                        throw new SQLException(ex.getMessage());
                    } else if (toProd.getCantFacturada() != 0) {
                        repetir = true;
                        toProd.setCantFacturada(toProd.getCantFacturada() - 1);
                        toProd.setCantSinCargo((int) (toProd.getCantFacturada() / boletin.get(0)) * boletin.get(1));
                    } else {
                        repetir = false;
                    }
                }
            } while (repetir);
        } else {
            cantSolicitada = cantSeparada - toProd.getCantFacturada() - toProd.getCantSinCargo();
            movimientos.Movimientos.liberar(cn, toMov, toProd.getIdProducto(), cantSolicitada);
        }
//     return this.obtenLotes(cn, toMov.getIdMovtoAlmacen(), toProd.getIdProducto());
    }

    public void actualizarProductoCantidad(TOVenta toMov, TOVentaProducto toProd, double cantSeparada) throws SQLException {
        // Cuando esto capturando en pantalla de edicion, en una venta nueva
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                this.actualizaProductoCantidad(cn, toMov, toProd, cantSeparada, false);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public double obtenerImpuestosProducto(int idMovto, int idEmpaque, ArrayList<ImpuestosProducto> impuestos) throws SQLException {
        double importeImpuestos = 0;
        try (Connection cn = this.ds.getConnection()) {
            importeImpuestos = movimientos.Movimientos.obtenImpuestosProducto(cn, idMovto, idEmpaque, impuestos);
        }
        return importeImpuestos;
    }

    public void agregarProducto(TOVenta toMov, TOVentaProducto toProd) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                movimientos.Movimientos.agregaProductoOficina(cn, toProd, toMov.getIdImpuestoZona());
                movimientos.Movimientos.actualizaProductoPrecio(cn, toMov, toProd);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
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
                // Obtiene el movimiento original (el primer idMovto), del pedido para con este crear el nuevo
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

                detalle = this.obtenDetalle(cn, toMov);

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

    public void agregarVenta(TOVenta toMov, int idMoneda) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                TOComprobante to = new TOComprobante(toMov.getIdTipo(), toMov.getIdEmpresa(), toMov.getIdReferencia(), idMoneda);
                to.setTipo('1');
                to.setNumero(String.valueOf(toMov.getReferencia()));
                to.setIdUsuario(toMov.getIdUsuario());
                to.setPropietario(0);
                comprobantes.Comprobantes.agregar(cn, to);

                toMov.setIdComprobante(to.getIdComprobante());
                movimientos.Movimientos.agregaMovimientoAlmacen(cn, toMov, false);
                movimientos.Movimientos.agregaMovimientoOficina(cn, toMov, false);
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw (e);
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

//    public TOVenta obtenerVenta(int idMovto) throws SQLException {
//        TOVenta toMov = null;
//        String strSQL = "SELECT M.*\n"
//                + "     , ISNULL(P.idPedidoOC, 0) AS idPedidoOC, ISNULL(P.canceladoFecha, '1900-01-01') AS canceladoFecha, ISNULL(P.canceladoMotivo, '') AS canceladoMotivo\n"
//                + "     , ISNULL(OC.ordenDeCompra, '') AS ordenDeCompra, ISNULL(OC.ordenDeCompraFecha, '1900-01-01') AS ordenDeCompraFecha\n"
//                + "FROM movimientos M\n"
//                + "LEFT JOIN pedidos P ON P.idPedido=M.referencia\n"
//                + "LEFT JOIN pedidosOC OC ON OC.idPedidoOC=P.idPedidoOC\n"
//                + "WHERE M.idMovto=" + idMovto;
//        Connection cn = this.ds.getConnection();
//        try (Statement st = cn.createStatement()) {
//            ResultSet rs = st.executeQuery(strSQL);
//            if (rs.next()) {
//                toMov = this.construir(rs);
//            }
//        } finally {
//            cn.close();
//        }
//        return toMov;
//    }
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

    private void construir(ResultSet rs, TOVenta toMov) throws SQLException {
        toMov.setIdPedidoOC(rs.getInt("idPedidoOC"));
        toMov.setIdMoneda(rs.getInt("idMoneda"));
        toMov.setOrdenDeCompra(rs.getString("ordenDeCompra"));
        toMov.setOrdenDeCompraFecha(new java.util.Date(rs.getTimestamp("ordenDeCompraFecha").getTime()));
        toMov.setCanceladoMotivo(rs.getString("canceladoMotivo"));
        toMov.setCanceladoFecha(new java.util.Date(rs.getDate("canceladoFecha").getTime()));
        movimientos.Movimientos.construirMovimientoOficina(rs, toMov);
    }

    private TOVenta construir(ResultSet rs) throws SQLException {
        TOVenta toMov = new TOVenta(28);
        this.construir(rs, toMov);
        return toMov;
    }

    public ArrayList<TOVenta> obtenerVentas(int idAlmacen, int estatus, Date fechaInicial) throws SQLException {
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
