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
                        + "SET estatus=6\n"
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

                strSQL = "DELETE FROM pedidosOC WHERE idPedidoOC =" + toPed.getIdPedidoOC();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientos WHERE idMovto=" + toPed.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalle WHERE idMovto=" + toPed.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalleImpuestos WHERE idMovto=" + toPed.getIdMovto();
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

                strSQL = "DELETE PD\n"
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

//                strSQL = "UPDATE pedidos\n"
//                        + "SET idUsuario=" + this.idUsuario + ", fecha=GETDATE(), estatus=5\n"
//                        + "WHERE idPedido=" + toPed.getReferencia();
                strSQL = "UPDATE pedidos\n"
                        + "SET estatus=5\n"
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

    public ArrayList<TOProductoPedido> grabarProductoCantidad(TOPedido toPed, TOProductoPedido toProd) throws SQLException {
        ArrayList<TOProductoPedido> similares = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                this.actualizaProductoCantidadPedido(cn, toPed, toProd);
                similares = this.obtenSimilares(cn, toProd.getIdMovto(), toProd.getIdProducto());
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

    private void agregaProducto(Connection cn, TOPedido toPed, TOProductoPedido toProd) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            movimientos.Movimientos.agregaProductoOficina(cn, toProd, toPed.getIdImpuestoZona());
            movimientos.Movimientos.actualizaProductoPrecio(cn, toPed, toProd);

            strSQL = "INSERT INTO pedidosDetalle (idPedido, idEmpaque, cantOrdenada, cantOrdenadaSinCargo, cantSurtida, cantSurtidaSinCargo, similar)\n"
                    + "VALUES (" + toProd.getIdPedido() + ", " + toProd.getIdProducto() + ", " + toProd.getCantOrdenada() + ", " + toProd.getCantOrdenadaSinCargo() + ", 0, 0, " + (toProd.isSimilar() ? 1 : 0) + ")";
            st.executeUpdate(strSQL);
        }
    }

    public void agregarProductoPedido(TOPedido toPed, TOProductoPedido toProd) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                this.agregaProducto(cn, toPed, toProd);
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

