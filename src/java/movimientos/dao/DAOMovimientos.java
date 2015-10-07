package movimientos.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOMovimientos {

    int idUsuario, idCedis;
    private DataSource ds = null;

    public DAOMovimientos() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
        this.idUsuario = usuarioSesion.getUsuario().getId();
        this.idCedis = usuarioSesion.getUsuario().getIdCedis();

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
    }

    public void liberar(int idMovto, int idMovtoAlmacen, int idAlmacen, int idProducto, double cantSolicitada) throws SQLException {
        String lote;
        double cantLiberar;
        double cantLiberada = 0;
        String strSQL = "SELECT D.lote, D.cantidad, ISNULL(A.separados, 0) AS separados\n"
                + "FROM movimientosDetalleAlmacen D\n"
                + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                + "LEFT JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen + " AND D.idEmpaque=" + idProducto + "\n"
                + "ORDER BY A.fechaCaducidad DESC";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement(); Statement st1 = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    lote = rs.getString("lote");
                    if (rs.getDouble("separados") < rs.getDouble("cantidad")) {
                        throw new SQLException("Inconsistencia: lote separados menor que producto cantidad. Producto (id=" + idProducto + "), lote: " + lote + " !!!");
                    }
                    cantLiberar = cantSolicitada;
                    if (rs.getDouble("cantidad") <= cantSolicitada) {
                        cantLiberar = rs.getDouble("cantidad");

                        strSQL = "DELETE FROM movimientosDetalleAlmacen\n"
                                + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                    } else {
                        strSQL = "UPDATE movimientosDetalleAlmacen\n"
                                + "SET cantidad=cantidad-" + cantLiberar + "\n"
                                + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                    }
                    st1.executeUpdate(strSQL);

                    strSQL = "UPDATE almacenesLotes\n"
                            + "SET separados=separados-" + cantLiberar + "\n"
                            + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                    st1.executeUpdate(strSQL);

                    cantSolicitada -= cantLiberar;
                    cantLiberada += cantLiberar;
                    if (cantSolicitada == 0) {
                        break;
                    }
                }
                if (cantSolicitada != 0) {
                    throw new SQLException("Inconsistencia de separados para liberar producto (id=" + idProducto + ") almacen !!!");
                } else {
                    strSQL = "SELECT separados\n"
                            + "FROM almacenesEmpaques\n"
                            + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto;
                    rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        if (rs.getDouble("separados") < cantLiberada) {
                            throw new SQLException("Inconsistencia de separados para liberar en el producto (id=" + idProducto + ") oficina !!!");
                        }
                        strSQL = "UPDATE almacenesEmpaques\n"
                                + "SET separados=separados-" + cantLiberada + "\n"
                                + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto;
                        st1.executeUpdate(strSQL);

                        strSQL = "UPDATE movimientosDetalle\n"
                                + "SET cantFacturada=cantFacturada-" + cantLiberada + "\n"
                                + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + idProducto;
                        st.executeUpdate(strSQL);
                    } else {
                        throw new SQLException("No se encontro producto (id=" + idProducto + ") en almacenesEmpaques !!!");
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

    private void separa(Connection cn, int idMovto, int idMovtoAlmacen, int idAlmacen, int idProducto, double cantSolicitada) throws SQLException {
        String strSQL = "";
        try (Statement st = cn.createStatement(); Statement st1 = cn.createStatement()) {
            strSQL = "UPDATE almacenesEmpaques\n"
                    + "SET separados=separados+" + cantSolicitada + "\n"
                    + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto;
            st1.executeUpdate(strSQL);

            strSQL = "UPDATE movimientosDetalle\n"
                    + "SET cantFacturada=cantFacturada+" + cantSolicitada + "\n"
                    + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + idProducto;
            st1.executeUpdate(strSQL);

            int n;
            String lote;
            double cantSeparar;
            strSQL = "SELECT lote, existencia-separados AS disponibles\n"
                    + "FROM almacenesLotes\n"
                    + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + " AND existencia > separados\n"
                    + "ORDER BY fechaCaducidad";
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                lote = rs.getString("lote");
                cantSeparar = rs.getDouble("disponibles");
                if (cantSolicitada < cantSeparar) {
                    cantSeparar = cantSolicitada;
                }
                strSQL = "UPDATE almacenesLotes\n"
                        + "SET separados=separados+" + cantSeparar + "\n"
                        + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                st1.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosDetalleAlmacen\n"
                        + "SET cantidad=cantidad+" + cantSeparar + "\n"
                        + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                n = st1.executeUpdate(strSQL);
                if (n == 0) {
                    strSQL = "INSERT INTO movimientosDetalleAlmacen (idMovtoAlmacen, idEmpaque, lote, cantidad, fecha, existenciaAnterior)\n"
                            + "VALUES (" + idMovtoAlmacen + ", " + idProducto + ", '" + lote + "', " + cantSeparar + ", '', 0)";
                    st.executeUpdate(strSQL);
                }
                cantSolicitada -= cantSeparar;
                if (cantSolicitada == 0) {
                    break;
                }
            }
        }
    }

    public double separar(int idMovto, int idMovtoAlmacen, int idAlmacen, int idProducto, double cantSolicitada, boolean total) throws SQLException {
        double disponibles;
        String strSQL = "SELECT E.idAlmacen, E.idEmpaque\n"
                + "     , E.existencia-E.separados AS disponiblesOficina\n"
                + "     , L.disponibles AS disponiblesAlmacen\n"
                + "FROM (SELECT idAlmacen, idEmpaque, SUM(existencia-separados) AS disponibles\n"
                + "		FROM almacenesLotes\n"
                + "		WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + "\n"
                + "		GROUP BY idAlmacen, idEmpaque) L\n"
                + "INNER JOIN almacenesEmpaques E ON E.idAlmacen=L.idAlmacen AND E.idEmpaque=L.idEmpaque";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    disponibles = rs.getDouble("disponiblesOficina");
                    if (rs.getDouble("disponiblesAlmacen") < disponibles) {
                        disponibles = rs.getDouble("disponiblesAlmacen");
                    }
                } else {
                    throw new SQLException("No hay existencia disponible !!!");
                }
                if (disponibles > 0) {
                    if (disponibles < cantSolicitada) {
                        if(total) {
                            throw new SQLException("No hay existencia total disponible !!!");
                        }
                        cantSolicitada = disponibles;
                    }
                    this.separa(cn, idMovto, idMovtoAlmacen, idAlmacen, idProducto, cantSolicitada);
                } else {
                    throw new SQLException("No hay existencia disponible !!!");
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return disponibles;
    }
}
