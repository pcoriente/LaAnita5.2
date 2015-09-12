package entradas.dao;

import entradas.dominio.MovimientoProducto;
import movimientos.to.TOMovimientoOficina;
import movimientos.to1.TOMovimientoProducto;
import impuestos.dominio.ImpuestosProducto;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
import movimientos.to1.Lote1;
import movimientos.dominio.MovimientoTipo;
import entradas.to.TOEntradaProducto;
import movimientos.to.TOLote;
import movimientos.to.TOMovimientoAlmacenProducto;
import movimientos.to1.TOMovimientoAlmacenProducto1;
import mvEntradas.TOEntradaOficinaProducto;
import pedidos.to.TOPedido;
import pedidos.to.TOPedidoProducto;
import salidas.TOSalidaOficinaProducto;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc costo
 */
public class DAOMovimientos1 {

    int idUsuario;
    int idCedis;
    private DataSource ds = null;
    private Connection cnx;
//    private Statement stx;

    public DAOMovimientos1() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
        this.idUsuario = usuarioSesion.getUsuario().getId();
        this.idCedis = usuarioSesion.getUsuario().getIdCedis();

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
    }

    public void costoDeVenta() throws SQLException {
        double capas;
        String strSQL;
        int idEmpaque;
        ResultSet rsE, rsS;
        this.cnx = this.ds.getConnection();
        Statement stE = this.cnx.createStatement();
        Statement stS = this.cnx.createStatement();
        Statement st1 = this.cnx.createStatement();
        Statement st = this.cnx.createStatement();
        try {
            strSQL = "SELECT DISTINCT idEmpaque\n"
                    + "FROM costoVenta\n"
                    + "WHERE idMovtoVenta != 0 AND capas = 0\n"
                    + "ORDER BY idEmpaque";
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                idEmpaque = rs.getInt("idEmpaque");
                strSQL = "SELECT * \n"
                        + "FROM costoVenta\n"
                        + "WHERE idMovtoVenta = 0 AND capas != 0 AND idEmpaque = " + idEmpaque + "\n"
                        + "ORDER BY fecha";
                rsE = stE.executeQuery(strSQL);
                while (rsE.next()) {
                    capas = rsE.getDouble("capas");
                    strSQL = "SELECT * \n"
                            + "FROM costoVenta\n"
                            + "WHERE idMovtoVenta != 0 AND costo = 0 AND idEmpaque = " + idEmpaque + "\n"
                            + "ORDER BY fecha";
                    rsS = stS.executeQuery(strSQL);
                    try {
                        st1.executeUpdate("BEGIN TRANSACTION");
                        while (rsS.next() && capas != 0) {
                            if (capas < rsS.getDouble("capas")) {
                                // A la salida se le pone el id de la entrada, las capas y el costo de Venta
                                strSQL = "UPDATE costoVenta\n"
                                        + "SET idMovto=" + rsE.getInt("idMovto") + ", capas=" + capas + ", costo=" + rsE.getDouble("costo") + "\n"
                                        + "WHERE idMovtoVenta=" + rsS.getInt("idMovtoVenta") + " AND idEmpaque=" + idEmpaque + " AND idMovto=0";
                                st1.executeUpdate(strSQL);
                                // Se crea el nuevo registro de salida, ya que con los de esta entrada, no se asignaron todas las capas
                                capas = rsS.getDouble("capas") - capas;
                                strSQL = "INSERT INTO costoVenta (idMovtoVenta, idEmpaque, idMovto, fecha, capas, costo)\n"
                                        + "VALUES (" + rsS.getInt("idMovtoVenta") + ", " + idEmpaque + ", 0, '" + rsS.getDate("fecha") + "', " + capas + ", 0)";
                                st1.executeUpdate(strSQL);
                                // Se actualiza la entrada, diciendo que ya no tiene mas capas disponibles por asignar
                                strSQL = "UPDATE costoVenta\n"
                                        + "SET capas=0\n"
                                        + "WHERE idMovtoVenta=0 AND idEmpaque=" + idEmpaque + " AND idMovto=" + rsE.getInt("idMovto");
                                st1.executeUpdate(strSQL);

                                capas = 0;
                            } else {
                                // A la salida se le pone el id de la entrada, las capas y el costo de Venta
                                strSQL = "UPDATE costoVenta\n"
                                        + "SET idMovto=" + rsE.getInt("idMovto") + ", costo=" + rsE.getDouble("costo") + "\n"
                                        + "WHERE idMovtoVenta=" + rsS.getInt("idMovtoVenta") + " AND idEmpaque=" + idEmpaque + " AND idMovto=0";
                                st1.executeUpdate(strSQL);
                                // Se actualiza la entrada, restando las capas de la salida que asigno
                                strSQL = "UPDATE costoVenta\n"
                                        + "SET capas=capas-" + rsS.getDouble("capas") + "\n"
                                        + "WHERE idMovtoVenta=0 AND idEmpaque=" + idEmpaque + " AND idMovto=" + rsE.getInt("idMovto");
                                st1.executeUpdate(strSQL);

                                capas -= rsS.getDouble("capas");
                            }
                        }
                        st1.executeUpdate("COMMIT TRANSACTION");
                    } catch (SQLException e) {
                        st1.executeUpdate("ROLLBACK TRANSACTION");
                        throw e;
                    }
                    if (capas != 0) {
                        break;
                    }
                }
            }
        } finally {
            st1.close();
            stS.close();
            stE.close();
            st.close();
            this.cnx.close();
        }
    }

    public ArrayList<TOEntradaOficinaProducto> obtenerDetalleEntradaOficina(int idMovto) throws SQLException {
        ArrayList<TOEntradaOficinaProducto> lista = new ArrayList<TOEntradaOficinaProducto>();
        String strSQL = "SELECT idEmpaque, cantFacturada, unitario "
                + "FROM movimientosDetalle "
                + "WHERE idMovto=" + idMovto;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            TOEntradaOficinaProducto to;
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                to = new TOEntradaOficinaProducto();
                to.setIdProducto(rs.getInt("idEmpaque"));
                to.setCantidad(rs.getDouble("cantFacturada"));
                to.setCosto(rs.getDouble("unitario"));
                lista.add(to);
            }
        } finally {
            st.close();
            cn.close();
        }
        return lista;
    }

    public void cancelarEntradaOficina(int idMovto) throws SQLException {
        String strSQL;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");

            strSQL = "DELETE FROM movimientosDetalle where idMovto=" + idMovto;
            st.executeUpdate(strSQL);

            strSQL = "DELETE FROM movimientos WHERE idMovto=" + idMovto;
            st.executeUpdate(strSQL);

            st.executeUpdate("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            cn.close();
        }
    }

    public void grabarEntradaOficina(TOMovimientoOficina to) throws SQLException {
        String strSQL;
        int folio;
        this.cnx = this.ds.getConnection();
        Statement st = this.cnx.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");

            folio = this.obtenerMovimientoFolio(true, to.getIdAlmacen(), to.getIdTipo());

            strSQL = "UPDATE movimientos SET fecha=GETDATE(), estatus=1, folio=" + folio + ", idUsuario=" + this.idUsuario + " "
                    + "WHERE idMovto=" + to.getIdMovto();
            st.executeUpdate(strSQL);

            strSQL = "DELETE FROM movimientosDetalle WHERE idMovto=" + to.getIdMovto() + " AND cantFacturada=0";
            st.executeUpdate(strSQL);

            strSQL = "UPDATE d "
                    + "SET d.unitario=e.promedioPonderado, d.fecha=GETDATE(), d.existenciaAnterior=a.existenciaOficina "
                    + "FROM movimientosDetalle d "
                    + "INNER JOIN movimientos m ON m.idMovto=d.idMovto "
                    + "INNER JOIN almacenesEmpaques a ON a.idAlmacen=m.idAlmacen AND a.idEmpaque=d.idEmpaque "
                    + "INNER JOIN empresasEmpaques e ON e.idEmpaque=d.idEmpaque "
                    + "WHERE e.idEmpresa=m.idEmpresa AND d.idMovto=" + to.getIdMovto();
            st.executeUpdate(strSQL);

            strSQL = "UPDATE a "
                    + "SET a.existenciaOficina=a.existenciaOficina+d.cantFacturada "
                    + "FROM (SELECT m.idAlmacen, d.* "
                    + "		FROM movimientosDetalle d "
                    + "		INNER JOIN movimientos m ON m.idMovto=d.idMovto "
                    + "		WHERE d.idMovto=" + to.getIdMovto() + ") d "
                    + "INNER JOIN almacenesEmpaques a ON a.idAlmacen=d.idAlmacen AND a.idEmpaque=d.idEmpaque";
            st.executeUpdate(strSQL);

            strSQL = "UPDATE e "
                    + "SET e.existenciaOficina=e.existenciaOficina+d.cantFacturada "
                    + "FROM (SELECT m.idEmpresa, d.* "
                    + "		FROM movimientosDetalle d "
                    + "		INNER JOIN movimientos m ON m.idMovto=d.idMovto "
                    + "		WHERE d.idMovto=" + to.getIdMovto() + ") d "
                    + "INNER JOIN empresasEmpaques e ON e.idEmpresa=d.idEmpresa AND e.idEmpaque=d.idEmpaque";
            st.executeUpdate(strSQL);

            st.executeUpdate("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            this.cnx.close();
        }
    }

    public double actualizaEntrada(int idMovto, int idAlmacen, int idProducto, double cantidad) throws SQLException {
        String strSQL = "UPDATE movimientosDetalle "
                + "SET cantFacturada=" + cantidad + " "
                + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + idProducto;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();

        try {
            st.executeUpdate(strSQL);
        } finally {
            st.close();
            cn.close();
        }
        return cantidad;
    }

    public void agregarProductoEntradaOficina(int idMovto, TOEntradaOficinaProducto to) throws SQLException {
        String strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantOrdenada, cantFacturada, cantSinCargo, cantRecibida, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior) "
                + "VALUES (" + idMovto + ", " + to.getIdProducto() + ", 0, " + to.getCantidad() + ", 0, 0, 0, 0, 0, 0, 0, 0, GETDATE(), 0)";
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate(strSQL);
        } finally {
            st.close();
            cn.close();
        }
    }

    public void cancelarEntradaAlmacen(int idMovto) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        String strSQL;
        try {
            st.executeUpdate("BEGIN TRANSACTION");

            strSQL = "DELETE FROM movimientosAlmacenDetalle where idMovto=" + idMovto;
            st.executeUpdate(strSQL);

            strSQL = "DELETE FROM movimientosAlmacen WHERE idMovtoAlmacen=" + idMovto;
            st.executeUpdate(strSQL);

            st.executeUpdate("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            cn.close();
        }
    }

    public void grabarEntradaAlmacen(TOMovimientoOficina to) throws SQLException {
        int folio;
        double saldo;
        String strSQL;

        this.cnx = this.ds.getConnection();
        Statement st = this.cnx.createStatement();
        Statement st1 = this.cnx.createStatement();
        ResultSet rs, rs1;
        try {
            st.executeUpdate("BEGIN TRANSACTION");

            folio = this.obtenerMovimientoFolio(false, to.getIdAlmacen(), to.getIdTipo());

            strSQL = "UPDATE movimientosAlmacen SET fecha=GETDATE(), estatus=1, folio=" + folio + ", idUsuario=" + this.idUsuario + " "
                    + "WHERE idMovtoAlmacen=" + to.getIdMovto();
            st.executeUpdate(strSQL);

            strSQL = "SELECT * FROM movimientosAlmacenDetalle "
                    + "WHERE idMovto=" + to.getIdMovto() + " "
                    + "ORDER BY idEmpaque";
            rs = st.executeQuery(strSQL);
            while (rs.next()) {
                strSQL = "SELECT saldo FROM almacenesLotes "
                        + "WHERE idAlmacen=" + to.getIdAlmacen() + " AND idEmpaque=" + rs.getInt("idEmpaque") + " AND lote='" + rs.getString("lote") + "'";
                rs1 = st1.executeQuery(strSQL);
                if (rs1.next()) {
                    saldo = rs1.getDouble("saldo");
                    strSQL = "UPDATE almacenesLotes "
                            + "SET cantidad=cantidad+" + rs.getDouble("cantidad") + ", saldo=saldo+" + rs.getDouble("cantidad") + " "
                            + "WHERE idAlmacen=" + to.getIdAlmacen() + " AND idEmpaque=" + rs.getInt("idEmpaque") + " AND lote='" + rs.getString("lote") + "'";
                } else {
                    saldo = 0;
                    strSQL = "INSERT INTO almacenesLotes (idAlmacen, idEmpaque, lote, fechaCaducidad, cantidad, saldo, separados, existenciaFisica) "
                            + "VALUES (" + to.getIdAlmacen() + ", " + rs.getInt("idEmpaque") + ", '" + rs.getString("lote") + "', '" + rs.getDate("fecha") + "', " + rs.getDouble("cantidad") + ", " + rs.getDouble("cantidad") + ", 0, 0)";
                }
                st1.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosAlmacenDetalle "
                        + "SET fecha=GETDATE(), existenciaAnterior=" + saldo + " "
                        + "WHERE idMovto=" + to.getIdMovto() + " AND idEmpaque=" + rs.getInt("idEmpaque") + " AND lote='" + rs.getString("lote") + "'";
                st1.executeUpdate(strSQL);
            }
            st.executeUpdate("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            this.cnx.close();
        }
    }

    public ArrayList<TOMovimientoAlmacenProducto1> obtenerDetalleMovimientoAlmacen(int idMovto) throws SQLException {
        ArrayList<TOMovimientoAlmacenProducto1> lista = new ArrayList<TOMovimientoAlmacenProducto1>();
        String strSQL = "SELECT idEmpaque, SUM(cantidad) AS cantidad "
                + "FROM movimientosAlmacenDetalle k "
                + "WHERE idMovto=" + idMovto + " "
                + "GROUP BY idEmpaque";
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            TOMovimientoAlmacenProducto1 to;
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                to = new TOMovimientoAlmacenProducto1();
                to.setIdProducto(rs.getInt("idEmpaque"));
                to.setCantidad(rs.getDouble("cantidad"));
                lista.add(to);
            }
        } finally {
            st.close();
            cn.close();
        }
        return lista;
    }

    public void cancelarSalidaOficina(int idMovto) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        String strSQL;
        try {
            st.executeUpdate("BEGIN TRANSACTION");

            strSQL = "UPDATE e "
                    + "SET e.separados=e.separados-d.cantFacturada "
                    + "FROM (SELECT m.idAlmacen, d.* "
                    + "		FROM movimientosDetalle d "
                    + "		INNER JOIN movimientos m ON m.idMovto=d.idMovto "
                    + "		WHERE d.idMovto=" + idMovto + ") d "
                    + "INNER JOIN almacenesEmpaques e ON e.idAlmacen=d.idAlmacen AND e.idEmpaque=d.idEmpaque";
            st.executeUpdate(strSQL);

            strSQL = "DELETE FROM movimientosDetalle where idMovto=" + idMovto;
            st.executeUpdate(strSQL);

            strSQL = "DELETE FROM movimientos WHERE idMovto=" + idMovto;
            st.executeUpdate(strSQL);

            st.executeUpdate("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            cn.close();
        }
    }

    public ArrayList<TOSalidaOficinaProducto> obtenerDetalleSalidaOficina(int idAlmacen, int idMovto) throws SQLException {
        ArrayList<TOSalidaOficinaProducto> lista = new ArrayList<TOSalidaOficinaProducto>();
        String strSQL = "SELECT idEmpaque, cantFacturada, unitario "
                + "FROM movimientosDetalle "
                + "WHERE idMovto=" + idMovto;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            TOSalidaOficinaProducto to;
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                to = new TOSalidaOficinaProducto();
                to.setIdProducto(rs.getInt("idEmpaque"));
                to.setCantidad(rs.getDouble("cantFacturada"));
                to.setCosto(rs.getDouble("unitario"));
                lista.add(to);
            }
        } finally {
            st.close();
            cn.close();
        }
        return lista;
    }

    private int obtenerMovimientoFolio(boolean oficina, int idAlmacen, int idTipo) throws SQLException {
        int folio;
        String tabla = "movimientosFoliosAlmacen";
        if (oficina) {
            tabla = "movimientosFolios";
        }
        Statement st = this.cnx.createStatement();
        try {
            ResultSet rs = st.executeQuery("SELECT folio FROM " + tabla + " WHERE idAlmacen=" + idAlmacen + " AND idTipo=" + idTipo);
            if (rs.next()) {
                folio = rs.getInt("folio");
                st.executeUpdate("UPDATE " + tabla + " SET folio=folio+1 WHERE idAlmacen=" + idAlmacen + " AND idTipo=" + idTipo);
            } else {
                folio = 1;
                st.executeUpdate("INSERT INTO " + tabla + " (idAlmacen, idTipo, folio) VALUES (" + idAlmacen + ", " + idTipo + ", 2)");
            }
        } finally {
            st.close();
        }
        return folio;
    }

    public void grabarSalidaOficina(TOMovimientoOficina to) throws SQLException {
        int folio;
        String strSQL;

        this.cnx = this.ds.getConnection();
        Statement st = this.cnx.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");

            folio = this.obtenerMovimientoFolio(true, to.getIdAlmacen(), to.getIdTipo());

            strSQL = "UPDATE movimientos SET fecha=GETDATE(), estatus=1, folio=" + folio + ", idUsuario=" + this.idUsuario + " "
                    + "WHERE idMovto=" + to.getIdMovto();
            st.executeUpdate(strSQL);

            strSQL = "DELETE FROM movimientosDetalle WHERE idMovto=" + to.getIdMovto() + " AND cantFacturada=0";
            st.executeUpdate(strSQL);

            strSQL = "UPDATE d "
                    + "SET d.unitario=e.promedioPonderado, d.fecha=GETDATE(), d.existenciaAnterior=a.existenciaOficina "
                    + "FROM movimientosDetalle d "
                    + "INNER JOIN movimientos m ON m.idMovto=d.idMovto "
                    + "INNER JOIN almacenesEmpaques a ON a.idAlmacen=m.idAlmacen AND a.idEmpaque=d.idEmpaque "
                    + "INNER JOIN empresasEmpaques e ON e.idEmpaque=d.idEmpaque "
                    + "WHERE e.idEmpresa=m.idEmpresa AND d.idMovto=" + to.getIdMovto();
            st.executeUpdate(strSQL);

            strSQL = "UPDATE e "
                    + "SET e.separados=e.separados-d.cantFacturada, e.existenciaOficina=e.existenciaOficina-d.cantFacturada "
                    + "FROM (SELECT m.idAlmacen, d.* "
                    + "		FROM movimientosDetalle d "
                    + "		INNER JOIN movimientos m ON m.idMovto=d.idMovto "
                    + "		WHERE d.idMovto=" + to.getIdMovto() + ") d "
                    + "INNER JOIN almacenesEmpaques e ON e.idAlmacen=d.idAlmacen AND e.idEmpaque=d.idEmpaque";
            st.executeUpdate(strSQL);

            strSQL = "UPDATE e "
                    + "SET e.existenciaOficina=e.existenciaOficina-d.cantFacturada "
                    + "FROM (SELECT m.idEmpresa, d.* "
                    + "		FROM movimientosDetalle d "
                    + "		INNER JOIN movimientos m ON m.idMovto=d.idMovto "
                    + "		WHERE d.idMovto=" + to.getIdMovto() + ") d "
                    + "INNER JOIN empresasEmpaques e ON e.idEmpresa=d.idEmpresa AND e.idEmpaque=d.idEmpaque";
            st.executeUpdate(strSQL);

            strSQL = "UPDATE e "
                    + "SET e.promedioPonderado=0 "
                    + "FROM (SELECT m.idEmpresa, d.* "
                    + "		FROM movimientosDetalle d "
                    + "		INNER JOIN movimientos m ON m.idMovto=d.idMovto "
                    + "		WHERE d.idMovto=" + to.getIdMovto() + ") d "
                    + "INNER JOIN empresasEmpaques e ON e.idEmpresa=d.idEmpresa AND e.idEmpaque=d.idEmpaque "
                    + "WHERE e.existenciaOficina=0";
            st.executeUpdate(strSQL);

            st.executeUpdate("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            this.cnx.close();
        }
    }

// ===================================== PEDIDOS ( Orden de Compra ) ==========================================================
    public boolean cambiarDirecto(boolean directo, int idEnvio, int idMovto, int idMovtoAlmacen, int idTienda, int idZonaImpuestos) throws SQLException {
        String strSQL;
        int idAlmacen = 3;
        int idEmpresa = 1;
        boolean ok = false;
        this.cnx = this.ds.getConnection();
        Statement st = this.cnx.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");
            if (directo) {
                strSQL = "INSERT INTO enviosPedidos (idEnvio, idCedis, idAlmacen, idMovto, agregado, directo, ordenDeCarga)\n"
                        + "SELECT " + idEnvio + ", idCedis, idAlmacen, idMovto, 0, 1, 0 FROM movimientos WHERE idMovto=" + idMovto;
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientos "
                        + "SET idCedis=(SELECT idCedis FROM almacenes WHERE idAlmacen=" + idAlmacen + "), idAlmacen=" + idAlmacen + ", estatus=1 "
                        + "WHERE idMovto=" + idMovto;
                st.executeUpdate(strSQL);
            } else {
                strSQL = "UPDATE M\n"
                        + "SET M.idCedis=P.idCedis, M.idAlmacen=P.idAlmacen, M.estatus=0\n"
                        + "FROM movimientos M\n"
                        + "INNER JOIN enviosPedidos P ON P.idEnvio=" + idEnvio + " AND P.idMovto=M.idMovto";
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM enviosPedidos WHERE idEnvio=" + idEnvio + " AND idMovto=" + idMovto;
                st.executeUpdate(strSQL);
            }
            surteOrdenDeCompra(idAlmacen, idEmpresa, idMovto, idMovtoAlmacen, idTienda, idZonaImpuestos);

            st.executeUpdate("COMMIT TRANSACTION");
            ok = true;
        } catch (SQLException e) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw e;
        } finally {
            st.close();
            this.cnx.close();
        }
        return ok;
    }

//    public void agregarPedido(int idMovto) throws SQLException {
//        String strSQL;
//        this.cnx = this.ds.getConnection();
//        Statement st = this.cnx.createStatement();
//        try {
//            st.executeUpdate("BEGIN TRANSACTION");
//            
//            strSQL= "INSERT INTO enviosPedidos (idEnvio, idCedis, idAlmacen, idMovto, agregado, directo, ordenDeCarga)\n" +
//                    "SELECT 0, idCedis, idAlmacen, idMovto, 1, 0, 0 FROM movimientos WHERE idMovto="+idMovto;
//            st.executeUpdate(strSQL);
//            
//            strSQL= "UPDATE movimientos SET idCedis=1, idAlmacen=3 WHERE idMovto="+idMovto;
//            st.executeUpdate(strSQL);
//            
//            st.executeUpdate("COMMIT TRANSACTION");
//        } catch (SQLException ex) {
//            st.executeUpdate("ROLLBACK TRANSACTION");
//            throw ex;
//        } finally {
//            st.close();
//            this.cnx.close();
//        }
//    }
    public void surtirOrdenDeCompra(boolean esVenta, int idAlmacen, int idMovto, int idMovtoAlmacen, int idZonaImpuestos) throws SQLException {
        this.cnx = this.ds.getConnection();
        Statement st = this.cnx.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");

            String strSQL;
            if (esVenta) {
                strSQL = "SELECT A.idEmpresa, M.idReferencia AS idTienda\n"
                        + "FROM movimientos M\n"
                        + "INNER JOIN almacenes A ON A.idAlmacen=M.idAlmacen\n"
                        + "WHERE M.idMovto=" + idMovto;
            } else {
                strSQL = "SELECT A.idEmpresa, P.idTienda\n"
                        + "FROM pedidosOC P\n"
                        + "INNER JOIN almacenes A ON A.idAlmacen=P.idAlmacen\n"
                        + "WHERE P.idPedido=" + idMovto;
            }
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                int idEmpresa = rs.getInt("idEmpresa");
                int idTienda = rs.getInt("idTienda");
                surteOrdenDeCompra(idAlmacen, idEmpresa, idMovto, idMovtoAlmacen, idTienda, idZonaImpuestos);
            }
            st.executeUpdate("COMMIT TRANSACTION");

        } catch (SQLException e) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw e;
        } finally {
            st.close();
            this.cnx.close();
        }
    }

    private void surteOrdenDeCompra(int idAlmacen, int idEmpresa, int idMovto, int idMovtoAlmacen, int idTienda, int idZonaImpuestos) throws SQLException {
        int idx;
        double separados;
        TOMovimientoProducto agregado;
        ArrayList<TOMovimientoProducto> agregados = new ArrayList<TOMovimientoProducto>();
        ArrayList<TOMovimientoProducto> detalle = new ArrayList<TOMovimientoProducto>();

        Statement st = this.cnx.createStatement();
        try {
            detalle = this.obtenMovimientoDetalle(idMovto);

            for (TOMovimientoProducto to : detalle) {
                separados = to.getCantFacturada() + to.getCantSinCargo();
                if (separados != 0) {
                    to.setCantFacturada(0);
                } else {
                    to.setCantFacturada(to.getCantOrdenada());
                }
                for (TOMovimientoProducto toAgr : this.grabaMovimientoDetalle(true, idAlmacen, idEmpresa, idMovto, idMovtoAlmacen, to, idTienda, separados, idZonaImpuestos)) {
                    if ((idx = agregados.indexOf(toAgr)) != -1) {
                        agregado = agregados.get(idx);
                        agregado.setCantFacturada(agregado.getCantFacturada() + toAgr.getCantFacturada());
                        agregado.setCantSinCargo(agregado.getCantSinCargo() + toAgr.getCantSinCargo());
                    } else {
                        agregados.add(toAgr);
                    }
                }
            }
        } finally {
            st.close();
        }
    }

    public void cerrarPedido(int idPedido) throws SQLException {
        String strSQL;
        this.cnx = this.ds.getConnection();
        Statement st = this.cnx.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");

            strSQL = "SELECT P.idAlmacen, P.idTienda, A.idEmpresa, T.idImpuestoZona\n"
                    + "FROM pedidosOC P\n"
                    + "INNER JOIN almacenes A ON A.idAlmacen=P.idAlmacen\n"
                    + "INNER JOIN clientesTiendas T ON T.idTienda=P.idTienda\n"
                    + "WHERE P.idPedido=" + idPedido;
            ResultSet rs = st.executeQuery(strSQL);
            rs.next();

            TOMovimientoOficina toMv = new TOMovimientoOficina();
            toMv.setIdTipo(28);
//            toMv.setIdEmpresa(rs.getInt("idEmpresa"));
            toMv.setIdAlmacen(rs.getInt("idAlmacen"));
//            toMv.setIdImpuestoZona(rs.getInt("idImpuestoZona"));
//            toMv.setIdMoneda(1);
            toMv.setTipoDeCambio(1);
            toMv.setIdReferencia(rs.getInt("idTienda"));
            toMv.setReferencia(idPedido);
//            toMv.setStatusOficina(0);
//            toMv.setStatusAlmacen(0);
            this.agregaMovimientoRelacionado(toMv);

//            strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantOrdenada, cantFacturada, cantSinCargo, cantRecibida, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior) "
//                    + "VALUES (" + toMv.getIdMovto() + ", ?, ?, 0, 0, 0, 0, 0, 0, 0, 0, ?, GETDATE(), 0)";
//            PreparedStatement ps = this.cnx.prepareStatement(strSQL);

            TOMovimientoProducto toProd;
            strSQL = "SELECT * FROM pedidosOCTiendaDetalle WHERE idPedido=" + idPedido;
            rs = st.executeQuery(strSQL);
            while (rs.next()) {
//                ps.setInt(1, rs.getInt("idEmpaque"));
//                ps.setDouble(2, rs.getDouble("cantFacturada"));
//                ps.setInt(3, rs.getInt("idImpuestoGrupo"));
//                ps.executeUpdate();

                toProd = new TOMovimientoProducto();
                toProd.setIdProducto(rs.getInt("idEmpaque"));
                toProd.setCantOrdenada(rs.getDouble("cantFacturada"));
                toProd.setIdImpuestoGrupo(rs.getInt("idImpuestoGrupo"));

//                this.agregarProductoOficina(toMv.getIdEmpresa(), toProd, toMv.getIdImpuestoZona(), toMv.getIdReferencia());
            }
            strSQL = "UPDATE movimientos SET propietario=0 WHERE idMovto=" + toMv.getIdMovto();
            st.executeUpdate(strSQL);

            strSQL = "UPDATE movimientosAlmacen SET propietario=0 WHERE idMovtoAlmacen=" + toMv.getIdMovtoAlmacen();
            st.executeUpdate(strSQL);

            strSQL = "UPDATE pedidosOC SET estatus=1 WHERE idPedido=" + idPedido;
            st.executeUpdate(strSQL);

            st.executeUpdate("COMMIT TRANSACTION");

        } catch (SQLException e) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw e;
        } finally {
            st.close();
            this.cnx.close();
        }
    }

