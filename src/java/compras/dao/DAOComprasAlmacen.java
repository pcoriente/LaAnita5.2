package compras.dao;

import compras.to.TOProductoCompraAlmacen;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import movimientos.dominio.ProductoAlmacen;
import movimientos.to.TOMovimientoAlmacen;
import movimientos.to.TOProductoAlmacen;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOComprasAlmacen {

    int idUsuario, idCedis;
    private DataSource ds = null;

    public DAOComprasAlmacen() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
        this.idUsuario = usuarioSesion.getUsuario().getId();
        this.idCedis = usuarioSesion.getUsuario().getIdCedis();

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
    }
    
    public ArrayList<TOProductoCompraAlmacen> obtenerComprobanteDetalle(int idComprobante) throws SQLException {
        String strSQL;
        ArrayList<TOProductoCompraAlmacen> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                strSQL="SELECT S.cantOrdenada+S.cantOrdenadaSinCargo AS cantOrdenada\n"
                        + "     , M.folio AS idMovtoAlmacen, D.idEmpaque, D.lote, D.cantidad\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M on M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN ordenCompraSurtido S ON S.idOrdenCompra=M.referencia AND S.idEmpaque=D.idEmpaque\n"
                        + "WHERE M.idComprobante="+idComprobante+" AND estatus=5\n"
                        + "ORDER BY M.referencia, M.folio";
                ResultSet rs=st.executeQuery(strSQL);
                while(rs.next()) {
                    detalle.add(this.construirProductoCompra(rs));
                }
            }
        }
        return detalle;
    }

    public void eliminarProducto(int idMovtoAlmacen, int idProducto) throws SQLException {
        String strSQL = "";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "UPDATE S\n"
                        + "SET separadosAlmacen=S.separadosAlmacen-D.cantidad\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN ordenCompraSurtido S ON S.idOrdenCompra=M.referencia AND S.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen + " AND D.idEmpaque=" + idProducto;
                st.executeUpdate(strSQL);

                movimientos.Movimientos.eliminaProductoAlmacen(cn, idMovtoAlmacen, idProducto);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }
    
    private void validarExistenciaAlmacen(Connection cn, int idMovtoAlmacen) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            strSQL = "SELECT D.*\n"
                    + "FROM movimientosDetalleAlmacen D\n"
                    + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                    + "LEFT JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                    + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen + " AND ISNULL(A.existencia, 0)-ISNULL(A.separados, 0) < D.cantidad";
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                throw new SQLException("Existencia insuficiente en almacen para realizar movimiento !!!");
            }
        }
    }
    
    public void cancelarCompra(int idMovtoAlmacen, int idAlmacen, int idOrdenDeCompra) throws SQLException {
        String strSQL;
        TOMovimientoAlmacen toMov;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                this.validarExistenciaAlmacen(cn, idMovtoAlmacen);
                
                strSQL = "UPDATE movimientosAlmacen SET estatus=8 WHERE idMovtoAlmacen=" + idMovtoAlmacen;
                st.executeUpdate(strSQL);
                
                toMov = movimientos.Movimientos.obtenMovimientoAlmacen(cn, idMovtoAlmacen);
                
                toMov.setFolio(0);
                toMov.setIdTipo(34);
                toMov.setIdUsuario(this.idUsuario);
                toMov.setPropietario(0);
                toMov.setEstatus(5);
                movimientos.Movimientos.agregaMovimientoAlmacen(cn, toMov, true);
                
                strSQL = "INSERT INTO movimientosDetalleAlmacen\n"
                        + "SELECT " + toMov.getIdMovtoAlmacen()+ ", idEmpaque, lote, cantidad, '', 0\n"
                        + "FROM movimientosDetalleAlmacen WHERE idMovtoAlmacen=" + idMovtoAlmacen;
                st.executeUpdate(strSQL);
                
                movimientos.Movimientos.actualizaDetalleAlmacen(cn, toMov.getIdMovtoAlmacen(), false);
                
                strSQL = "INSERT INTO devolucionesAlmacen (idMovtoAlmacen, idDevolucion) VALUES (" + idMovtoAlmacen + ", " + toMov.getIdMovtoAlmacen()+ ")";
                st.executeUpdate(strSQL);
                
                if (idOrdenDeCompra != 0) {
                    strSQL = "UPDATE S\n"
                            + "SET surtidosAlmacen=S.surtidosAlmacen-D.cantidad\n"
                            + "FROM movimientosDetalleAlmacen D\n"
                            + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                            + "INNER JOIN ordenCompraSurtido S ON S.idOrdenCompra=M.referencia AND S.idEmpaque=D.idEmpaque\n"
                            + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen;
                    st.executeUpdate(strSQL);

                    strSQL = "SELECT idEmpaque\n"
                            + "FROM ordenCompraSurtido\n"
                            + "WHERE idOrdenCompra=" + idOrdenDeCompra + "\n"
                            + "         AND (surtidosAlmacen < cantOrdenada + cantOrdenadaSinCargo)";
                    ResultSet rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        strSQL = "UPDATE ordenCompra SET estadoAlmacen=5, fechaCierreAlmacen='' WHERE idOrdenCompra=" + idOrdenDeCompra;
                        st.executeUpdate(strSQL);
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

    public void grabarCompra(TOMovimientoAlmacen toMov) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                toMov.setEstatus(5);
                toMov.setIdUsuario(this.idUsuario);
                toMov.setFolio(movimientos.Movimientos.obtenMovimientoFolioAlmacen(cn, toMov.getIdAlmacen(), toMov.getIdTipo()));
                movimientos.Movimientos.grabaMovimientoAlmacen(cn, toMov);

                strSQL = "UPDATE D\n"
                        + "SET lote=L.lote+'1'\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN lotes L ON L.fecha=CONVERT(DATE, M.fecha)\n"
                        + "WHERE D.idMovtoAlmacen=" + toMov.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                movimientos.Movimientos.actualizaDetalleAlmacen(cn, toMov.getIdMovtoAlmacen(), true);

                if (toMov.getReferencia() != 0) {
                    strSQL = "UPDATE S\n"
                            + "SET surtidosAlmacen=S.surtidosAlmacen+D.cantidad\n"
                            + "	, separadosAlmacen=S.separadosAlmacen-D.cantidad\n"
                            + "FROM movimientosDetalleAlmacen D\n"
                            + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                            + "INNER JOIN ordenCompraSurtido S ON S.idOrdenCompra=M.referencia AND S.idEmpaque=D.idEmpaque\n"
                            + "WHERE D.idMovtoAlmacen=" + toMov.getIdMovtoAlmacen();
                    st.executeUpdate(strSQL);

                    strSQL = "SELECT idEmpaque\n"
                            + "FROM ordenCompraSurtido\n"
                            + "WHERE idOrdenCompra=" + toMov.getReferencia() + "\n"
                            + "         AND (surtidosAlmacen < cantOrdenada + cantOrdenadaSinCargo)";
                    ResultSet rs = st.executeQuery(strSQL);
                    if (!rs.next()) {
                        strSQL = "UPDATE ordenCompra SET estadoAlmacen=7, fechaCierreAlmacen=GETDATE() WHERE idOrdenCompra=" + toMov.getReferencia();
                        st.executeUpdate(strSQL);
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

    public void cerrarOrdenDeCompra(int idOrdenDeCompra) throws SQLException {
        String strSQL = "UPDATE ordenCompra SET estadoAlmacen=6, fechaCierreAlmacen=GETDATE() WHERE idOrdenCompra=" + idOrdenDeCompra;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);
            }
        }
    }

    public void inicializarCompra(TOMovimientoAlmacen toMov) throws SQLException {
        String strSQL = "";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "UPDATE S\n"
                        + "SET separadosAlmacen=S.separadosAlmacen-D.cantidad\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN ordenCompraSurtido S ON S.idOrdenCompra=M.referencia AND S.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovtoAlmacen=" + toMov.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalleAlmacen WHERE idMovtoAlmacen=" + toMov.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosAlmacen SET referencia=0 WHERE idMovtoAlmacen=" + toMov.getIdMovtoAlmacen();
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

    public void liberar(int idMovtoAlmacen, int idEmpaque, String lote, double cantLiberar, int idOrdenCompra) throws SQLException {
        String strSQL = "";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                if (idOrdenCompra != 0) {
                    strSQL = "UPDATE ordenCompraSurtido\n"
                            + "SET separadosAlmacen=separadosAlmacen-" + cantLiberar + "\n"
                            + "WHERE idOrdenCompra=" + idOrdenCompra + " AND idEmpaque=" + idEmpaque;
                    st.executeUpdate(strSQL);
                }
                strSQL = "UPDATE movimientosDetalleAlmacen\n"
                        + "SET cantidad=cantidad-" + cantLiberar + "\n"
                        + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idEmpaque + " AND lote='" + lote + "'";
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

    public double separar(int idMovtoAlmacen, int idEmpaque, String lote, double cantSeparar, int idOrdenCompra) throws SQLException {
        String strSQL = "";
        double disponibles = 0;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                if (idOrdenCompra != 0) {
                    strSQL = "SELECT cantOrdenada + cantOrdenadaSinCargo - surtidosAlmacen - separadosAlmacen AS disponibles\n"
                            + "FROM ordenCompraSurtido \n"
                            + "WHERE idOrdenCompra=" + idOrdenCompra + " AND idEmpaque=" + idEmpaque;
                    ResultSet rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        disponibles = rs.getDouble("disponibles");
                    }
                    if (disponibles < cantSeparar) {
                        cantSeparar = disponibles;
                    }
                    strSQL = "UPDATE ordenCompraSurtido\n"
                            + "SET separadosAlmacen=separadosAlmacen+" + cantSeparar + "\n"
                            + "WHERE idOrdenCompra=" + idOrdenCompra + " AND idEmpaque=" + idEmpaque;
                    st.executeUpdate(strSQL);
                }
                strSQL = "UPDATE movimientosDetalleAlmacen\n"
                        + "SET cantidad=cantidad+" + cantSeparar + "\n"
                        + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idEmpaque + " AND lote='" + lote + "'";
                st.executeUpdate(strSQL);

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

    public ArrayList<TOProductoCompraAlmacen> cargarOrdenDeCompraDetalle(int idOrdenCompra, int idMovtoAlmacen) throws SQLException {
        String strSQL = "";
        ArrayList<TOProductoCompraAlmacen> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "SELECT TOP 1 * FROM ordenCompraSurtido WHERE idOrdenCompra=" + idOrdenCompra;
                ResultSet rs = st.executeQuery(strSQL);
                if (!rs.next()) {
                    strSQL = "INSERT INTO ordenCompraSurtido (idOrdenCompra, idEmpaque, cantOrdenada, cantOrdenadaSinCargo, costoOrdenado, surtidosOficina, separadosOficina, surtidosOficinaSinCargo, separadosOficinaSinCargo, surtidosAlmacen, separadosAlmacen)\n"
                            + "SELECT idOrdenCompra, idEmpaque, cantOrdenada, cantOrdenadaSinCargo, costoOrdenado, 0, 0, 0, 0, 0, 0\n"
                            + "FROM ordenCompraDetalle\n"
                            + "WHERE idOrdenCompra=" + idOrdenCompra;
                    st.executeUpdate(strSQL);
                }
                strSQL = "UPDATE movimientosAlmacen SET referencia=" + idOrdenCompra + " WHERE idMovtoAlmacen=" + idMovtoAlmacen;
                st.executeUpdate(strSQL);

                strSQL = "INSERT INTO movimientosDetalleAlmacen\n"
                        + "SELECT " + idMovtoAlmacen + " AS idMovtoAlmacen, S.idEmpaque, '' AS lote\n"
                        + "	, CASE WHEN DA.cantidad IS NULL \n"
                        + "				THEN (S.cantOrdenada-S.surtidosAlmacen-S.separadosAlmacen)+(S.cantOrdenadaSinCargo-S.surtidosOficinaSinCargo-S.separadosOficinaSinCargo)\n"
                        + "			WHEN DA.cantidad+S.surtidosOficina+S.surtidosOficinaSinCargo+S.separadosOficina+S.separadosOficinaSinCargo > S.cantOrdenada+S.cantOrdenadaSinCargo\n"
                        + "				THEN S.cantOrdenada-S.surtidosAlmacen-S.separadosAlmacen\n"
                        + "			ELSE DA.cantidad END AS cantidad, '', 0\n"
                        + "FROM (SELECT M.referencia, D.idEmpaque, SUM(D.cantFacturada+D.cantSinCargo) AS cantidad\n"
                        + "	FROM movimientosDetalle D\n"
                        + "	INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "	INNER JOIN movimientosAlmacen MA ON MA.idAlmacen=M.idAlmacen AND MA.idTipo=M.idTipo AND MA.idReferencia=M.idReferencia AND MA.referencia=M.referencia AND MA.idComprobante=M.idComprobante\n"
                        + "    WHERE MA.idMovtoAlmacen=" + idMovtoAlmacen + "\n"
                        + "	GROUP BY M.referencia, D.idEmpaque) DA\n"
                        + "RIGHT JOIN ordenCompraSurtido S ON S.idOrdenCompra=DA.referencia AND S.idEmpaque=DA.idEmpaque\n"
                        + "WHERE S.idOrdenCompra=" + idOrdenCompra;
                st.executeUpdate(strSQL);

                strSQL = "UPDATE S\n"
                        + "SET separadosAlmacen=S.separadosAlmacen+D.cantidad\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN ordenCompraSurtido S ON S.idOrdenCompra=M.referencia\n"
                        + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen;
                st.executeUpdate(strSQL);

                detalle = this.obtenCompraDetalle(cn, idMovtoAlmacen);

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

    private TOProductoCompraAlmacen construirProductoCompra(ResultSet rs) throws SQLException {
        TOProductoCompraAlmacen toProd = new TOProductoCompraAlmacen();
        toProd.setCantOrdenada(rs.getDouble("cantOrdenada"));
        toProd.setIdMovtoAlmacen(rs.getInt("idMovtoAlmacen"));
        toProd.setIdProducto(rs.getInt("idEmpaque"));
        toProd.setLote(rs.getString("lote"));
        toProd.setCantidad(rs.getDouble("cantidad"));
        return toProd;
    }

    private ArrayList<TOProductoCompraAlmacen> obtenCompraDetalle(Connection cn, int idMovtoAlmacen) throws SQLException {
        String strSQL = "";
        ArrayList<TOProductoCompraAlmacen> detalle = new ArrayList<>();
        try (Statement st = cn.createStatement()) {
            strSQL = "SELECT D.*, ISNULL(S.cantOrdenada, 0)+ISNULL(S.cantOrdenadaSinCargo, 0) AS cantOrdenada\n"
                    + "FROM movimientosDetalleAlmacen D\n"
                    + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                    + "LEFT JOIN ordenCompraSurtido S ON S.idOrdenCompra=M.referencia AND S.idEmpaque=D.idEmpaque\n"
                    + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen;
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                detalle.add(this.construirProductoCompra(rs));
            }
        }
        return detalle;
    }
    
    public ArrayList<TOProductoCompraAlmacen> obtenerCompraDetalle(int idMovtoAlmacen) throws SQLException {
        ArrayList<TOProductoCompraAlmacen> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            detalle = this.obtenCompraDetalle(cn, idMovtoAlmacen);
        }
        return detalle;
    }

//    public ArrayList<TOProductoCompraAlmacen> cargarCompraDetalle(int idMovtoAlmacen) throws SQLException {
//        ArrayList<TOProductoCompraAlmacen> detalle = new ArrayList<>();
//        try (Connection cn = this.ds.getConnection()) {
//            cn.setAutoCommit(false);
//            try {
//                detalle = this.obtenCompraDetalle(cn, idMovtoAlmacen);
//                cn.commit();
//            } catch (SQLException ex) {
//                cn.rollback();
//                throw ex;
//            } finally {
//                cn.setAutoCommit(true);
//            }
//        }
//        return detalle;
//    }

//    private Lote construirLote(ResultSet rs) throws SQLException {
//        Lote lote = new Lote();
//        lote.setLote(rs.getString("lote"));
//        lote.setCantidad(rs.getDouble("cantidad"));
//        lote.setFechaCaducidad(new java.util.Date(rs.getDate("fechaCaducidad").getTime()));
//        return lote;
//    }

    public ProductoAlmacen obtenerLoteHoy(int dias) throws SQLException {
        ProductoAlmacen lote = null;
        String strSQL = "SELECT 0 AS idMovtoAlmacen, 0 AS idEmpaque, lote, 0 AS cantidad, DATEADD(DAY, " + dias + ", fecha) AS fechaCaducidad\n"
                + "FROM lotes\n"
                + "WHERE fecha=CONVERT(DATE, GETDATE())";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    lote = movimientos.Movimientos.construirLote(rs);
                } else {
                    throw new SQLException("No hay lote creado con la fecha de hoy !!!");
                }
            }
        }
        return lote;
    }

    public void agregarProductoAlmacen(TOProductoAlmacen toProd) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            movimientos.Movimientos.agregaProductoAlmacen(cn, toProd);
        }
    }
}
