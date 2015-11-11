package ventas.dao;

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

    public ArrayList<TOVentaProducto> surtirFincado(TOVenta toMov) throws SQLException {
        // Intenta surtir en automatico TODO el pedido fincado
        double cantSolicitada;
        ArrayList<TOVentaProducto> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                detalle = this.obtenerDetalle(toMov);
                if (toMov.getIdUsuario() == toMov.getPropietario() && toMov.getEstatus() == 5) {
                    for (TOVentaProducto to : detalle) {
                        if (to.getCantFacturada() + to.getCantSinCargo() < to.getCantOrdenada() + to.getCantOrdenadaSinCargo()) {
                            cantSolicitada = to.getCantOrdenada() + to.getCantOrdenadaSinCargo() - (to.getCantFacturada() + to.getCantSinCargo());
                            try {
                                movimientos.Movimientos.separar(cn, toMov, to.getIdProducto(), cantSolicitada, true);
                            } catch (Exception ex) {
                            }
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

    public ArrayList<TOVentaProducto> obtenerDetalleFincado(TOVenta toMov) throws SQLException {
        // Al cargar un pedido fincado no se modifica ni precios ni cantidades separadas
        ArrayList<TOVentaProducto> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                detalle = this.obtenerDetalle(toMov);
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

    private ArrayList<TOVentaProducto> obtenerDetalle(TOVenta toMov) throws SQLException {
        String strSQL;
        int propietario = 0;
        ArrayList<TOVentaProducto> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "SELECT MD.*, M.referencia AS idPedido\n"
                        + "         , ISNULL(PD.cantOrdenada, 0) AS cantOrdenada"
                        + "         , ISNULL(PD.cantOrdenadaSinCargo, 0) AS cantOrdenadaSinCargo\n"
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
                    propietario = rs.getInt("propietario");
                    if (propietario == 0) {
                        strSQL = "UPDATE movimientos SET propietario=" + this.idUsuario + "\n"
                                + "WHERE idMovto=" + toMov.getIdMovto();
                        st.executeUpdate(strSQL);
                        toMov.setPropietario(this.idUsuario);
                    } else {
                        toMov.setPropietario(propietario);
                    }
                    toMov.setEstatus(rs.getInt("estatus"));
                    toMov.setIdUsuario(this.idUsuario);
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
        return detalle;
    }

    public ArrayList<TOVentaProducto> obtenerDetalleVenta(TOVenta toMov) throws SQLException {
        // Al cargar una venta no cerrada, actualiza precios y verifica boletines de productos
        double cantSeparada;
        ArrayList<TOVentaProducto> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                detalle = this.obtenerDetalle(toMov);
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

    public void agregarVenta(TOVenta toMov) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
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

    public TOVenta obtenerVenta(int idMovto) throws SQLException {
        TOVenta toMov = null;
        String strSQL = "SELECT M.*\n"
                + "     , ISNULL(P.idPedidoOC, 0) AS idPedidoOC, ISNULL(P.canceladoFecha, '1900-01-01') AS canceladoFecha, ISNULL(P.canceladoMotivo, '') AS canceladoMotivo\n"
                + "     , ISNULL(OC.ordenDeCompra, '') AS ordenDeCompra, ISNULL(OC.ordenDeCompraFecha, '1900-01-01') AS ordenDeCompraFecha\n"
                + "FROM movimientos M\n"
                + "LEFT JOIN pedidos P ON P.idPedido=M.referencia\n"
                + "LEFT JOIN pedidosOC OC ON OC.idPedidoOC=P.idPedidoOC\n"
                + "WHERE M.idMovto=" + idMovto;
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                toMov = this.construir(rs);
            }
        } finally {
            cn.close();
        }
        return toMov;
    }

    private TOVenta construir(ResultSet rs) throws SQLException {
        TOVenta toMov = new TOVenta(28);
        toMov.setIdPedidoOC(rs.getInt("idPedidoOC"));
        toMov.setOrdenDeCompra(rs.getString("ordenDeCompra"));
        toMov.setOrdenDeCompraFecha(new java.util.Date(rs.getTimestamp("ordenDeCompraFecha").getTime()));
        toMov.setCanceladoMotivo(rs.getString("canceladoMotivo"));
        toMov.setCanceladoFecha(new java.util.Date(rs.getDate("canceladoFecha").getTime()));
        movimientos.Movimientos.construirMovimientoOficina(rs, toMov);
        return toMov;
    }

    public ArrayList<TOVenta> obtenerVentas(int idAlmacen, int estatus, Date fechaInicial) throws SQLException {
        String condicion = ">=7 ";
        if (estatus == 0) {
            condicion = "<7";
        }
        if (fechaInicial == null) {
            fechaInicial = new Date();
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TOVenta> ventas = new ArrayList<>();
        String strSQL = "SELECT M.*\n"
                + "     , ISNULL(P.idPedidoOC, 0) AS idPedidoOC, ISNULL(P.canceladoFecha, '1900-01-01') AS canceladoFecha, ISNULL(P.canceladoMotivo, '') AS canceladoMotivo\n"
                + "     , ISNULL(OC.ordenDeCompra, '') AS ordenDeCompra, ISNULL(OC.ordenDeCompraFecha, '1900-01-01') AS ordenDeCompraFecha\n"
                + "FROM movimientos M\n"
                + "LEFT JOIN pedidos P ON P.idPedido=M.referencia\n"
                + "LEFT JOIN pedidosOC OC ON OC.idPedidoOC=P.idPedidoOC\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=28 AND M.estatus" + condicion + "\n"
                + "         AND CONVERT(date, M.fecha) >= '" + format.format(fechaInicial) + "'\n"
                + "ORDER BY M.fecha DESC";
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                if (rs.getInt("referencia") == 0 || rs.getInt("estatus") != 0) {
                    this.construir(rs);
                }
            }
        } finally {
            cn.close();
        }
        return ventas;
    }
}
