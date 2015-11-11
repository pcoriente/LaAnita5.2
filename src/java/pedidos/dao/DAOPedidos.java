package pedidos.dao;

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

    private void liberaMovimientoSeparados(Connection cn, int idMovto, int idMovtoAlmacen) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            strSQL = "UPDATE A\n"
                    + "SET separados=A.separados-(D.cantFacturada+D.cantSinCargo)\n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                    + "WHERE D.idMovto=" + idMovto;
            st.executeUpdate(strSQL);

            strSQL = "UPDATE movimientosDetalle\n"
                    + "SET cantFacturada=0, cantSinCargo=0\n"
                    + "WHERE idMovto=" + idMovto;
            st.executeUpdate(strSQL);

            strSQL = "UPDATE A\n"
                    + "SET separados=A.separados-D.cantidad\n"
                    + "FROM movimientosDetalleAlmacen D\n"
                    + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                    + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                    + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen;
            st.executeUpdate(strSQL);

            strSQL = "UPDATE movimientosDetalleAlmacen\n"
                    + "SET cantidad=0\n"
                    + "WHERE idMovtoAlmacen=" + idMovtoAlmacen;
            st.executeUpdate(strSQL);
        }
    }

    public void cancelarPedido(TOPedido toPed) throws SQLException {
        String strSQL = "";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                this.liberaMovimientoSeparados(cn, toPed.getIdMovto(), toPed.getIdMovtoAlmacen());

                strSQL = "UPDATE pedidos\n"
                        + "SET idUsuario=" + this.idUsuario + ", estatus=6\n"
                        + "     , canceladoMotivo='" + toPed.getCanceladoMotivo() + "', canceladoFecha=GETDATE()\n"
                        + "WHERE idPedido=" + toPed.getReferencia();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientos\n"
                        + "SET idUsuario=" + this.idUsuario + ", estatus=6\n"
                        + "WHERE idMovto=" + toPed.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosAlmacen\n"
                        + "SET idUsuario=" + this.idUsuario + ", estatus=6\n"
                        + "WHERE idMovtoAlmacen=" + toPed.getIdMovtoAlmacen();
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

    public void eliminarPedido(TOPedido toPed) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "DELETE FROM pedidos WHERE idPedido=" + toPed.getReferencia();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM pedidosDetalle WHERE idPedido=" + toPed.getReferencia();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientos WHERE idMovto=" + toPed.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalle WHERE idMovto=" + toPed.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalleImpuestos WHERE idMovto=" + toPed.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosAlmacen WHERE idMovtoAlmacen=" + toPed.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalleAlmacen WHERE idMovtoAlmacen=" + toPed.getIdMovtoAlmacen();
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
                strSQL = "DELETE FROM pedidosDetalle\n"
                        + "WHERE idPedido=" + toPed.getReferencia() + " AND cantOrdenada+cantOrdenadaSinCargo=0";
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientos\n"
                        + "SET desctoComercial=" + toPed.getDesctoComercial() + ", propietario=0, estatus=5\n"
                        + "WHERE idMovto=" + toPed.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE MD\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "LEFT JOIN pedidosDetalle PD ON PD.idPedido=M.referencia AND PD.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toPed.getIdMovto() + " AND PD.idPedido IS NULL";
                st.executeUpdate(strSQL);

                strSQL = "DELETE I\n"
                        + "FROM movimientosDetalleImpuestos I\n"
                        + "LEFT JOIN movimientosDetalle D ON D.idMovto=I.idMovto AND D.idEmpaque=I.idEmpaque\n"
                        + "WHERE I.idMovto=" + toPed.getIdMovto() + " AND D.idMovto IS NULL";
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosAlmacen\n"
                        + "SET propietario=0, estatus=5\n"
                        + "WHERE idMovtoAlmacen=" + toPed.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE pedidos\n"
                        + "SET idUsuario=" + this.idUsuario + ", fecha=GETDATE(), estatus=5\n"
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

    public boolean liberarPedido(int idMovto) throws SQLException, Exception {
        boolean liberado = true;
        String strSQL = "SELECT propietario FROM movimientos WHERE idMovto=" + idMovto;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    int propietario = rs.getInt("propietario");
                    if (propietario == this.idUsuario) {
                        strSQL = "UPDATE movimientos SET propietario=0 WHERE idMovto=" + idMovto;
                        st.executeUpdate(strSQL);
                    }
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
            return liberado;
        }
    }

    public void grabarPedidoDetalle(TOPedido toPed, TOProductoPedido toProd) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                this.actualizaProductoCantidadPedido(cn, toPed.getIdEmpresa(), toPed.getIdReferencia(), toPed.getReferencia(), toProd);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void agregarProductoPedido(TOPedido toPed, TOProductoPedido toProd) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {

                movimientos.Movimientos.agregaProductoOficina(cn, toProd, toPed.getIdImpuestoZona());
                movimientos.Movimientos.actualizaProductoPrecio(cn, toPed, toProd);

                strSQL = "INSERT INTO pedidosDetalle (idPedido, idEmpaque, cantOrdenada, cantOrdenadaSinCargo)\n"
                        + "VALUES (" + toProd.getIdPedido() + ", " + toProd.getIdProducto() + ", " + toProd.getCantOrdenada() + ", " + toProd.getCantOrdenadaSinCargo() + ")";
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

    public double obtenerImpuestosProducto(int idMovto, int idEmpaque, ArrayList<ImpuestosProducto> impuestos) throws SQLException {
        double importeImpuestos = 0;
        try (Connection cn = this.ds.getConnection()) {
            importeImpuestos = movimientos.Movimientos.obtenImpuestosProducto(cn, idMovto, idEmpaque, impuestos);
        }
        return importeImpuestos;
    }

    private void actualizaProductoCantidadPedido(Connection cn, int idEmpresa, int idTienda, int idPedido, TOProductoPedido toProd) throws SQLException {
        ArrayList<Double> boletin = movimientos.Movimientos.obtenerBoletinSinCargo(cn, idEmpresa, idTienda, toProd.getIdProducto());
        if (boletin.get(0) > 0) {
            toProd.setCantOrdenadaSinCargo((int) (toProd.getCantOrdenada() / boletin.get(0)) * boletin.get(1));
        } else {
            toProd.setCantOrdenadaSinCargo(0);
        }
        try (Statement st = cn.createStatement()) {
            String strSQL = "UPDATE pedidosDetalle\n"
                    + "SET cantOrdenada=" + toProd.getCantOrdenada() + ", cantOrdenadaSinCargo=" + toProd.getCantOrdenadaSinCargo() + "\n"
                    + "WHERE idPedido=" + idPedido + " AND idEmpaque=" + toProd.getIdProducto();
            st.executeUpdate(strSQL);
        }
    }

    private void actualizaProductoPedido(Connection cn, TOPedido toMov, TOProductoPedido toProd) throws SQLException {
        movimientos.Movimientos.actualizaProductoPrecio(cn, toMov, toProd);
        this.actualizaProductoCantidadPedido(cn, toMov.getIdEmpresa(), toMov.getIdReferencia(), toProd.getIdPedido(), toProd);
    }

//    public void actualizarPedido(int idEmpresa, TOPedido toMov, ArrayList<TOProductoPedido> tos) throws SQLException {
//        try (Connection cn = this.ds.getConnection()) {
//            cn.setAutoCommit(false);
//            try {
//                for (TOProductoPedido toProd : tos) {
//                    this.actualizaProductoPedido(cn, toMov, toProd);
//                }
//                cn.commit();
//            } catch (SQLException ex) {
//                cn.rollback();
//                throw ex;
//            } finally {
//                cn.setAutoCommit(true);
//            }
//        }
//    }
    public TOProductoPedido construirProductoPedido(ResultSet rs) throws SQLException {
        TOProductoPedido to = new TOProductoPedido();
        to.setIdPedido(rs.getInt("idPedido"));
        to.setCantOrdenada(rs.getDouble("cantOrdenada"));
        to.setCantOrdenadaSinCargo(rs.getDouble("cantOrdenadaSinCargo"));
        movimientos.Movimientos.construirProductoOficina(rs, to);
        return to;
    }

    public ArrayList<TOProductoPedido> obtenerPedidoDetalle(TOPedido toPed) throws SQLException {
        String strSQL;
        TOProductoPedido toProd;
        ArrayList<TOProductoPedido> productos = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "SELECT propietario, estatus FROM movimientos WHERE idMovto=" + toPed.getIdMovto();
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    toPed.setIdUsuario(this.idUsuario);
                    toPed.setEstatus(rs.getInt("estatus"));
                    toPed.setPropietario(rs.getInt("propietario"));
                    if (toPed.getPropietario() == 0) {
                        toPed.setPropietario(this.idUsuario);
                        strSQL = "UPDATE movimientos SET propietario=" + this.idUsuario + "\n"
                                + "WHERE idMovto=" + toPed.getIdMovto();
                        st.executeUpdate(strSQL);
                    }
                } else {
                    throw new SQLException("No se encontro el movimiento !!!");
                }
                strSQL = "SELECT ISNULL(PD.idPedido, 0) AS idPedido, ISNULL(PD.cantOrdenada, 0) AS cantOrdenada"
                        + "         , ISNULL(PD.cantOrdenadaSinCargo, 0) AS cantOrdenadaSinCargo, D.*\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "LEFT JOIN pedidosDetalle PD ON PD.idPedido=M.referencia AND PD.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toPed.getIdMovto();
                rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    toProd = this.construirProductoPedido(rs);
                    if (toPed.getIdUsuario() == toPed.getPropietario() && toPed.getEstatus() == 0) {
                        this.actualizaProductoPedido(cn, toPed, toProd);
                    }
                    productos.add(toProd);
                }
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

    public void agregarPedido(TOPedido toPed) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "INSERT INTO pedidosOC (fecha, ordenDeCompra, ordenDeCompraFecha, embarqueFecha, entregaFolio, entregaFecha)\n"
                        + "VALUES (GETDATE(), '" + toPed.getOrdenDeCompra() + "', '1900-01-01', '1900-01-01', '', '1900-01-01')";
                st.executeUpdate(strSQL);

                ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idPedidoOC");
                if (rs.next()) {
                    toPed.setIdPedidoOC(rs.getInt("idPedidoOC"));
                }
                strSQL = "INSERT INTO pedidos (idPedidoOC, canceladoMotivo, canceladoFecha)\n"
                        + "VALUES (" + toPed.getIdPedidoOC() + ", '', '1900-01-01')";
                st.executeUpdate(strSQL);

                rs = st.executeQuery("SELECT @@IDENTITY AS idPedido");
                if (rs.next()) {
                    toPed.setReferencia(rs.getInt("idPedido"));
                }
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
        to.setOrdenDeCompra(rs.getString("ordenDeCompra"));
        to.setOrdenDeCompraFecha(new java.util.Date(rs.getTimestamp("ordenDeCompraFecha").getTime()));
        to.setCanceladoMotivo(rs.getString("canceladoMotivo"));
        to.setCanceladoFecha(new java.util.Date(rs.getDate("canceladoFecha").getTime()));
        movimientos.Movimientos.construirMovimientoOficina(rs, to);
        return to;
    }

    public ArrayList<TOPedido> obtenerPedidos(int idAlmacen, int estatus, Date fechaInicial) throws SQLException {
        if (fechaInicial == null) {
            fechaInicial = new Date();
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TOPedido> pedidos = new ArrayList<>();
        String strSQL = "SELECT M.*, P.idPedidoOC, P.canceladoFecha, P.canceladoMotivo\n"
                + "     , ISNULL(OC.ordenDeCompra, '') AS ordenDeCompra, ISNULL(OC.ordenDeCompraFecha, '1900-01-01') AS ordenDeCompraFecha\n"
                + "FROM movimientos M\n"
                + "INNER JOIN pedidos P ON P.idPedido=M.referencia\n"
                + "LEFT JOIN pedidosOC OC ON OC.idPedidoOC=P.idPedidoOC\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=28 AND M.referencia!=0\n"
                + "         AND CONVERT(date, M.fecha) <= '" + format.format(fechaInicial) + "' AND M.estatus=" + estatus;
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

//    vvvvvvvvvvvvvvvvvvvvvvvvvvv NO SE USAN vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
    public ArrayList<TOProductoPedido> obtenerSimilaresPedido(int idPedido, int idProducto) throws SQLException {
        ArrayList<TOProductoPedido> productos = new ArrayList<>();
        String strSQL = "SELECT CASE WHEN S.idEmpaque=S.idSimilar THEN 1 ELSE 0 END AS principal\n"
                + "	, ISNULL(D.idPedido, 0) AS idPedido, ISNULL(D.idEmpaque,S.idEmpaque) AS idEmpaque\n"
                + "	, ISNULL(D.cantFacturada, 0) AS cantFacturada, ISNULL(D.cantSinCargo, 0) AS cantSinCargo\n"
                + "	, ISNULL(D.unitario, 0) AS unitario, P.idImpuesto AS idImpuestoGrupo\n"
                + "FROM empaquesSimilares S\n"
                + "LEFT JOIN (SELECT * FROM pedidosOCTiendaDetalle WHERE idPedido=" + idPedido + ") D ON D.idEmpaque=S.idEmpaque\n"
                + "INNER JOIN empaques E ON E.idEmpaque=S.idEmpaque\n"
                + "INNER JOIN productos P ON P.idProducto=E.idProducto\n"
                + "WHERE S.idSimilar=" + idProducto + " AND S.idSimilar!=S.idEmpaque\n"
                + "ORDER BY principal DESC, idPedido DESC";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    productos.add(this.construirProductoPedido(rs));
                }
            }
        }
        return productos;
    }

    public ArrayList<TOProductoPedido> obtenerPedidoSimilares(int idPedido, int idProducto) throws SQLException {
        ArrayList<TOProductoPedido> productos = new ArrayList<>();
        String strSQL = "SELECT D.*\n"
                + "FROM empaquesSimilares S\n"
                + "INNER JOIN pedidosDetalle D ON D.idEmpaque=S.idEmpaque\n"
                + "WHERE D.idPedido=" + idPedido + " AND D.idEmpaque=" + idProducto;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    productos.add(this.construirProductoPedido(rs));
                }
            }
        }
        return productos;
    }

    public void trasferirSinCargo(TOProductoPedido toProdOrigen, TOProductoPedido toProdSimilar, int idImpuestoZona, double cantidad) throws SQLException {
        String strSQL;
        TOPedido toPed;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "SELECT idEmpresa, idTienda FROM movimientos WHERE idMovto=" + toProdOrigen.getIdMovto();
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    toPed = new TOPedido();
                    toPed.setReferencia(toProdOrigen.getIdPedido());
                    toPed.setIdEmpresa(rs.getInt("idEmpresa"));
                    toPed.setIdReferencia(rs.getInt("idTienda"));
                } else {
                    throw new SQLException("No se encontro el movimiento del pedido !!!");
                }
                if (toProdSimilar.getIdPedido() == 0) {
                    toProdSimilar.setIdPedido(toProdOrigen.getIdPedido());
                    toProdSimilar.setCantSinCargo(cantidad);
                    this.agregarProductoPedido(toPed, toProdSimilar);
                } else {
                    toProdSimilar.setCantSinCargo(toProdSimilar.getCantSinCargo() + cantidad);

                    strSQL = "UPDATE pedidosDetalle\n"
                            + "SET cantOrdenadaSinCargo=cantOrdenadaSinCargo+" + cantidad + "\n"
                            + "WHERE idPedido=" + toPed.getReferencia() + " AND idEmpaque=" + toProdSimilar.getIdProducto();
                    st.executeUpdate(strSQL);
                }
                strSQL = "UPDATE pedidosOCTiendaDetalle SET cantSinCargo=cantSinCargo-" + cantidad + "\n"
                        + "WHERE idPedido=" + toPed.getReferencia() + " AND idEmpaque=" + toProdOrigen.getIdProducto();
                st.executeUpdate(strSQL);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.close();
            }
        }
    }
//    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ NO SE USAN ^^^^^^^^^^^^^^^^^^^^^^^^^^
}