//  ==================================== FINALIZA PROCEDIMIENTO DE VENTAS =================================
    private void asignaCostoPromedio(int idMovto) throws SQLException {
        String strSQL;
        Statement st = this.cnx.createStatement();
        try {
            strSQL = "UPDATE D\n"
                    + "SET D.costoPromedio=E.costoUnitarioPromedio \n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                    + "WHERE D.idMovto=" + idMovto + " AND E.existenciaOficina > (D.cantFacturada+D.cantSinCargo)";
            st.executeUpdate(strSQL);

            strSQL = "UPDATE E\n"
                    + "SET E.existenciaOficina=E.existenciaOficina-(D.cantFacturada+D.cantSinCargo) \n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                    + "WHERE D.idMovto=" + idMovto + " AND E.existenciaOficina > (D.cantFacturada+D.cantSinCargo)";
            st.executeUpdate(strSQL);
        } finally {
            st.close();
        }
    }

    public int cerrarMovtoAlmacenSalidaRelacionado(int idAlmacen, int idMovto, int idMovtoAlmacen) throws SQLException {
        int remision = 0;
        this.cnx = this.ds.getConnection();
        Statement st = this.cnx.createStatement();
        try {
            st.execute("BEGIN TRANSACTION");

            st.executeUpdate("UPDATE D\n"
                    + "SET D.fecha=GETDATE(), D.existenciaAnterior=L.saldo\n"
                    + "FROM movimientosAlmacenDetalle D\n"
                    + "INNER JOIN almacenesLotes L ON L.idAlmacen=" + idAlmacen + " AND L.idEmpaque=D.idEmpaque AND L.lote=D.lote\n"
                    + "WHERE idMovtoAlmacen=" + idMovtoAlmacen);
            st.executeUpdate("UPDATE L\n"
                    + "SET L.separados=L.separados-K.cantidad, L.saldo=L.saldo-K.cantidad\n"
                    + "FROM almacenesLotes L\n"
                    + "INNER JOIN movimientosAlmacenDetalle K ON K.idMovtoAlmacen=" + idMovtoAlmacen + " AND K.idEmpaque=L.idEmpaque AND K.lote=L.lote\n"
                    + "WHERE L.idAlmacen=" + idAlmacen);
            st.executeUpdate("UPDATE D\n"
                    + "SET D.fecha=GETDATE(), D.existenciaAnterior=E.existenciaOficina\n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN almacenesEmpaques E ON E.idAlmacen=" + idAlmacen + " AND E.idEmpaque=D.idEmpaque\n"
                    + "WHERE D.idMovto=" + idMovto);
            st.executeUpdate("UPDATE E\n"
                    + "SET E.existenciaOficina=E.existenciaOficina-(D.cantFacturada+D.cantSinCargo)\n"
                    + ", E.separados=E.separados-(D.cantFacturada+D.cantSinCargo)\n"
                    + "FROM almacenesEmpaques E\n"
                    + "INNER JOIN movimientosDetalle D ON D.idMovto=" + idMovto + " AND D.idEmpaque=E.idEmpaque\n"
                    + "WHERE E.idAlmacen=" + idAlmacen);
            ResultSet rs = st.executeQuery("SELECT remision FROM cedis WHERE idCedis=" + this.idCedis);
            if (rs.next()) {
                remision = rs.getInt("remision") + 1;
            } else {
                throw new SQLException("No se encotro cedis (id=" + this.idCedis + ") !!!");
            }
            st.executeUpdate("UPDATE cedis SET remision=remision+1 WHERE idCedis=" + this.idCedis);

            st.executeUpdate("INSERT INTO comprobantes (tipoComprobante, remision, serie, numero, idUsuario, fecha, propietario, idMovto, idMovtoAlmacen)"
                    + "VALUES (1, '" + remision + "', '', '', " + this.idUsuario + ", GETDATE(), 0, " + idMovto + ", " + idMovtoAlmacen + ")");

            int idComprobante = 0;
            rs = st.executeQuery("SELECT @@IDENTITY AS idComprobante");
            if (rs.next()) {
                idComprobante = rs.getInt("idComprobante");
            }
            st.executeUpdate("UPDATE movimientos SET estatus=2, idComprobante=" + idComprobante + " WHERE idMovto=" + idMovto);
            st.executeUpdate("UPDATE movimientosAlmacen SET estatus=2, idComprobante=" + idComprobante + " WHERE idMovtoAlmacen=" + idMovtoAlmacen);
            this.asignaCostoPromedio(idMovto);

            st.execute("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.execute("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            this.cnx.close();
        }
        return remision;
    }

    public void traspasarLote(int idAlmacen, TOMovimientoAlmacenProducto toOrigen, String loteDestino, double cantTraspasar) throws SQLException {
        this.cnx = this.ds.getConnection();
        Statement st = this.cnx.createStatement();
        try {
            st.execute("BEGIN TRANSACTION");

            this.separaLote1(idAlmacen, toOrigen.getIdMovtoAlmacen(), toOrigen.getIdProducto(), loteDestino, cantTraspasar, true);
//            this.liberaLote1(idAlmacen, toOrigen.getIdMovtoAlmacen(), toOrigen.getIdProducto(), toOrigen.getLote(), cantTraspasar);

            st.execute("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            this.cnx.close();
        }
    }

    private TOLote construirLote(ResultSet rs) throws SQLException {
        TOLote to = new TOLote();
        to.setIdAlmacen(rs.getInt("idAlmacen"));
        to.setIdEmpaque(rs.getInt("idEmpaque"));
        to.setLote(rs.getString("lote"));
        to.setFechaCaducidad(new java.util.Date(rs.getDate("fechaCaducidad").getTime()));
        to.setCantidad(rs.getDouble("cantidad"));
        to.setSaldo(rs.getDouble("saldo"));
        to.setSeparados(rs.getDouble("separados"));
        to.setExistenciaFisica(rs.getDouble("existenciaFisica"));
        return to;
    }

    public ArrayList<TOLote> obtenerEmpaqueLotesDisponibles(int idAlmacen, int idProducto) throws SQLException {
        String strSQL;
        ArrayList<TOLote> lotes = new ArrayList<TOLote>();

        this.cnx = this.ds.getConnection();
        Statement st = this.cnx.createStatement();
        try {
            strSQL = "SELECT *\n"
                    + "FROM almacenesLotes\n"
                    + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + " AND saldo-separados > 0\n"
                    + "ORDER BY fechaCaducidad";
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                lotes.add(this.construirLote(rs));
            }
        } finally {
            st.close();
            this.cnx.close();
        }
        return lotes;
    }

    public boolean eliminarMovtoSalidaRelacionado(int idAlmacen, int idMovto, int idMovtoAlmacen) throws SQLException {
        boolean ok = false;
        this.cnx = this.ds.getConnection();
        Statement st = this.cnx.createStatement();
        try {
            st.execute("BEGIN TRANSACTION");

            st.executeUpdate("UPDATE L\n"
                    + "SET L.separados=L.separados-K.cantidad\n"
                    + "FROM almacenesLotes L\n"
                    + "INNER JOIN movimientosAlmacenDetalle K ON K.idMovtoAlmacen=" + idMovtoAlmacen + " AND K.idEmpaque=L.idEmpaque AND K.lote=L.lote\n"
                    + "WHERE L.idAlmacen=" + idAlmacen);
            st.executeUpdate("DELETE FROM movimientosAlmacenDetalle WHERE idMovtoAlmacen=" + idMovtoAlmacen);
            st.executeUpdate("DELETE FROM movimientosAlmacen WHERE idMovtoAlmacen=" + idMovtoAlmacen);
            st.executeUpdate("UPDATE E\n"
                    + "SET E.separados=E.separados-(D.cantFacturada+D.cantSinCargo)\n"
                    + "FROM almacenesEmpaques E\n"
                    + "INNER JOIN movimientosDetalle D ON D.idMovto=" + idMovto + " AND D.idEmpaque=E.idEmpaque\n"
                    + "WHERE E.idAlmacen=" + idAlmacen);
            st.executeUpdate("DELETE FROM movimientosDetalle WHERE idMovto=" + idMovto);
            st.executeUpdate("DELETE FROM movimientos WHERE idMovto=" + idMovto);
            st.executeUpdate("DELETE FROM movimientosDetalleImpuestos WHERE idMovto=" + idMovto);
            st.executeUpdate("DELETE FROM movimientosRelacionados WHERE idMovto=" + idMovto + " AND idMovtoAlmacen=" + idMovtoAlmacen);

            st.execute("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.execute("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            this.cnx.close();
        }
        return ok;
    }

    public int cerrarMovtoSalidaRelacionado(int idAlmacen, int idMovto, int idMovtoAlmacen, int tipo) throws SQLException {
        int folio;
        String strSQL;
        this.cnx = this.ds.getConnection();
        Statement st = this.cnx.createStatement();
        try {
            st.execute("BEGIN TRANSACTION");

            folio = this.obtenerMovimientoFolio(false, idAlmacen, tipo);
            strSQL = "UPDATE movimientosAlmacen SET folio=" + folio + ", estatus=1 WHERE idMovtoAlmacen=" + idMovtoAlmacen;
            st.executeUpdate(strSQL);

            folio = this.obtenerMovimientoFolio(true, idAlmacen, tipo);
            strSQL = "UPDATE movimientos SET folio=" + folio + ", estatus=1 WHERE idMovto=" + idMovto;
            st.executeUpdate(strSQL);

            st.execute("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.execute("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            this.cnx.close();
        }
        return folio;
    }

    private void liberaLote1(int idAlmacen, int idMovtoAlmacen, int idProducto, String lote, double solicitados) throws SQLException {
        double disponibles;
        String strSQL = "SELECT K.cantidad, L.separados\n"
                + "FROM movimientosAlmacenDetalle K\n"
                + "INNER JOIN almacenesLotes L ON L.idAlmacen=K.idAlmacen AND L.idEmpaque=K.idEmpaque AND L.lote=K.lote\n"
                + "WHERE K.idMovtoAlmacen=" + idMovtoAlmacen + " AND K.idEmpaque=" + idProducto + " AND K.lote='" + lote + "'\n"
                + "ORDER BY L.fechaCaducidad DESC";
        Statement st = cnx.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                disponibles = rs.getDouble("cantidad");
                if (solicitados > disponibles) {
                    throw (new SQLException("Descuedre de lotes del producto en kardex !!!"));
                } else {
                    if (rs.getDouble("separados") < solicitados) {
                        throw (new SQLException("Descuedre de lotes del producto en almacen !!!"));
                    } else if (solicitados < disponibles) {
                        strSQL = "UPDATE movimientosAlmacenDetalle SET cantidad=cantidad-" + solicitados + " "
                                + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                    } else {    // Entonces es igual
                        strSQL = "DELETE FROM movimientosAlmacenDetalle "
                                + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + "AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                    }
                    st.executeUpdate(strSQL);

                    strSQL = "UPDATE almacenesLotes SET separados=separados-" + solicitados + " "
                            + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                    st.executeUpdate(strSQL);
                }
            } else {
                throw (new SQLException("El lote no se encuentra en el movimiento para el producto seleccionado !!!"));
            }
        } finally {
            st.close();
        }
    }

    private void liberaRelacionados(int idAlmacen, int idMovtoAlmacen, int idProducto, double solicitados) throws SQLException {
        this.liberaLotes(idAlmacen, idMovtoAlmacen, idProducto, solicitados);
        this.libera1(idAlmacen, idProducto, solicitados);
    }

    private void libera1(int idAlmacen, int idProducto, double solicitados) throws SQLException {
        String strSQL = "SELECT AE.separados "
                + "FROM almacenesEmpaques AE "
                + "WHERE AE.idAlmacen=" + idAlmacen + " AND AE.idEmpaque=" + idProducto;
        Statement st = this.cnx.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                if (rs.getDouble("separados") < solicitados) {
                    throw (new SQLException("Descuedre de lotes del producto en almacen !!!"));
                }
            } else {
                throw (new SQLException("No se encontro producto(" + idProducto + ") en almacen !!!"));
            }
            strSQL = "UPDATE almacenesEmpaques SET separados=separados-" + solicitados + " "
                    + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto;
            st.executeUpdate(strSQL);
        } finally {
            st.close();
        }
    }

    private void liberaLotes(int idAlmacen, int idMovtoAlmacen, int idProducto, double solicitados) throws SQLException {
        double liberar;
        String strSQL = "SELECT K.lote, K.cantidad, L.separados\n"
                + "FROM movimientosAlmacenDetalle K\n"
                + "INNER JOIN almacenesLotes L ON L.idAlmacen=K.idAlmacen AND L.idEmpaque=K.idEmpaque AND L.lote=K.lote\n"
                + "WHERE K.idMovtoAlmacen=" + idMovtoAlmacen + " AND K.idEmpaque=" + idProducto + "\n"
                + "ORDER BY L.fechaCaducidad DESC";
        Statement st1 = cnx.createStatement();
        Statement st = cnx.createStatement();
        try {
            ResultSet rs = st1.executeQuery(strSQL);
            while (rs.next()) {
                liberar = rs.getDouble("cantidad");
                if (solicitados < liberar) {
                    liberar = solicitados;
                    strSQL = "UPDATE movimientosAlmacenDetalle SET cantidad=cantidad-" + liberar + " "
                            + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + rs.getString("lote") + "'";
                } else {
                    strSQL = "DELETE FROM movimientosAlmacenDetalle "
                            + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + "AND idEmpaque=" + idProducto + " AND lote='" + rs.getString("lote") + "'";
                }
                st.executeUpdate(strSQL);

                if (rs.getDouble("separados") < liberar) {
                    throw (new SQLException("Descuedre de lote (" + rs.getString("lote") + ") del producto en almacen !!!"));
                } else {
                    strSQL = "UPDATE almacenesLotes SET separados=separados-" + liberar + " "
                            + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + rs.getString("lote") + "'";
                    st.executeUpdate(strSQL);
                }
                solicitados -= liberar;
                if (solicitados == 0) {
                    break;
                }
            }
        } finally {
            st.close();
            st1.close();
        }
    }

//    private TOMovimientoDetalle obtenerMovimientoDetalle(int idMovto, int idEmpaque) throws SQLException {
//        TOMovimientoDetalle to=null;
//        Statement st=this.cnx.createStatement();
//        try {
//            ResultSet rs = st.executeQuery("SELECT * FROM movimientosDetalle WHERE idMovto=" + idMovto + " AND idEmpaque="+idEmpaque);
//            if (rs.next()) {
//                to=this.construirDetalle(rs);
//            } else {
//                throw (new SQLException("No se encontro el producto !!!"));
//            }
//        } finally {
//            st.close();
//        }
//        return to;
//    }
    private ArrayList<Double> obtenerBoletinSinCargo(int idEmpresa, int idProducto, int idTienda) throws SQLException {
        ArrayList<Double> boletin;
        String strSQL = "SELECT G.idGrupoCte, C.idCliente, F.idFormato, T.idTienda, P.idGrupo, P.idSubGrupo\n"
                + "FROM clientesTiendas T\n"
                + "INNER JOIN clientesFormatos F ON F.idFormato=T.idFormato\n"
                + "INNER JOIN clientesGrupos G ON G.idGrupoCte=F.idGrupoCte\n"
                + "INNER JOIN clientes C ON C.idCliente=T.idCliente\n"
                + "INNER JOIN empaques E ON E.idEmpaque=" + idProducto + "\n"
                + "INNER JOIN productos P ON P.idProducto=E.idProducto\n"
                + "WHERE T.idTienda=" + idTienda;
        Statement st = this.cnx.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                int idGrupoCte = rs.getInt("idGrupoCte");
                int idCliente = rs.getInt("idCliente");
                int idFormato = rs.getInt("idFormato");
                int idGrupo = rs.getInt("idGrupo");
                int idSubGrupo = rs.getInt("idSubGrupo");
                boletin = new ArrayList<Double>();
                boletin.add(0.0);
                boletin.add(0.0);
                strSQL = "SELECT B.* \n"
                        + "FROM clientesBoletinesDetalle B\n"
                        + "WHERE B.idEmpresa=" + idEmpresa + "\n"
                        + "		AND ((B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=0 AND B.idFormato=0 AND B.idTienda=0)\n"
                        + "			 OR (B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=" + idCliente + " AND B.idFormato=0 AND B.idTienda=0)\n"
                        + "			 OR (B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=" + idCliente + " AND B.idFormato=" + idFormato + " AND B.idTienda=0)\n"
                        + "			 OR (B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=" + idCliente + " AND B.idFormato=" + idFormato + " AND B.idTienda=" + idTienda + "))\n"
                        + "		AND ((B.idGrupo=" + idGrupo + " AND B.idSubGrupo=0 AND B.idEmpaque=0) \n"
                        + "				OR (B.idGrupo=" + idGrupo + " AND B.idSubGrupo=" + idSubGrupo + " AND B.idEmpaque=0) \n"
                        + "				OR (B.idGrupo=" + idGrupo + " AND B.idSubGrupo=" + idSubGrupo + " AND B.idEmpaque=" + idProducto + "))\n"
                        + "		AND CONVERT(date, GETDATE()) BETWEEN B.iniVigencia AND B.finVigencia";
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    boletin.set(0, rs.getDouble("conCargo"));
                    boletin.set(1, rs.getDouble("sinCargo"));
                }
            } else {
                throw (new SQLException("No se encontro producto en detalle !!!"));
            }
        } finally {
            st.close();
        }
        return boletin;
    }

