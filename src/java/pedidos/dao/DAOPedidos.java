package pedidos.dao;

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
import pedidos.to.TOPedido;
import pedidos.to.TOProductoPedido;
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

    public void cancelarPedido(TOPedido toPed) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                toPed.setIdUsuario(this.idUsuario);
                toPed.setPropietario(0);
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
                        + "SET fecha=GETDATE(), idUsuario=" + this.idUsuario + ", propietario=0, estatus=6\n"
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
                        + "SET fecha=GETDATE(), idUsuario=" + this.idUsuario + ", propietario=0, estatus=6\n"
                        + "WHERE idMovto=" + toPed.getIdMovto();
                st.executeUpdate(strSQL);

                java.sql.Date fechaCancelacion = new java.sql.Date(toPed.getCanceladoFecha().getTime());
                strSQL = "UPDATE pedidos\n"
                        + "SET fecha=GETDATE(), canceladoMotivo='" + toPed.getCanceladoMotivo() + "', canceladoFecha='" + fechaCancelacion.toString() + "', estatus=6\n"
                        + "WHERE idPedido=" + toPed.getReferencia();
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

    public void cerrarPedido(TOPedido toPed) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                toPed.setIdUsuario(this.idUsuario);
                toPed.setPropietario(0);
                toPed.setEstatus(5);

                strSQL = "DELETE FROM pedidosDetalle\n"
                        + "WHERE idPedido=" + toPed.getReferencia() + " AND cantOrdenada+cantOrdenadaSinCargo=0";
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
                        + "     , fecha=GETDATE(), idUsuario=" + this.idUsuario + ", propietario=0, estatus=5\n"
                        + "WHERE idMovto=" + toPed.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosAlmacen\n"
                        + "SET fecha=GETDATE(), idUsuario=" + this.idUsuario + ", propietario=0, estatus=5\n"
                        + "WHERE idMovtoAlmacen=" + toPed.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE pedidos\n"
                        + "SET fecha=GETDATE(), estatus=5\n"
                        + "WHERE idPedido=" + toPed.getReferencia();
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

    public void eliminarPedido(TOPedido toPed) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "DELETE FROM pedidos WHERE idPedido=" + toPed.getReferencia();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM pedidosDetalle WHERE idPedido=" + toPed.getReferencia();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM pedidosOC WHERE idPedidoOC =" + toPed.getIdPedidoOC();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientos WHERE idMovto=" + toPed.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalle WHERE idMovto=" + toPed.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalleImpuestos WHERE idMovto=" + toPed.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM comprobantes WHERE idComprobante=" + toPed.getIdComprobante();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosAlmacen WHERE idMovtoAlmacen=" + toPed.getIdMovtoAlmacen();
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

    public void liberarPedido(TOPedido toPed) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                movimientos.Movimientos.liberarMovimientoOficina(cn, toPed.getIdMovto(), this.idUsuario);
                toPed.setPropietario(0);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public ArrayList<TOProductoPedido> eliminarProducto(TOPedido toPed, TOProductoPedido toProd) throws SQLException {
        String strSQL;
        ArrayList<TOProductoPedido> similares = new ArrayList<>();
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

    public ArrayList<TOProductoPedido> trasferirSinCargo(TOPedido toPed, TOProductoPedido toProd, TOProductoPedido toSimilar, double cantidad) throws SQLException {
        String strSQL;
        ArrayList<TOProductoPedido> similares = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                if (toSimilar.getIdPedido() == 0) {
                    toSimilar.setIdPedido(toPed.getReferencia());
                    toSimilar.setIdMovto(toPed.getIdMovto());

                    this.agregaProductoPedido(cn, toPed, toSimilar);
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

    private String sqlSimilares(int idMovto, int idProducto) {
        return "SELECT ISNULL(D.idPedido, 0) AS idPedido, ISNULL(D.cantOrdenada, 0) AS cantOrdenada\n"
                + "     , ISNULL(D.cantOrdenadaSinCargo, 0) AS cantOrdenadaSinCargo\n"
                + "     , ISNULL(D.idMovto, 0) AS idMovto, ISNULL(D.idEmpaque, S.idSimilar) AS idEmpaque\n"
                + "     , ISNULL(D.cantFacturada, 0) AS cantFacturada, ISNULL(D.cantSinCargo, 0) AS cantSinCargo\n"
                + "	, ISNULL(D.costoPromedio, 0) AS costoPromedio, ISNULL(D.costo, 0) AS costo\n"
                + "	, ISNULL(D.desctoProducto1, 0) AS desctoProducto1, ISNULL(D.desctoProducto2, 0) AS desctoProducto2\n"
                + "	, ISNULL(D.desctoConfidencial, 0) AS desctoConfidencial, ISNULL(D.unitario, 0) AS unitario\n"
                + "	, ISNULL(D.idImpuestoGrupo, 0) AS idImpuestoGrupo\n"
                + "	, ISNULL(D.fecha, '1900-01-01') AS fecha, ISNULL(D.existenciaAnterior, 0) AS existenciaAnterior\n"
                + "FROM (" + this.sqlObtenProducto() + "\n"
                + "         WHERE D.idMovto=" + idMovto + ") D\n"
                + "RIGHT JOIN empaquesSimilares S ON S.idSimilar=D.idEmpaque\n"
                + "WHERE S.idEmpaque=" + idProducto + " AND S.idSimilar!=S.idEmpaque";
    }

    public ArrayList<TOProductoPedido> obtenerSimilares(int idMovto, int idProducto) throws SQLException {
        ArrayList<TOProductoPedido> productos = new ArrayList<>();
        String strSQL = this.sqlSimilares(idMovto, idProducto);
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    productos.add(this.construirProducto(rs));
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

    private void actualizaProductoCantidadPedido(Connection cn, TOPedido toPed, TOProductoPedido toProd) throws SQLException {
        String strSQL;
        ArrayList<Double> boletin = movimientos.Movimientos.obtenerBoletinSinCargo(cn, toPed.getIdEmpresa(), toPed.getIdReferencia(), toProd.getIdProducto());
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
                    double cantOrdenada = rs.getDouble("cantOrdenada");
                    double cantOrdenadaSinCargo = rs.getDouble("cantOrdenadaSinCargo");
                    double cantSimilaresSinCargo = (int) (cantOrdenada / boletin.get(0)) * boletin.get(1);
                    if (cantSimilaresSinCargo > cantOrdenadaSinCargo) {
                        toProd.setCantOrdenadaSinCargo(toProd.getCantOrdenadaSinCargo() + (cantSimilaresSinCargo - cantOrdenadaSinCargo));
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
                                + "WHERE S.idEmpaque=" + toProd.getIdProducto() + "\n"
                                + "ORDER BY D.cantOrdenada, D.cantOrdenadaSinCargo DESC";
                        rs = st.executeQuery(strSQL);
                        while (rs.next() && cantLiberar > 0) {
                            cantSinCargo = (int) (rs.getDouble("cantOrdenada") / boletin.get(0)) * boletin.get(1);
                            if (rs.getDouble("cantOrdenadaSinCargo") > cantSinCargo) {
                                disponibles = rs.getDouble("cantOrdenadaSinCargo") - cantSinCargo;
                                if (disponibles >= cantLiberar) {
                                    strSQL = "UPDATE pedidosDetalle\n"
                                            + "SET cantOrdenadaSinCargo=cantOrdenadaSinCargo-" + cantLiberar + "\n"
                                            + "WHERE idPedido=" + toPed.getReferencia() + " AND idEmpaque=" + rs.getInt("idEmpaque");
                                    cantLiberar = 0;
                                } else {
                                    strSQL = "UPDATE pedidosDetalle\n"
                                            + "SET cantOrdenadaSinCargo=cantOrdenadaSinCargo-" + disponibles + "\n"
                                            + "WHERE idPedido=" + toPed.getReferencia() + " AND idEmpaque=" + rs.getInt("idEmpaque");
                                    cantLiberar -= disponibles;
                                }
                                st1.executeUpdate(strSQL);
                            }
                        }
                    }
                } else {
                    toProd.setCantOrdenadaSinCargo((int) (toProd.getCantOrdenada() / boletin.get(0)) * boletin.get(1));
                    strSQL = "UPDATE pedidosDetalle\n"
                            + "SET cantOrdenadaSinCargo=" + toProd.getCantOrdenadaSinCargo() + "\n"
                            + "WHERE idPedido=" + toPed.getReferencia() + " AND idEmpaque=" + toProd.getIdProducto();
                    st.executeUpdate(strSQL);
                }
            } else {
                toProd.setCantOrdenadaSinCargo(0);
                strSQL = "UPDATE pedidosDetalle\n"
                        + "SET cantOrdenadaSinCargo=" + toProd.getCantOrdenadaSinCargo() + "\n"
                        + "WHERE idPedido=" + toPed.getReferencia() + " AND idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);
            }
        }
    }

    private ArrayList<TOProductoPedido> obtenSimilares(Connection cn, int idMovto, int idProducto) throws SQLException {
        ArrayList<TOProductoPedido> similares = new ArrayList<>();
        String strSQL = sqlObtenProducto() + "\n"
                + "INNER JOIN empaquesSimilares S ON S.idSimilar=D.idEmpaque\n"
                + "WHERE D.idMovto=" + idMovto + " AND S.idEmpaque=" + idProducto;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                similares.add(this.construirProducto(rs));
            }
        }
        return similares;
    }

    public void grabarProductoCantidadSinCargo(TOPedido toPed, TOProductoPedido toProd) throws SQLException {
        String strSQL = "UPDATE pedidosDetalle\n"
                + "SET cantOrdenadaSinCargo=" + toProd.getCantOrdenadaSinCargo() + "\n"
                + "WHERE idPedido=" + toPed.getReferencia() + " AND idEmpaque=" + toProd.getIdProducto();
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);
            }
        }
    }

    public ArrayList<TOProductoPedido> grabarProductoCantidad(TOPedido toPed, TOProductoPedido toProd) throws SQLException {
        ArrayList<TOProductoPedido> similares = new ArrayList<>();
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

    private void agregaProductoPedido(Connection cn, TOPedido toPed, TOProductoPedido toProd) throws SQLException {
        String strSQL = "INSERT INTO pedidosDetalle (idPedido, idEmpaque, cantOrdenada, cantOrdenadaSinCargo, cantSurtida, cantSurtidaSinCargo)\n"
                + "VALUES (" + toProd.getIdPedido() + ", " + toProd.getIdProducto() + ", " + toProd.getCantOrdenada() + ", " + toProd.getCantOrdenadaSinCargo() + ", 0, 0)";
        try (Statement st = cn.createStatement()) {
            movimientos.Movimientos.agregaProductoOficina(cn, toProd, toPed.getIdImpuestoZona());
            movimientos.Movimientos.actualizaProductoPrecio(cn, toPed, toProd);

            st.executeUpdate(strSQL);
        }
    }

    public void agregarProductoPedido(TOPedido toPed, TOProductoPedido toProd) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                this.agregaProductoPedido(cn, toPed, toProd);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public TOProductoPedido construirProducto(ResultSet rs) throws SQLException {
        TOProductoPedido to = new TOProductoPedido();
        to.setIdPedido(rs.getInt("idPedido"));
        to.setCantOrdenada(rs.getDouble("cantOrdenada"));
        to.setCantOrdenadaSinCargo(rs.getDouble("cantOrdenadaSinCargo"));
        movimientos.Movimientos.construirProductoOficina(rs, to);
        return to;
    }

    private String sqlObtenProducto() {
        // LEFT JOIN con pedidosDetalle por los posibles productos agregados (SIMILARES) por cantidad sin cargo
        return "SELECT ISNULL(PD.idPedido, 0) AS idPedido, ISNULL(PD.cantOrdenada, 0) AS cantOrdenada\n"
                + "         , ISNULL(PD.cantOrdenadaSinCargo, 0) AS cantOrdenadaSinCargo, D.*\n"
                + "FROM movimientosDetalle D\n"
                + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                + "LEFT JOIN pedidosDetalle PD ON PD.idPedido=M.referencia AND PD.idEmpaque=D.idEmpaque";
    }

    private ArrayList<TOProductoPedido> obtenDetalle(Connection cn, TOPedido toPed) throws SQLException {
        ArrayList<TOProductoPedido> detalle = new ArrayList<>();
        String strSQL = sqlObtenProducto() + "\n"
                + "WHERE D.idMovto=" + toPed.getIdMovto();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                detalle.add(this.construirProducto(rs));
            }
            movimientos.Movimientos.bloquearMovimientoOficina(cn, toPed, this.idUsuario);
        }
        return detalle;
    }

    public ArrayList<TOProductoPedido> obtenerDetalle(TOPedido toPed) throws SQLException {
        ArrayList<TOProductoPedido> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                detalle = this.obtenDetalle(cn, toPed);
                if (toPed.getIdUsuario() == toPed.getPropietario() && toPed.getEstatus() == 0 && toPed.getEspecial() == 0) {
                    for (TOProductoPedido toProd : detalle) {
                        movimientos.Movimientos.actualizaProductoPrecio(cn, toPed, toProd);
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

    public void agregarPedido(TOPedido toPed) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                toPed.setEstatus(0);
                toPed.setIdUsuario(this.idUsuario);
                toPed.setPropietario(this.idUsuario);

                String fechaOrden = "";
                if (!toPed.getOrdenDeCompra().isEmpty()) {
                    fechaOrden = new java.sql.Date(toPed.getOrdenDeCompraFecha().getTime()).toString();
                }
                strSQL = "INSERT INTO pedidosOC (fecha, ordenDeCompra, ordenDeCompraFecha, cancelacionFecha, entregaFolio, entregaFecha)\n"
                        + "VALUES (GETDATE(), '" + toPed.getOrdenDeCompra() + "', '" + fechaOrden + "', '', '', '')";
                st.executeUpdate(strSQL);

                ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idPedidoOC");
                if (rs.next()) {
                    toPed.setIdPedidoOC(rs.getInt("idPedidoOC"));
                }
                strSQL = "INSERT INTO pedidos (idTienda, idMoneda, idPedidoOC, fecha, canceladoMotivo, canceladoFecha, especial, electronico, estatus)\n"
                        + "VALUES (" + toPed.getIdReferencia() + ", " + toPed.getIdMoneda() + ", " + toPed.getIdPedidoOC() + ", GETDATE(), '', '', " + toPed.getEspecial() + ", '" + toPed.getElectronico() + "', 0)";
                st.executeUpdate(strSQL);

                rs = st.executeQuery("SELECT @@IDENTITY AS idPedido");
                if (rs.next()) {
                    toPed.setReferencia(rs.getInt("idPedido"));
                }
                TOComprobante toComprobante = new TOComprobante();
                toComprobante.setIdTipoMovto(toPed.getIdTipo());
                toComprobante.setIdEmpresa(toPed.getIdEmpresa());
                toComprobante.setIdReferencia(toPed.getIdReferencia());
                toComprobante.setTipo(1);
                toComprobante.setSerie("");
                toComprobante.setNumero(String.valueOf(toPed.getReferencia()));
                toComprobante.setFechaFactura(toPed.getFecha());
                toComprobante.setIdMoneda(toPed.getIdMoneda());
                toComprobante.setIdUsuario(toPed.getIdUsuario());
                toComprobante.setPropietario(0);
                toComprobante.setEstatus(5);
                toComprobante.setCerradoAlmacen(false);
                toComprobante.setCerradoOficina(false);
                comprobantes.Comprobantes.agregar(cn, toComprobante);

                toPed.setIdComprobante(toComprobante.getIdComprobante());
                movimientos.Movimientos.agregaMovimientoAlmacen(cn, toPed, false);
                movimientos.Movimientos.agregaMovimientoOficina(cn, toPed, false);

                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw (e);
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private TOPedido construirPedido(ResultSet rs) throws SQLException {
        TOPedido to = new TOPedido();
        to.setIdPedidoOC(rs.getInt("idPedidoOC"));
        to.setIdMoneda(rs.getInt("idMoneda"));
        to.setOrdenDeCompra(rs.getString("ordenDeCompra"));
        to.setOrdenDeCompraFecha(new java.util.Date(rs.getTimestamp("ordenDeCompraFecha").getTime()));
        to.setCanceladoMotivo(rs.getString("canceladoMotivo"));
        to.setCanceladoFecha(new java.util.Date(rs.getDate("canceladoFecha").getTime()));
        to.setEspecial(rs.getInt("especial"));
        to.setElectronico(rs.getString("electronico"));
        movimientos.Movimientos.construirMovimientoOficina(rs, to);
        return to;
    }

    public ArrayList<TOPedido> obtenerPedidos(int idAlmacen, int estatus, Date fechaInicial) throws SQLException {
        String condicion = "=0";
        if (estatus != 0) {
            condicion = " IN (5, 6)";
        }
        ArrayList<TOPedido> pedidos = new ArrayList<>();
        String strSQL = "SELECT M.*, P.idPedidoOC, P.idMoneda, P.canceladoFecha, P.canceladoMotivo, P.especial, P.electronico\n"
                + "     , ISNULL(OC.ordenDeCompra, '') AS ordenDeCompra, ISNULL(OC.ordenDeCompraFecha, '1900-01-01') AS ordenDeCompraFecha\n"
                + "FROM movimientos M\n"
                + "INNER JOIN pedidos P ON P.idPedido=M.referencia\n"
                + "LEFT JOIN pedidosOC OC ON OC.idPedidoOC=P.idPedidoOC\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=28 AND P.estatus" + condicion + "\n";
        if (estatus != 0) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            strSQL += "         AND CONVERT(date, P.fecha) >= '" + format.format(fechaInicial) + "'\n";
        }
        strSQL += "ORDER BY P.fecha";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    pedidos.add(this.construirPedido(rs));
                }
            }
        }
        return pedidos;
    }
}