//    private void actualizaProductoPedido1(Connection cn, TOPedido toMov, TOProductoPedido toProd) throws SQLException {
//        movimientos.Movimientos.actualizaProductoPrecio(cn, toMov, toProd);
//        this.actualizaProductoCantidadPedido(cn, toMov.getIdEmpresa(), toMov.getIdReferencia(), toProd.getIdPedido(), toProd);
//    }
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
//    
    private void actualizaProductoCantidadPedido(Connection cn, TOPedido toPed, TOProductoPedido toProd) throws SQLException {
        String strSQL;
        ArrayList<Double> boletin = movimientos.Movimientos.obtenerBoletinSinCargo(cn, toPed.getIdEmpresa(), toPed.getIdReferencia(), toProd.getIdProducto());
//        if (boletin.get(0) > 0) {
//            toProd.setCantOrdenadaSinCargo((int) (toProd.getCantOrdenada() / boletin.get(0)) * boletin.get(1));
//        } else {
//            toProd.setCantOrdenadaSinCargo(0);
//        }
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
    
    public TOProductoPedido construirProducto(ResultSet rs) throws SQLException {
        TOProductoPedido to = new TOProductoPedido();
        to.setIdPedido(rs.getInt("idPedido"));
        to.setCantOrdenada(rs.getDouble("cantOrdenada"));
        to.setCantOrdenadaSinCargo(rs.getDouble("cantOrdenadaSinCargo"));
        to.setSimilar(rs.getBoolean("similar"));
        movimientos.Movimientos.construirProductoOficina(rs, to);
        return to;
    }

    private ArrayList<TOProductoPedido> obtenDetalle(Connection cn, TOPedido toPed) throws SQLException {
        String strSQL;
        int propietario = 0;
        ArrayList<TOProductoPedido> detalle = new ArrayList<>();
        try (Statement st = cn.createStatement()) {
            strSQL = "SELECT ISNULL(PD.idPedido, 0) AS idPedido, ISNULL(PD.cantOrdenada, 0) AS cantOrdenada\n"
                    + "         , ISNULL(PD.cantOrdenadaSinCargo, 0) AS cantOrdenadaSinCargo, ISNULL(PD.similar, 0) AS similar, D.*\n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "LEFT JOIN pedidosDetalle PD ON PD.idPedido=M.referencia AND PD.idEmpaque=D.idEmpaque\n"
                    + "WHERE D.idMovto=" + toPed.getIdMovto();
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                detalle.add(this.construirProducto(rs));
            }
            strSQL = "SELECT propietario, estatus FROM movimientos WHERE idMovto=" + toPed.getIdMovto();
            rs = st.executeQuery(strSQL);
            if (rs.next()) {
                toPed.setEstatus(rs.getInt("estatus"));
                propietario = rs.getInt("propietario");
                if (propietario == 0) {
                    strSQL = "UPDATE movimientos SET propietario=" + this.idUsuario + "\n"
                            + "WHERE idMovto=" + toPed.getIdMovto();
                    st.executeUpdate(strSQL);
                    toPed.setPropietario(this.idUsuario);
                } else {
                    toPed.setPropietario(propietario);
                }
                toPed.setIdUsuario(this.idUsuario);
            } else {
                throw new SQLException("No se encontro el movimiento !!!");
            }
        }
        return detalle;
    }

    public ArrayList<TOProductoPedido> obtenerDetalle(TOPedido toPed) throws SQLException {
        ArrayList<TOProductoPedido> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                detalle = this.obtenDetalle(cn, toPed);
                if (toPed.getIdUsuario() == toPed.getIdUsuario() && toPed.getEstatus() == 0) {
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
                toPed.setIdUsuario(this.idUsuario);
                toPed.setPropietario(this.idUsuario);

                strSQL = "INSERT INTO pedidosOC (fecha, ordenDeCompra, ordenDeCompraFecha, embarqueFecha, entregaFolio, entregaFecha)\n"
                        + "VALUES (GETDATE(), '" + toPed.getOrdenDeCompra() + "', '1900-01-01', '1900-01-01', '', '1900-01-01')";
                st.executeUpdate(strSQL);

                ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idPedidoOC");
                if (rs.next()) {
                    toPed.setIdPedidoOC(rs.getInt("idPedidoOC"));
                }
                strSQL = "INSERT INTO pedidos (idTienda, idMoneda, idPedidoOC, fecha, canceladoMotivo, canceladoFecha, estatus)\n"
                        + "VALUES (" + toPed.getIdTienda() + ", " + toPed.getIdMoneda() + ", " + toPed.getIdPedidoOC() + ", GETDATE(), '', '1900-01-01', 0)";
                st.executeUpdate(strSQL);

                rs = st.executeQuery("SELECT @@IDENTITY AS idPedido");
                if (rs.next()) {
                    toPed.setReferencia(rs.getInt("idPedido"));
                }
                TOComprobante to=new TOComprobante();
                to.setIdTipoMovto(toPed.getIdTipo());
                to.setIdEmpresa(toPed.getIdEmpresa());
                to.setIdReferencia(toPed.getIdReferencia());
                to.setTipo(1);
                to.setSerie("");
                to.setNumero(String.valueOf(toPed.getReferencia()));
                to.setFechaFactura(toPed.getFecha());
                to.setIdMoneda(toPed.getIdMoneda());
                to.setIdUsuario(this.idUsuario);
                to.setPropietario(0);
                to.setEstatus(5);
                to.setCerradoAlmacen(false);
                to.setCerradoOficina(false);
                comprobantes.Comprobantes.agregar(cn, to);
                
                toPed.setIdComprobante(to.getIdComprobante());
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
        movimientos.Movimientos.construirMovimientoOficina(rs, to);
        return to;
    }

    public ArrayList<TOPedido> obtenerPedidos(int idAlmacen, int estatus, Date fechaInicial) throws SQLException {
        String condicion = "=0";
        if (estatus != 0) {
            condicion = "!=0";
        }
        if (fechaInicial == null) {
            fechaInicial = new Date();
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TOPedido> pedidos = new ArrayList<>();
        String strSQL = "SELECT M.*, P.idPedidoOC, P.idMoneda, P.canceladoFecha, P.canceladoMotivo\n"
                + "     , ISNULL(OC.ordenDeCompra, '') AS ordenDeCompra, ISNULL(OC.ordenDeCompraFecha, '1900-01-01') AS ordenDeCompraFecha\n"
                + "FROM movimientos M\n"
                + "INNER JOIN pedidos P ON P.idPedido=M.referencia\n"
                + "LEFT JOIN pedidosOC OC ON OC.idPedidoOC=P.idPedidoOC\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=28 AND P.estatus" + condicion + "\n"
                + "         AND CONVERT(date, P.fecha) >= '" + format.format(fechaInicial) + "'";
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
    private ArrayList<TOProductoPedido> obtenSimilares(Connection cn, int idMovto, int idProducto) throws SQLException {
        ArrayList<TOProductoPedido> similares = new ArrayList<>();
        String strSQL = "SELECT PD.idPedido, PD.cantOrdenada, PD.cantOrdenadaSinCargo, PD.similar, D.*\n"
                + "FROM movimientosDetalle D\n"
                + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                + "INNER JOIN pedidosDetalle PD ON PD.idPedido=M.referencia AND PD.idEmpaque=D.idEmpaque\n"
                + "INNER JOIN empaquesSimilares S ON S.idSimilar=D.idEmpaque\n"
                + "WHERE M.idMovto=" + idMovto + " AND S.idEmpaque=" + idProducto;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                similares.add(this.construirProducto(rs));
            }
        }
        return similares;
    }

    public ArrayList<TOProductoPedido> trasferirSinCargo(TOPedido toPed, TOProductoPedido toProd, TOProductoPedido toSimilar, int idImpuestoZona, double cantidad) throws SQLException {
        String strSQL;
        ArrayList<TOProductoPedido> similares = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                if (toSimilar.isSimilar()) {
                    toSimilar.setIdPedido(toProd.getIdPedido());
                    toSimilar.setIdMovto(toProd.getIdMovto());
                    toSimilar.setCantOrdenadaSinCargo(cantidad);
                    toSimilar.setSimilar(false);
                    this.agregaProducto(cn, toPed, toSimilar);
                } else {
                    toSimilar.setCantSinCargo(toSimilar.getCantSinCargo() + cantidad);

                    strSQL = "UPDATE pedidosDetalle\n"
                            + "SET cantOrdenadaSinCargo=cantOrdenadaSinCargo+" + cantidad + "\n"
                            + "WHERE idPedido=" + toPed.getReferencia() + " AND idEmpaque=" + toSimilar.getIdProducto();
                    st.executeUpdate(strSQL);
                }
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

    public ArrayList<TOProductoPedido> obtenerSimilares(int idMovto, int idProducto) throws SQLException {
        ArrayList<TOProductoPedido> productos = new ArrayList<>();
        String strSQL = "SELECT ISNULL(D.cantOrdenada, 0) AS cantOrdenada, ISNULL(D.cantOrdenadaSinCargo, 0) AS cantOrdenadaSinCargo\n"
                + "     , ISNULL(D.idMovto, 0) AS idMovto, ISNULL(D.idPedido, 0) AS idPedido, ISNULL(D.idEmpaque, S.idSimilar) AS idEmpaque\n"
                + "     , ISNULL(D.cantFacturada, 0) AS cantFacturada, ISNULL(D.cantSinCargo, 0) AS cantSinCargo\n"
                + "	, ISNULL(D.costoPromedio, 0) AS costoPromedio, ISNULL(D.costo, 0) AS costo\n"
                + "	, ISNULL(D.desctoProducto1, 0) AS desctoProducto1, ISNULL(D.desctoProducto2, 0) AS desctoProducto2\n"
                + "	, ISNULL(D.desctoConfidencial, 0) AS desctoConfidencial, ISNULL(D.unitario, 0) AS unitario\n"
                + "	, ISNULL(D.idImpuestoGrupo, 0) AS idImpuestoGrupo\n"
                + "	, ISNULL(D.fecha, '1900-01-01') AS fecha, ISNULL(D.existenciaAnterior, 0) AS existenciaAnterior\n"
                + "FROM (SELECT M.referencia AS idPedido, PD.cantOrdenada, PD.cantOrdenadaSinCargo, PD.similar, D.*\n"
                + "	FROM movimientosDetalle D\n"
                + "	INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                + "	INNER JOIN pedidosDetalle PD ON PD.idPedido=M.referencia AND PD.idEmpaque=D.idEmpaque\n"
                + "	WHERE M.idMovto=" + idMovto + ") D\n"
                + "RIGHT JOIN empaquesSimilares S ON S.idSimilar=D.idEmpaque\n"
                + "WHERE S.idEmpaque=" + idProducto + " AND S.idSimilar!=S.idEmpaque";
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
//    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ NO SE USAN ^^^^^^^^^^^^^^^^^^^^^^^^^^
}
