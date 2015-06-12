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
    private String strSQL;
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

    public void agregarLoteEntradaAlmacen(int idMovto, Lote l) throws SQLException {
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            this.strSQL = "INSERT INTO movimientosDetalleAlmacen (idAlmacen, idMovto, idEmpaque, lote, cantidad, SUMA, fecha, existenciaAnterior) "
                    + "VALUES (" + l.getIdAlmacen() + ", " + idMovto + ", " + l.getIdProducto() + ", '" + l.getLote() + "', " + l.getCantidad() + ", 1, GETDATE(), 0)";
            st.executeUpdate(this.strSQL);
        } finally {
            cn.close();
        }
    }

    public void editarLoteEntradaAlmacen(int idMovto, Lote l) throws SQLException {
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            if (l.getCantidad() == 0) {
                this.strSQL = "DELETE FROM movimientosDetalleAlmacen "
                        + "WHERE idMovto=" + idMovto + " AND lote='" + l.getLote() + "'";
            } else if (l.getSeparados() == 0) {
                this.strSQL = "INSERT INTO movimientosDetalleAlmacen (idAlmacen, idMovto, idEmpaque, lote, cantidad, SUMA, fecha, existenciaAnterior) "
                        + "VALUES (" + l.getIdAlmacen() + ", " + idMovto + ", " + l.getIdProducto() + ", '" + l.getLote() + "', " + l.getCantidad() + ", 1, GETDATE(), 0)";
            } else {
                this.strSQL = "UPDATE movimientosDetalleAlmacen "
                        + "SET lote='" + l.getLote() + "', cantidad=" + l.getCantidad() + " "
                        + "WHERE idMovto=" + idMovto + " AND lote='" + l.getLote() + "'";
            }
            st.executeUpdate(this.strSQL);
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
        ArrayList<Lote> lotes = new ArrayList<>();
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            this.strSQL = "SELECT K.*, COALESCE(DATEADD(DAY, " + this.diasCaducidad + ",L.fecha), DATEADD(DAY, -1, CONVERT(DATETIME, DATEDIFF(DAY, 0, GETDATE()), 102))) AS fechaCaducidad "
                    + "FROM movimientosDetalleAlmacen K "
                    + "LEFT JOIN lotes L ON L.lote=K.lote "
                    + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + idProducto;
            ResultSet rs = st.executeQuery(this.strSQL);
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
    
    public void libera(int idMovto, int idMovtoAlmacen, Lote lote, double cantidad) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                this.liberaLotesAlmacen(idMovtoAlmacen, lote.getIdAlmacen(), lote.getLote(), lote.getIdProducto(), cantidad);
                this.liberaOficina(idMovto, lote.getIdAlmacen(), lote.getIdProducto(), cantidad);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                cn.setAutoCommit(true);
                throw ex;
            }
            cn.setAutoCommit(true);
        }
    }

    private void liberaOficina(int idMovto, int idAlmacen, int idProducto, double liberar) throws SQLException {
        this.strSQL = "SELECT * FROM almacenesEmpaques WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                double separados = 0;
                ResultSet rs = st.executeQuery(this.strSQL);
                if (rs.next()) {
                    separados = rs.getDouble("separados");
                    if (separados < liberar) {
                        throw new SQLException("No se puede liberar mas que lo separado !!!");
                    }
                } else {
                    throw new SQLException("No se encontro el empaque en almacen !!!");
                }
                this.strSQL = "UPDATE almacenesEmpaques SET separados=separados-" + liberar + " "
                        + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto;
                st.executeUpdate(this.strSQL);

                this.strSQL = "UPDATE movimientosDetalle SET cantFacturada=cantFacturada-" + liberar + " "
                        + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + idProducto;
                st.executeUpdate(this.strSQL);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                cn.setAutoCommit(true);
                throw ex;
            }
            cn.setAutoCommit(true);
        }
    }
    
    public void liberaAlmacen(int idMovtoAlmacen, Lote lote, double liberar) throws SQLException {
        this.liberaLotesAlmacen(idMovtoAlmacen, lote.getIdAlmacen(), lote.getLote(), lote.getIdProducto(), liberar);
    }

    private void liberaLotesAlmacen(int idMovtoAlmacen, int idAlmacen, String lote, int idProducto, double liberar) throws SQLException {
        this.strSQL = "SELECT * FROM almacenesLotes WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                double separados = 0;
                ResultSet rs = st.executeQuery(this.strSQL);
                if (rs.next()) {
                    separados = rs.getDouble("separados");
                    if (separados < liberar) {
                        throw new SQLException("No se pueden liberar mas lotes que los separados !!!");
                    }
                } else {
                    throw new SQLException("No se encontro el lote en almacen !!!");
                }
                this.strSQL = "UPDATE almacenesLotes SET separados=separados-" + liberar + " "
                        + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                st.executeUpdate(this.strSQL);

                if (separados == liberar) {
                    this.strSQL = "DELETE FROM movimientosDetalleAlmacen "
                            + "WHERE idAlmacen=" + idAlmacen + " AND idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                } else {
                    this.strSQL = "UPDATE movimientosDetalleAlmacen SET cantidad=cantidad-" + liberar + " "
                            + "WHERE idAlmacen=" + idAlmacen + " AND idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                }
                st.executeUpdate(this.strSQL);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                cn.setAutoCommit(true);
                throw ex;
            }
            cn.setAutoCommit(true);
        }
    }

    public double separa(int idMovto, int idMovtoAlmacen, Lote lote, double cantidad, boolean exacto) throws SQLException {
        double cantAlmacen, cantOficina;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                cantAlmacen=this.separaLotesAlmacen(idMovtoAlmacen, lote.getIdAlmacen(), lote.getIdProducto(), lote.getLote(), cantidad, exacto);
                cantOficina=this.separaOficina(idMovto, lote.getIdAlmacen(), lote.getIdProducto(), cantidad, exacto);
                if(!exacto && cantAlmacen!=cantOficina) {
                    if(cantAlmacen < cantOficina) {
                        throw new SQLException("No hay suficientes lotes en almacen !!!");
                    } else {
                        throw new SQLException("No hay existencia suficiente !!!");
                    }
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
    
    public double separaAlmacen(int idMovtoAlmacen, Lote lote, double cantidad, boolean exacto) throws SQLException {
        return this.separaLotesAlmacen(idMovtoAlmacen, lote.getIdAlmacen(), lote.getIdProducto(), lote.getLote(), cantidad, exacto);
    }

    private double separaLotesAlmacen(int idMovtoAlmacen, int idAlmacen, int idProducto, String lote, double cantidad, boolean exacto) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                this.strSQL = "SELECT saldo-separados AS saldo "
                        + "FROM almacenesLotes "
                        + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                ResultSet rs = st.executeQuery(this.strSQL);
                if (rs.next()) {
                    if (rs.getDouble("saldo") <= 0) {
                        cantidad = 0;
                        throw new SQLException("No hay existencia !!!");
                    } else if (rs.getDouble("saldo") < cantidad) {
                        if(exacto) {
                            throw new SQLException("No hay lotes suficientes en almacen !!!");
                        }
                        cantidad = rs.getDouble("saldo");
                    }
                } else {
                    throw new SQLException("No hay existencia, No existe lote en almacen !!!");
                }
                this.strSQL = "UPDATE almacenesLotes SET separados=separados+" + cantidad + " "
                        + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                st.executeUpdate(this.strSQL);
                
                this.strSQL="SELECT cantidad FROM movimientosDetalleAlmacen "
                        + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                rs=st.executeQuery(this.strSQL);
                if(rs.next()) {
                    this.strSQL = "UPDATE movimientosDetalleAlmacen SET cantidad=cantidad+" + cantidad + " "
                            + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                } else {
                    this.strSQL = "INSERT INTO movimientosDetalleAlmacen (idAlmacen, idMovtoAlmacen, idEmpaque, lote, cantidad, suma, fecha, existenciaAnterior) "
                            + "VALUES(" + idAlmacen + ", " + idMovtoAlmacen + ", " + idProducto + ", '" + lote + "', " + cantidad + ", 0, GETDATE(), 0)";
                }
                st.executeUpdate(this.strSQL);
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

    private double separaOficina(int idMovto, int idAlmacen, int idProducto, double cantidad, boolean exacto) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                this.strSQL = "SELECT existenciaOficina-separados AS saldo "
                        + "FROM almacenesEmpaques "
                        + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto;
                ResultSet rs = st.executeQuery(this.strSQL);
                if (rs.next()) {
                    if (rs.getDouble("saldo") <= 0) {
                        cantidad = 0;
                        throw new SQLException("No hay existencia !!!");
                    } else if (rs.getDouble("saldo") < cantidad) {
                        if(exacto) {
                            throw new SQLException("No hay existencia suficiente !!!");
                        }
                        cantidad = rs.getDouble("saldo");
                    }
                } else {
                    throw new SQLException("No hay existencia, no existe lote en almacen !!!");
                }
                this.strSQL = "UPDATE almacenesEmpaques SET separados=separados+" + cantidad + " "
                        + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto;
                st.executeUpdate(this.strSQL);
                
                this.strSQL = "UPDATE movimientosDetalle SET cantFacturada=cantFacturada+" + cantidad + " "
                        + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + idProducto;
                st.executeUpdate(this.strSQL);
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

    public ArrayList<Lote> obtenerLotesMovtoEmpaque(int idMovtoAlmacen, int idProducto) throws SQLException {
        ArrayList<Lote> lotes = new ArrayList<>();
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
//            this.strSQL = "SELECT K.idEmpaque, K.lote, K.cantidad, 0 AS saldo, K.cantidad AS separados, A.fechaCaducidad "
//                    + "FROM movimientosDetalleAlmacen K "
//                    + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=K.idAlmacen AND A.idEmpaque=K.idEmpaque AND A.lote=K.lote "
//                    + "WHERE K.idAlmacen=" + idAlmacen + " AND K.idMovto=" + idMovto + " AND K.idEmpaque=" + idProducto;
            this.strSQL = "SELECT K.idEmpaque, K.lote, K.cantidad, 0 AS saldo, K.cantidad AS separados "
                    + "FROM movimientosDetalleAlmacen K "
                    + "WHERE K.idMovtoAlmacen=" + idMovtoAlmacen + " AND K.idEmpaque=" + idProducto;
            ResultSet rs = st.executeQuery(this.strSQL);
            while (rs.next()) {
                lotes.add(this.construirLotesMovtoEmpaque(rs));
            }
        } finally {
            cn.close();
        }
        return lotes;
    }

    private Lote construirLotesMovtoEmpaque(ResultSet rs) throws SQLException {
        Lote lote = new Lote();
        lote.setIdAlmacen(0);
        lote.setIdProducto(rs.getInt("idEmpaque"));
        lote.setLote(rs.getString("lote"));
        lote.setCantidad(rs.getDouble("cantidad"));
        lote.setSaldo(0);
        lote.setSeparados(rs.getDouble("separados"));
        lote.setFechaCaducidad(new java.util.Date());
        return lote;
    }

    public ArrayList<Lote> obtenerLotes(int idAlmacen, int idMovtoAlmacen, int idProducto) throws SQLException {
        ArrayList<Lote> lotes = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                this.strSQL = "SELECT idAlmacen, idEmpaque, lote, cantidad, saldo, separados, fechaCaducidad "
                        + "FROM almacenesLotes "
                        + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + " AND saldo > 0 "
                        + "ORDER BY lote";
                ResultSet rs = st.executeQuery(this.strSQL);
                while (rs.next()) {
                    lotes.add(this.construir(rs));
                }
                this.strSQL = "SELECT * FROM movimientosDetalleAlmacen "
                        + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idProducto;
                rs = st.executeQuery(this.strSQL);
                while (rs.next()) {
                    for (Lote l : lotes) {
                        if (l.getLote().equals(rs.getString("lote"))) {
                            l.setSeparados(rs.getDouble("cantidad"));
                            l.setCantidad(rs.getDouble("cantidad"));
                            break;
                        }
                    }
                }
            }
        }
        return lotes;
    }

    private Lote construir(ResultSet rs) throws SQLException {
        Lote lote = new Lote();
        lote.setIdAlmacen(rs.getInt("idAlmacen"));
        lote.setIdProducto(rs.getInt("idEmpaque"));
        lote.setLote(rs.getString("lote"));
        lote.setSaldo(rs.getDouble("saldo") - rs.getDouble("separados"));
        lote.setCantidad(0);
        lote.setSeparados(0);
        lote.setFechaCaducidad(new java.util.Date(rs.getDate("fechaCaducidad").getTime()));
        return lote;
    }
}