//    private ArrayList<Double> obtenerBoletinSinCargo(int idEmpresa, int idProducto, int idTienda) throws SQLException {
//        ArrayList<Double> boletin;
//        String strSQL = "SELECT LP.idEmpresa, LP.idGrupoCte, LP.idCliente, LP.idFormato, LP.idTienda, LP.idGrupo, LP.idSubGrupo, LP.idEmpaque, iniVigencia\n" +
//                        "		, CASE WHEN LP.idEmpaque IS NOT NULL THEN LP.conCargo ELSE 0 END AS conCargo\n" +
//                        "		, CASE WHEN LP.idEmpaque IS NOT NULL THEN LP.sinCargo ELSE 0 END AS sinCargo\n" +
//                        "FROM (SELECT M.idEmpresa, D.idEmpaque, G.idGrupoCte, C.idCliente, F.idFormato, T.idTienda, P.idGrupo, P.idSubGrupo, CONVERT(date, GETDATE()) AS fechaHoy\n" +
//                        "     FROM movimientosDetalle D\n" +
//                        "	INNER JOIN movimientos M ON M.idMovto=D.idMovto\n" +
//                        "       INNER JOIN empaques E ON E.idEmpaque=D.idEmpaque\n" +
//                        "       INNER JOIN productos P ON P.idProducto=E.idProducto\n" +
//                        "	INNER JOIN clientesTiendas T ON T.idTienda=M.idReferencia\n" +
//                        "	INNER JOIN clientesFormatos F ON F.idFormato=T.idFormato\n" +
//                        "	INNER JOIN clientes C ON C.idCliente=F.idCliente\n" +
//                        "	INNER JOIN clientesGrupos G ON G.idGrupoCte=C.idGrupoCte\n" +
//                        "	WHERE D.idMovto=" + idMovto + " AND D.idEmpaque=" + idProducto + ") D\n" +
//                        "LEFT JOIN clientesBoletinesDetalle LP\n" +
//                        "             ON LP.idEmpresa=D.idEmpresa AND (LP.idGrupoCte=D.idGrupoCte OR LP.idGrupoCte=0) AND (LP.idCliente=D.idCliente OR LP.idCliente=0) AND (LP.idFormato=D.idFormato OR LP.idFormato=0) AND (LP.idTienda=D.idTienda OR LP.idTienda=0) AND (LP.idGrupo=D.idGrupo OR LP.idGrupo=0) AND (LP.idSubGrupo=D.idSubGrupo OR LP.idSubGrupo=0) AND (LP.idEmpaque="+idProducto+" OR LP.idEmpaque=0) AND fechaHoy BETWEEN LP.iniVigencia AND LP.finVigencia\n" +
//                        "ORDER BY LP.idGrupoCte DESC, LP.idCliente DESC, LP.idFormato DESC, LP.idTienda DESC, LP.idGrupo DESC, LP.idSubGrupo DESC, LP.idEmpaque DESC, LP.iniVigencia DESC";
//        Statement st=this.cnx.createStatement();
//        try {
//            ResultSet rs = st.executeQuery(strSQL);
//            if (rs.next()) {
//                boletin=new ArrayList<Double>();
//                boletin.add(rs.getDouble("conCargo"));
//                boletin.add(rs.getDouble("sinCargo"));
//            } else {
//                throw (new SQLException("No se encontro producto en detalle !!!"));
//            }
//        } finally {
//            st.close();
//        }
//        return boletin;
//    }
    private double separaLote1(int idAlmacen, int idMovtoAlmacen, int idProducto, String lote, double solicitados, boolean total) throws SQLException {
        boolean nuevo;
        double separados = 0;
        double disponibles, saldo;
        String strSQL = "SELECT ISNULL(K.idMovtoAlmacen, 0) AS idMovtoAlmacen, L.lote, L.saldo, L.saldo-L.separados AS disponibles\n"
                + "FROM (SELECT * FROM movimientosAlmacenDetalle WHERE idMovtoAlmacen=" + idMovtoAlmacen + ") K\n"
                + "RIGHT JOIN almacenesLotes L ON L.idAlmacen=K.idAlmacen AND L.idEmpaque=K.idEmpaque AND L.lote=K.lote\n"
                + "WHERE L.idAlmacen=" + idAlmacen + " AND L.idEmpaque=" + idProducto + " AND L.lote='" + lote + "'";
        Statement st = cnx.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                saldo = rs.getDouble("saldo");
                nuevo = rs.getInt("idMovtoAlmacen") == 0;
                disponibles = rs.getDouble("disponibles");
                if (disponibles <= 0) {
                    throw (new SQLException("No hay disponibles del lote !!!"));
                } else if (disponibles < solicitados) {
                    if (total) {
                        throw (new SQLException("No hay existencia suficiente, solo hay " + disponibles + " disponibles !!!"));
                    } else {
                        solicitados = disponibles;
                    }
                }
            } else {
                throw (new SQLException("El lote no se encuentra en el almacen para el empaque solicitado !!!"));
            }
            strSQL = "UPDATE almacenesLotes SET separados=separados+" + solicitados + " "
                    + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
            st.executeUpdate(strSQL);

            if (nuevo) {
                strSQL = "INSERT INTO movimientosAlmacenDetalle (idMovtoAlmacen, idEmpaque, lote, idAlmacen, cantidad, suma, fecha, existenciaAnterior) "
                        + "VALUES (" + idMovtoAlmacen + ", " + idProducto + ", '" + lote + "', " + idAlmacen + ", " + solicitados + ", 0, GETDATE(), " + saldo + ")";
            } else {
                strSQL = "UPDATE movimientosAlmacenDetalle "
                        + "SET cantidad=cantidad+" + solicitados + ", fecha=GETDATE(), existenciaAnterior=" + saldo + " "
                        + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
            }
            st.executeUpdate(strSQL);
        } finally {
            st.close();
        }
        return separados;
    }

    private double separaRelacionados(int idAlmacen, int idMovtoAlmacen, int idProducto, double solicitados, boolean total) throws SQLException {
        double cantSeparada = this.separaLotes(idAlmacen, idMovtoAlmacen, idProducto, solicitados, total);
        if (cantSeparada > 0) {
            this.separa1(idAlmacen, idProducto, cantSeparada, true);
        }
        return cantSeparada;
    }

    private double separa1(int idAlmacen, int idProducto, double solicitados, boolean total) throws SQLException {
        double separados = 0;
        double disponibles;
        String strSQL = "SELECT AE.existenciaOficina-AE.separados AS disponibles "
                + "FROM almacenesEmpaques AE "
                + "WHERE AE.idAlmacen=" + idAlmacen + " AND AE.idEmpaque=" + idProducto;
        Statement st = cnx.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                disponibles = rs.getDouble("disponibles");
                if (disponibles <= 0) {
                    throw (new SQLException("No hay existencia !!!"));
                } else if (total && disponibles < solicitados) {
                    throw (new SQLException("No hay existencia suficiente, solo hay " + disponibles + " disponibles !!!"));
                } else {
                    separados = solicitados;
                }
            } else {
                throw (new SQLException("No se encontro producto(" + idProducto + ") en almacen !!!"));
            }
            strSQL = "UPDATE almacenesEmpaques SET separados=separados+" + separados + " "
                    + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto;
            st.executeUpdate(strSQL);
        } finally {
            st.close();
        }
        return separados;
    }

    private double separaLotes(int idAlmacen, int idMovtoAlmacen, int idProducto, double solicitados, boolean total) throws SQLException {
        double separar;
        double separados = 0;
        String strSQL = "SELECT ISNULL(K.idMovtoAlmacen, 0) AS idMovtoAlmacen, L.lote, L.saldo, L.saldo-L.separados AS disponibles\n"
                + "FROM (SELECT * FROM movimientosAlmacenDetalle WHERE idMovtoAlmacen=" + idMovtoAlmacen + ") K\n"
                + "RIGHT JOIN almacenesLotes L ON L.idAlmacen=K.idAlmacen AND L.idEmpaque=K.idEmpaque AND L.lote=K.lote\n"
                + "WHERE L.idAlmacen=" + idAlmacen + " AND L.idEmpaque=" + idProducto + " AND L.saldo-L.separados > 0\n"
                + "ORDER BY L.fechaCaducidad";
        Statement st1 = this.cnx.createStatement();
        Statement st = this.cnx.createStatement();
        try {
            ResultSet rs = st1.executeQuery(strSQL);
            while (rs.next()) {
                separar = rs.getDouble("disponibles");
                if (solicitados < separar) {
                    separar = solicitados;
                }
                strSQL = "UPDATE almacenesLotes SET separados=separados+" + separar + " "
                        + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + rs.getString("lote") + "'";
                st.executeUpdate(strSQL);

                if (rs.getInt("idMovtoAlmacen") == 0) {
                    strSQL = "INSERT INTO movimientosAlmacenDetalle (idMovtoAlmacen, idEmpaque, lote, idAlmacen, cantidad, suma, fecha, existenciaAnterior) "
                            + "VALUES (" + idMovtoAlmacen + ", " + idProducto + ", '" + rs.getString("lote") + "', " + idAlmacen + ", " + separar + ", 0, GETDATE(), " + rs.getDouble("saldo") + ")";
                } else {
                    strSQL = "UPDATE movimientosAlmacenDetalle "
                            + "SET cantidad=cantidad+" + separar + ", fecha=GETDATE(), existenciaAnterior=" + rs.getDouble("saldo") + " "
                            + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + rs.getString("lote") + "'";
                }
                st.executeUpdate(strSQL);

                separados += separar;
                solicitados -= separar;
                if (solicitados == 0) {
                    break;
                }
            }
            if (total && solicitados != 0) {
                throw (new SQLException("No hay suficientes lotes para separar !!!"));
            }
        } finally {
            st.close();
            st1.close();
        }
        return separados;
    }

    private ArrayList<Double> calculaPrecioNeto(ResultSet rs) throws SQLException {
        ArrayList<Double> precio = new ArrayList<Double>();
        double precioUnitario, desctoProducto1, precioLista;
        if (rs.getDouble("precioVenta") == 0) {
            throw (new SQLException("No tiene precio de lista vigente !!!"));
        } else {
//            to.setUnitario(rs.getDouble("precioUnitario"));
            precioUnitario=rs.getDouble("precioVenta");
            if (!rs.getString("descuentos").equals("")) {
                double descuento = 1.00;
                for (String str : rs.getString("descuentos").split(",")) {
                    descuento = descuento * (1 - Double.parseDouble(str) / 100.00);
                }
//                to.setDesctoProducto1((1.00 - descuento) * 100.00);
                desctoProducto1=(1.00 - descuento) * 100.00;
            } else {
//                to.setDesctoProducto1(0.00);
                desctoProducto1=0.00;
            }
//            to.setCosto((to.getUnitario() / (1 - to.getDesctoProducto1() / 100.00)));
            precioLista=(precioUnitario / (1 - desctoProducto1 / 100.00));
        }
        precio.add(precioUnitario);
        precio.add(desctoProducto1);
        precio.add(precioLista);
        return precio;
    }
    
    private void calcularPrecioNeto(int idEmpresa, TOPedidoProducto to, int idTienda) throws SQLException {
        String strSQL = "SELECT G.idGrupoCte, C.idCliente, F.idFormato, T.idTienda, P.idGrupo, P.idSubGrupo\n"
                + "FROM clientesTiendas T\n"
                + "INNER JOIN clientesFormatos F ON F.idFormato=T.idFormato\n"
                + "INNER JOIN clientesGrupos G ON G.idGrupoCte=F.idGrupoCte\n"
                + "INNER JOIN clientes C ON C.idCliente=T.idCliente\n"
                + "INNER JOIN empaques E ON E.idEmpaque=" + to.getIdProducto() + "\n"
                + "INNER JOIN productos P ON P.idProducto=E.idProducto\n"
                + "WHERE T.idTienda=" + idTienda;
        Statement st = this.cnx.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                int idGrupoCte = rs.getInt("idGrupoCte");
                int idCliente = rs.getInt("idCliente");
                int idFormato = rs.getInt("idFormato");
                int idGrupo = rs.getInt("idGrupo");
                int idSubGrupo = rs.getInt("idSubGrupo");
                strSQL = "SELECT B.*\n"
                        + "FROM clientesListasPrecios B\n"
                        + "WHERE B.idEmpresa=" + idEmpresa + "\n"
                        + "		AND ((B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=0 AND B.idFormato=0 AND B.idTienda=0)\n"
                        + "			 OR (B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=" + idCliente + " AND B.idFormato=0 AND B.idTienda=0)\n"
                        + "			 OR (B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=" + idCliente + " AND B.idFormato=" + idFormato + " AND B.idTienda=0)\n"
                        + "			 OR (B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=" + idCliente + " AND B.idFormato=" + idFormato + " AND B.idTienda=" + idTienda + "))\n"
                        + "		AND ((B.idGrupo=" + idGrupo + " AND B.idSubGrupo=0 AND B.idEmpaque=0) \n"
                        + "				OR (B.idGrupo=" + idGrupo + " AND B.idSubGrupo=" + idSubGrupo + " AND B.idEmpaque=0) \n"
                        + "				OR (B.idGrupo=" + idGrupo + " AND B.idSubGrupo=" + idSubGrupo + " AND B.idEmpaque=" + to.getIdProducto() + "))\n"
                        + "		AND CONVERT(date, GETDATE()) BETWEEN B.iniVigencia AND B.finVigencia";
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    ArrayList<Double> precio=this.calculaPrecioNeto(rs);
                    to.setUnitario(precio.get(0).doubleValue());
                    
                    strSQL = "UPDATE pedidosOCTiendaDetalle\n"
                            + "SET unitario=" + to.getUnitario() + "\n"
                            + "WHERE idPedido=" + to.getIdPedido() + " AND idEmpaque=" + to.getIdProducto();
                    st.executeUpdate(strSQL);
                } else {
                    throw (new SQLException("No se encontro precio de venta !!!"));
                }
            }
        } finally {
            st.close();
        }
    }

    private void calcularPrecioNeto(int idEmpresa, TOMovimientoProducto to, int idTienda) throws SQLException {
        String strSQL = "SELECT G.idGrupoCte, C.idCliente, F.idFormato, T.idTienda, P.idGrupo, P.idSubGrupo\n"
                + "FROM clientesTiendas T\n"
                + "INNER JOIN clientesFormatos F ON F.idFormato=T.idFormato\n"
                + "INNER JOIN clientesGrupos G ON G.idGrupoCte=F.idGrupoCte\n"
                + "INNER JOIN clientes C ON C.idCliente=T.idCliente\n"
                + "INNER JOIN empaques E ON E.idEmpaque=" + to.getIdProducto() + "\n"
                + "INNER JOIN productos P ON P.idProducto=E.idProducto\n"
                + "WHERE T.idTienda=" + idTienda;
        Statement st = this.cnx.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                int idGrupoCte = rs.getInt("idGrupoCte");
                int idCliente = rs.getInt("idCliente");
                int idFormato = rs.getInt("idFormato");
                int idGrupo = rs.getInt("idGrupo");
                int idSubGrupo = rs.getInt("idSubGrupo");
                strSQL = "SELECT B.*\n"
                        + "FROM clientesListasPrecios B\n"
                        + "WHERE B.idEmpresa=" + idEmpresa + "\n"
                        + "		AND ((B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=0 AND B.idFormato=0 AND B.idTienda=0)\n"
                        + "			 OR (B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=" + idCliente + " AND B.idFormato=0 AND B.idTienda=0)\n"
                        + "			 OR (B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=" + idCliente + " AND B.idFormato=" + idFormato + " AND B.idTienda=0)\n"
                        + "			 OR (B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=" + idCliente + " AND B.idFormato=" + idFormato + " AND B.idTienda=" + idTienda + "))\n"
                        + "		AND ((B.idGrupo=" + idGrupo + " AND B.idSubGrupo=0 AND B.idEmpaque=0) \n"
                        + "				OR (B.idGrupo=" + idGrupo + " AND B.idSubGrupo=" + idSubGrupo + " AND B.idEmpaque=0) \n"
                        + "				OR (B.idGrupo=" + idGrupo + " AND B.idSubGrupo=" + idSubGrupo + " AND B.idEmpaque=" + to.getIdProducto() + "))\n"
                        + "		AND CONVERT(date, GETDATE()) BETWEEN B.iniVigencia AND B.finVigencia";
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    ArrayList<Double> precio=this.calculaPrecioNeto(rs);
                    to.setCosto(precio.get(2).doubleValue());
                    to.setDesctoProducto1(precio.get(1).doubleValue());
                    to.setUnitario(precio.get(0).doubleValue());
                    
                    strSQL = "UPDATE movimientosDetalle\n"
                            + "SET costo=" + to.getCosto() + ",desctoProducto1=" + to.getDesctoProducto1() + ",unitario=" + to.getUnitario() + ",fecha=GETDATE()\n"
                            + "WHERE idMovto=" + to.getIdMovto() + " AND idEmpaque=" + to.getIdProducto();
                    st.executeUpdate(strSQL);

                    this.calculaImpuestosProducto(to.getIdMovto(), to.getIdProducto(), to.getUnitario());
                } else {
                    throw (new SQLException("No se encontro precio de venta !!!"));
                }
            }
        } finally {
            st.close();
        }
    }
    
    public void agregarPedidoProducto(int idEmpresa, TOPedidoProducto to, int idTienda) throws SQLException {
        String strSQL = "INSERT INTO pedidosOCTiendaDetalle (idPedido, idTienda, idEmpaque, cantFacturada, cantSinCargo, unitario, idImpuestoGrupo)\n" +
                        "VALUES ("+to.getIdPedido()+", "+idTienda+", "+to.getIdProducto()+", "+to.getCantFacturada()+", "+to.getCantSinCargo()+", "+to.getUnitario()+", "+to.getIdImpuestoGrupo()+")";
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.execute("BEGIN TRANSACTION");
            
            st.executeUpdate(strSQL);
            
            this.calcularPrecioNeto(idEmpresa, to, idTienda);
            
            st.execute("COMMIT TRANSACTION");
        } catch(SQLException ex) {
            st.execute("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            cn.close();
        }
    }

    private void agregarProductoOficina(int idEmpresa, TOMovimientoProducto to, int idZonaImpuestos, int idTienda) throws SQLException {
        String strSQL;
        Statement st = this.cnx.createStatement();
        try {
            strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantOrdenada, cantFacturada, cantSinCargo, cantRecibida, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior, costoPromedio) "
                    + "VALUES (" + to.getIdMovto() + ", " + to.getIdProducto() + ", " + to.getCantOrdenada() + ", " + to.getCantFacturada() + ", " + to.getCantSinCargo() + ", " + to.getCantRecibida() + ", " + to.getCosto() + ", " + to.getDesctoProducto1() + ", " + to.getDesctoProducto2() + ", " + to.getDesctoConfidencial() + ", " + to.getUnitario() + ", " + to.getIdImpuestoGrupo() + ", GETDATE(), 0, 0)";
            st.executeUpdate(strSQL);

            strSQL = "INSERT INTO movimientosDetalleImpuestos (idMovto, idEmpaque, idImpuesto, impuesto, valor, aplicable, modo, acreditable, importe, acumulable) "
                    + "SELECT " + to.getIdMovto() + ", " + to.getIdProducto() + ", ID.idImpuesto, I.impuesto, ID.valor, I.aplicable, I.modo, I.acreditable, 0.00 as importe, I.acumulable "
                    + "FROM impuestosDetalle ID "
                    + "INNER JOIN impuestos I on I.idImpuesto=ID.idImpuesto "
                    + "WHERE ID.idGrupo=" + to.getIdImpuestoGrupo() + " AND ID.idZona=" + idZonaImpuestos + " AND GETDATE() BETWEEN ID.fechaInicial AND ID.fechaFinal";
            if (st.executeUpdate(strSQL) == 0) {
                throw (new SQLException("No se insertaron impuestos !!!"));
            }
            this.calcularPrecioNeto(idEmpresa, to, idTienda);
        } finally {
            st.close();
        }
    }

