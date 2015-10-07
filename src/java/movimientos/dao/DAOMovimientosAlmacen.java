package movimientos.dao;

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
import movimientos.dominio.ProductoAlmacen;
import movimientos.dominio.MovimientoTipo;
import movimientos.to.TOMovimientoAlmacen;
import movimientos.to.TOProductoAlmacen;
import movimientos.to.TOProductoLotes;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOMovimientosAlmacen {

    int idUsuario, idCedis;
    private DataSource ds = null;

    public DAOMovimientosAlmacen() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
        this.idUsuario = usuarioSesion.getUsuario().getId();
        this.idCedis = usuarioSesion.getUsuario().getIdCedis();

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
    }

    public void cancelarMovimiento(int idMovtoAlmacen, boolean suma) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                if (!suma) {
                    strSQL = "UPDATE A\n"
                            + "SET separados=A.separados-k.cantidad\n"
                            + "FROM movimientosDetalleAlmacen D\n"
                            + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                            + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                            + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen;
                    st.executeUpdate(strSQL);
                }
                strSQL = "DELETE FROM movimientosDetalleAlmacen where idMovtoAlmacen=" + idMovtoAlmacen;
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosAlmacen WHERE idMovtoAlmacen=" + idMovtoAlmacen;
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

    public void grabarDetalle(TOMovimientoAlmacen toMov, boolean suma) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                toMov.setEstatus(5);
                toMov.setIdUsuario(this.idUsuario);
                toMov.setFolio(movimientos.Movimientos.obtenMovimientoFolioAlmacen(cn, toMov.getIdAlmacen(), toMov.getIdTipo()));
                movimientos.Movimientos.grabaMovimientoAlmacen(cn, toMov);

                movimientos.Movimientos.actualizaDetalleAlmacen(cn, toMov.getIdMovtoAlmacen(), suma);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void cancelarProducto(int idMovtoAlmacen, int idEmpaque, boolean suma) throws SQLException {
        String strSQL = "";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                if (!suma) {
                    strSQL = "UPDATE A\n"
                            + "SET separados=A.separados-D.cantidad\n"
                            + "FROM movimientosDetalleAlmacen D\n"
                            + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                            + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                            + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen + " AND D.idEmpaque=" + idEmpaque;
                    st.executeUpdate(strSQL);
                }
                strSQL = "DELETE FROM movimientosDetalleAlmacen where idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idEmpaque;
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

    public ArrayList<ProductoAlmacen> obtenerLotesDisponibles(int idAlmacen, int idMovtoAlmacen, int idEmpaque) throws SQLException {
        String strSQL;
        ArrayList<ProductoAlmacen> lotes = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                strSQL = "SELECT 0 AS idMovtoAlmacen, A.idEmpaque, A.lote, 0 AS cantidad, A.fechaCaducidad\n"
                        + "FROM (SELECT M.idAlmacen, D.idMovtoAlmacen, D.idEmpaque, D.lote, D.cantidad\n"
                        + "		FROM movimientosDetalleAlmacen D\n"
                        + "		INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "		WHERE D.idMovtoAlmacen=" + idMovtoAlmacen + ") D\n"
                        + "RIGHT JOIN almacenesLotes A ON A.idAlmacen=D.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                        + "WHERE A.existencia-A.separados > 0 AND D.idEmpaque IS NULL\n"
                        + "ORDER BY A.fechaCaducidad";
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    lotes.add(movimientos.Movimientos.construirLote(rs));
                }
            }
        }
        return lotes;
    }

    public void grabarProductoCantidad(TOProductoAlmacen toProd) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            movimientos.Movimientos.grabaProductoAlmacen(cn, toProd);
        }
    }

    public boolean validaLote(String lote) throws SQLException {
        boolean ok = false;
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                strSQL = "SELECT fecha FROM lotes WHERE lote=SUBSTRING('" + lote + "', 1, 4)";
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    ok = true;
                }
            }
        }
        return ok;
    }

    public void liberar(int idAlmacen, TOProductoAlmacen toProd, double separados) throws SQLException {
        String strSQL = "";
        double cantLiberar = separados - toProd.getCantidad();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "UPDATE almacenesLotes\n"
                        + "SET separados=separados-" + cantLiberar + "\n"
                        + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + toProd.getIdProducto() + " AND lote='" + toProd.getLote() + "'";
                st.executeUpdate(strSQL);

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

    public double separar(int idAlmacen, TOProductoAlmacen toProd, double separados) throws SQLException {
        double disponibles = 0;
        double cantSeparar = toProd.getCantidad() - separados;
        String strSQL = "SELECT lote,  existencia-separados AS disponibles\n"
                + "FROM almacenesLotes\n"
                + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + toProd.getIdProducto() + " AND lote='" + toProd.getLote() + "'";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    disponibles = rs.getDouble("disponibles");
                } else {
                    throw new SQLException("No se encontró el lote solicitado !!!");
                }
                if (disponibles <= 0) {
                    throw new SQLException("No hay unidades disponibles del lote solicitado !!!");
                } else if (disponibles < cantSeparar) {
                    cantSeparar = disponibles;
                }
                strSQL = "UPDATE almacenesLotes\n"
                        + "SET separados=separados+" + cantSeparar + "\n"
                        + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + toProd.getIdProducto() + " AND lote='" + toProd.getLote() + "'";
                st.executeUpdate(strSQL);

                toProd.setCantidad(separados + cantSeparar);
                movimientos.Movimientos.grabaProductoAlmacen(cn, toProd);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return cantSeparar;
    }

