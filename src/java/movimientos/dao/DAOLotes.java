package movimientos.dao;

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
import movimientos.dominio.Lote;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOLotes {

    private int diasCaducidad = 365;
    private DataSource ds = null;

    public DAOLotes() throws NamingException {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");

            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
        } catch (NamingException ex) {
            throw (ex);
        }
    }

    public void eliminarProductoSalidaAlmacen(int idMovtoAlmacen, int idProducto) throws SQLException {
        String strSQL = "SELECT M.idAlmacen, D.lote, D.cantidad\n"
                + "FROM movimientosDetalleAlmacen D\n"
                + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen + " AND D.idEmpaque=" + idProducto;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement();) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    if (rs.getDouble("cantidad") != 0) {
                        this.liberaLotes(cn, idMovtoAlmacen, rs.getInt("idAlmacen"), rs.getString("lote"), idProducto, rs.getDouble("cantidad"));
                    }
                }
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public boolean validaLote(Lote lote) throws SQLException {
        boolean ok = false;
        String strSQL = "SELECT DATEADD(DAY, 365, convert(date, fecha)) AS fecha FROM lotes WHERE lote='" + lote.getLote().substring(0, 4) + "'";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    lote.setFechaCaducidad(new java.util.Date(rs.getDate("fecha").getTime()));
                    ok=true;
                }
            }
        }
        return ok;
    }

    public void agregarLoteEntradaAlmacen(int idMovtoAlmacen, Lote l) throws SQLException {
        String strSQL = "INSERT INTO movimientosDetalleAlmacen (idMovtoAlmacen, idEmpaque, lote, cantidad, fecha, existenciaAnterior) "
                + "VALUES (" + idMovtoAlmacen + ", " + l.getIdProducto() + ", '" + l.getLote() + "', " + l.getCantidad() + ", GETDATE(), 0)";
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            st.executeUpdate(strSQL);
        } finally {
            cn.close();
        }
    }

    public void editarLoteEntradaAlmacen(int idMovtoAlmacen, Lote l) throws SQLException {
        String strSQL;
        if (l.getCantidad() == 0) {
            strSQL = "DELETE FROM movimientosDetalleAlmacen "
                    + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + l.getIdProducto() + " AND lote='" + l.getLote() + "'";
        } else if (l.getSeparados() == 0) {
            strSQL = "INSERT INTO movimientosDetalleAlmacen (idMovtoAlmacen, idEmpaque, lote, cantidad, fecha, existenciaAnterior) "
                    + "VALUES (" + idMovtoAlmacen + ", " + l.getIdProducto() + ", '" + l.getLote() + "', " + l.getCantidad() + ", GETDATE(), 0)";
        } else {
            strSQL = "UPDATE movimientosDetalleAlmacen "
                    + "SET cantidad=" + l.getCantidad() + " "
                    + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + l.getIdProducto() + " AND lote='" + l.getLote() + "'";
        }
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            st.executeUpdate(strSQL);
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 2627) {
                ex = new SQLException("El lote ya existe !!");
            }
            throw ex;
        } finally {
            cn.close();
        }
    }

    public ArrayList<Lote> obtenerLotesKardex(int idMovto, int idProducto) throws SQLException {
        Lote lote;
        String strSQL = "SELECT K.*, COALESCE(DATEADD(DAY, " + this.diasCaducidad + ",L.fecha), DATEADD(DAY, -1, CONVERT(DATETIME, DATEDIFF(DAY, 0, GETDATE()), 102))) AS fechaCaducidad "
                + "FROM movimientosDetalleAlmacen K "
                + "LEFT JOIN lotes L ON L.lote=K.lote "
                + "WHERE idMovtoAlmacen=" + idMovto + " AND idEmpaque=" + idProducto;
        ArrayList<Lote> lotes = new ArrayList<>();
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                lote = new Lote();
                lote.setIdAlmacen(rs.getInt("idAlmacen"));
                lote.setIdProducto(rs.getInt("idEmpaque"));
                lote.setLote(rs.getString("lote"));
                lote.setSaldo(0);
                lote.setCantidad(rs.getDouble("cantidad"));
                lote.setSeparados(rs.getDouble("cantidad"));
                lote.setFechaCaducidad(rs.getDate("fechaCaducidad"));
                lotes.add(lote);
            }
        } finally {
            cn.close();
        }
        return lotes;
    }

    public void liberarOficina(int idMovto, int idAlmacen, int idProducto, double liberar) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                this.liberaOficina(cn, idMovto, idAlmacen, idProducto, liberar);
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void libera(int idMovto, int idMovtoAlmacen, Lote lote, double cantidad) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                this.liberaLotes(cn, idMovtoAlmacen, lote.getIdAlmacen(), lote.getLote(), lote.getIdProducto(), cantidad);
                this.liberaOficina(cn, idMovto, lote.getIdAlmacen(), lote.getIdProducto(), cantidad);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private void liberaOficina(Connection cn, int idMovto, int idAlmacen, int idProducto, double liberar) throws SQLException {
        String strSQL = "SELECT * FROM almacenesEmpaques WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto;
        try (Statement st = cn.createStatement()) {
            double separados = 0;
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                separados = rs.getDouble("separados");
                if (separados < liberar) {
                    throw new SQLException("No se puede liberar mas que lo separado !!!");
                }
            } else {
                throw new SQLException("No se encontro el empaque en almacen !!!");
            }
            strSQL = "UPDATE almacenesEmpaques SET separados=separados-" + liberar + " "
                    + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto;
            st.executeUpdate(strSQL);

            strSQL = "UPDATE movimientosDetalle SET cantFacturada=cantFacturada-" + liberar + " "
                    + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + idProducto;
            st.executeUpdate(strSQL);
        }
    }

    public void liberaAlmacen(int idMovtoAlmacen, Lote lote, double liberar) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                this.liberaLotes(cn, idMovtoAlmacen, lote.getIdAlmacen(), lote.getLote(), lote.getIdProducto(), liberar);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        } 
    }

    private void liberaLotes(Connection cn, int idMovtoAlmacen, int idAlmacen, String lote, int idProducto, double liberar) throws SQLException {
        String strSQL = "SELECT * FROM almacenesLotes "
                + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
        try (Statement st = cn.createStatement()) {
            double separados = 0;
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                separados = rs.getDouble("separados");
                if (separados < liberar) {
                    throw new SQLException("No se pueden liberar mas lotes que los separados !!!");
                }
            } else {
                throw new SQLException("No se encontro el lote en almacen !!!");
            }
            strSQL = "UPDATE almacenesLotes SET separados=separados-" + liberar + " "
                    + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
            st.executeUpdate(strSQL);

            if (separados == liberar) {
                strSQL = "DELETE FROM movimientosDetalleAlmacen "
                        + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
            } else {
                strSQL = "UPDATE movimientosDetalleAlmacen SET cantidad=cantidad-" + liberar + " "
                        + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
            }
            st.executeUpdate(strSQL);
        }
    }

    private void liberaLotes1(int idMovtoAlmacen, int idAlmacen, String lote, int idProducto, double liberar) throws SQLException {
        String strSQL = "SELECT * FROM almacenesLotes "
                + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                double separados = 0;
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    separados = rs.getDouble("separados");
                    if (separados < liberar) {
                        throw new SQLException("No se pueden liberar mas lotes que los separados !!!");
                    }
                } else {
                    throw new SQLException("No se encontro el lote en almacen !!!");
                }
                strSQL = "UPDATE almacenesLotes SET separados=separados-" + liberar + " "
                        + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                st.executeUpdate(strSQL);

                if (separados == liberar) {
                    strSQL = "DELETE FROM movimientosDetalleAlmacen "
                            + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                } else {
                    strSQL = "UPDATE movimientosDetalleAlmacen SET cantidad=cantidad-" + liberar + " "
                            + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                }
                st.executeUpdate(strSQL);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                cn.setAutoCommit(true);
                throw ex;
            }
            cn.setAutoCommit(true);
        }
    }

    public double separarOficina(int idMovto, int idAlmacen, int idProducto, double cantidad, boolean exacto) throws SQLException {
        double cantOficina = 0;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                cantOficina = this.separaOficina(cn, idMovto, idAlmacen, idProducto, cantidad, exacto);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return cantOficina;
    }

    public double separa(int idMovto, int idMovtoAlmacen, Lote lote, double cantidad, boolean exacto) throws SQLException {
        double cantAlmacen, cantOficina;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                cantAlmacen = this.separaLotes(idMovtoAlmacen, lote.getIdAlmacen(), lote.getIdProducto(), lote.getLote(), cantidad, exacto);
                cantOficina = this.separaOficina(cn, idMovto, lote.getIdAlmacen(), lote.getIdProducto(), cantidad, exacto);
                if (!exacto && cantAlmacen != cantOficina) {
                    if (cantAlmacen < cantOficina) {
                        throw new SQLException("No hay suficientes lotes en almacen !!!");
                    } else {
                        throw new SQLException("No hay existencia suficiente !!!");
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
        return cantidad;
    }

    public double separaAlmacen(int idMovtoAlmacen, Lote lote, double cantidad, boolean exacto) throws SQLException {
        return this.separaLotes(idMovtoAlmacen, lote.getIdAlmacen(), lote.getIdProducto(), lote.getLote(), cantidad, exacto);
    }

    private double separaLotes(int idMovtoAlmacen, int idAlmacen, int idProducto, String lote, double cantidad, boolean exacto) throws SQLException {
        String strSQL = "SELECT saldo-separados AS saldo "
                + "FROM almacenesLotes "
                + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    if (rs.getDouble("saldo") <= 0) {
                        cantidad = 0;
                        throw new SQLException("No hay existencia !!!");
                    } else if (rs.getDouble("saldo") < cantidad) {
                        if (exacto) {
                            throw new SQLException("No hay lotes suficientes en almacen !!!");
                        }
                        cantidad = rs.getDouble("saldo");
                    }
                } else {
                    throw new SQLException("No hay existencia, No existe lote en almacen !!!");
                }
                strSQL = "UPDATE almacenesLotes SET separados=separados+" + cantidad + " "
                        + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosDetalleAlmacen SET cantidad=cantidad+" + cantidad + " "
                        + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                if (st.executeUpdate(strSQL) == 0) {
                    strSQL = "INSERT INTO movimientosDetalleAlmacen (idMovtoAlmacen, idEmpaque, lote, cantidad, fecha, existenciaAnterior) "
                            + "VALUES(" + idMovtoAlmacen + ", " + idProducto + ", '" + lote + "', " + cantidad + ", GETDATE(), 0)";
                    st.executeUpdate(strSQL);
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                cn.setAutoCommit(true);
                throw ex;
            }
            cn.setAutoCommit(true);
        }
        return cantidad;
    }

    private double separaOficina(Connection cn, int idMovto, int idAlmacen, int idProducto, double cantidad, boolean exacto) throws SQLException {
        String strSQL = "SELECT existenciaOficina-separados AS saldo "
                + "FROM almacenesEmpaques "
                + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                if (rs.getDouble("saldo") <= 0) {
                    cantidad = 0;
                    throw new SQLException("No hay existencia !!!");
                } else if (rs.getDouble("saldo") < cantidad) {
                    if (exacto) {
                        throw new SQLException("No hay existencia suficiente !!!");
                    }
                    cantidad = rs.getDouble("saldo");
                }
            } else {
                throw new SQLException("No hay existencia, no existe lote en almacen !!!");
            }
            strSQL = "UPDATE almacenesEmpaques SET separados=separados+" + cantidad + " "
                    + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto;
            st.executeUpdate(strSQL);

            strSQL = "UPDATE movimientosDetalle SET cantFacturada=cantFacturada+" + cantidad + " "
                    + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + idProducto;
            st.executeUpdate(strSQL);
        }
        return cantidad;
    }
    
    private Lote construirLotesMovtoEmpaque(ResultSet rs) throws SQLException {
        Lote lote = new Lote();
        lote.setIdAlmacen(rs.getInt("idAlmacen"));
        lote.setIdProducto(rs.getInt("idEmpaque"));
        lote.setLote(rs.getString("lote"));
        lote.setSaldo(rs.getDouble("saldo"));
        lote.setCantidad(rs.getDouble("cantidad"));
        lote.setSeparados(rs.getDouble("separados"));
        lote.setFechaCaducidad(new java.util.Date(rs.getDate("fechaCaducidad").getTime()));
        return lote;
    }

    public ArrayList<Lote> obtenerLotesMovtoEmpaque(int idMovtoAlmacen, int idProducto) throws SQLException {
        String strSQL = "SELECT M.idAlmacen, K.idEmpaque, K.lote, K.cantidad AS saldo"
                + "     , K.cantidad, K.cantidad AS separados, L.fechaCaducidad "
                + "FROM movimientosDetalleAlmacen K "
                + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=K.idMovtoAlmacen "
                + "INNER JOIN almacenesLotes L ON L.idAlmacen=M.idAlmacen AND L.idEmpaque=K.idEmpaque AND L.idLote=K.idLote "
                + "WHERE K.idMovtoAlmacen=" + idMovtoAlmacen + " AND K.idEmpaque=" + idProducto;
        ArrayList<Lote> lotes = new ArrayList<>();
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                lotes.add(this.construirLotesMovtoEmpaque(rs));
            }
        } finally {
            cn.close();
        }
        return lotes;
    }
    
    private Lote construir(ResultSet rs) throws SQLException {
        Lote lote = new Lote();
        lote.setIdAlmacen(rs.getInt("idAlmacen"));
        lote.setIdProducto(rs.getInt("idEmpaque"));
        lote.setLote(rs.getString("lote"));
        lote.setSaldo(rs.getDouble("saldo"));
        lote.setCantidad(rs.getDouble("cantidad"));
        lote.setSeparados(rs.getDouble("cantidad"));
        lote.setFechaCaducidad(new java.util.Date(rs.getDate("fechaCaducidad").getTime()));
        return lote;
    }

    public ArrayList<Lote> obtenerLotes(int idAlmacen, int idMovtoAlmacen, int idProducto) throws SQLException {
        ArrayList<Lote> lotes = new ArrayList<>();
        String strSQL = "SELECT L.idAlmacen, L.idEmpaque, L.lote, L.fechaCaducidad, L.saldo-L.separados AS saldo\n"
                + "	, ISNULL(D.cantidad, 0) AS cantidad\n"
                + "FROM (SELECT M.idAlmacen, D.idEmpaque, D.lote, D.cantidad\n"
                + "		FROM movimientosDetalleAlmacen D\n"
                + "		inner join movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                + "		WHERE D.idMovtoAlmacen=" + idMovtoAlmacen + ") D\n"
                + "RIGHT JOIN almacenesLotes L ON L.idAlmacen=D.idAlmacen AND L.idEmpaque=D.idEmpaque AND L.lote=D.lote\n"
                + "WHERE L.idAlmacen="+idAlmacen+" AND L.idEmpaque=" + idProducto + " AND (L.saldo-L.separados > 0 OR ISNULL(D.cantidad, 0) > 0)";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    lotes.add(this.construir(rs));
                }
            }
        }
        return lotes;
    }
}