//    private void liberarSimilaresSinCargo1(int idAlmacen, int idMovto, int idMovtoAlmacen, int idProducto, double cantLiberar) throws SQLException {
//        String lote;
//        double liberar;
//        String strSQL = "SELECT D.idEmpaque, L.lote, L.cantidad\n" +
//                        "FROM empaquesSimilares S\n" +
//                        "INNER JOIN movimientosDetalle D ON D.idMovto="+idMovto+" AND D.idEmpaque=S.idSimilar\n" +
//                        "LEFT JOIN movimientosAlmacenDetalle K ON K.idMovtoAlmacen="+idMovtoAlmacen+" AND K.idEmpaque=D.idEmpaque\n" +
//                        "INNER JOIN almacenesLotes L ON L.idAlmacen="+idAlmacen+" AND L.idEmpaque=K.idEmpaque AND L.lote=K.lote\n" +
//                        "WHERE S.idEmpaque="+idProducto+" AND K.lote IS NOT NULL\n" +
//                        "ORDER BY L.fechaCaducidad DESC";
//        Statement st1=this.cnx.createStatement();
//        Statement st=this.cnx.createStatement();
//        try {
//            ResultSet rs=st1.executeQuery(strSQL);
//            while(rs.next()) {
//                lote=rs.getString("lote");
//                idProducto=rs.getInt("idEmpaque");
//                if(cantLiberar > rs.getDouble("cantidad")) {
//                    liberar=rs.getDouble("cantidad");
//                } else {
//                    liberar=cantLiberar;
//                }
//                this.liberaLote1(idAlmacen, idMovtoAlmacen, idProducto, lote, liberar);
//                strSQL= "UPDATE movimientosDetalle\n" +
//                        "SET cantSinCargo=cantSinCargo-" + liberar + "\n" +
//                        "WHERE idMovto=" + idMovto + " AND idEmpaque=" + idProducto;
//                st.executeUpdate(strSQL);
//
//                cantLiberar-=liberar;
//                if(cantLiberar==0) {
//                    break;
//                }
//            }
//            if(cantLiberar!=0) {
//                throw (new SQLException("No hay existencia suficiente para liberar sin cargo. Descuadre de lotes !!!"));
//            }
//        } finally {
//            st.close();
//            st1.close();
//        }
//    }
    public ArrayList<TOMovimientoProducto> agregarSimilaresSinCargo(int idAlmacen, int idMovto, int idMovtoAlmacen, int idProducto, double cantSolicitada, int idZonaImpuestos) throws SQLException {
        int idx;
        double cantSeparada;
        TOMovimientoProducto newTo;
        ArrayList<TOMovimientoProducto> agregados = new ArrayList<TOMovimientoProducto>();
        String strSQL = "SELECT M.idEmpresa, M.idReferencia AS idTienda"
                + "     , ISNULL(D.idMovto, 0) AS idMovto, S.idSimilar, L.lote, L.saldo-L.separados AS disponibles, L.fechaCaducidad, P.idImpuesto\n"
                + "FROM empaquesSimilares S\n"
                + "LEFT JOIN movimientosDetalle D ON D.idMovto=" + idMovto + " AND D.idEmpaque=S.idSimilar\n"
                + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                + "LEFT JOIN almacenesLotes L ON L.idAlmacen=" + idAlmacen + " AND L.idEmpaque=S.idSimilar\n"
                + "INNER JOIN empaques E ON E.idEmpaque=S.idSimilar\n"
                + "INNER JOIN productos P ON P.idProducto=E.idProducto\n"
                + "WHERE S.idEmpaque=" + idProducto + " AND L.idEmpaque IS NOT NULL AND L.saldo-L.separados > 0\n"
                + "ORDER BY ISNULL(D.idMovto, 0) DESC, L.fechaCaducidad";
        Statement st1 = this.cnx.createStatement();
        Statement st = this.cnx.createStatement();
        try {
            ResultSet rs = st1.executeQuery(strSQL);
            while (rs.next()) {
                idProducto = rs.getInt("idSimilar");
                newTo = new TOMovimientoProducto();
                newTo.setIdProducto(idProducto);
                newTo.setIdImpuestoGrupo(rs.getInt("idImpuesto"));
                cantSeparada = this.separaRelacionados(idAlmacen, idMovtoAlmacen, idProducto, cantSolicitada, false);
                if (cantSeparada > 0) {
                    idx = agregados.indexOf(newTo);
                    if (idx != -1) {
                        newTo = agregados.get(idx);
                    } else {
                        if (rs.getInt("idMovto") == 0) {
                            this.agregarProductoOficina(rs.getInt("idEmpresa"), newTo, idZonaImpuestos, rs.getInt("idTienda"));
                        } else {
                            newTo.setIdMovto(idMovto);
                        }
                        agregados.add(newTo);
                    }
                    newTo.setCantSinCargo(newTo.getCantSinCargo() + cantSeparada);

                    strSQL = "UPDATE movimientosDetalle\n"
                            + "SET cantSinCargo=cantSinCargo+" + cantSeparada + "\n"
                            + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + idProducto;
                    st.executeUpdate(strSQL);

                    cantSolicitada -= cantSeparada;
                    if (cantSolicitada == 0) {
                        break;
                    }
                }
            }
            if (cantSolicitada != 0) {
                throw (new SQLException("No hay existencia suficiente para separar productos sin cargo !!!"));
            }
        } finally {
            st.close();
            st1.close();
        }
        return agregados;
    }
    
    public void trasferirSinCargo(int idPedido, int idProdOrigen, TOPedidoProducto to, double cantidad) throws SQLException {
        String strSQL="";
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.execute("BEGIN TRANSACTION");
            
            if(to.getIdPedido()==0) {
                strSQL="SELECT idEmpresa, idTienda FROM pedidosOC WHERE idPedido="+idPedido;
                ResultSet rs=st.executeQuery(strSQL);
                if(rs.next()) {
                    to.setIdPedido(idPedido);
                    to.setCantSinCargo(cantidad);
                    
                    this.agregarPedidoProducto(rs.getInt("idEmpresa"), to, rs.getInt("idTienda"));
                }
            } else {
                to.setCantSinCargo(to.getCantSinCargo()+cantidad);
                
                strSQL= "UPDATE pedidosOCTiendaDetalle\n" +
                        "SET cantSinCargo=cantSinCargo+"+cantidad+"\n" +
                        "WHERE idPedido="+idPedido+" AND idEmpaque="+to.getIdProducto();
                st.executeUpdate(strSQL);
            }
            strSQL= "UPDATE pedidosOCTiendaDetalle SET cantSinCargo=cantSinCargo-"+cantidad+"\n" +
                    "WHERE idPedido="+idPedido+" AND idEmpaque="+idProdOrigen;
            st.executeUpdate(strSQL);
            
            st.execute("COMMIT TRANSACTION");
        } finally {
            st.close();
            cn.close();
        }
    }

    public void tranferirSinCargo(int idAlmacen, int idMovto, int idMovtoAlmacen, TOMovimientoProducto toOrigen, TOMovimientoProducto toDestino, double cantidad, int idZonaImpuestos) throws SQLException {
        String strSQL;
        this.cnx = this.ds.getConnection();
        Statement st = this.cnx.createStatement();
        try {
            st.execute("BEGIN TRANSACTION");

            this.separaRelacionados(idAlmacen, idMovtoAlmacen, toDestino.getIdProducto(), cantidad, true);
            if (toDestino.getIdMovto() == 0) {
                strSQL="SELECT idEmpresa, idReferencia AS idTienda FROM movimientos WHERE idMovto="+idMovto;
                ResultSet rs=st.executeQuery(strSQL);
                if(rs.next()) {
                    this.agregarProductoOficina(rs.getInt("idEmpresa"), toDestino, idZonaImpuestos, rs.getInt("idTienda"));
                    toDestino.setIdMovto(idMovto);
                }
            }
            strSQL = "UPDATE movimientosDetalle\n"
                    + "SET cantSinCargo=cantSinCargo+" + cantidad + "\n"
                    + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + toDestino.getIdProducto();
            st.executeUpdate(strSQL);

            this.liberaRelacionados(idAlmacen, idMovtoAlmacen, toOrigen.getIdProducto(), cantidad);
            strSQL = "UPDATE movimientosDetalle\n"
                    + "SET cantSinCargo=cantSinCargo-" + cantidad + "\n"
                    + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + toOrigen.getIdProducto();
            st.executeUpdate(strSQL);

            toDestino.setCantSinCargo(toDestino.getCantSinCargo() + cantidad);
            toOrigen.setCantSinCargo(toOrigen.getCantSinCargo() - cantidad);

            st.execute("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.execute("ROLLBACK TRANSACTION");
            throw (ex);
        } finally {
            st.close();
            this.cnx.close();
        }
    }

    private ArrayList<TOMovimientoProducto> liberarSimilaresSinCargo(int idAlmacen, int idMovto, int idMovtoAlmacen, int idProducto, double cantLiberar, double bolentinConCargo, double boletinSinCargo) throws SQLException {
        int idImpuestoGrupo;
        double liberar, cantSinCargo, cantDisponible;
        TOMovimientoProducto newTo;
        ArrayList<TOMovimientoProducto> liberados = new ArrayList<TOMovimientoProducto>();
        String strSQL = "SELECT D.idEmpaque, D.cantFacturada, D.cantSinCargo, D.idImpuestoGrupo\n"
                + "FROM empaquesSimilares S\n"
                + "INNER JOIN movimientosDetalle D ON D.idMovto=" + idMovto + " AND D.idEmpaque=S.idSimilar\n"
                + "WHERE S.idEmpaque=" + idProducto;
        Statement st1 = this.cnx.createStatement();
        Statement st = this.cnx.createStatement();
        try {
            ResultSet rs = st1.executeQuery(strSQL);
            while (rs.next()) {
                idProducto = rs.getInt("idEmpaque");
                idImpuestoGrupo = rs.getInt("idImpuestoGrupo");
                cantSinCargo = ((int) (rs.getDouble("cantFacturada") / bolentinConCargo)) * boletinSinCargo;     // Lo que debe quedar
                cantDisponible = rs.getDouble("cantSinCargo") - cantSinCargo;
                if (cantDisponible > 0) {
                    if (cantLiberar > cantDisponible) {
                        liberar = cantDisponible;
                    } else {
                        liberar = cantLiberar;
                    }
                    this.liberaRelacionados(idAlmacen, idMovtoAlmacen, idProducto, liberar);
                    strSQL = "UPDATE movimientosDetalle\n"
                            + "SET cantSinCargo=cantSinCargo-" + liberar + "\n"
                            + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + idProducto;
                    st.executeUpdate(strSQL);

                    newTo = new TOMovimientoProducto();
                    newTo.setIdMovto(idMovto);
                    newTo.setIdProducto(idProducto);
                    newTo.setCantSinCargo(-liberar);
                    newTo.setIdImpuestoGrupo(idImpuestoGrupo);
                    liberados.add(newTo);

                    cantLiberar -= liberar;
                }
                if (cantLiberar == 0) {
                    break;
                }
            }
            if (cantLiberar != 0) {
                throw (new SQLException("No hay existencia suficiente para liberar sin cargo. Descuadre de lotes !!!"));
            }
        } finally {
            st.close();
            st1.close();
        }
        return liberados;
    }

    public boolean grabarPedidoDetalle(int idEmpresa, TOPedido ped, int idImpuestoZona, TOPedidoProducto prod, double cantFacturadaOld) throws SQLException {
        double cantSolicitada, cantSeparada, cantLiberar, cantLiberada;
        double cantSinCargo, boletinConCargo, boletinSinCargo;
        int idProducto = prod.getIdProducto();
        boolean similares = false;
        String strSQL;

        ResultSet rs;
        this.cnx = this.ds.getConnection();
        Statement st = this.cnx.createStatement();
        try {
            st.execute("BEGIN TRANSACTION");

            if (prod.getCantFacturada() > cantFacturadaOld) {
                cantSolicitada = prod.getCantFacturada() - cantFacturadaOld;

                cantSeparada = cantSolicitada;
                strSQL = "UPDATE pedidosOCTiendaDetalle "
                        + "SET cantFacturada=cantFacturada+" + cantSeparada + " "
                        + "WHERE idPedido=" + ped.getReferencia() + " AND idEmpaque=" + idProducto;
            } else {
                cantLiberar = cantFacturadaOld - prod.getCantFacturada();

                cantLiberada = cantLiberar;
                strSQL = "UPDATE pedidosOCTiendaDetalle "
                        + "SET cantFacturada=cantFacturada-" + cantLiberada + " "
                        + "WHERE idPedido=" + ped.getReferencia() + " AND idEmpaque=" + idProducto;
            }
            st.executeUpdate(strSQL);

            ArrayList<Double> boletin = this.obtenerBoletinSinCargo(idEmpresa, prod.getIdProducto(), ped.getIdReferencia());
            boletinConCargo = boletin.get(0);
            boletinSinCargo = boletin.get(1);
            if (boletinConCargo > 0 && boletinSinCargo > 0) {
                strSQL = "SELECT ISNULL(SUM(D.cantFacturada),0) AS cantFacturada, ISNULL(SUM(D.cantSinCargo),0) AS cantSinCargo\n"
                        + "FROM pedidosOCTiendaDetalle D\n"
                        + "INNER JOIN empaquesSimilares S ON S.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idPedido=" + ped.getReferencia() + " AND S.idSimilar=" + prod.getIdProducto();
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    similares = true;
                    cantSinCargo = ((int) (rs.getDouble("cantFacturada") / boletinConCargo)) * boletinSinCargo;
                    double cantSinCargoHay = rs.getDouble("cantSinCargo");
                    if (cantSinCargo > cantSinCargoHay) {
                        cantSolicitada = cantSinCargo - cantSinCargoHay;

                        cantSeparada = cantSolicitada;
                        strSQL = "UPDATE pedidosOCTiendaDetalle "
                                + "SET cantSinCargo=cantSinCargo+" + cantSeparada + " "
                                + "WHERE idPedido=" + ped.getReferencia() + " AND idEmpaque=" + idProducto;
                        st.executeUpdate(strSQL);
                    } else if (cantSinCargo < cantSinCargoHay) {
                        double disponibles;
                        cantLiberar = cantSinCargoHay - cantSinCargo;

                        strSQL = "SELECT P.principal, P.idEmpaque, P.cantFacturada, P.cantSinCargo, P.unitario, P.idImpuesto\n"
                                + "FROM (SELECT CASE WHEN D.idEmpaque=S.idSimilar THEN 1 ELSE 0 END AS principal\n"
                                + "           , ISNULL(D.idEmpaque,S.idEmpaque) AS idEmpaque\n"
                                + "           , ISNULL(D.cantFacturada, 0) AS cantFacturada, ISNULL(D.cantSinCargo, 0) AS cantSinCargo\n"
                                + "           , ISNULL(D.unitario, 0) AS unitario, P.idImpuesto\n"
                                + "       FROM (SELECT * FROM pedidosOCTiendaDetalle WHERE idPedido=" + ped.getReferencia() + ") D\n"
                                + "	RIGHT JOIN empaquesSimilares S ON S.idEmpaque=D.idEmpaque\n"
                                + "	INNER JOIN empaques E ON E.idEmpaque=S.idEmpaque\n"
                                + "	INNER JOIN productos P ON P.idProducto=E.idProducto\n"
                                + "	WHERE S.idSimilar=" + idProducto + " AND D.idPedido IS NOT NULL AND D.cantSinCargo > 0) P\n"
                                + "ORDER BY P.principal DESC, P.idEmpaque";
                        rs = st.executeQuery(strSQL);
                        while (rs.next()) {
                            cantSinCargo = ((int) (rs.getDouble("cantFacturada") / boletinConCargo)) * boletinSinCargo;
                            if (rs.getDouble("cantSinCargo") > cantSinCargo) {           // Si los que hay, son mas que los que debieran haber
                                disponibles = rs.getDouble("cantSinCargo") - cantSinCargo;  // Entonces si hay disponibles

                                if (disponibles <= cantLiberar) {
                                    cantLiberada = disponibles;
                                } else {
                                    cantLiberada = cantLiberar;
                                }
                                strSQL = "UPDATE pedidosOCTiendaDetalle "
                                        + "SET cantSinCargo=cantSinCargo-" + cantLiberada + " "
                                        + "WHERE idPedido=" + ped.getReferencia() + " AND idEmpaque=" + rs.getInt("idEmpaque");
                                st.executeUpdate(strSQL);

                                cantLiberar -= cantLiberada;
                                if (cantLiberar == 0) {
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    cantSinCargo = ((int) (prod.getCantFacturada() / boletinConCargo)) * boletinSinCargo;
                    if (cantSinCargo > prod.getCantSinCargo()) {
                        cantSolicitada = cantSinCargo - prod.getCantSinCargo();

                        cantSeparada = cantSolicitada;
                        strSQL = "UPDATE pedidosOCTiendaDetalle "
                                + "SET cantSinCargo=cantSinCargo+" + cantSeparada + " "
                                + "WHERE idPedido=" + ped.getReferencia() + " AND idEmpaque=" + idProducto;
                        st.executeUpdate(strSQL);

                        prod.setCantSinCargo(prod.getCantSinCargo() + cantSeparada);
                    } else if (prod.getCantSinCargo() < cantSinCargo) {
                        cantLiberar = prod.getCantSinCargo() - cantSinCargo;

                        cantLiberada = cantLiberar;
                        strSQL = "UPDATE pedidosOCTiendaDetalle "
                                + "SET cantSinCargo=cantSinCargo-" + cantLiberada + " "
                                + "WHERE idPedido=" + ped.getReferencia() + " AND idEmpaque=" + rs.getInt("idEmpaque");
                        st.executeUpdate(strSQL);

                        prod.setCantSinCargo(prod.getCantSinCargo() - cantLiberada);
                    }
                }
            }
            st.execute("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            prod.setCantFacturada(cantFacturadaOld);
            st.execute("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            this.cnx.close();
        }
        return similares;
    }

    public ArrayList<TOMovimientoProducto> grabarMovimientoDetalle(boolean esVenta, int idAlmacen, int idMovto, int idMovtoAlmacen, TOMovimientoProducto to, double separados, int idZonaImpuestos) throws SQLException {
        ArrayList<TOMovimientoProducto> agregados = new ArrayList<TOMovimientoProducto>();
        this.cnx = this.ds.getConnection();
        Statement st = this.cnx.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");

            String strSQL;
            if (esVenta) {
                strSQL = "SELECT A.idEmpresa, M.idReferencia AS idTienda\n"
                        + "FROM movimientos M\n"
                        + "INNER JOIN almacenes A ON A.idAlmacen=M.idAlmacen\n"
                        + "WHERE M.idMovto=" + idMovto;
            } else {
                strSQL = "SELECT A.idEmpresa, P.idTienda\n"
                        + "FROM pedidosOC P\n"
                        + "INNER JOIN almacenes A ON A.idAlmacen=P.idAlmacen\n"
                        + "WHERE P.idPedido=" + idMovto;
            }
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                int idEmpresa = rs.getInt("idEmpresa");
                int idTienda = rs.getInt("idTienda");
                agregados = this.grabaMovimientoDetalle(true, idEmpresa, idAlmacen, idMovto, idMovtoAlmacen, to, idTienda, separados, idZonaImpuestos);
            }
            st.executeUpdate("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw (ex);
        } finally {
            st.close();
            this.cnx.close();
        }
        return agregados;
    }

    private ArrayList<TOMovimientoProducto> grabaMovimientoDetalle(boolean esVenta, int idEmpresa, int idAlmacen, int idMovto, int idMovtoAlmacen, TOMovimientoProducto to, int idTienda, double separados, int idZonaImpuestos) throws SQLException {
        ArrayList<TOMovimientoProducto> agregados = new ArrayList<TOMovimientoProducto>();
        double cantSolicitada, cantSeparada, cantLiberar, cantLiberada;
        int idProducto;
        String strSQL;

        Statement st = this.cnx.createStatement();
        try {
            if (to.getIdMovto() == 0) {
                to.setIdMovto(idMovto);
                this.agregarProductoOficina(idEmpresa, to, idZonaImpuestos, idTienda);
            } else if (to.getCantFacturada() + to.getCantSinCargo() != separados) {
                idProducto = to.getIdProducto();
                if (to.getCantFacturada() > (separados - to.getCantSinCargo())) {
                    cantSolicitada = to.getCantFacturada() - (separados - to.getCantSinCargo());
                    if (esVenta) {
                        // Falta validar si es con o sin pedido
                        cantSeparada = this.separaRelacionados(idAlmacen, idMovtoAlmacen, idProducto, cantSolicitada, true);
                        strSQL = "UPDATE movimientosDetalle\n"
                                + "SET cantFacturada=cantFacturada+" + cantSeparada + "\n"
                                + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + to.getIdProducto();
                    } else {
                        cantSeparada = cantSolicitada;
                        strSQL = "UPDATE pedidosOCTiendaDetalle "
                                + "SET cantFacturada=cantFacturada+" + cantSeparada + " "
                                + "WHERE idPedido=" + idMovto + " AND idEmpaque=" + idProducto;
                    }
                    st.executeUpdate(strSQL);
                } else {
                    cantLiberar = (separados - to.getCantSinCargo()) - to.getCantFacturada();
                    if (esVenta) {
                        // Falta validar si es con o sin pedido
                        this.liberaRelacionados(idAlmacen, idMovtoAlmacen, idProducto, cantLiberar);
                        strSQL = "UPDATE movimientosDetalle\n"
                                + "SET cantFacturada=cantFacturada-" + cantLiberar + "\n"
                                + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + idProducto;
                    } else {
                        cantSeparada = cantLiberar;
                        strSQL = "UPDATE pedidosOCTiendaDetalle "
                                + "SET cantFacturada=cantFacturada-" + cantSeparada + " "
                                + "WHERE idPedido=" + idMovto + " AND idEmpaque=" + idProducto;
                    }
                    st.executeUpdate(strSQL);
                }
                ArrayList<Double> boletin = this.obtenerBoletinSinCargo(idEmpresa, to.getIdProducto(), idTienda);
                double boletinConCargo = boletin.get(0);
                double boletinSinCargo = boletin.get(1);
                if (boletinConCargo > 0 && boletinSinCargo > 0) {
                    if (esVenta) {
                        // Falta validar si es con o sin pedido
                        strSQL = "SELECT SUM(D.cantFacturada) AS cantFacturada, SUM(D.cantSinCargo) AS cantSinCargo\n"
                                + "FROM (SELECT idEmpaque FROM empaquesSimilares WHERE idSimilar=" + idProducto + ") S\n"
                                + "INNER JOIN (SELECT * FROM movimientosDetalle WHERE idMovto=" + idMovto + ") D ON D.idEmpaque=S.idEmpaque";
                    } else {
                        strSQL = "SELECT SUM(D.cantFacturada) AS cantFacturada, SUM(D.cantSinCargo) AS cantSinCargo\n"
                                + "FROM (SELECT idEmpaque FROM empaquesSimilares WHERE idSimilar=" + idProducto + ") S\n"
                                + "INNER JOIN (SELECT * FROM pedidosOCTiendaDetalle WHERE idPedido=" + idMovto + ") D ON D.idEmpaque=S.idEmpaque";
                    }
                    ResultSet rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        double cantSinCargoSimilares = ((int) (rs.getDouble("cantFacturada") / boletinConCargo)) * boletinSinCargo;
                        if (cantSinCargoSimilares != rs.getDouble("cantSinCargo")) {
                            if (cantSinCargoSimilares > rs.getDouble("cantSinCargo")) {
                                cantSolicitada = cantSinCargoSimilares - rs.getDouble("cantSinCargo");
                                if (esVenta) {
                                    // Falta validar si es con o sin pedido
                                    strSQL = "SELECT P.principal, P.idPedido, P.idEmpaque, P.cantFacturada, P.cantSinCargo\n"
                                            + "       , P.unitario, P.idImpuesto, L.fechaCaducidad, L.saldo-L.separados AS disponibles\n"
                                            + "FROM (SELECT CASE WHEN D.idEmpaque=S.idSimilar THEN 1 ELSE 0 END AS principal\n"
                                            + "         , ISNULL(D.idPedido, 0) AS idPedido, ISNULL(D.idEmpaque,S.idEmpaque) AS idEmpaque\n"
                                            + "         , ISNULL(D.cantFacturada, 0) AS cantFacturada, ISNULL(D.cantSinCargo, 0) AS cantSinCargo\n"
                                            + "         , ISNULL(D.unitario, 0) AS unitario, P.idImpuesto\n"
                                            + "     FROM (SELECT * FROM pedidosOCTiendaDetalle WHERE idPedido=" + idMovto + ") D\n"
                                            + "     RIGHT JOIN empaquesSimilares S ON S.idEmpaque=D.idEmpaque\n"
                                            + "     INNER JOIN empaques E ON E.idEmpaque=S.idEmpaque\n"
                                            + "     INNER JOIN productos P ON P.idProducto=E.idProducto\n"
                                            + "     WHERE S.idSimilar=" + idProducto + ") P\n"
                                            + "LEFT JOIN almacenesLotes L ON L.idAlmacen=" + idAlmacen + " AND L.idEmpaque=P.idEmpaque\n"
                                            + "WHERE L.idAlmacen IS NOT NULL AND L.saldo-L.separados > 0\n"
                                            + "ORDER BY P.principal DESC, P.idPedido DESC, L.fechaCaducidad";
                                } else {
                                    strSQL = "SELECT 1 AS principal, idPedido, idEmpaque, cantFacturada, cantSinCargo, unitario, idImpuesto\n"
                                            + "FROM pedidosOCTiendaDetalle\n"
                                            + "WHERE idPedido=" + idMovto + " AND idEmpaque=" + idProducto;
                                }
                                rs = st.executeQuery(strSQL);
                                while (rs.next()) {
                                    if (esVenta) {
                                        cantSeparada = this.separaRelacionados(idAlmacen, idMovtoAlmacen, idProducto, cantSolicitada, true);
                                        strSQL = "UPDATE movimientosDetalle\n"
                                                + "SET cantFacturada=cantFacturada+" + cantSeparada + "\n"
                                                + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + rs.getInt("idEmpaque");
                                    } else {
                                        cantSeparada = cantSolicitada;
                                        strSQL = "UPDATE pedidosOCTiendaDetalle "
                                                + "SET cantSinCargo=cantSinCargo+" + cantSeparada + " "
                                                + "WHERE idPedido=" + idMovto + " AND idEmpaque=" + rs.getInt("idEmpaque");

                                    }
                                    st.executeUpdate(strSQL);
                                    cantSolicitada -= cantSeparada;
                                    if (cantSolicitada == 0) {
                                        break;
                                    }
                                }
                            } else {
                                cantLiberar = rs.getDouble("cantSinCargo") - cantSinCargoSimilares;
                                if (esVenta) {
                                } else {
                                    strSQL = "SELECT P.principal, P.idEmpaque, P.cantFacturada, P.cantSinCargo, P.unitario, P.idImpuesto*\n"
                                            + "FROM (SELECT CASE WHEN D.idEmpaque=S.idSimilar THEN 1 ELSE 0 END AS principal\n"
                                            + "		, ISNULL(D.idEmpaque,S.idEmpaque) AS idEmpaque\n"
                                            + "		, ISNULL(D.cantFacturada, 0) AS cantFacturada, ISNULL(D.cantSinCargo, 0) AS cantSinCargo\n"
                                            + "		, ISNULL(D.unitario, 0) AS unitario, P.idImpuesto\n"
                                            + "	FROM (SELECT * FROM pedidosOCTiendaDetalle WHERE idPedido=" + idMovto + ") D\n"
                                            + "	RIGHT JOIN empaquesSimilares S ON S.idEmpaque=D.idEmpaque\n"
                                            + "	INNER JOIN empaques E ON E.idEmpaque=S.idEmpaque\n"
                                            + "	INNER JOIN productos P ON P.idProducto=E.idProducto\n"
                                            + "	WHERE S.idSimilar=837 AND D.idPedido IS NOT NULL AND D.cantSinCargo > 0) P\n"
                                            + "ORDER BY P.principal DESC, P.idEmpaque";
                                }
                                rs = st.executeQuery(strSQL);
                                while (rs.next()) {
                                    if (esVenta) {
                                    } else {
                                        if (rs.getDouble("cantSinCargo") <= cantLiberar) {
                                            cantLiberada = rs.getDouble("cantSinCargo");
                                        } else {
                                            cantLiberada = cantLiberar;
                                        }
                                        strSQL = "UPDATE pedidosOCTiendaDetalle "
                                                + "SET cantSinCargo=cantSinCargo" + cantLiberada + " "
                                                + "WHERE idPedido=" + idMovto + " AND idEmpaque=" + rs.getInt("idEmpaque");
                                        cantLiberar -= cantLiberada;
                                        if (cantLiberar == 0) {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
//            st.executeUpdate("COMMIT TRANSACTION");
//        } catch (SQLException ex) {
//            st.executeUpdate("ROLLBACK TRANSACTION");
//            throw (ex);
        } finally {
            st.close();
//            this.cnx.close();
        }
        return agregados;
    }

//  ====================================== INICIA PROCEDIMIENTO DE VENTAS =================================
    public void agregarProductoSalidaOficina(int idMovto, TOSalidaOficinaProducto to) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        String strSQL;
        try {
            strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantOrdenada, cantFacturada, cantSinCargo, cantRecibida, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior) "
                    + "VALUES (" + idMovto + ", " + to.getIdProducto() + ", 0, " + to.getCantidad() + ", 0, 0, 0, 0, 0, 0, 0, 0, GETDATE(), 0)";
            st.executeUpdate(strSQL);
        } finally {
            st.close();
            cn.close();
        }
    }

    public void liberaSalida(int idMovto, int idAlmacen, int idProducto, double cantidad) throws SQLException {
        String strSQL;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");

            this.liberaOficina(idAlmacen, idProducto, cantidad, st);

            strSQL = "UPDATE movimientosDetalle "
                    + "SET cantFacturada=cantFacturada-" + cantidad + " "
                    + "WHERE idMovto=" + idMovto + " AND idProducto=" + idProducto;
            st.executeUpdate(strSQL);

            st.executeUpdate("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            cn.close();
        }
    }

    private void liberaOficina(int idAlmacen, int idProducto, double cantidad, Statement st) throws SQLException {
        String strSQL = "UPDATE almacenesEmpaques "
                + "SET separados=separados-" + cantidad + " "
                + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto;
        st.executeUpdate(strSQL);
    }

    public double separaSalida(int idMovto, int idAlmacen, int idProducto, double cantidad) throws SQLException {
        String strSQL;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");

            cantidad = this.separaOficina(idAlmacen, idProducto, cantidad, st);

            strSQL = "UPDATE movimientosDetalle "
                    + "SET cantFacturada=cantFacturada+" + cantidad + " "
                    + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + idProducto;
            st.executeUpdate(strSQL);

            st.executeUpdate("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            cn.close();
        }
        return cantidad;
    }

    private double separaOficina(int idAlmacen, int idProducto, double cantidad, Statement st) throws SQLException {
        String strSQL = "SELECT existenciaOficina-separados AS saldo "
                + "FROM almacenesEmpaques "
                + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto;
        ResultSet rs = st.executeQuery(strSQL);
        if (rs.next()) {
            if (rs.getDouble("saldo") <= 0) {
                cantidad = 0;
                throw new SQLException("No hay existencia para salida");
            } else if (rs.getDouble("saldo") < cantidad) {
                cantidad = rs.getDouble("saldo");
            }
        } else {
            throw new SQLException("No se encontro el producto en tabla(almacenesEmpaques)");
        }
        strSQL = "UPDATE almacenesEmpaques "
                + "SET separados=separados+" + cantidad + " "
                + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto;
        st.executeUpdate(strSQL);

        return cantidad;
    }

    public int agregarMovimientoOficina(TOMovimientoOficina to) throws SQLException {
        int idMovto = 0;
//        String strSQL = "INSERT INTO movimientos (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, idImpuestoZona, desctoComercial, desctoProntoPago, fecha, idUsuario, idMoneda, tipoCambio, estatus) "
//                + "VALUES(" + to.getIdTipo() + ", " + to.getIdCedis() + ", " + to.getIdEmpresa() + ", " + to.getIdAlmacen() + ", 0, 0, 0, 0, 0, GETDATE(), " + this.idUsuario + ", 1, 1, 0)";
        String strSQL="";
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");

            st.executeUpdate(strSQL);
            ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
            if (rs.next()) {
                idMovto = rs.getInt("idMovto");
            }
            st.executeUpdate("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            cn.close();
        }
        return idMovto;
    }

    public void cancelarSalidaAlmacen(int idMovto) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        String strSQL;
        try {
            st.executeUpdate("BEGIN TRANSACTION");

            strSQL = "UPDATE l "
                    + "SET l.separados=l.separados-k.cantidad "
                    + "FROM movimientosAlmacenDetalle k "
                    + "INNER JOIN almacenesLotes l ON l.idAlmacen=k.idAlmacen AND l.idEmpaque=k.idEmpaque AND l.lote=k.lote "
                    + "WHERE k.idMovto=" + idMovto;
            st.executeUpdate(strSQL);

            strSQL = "DELETE FROM movimientosAlmacenDetalle where idMovto=" + idMovto;
            st.executeUpdate(strSQL);

            strSQL = "DELETE FROM movimientosAlmacen WHERE idMovtoAlmacen=" + idMovto;
            st.executeUpdate(strSQL);

            st.executeUpdate("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            cn.close();
        }
    }

    public ArrayList<TOMovimientoOficina> movimientosPendientes(boolean oficina, int entrada) throws SQLException {
        ArrayList<TOMovimientoOficina> lista = new ArrayList<TOMovimientoOficina>();
        String tabla = "movimientosAlmacen";
        if (oficina) {
            tabla = "movimientos";
        }
        String strSQL = "SELECT m.* "
                + "FROM " + tabla + " m "
                + "INNER JOIN movimientosTipos t ON t.idTipo=m.idTipo "
                + "WHERE m.idCedis=" + this.idCedis + " AND m.estatus=0 AND t.eliminable=1 AND t.suma=" + entrada + " "
                + "ORDER BY m.idAlmacen";
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                lista.add(this.construirMovimientoAlmacen(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return lista;
    }

    private TOMovimientoOficina construirMovimientoAlmacen(ResultSet rs) throws SQLException {
        TOMovimientoOficina to = new TOMovimientoOficina();
        to.setIdMovto(rs.getInt("idMovto"));
        to.setIdTipo(rs.getInt("idTipo"));
        to.setFolio(rs.getInt("folio"));
//        to.setIdCedis(rs.getInt("idCedis"));
//        to.setIdEmpresa(rs.getInt("idEmpresa"));
        to.setIdAlmacen(rs.getInt("idAlmacen"));
        to.setIdComprobante(rs.getInt("idComprobante"));
        java.sql.Date f = rs.getDate("fecha");
        to.setFecha(new java.util.Date(f.getTime()));
        to.setIdUsuario(rs.getInt("idUsuario"));
        to.setEstatus(rs.getInt("estatus"));
        return to;
    }

    public void grabarSalidaAlmacen(TOMovimientoOficina to) throws SQLException {
        ResultSet rs, rs1;

        String strSQL;
        int folio;

        this.cnx = this.ds.getConnection();
        Statement st = this.cnx.createStatement();
        Statement st1 = this.cnx.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");

            folio = this.obtenerMovimientoFolio(false, to.getIdAlmacen(), to.getIdTipo());

            strSQL = "UPDATE movimientosAlmacen SET fecha=GETDATE(), estatus=1, folio=" + folio + ", idUsuario=" + this.idUsuario + " "
                    + "WHERE idMovtoAlmacen=" + to.getIdMovto();
            st.executeUpdate(strSQL);

            strSQL = "SELECT * FROM movimientosAlmacenDetalle "
                    + "WHERE idAlmacen=" + to.getIdAlmacen() + " AND idMovto=" + to.getIdMovto() + " "
                    + "ORDER BY idEmpaque";
            rs = st.executeQuery(strSQL);
            while (rs.next()) {
                strSQL = "SELECT saldo, separados FROM almacenesLotes "
                        + "WHERE idAlmacen=" + to.getIdAlmacen() + " AND idEmpaque=" + rs.getInt("idEmpaque") + " AND lote='" + rs.getString("lote") + "'";
                rs1 = st1.executeQuery(strSQL);
                if (rs1.next()) {
                    if (rs1.getDouble("saldo") < rs.getDouble("cantidad")) {
                        throw new SQLException("El saldo del lote es menor a la cantidad separada en kardex del movimiento");
                    } else if (rs1.getDouble("separados") < rs.getDouble("cantidad")) {
                        throw new SQLException("La cantidad separada del lote es menor a la cantidad separada en kardex del movimiento");
                    }
                } else {
                    throw new SQLException("No se encontro lote(" + rs.getString("lote") + ") del empaque(" + rs.getInt("idEmpaque") + ") en tabla almacenesLotes");
                }
                strSQL = "UPDATE movimientosAlmacenDetalle "
                        + "SET fecha=GETDATE(), existenciaAnterior=" + rs1.getDouble("saldo") + " "
                        + "WHERE idAlmacen=" + to.getIdAlmacen() + " AND idMovto=" + to.getIdMovto() + " AND idEmpaque=" + rs.getInt("idEmpaque") + " AND lote='" + rs.getString("lote") + "'";
                st1.executeUpdate(strSQL);

                strSQL = "UPDATE almacenesLotes "
                        + "SET saldo=saldo-" + rs.getDouble("cantidad") + ", separados=separados-" + rs.getDouble("cantidad") + " "
                        + "WHERE idAlmacen=" + to.getIdAlmacen() + " AND idEmpaque=" + rs.getInt("idEmpaque") + " AND lote='" + rs.getString("lote") + "'";
                st1.executeUpdate(strSQL);
            }
            st.executeUpdate("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            this.cnx.close();
        }
    }

    public int agregarMovimientoAlmacen(TOMovimientoOficina to) throws SQLException {
        int idMovto = 0;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");
//            String strSQL = "INSERT INTO movimientosAlmacen (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, fecha, idUsuario, estatus) "
//                    + "VALUES (" + to.getIdTipo() + ", " + to.getIdCedis() + ", " + to.getIdEmpresa() + ", " + to.getIdAlmacen() + ", 0, 0, GETDATE(), " + this.idUsuario + ", 0)";
            String strSQL="";
            st.executeUpdate(strSQL);

            ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
            if (rs.next()) {
                idMovto = rs.getInt("idMovto");
            }
            st.executeUpdate("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            cn.close();
        }
        return idMovto;
    }

    public MovimientoTipo obtenerMovimientoTipo(int idTipo) throws SQLException {
        MovimientoTipo t = null;
        String strSQL = "SELECT tipo FROM movimientosTipos WHERE idTipo=" + idTipo;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                t = new MovimientoTipo(idTipo, rs.getString("tipo"));
            }
        } finally {
            st.close();
            cn.close();
        }
        return t;
    }

    public ArrayList<MovimientoTipo> obtenerMovimientosTipos(boolean suma) throws SQLException {
        ArrayList<MovimientoTipo> lst = new ArrayList<MovimientoTipo>();
        String strSQL = "SELECT idTipo, tipo FROM movimientosTipos WHERE suma=" + (suma ? 1 : 0) + " AND eliminable=1";
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                lst.add(new MovimientoTipo(rs.getInt("idTipo"), rs.getString("tipo")));
            }
        } finally {
            st.close();
            cn.close();
        }
        return lst;
    }

    public void grabarTraspasoRecepcion(TOMovimientoOficina m, ArrayList<TOEntradaProducto> detalle) throws SQLException {
        String strSQL;
        ResultSet rs;
        Statement st;
//        int idMovtoAlmacen=0;
        double existenciaAnterior, promedioPonderado, existenciaOficina, saldo;
        Connection cn = this.ds.getConnection();
        st = cn.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");

//            strSQL="SELECT m.idMovto " +
//                    "FROM movimientosAlmacen m " +
//                    "WHERE m.idAlmacen="+m.getIdAlmacen()+" AND m.idTipo=9 and folio=(SELECT numero FROM comprobantes WHERE idComprobante="+m.getIdReferencia()+")";
//            rs=st.executeQuery(strSQL);
//            if(rs.next()) {
//                idMovtoAlmacen=rs.getInt("idMovto");
//            } else {
//                throw new SQLException("No se encontro el movimiento de almacen con la referencia("+m.getIdReferencia()+")");
//            }
//            idMovtoAlmacen=m.getIdMovtoAlmacen();

            strSQL = "UPDATE movimientosAlmacen SET fecha=GETDATE(), estatus=1 WHERE idMovtoAlmacen=" + m.getIdMovtoAlmacen();
            st.executeUpdate(strSQL);

            strSQL = "UPDATE movimientos SET fecha=GETDATE(), estatus=1 WHERE idMovto=" + m.getIdMovto();
            st.executeUpdate(strSQL);

            for (TOEntradaProducto to : detalle) {
                strSQL = "SELECT existenciaOficina FROM almacenesEmpaques "
                        + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=" + to.getIdProducto();
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    existenciaAnterior = rs.getDouble("existenciaOficina");
                    strSQL = "UPDATE almacenesEmpaques SET existenciaOficina=existenciaOficina+" + to.getCantFacturada() + " "
                            + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=" + to.getIdProducto();
                } else {
                    existenciaAnterior = 0;
                    strSQL = "INSERT INTO almacenesEmpaques (idAlmacen, idEmpaque, existenciaOficina, separados, existenciaAlmacen, existenciaMinima, existenciaMaxima) "
                            + "VALUES (" + m.getIdAlmacen() + ", " + to.getIdProducto() + ", " + to.getCantFacturada() + ", 0, 0, 0, 0)";
                }
                st.executeUpdate(strSQL);

//                strSQL="INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantOrdenada, cantFacturada, cantSinCargo, cantRecibida, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior) "
//                        + "VALUES ("+m.getIdMovto()+", "+to.getIdProducto()+", 0, "+to.getCantFacturada()+", 0, 0, "+to.getCosto()+", 0, 0, 0, "+to.getUnitario()+", 0, GETDATE(), "+existenciaAnterior+")";
                strSQL = "UPDATE movimientosDetalle "
                        + "SET cantFacturada=" + to.getCantFacturada() + ", existenciaAnterior=" + existenciaAnterior + ", fecha=GETDATE() "
                        + "WHERE idMovto=" + m.getIdMovto() + " AND idEmpaque=" + to.getIdProducto();
                st.executeUpdate(strSQL);

//                strSQL = "SELECT promedioPonderado, existenciaOficina FROM empresasEmpaques "
//                        + "WHERE idEmpresa=" + m.getIdEmpresa() + " AND idEmpaque=" + to.getIdProducto();
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    promedioPonderado = rs.getDouble("promedioPonderado");
                    existenciaOficina = rs.getDouble("existenciaOficina");
                    promedioPonderado = (promedioPonderado * existenciaOficina + to.getUnitario() * to.getCantFacturada()) / (existenciaOficina + to.getCantFacturada());
//                    strSQL = "UPDATE empresasEmpaques "
//                            + "SET promedioPonderado=" + promedioPonderado + ", existenciaOficina=existenciaOficina+" + to.getCantFacturada() + " "
//                            + "WHERE idEmpresa=" + m.getIdEmpresa() + " AND idEmpaque=" + to.getIdProducto();
                } else {
//                    strSQL = "INSERT INTO empresasEmpaques (idEmpresa, idEmpaque, promedioPonderado, existenciaOficina, idMovtoUltimaEntrada) "
//                            + "VALUES (" + m.getIdEmpresa() + ", " + to.getIdProducto() + ", " + to.getUnitario() + ", " + to.getCantFacturada() + ", 0)";
                }
                st.executeUpdate(strSQL);

                for (Lote1 l : to.getLotes()) {
                    strSQL = "SELECT saldo FROM almacenesLotes "
                            + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=" + to.getIdProducto() + " AND lote='" + l.getLote() + "'";
                    rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        saldo = rs.getDouble("saldo");
                        strSQL = "UPDATE almacenesLotes "
                                + "SET saldo=saldo+" + l.getSeparados() + ", cantidad=cantidad+" + l.getSeparados() + " "
                                + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=" + to.getIdProducto() + " AND lote='" + l.getLote() + "'";
                    } else {
                        saldo = 0;
                        strSQL = "INSERT INTO almacenesLotes (idAlmacen, idEmpaque, lote, fechaCaducidad, cantidad, saldo, separados, existenciaFisica) "
                                + "VALUES (" + m.getIdAlmacen() + ", " + to.getIdProducto() + ", '" + l.getLote() + "', '" + new java.sql.Date(l.getFechaCaducidad().getTime()) + "', " + l.getSeparados() + ", " + l.getSeparados() + ", 0, 0)";
                    }
                    st.executeUpdate(strSQL);

//                    strSQL="INSERT INTO movimientosAlmacenDetalle (idAlmacen, idMovto, idEmpaque, lote, cantidad, suma, fecha, existenciaAnterior) "
//                            + "VALUES("+m.getIdAlmacen()+", "+idMovtoAlmacen+", "+to.getIdProducto()+", '"+l.getLote()+"', "+l.getSeparados()+", 1, GETDATE(), "+saldo+")";
                    strSQL = "UPDATE movimientosAlmacenDetalle "
                            + "SET cantidad=" + l.getSeparados() + ", existenciaAnterior=" + saldo + ", fecha=GETDATE() "
                            + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idMovto=" + m.getIdMovtoAlmacen() + " AND idEmpaque=" + to.getIdProducto() + " AND lote='" + l.getLote() + "'";
                    st.executeUpdate(strSQL);
                }
            }
            st.executeUpdate("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            cn.close();
        }
    }

    public int obtenerIdMovto(int idAlmacen, int idTipo, String folio) throws SQLException {
        int idMovto = 0;
        String strSQL = "SELECT idMovto FROM movimientos "
                + "WHERE idAlmacen=" + idAlmacen + " AND idTipo=" + idTipo + " AND folio=" + folio;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                idMovto = rs.getInt("idMovto");
            } else {
                throw new SQLException("No se encotro el movimiento folio(" + folio + ") en el almacen(" + idAlmacen + ")");
            }
        } finally {
            st.close();
            cn.close();
        }
        return idMovto;
    }

//    public int obtenerIdMovtoAlmacen(int idAlmacen, int idTipo, String folio) throws SQLException {
//        String strSQL;
//        int idMovto=0;
//        Connection cn=this.ds.getConnection();
//        Statement st=cn.createStatement();
//        try {
//            strSQL="SELECT idMovto FROM movimientosAlmacen WHERE idAlmacen="+idAlmacen+" AND idTipo="+idTipo+" AND folio="+folio;
//            ResultSet rs=st.executeQuery(strSQL);
//            if(rs.next()) {
//                idMovto=rs.getInt("idMovto");
//            } else {
//                throw new SQLException("No se encotro el movimientoAlmacen folio("+folio+") en el almacen("+idAlmacen+")");
//            }
//        } finally {
//            cn.close();
//        }
//        return idMovto;
//    }
    public void grabarTraspasoEnvio(int idAlmacenDestino, TOMovimientoOficina m, ArrayList<TOMovimientoProducto> detalle) throws SQLException {
        String strSQL;
        ResultSet rs;
        Statement st, st1;
        double sumaLotes, costo;

        Connection cn = this.ds.getConnection();
        st = cn.createStatement();
        st1 = cn.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");

            strSQL = "UPDATE movimientosAlmacen SET fecha=GETDATE() WHERE idMovtoAlmacen=" + m.getIdMovtoAlmacen();
            st.executeUpdate(strSQL);

            strSQL = "UPDATE movimientos SET fecha=GETDATE(), estatus=1 WHERE idMovto=" + m.getIdMovto();
            st.executeUpdate(strSQL);

            for (TOMovimientoProducto d : detalle) {
                sumaLotes = 0;
                strSQL = "SELECT K.lote, K.cantidad, ISNULL(L.saldo, 0) AS saldo "
                        + "FROM movimientosAlmacenDetalle K "
                        + "LEFT JOIN almacenesLotes L ON L.idAlmacen=K.idAlmacen AND L.idEmpaque=K.idEmpaque AND L.lote=K.lote "
                        + "WHERE K.idAlmacen=" + m.getIdAlmacen() + " AND K.idMovto=" + m.getIdMovtoAlmacen() + " AND K.idEmpaque=" + d.getIdProducto();
                rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    if (rs.getDouble("saldo") < rs.getDouble("cantidad")) {
                        throw new SQLException("No hay saldo o No se encontro el lote(" + rs.getString("lote") + ") del producto(" + d.getIdProducto() + ") en el almacen");
                    }
                    strSQL = "UPDATE movimientosAlmacenDetalle "
                            + "SET existenciaAnterior=" + rs.getDouble("saldo") + ", fecha=GETDATE() "
                            + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idMovto=" + m.getIdMovtoAlmacen() + " AND idEmpaque=" + d.getIdProducto() + " AND lote='" + rs.getString("lote") + "'";
                    st1.executeUpdate(strSQL);

                    strSQL = "UPDATE almacenesLotes "
                            + "SET saldo=saldo-" + rs.getDouble("cantidad") + ", separados=separados-" + rs.getDouble("cantidad") + " "
                            + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=" + d.getIdProducto() + " AND lote='" + rs.getString("lote") + "'";
                    st1.executeUpdate(strSQL);

                    sumaLotes += rs.getDouble("cantidad");
                }
                if (sumaLotes != d.getCantFacturada()) {
                    throw new SQLException("Diferencia entre lotes y cantidad Facturada del producto(" + d.getIdProducto() + ")");
                } else if (sumaLotes > 0) {
                    strSQL = "SELECT AE.existenciaOficina AS saldo, ISNULL(EE.existenciaOficina, 0) AS existencia, ISNULL(EE.promedioPonderado, 0) AS costo "
                            + "FROM almacenesEmpaques AE "
                            + "INNER JOIN almacenes A ON A.idAlmacen=AE.idAlmacen "
                            + "LEFT JOIN empresasEmpaques EE ON EE.idEmpresa=A.idEmpresa AND EE.idEmpaque=AE.idEmpaque "
                            + "WHERE AE.idAlmacen=" + m.getIdAlmacen() + " AND AE.idEmpaque=" + d.getIdProducto();
                    rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        if (rs.getDouble("existencia") < d.getCantFacturada()) {
                            throw new SQLException("No hay capas de costos suficientes o No se encontro el producto(" + d.getIdProducto() + ") en la empresa");
                        } else if (rs.getDouble("costo") < 0) {
                            throw new SQLException("Costo no valido (menor que cero)");
                        }
                    } else {
                        throw new SQLException("No se encontro producto(" + d.getIdProducto() + ") en el almacen");
                    }
                    d.setCosto(rs.getDouble("costo"));
                    d.setUnitario(rs.getDouble("costo"));
                    costo = rs.getDouble("costo");
                    sumaLotes = rs.getDouble("existencia");

                    strSQL = "UPDATE movimientosDetalle "
                            + "SET costo=" + d.getCosto() + ", unitario=" + d.getUnitario() + ", cantFacturada=" + d.getCantFacturada() + ", existenciaAnterior=" + rs.getDouble("saldo") + ", fecha=GETDATE() "
                            + "WHERE idMovto=" + m.getIdMovto() + " AND idEmpaque=" + d.getIdProducto();
                    st.executeUpdate(strSQL);

                    strSQL = "UPDATE almacenesEmpaques "
                            + "SET existenciaOficina=existenciaOficina-" + d.getCantFacturada() + ", separados=separados-" + d.getCantFacturada() + " "
                            + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=" + d.getIdProducto();
                    st.executeUpdate(strSQL);

                    if (d.getCantFacturada() == sumaLotes) {
                        costo = 0;    // Cuando ya no hay existencia el costo promedio de la empresa se hace cero
                    }
//                    strSQL = "UPDATE empresasEmpaques SET existenciaOficina=existenciaOficina-" + d.getCantFacturada() + ", promedioPonderado=" + costo + " "
//                            + "WHERE idEmpresa=" + m.getIdEmpresa() + " AND idEmpaque=" + d.getIdProducto();
                    st.executeUpdate(strSQL);
                }
            }
            // ----------------------- SECCION: CREAR ENLACE ENVIO-RECEPCION ------------------
            int folioRecepcion, folioRecepcionAlmacen, idComprobante;
            strSQL = "SELECT folio FROM movimientosFolios WHERE idAlmacen=" + m.getIdAlmacen() + " AND idTipo=9";
            rs = st.executeQuery(strSQL);
            if (rs.next()) {
                folioRecepcion = rs.getInt("folio");
                strSQL = "UPDATE movimientosFolios SET folio=folio+1 WHERE idAlmacen=" + m.getIdAlmacen() + " AND idTipo=9";
            } else {
                folioRecepcion = 1;
                strSQL = "INSERT INTO movimientosFolios (idAlmacen, idTipo, folio) VALUES (" + m.getIdAlmacen() + ", 9, 2)";
            }
            st.executeUpdate(strSQL);

            strSQL = "UPDATE comprobantes SET numero=" + folioRecepcion + " "
                    + "WHERE idComprobante=" + m.getIdComprobante();
            st.executeUpdate(strSQL);
            // ------------------------- SECCION: CREAR RECEPCION ---------------------
            strSQL = "SELECT folio FROM movimientosFoliosAlmacen WHERE idAlmacen=" + m.getIdAlmacen() + " AND idTipo=9";
            rs = st.executeQuery(strSQL);
            if (rs.next()) {
                folioRecepcionAlmacen = rs.getInt("folio");
                strSQL = "UPDATE movimientosFoliosAlmacen SET folio=folio+1 WHERE idAlmacen=" + m.getIdAlmacen() + " AND idTipo=9";
            } else {
                folioRecepcionAlmacen = 1;
                strSQL = "INSERT INTO movimientosFoliosAlmacen (idAlmacen, idTipo, folio) VALUES (" + m.getIdAlmacen() + ", 9, 2)";
            }
            st.executeUpdate(strSQL);

            int idCedisDestino = 0;
            strSQL = "SELECT idCedis FROM almacenes WHERE idAlmacen=" + idAlmacenDestino;
            rs = st.executeQuery(strSQL);
            if (rs.next()) {
                idCedisDestino = rs.getInt("idCedis");
            } else {
                throw new SQLException("No se encontro almacen=" + idAlmacenDestino);
            }
            int idMovto = 0;
//            strSQL = "INSERT INTO movimientos (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, idImpuestoZona, desctoComercial, desctoProntoPago, fecha, idUsuario, idMoneda, tipoCambio, estatus) "
//                    + "VALUES(9, " + idCedisDestino + ", " + m.getIdEmpresa() + ", " + idAlmacenDestino + ", " + folioRecepcion + ", 0, 0, 0, 0, GETDATE(), " + this.idUsuario + ", 1, 1, 0)";
            st.executeUpdate(strSQL);
            rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
            if (rs.next()) {
                idMovto = rs.getInt("idMovto");
            }
            idComprobante = 0;
            // Se crea el comprobante de la recepcion, 
            strSQL = "INSERT INTO comprobantes (idAlmacen, idProveedor, tipoComprobante, remision, serie, numero, idUsuario, fecha, statusOficina, statusAlmacen, propietario) "
                    + "VALUES(" + m.getIdAlmacen() + ", " + idAlmacenDestino + ", 0, '" + idMovto + "', '', '" + m.getFolio() + "', " + this.idUsuario + ", GETDATE(), 0, 0, 0)";
            st.executeUpdate(strSQL);
            rs = st.executeQuery("SELECT @@IDENTITY AS idComprobante");
            if (rs.next()) {
                idComprobante = rs.getInt("idComprobante");
            }
            strSQL = "UPDATE movimientos SET idComprobante=" + idComprobante + " WHERE idMovto=" + idMovto;
            st.executeUpdate(strSQL);

            int idMovtoAlmacen = 0;
//            strSQL = "INSERT INTO movimientosAlmacen (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, fecha, idUsuario) "
//                    + "VALUES (9, " + idCedisDestino + ", " + m.getIdEmpresa() + ", " + idAlmacenDestino + ", " + folioRecepcionAlmacen + ", " + idComprobante + ", GETDATE(), " + this.idUsuario + ")";
            st.executeUpdate(strSQL);
            rs = st.executeQuery("SELECT @@IDENTITY AS idMovtoAlmacen");
            if (rs.next()) {
                idMovtoAlmacen = rs.getInt("idMovtoAlmacen");
            }
            strSQL = "INSERT INTO movimientosRelacionados (idMovto, idMovtoAlmacen) VALUES (" + idMovto + ", " + idMovtoAlmacen + ")";
            st.executeUpdate(strSQL);

            for (TOMovimientoProducto d : detalle) {
                if (d.getCantFacturada() > 0) {
                    strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantOrdenada, cantFacturada, cantSinCargo, cantRecibida, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior) "
                            + "VALUES (" + idMovto + ", " + d.getIdProducto() + ", " + d.getCantFacturada() + ", " + d.getCantFacturada() + ", 0, 0, " + d.getCosto() + ", 0, 0, 0, " + d.getUnitario() + ", " + d.getIdImpuestoGrupo() + ", GETDATE(), 0)";
                    st.executeUpdate(strSQL);

                    strSQL = "SELECT K.lote, K.cantidad "
                            + "FROM movimientosAlmacenDetalle K "
                            + "WHERE K.idAlmacen=" + m.getIdAlmacen() + " AND K.idMovto=" + m.getIdMovtoAlmacen() + " AND K.idEmpaque=" + d.getIdProducto() + " AND K.cantidad>0";
                    rs = st.executeQuery(strSQL);
                    while (rs.next()) {
                        strSQL = "INSERT INTO movimientosAlmacenDetalle (idAlmacen, idMovto, idEmpaque, lote, cantidad, suma, fecha, existenciaAnterior) "
                                + "VALUES(" + idAlmacenDestino + ", " + idMovtoAlmacen + ", " + d.getIdProducto() + ", '" + rs.getString("lote") + "', " + rs.getDouble("cantidad") + ", 1, GETDATE(), 0)";
                        st1.executeUpdate(strSQL);
                    }
                }
            }
            st.executeUpdate("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            cn.close();
        }
    }

    public boolean grabarTraspasoSolicitud(TOMovimientoOficina solicitud, ArrayList<MovimientoProducto> productos) throws SQLException {
        boolean ok = true;
        int folio;
        int idMovto, idMovtoAlmacen;
        int idComprobante;
        String strSQL;
        ResultSet rs;

        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");

            folio = 0;
            strSQL = "SELECT folio FROM movimientosFolios WHERE idAlmacen=" + solicitud.getIdAlmacen() + " AND idTipo=35";
            rs = st.executeQuery(strSQL);
            if (rs.next()) {
                folio = rs.getInt("folio");
                strSQL = "UPDATE movimientosFolios SET folio=folio+1 WHERE idAlmacen=" + solicitud.getIdAlmacen() + " AND idTipo=35";
            } else {
                folio = 1;
                strSQL = "INSERT INTO movimientosFolios (idAlmacen, idTipo, folio) VALUES (" + solicitud.getIdAlmacen() + ", 35, 2)";
            }
            st.executeUpdate(strSQL);

            idMovto = 0;
//            strSQL = "INSERT INTO movimientos (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, idImpuestoZona, desctoComercial, desctoProntoPago, fecha, idUsuario, idMoneda, tipoCambio, estatus, propietario, idReferencia, referencia) "
//                    + "VALUES (35, " + solicitud.getIdCedis() + ", " + solicitud.getIdEmpresa() + ", " + solicitud.getIdAlmacen() + ", " + folio + ", 0, " + solicitud.getIdImpuestoZona() + ", 0, 0, GETDATE(), " + this.idUsuario + ", 1, 1, 0, 0, " + solicitud.getIdReferencia() + ", 0)";
            st.executeUpdate(strSQL);
            rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
            if (rs.next()) {
                idMovto = rs.getInt("idMovto");
            }
//            strSQL = "INSERT INTO comprobantes (idAlmacen, idProveedor, tipoComprobante, remision, serie, numero, idUsuario, fecha, statusOficina, statusAlmacen, propietario) "
//                    + "VALUES (" + idAlmacenSolicita + ", " + solicitud.getIdAlmacen() + ", 0, '" + idMovto + "', '', '', " + this.idUsuario + ", GETDATE(), 0, 0, 0)";
//            st.executeUpdate(strSQL);
//
            idComprobante = 0;
//            rs = st.executeQuery("SELECT @@IDENTITY AS idComprobante");
//            if (rs.next()) {
//                idComprobante = rs.getInt("idComprobante");
//            }
//            strSQL = "UPDATE movimientos SET idComprobante=" + idComprobante + " WHERE idMovto=" + idMovto;
//            st.executeUpdate(strSQL);

            int folioAlmacen = 0;
            strSQL = "SELECT folio FROM movimientosFoliosAlmacen WHERE idAlmacen=" + solicitud.getIdAlmacen() + " AND idTipo=35";
            rs = st.executeQuery(strSQL);
            if (rs.next()) {
                folioAlmacen = rs.getInt("folio");
                strSQL = "UPDATE movimientosFoliosAlmacen SET folio=folio+1 WHERE idAlmacen=" + solicitud.getIdAlmacen() + " AND idTipo=35";
            } else {
                folioAlmacen = 1;
                strSQL = "INSERT INTO movimientosFoliosAlmacen (idAlmacen, idTipo, folio) VALUES (" + solicitud.getIdAlmacen() + ", 35, 2)";
            }
            st.executeUpdate(strSQL);

            idMovtoAlmacen = 0;
//            strSQL = "INSERT INTO movimientosAlmacen (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, fecha, idUsuario, propietario, idReferencia, referencia, estatus) "
//                    + "VALUES (35, " + solicitud.getIdCedis() + ", " + solicitud.getIdEmpresa() + ", " + solicitud.getIdAlmacen() + ", " + folioAlmacen + ", " + idComprobante + ", GETDATE(), " + this.idUsuario + ", 0, " + solicitud.getIdReferencia() + ", 0, 0)";
            st.executeUpdate(strSQL);
            rs = st.executeQuery("SELECT @@IDENTITY AS idMovtoAlmacen");
            if (rs.next()) {
                idMovtoAlmacen = rs.getInt("idMovtoAlmacen");
            }
            strSQL = "INSERT INTO movimientosRelacionados (idMovto, idMovtoAlmacen) VALUES (" + idMovto + ", " + idMovtoAlmacen + ")";
            st.executeUpdate(strSQL);

            strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantOrdenada, cantFacturada, cantSinCargo, cantRecibida, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior) "
                    + "VALUES (" + idMovto + ", ?, ?, 0, 0, 0, 0, 0, 0, 0, 0, ?, GETDATE(), 0)";
            PreparedStatement ps = cn.prepareStatement(strSQL);
            for (MovimientoProducto p : productos) {
                ps.setInt(1, p.getProducto().getIdProducto());
                ps.setDouble(2, p.getCantOrdenada());
                ps.setInt(3, p.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());
                ps.executeUpdate();
            }
            st.executeUpdate("COMMIT TRANSACTION");
        } catch (SQLException e) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw (e);
        } finally {
            st.close();
            cn.close();
        }
        return ok;
    }

    public double obtenerPrecioUltimaCompra(int idEmpresa, int idEmpaque) throws SQLException {
        double precioLista = 0;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();

        String strSQL = "SELECT idMovtoUltimaEntrada FROM empresasEmpaques "
                + "WHERE idEmpresa=" + idEmpresa + " AND idEmpaque=" + idEmpaque;
        try {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                int idMovto = rs.getInt("idMovtoUltimaEntrada");

                strSQL = "SELECT costo FROM movimientosDetalle "
                        + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + idEmpaque;
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    precioLista = rs.getDouble("costo");
                }
            }
        } catch (SQLException e) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw (e);
        } finally {
            st.close();
            cn.close();
        }
        return precioLista;
    }

    public boolean cancelarEntrada(int idMovto) throws SQLException {
        ResultSet rs;
        ResultSet rs1;
        String strSQL;
        int idEmpresa, idAlmacen, idEmpaque;
        double existenciaAnterior, cantidad, cantFacturada, unitario;

        boolean ok = false;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        PreparedStatement ps;
        PreparedStatement ps1;
        PreparedStatement ps2;
        PreparedStatement ps3;

        idAlmacen = 0;
        idEmpresa = 0;
        rs = st.executeQuery("SELECT idEmpresa, idAlmacen FROM movimientos WHERE idMovto=" + idMovto);
        if (rs.next()) {
            idEmpresa = rs.getInt("idEmpresa");
            idAlmacen = rs.getInt("idAlmacen");
        }
        strSQL = "SELECT existenciaOficina FROM almacenesEmpaques WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=?";
        ps = cn.prepareStatement(strSQL);

        strSQL = "INSERT INTO kardexOficina (idAlmacen, idEmpaque, fecha, idTipoMovto, idMovto, existenciaAnterior, cantidad) "
                + "VALUES (" + idAlmacen + ", ?, GETDATE(), 7, " + idMovto + ", ?, ?)";
        ps1 = cn.prepareStatement(strSQL);

        strSQL = "UPDATE almacenesEmpaques SET existenciaOficina=existenciaOficina-? "
                + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=?";
        ps2 = cn.prepareStatement(strSQL);

        strSQL = "UPDATE empresasEmpaques "
                + "SET existenciaOficina=existenciaOficina-?, "
                + "promedioPonderado=(existenciaOficina*promedioPonderado-?*?)/(existenciaOficina+?) "
                + "WHERE idEmpresa=" + idEmpresa + " AND idEmpaque=?";
        ps3 = cn.prepareStatement(strSQL);
        try {
            st.executeUpdate("BEGIN TRANSACTION");

            st.executeUpdate("UPDATE movimientos SET estatus=3 WHERE idMovto=" + idMovto);

            rs = st.executeQuery("SELECT idEmpaque, cantFacturada, cantSinCargo, unitario "
                    + "FROM movimientosDetalle WHERE idMovto=" + idMovto);
            while (rs.next()) {
                idEmpaque = rs.getInt("idEmpaque");
                cantFacturada = rs.getDouble("cantFacturada");
                cantidad = rs.getDouble("cantFacturada") + rs.getDouble("cantSinCargo");
                unitario = rs.getDouble("unitario");



                ps.setInt(1, idEmpaque);
                rs1 = ps.executeQuery();
                //rs1=st.executeQuery(strSQL);
                existenciaAnterior = 0;
                if (rs1.next()) {
                    existenciaAnterior = rs1.getDouble("existenciaOficina");
                }
                ps1.setInt(1, idEmpaque);
                ps1.setDouble(2, existenciaAnterior);
                ps1.setDouble(3, cantidad);
                ps1.execute();

                ps2.setDouble(1, cantidad);
                ps2.setInt(2, idEmpaque);
                ps2.execute();

                ps3.setDouble(1, cantidad);
                ps3.setDouble(2, cantFacturada);
                ps3.setDouble(3, unitario);
                ps3.setDouble(4, cantidad);
                ps3.setInt(5, idEmpaque);
                ps3.executeUpdate();
            }
            st.executeUpdate("COMMIT TRANSACTION");
        } catch (SQLException e) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw (e);
        } finally {
            st.close();
            cn.close();
        }
        return ok;
    }

    public boolean grabarComprasAlmacen(TOMovimientoOficina m, ArrayList<MovimientoProducto> productos, int idOrdenCompra) throws SQLException {
        boolean ok = false;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        String strSQL;
        try {
            st.executeUpdate("BEGIN TRANSACTION");
            strSQL = "SELECT statusAlmacen FROM comprobantes where idComprobante=" + m.getIdComprobante();
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                if (rs.getBoolean("statusAlmacen")) {
                    throw new SQLException("Ya se ha capturado y cerrado la entrada");
                } else {
                    strSQL = "UPDATE comprobantes SET statusAlmacen=1 WHERE idComprobante=" + m.getIdComprobante();
                    st.executeUpdate(strSQL);
                }
            } else {
                throw new SQLException("No se encontro el comprobante");
            }
            int folio = 0;
            strSQL = "SELECT folio FROM movimientosFoliosAlmacen WHERE idAlmacen=" + m.getIdAlmacen() + " AND idTipo=1";
            rs = st.executeQuery(strSQL);
            if (rs.next()) {
                folio = rs.getInt("folio");
                strSQL = "UPDATE movimientosFoliosAlmacen SET folio=folio+1 WHERE idAlmacen=" + m.getIdAlmacen() + " AND idTipo=1";
            } else {
                folio = 1;
                strSQL = "INSERT INTO movimientosFoliosAlmacen (idAlmacen, idTipo, folio) VALUES (" + m.getIdAlmacen() + ", 1, 2)";
            }
            st.executeUpdate(strSQL);

            String lote = "";
            strSQL = "SELECT lote FROM lotes WHERE fecha=CONVERT(date, GETDATE())";
            rs = st.executeQuery(strSQL);
            if (rs.next()) {
                lote = rs.getString("lote") + "1";
            } else {
                throw new SQLException("No se encontro el lote de fecha de hoy");
            }
//            strSQL = "INSERT INTO movimientosAlmacen (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, fecha, idUsuario) "
//                    + "VALUES (1, " + m.getIdCedis() + ", " + m.getIdEmpresa() + ", " + m.getIdAlmacen() + ", " + folio + ", " + m.getIdComprobante() + ", GETDATE(), " + this.idUsuario + ")";
            st.executeUpdate(strSQL);
            rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
            if (rs.next()) {
                m.setIdMovto(rs.getInt("idMovto"));
            }
            int idProducto;
            for (MovimientoProducto p : productos) {
                if (p.getCantRecibida() > 0) {
                    idProducto = p.getProducto().getIdProducto();

                    strSQL = "SELECT saldo FROM almacenesLotes "
                            + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                    rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        strSQL = "UPDATE almacenesLotes SET cantidad=cantidad+" + p.getCantRecibida() + ", saldo=saldo+" + p.getCantRecibida() + " "
                                + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                    } else {
                        strSQL = "INSERT INTO almacenesLotes (idAlmacen, idEmpaque, fechaCaducidad, lote, cantidad, saldo, existenciaFisica, separados) "
                                + "VALUES (" + m.getIdAlmacen() + ", " + idProducto + ", DATEADD(DAY, 365, convert(date, GETDATE())), '" + lote + "', " + p.getCantRecibida() + ", " + p.getCantRecibida() + ", 0, 0)";
                    }
                    st.executeUpdate(strSQL);

                    strSQL = "INSERT INTO movimientosAlmacenDetalle (idAlmacen, idMovto, idEmpaque, lote, cantidad, suma, fecha, existenciaAnterior) "
                            + "VALUES (" + m.getIdAlmacen() + ", " + m.getIdMovto() + ", " + idProducto + ", '" + lote + "', " + p.getCantRecibida() + ", 1, GETDATE(), 0)";
                    st.executeUpdate(strSQL);
                }
            }
            if (idOrdenCompra != 0) {
                strSQL = "UPDATE ordenCompra SET propietario=0, estado=2 WHERE idOrdenCompra=" + idOrdenCompra;
                st.executeUpdate(strSQL);
            }
            st.executeUpdate("COMMIT TRANSACTION");
            ok = true;
        } catch (SQLException e) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw (e);
        } finally {
            st.close();
            cn.close();
        }
        return ok;
    }