//    public TOProductoLotes construirProductoAlmacen(ResultSet rs) throws SQLException, Exception {
//        TOProductoLotes toProd = new TOProductoLotes();
//        boolean fin = movimientos.Movimientos.construirProducto(rs, toProd);
//        return toProd;
//    }
    public ArrayList<TOProductoAlmacen> obtenerDetalleProducto(int idMovtoAlmacen, int idProducto) throws SQLException {
        ArrayList<TOProductoAlmacen> producto = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            this.obtenDetalleProducto(cn, idMovtoAlmacen, idProducto);
        }
        return producto;
    }

    private ArrayList<TOProductoAlmacen> obtenDetalleProducto(Connection cn, int idMovtoAlmacen, int idProducto) throws SQLException {
        String strSQL = "";
        ArrayList<TOProductoAlmacen> producto = new ArrayList<>();
        try (Statement st = cn.createStatement()) {
            strSQL = "SELECT D.*, DATEADD(DAY, 365, L.fecha) AS fechaCaducidad\n"
                    + "FROM movimientosDetalleAlmacen D\n"
                    + "INNER JOIN lotes L ON L.lote=SUBSTRING(D.lote, 1, 4)\n"
                    + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen + " AND D.idEmpaque=" + idProducto + "\n"
                    + "ORDER BY D.idEmpaque, D.lote";
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                producto.add(movimientos.Movimientos.construirLote(rs));
            }
        }
        return producto;
    }

    private TOProductoLotes construir(ResultSet rs) throws SQLException {
        TOProductoLotes toProd = new TOProductoLotes();
        toProd.setIdMovtoAlmacen(rs.getInt("idMovtoAlmacen"));
        toProd.setIdProducto(rs.getInt("idEmpaque"));
        toProd.setCantidad(rs.getDouble("cantidad"));
        return toProd;
    }

    public ArrayList<TOProductoLotes> obtenerDetalle(int idMovtoAlmacen) throws SQLException {
        String strSQL = "";
        TOProductoLotes toProd;
        ArrayList<TOProductoLotes> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "SELECT idMovtoAlmacen, idEmpaque, SUM(cantidad) AS cantidad\n"
                        + "FROM movimientosDetalleAlmacen\n"
                        + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + "\n"
                        + "GROUP BY idMovtoAlmacen, idEmpaque";
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    toProd = this.construir(rs);
                    toProd.setLotes(this.obtenDetalleProducto(cn, toProd.getIdMovtoAlmacen(), toProd.getIdProducto()));
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

//    public ArrayList<TOProductoLotes> obtenerDetalle1(int idMovtoAlmacen) throws SQLException {
//        String strSQL = "";
//        ArrayList<TOProductoLotes> detalle = new ArrayList<>();
//        try (Connection cn = this.ds.getConnection()) {
//            try (Statement st = cn.createStatement()) {
//                strSQL = "SELECT D.*, DATEADD(DAY, 365, L.fecha) AS fechaCaducidad\n"
//                        + "FROM movimientosDetalleAlmacen D\n"
//                        + "INNER JOIN lotes L ON L.lote=SUBSTRING(D.lote, 1, 4)\n"
//                        + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen + "\n"
//                        + "ORDER BY D.idEmpaque, D.lote";
//                ResultSet rs = st.executeQuery(strSQL);
//                if (rs.next()) {
//                    boolean fin;
//                    TOProductoLotes toProd;
//                    do {
//                        toProd = new TOProductoLotes();
//                        fin = movimientos.Movimientos.construirProducto(rs, toProd);
//                        detalle.add(toProd);
//                    } while (!fin);
//                }
//            }
//        }
//        return detalle;
//    }

    public void agregarProducto(TOProductoAlmacen toProd) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            movimientos.Movimientos.agregaProductoAlmacen(cn, toProd);
        }
    }

    public ArrayList<TOMovimientoAlmacen> obtenerMovimientos(int idAlmacen, int idTipo, int estatus, Date fechaInicial) throws SQLException {
        String condicion = "=0 ";
        if (estatus != 0) {
            condicion = "!=0 ";
        }
        if (fechaInicial == null) {
            fechaInicial = new Date();
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TOMovimientoAlmacen> tos = new ArrayList<>();
        String strSQL = "SELECT M.*\n"
                + "FROM movimientosAlmacen M\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=" + idTipo + " AND M.estatus" + condicion + "\n"
                + "         AND CONVERT(date, M.fecha) <= '" + format.format(fechaInicial) + "'\n"
                + "ORDER BY M.fecha DESC";
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                tos.add(movimientos.Movimientos.construirMovimientoAlmacen(rs));
            }
        } finally {
            cn.close();
        }
        return tos;
    }

    public ArrayList<TOMovimientoAlmacen> obtenerMovimientosComprobante(int idAlmacen, int idTipo, int idComprobante) throws SQLException {
        ArrayList<TOMovimientoAlmacen> tos = new ArrayList<>();
        String strSQL = "SELECT * FROM movimientosAlmacen M\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=" + idTipo + " AND M.idComprobante=" + idComprobante;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    tos.add(movimientos.Movimientos.construirMovimientoAlmacen(rs));
                }
            }
        }
        return tos;
    }

    public int agregarMovimiento(TOMovimientoAlmacen toMov, boolean definitivo) throws SQLException {
        int idMovtoAlmacen = 0;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                toMov.setEstatus(0);
                toMov.setIdUsuario(this.idUsuario);
                toMov.setPropietario(this.idUsuario);
                movimientos.Movimientos.agregaMovimientoAlmacen(cn, toMov, definitivo);
                idMovtoAlmacen = toMov.getIdMovtoAlmacen();
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return idMovtoAlmacen;
    }

    public ArrayList<MovimientoTipo> obtenerMovimientosTipos(boolean suma) throws SQLException {
        ArrayList<MovimientoTipo> tipos = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            tipos = movimientos.Movimientos.obtenMovimientosTipos(cn, suma);
        }
        return tipos;
    }
}