//    public boolean grabarComprasOficina(TOMovimiento m, ArrayList<MovimientoProducto> productos, int idOrdenCompra) throws SQLException {
//        int capturados;
//        boolean ok = false;
//        boolean nueva;
//        ArrayList<ImpuestosProducto> impuestos;
//
//        this.cnx = this.ds.getConnection();
//        String strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantFacturada, cantSinCargo, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, cantOrdenada, cantRecibida, idImpuestoGrupo, fecha, existenciaAnterior) "
//                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, getdate(), ?)";
//        PreparedStatement ps = this.cnx.prepareStatement(strSQL);
//
//        String strSQL1 = "UPDATE movimientosDetalle "
//                + "SET costo=?, desctoProducto1=?, desctoProducto2=?, desctoConfidencial=?, unitario=?, cantFacturada=?, cantSinCargo=?, existenciaAnterior=? "
//                + "WHERE idMovto=" + m.getIdMovto() + " AND idEmpaque=?";
//        PreparedStatement ps1 = this.cnx.prepareStatement(strSQL1);
//
//        String strSQL2 = "UPDATE movimientosDetalleImpuestos "
//                + "SET importe=? "
//                + "WHERE idMovto=" + m.getIdMovto() + " AND idEmpaque=?";
//        PreparedStatement ps2 = this.cnx.prepareStatement(strSQL2);
//
////        String strSQL3="INSERT INTO kardexOficina (idAlmacen, idMovto, idTipoMovto, idEmpaque, fecha, existenciaAnterior, cantidad) " +
////                    "VALUES ("+m.getIdAlmacen()+", "+m.getIdMovto()+", 1, ?, GETDATE(), ?, ?)";
////        PreparedStatement ps3=cn.prepareStatement(strSQL3);
//
//        String strSQL4 = "INSERT INTO almacenesEmpaques (idAlmacen, idEmpaque, existenciaAlmacen, existenciaOficina, existenciaMinima, existenciaMaxima) "
//                + "VALUES (?, ?, 0, ?, 0, 0)";
//        PreparedStatement ps4 = this.cnx.prepareStatement(strSQL4);
//
//        String strSQL5 = "UPDATE almacenesEmpaques "
//                + "SET existenciaOficina=existenciaOficina+? "
//                + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=?";
//        PreparedStatement ps5 = this.cnx.prepareStatement(strSQL5);
//
//        String strSQL6 = "INSERT INTO empresasEmpaques (idEmpresa, idEmpaque, promedioPonderado, existenciaOficina, idMovtoUltimaEntrada) "
//                + "VALUES (" + m.getIdEmpresa() + ", ?, ?, ?, ?)";
//        PreparedStatement ps6 = this.cnx.prepareStatement(strSQL6);
//
//        String strSQL7 = "UPDATE empresasEmpaques "
//                + "SET existenciaOficina=existenciaOficina+?"
//                + ", promedioPonderado=(existenciaOficina*promedioPonderado+?*?)/(existenciaOficina+?+?)"
//                + ", idMovtoUltimaEntrada=? "
//                + "WHERE idEmpresa=" + m.getIdEmpresa() + " AND idEmpaque=?";
//        PreparedStatement ps7 = this.cnx.prepareStatement(strSQL7);
//
//        ResultSet rs;
//        int idEmpaque, folio, idImpuestoGrupo;
//        double existenciaAnterior;
//        Statement st = this.cnx.createStatement();
//        try {
//            capturados = 0;
//            st.executeUpdate("BEGIN TRANSACTION");
//
//            strSQL = "SELECT statusOficina FROM comprobantes where idComprobante=" + m.getIdComprobante();
//            rs = st.executeQuery(strSQL);
//            if (rs.next()) {
//                if (rs.getBoolean("statusOficina")) {
//                    throw new SQLException("Ya se ha capturado y cerrado la entrada");
//                } else {
//                    strSQL = "UPDATE comprobantes SET statusOficina=1 WHERE idComprobante=" + m.getIdComprobante();
//                    st.executeUpdate(strSQL);
//                }
//            } else {
//                throw new SQLException("No se encontro el comprobante");
//            }
//            if (m.getIdMovto() == 0) {
//                folio = 0;
//                nueva = true;
//                rs = st.executeQuery("SELECT folio FROM movimientosFolios WHERE idAlmacen=" + m.getIdAlmacen() + " AND idTipo=1");
//                if (rs.next()) {
//                    folio = rs.getInt("folio");
//                    strSQL = "UPDATE movimientosFolios SET folio=folio+1 WHERE idAlmacen=" + m.getIdAlmacen() + " AND idTipo=1";
//                } else {
//                    folio = 1;
//                    strSQL = "INSERT INTO movimientosFolios (idAlmacen, idTipo, folio) VALUES (" + m.getIdAlmacen() + ", 1, 2)";
//                }
//                st.executeUpdate(strSQL);
//
//                strSQL = "INSERT INTO movimientos (idTipo, idCedis, folio, idEmpresa, idAlmacen, idComprobante, idImpuestoZona, idMoneda, tipoCambio, desctoComercial, desctoProntoPago, idUsuario, fecha, estatus) "
//                        + "VALUES (1, " + m.getIdCedis() + ", " + folio + ", " + m.getIdEmpresa() + ", " + m.getIdAlmacen() + ", " + m.getIdComprobante() + ", " + m.getIdImpuestoZona() + ", " + m.getIdMoneda() + ", " + m.getTipoCambio() + ", " + m.getDesctoComercial() + ", " + m.getDesctoProntoPago() + ", " + this.idUsuario + ", getdate(), 1)";
//                st.executeUpdate(strSQL);
//
//                rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
//                if (rs.next()) {
//                    m.setIdMovto(rs.getInt("idMovto"));
//                }
//                if (idOrdenCompra != 0) {
//                    strSQL = "UPDATE ordenCompra SET propietario=0, estado=2 WHERE idOrdenCompra=" + idOrdenCompra;
//                    st.executeUpdate(strSQL);
//                }
//            } else {
//                nueva = false;
//                st.executeUpdate("UPDATE movimientos "
//                        + "SET idMoneda=" + m.getIdMoneda() + ", tipoCambio=" + m.getTipoCambio() + " "
//                        + ", desctoComercial=" + m.getDesctoComercial() + ", desctoProntoPago=" + m.getDesctoProntoPago() + " "
//                        + ", fecha=GETDATE(), estatus=1 "
//                        + "WHERE idMovto=" + m.getIdMovto());
//            }
//
//            //rs=st.executeQuery("select DATEPART(weekday, getdate()-1) AS DIA, DATEPART(week, GETDATE()) AS SEM, DATEPART(YEAR, GETDATE())%10 AS ANIO");
//            //lote=""+rs.getInt("DIA")+String.format("%02d", rs.getInt("SEM"))+rs.getInt("ANIO")+"1";
//
//            for (MovimientoProducto p : productos) {
//                idEmpaque = p.getProducto().getIdProducto();
//
//                if (p.getCantFacturada() > 0) {
//                    capturados++;
//                    rs = st.executeQuery("SELECT existenciaOficina "
//                            + "FROM almacenesEmpaques "
//                            + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=" + idEmpaque);
//                    if (rs.next()) {
//                        existenciaAnterior = rs.getDouble("existenciaOficina");
//
//                        ps5.setDouble(1, p.getCantFacturada() + p.getCantSinCargo());
//                        ps5.setInt(2, idEmpaque);
//                        ps5.executeUpdate();
//
//                        ps7.setDouble(1, p.getCantFacturada() + p.getCantSinCargo());
//                        ps7.setDouble(2, p.getCantFacturada());
//                        ps7.setDouble(3, p.getUnitario());
//                        ps7.setDouble(4, p.getCantFacturada());
//                        ps7.setDouble(5, p.getCantSinCargo());
//                        ps7.setInt(6, m.getIdMovto());
//                        ps7.setInt(7, idEmpaque);
//                        ps7.executeUpdate();
//                    } else {
//                        existenciaAnterior = 0;
//
//                        ps4.setInt(1, m.getIdAlmacen());
//                        ps4.setInt(2, idEmpaque);
//                        ps4.setDouble(3, p.getCantFacturada() + p.getCantSinCargo());
//                        ps4.executeUpdate();
//
//                        rs = st.executeQuery("SELECT existenciaOficina "
//                                + "FROM empresasEmpaques "
//                                + "WHERE idEmpresa=" + m.getIdEmpresa() + " AND idEmpaque=" + idEmpaque);
//                        if (rs.next()) {
//                            ps7.setDouble(1, p.getCantFacturada() + p.getCantSinCargo());
//                            ps7.setDouble(2, p.getCantFacturada());
//                            ps7.setDouble(3, p.getUnitario());
//                            ps7.setDouble(4, p.getCantFacturada());
//                            ps7.setDouble(5, p.getCantSinCargo());
//                            ps7.setInt(6, m.getIdMovto());
//                            ps7.setInt(7, idEmpaque);
//                            ps7.executeUpdate();
//                        } else {
//                            ps6.setInt(1, idEmpaque);
//                            ps6.setDouble(2, p.getUnitario());
//                            ps6.setDouble(3, p.getCantFacturada() + p.getCantSinCargo());
//                            ps6.setInt(4, m.getIdMovto());
//                            ps6.executeUpdate();
//                        }
//                    }
////                    ps3.setInt(1, idEmpaque);
////                    ps3.setDouble(2, existenciaAnterior);
////                    ps3.setDouble(3, p.getCantFacturada()+p.getCantSinCargo());
////                    ps3.executeUpdate();
//                    if (nueva) {
//                        ps.setInt(1, m.getIdMovto());
//                        ps.setInt(2, idEmpaque);
//                        ps.setDouble(3, p.getCantFacturada());
//                        ps.setDouble(4, p.getCantSinCargo());
//                        ps.setDouble(5, p.getCosto());
//                        ps.setDouble(6, p.getDesctoProducto1());
//                        ps.setDouble(7, p.getDesctoProducto2());
//                        ps.setDouble(8, p.getDesctoConfidencial());
//                        ps.setDouble(9, p.getUnitario());
//                        ps.setDouble(10, p.getCantOrdenada());
//                        ps.setDouble(11, 0);
//                        ps.setInt(12, p.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());
//                        ps.setDouble(13, existenciaAnterior);
//                        ps.executeUpdate();
//
//                        idImpuestoGrupo = p.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo();
//                        this.agregarImpuestosProducto(m.getIdMovto(), idEmpaque, idImpuestoGrupo, m.getIdImpuestoZona());
//                        this.calculaImpuestosProducto(m.getIdMovto(), idEmpaque, p.getUnitario());
//                    } else {
//                        ps1.setDouble(1, p.getCosto());
//                        ps1.setDouble(2, p.getDesctoProducto1());
//                        ps1.setDouble(3, p.getDesctoProducto2());
//                        ps1.setDouble(4, p.getDesctoConfidencial());
//                        ps1.setDouble(5, p.getUnitario());
//                        ps1.setDouble(6, p.getCantFacturada());
//                        ps1.setDouble(7, p.getCantSinCargo());
//                        ps1.setDouble(8, existenciaAnterior);
//                        ps1.setInt(9, idEmpaque);
//                        ps1.executeUpdate();
//                    }
//                    impuestos = p.getImpuestos();
//                    for (ImpuestosProducto i : impuestos) {
//                        ps2.setDouble(1, i.getImporte());
//                        ps2.setInt(2, idEmpaque);
//                        ps2.executeUpdate();
//                    }
//                }
//            }
//            if (capturados == 0) {
//                st.executeUpdate("UPDATE comprobantes SET statusOficina=0 WHERE idComprobante=" + m.getIdComprobante());
//            }
//            st.executeUpdate("COMMIT TRANSACTION");
//            ok = true;
//        } catch (SQLException e) {
//            st.executeUpdate("ROLLBACK TRANSACTION");
//            throw (e);
//        } finally {
//            st.close();
//            this.cnx.close();
//        }
//        return ok;
//    }

    public int obtenerIdOrdenCompra(int idComprobante, int idEntrada) throws SQLException {
        int idOrdenCompra = 0;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery("SELECT idOrdenCompra FROM comprobantesOrdenesCompra "
                    + "WHERE idComprobante=" + idComprobante + " AND idEntrada=" + idEntrada);
            if (rs.next()) {
                idOrdenCompra = rs.getInt("idOrdenCompra");
            }
        } finally {
            st.close();
            cn.close();
        }
        return idOrdenCompra;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

//    private void calculaImpuestosProducto(int idMovto, int idEmpaque, double unitario) throws SQLException {
//        String strSQL = "UPDATE d "
//                + "SET d.importe=CASE WHEN d.aplicable=0 THEN 0 WHEN d.modo=1 THEN " + unitario + "*valor/100.00 ELSE e.piezas*valor END "
//                + "FROM movimientosDetalleImpuestos d "
//                + "INNER JOIN empaques e ON e.idEmpaque=d.idEmpaque "
//                + "WHERE d.idMovto=" + idMovto + " AND d.idEmpaque=" + idEmpaque + " AND d.acumulable=1";
//        stx.executeUpdate(strSQL);
//
//        strSQL = "UPDATE d "
//                + "SET importe=CASE WHEN aplicable=0 THEN 0 "
//                + "                 WHEN modo=1 THEN (" + unitario + "+COALESCE(a.acumulable, 0))*valor/100.00 "
//                + "                 ELSE e.piezas*valor END "
//                + "FROM movimientosDetalleImpuestos d "
//                + "INNER JOIN empaques e on e.idEmpaque=d.idEmpaque "
//                + "LEFT JOIN (SELECT idMovto, idEmpaque, SUM(importe) AS acumulable "
//                + "             FROM movimientosDetalleImpuestos "
//                + "             WHERE idMovto=" + idMovto + " AND idEmpaque=" + idEmpaque + " AND acumulable=1 "
//                + "             GROUP BY idMovto, idEmpaque) a ON a.idMovto=d.idMovto AND a.idEmpaque=d.idEmpaque "
//                + "WHERE d.idMovto=" + idMovto + " AND d.idEmpaque=" + idEmpaque + " AND d.acumulable=0";
//        stx.executeUpdate(strSQL);
//    }
    private void calculaImpuestosProducto(int idMovto, int idEmpaque, double unitario) throws SQLException {
        String strSQL;
        Statement st = this.cnx.createStatement();
        try {
            strSQL = "UPDATE d\n"
                    + "SET d.importe=CASE WHEN d.aplicable=0 THEN 0 WHEN d.modo=1 THEN " + unitario + "*valor/100.00 ELSE e.piezas*valor END\n"
                    + "FROM movimientosDetalleImpuestos d\n"
                    + "INNER JOIN empaques e ON e.idEmpaque=d.idEmpaque\n"
                    + "WHERE d.idMovto=" + idMovto + " AND d.idEmpaque=" + idEmpaque + " AND d.acumulable=1";
            st.executeUpdate(strSQL);

            strSQL = "UPDATE d\n"
                    + "SET importe=CASE WHEN aplicable=0 THEN 0 WHEN modo=1 THEN (" + unitario + "+COALESCE(a.acumulable, 0))*valor/100.00 ELSE e.piezas*valor END\n"
                    + "FROM movimientosDetalleImpuestos d\n"
                    + "INNER JOIN empaques e on e.idEmpaque=d.idEmpaque\n"
                    + "LEFT JOIN (SELECT idMovto, idEmpaque, SUM(importe) AS acumulable\n"
                    + "             FROM movimientosDetalleImpuestos\n"
                    + "             WHERE idMovto=" + idMovto + " AND idEmpaque=" + idEmpaque + " AND acumulable=1\n"
                    + "             GROUP BY idMovto, idEmpaque) a ON a.idMovto=d.idMovto AND a.idEmpaque=d.idEmpaque\n"
                    + "WHERE d.idMovto=" + idMovto + " AND d.idEmpaque=" + idEmpaque + " AND d.acumulable=0";
            st.executeUpdate(strSQL);
        } finally {
            st.close();
        }
    }

    private ArrayList<ImpuestosProducto> obtenerImpuestosProductoPrivado(int idMovto, int idEmpaque) throws SQLException {
        ArrayList<ImpuestosProducto> impuestos = new ArrayList<ImpuestosProducto>();
        String strSQL = "select idImpuesto, impuesto, valor, aplicable, modo, acreditable, importe, acumulable\n"
                + "from movimientosDetalleImpuestos\n"
                + "where idMovto=" + idMovto + " and idEmpaque=" + idEmpaque;
        Statement st = this.cnx.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                impuestos.add(construirImpuestosProducto(rs));
            }
        } finally {
            st.close();
        }
        return impuestos;
    }

    public ArrayList<ImpuestosProducto> obtenerImpuestosProducto(int idMovto, int idEmpaque) throws SQLException {
        ArrayList<ImpuestosProducto> impuestos = new ArrayList<ImpuestosProducto>();
        this.cnx = this.ds.getConnection();
        Statement st = cnx.createStatement();
        try {
            impuestos = this.obtenerImpuestosProductoPrivado(idMovto, idEmpaque);
        } finally {
            st.close();
            cnx.close();
        }
        return impuestos;
    }

    public ArrayList<ImpuestosProducto> generarImpuestosProducto(int idImpuestoGrupo, int idZona) throws SQLException {
        ArrayList<ImpuestosProducto> impuestos = new ArrayList<ImpuestosProducto>();
        String strSQL = "SELECT id.idImpuesto, i.impuesto, id.valor, i.aplicable, i.modo, i.acreditable, 0.00 as importe, i.acumulable\n"
                + "FROM impuestosDetalle id\n"
                + "INNER JOIN impuestos i ON i.idImpuesto=id.idImpuesto\n"
                + "WHERE id.idGrupo=" + idImpuestoGrupo + " and id.idZona=" + idZona + " and GETDATE() between fechaInicial and fechaFinal";
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                impuestos.add(this.construirImpuestosProducto(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return impuestos;
    }

    private ImpuestosProducto construirImpuestosProducto(ResultSet rs) throws SQLException {
        ImpuestosProducto ip = new ImpuestosProducto();
        ip.setIdImpuesto(rs.getInt("idImpuesto"));
        ip.setImpuesto(rs.getString("impuesto"));
        ip.setValor(rs.getDouble("valor"));
        ip.setAplicable(rs.getBoolean("aplicable"));
        ip.setModo(rs.getInt("modo"));
        ip.setAcreditable(rs.getBoolean("acreditable"));
        ip.setImporte(rs.getDouble("importe"));
        ip.setAcumulable(rs.getBoolean("acumulable"));
        return ip;
    }

    private void agregarImpuestosProducto(int idMovto, int idEmpaque, int idImpuestoGrupo, int idZona) throws SQLException {
        String strSQL = "insert into movimientosDetalleImpuestos (idMovto, idEmpaque, idImpuesto, impuesto, valor, aplicable, modo, acreditable, importe, acumulable) "
                + "select " + idMovto + ", " + idEmpaque + ", id.idImpuesto, i.impuesto, id.valor, i.aplicable, i.modo, i.acreditable, 0.00 as importe, i.acumulable "
                + "from impuestosDetalle id "
                + "inner join impuestos i on i.idImpuesto=id.idImpuesto "
                + "where id.idGrupo=" + idImpuestoGrupo + " and id.idZona=" + idZona + " and GETDATE() between fechaInicial and fechaFinal";
        Statement st = this.cnx.createStatement();
        try {
            if (st.executeUpdate(strSQL) == 0) {
                throw (new SQLException("No se insertaron impuestos !!!"));
            }
        } finally {
            st.close();
        }
    }

//    private void agregarImpuestosProducto(Connection cn, int idImpuestoGrupo, int idZona, int idMovto, int idEmpaque) throws SQLException {
//        Statement st = cn.createStatement();
//        String strSQL = "insert into movimientosDetalleImpuestos (idMovto, idEmpaque, idImpuesto, impuesto, valor, aplicable, modo, acreditable, importe, acumulable) "
//                + "select " + idMovto + ", " + idEmpaque + ", id.idImpuesto, i.impuesto, id.valor, i.aplicable, i.modo, i.acreditable, 0.00 as importe, i.acumulable "
//                + "from impuestosDetalle id "
//                + "inner join impuestos i on i.idImpuesto=id.idImpuesto "
//                + "where id.idGrupo=" + idImpuestoGrupo + " and id.idZona=" + idZona + " and GETDATE() between fechaInicial and fechaFinal";
//        st.executeUpdate(strSQL);
//    }
    public TOMovimientoAlmacenProducto construirAlmacenDetalle(ResultSet rs) throws SQLException {
        TOMovimientoAlmacenProducto to = new TOMovimientoAlmacenProducto();
        to.setIdMovtoAlmacen(rs.getInt("idMovtoAlmacen"));
        to.setIdProducto(rs.getInt("idEmpaque"));
//        to.setLote(rs.getString("lote"));
        to.setCantidad(rs.getDouble("cantidad"));
        return to;
    }

    public ArrayList<TOMovimientoAlmacenProducto> obtenerMovimientoAlmacenDetalle(int idMovtoAlmacen) throws SQLException, NamingException {
        ArrayList<TOMovimientoAlmacenProducto> productos = new ArrayList<TOMovimientoAlmacenProducto>();
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery("SELECT * FROM movimientosAlmacenDetalle WHERE idMovtoAlmacen=" + idMovtoAlmacen);
            while (rs.next()) {
                productos.add(this.construirAlmacenDetalle(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return productos;
    }

    public ArrayList<TOMovimientoProducto> obtenerMovimientoDetalle(int idMovto) throws SQLException {
        ArrayList<TOMovimientoProducto> productos = new ArrayList<TOMovimientoProducto>();
        this.cnx = this.ds.getConnection();
        try {
            productos = obtenMovimientoDetalle(idMovto);
        } finally {
            this.cnx.close();
        }
        return productos;
    }

    private ArrayList<TOMovimientoProducto> obtenMovimientoDetalle(int idMovto) throws SQLException {
        ArrayList<TOMovimientoProducto> productos = new ArrayList<TOMovimientoProducto>();
        Statement st = this.cnx.createStatement();
        try {
            ResultSet rs = st.executeQuery("SELECT * FROM movimientosDetalle WHERE idMovto=" + idMovto);
            while (rs.next()) {
                productos.add(this.construirDetalle(rs));
            }
        } finally {
            st.close();
        }
        return productos;
    }

    public TOMovimientoProducto construirDetalle(ResultSet rs) throws SQLException {
        TOMovimientoProducto to = new TOMovimientoProducto();
        to.setIdMovto(rs.getInt("idMovto"));
        to.setIdProducto(rs.getInt("idEmpaque"));
        to.setCantOrdenada(rs.getDouble("cantOrdenada"));
        to.setCantFacturada(rs.getDouble("cantFacturada"));
        to.setCantSinCargo(rs.getDouble("cantSinCargo"));
        to.setCantRecibida(rs.getDouble("cantRecibida"));
        to.setCosto(rs.getDouble("costo"));
        to.setDesctoConfidencial(rs.getDouble("desctoConfidencial"));
        to.setDesctoProducto1(rs.getDouble("desctoProducto1"));
        to.setDesctoProducto2(rs.getDouble("desctoProducto2"));
        to.setUnitario(rs.getDouble("unitario"));
        return to;
    }

//    public TOMovimiento obtenerMovimientoComprobante(int idComprobante) throws SQLException {
//        TOMovimiento to = null;
//        String strSQL = "SELECT M.*"
//                + ", ISNULL(C.tipoComprobante, 0) AS tipoComprobante, ISNULL(C.remision, '') AS remision"
//                + ", ISNULL(C.serie, '') AS  serie, ISNULL(C.numero, '') AS numero, ISNULL(C.idUsuario, 0) AS idUsuarioComprobante"
//                + ", ISNULL(C.fecha, GETDATE()) AS fechaComprobante, ISNULL(C.propietario, 0) AS propietario"
//                + ", ISNULL(MA.idMovtoAlmacen, 0) AS idMovtoAlmacen, ISNULL(MA.fecha, GETDATE()) AS fechaAlmacen"
//                + ", ISNULL(MA.idUsuario, 0) AS idUsuarioAlmacen, ISNULL(MA.estatus, 0) AS statusAlmacen "
//                + "FROM movimientos M "
//                + "LEFT JOIN comprobantes C ON C.idComprobante=M.idComprobante "
//                + "LEFT JOIN movimientosRelacionados MR ON MR.idMovto=M.idMovto "
//                + "INNER JOIN movimientosAlmacen MA ON MA.idMovtoAlmacen=MR.idMovtoAlmacen "
//                + "WHERE C.idComprobante=" + idComprobante;
//        Connection cn = this.ds.getConnection();
//        Statement st = cn.createStatement();
//        try {
//            ResultSet rs = st.executeQuery(strSQL);
//            if (rs.next()) {
//                to = construirMovimientoRelacionado(rs);
//            }
//        } finally {
//            st.close();
//            cn.close();
//        }
//        return to;
//    }

    public void agregarMovimientoRelacionado(TOMovimientoOficina to) throws SQLException {
        this.cnx = this.ds.getConnection();
        Statement st = this.cnx.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");
            this.agregaMovimientoRelacionado(to);
            st.executeUpdate("COMMIT TRANSACTION");

        } catch (SQLException ex) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            this.cnx.close();
        }
    }

    private void agregaMovimientoRelacionado(TOMovimientoOficina to) throws SQLException {
        String strSQL="";
        Statement st = this.cnx.createStatement();
        try {
//            strSQL = "INSERT INTO movimientos (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, idImpuestoZona, desctoComercial, desctoProntoPago, fecha, idUsuario, idMoneda, tipoCambio, idReferencia, referencia, estatus, propietario) "
//                    + "VALUES(" + to.getIdTipo() + ", " + this.idCedis + ", " + to.getIdEmpresa() + ", " + to.getIdAlmacen() + ", 0, 0, " + to.getIdImpuestoZona() + ", 0, 0, GETDATE(), " + this.idUsuario + ", " + to.getIdMoneda() + ", " + to.getTipoDeCambio() + ", " + to.getIdReferencia() + ", " + to.getReferencia() + ", " + to.getEstatus() + ", " + this.idUsuario + ")";
            st.executeUpdate(strSQL);

            ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
            if (rs.next()) {
                to.setIdMovto(rs.getInt("idMovto"));
            }
//            strSQL = "INSERT INTO movimientosAlmacen (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, fecha, idReferencia, referencia, idUsuario, estatus, propietario) "
//                    + "VALUES (" + to.getIdTipo() + ", " + this.idCedis + ", " + to.getIdEmpresa() + ", " + to.getIdAlmacen() + ", 0, 0, GETDATE(), " + to.getIdReferencia() + ", " + to.getReferencia() + ", " + this.idUsuario + ", " + to.getEstatus() + ", " + this.idUsuario + ")";
            st.executeUpdate(strSQL);

            rs = st.executeQuery("SELECT @@IDENTITY AS idMovtoAlmacen");
            if (rs.next()) {
                to.setIdMovtoAlmacen(rs.getInt("idMovtoAlmacen"));
            }
            strSQL = "INSERT INTO movimientosRelacionados (idMovto, idMovtoAlmacen) VALUES (" + to.getIdMovto() + ", " + to.getIdMovtoAlmacen() + ")";
            st.executeUpdate(strSQL);
        } finally {
            st.close();
        }
    }

    public ArrayList<TOMovimientoOficina> obtenerMovimientosAlmacenRelacionados(int idAlmacen, int idTipo, int estatus, Date fechaInicial) throws SQLException {
        if (fechaInicial == null) {
            fechaInicial = new Date();
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TOMovimientoOficina> solicitudes = new ArrayList<TOMovimientoOficina>();
        String strSQL = "SELECT M.*"
                + ", ISNULL(C.tipoComprobante, 0) AS tipoComprobante, ISNULL(C.remision, '') AS remision"
                + ", ISNULL(C.serie, '') AS  serie, ISNULL(C.numero, '') AS numero, ISNULL(C.idUsuario, 0) AS idUsuarioComprobante"
                + ", ISNULL(C.fecha, GETDATE()) AS fechaComprobante, ISNULL(C.propietario, 0) AS propietario"
                + ", MA.idMovtoAlmacen, MA.fecha AS fechaAlmacen, MA.idUsuario AS idUsuarioAlmacen, MA.estatus AS statusAlmacen "
                + "FROM movimientos M "
                + "LEFT JOIN comprobantes C ON C.idComprobante=M.idComprobante "
                + "INNER JOIN movimientosRelacionados MR ON MR.idMovto=M.idMovto "
                + "INNER JOIN movimientosAlmacen MA ON MA.idMovtoAlmacen=MR.idMovtoAlmacen "
                + "WHERE MA.idAlmacen=" + idAlmacen + " AND MA.idTipo=" + idTipo + " AND MA.estatus BETWEEN 1 AND " + estatus + " AND CONVERT(date, MA.fecha) <= '" + format.format(fechaInicial) + "'";
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
//                solicitudes.add(this.construirMovimientoRelacionado(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return solicitudes;
    }

    public ArrayList<TOMovimientoOficina> obtenerMovimientosRelacionados(int idAlmacen, int idTipo, int estatus, Date fechaInicial) throws SQLException {
        if (fechaInicial == null) {
            fechaInicial = new Date();
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TOMovimientoOficina> solicitudes = new ArrayList<TOMovimientoOficina>();
        String strSQL = "SELECT M.*"
                + ", ISNULL(C.tipoComprobante, 0) AS tipoComprobante, ISNULL(C.remision, '') AS remision"
                + ", ISNULL(C.serie, '') AS  serie, ISNULL(C.numero, '') AS numero, ISNULL(C.idUsuario, 0) AS idUsuarioComprobante"
                + ", ISNULL(C.fecha, GETDATE()) AS fechaComprobante, ISNULL(C.propietario, 0) AS propietario"
                + ", MA.idMovtoAlmacen, MA.fecha AS fechaAlmacen, MA.idUsuario AS idUsuarioAlmacen, MA.estatus AS statusAlmacen "
                + "FROM movimientos M "
                + "LEFT JOIN comprobantes C ON C.idComprobante=M.idComprobante "
                + "INNER JOIN movimientosRelacionados MR ON MR.idMovto=M.idMovto "
                + "INNER JOIN movimientosAlmacen MA ON MA.idMovtoAlmacen=MR.idMovtoAlmacen "
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=" + idTipo + " AND M.estatus BETWEEN 0 AND " + estatus + " AND CONVERT(date, M.fecha) <= '" + format.format(fechaInicial) + "'";
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
//                solicitudes.add(this.construirMovimientoRelacionado(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return solicitudes;
    }

//    public TOMovimiento obtenerMovimientoRelacionado(int idMovto) throws SQLException {
//        TOMovimiento to = null;
//        String strSQL = "SELECT M.*"
//                + ", ISNULL(C.tipoComprobante, 0) AS tipoComprobante, ISNULL(C.remision, '') AS remision"
//                + ", ISNULL(C.serie, '') AS  serie, ISNULL(C.numero, '') AS numero, ISNULL(C.idUsuario, 0) AS idUsuarioComprobante"
//                + ", ISNULL(C.fecha, GETDATE()) AS fechaComprobante, ISNULL(C.propietario, 0) AS propietario "
//                + ", MA.idMovtoAlmacen, MA.fecha AS fechaAlmacen, MA.idUsuario AS idUsuarioAlmacen, MA.estatus AS statusAlmacen "
//                + "FROM movimientos M "
//                + "LEFT JOIN comprobantes C ON C.idComprobante=M.idComprobante "
//                + "INNER JOIN movimientosRelacionados MR ON MR.idMovto=M.idMovto "
//                + "INNER JOIN movimientosAlmacen MA ON MA.idMovtoAlmacen=MR.idMovtoAlmacen "
//                + "WHERE M.idMovto=" + idMovto;
//        Connection cn = this.ds.getConnection();
//        Statement st = cn.createStatement();
//        try {
//            ResultSet rs = st.executeQuery(strSQL);
//            if (rs.next()) {
//                to = construirMovimientoRelacionado(rs);
//            }
//        } finally {
//            st.close();
//            cn.close();
//        }
//        return to;
//    }

//    private TOMovimiento construirMovimientoRelacionado(ResultSet rs) throws SQLException {
//        TOMovimiento to = new TOMovimiento();
//        to.setIdMovto(rs.getInt("idMovto"));
//        to.setIdTipo(rs.getInt("idTipo"));
//        to.setIdCedis(rs.getInt("idCedis"));
//        to.setIdEmpresa(rs.getInt("idEmpresa"));
//        to.setIdAlmacen(rs.getInt("idAlmacen"));
//        to.setFolio(rs.getInt("folio"));
//        to.setIdComprobante(rs.getInt("idComprobante"));
//        to.setIdImpuestoZona(rs.getInt("idImpuestoZona"));
//        to.setDesctoComercial(rs.getDouble("desctoComercial"));
//        to.setDesctoProntoPago(rs.getDouble("desctoprontoPago"));
//        to.setFecha(new java.util.Date(rs.getDate("fecha").getTime()));
//        to.setIdUsuario(rs.getInt("idUsuario"));
//        to.setIdMoneda(rs.getInt("idMoneda"));
//        to.setTipoCambio(rs.getDouble("tipoCambio"));
//        to.setIdReferencia(rs.getInt("idReferencia"));
//        to.setReferencia(rs.getInt("referencia"));
//        to.setStatusOficina(rs.getInt("estatus"));
//        ///////////////////////////////////////////////////////////////
//        to.setTipoComprobante(rs.getInt("tipoComprobante"));
//        to.setRemision(rs.getString("remision"));
//        to.setSerie(rs.getString("serie"));
//        to.setNumero(rs.getString("numero"));
//        to.setIdUsuarioComprobante(rs.getInt("idUsuarioComprobante"));
//        to.setFechaComprobante(new java.util.Date(rs.getDate("fechaComprobante").getTime()));
//        to.setPropietario(rs.getInt("propietario"));
//        ///////////////////////////////////////////////////////////////
//        to.setIdMovtoAlmacen(rs.getInt("idMovtoAlmacen"));
//        to.setFechaAlmacen(new java.util.Date(rs.getDate("fechaAlmacen").getTime()));
//        to.setIdUsuarioAlmacen(rs.getInt("idUsuarioAlmacen"));
//        to.setStatusAlmacen(rs.getInt("statusAlmacen"));
//        return to;
//    }

    public boolean liberarMovtoRelacionado(int idMovto) throws SQLException, Exception {
        boolean liberado = false;
        String strSQL = "SELECT M.propietario, MR.idMovtoAlmacen "
                + "FROM movimientos M "
                + "INNER JOIN movimientosRelacionados MR ON MR.idMovto=M.idMovto "
                + "WHERE M.idMovto=" + idMovto;
        this.cnx = this.ds.getConnection();
        Statement st = this.cnx.createStatement();
        try {
            st.execute("BEGIN TRANSACTION");

            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                int propietario = rs.getInt("propietario");

                if (propietario == this.idUsuario) {
                    int idMovtoAlmacen = rs.getInt("idMovtoAlmacen");

                    strSQL = "UPDATE movimientos SET propietario=0 WHERE idMovto=" + idMovto;
                    if (st.executeUpdate(strSQL) == 0) {
                        throw new SQLException("No se libero el movimiento !!!");
                    }
                    strSQL = "UPDATE movimientosAlmacen SET propietario=0 WHERE idMovtoAlmacen=" + idMovtoAlmacen;
                    if (st.executeUpdate(strSQL) == 0) {
                        throw new SQLException("No se libero el movimiento de almacen !!!");
                    }
                    liberado = true;
                } else {
                    strSQL = "SELECT * FROM webSystem.dbo.usuarios WHERE idUsuario=" + propietario;
                    rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        strSQL = rs.getString("usuario");
                    } else {
                        strSQL = "";
                    }
                    throw new Exception("No se libero el movimiento, lo tiene (id=" + propietario + "): " + strSQL + " !!!");
                }
            } else {
                throw new SQLException("No se encontro el movimiento !!!");
            }
            st.execute("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.execute("ROLLBACK TRANSACTION");
            throw ex;
        } catch (Exception ex) {
            st.execute("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            this.cnx.close();
        }
        return liberado;
    }

    public boolean asegurarMovtoRelacionado(int idMovto) throws SQLException, Exception {
        boolean asegurado = false;
        String strSQL = "SELECT M.propietario, MR.idMovtoAlmacen "
                + "FROM movimientos M "
                + "INNER JOIN movimientosRelacionados MR ON MR.idMovto=M.idMovto "
                + "WHERE M.idMovto=" + idMovto;
        this.cnx = this.ds.getConnection();
        Statement st = this.cnx.createStatement();
        try {
            st.execute("BEGIN TRANSACTION");

            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                int propietario = rs.getInt("propietario");

                if (propietario == 0 || propietario == this.idUsuario) {
                    int idMovtoAlmacen = rs.getInt("idMovtoAlmacen");

                    strSQL = "UPDATE movimientos SET propietario=" + this.idUsuario + " WHERE idMovto=" + idMovto;
                    if (st.executeUpdate(strSQL) == 0) {
                        throw new SQLException("No se aseguro el movimiento !!!");
                    }
                    strSQL = "UPDATE movimientosAlmacen SET propietario=" + this.idUsuario + " WHERE idMovtoAlmacen=" + idMovtoAlmacen;
                    if (st.executeUpdate(strSQL) == 0) {
                        throw new SQLException("No se aseguro el movimiento de almacen !!!");
                    }
                    asegurado = true;
                } else {
                    strSQL = "SELECT * FROM webSystem.dbo.usuarios WHERE idUsuario=" + propietario;
                    rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        strSQL = rs.getString("usuario");
                    } else {
                        strSQL = "";
                    }
                    throw new Exception("No se puede asegurar el movimiento, lo tiene el usuario(id=" + propietario + "): " + strSQL + " !!!");
                }
            } else {
                throw new SQLException("No se encontro el movimiento !!!");
            }
            st.execute("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.execute("ROLLBACK TRANSACTION");
            throw ex;
        } catch (Exception ex) {
            st.execute("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            this.cnx.close();
        }
        return asegurado;
    }

    public int obtenerIdUsuario() {
        return this.idUsuario;
    }
//    private double liberaLote(int idAlmacen, int idMovtoAlmacen, int idProducto, String lote, double cantidadSolicitada) throws SQLException {
//        double liberar;
//        double cantidadLiberar = cantidadSolicitada;
//        double liberados = 0;
//
//        String strSQL = "SELECT K.lote, K.cantidad, ISNULL(L.cantidad, -1) AS existe\n" +
//                        "FROM movimientosAlmacenDetalle K\n" +
//                        "LEFT JOIN almacenesLotes L ON L.idAlmacen=K.idAlmacen AND L.idEmpaque=K.idEmpaque AND L.lote=K.lote\n" +
//                        "WHERE K.idMovtoAlmacen=" + idMovtoAlmacen + " AND K.idEmpaque=" + idProducto + " AND K.lote='"+lote+"'\n" +
//                        "ORDER BY L.fechaCaducidad DESC";
//        Statement st = cnx.createStatement();
//        try {
//            Statement st1 = cnx.createStatement();
//
//            ResultSet rs = st.executeQuery(strSQL);
//            while (rs.next()) {
//                liberar = rs.getDouble("cantidad");
//                if (cantidadLiberar < liberar) {
//                    liberar = cantidadLiberar;
//
//                    strSQL = "UPDATE movimientosAlmacenDetalle SET cantidad=cantidad-" + liberar + " "
//                            + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + rs.getString("lote") + "'";
//                } else {
//                    strSQL = "DELETE FROM movimientosAlmacenDetalle "
//                            + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + "AND idEmpaque=" + idProducto + " AND lote='" + rs.getString("lote") + "'";
//                }
//                st1.executeUpdate(strSQL);
//
//                if (rs.getInt("existe") == -1) {
//                    strSQL = "INSERT INTO almacenesLotes (idAlmacen, idEmpaque, lote, fechaCaducidad, cantidad, saldo, separados, existenciaFisica) "
//                            + "VALUES(" + idAlmacen + ", " + idProducto + ", '" + rs.getString("lote") + "', GETDATE(), " + liberar + ", " + liberar + ", 0, 0)";
//                } else {
//                    strSQL = "UPDATE almacenesLotes SET separados=separados-" + liberar + " "
//                            + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + rs.getString("lote") + "'";
//                }
//                st1.executeUpdate(strSQL);
//
//                liberados += liberar;
//                cantidadLiberar -= liberar;
//                if (cantidadLiberar == 0) {
//                    break;
//                }
//            }
//            strSQL = "UPDATE almacenesEmpaques "
//                    + "SET separados=separados-" + liberados + " "
//                    + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto;
//            int nRegs = st.executeUpdate(strSQL);
//            if (nRegs == 0) {
//                throw (new SQLException("No se encontro empaque (" + idProducto + ") en almacenesEmpaques !!!"));
//            }
//        } finally {
//            st.close();
//        }
//        return liberados;
//    }
//    private double separaLote(int idAlmacen, int idMovtoAlmacen, int idProducto, String lote, double cantidadSolicitada, boolean total) throws SQLException {
//        double cantidadSeparar=cantidadSolicitada;
//        double disponibles = 0;
//        double separados=0;
//        
//        Statement st = cnx.createStatement();
//        try {
//            String strSQL = "SELECT AE.existenciaOficina-AE.separados AS saldoOficina "
//                            + "FROM almacenesEmpaques AE "
//                            + "WHERE AE.idAlmacen=" + idAlmacen + " AND AE.idEmpaque=" + idProducto;
//            ResultSet rs = st.executeQuery(strSQL);
//            if (rs.next()) {
//                disponibles=rs.getDouble("saldoOficina");
//                if (disponibles <= 0) {
//                    throw (new SQLException("No hay existencia oficina !!!"));
//                } else if (disponibles < cantidadSeparar) {
//                    if(total) {
//                        throw (new SQLException("No hay existencia suficiente, solo hay "+disponibles+" disponibles !!!"));
//                    } else {
//                        cantidadSeparar = disponibles;
//                    }
//                }
//            } else {
//                throw (new SQLException("No se encontro producto(" + idProducto + ") en almacen !!!"));
//            }
//            
//            double separar;
//            Statement st1 = cnx.createStatement();
//            strSQL = "SELECT L.lote, L.saldo, L.saldo-L.separados AS disponibles, ISNULL(K.cantidad, 0) AS cantidad, ISNULL(K.cantidad, -1) AS existe\n"
//                    + "FROM (SELECT * FROM movimientosAlmacenDetalle WHERE idMovto="+idMovtoAlmacen+" AND idEmpaque="+idProducto+") K \n"
//                    + "RIGHT JOIN almacenesLotes L ON L.idAlmacen=K.idAlmacen AND L.idEmpaque=K.idEmpaque AND L.lote=K.lote\n"
//                    + "WHERE L.idAlmacen="+idAlmacen+" AND L.idEmpaque="+idProducto+" AND L.lote='"+lote+"'\n"
//                    + "ORDER BY L.fechaCaducidad";
//            rs = st.executeQuery(strSQL);
//            while (rs.next()) {
//                separar = rs.getDouble("disponibles");
//                if (cantidadSeparar < separar) {
//                    separar = cantidadSeparar;
//                }
//                strSQL = "UPDATE almacenesLotes SET separados=separados+" + separar + " "
//                        + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + rs.getString("lote") + "'";
//                st1.executeUpdate(strSQL);
//
//                if (rs.getInt("existe") == -1) {
//                    strSQL = "INSERT INTO movimientosAlmacenDetalle (idMovto, idEmpaque, lote, idAlmacen, cantidad, suma, fecha, existenciaAnterior) "
//                            + "VALUES (" + idMovtoAlmacen + ", " + idProducto + ", '" + rs.getString("lote") + "', " + idAlmacen + ", " + separar + ", 0, GETDATE(), " + rs.getDouble("saldo") + ")";
//                } else {
//                    strSQL = "UPDATE movimientosAlmacenDetalle "
//                            + "SET cantidad=cantidad+" + separar + ", fecha=GETDATE(), existenciaAnterior=" + rs.getDouble("saldo") + " "
//                            + "WHERE idMovto=" + idMovtoAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + rs.getString("lote") + "'";
//                }
//                st1.executeUpdate(strSQL);
//
//                separados += separar;
//                cantidadSeparar -= separar;
//                if (cantidadSeparar == 0) {
//                    break;
//                }
//            }
//            if(cantidadSeparar==0) {
//                strSQL = "UPDATE almacenesEmpaques SET separados=separados+" + separados + " "
//                        + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto;
//                st.executeUpdate(strSQL);
//            } else {
//                throw (new SQLException("Existencia almacen del producto y lotes se encuentran descuadrados !!!"));
//            }
//        } finally {
//            st.close();
//        }
//        return separados;
//    }
//    private double libera(int idAlmacen, int idMovtoAlmacen, int idProducto, double cantidadSolicitada) throws SQLException {
//        double liberar;
//        double cantidadLiberar = cantidadSolicitada;
//        double liberados = 0;
//
//        String strSQL = "SELECT K.lote, K.cantidad, ISNULL(L.cantidad, -1) AS existe\n"
//                + "FROM movimientosAlmacenDetalle K\n"
//                + "LEFT JOIN almacenesLotes L ON L.idAlmacen=K.idAlmacen AND L.idEmpaque=K.idEmpaque AND L.lote=K.lote\n"
//                + "WHERE K.idMovtoAlmacen=" + idMovtoAlmacen + " AND K.idEmpaque=" + idProducto + " "
//                + "ORDER BY L.fechaCaducidad DESC";
//        Statement st = cnx.createStatement();
//        try {
//            Statement st1 = cnx.createStatement();
//
//            ResultSet rs = st.executeQuery(strSQL);
//            while (rs.next()) {
//                liberar = rs.getDouble("cantidad");
//                if (cantidadLiberar < liberar) {
//                    liberar = cantidadLiberar;
//
//                    strSQL = "UPDATE movimientosAlmacenDetalle SET cantidad=cantidad-" + liberar + " "
//                            + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + rs.getString("lote") + "'";
//                } else {
//                    strSQL = "DELETE FROM movimientosAlmacenDetalle "
//                            + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + "AND idEmpaque=" + idProducto + " AND lote='" + rs.getString("lote") + "'";
//                }
//                st1.executeUpdate(strSQL);
//
//                if (rs.getInt("existe") == -1) {
//                    strSQL = "INSERT INTO almacenesLotes (idAlmacen, idEmpaque, lote, fechaCaducidad, cantidad, saldo, separados, existenciaFisica) "
//                            + "VALUES(" + idAlmacen + ", " + idProducto + ", '" + rs.getString("lote") + "', GETDATE(), " + liberar + ", " + liberar + ", 0, 0)";
//                } else {
//                    strSQL = "UPDATE almacenesLotes SET separados=separados-" + liberar + " "
//                            + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + rs.getString("lote") + "'";
//                }
//                st1.executeUpdate(strSQL);
//
//                liberados += liberar;
//                cantidadLiberar -= liberar;
//                if (cantidadLiberar == 0) {
//                    break;
//                }
//            }
//            strSQL = "UPDATE almacenesEmpaques "
//                    + "SET separados=separados-" + liberados + " "
//                    + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto;
//            int nRegs = st.executeUpdate(strSQL);
//            if (nRegs == 0) {
//                throw (new SQLException("No se encontro empaque (" + idProducto + ") en almacenesEmpaques !!!"));
//            }
//        } finally {
//            st.close();
//        }
//        return liberados;
//    }
//    private double separa(int idAlmacen, int idMovtoAlmacen, int idProducto, double cantidadSolicitada, boolean total) throws SQLException {
//        double cantidadSeparar=cantidadSolicitada;
//        double disponibles = 0;
//        double separados=0;
//        
//        Statement st = cnx.createStatement();
//        try {
//            String strSQL = "SELECT AE.existenciaOficina-AE.separados AS saldoOficina "
//                            + "FROM almacenesEmpaques AE "
//                            + "WHERE AE.idAlmacen=" + idAlmacen + " AND AE.idEmpaque=" + idProducto;
//            ResultSet rs = st.executeQuery(strSQL);
//            if (rs.next()) {
//                disponibles=rs.getDouble("saldoOficina");
//                if (disponibles <= 0) {
//                    throw (new SQLException("No hay existencia oficina !!!"));
//                } else if (disponibles < cantidadSeparar) {
//                    if(total) {
//                        throw (new SQLException("No hay existencia suficiente, solo hay "+disponibles+" disponibles !!!"));
//                    } else {
//                        cantidadSeparar = disponibles;
//                    }
//                }
//            } else {
//                throw (new SQLException("No se encontro producto(" + idProducto + ") en almacen !!!"));
//            }
//            
//            double separar;
//            Statement st1 = cnx.createStatement();
//            strSQL = "SELECT L.lote, L.saldo, L.saldo-L.separados AS disponibles, ISNULL(K.cantidad, 0) AS cantidad, ISNULL(K.cantidad, -1) AS existe\n"
//                    + "FROM (SELECT * FROM movimientosAlmacenDetalle WHERE idMovtoAlmacen="+idMovtoAlmacen+" AND idEmpaque="+idProducto+") K \n"
//                    + "RIGHT JOIN almacenesLotes L ON L.idAlmacen=K.idAlmacen AND L.idEmpaque=K.idEmpaque AND L.lote=K.lote\n"
//                    + "WHERE L.idAlmacen="+idAlmacen+" AND L.idEmpaque="+idProducto+" AND L.saldo-L.separados > 0 \n"
//                    + "ORDER BY L.fechaCaducidad";
//            rs = st.executeQuery(strSQL);
//            while (rs.next()) {
//                separar = rs.getDouble("disponibles");
//                if (cantidadSeparar < separar) {
//                    separar = cantidadSeparar;
//                }
//                strSQL = "UPDATE almacenesLotes SET separados=separados+" + separar + " "
//                        + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + rs.getString("lote") + "'";
//                st1.executeUpdate(strSQL);
//
//                if (rs.getInt("existe") == -1) {
//                    strSQL = "INSERT INTO movimientosAlmacenDetalle (idMovtoAlmacen, idEmpaque, lote, idAlmacen, cantidad, suma, fecha, existenciaAnterior) "
//                            + "VALUES (" + idMovtoAlmacen + ", " + idProducto + ", '" + rs.getString("lote") + "', " + idAlmacen + ", " + separar + ", 0, GETDATE(), " + rs.getDouble("saldo") + ")";
//                } else {
//                    strSQL = "UPDATE movimientosAlmacenDetalle "
//                            + "SET cantidad=cantidad+" + separar + ", fecha=GETDATE(), existenciaAnterior=" + rs.getDouble("saldo") + " "
//                            + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + rs.getString("lote") + "'";
//                }
//                st1.executeUpdate(strSQL);
//
//                separados += separar;
//                cantidadSeparar -= separar;
//                if (cantidadSeparar == 0) {
//                    break;
//                }
//            }
//            if(cantidadSeparar==0) {
//                strSQL = "UPDATE almacenesEmpaques SET separados=separados+" + separados + " "
//                        + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto;
//                st.executeUpdate(strSQL);
//            } else {
//                throw (new SQLException("Existencia almacen del producto y lotes se encuentran descuadrados !!!"));
//            }
//        } finally {
//            st.close();
//        }
//        return separados;
//    }
//    public ArrayList<TOMovimientoDetalle> completarSinCargo(int idAlmacen, int idMovto, int idMovtoAlmacen, int idProducto, double cantSolicitada, int idZonaImpuestos) throws SQLException {
//        ArrayList<TOMovimientoDetalle> agregados=new ArrayList<TOMovimientoDetalle>();
//        double cantSeparada;
//        String strSQL = "SELECT S.idEmpaque, S.idImpuesto, ISNULL(L.lote, '') AS lote\n" +
//                        "FROM (SELECT E.idEmpaque, P.idImpuesto\n" +
//                        "	FROM (SELECT E.idUnidadEmpaque, E.piezas, E.idSubEmpaque\n" +
//                        "			, P.idProducto, P.idMarca, P.idParte, P.idPresentacion, P.contenido, P.idUnidadMedida\n" +
//                        "		FROM empaques E\n" +
//                        "		INNER JOIN productos P ON P.idProducto=E.idProducto\n" +
//                        "		WHERE E.idEmpaque="+idProducto+") M\n" +
//                        "	INNER JOIN empaques E ON E.piezas=M.piezas AND E.idUnidadEmpaque=M.idUnidadEmpaque AND E.idSubEmpaque=M.idSubEmpaque\n" +
//                        "	INNER JOIN productos P ON P.idProducto=E.idProducto\n" +
//                        "	WHERE P.idMarca=M.idMarca AND P.idParte=M.idParte AND P.idPresentacion=M.idPresentacion AND P.contenido=M.contenido AND P.idUnidadMedida=M.idUnidadMedida) S\n" +
//                        "INNER JOIN almacenesLotes L ON L.idAlmacen="+idAlmacen+" AND L.idEmpaque=S.idEmpaque\n" +
//                        "LEFT JOIN (SELECT * FROM movimientosDetalle WHERE idMovto="+idMovto+") D ON D.idEmpaque=S.idEmpaque\n" +
//                        "WHERE D.idEmpaque IS NOT NULL AND ISNULL(L.saldo,0)-ISNULL(L.separados,0) > 0\n" +
//                        "ORDER BY L.fechaCaducidad";
//        ResultSet rs=stx.executeQuery(strSQL);
//        while(rs.next()) {
//            cantSeparada=this.separaLote(idAlmacen, idMovtoAlmacen, idProducto, rs.getString("lote"), cantSolicitada, false);
//            strSQL ="UPDATE movimientosDetalle\n" +
//                    "SET cantSinCargo=cantSinCargo+" + cantSeparada + "\n" +
//                    "WHERE idMovto=" + idMovto + " AND idEmpaque=" + rs.getInt("idEmpaque");
//            stx.executeUpdate(strSQL);
//            cantSolicitada-=cantSeparada;
//            if(cantSolicitada==0) {
//                break;
//            }
//        }
//        if(cantSolicitada!=0) {
//            TOMovimientoDetalle newTo;
//            strSQL ="SELECT S.idEmpaque, S.idImpuesto, ISNULL(L.lote, '') AS lote\n" +
//                    "FROM (SELECT E.idEmpaque, P.idImpuesto\n" +
//                    "	FROM (SELECT E.idUnidadEmpaque, E.piezas, E.idSubEmpaque\n" +
//                    "			, P.idProducto, P.idMarca, P.idParte, P.idPresentacion, P.contenido, P.idUnidadMedida\n" +
//                    "		FROM empaques E\n" +
//                    "		INNER JOIN productos P ON P.idProducto=E.idProducto\n" +
//                    "		WHERE E.idEmpaque="+idProducto+") M\n" +
//                    "	INNER JOIN empaques E ON E.piezas=M.piezas AND E.idUnidadEmpaque=M.idUnidadEmpaque AND E.idSubEmpaque=M.idSubEmpaque\n" +
//                    "	INNER JOIN productos P ON P.idProducto=E.idProducto\n" +
//                    "	WHERE P.idMarca=M.idMarca AND P.idParte=M.idParte AND P.idPresentacion=M.idPresentacion AND P.contenido=M.contenido AND P.idUnidadMedida=M.idUnidadMedida) S\n" +
//                    "INNER JOIN almacenesLotes L ON L.idAlmacen="+idAlmacen+" AND L.idEmpaque=S.idEmpaque\n" +
//                    "LEFT JOIN (SELECT * FROM movimientosDetalle WHERE idMovto="+idMovto+") D ON D.idEmpaque=S.idEmpaque\n" +
//                    "WHERE D.idEmpaque IS NULL AND ISNULL(L.saldo,0)-ISNULL(L.separados,0) > 0\n" +
//                    "ORDER BY L.fechaCaducidad";
//            rs=stx.executeQuery(strSQL);
//            while(rs.next()) {
//                newTo=new TOMovimientoDetalle();
//                newTo.setIdProducto(rs.getInt("idEmpaque"));
//                newTo.setIdImpuestoGrupo(rs.getInt("idImpuesto"));
//
//                cantSeparada=this.separaLote(idAlmacen, idMovtoAlmacen, newTo.getIdProducto(), rs.getString("lote"), cantSolicitada, false);
//                newTo.setCantSinCargo(cantSeparada);
//                this.agregarProductoOficina(idMovto, newTo, idZonaImpuestos);
//                this.calcularPrecioNeto(newTo);
//                agregados.add(newTo);
//
//                cantSolicitada-=cantSeparada;
//                if(cantSolicitada==0) {
//                    break;
//                }
//            }
//            if(cantSolicitada!=0) {
//                throw (new SQLException("No hay existencia suficiente para productos sin cargo !!!"));
//            }
//        }
//        return agregados;
//    }
//    public ArrayList<TOMovimientoDetalle> grabarMovimientoDetalle(int idAlmacen, int idMovto, int idMovtoAlmacen, TOMovimientoDetalle to, double separados, int idZonaImpuestos) throws SQLException {
//        ArrayList<TOMovimientoDetalle> agregados=new ArrayList<TOMovimientoDetalle>();
//        double cantSolicitada, cantSeparada, cantLiberada;
//        String strSQL;
//        
//        cnx=this.ds.getConnection();
//        stx=cnx.createStatement();
//        try {
//            stx.executeUpdate("BEGIN TRANSACTION");
//
//            if (to.getIdMovto() == 0) {
//                this.agregarProductoOficina(idMovto, to, idZonaImpuestos);
//            } else if (to.getCantFacturada() != separados) {
//                if (to.getCantFacturada() > separados) {
//                    cantSolicitada=to.getCantFacturada()-separados;
//                    
//                    cantSeparada=this.separa(idAlmacen, idMovtoAlmacen, to.getIdProducto(), cantSolicitada, true);
//                    strSQL = "UPDATE movimientosDetalle\n"
//                            + "SET cantFacturada=cantFacturada+" + cantSeparada + "\n"
//                            + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + to.getIdProducto();
//                    stx.executeUpdate(strSQL);
//                    
//                    ArrayList<Double> boletin=this.obtenerBoletinSinCargo(to);
//                    double boletinConCargo=boletin.get(0);
//                    double boletinSinCargo=boletin.get(1);
//                    if(boletinConCargo>0 && boletinSinCargo>0) {
//                        double cantSinCargo=((int)to.getCantFacturada()/boletinConCargo)*boletinSinCargo;
//                        cantSinCargo-=to.getCantSinCargo();
//                        if(cantSinCargo > 0) {
//                            cantSolicitada=cantSinCargo;
//                            cantSeparada=this.separa(idAlmacen, idMovtoAlmacen, to.getIdProducto(), cantSolicitada, false);
//                            cantSolicitada-=cantSeparada;
//                            strSQL ="UPDATE movimientosDetalle\n" +
//                                    "SET cantSinCargo=cantSinCargo+" + cantSeparada + "\n" +
//                                    "WHERE idMovto=" + idMovto + " AND idEmpaque=" + to.getIdProducto();
//                            stx.executeUpdate(strSQL);
//                            to.setCantSinCargo(to.getCantSinCargo()+cantSeparada);
//                            
//                            if(cantSolicitada!=0) {
//                                agregados=this.completarSinCargo(idAlmacen, idMovto, idMovtoAlmacen, to.getIdProducto(), cantSolicitada, idZonaImpuestos);
//                            }
//                        }
//                        strSQL= "SELECT SUM(D.cantFacturada) AS cantFacturada, SUM(D.cantSinCargo) AS cantSinCargo\n" +
//                                "FROM (SELECT E.idEmpaque, P.idImpuesto\n" +
//                                "	FROM (SELECT E.piezas, E.idUnidadEmpaque, E.idSubEmpaque\n" +
//                                "				, P.idProducto, P.idMarca, P.idParte, P.idPresentacion, P.contenido, P.idUnidadMedida\n" +
//                                "			FROM empaques E\n" +
//                                "			INNER JOIN productos P ON P.idProducto=E.idProducto\n" +
//                                "			WHERE E.idEmpaque="+to.getIdProducto()+") M\n" +
//                                "	INNER JOIN empaques E ON E.piezas=M.piezas AND E.idUnidadEmpaque=M.idUnidadEmpaque AND E.idSubEmpaque=M.idSubEmpaque\n" +
//                                "	INNER JOIN productos P ON P.idProducto=E.idProducto\n" +
//                                "	WHERE P.idMarca=M.idMarca AND P.idParte=M.idParte AND P.idPresentacion=M.idPresentacion AND P.contenido=M.contenido \n" +
//                                "			AND P.idUnidadMedida=M.idUnidadMedida) S\n" +
//                                "INNER JOIN (SELECT * FROM movimientosDetalle WHERE idMovto="+idMovto+") D ON D.idEmpaque=S.idEmpaque";
//                        ResultSet rs=stx.executeQuery(strSQL);
//                        if(rs.next()) {
//                            cantSinCargo=((int)rs.getDouble("cantFacturada")/boletinConCargo)*boletinSinCargo;
//                            cantSinCargo-=rs.getDouble("cantSinCargo");
//                            if(cantSinCargo!=0) {
//                                cantSolicitada=cantSinCargo;
//                                for(TOMovimientoDetalle d: this.completarSinCargo(idAlmacen, idMovto, idMovtoAlmacen, to.getIdProducto(), cantSolicitada, idZonaImpuestos)) {
//                                    agregados.add(d);
//                                }
//                            }
//                        }
//                    }
//                    this.calcularPrecioNeto(to);
//                } else {
//                    cantSolicitada=separados - to.getCantFacturada();
//                    
//                    cantLiberada=this.libera(idAlmacen, idMovtoAlmacen, to.getIdProducto(), cantSolicitada);
//                    strSQL = "UPDATE movimientosDetalle\n"
//                            + "SET cantFacturada=cantFacturada-" + cantLiberada + "\n"
//                            + "WHERE idMovto=" + to.getIdMovto() + " AND idEmpaque=" + to.getIdProducto();
//                    stx.executeUpdate(strSQL);
//                    
//                    ArrayList<Double> boletin=this.obtenerBoletinSinCargo(to);
//                    double boletinConCargo=boletin.get(0);
//                    double boletinSinCargo=boletin.get(1);
//                    if(boletinConCargo>0 && boletinSinCargo>0) {
//                        double cantSinCargo=((int)to.getCantFacturada()/boletinConCargo)*boletinSinCargo;
//                        cantSinCargo=to.getCantSinCargo()-cantSinCargo;
//                        if(cantSinCargo > 0) {
//                            cantSolicitada=cantSinCargo;
//                            cantLiberada=this.libera(idAlmacen, idMovtoAlmacen, to.getIdProducto(), cantSolicitada);
//                            cantSolicitada-=cantLiberada;
//                            strSQL = "UPDATE movimientosDetalle\n"
//                                    + "SET cantFacturada=cantFacturada-" + cantLiberada + "\n"
//                                    + "WHERE idMovto=" + to.getIdMovto() + " AND idEmpaque=" + to.getIdProducto();
//                            stx.executeUpdate(strSQL);
//                            to.setCantSinCargo(to.getCantSinCargo()-cantLiberada);
//                        }
//                        strSQL= "SELECT SUM(D.cantFacturada) AS cantFacturada, SUM(D.cantSinCargo) AS cantSinCargo\n" +
//                                "FROM (SELECT E.idEmpaque, P.idImpuesto\n" +
//                                "	FROM (SELECT E.piezas, E.idUnidadEmpaque, E.idSubEmpaque\n" +
//                                "				, P.idProducto, P.idMarca, P.idParte, P.idPresentacion, P.contenido, P.idUnidadMedida\n" +
//                                "			FROM empaques E\n" +
//                                "			INNER JOIN productos P ON P.idProducto=E.idProducto\n" +
//                                "			WHERE E.idEmpaque="+to.getIdProducto()+") M\n" +
//                                "	INNER JOIN empaques E ON E.piezas=M.piezas AND E.idUnidadEmpaque=M.idUnidadEmpaque AND E.idSubEmpaque=M.idSubEmpaque\n" +
//                                "	INNER JOIN productos P ON P.idProducto=E.idProducto\n" +
//                                "	WHERE P.idMarca=M.idMarca AND P.idParte=M.idParte AND P.idPresentacion=M.idPresentacion AND P.contenido=M.contenido \n" +
//                                "			AND P.idUnidadMedida=M.idUnidadMedida) S\n" +
//                                "INNER JOIN (SELECT * FROM movimientosDetalle WHERE idMovto="+idMovto+") D ON D.idEmpaque=S.idEmpaque";
//                        ResultSet rs=stx.executeQuery(strSQL);
//                        if(rs.next()) {
//                            cantSinCargo=((int)rs.getDouble("cantFacturada")/boletinConCargo)*boletinSinCargo;
//                            cantSinCargo=rs.getDouble("cantSinCargo")-cantSinCargo;
//                            if(cantSinCargo>0) {
//                                cantSolicitada=cantSinCargo;
//                                strSQL= "SELECT S.idEmpaque, ISNULL(L.lote, '') AS lote, D.cantSinCargo\n" +
//                                        "FROM (SELECT E.idEmpaque, P.idImpuesto\n" +
//                                        "	FROM (SELECT E.piezas, E.idUnidadEmpaque, E.idSubEmpaque\n" +
//                                        "				, P.idProducto, P.idMarca, P.idParte, P.idPresentacion, P.contenido, P.idUnidadMedida\n" +
//                                        "			FROM empaques E\n" +
//                                        "			INNER JOIN productos P ON P.idProducto=E.idProducto\n" +
//                                        "			WHERE E.idEmpaque="+to.getIdProducto()+") M\n" +
//                                        "	INNER JOIN empaques E ON E.piezas=M.piezas AND E.idUnidadEmpaque=M.idUnidadEmpaque AND E.idSubEmpaque=M.idSubEmpaque\n" +
//                                        "	INNER JOIN productos P ON P.idProducto=E.idProducto\n" +
//                                        "	WHERE P.idMarca=M.idMarca AND P.idParte=M.idParte AND P.idPresentacion=M.idPresentacion AND P.contenido=M.contenido AND P.idUnidadMedida=M.idUnidadMedida) S\n" +
//                                        "INNER JOIN almacenesLotes L ON L.idAlmacen="+idAlmacen+" AND L.idEmpaque=S.idEmpaque\n" +
//                                        "INNER JOIN movimientosAlmacenDetalle K ON K.idMovto="+idMovtoAlmacen+" AND K.idEmpaque=S.idEmpaque AND K.lote=L.lote\n" +
//                                        "INNER JOIN (SELECT * FROM movimientosDetalle WHERE idMovto="+idMovto+") D ON D.idEmpaque=S.idEmpaque\n" +
//                                        "ORDER BY L.fechaCaducidad DESC";
//                                rs=stx.executeQuery(strSQL);
//                                while(rs.next()) {
//                                    cantLiberada=this.liberaLote(idAlmacen, idMovtoAlmacen, to.getIdProducto(), rs.getString("lote"), cantSolicitada);
//                                    cantSolicitada-=cantLiberada;
//                                    if(cantSolicitada==0) {
//                                        break;
//                                    }
//                                }
//                                if(cantSolicitada!=0) {
//                                    throw (new SQLException("No hay existencia suficiente para liberar sin cargo. Descuadre de lotes !!!"));
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            stx.executeUpdate("COMMIT TRANSACTION");
//        } catch (SQLException ex) {
//            stx.executeUpdate("ROLLBACK TRANSACTION");
//            throw (ex);
//        } finally {
//            stx.close();
//            cnx.close();
//        }
//        return agregados;
//    }
//    public ArrayList<TOMovimientoDetalle> agregarSimilaresSinCargo1(int idAlmacen, int idMovto, int idMovtoAlmacen, int idProducto, double cantSolicitada, int idZonaImpuestos) throws SQLException {
//        ArrayList<TOMovimientoDetalle> agregados=new ArrayList<TOMovimientoDetalle>();
//        double cantSeparada;
//        String strSQL = "SELECT E.idEmpaque, L.lote, P.idImpuesto\n" +
//                        "FROM (SELECT D.idEmpaque\n" +
//                        "	FROM movimientosDetalle D\n" +
//                        "	INNER JOIN empaquesSimilares S ON S.idEmpaque=D.idEmpaque\n" +
//                        "	WHERE D.idMovto="+idMovto+" AND S.idSimilar=(SELECT idSimilar FROM empaquesSimilares WHERE idEmpaque="+idProducto+")) S\n" +
//                        "INNER JOIN empaques E ON E.idEmpaque=S.idEmpaque\n" +
//                        "INNER JOIN productos P ON P.idProducto=E.idProducto\n" +
//                        "INNER JOIN almacenesLotes L ON L.idAlmacen="+idAlmacen+" AND L.idEmpaque=S.idEmpaque\n" +
//                        "ORDER BY L.fechaCaducidad";
//        ResultSet rs=stx.executeQuery(strSQL);
//        while(rs.next()) {
//            cantSeparada=this.separaLote(idAlmacen, idMovtoAlmacen, idProducto, rs.getString("lote"), cantSolicitada, false);
//            strSQL ="UPDATE movimientosDetalle\n" +
//                    "SET cantSinCargo=cantSinCargo+" + cantSeparada + "\n" +
//                    "WHERE idMovto=" + idMovto + " AND idEmpaque=" + rs.getInt("idEmpaque");
//            stx.executeUpdate(strSQL);
//            cantSolicitada-=cantSeparada;
//            if(cantSolicitada==0) {
//                break;
//            }
//        }
//        if(cantSolicitada!=0) {
//            TOMovimientoDetalle newTo;
//            strSQL ="SELECT E.idEmpaque, L.lote, P.idImpuesto\n" +
//                    "FROM (SELECT S.idEmpaque\n" +
//                    "		FROM (SELECT idEmpaque FROM movimientosDetalle WHERE idMovto="+idMovto+") D\n" +
//                    "		RIGHT JOIN empaquesSimilares S ON S.idEmpaque=D.idEmpaque\n" +
//                    "		WHERE S.idSimilar=(SELECT idSimilar FROM empaquesSimilares WHERE idEmpaque="+idProducto+") AND D.idEmpaque IS NULL) S\n" +
//                    "INNER JOIN empaques E ON E.idEmpaque=S.idEmpaque\n" +
//                    "INNER JOIN productos P ON P.idProducto=E.idProducto\n" +
//                    "INNER JOIN almacenesLotes L ON L.idAlmacen="+idAlmacen+" AND L.idEmpaque=S.idEmpaque\n" +
//                    "ORDER BY L.fechaCaducidad";
//            rs=stx.executeQuery(strSQL);
//            while(rs.next()) {
//                newTo=new TOMovimientoDetalle();
//                newTo.setIdProducto(rs.getInt("idEmpaque"));
//                newTo.setIdImpuestoGrupo(rs.getInt("idImpuesto"));
//
//                cantSeparada=this.separaLote(idAlmacen, idMovtoAlmacen, newTo.getIdProducto(), rs.getString("lote"), cantSolicitada, false);
//                newTo.setCantSinCargo(cantSeparada);
//                this.agregarProductoOficina(idMovto, newTo, idZonaImpuestos);
//                this.calcularPrecioNeto(newTo);
//                agregados.add(newTo);
//
//                cantSolicitada-=cantSeparada;
//                if(cantSolicitada==0) {
//                    break;
//                }
//            }
//            if(cantSolicitada!=0) {
//                throw (new SQLException("No hay existencia suficiente para productos sin cargo !!!"));
//            }
//        }
//        return agregados;
//    }
}