package movimientos.dao;

import entradas.dominio.MovimientoAlmacenProducto;
import entradas.dominio.MovimientoProducto;
import entradas.to.TOEntradaProducto;
import impuestos.dominio.ImpuestosProducto;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
import movimientos.dominio.MovimientoTipo;
import movimientos.to.TOMovimiento;
import movimientos.to.TOMovimientoAlmacen;
import movimientos.to.TOMovimientoAlmacenProducto;
import movimientos.to.TOMovimientoProducto;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOMovimientos {

    int idUsuario;
    int idCedis;
    private DataSource ds = null;
//    private Connection cnx;

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

    // ----------------------------- MOVIMIENTOS DE INVENTARIO ----------------------
    public void cancelarSalidaAlmacen(int idMovto) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "UPDATE l "
                        + "SET l.separados=l.separados-k.cantidad "
                        + "FROM movimientosDetalleAlmacen k "
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=k.idMovtoAlmacen "
                        + "INNER JOIN almacenesLotes l ON l.idAlmacen=M.idAlmacen AND l.idEmpaque=k.idEmpaque AND l.lote=k.lote "
                        + "WHERE k.idMovtoAlmacen=" + idMovto;
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalleAlmacen where idMovtoAlmacen=" + idMovto;
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosAlmacen WHERE idMovtoAlmacen=" + idMovto;
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

    public void grabarSalidaAlmacen(TOMovimiento to) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement(); Statement st1 = cn.createStatement()) {
                ResultSet rs1;
                to.setFolio(this.obtenerMovimientoFolio(cn, false, to.getIdAlmacen(), to.getIdTipo()));

                strSQL = "UPDATE movimientosAlmacen SET fecha=GETDATE(), estatus=1, folio=" + to.getFolio() + ", idUsuario=" + this.idUsuario + " "
                        + "WHERE idMovtoAlmacen=" + to.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "SELECT * FROM movimientosDetalleAlmacen "
                        + "WHERE idMovtoAlmacen=" + to.getIdMovto() + " "
                        + "ORDER BY idEmpaque";
                ResultSet rs = st.executeQuery(strSQL);
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
                    strSQL = "UPDATE movimientosDetalleAlmacen "
                            + "SET fecha=GETDATE(), existenciaAnterior=" + rs1.getDouble("saldo") + " "
                            + "WHERE idMovtoAlmacen=" + to.getIdMovto() + " AND idEmpaque=" + rs.getInt("idEmpaque") + " AND lote='" + rs.getString("lote") + "'";
                    st1.executeUpdate(strSQL);

                    strSQL = "UPDATE almacenesLotes "
                            + "SET saldo=saldo-" + rs.getDouble("cantidad") + ", separados=separados-" + rs.getDouble("cantidad") + " "
                            + "WHERE idAlmacen=" + to.getIdAlmacen() + " AND idEmpaque=" + rs.getInt("idEmpaque") + " AND lote='" + rs.getString("lote") + "'";
                    st1.executeUpdate(strSQL);
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

    public void cancelarSalidaOficina(int idMovto) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
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

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void grabarSalidaOficina(TOMovimiento to) throws SQLException {
        String strSQL;

        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                to.setFolio(this.obtenerMovimientoFolio(cn, true, to.getIdAlmacen(), to.getIdTipo()));

                strSQL = "UPDATE movimientos SET fecha=GETDATE(), estatus=1, folio=" + to.getFolio() + ", idUsuario=" + this.idUsuario + " "
                        + "WHERE idMovto=" + to.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalle WHERE idMovto=" + to.getIdMovto() + " AND cantFacturada=0";
                st.executeUpdate(strSQL);

                strSQL = "UPDATE d "
                        + "SET d.costoPromedio=e.costoUnitarioPromedio, d.costo=e.costoUnitarioPromedio, d.unitario=e.costoUnitarioPromedio"
                        + "     , d.fecha=GETDATE(), d.existenciaAnterior=a.existenciaOficina "
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
                if(st.executeUpdate(strSQL)==0) {
                    throw new SQLException("No se encuentra empaque en almacen !!!");
                }

                strSQL = "UPDATE e "
                        + "SET e.existenciaOficina=e.existenciaOficina-d.cantFacturada "
                        + "FROM (SELECT m.idEmpresa, d.* "
                        + "		FROM movimientosDetalle d "
                        + "		INNER JOIN movimientos m ON m.idMovto=d.idMovto "
                        + "		WHERE d.idMovto=" + to.getIdMovto() + ") d "
                        + "INNER JOIN empresasEmpaques e ON e.idEmpresa=d.idEmpresa AND e.idEmpaque=d.idEmpaque";
                if(st.executeUpdate(strSQL)==0) {
                    throw new SQLException("No se encuentra empaque en empresa !!!");
                }

                strSQL = "UPDATE e "
                        + "SET e.costoUnitarioPromedio=0 "
                        + "FROM (SELECT m.idEmpresa, d.* "
                        + "		FROM movimientosDetalle d "
                        + "		INNER JOIN movimientos m ON m.idMovto=d.idMovto "
                        + "		WHERE d.idMovto=" + to.getIdMovto() + ") d "
                        + "INNER JOIN empresasEmpaques e ON e.idEmpresa=d.idEmpresa AND e.idEmpaque=d.idEmpaque "
                        + "WHERE e.existenciaOficina=0";
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

    public void agregarProductoSalidaOficina(int idMovto, TOMovimientoProducto to) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        String strSQL;
        try {
            strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantOrdenada, cantFacturada, cantSinCargo, cantRecibida, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior) "
                    + "VALUES (" + idMovto + ", " + to.getIdProducto() + ", 0, " + to.getCantFacturada() + ", 0, 0, 0, 0, 0, 0, 0, 0, 0, GETDATE(), 0)";
            st.executeUpdate(strSQL);
        } finally {
            st.close();
            cn.close();
        }
    }

    public void cancelarEntradaOficina(int idMovto) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "DELETE FROM movimientosDetalle where idMovto=" + idMovto;
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientos WHERE idMovto=" + idMovto;
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

    public void grabarEntradaOficina(TOMovimiento to) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                to.setFolio(this.obtenerMovimientoFolio(cn, true, to.getIdAlmacen(), to.getIdTipo()));

                strSQL = "UPDATE movimientos SET fecha=GETDATE(), estatus=1, folio=" + to.getFolio() + ", idUsuario=" + this.idUsuario + " "
                        + "WHERE idMovto=" + to.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalle WHERE idMovto=" + to.getIdMovto() + " AND cantFacturada=0";
                st.executeUpdate(strSQL);

                strSQL = "UPDATE d "
                        + "SET d.costoPromedio=e.costoUnitarioPromedio, d.costo=e.costoUnitarioPromedio, d.unitario=e.costoUnitarioPromedio"
                        + "     , d.fecha=GETDATE(), d.existenciaAnterior=a.existenciaOficina "
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
                if(st.executeUpdate(strSQL)==0) {
                    throw new SQLException("No se encontro empaque en almacen !!!");
//                    strSQL = "INSERT INTO almacenesEmpaques (idAlmacen, idEmpaque, existenciaOficina, separados, existenciaAlmacen, existenciaMinima, existenciaMaxima)\n" +
//                            "SELECT M.idAlmacen, D.idEmpaque, D.cantFacturada, 0, 0, 0, 0 \n" +
//                            "FROM movimientosDetalle D\n" +
//                            "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n" +
//                            "WHERE D.idMovto="+to.getIdMovto();
//                    st.executeUpdate(strSQL);
                }
                strSQL = "UPDATE e "
                        + "SET e.existenciaOficina=e.existenciaOficina+d.cantFacturada "
                        + "FROM (SELECT m.idEmpresa, d.* "
                        + "		FROM movimientosDetalle d "
                        + "		INNER JOIN movimientos m ON m.idMovto=d.idMovto "
                        + "		WHERE d.idMovto=" + to.getIdMovto() + ") d "
                        + "INNER JOIN empresasEmpaques e ON e.idEmpresa=d.idEmpresa AND e.idEmpaque=d.idEmpaque";
                if(st.executeUpdate(strSQL)==0) {
                    throw new SQLException("No se encontro empaque en la empresa !!!");
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

    public double actualizaEntrada(int idMovto, int idAlmacen, int idProducto, double cantidad) throws SQLException {
        String strSQL = "UPDATE movimientosDetalle "
                + "SET cantFacturada=" + cantidad + " "
                + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + idProducto;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);
            }
        }
        return cantidad;
    }

    public void agregarProductoEntradaOficina(int idMovto, TOMovimientoProducto to) throws SQLException {
        String strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantOrdenada, cantFacturada, cantSinCargo, cantRecibida, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior) "
                + "VALUES (" + idMovto + ", " + to.getIdProducto() + ", 0, " + to.getCantFacturada() + ", 0, 0, 0, 0, 0, 0, 0, 0, 0, GETDATE(), 0)";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);
            }
        }
    }

    public int agregarMovimientoOficina(TOMovimiento to) throws SQLException {
        int idMovto = 0;
        String strSQL = "INSERT INTO movimientos (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, idImpuestoZona, desctoComercial, desctoProntoPago, fecha, idUsuario, idMoneda, tipoCambio, estatus, idReferencia, referencia, propietario) "
                + "VALUES(" + to.getIdTipo() + ", " + to.getIdCedis() + ", " + to.getIdEmpresa() + ", " + to.getIdAlmacen() + ", 0, 0, 0, 0, 0, GETDATE(), " + this.idUsuario + ", 1, 1, 0, 0, 0, 0)";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {

                st.executeUpdate(strSQL);
                ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
                if (rs.next()) {
                    idMovto = rs.getInt("idMovto");
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return idMovto;
    }

    public void cancelarEntradaAlmacen(int idMovto) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "DELETE FROM movimientosDetalleAlmacen where idMovtoAlmacen=" + idMovto;
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosAlmacen WHERE idMovtoAlmacen=" + idMovto;
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

    public void grabarEntradaAlmacen(TOMovimiento to) throws SQLException {
        double saldo;
        String strSQL;

        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement(); Statement st1 = cn.createStatement()) {
                ResultSet rs, rs1;

                to.setFolio(this.obtenerMovimientoFolio(cn, false, to.getIdAlmacen(), to.getIdTipo()));

                strSQL = "UPDATE movimientosAlmacen SET fecha=GETDATE(), estatus=1, folio=" + to.getFolio() + ", idUsuario=" + this.idUsuario + " "
                        + "WHERE idMovtoAlmacen=" + to.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "SELECT * FROM movimientosDetalleAlmacen "
                        + "WHERE idMovtoAlmacen=" + to.getIdMovto() + " "
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

                    strSQL = "UPDATE movimientosDetalleAlmacen "
                            + "SET fecha=GETDATE(), existenciaAnterior=" + saldo + " "
                            + "WHERE idMovtoAlmacen=" + to.getIdMovto() + " AND idEmpaque=" + rs.getInt("idEmpaque") + " AND lote='" + rs.getString("lote") + "'";
                    st1.executeUpdate(strSQL);
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

//    public ArrayList<TOMovimientoAlmacen> movimientosPendientesAlmacen(int entrada) throws SQLException {
//        ArrayList<TOMovimientoAlmacen> lista = new ArrayList<>();
//        String strSQL = "SELECT m.* "
//                + "FROM movimientosAlmacen m "
//                + "INNER JOIN movimientosTipos t ON t.idTipo=m.idTipo "
//                + "WHERE m.idCedis=" + this.idCedis + " AND m.estatus=0 AND t.eliminable=1 AND t.suma=" + entrada + " "
//                + "ORDER BY m.idAlmacen";
//        try (Connection cn = this.ds.getConnection()) {
//            try (Statement st = cn.createStatement()) {
//                ResultSet rs = st.executeQuery(strSQL);
//                while (rs.next()) {
//                        lista.add(this.construirMovimientoAlmacen(rs));
//                }
//            }
//        }
//        return lista;
//    }
//    
//    public ArrayList<TOMovimiento> movimientosPendientes(int entrada) throws SQLException {
//        ArrayList<TOMovimiento> lista = new ArrayList<>();
//        String strSQL = "SELECT m.* "
//                + "FROM movimientos m "
//                + "INNER JOIN movimientosTipos t ON t.idTipo=m.idTipo "
//                + "WHERE m.idCedis=" + this.idCedis + " AND m.estatus=0 AND t.eliminable=1 AND t.suma=" + entrada + " "
//                + "ORDER BY m.idAlmacen";
//        try (Connection cn = this.ds.getConnection()) {
//            try (Statement st = cn.createStatement()) {
//                ResultSet rs = st.executeQuery(strSQL);
//                while (rs.next()) {
//                        lista.add(this.construirMovimientoRelacionado(rs));
//                }
//            }
//        }
//        return lista;
//    }

    public int agregarMovimientoAlmacen(TOMovimiento to) throws SQLException {
        int idMovtoAlmacen = 0;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                String strSQL = "INSERT INTO movimientosAlmacen (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, fecha, idUsuario, estatus, idReferencia, referencia, propietario) "
                        + "VALUES (" + to.getIdTipo() + ", " + to.getIdCedis() + ", " + to.getIdEmpresa() + ", " + to.getIdAlmacen() + ", 0, 0, GETDATE(), " + this.idUsuario + ", 0, 0, 0, 0)";
                st.executeUpdate(strSQL);

                ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idMovtoAlmacen");
                if (rs.next()) {
                    idMovtoAlmacen = rs.getInt("idMovtoAlmacen");
                }
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

    public MovimientoTipo obtenerMovimientoTipo(int idTipo) throws SQLException {
        MovimientoTipo t = null;
        String strSQL = "SELECT tipo FROM movimientosTipos WHERE idTipo=" + idTipo;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    t = new MovimientoTipo(idTipo, rs.getString("tipo"));
                }
            }
        }
        return t;
    }

    public ArrayList<MovimientoTipo> obtenerMovimientosTipos(boolean suma) throws SQLException {
        ArrayList<MovimientoTipo> lst = new ArrayList<>();
        String strSQL = "SELECT idTipo, tipo FROM movimientosTipos WHERE suma=" + (suma ? 1 : 0) + " AND eliminable=1";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    lst.add(new MovimientoTipo(rs.getInt("idTipo"), rs.getString("tipo")));
                }
            }
        }
        return lst;
    }

    // ----------------------------- TRASPASOS --------------------------------------
    public void grabarTraspasoRecepcion(TOMovimiento m, ArrayList<TOEntradaProducto> detalle) throws SQLException {
        String strSQL;
        double existenciaAnterior, promedioPonderado, existenciaOficina, saldo;

        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs;

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

                    strSQL = "SELECT costoUnitarioPromedio, existenciaOficina FROM empresasEmpaques "
                            + "WHERE idEmpresa=" + m.getIdEmpresa() + " AND idEmpaque=" + to.getIdProducto();
                    rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        promedioPonderado = rs.getDouble("costoUnitarioPromedio");
                        existenciaOficina = rs.getDouble("existenciaOficina");
                        promedioPonderado = (promedioPonderado * existenciaOficina + to.getUnitario() * to.getCantFacturada()) / (existenciaOficina + to.getCantFacturada());
                        strSQL = "UPDATE empresasEmpaques "
                                + "SET costoUnitarioPromedio=" + promedioPonderado + ", existenciaOficina=existenciaOficina+" + to.getCantFacturada() + " "
                                + "WHERE idEmpresa=" + m.getIdEmpresa() + " AND idEmpaque=" + to.getIdProducto();
                    } else {
                        strSQL = "INSERT INTO empresasEmpaques (idEmpresa, idEmpaque, costoUnitarioPromedio, existenciaOficina, idMovtoUltimaEntrada) "
                                + "VALUES (" + m.getIdEmpresa() + ", " + to.getIdProducto() + ", " + to.getUnitario() + ", " + to.getCantFacturada() + ", 0)";
                    }
                    st.executeUpdate(strSQL);

                    for (Lote l : to.getLotes()) {
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
                        strSQL = "UPDATE movimientosDetalleAlmacen "
                                + "SET cantidad=" + l.getSeparados() + ", existenciaAnterior=" + saldo + ", fecha=GETDATE() "
                                + "WHERE idMovtoAlmacen=" + m.getIdMovtoAlmacen() + " AND idEmpaque=" + to.getIdProducto() + " AND lote='" + l.getLote() + "'";
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

    public void grabarTraspasoEnvio(TOMovimiento m, ArrayList<TOMovimientoProducto> detalle) throws SQLException {
        String strSQL;
        double sumaLotes, costoPromedio;

        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement(); Statement st1 = cn.createStatement();) {
                ResultSet rs;

                strSQL = "UPDATE movimientosAlmacen SET fecha=GETDATE(), estatus=1 WHERE idMovtoAlmacen=" + m.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientos SET fecha=GETDATE(), estatus=1 WHERE idMovto=" + m.getIdMovto();
                st.executeUpdate(strSQL);

                for (TOMovimientoProducto d : detalle) {
                    sumaLotes = 0;
                    strSQL = "SELECT K.lote, K.cantidad, ISNULL(L.saldo, 0) AS saldo "
                            + "FROM movimientosDetalleAlmacen K "
                            + "LEFT JOIN almacenesLotes L ON L.idAlmacen=K.idAlmacen AND L.idEmpaque=K.idEmpaque AND L.lote=K.lote "
                            + "WHERE K.idAlmacen=" + m.getIdAlmacen() + " AND K.idMovtoAlmacen=" + m.getIdMovtoAlmacen() + " AND K.idEmpaque=" + d.getIdProducto();
                    rs = st.executeQuery(strSQL);
                    while (rs.next()) {
                        if (rs.getDouble("saldo") < rs.getDouble("cantidad")) {
                            throw new SQLException("No hay saldo o No se encontro el lote(" + rs.getString("lote") + ") del producto(" + d.getIdProducto() + ") en el almacen");
                        }
                        strSQL = "UPDATE movimientosDetalleAlmacen "
                                + "SET existenciaAnterior=" + rs.getDouble("saldo") + ", fecha=GETDATE() "
                                + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idMovtoAlmacen=" + m.getIdMovtoAlmacen() + " AND idEmpaque=" + d.getIdProducto() + " AND lote='" + rs.getString("lote") + "'";
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
                        strSQL = "SELECT AE.existenciaOficina AS saldo, ISNULL(EE.existenciaOficina, 0) AS existencia, ISNULL(EE.costoUnitarioPromedio, 0) AS costo "
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
                        d.setCostoPromedio(rs.getDouble("costo"));
                        d.setCosto(rs.getDouble("costo"));
                        d.setUnitario(rs.getDouble("costo"));
                        costoPromedio = d.getCostoPromedio();
                        if (d.getCantFacturada() == rs.getDouble("existencia")) {
                            costoPromedio = 0;    // Cuando ya no hay existencia el costo promedio de la empresa se hace cero
                        }
                        strSQL = "UPDATE movimientosDetalle "
                                + "SET costoPromedio=" + d.getCostoPromedio() + ", costo=" + d.getCosto() + ", unitario=" + d.getUnitario() + ", cantFacturada=" + d.getCantFacturada() + ", existenciaAnterior=" + rs.getDouble("saldo") + ", fecha=GETDATE() "
                                + "WHERE idMovto=" + m.getIdMovto() + " AND idEmpaque=" + d.getIdProducto();
                        st.executeUpdate(strSQL);

                        strSQL = "UPDATE almacenesEmpaques "
                                + "SET existenciaOficina=existenciaOficina-" + d.getCantFacturada() + ", separados=separados-" + d.getCantFacturada() + " "
                                + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=" + d.getIdProducto();
                        st.executeUpdate(strSQL);

                        strSQL = "UPDATE empresasEmpaques SET existenciaOficina=existenciaOficina-" + d.getCantFacturada() + ", costoUnitarioPromedio=" + costoPromedio + " "
                                + "WHERE idEmpresa=" + m.getIdEmpresa() + " AND idEmpaque=" + d.getIdProducto();
                        st.executeUpdate(strSQL);
                    }
                }
                // ----------------------- SECCION: CREAR ENLACE ENVIO-RECEPCION ------------------

                int folioRecepcion = this.obtenerMovimientoFolio(cn, true, m.getIdAlmacen(), 9);
                strSQL = "UPDATE movimientos SET referencia=" + folioRecepcion + " WHERE idMovto=" + m.getIdMovto();
                st.executeUpdate(strSQL);

                int folioRecepcionAlmacen = this.obtenerMovimientoFolio(cn, false, m.getIdAlmacen(), 9);
                strSQL = "UPDATE movimientosAlmacen SET referencia=" + folioRecepcionAlmacen + " WHERE idMovtoAlmacen=" + m.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                // ------------------------- SECCION: CREAR RECEPCION ---------------------

                int idCedisDestino = 0;
                strSQL = "SELECT idCedis FROM almacenes WHERE idAlmacen=" + m.getIdReferencia();
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    idCedisDestino = rs.getInt("idCedis");
                } else {
                    throw new SQLException("No se encontro almacen=" + m.getIdReferencia());
                }
                int idMovto = 0;
                strSQL = "INSERT INTO movimientos (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, idImpuestoZona, desctoComercial, desctoProntoPago, fecha, idUsuario, idMoneda, tipoCambio, estatus, idReferencia, referencia, propietario) "
                        + "VALUES(9, " + idCedisDestino + ", " + m.getIdEmpresa() + ", " + m.getIdReferencia() + ", " + folioRecepcion + ", 0, 0, 0, 0, GETDATE(), " + this.idUsuario + ", 1, 1, 0, " + m.getIdAlmacen() + ", " + m.getFolio() + ", 0)";
                st.executeUpdate(strSQL);
                rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
                if (rs.next()) {
                    idMovto = rs.getInt("idMovto");
                }
                int idMovtoAlmacen = 0;
                strSQL = "INSERT INTO movimientosAlmacen (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, fecha, idUsuario, estatus, idReferencia, referencia, propietario) "
                        + "VALUES (9, " + idCedisDestino + ", " + m.getIdEmpresa() + ", " + m.getIdReferencia() + ", " + folioRecepcionAlmacen + ", 0, GETDATE(), " + this.idUsuario + ", 0, " + m.getIdAlmacen() + ", " + m.getFolioAlmacen() + ", 0)";
                st.executeUpdate(strSQL);
                rs = st.executeQuery("SELECT @@IDENTITY AS idMovtoAlmacen");
                if (rs.next()) {
                    idMovtoAlmacen = rs.getInt("idMovtoAlmacen");
                }
                strSQL = "INSERT INTO movimientosRelacionados (idMovto, idMovtoAlmacen) VALUES (" + idMovto + ", " + idMovtoAlmacen + ")";
                st.executeUpdate(strSQL);

                strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantOrdenada, cantFacturada, cantSinCargo, cantRecibida, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior)\n"
                        + "SELECT " + idMovto + ", idEmpaque, cantFacturada, cantFacturada, cantSinCargo, cantRecibida, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, GETDATE(), 0\n"
                        + "FROM movimientosDetalle WHERE idMovto=" + m.getIdMovto() + " AND cantFacturada > 0";
                st.executeUpdate(strSQL);

                strSQL = "INSERT INTO movimientosDetalleAlmacen (idMovtoAlmacen, idEmpaque, lote, idAlmacen, cantidad, suma, fecha, existenciaAnterior)\n"
                        + "SELECT " + idMovtoAlmacen + ", MD.idEmpaque, MD.lote, " + m.getIdReferencia() + ", MD.cantidad, 1, GETDATE(), 0 AS existenciaAnterior\n"
                        + "FROM movimientosDetalleAlmacen MD\n"
                        + "WHERE MD.idMovtoAlmacen=" + m.getIdMovtoAlmacen() + " AND MD.cantidad > 0";
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
    
    public ArrayList<TOMovimientoAlmacen> obtenerMovimientosAlmacen(int idAlmacen, int idTipo, int estatus) throws SQLException {
        ArrayList<TOMovimientoAlmacen> solicitudes = new ArrayList<>();
        String strSQL = "SELECT M.* FROM movimientosAlmacen M "
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=" + idTipo + " AND M.estatus BETWEEN 0 AND " + estatus;
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                solicitudes.add(this.construirMovimientoAlmacen(rs));
            }
        } finally {
            cn.close();
        }
        return solicitudes;
    }

    public ArrayList<TOMovimiento> obtenerMovimientos(int idAlmacen, int idTipo, int estatus) throws SQLException {
//        if (fechaInicial == null) {
//            fechaInicial = new Date();
//        }
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TOMovimiento> solicitudes = new ArrayList<>();
//        String strSQL = "SELECT M.*"
//                + "     , ISNULL(MA.idMovtoAlmacen, 0) AS idMovtoAlmacen, MA.folio AS folioAlmacen, ISNULL(MA.fecha, GETDATE()) AS fechaAlmacen"
//                + "     , ISNULL(MA.idUsuario, 0) AS idUsuarioAlmacen, ISNULL(MA.estatus, 0) AS statusAlmacen "
//                + "FROM movimientos M "
//                + "LEFT JOIN movimientosRelacionados MR ON MR.idMovto=M.idMovto "
//                + "LEFT JOIN movimientosAlmacen MA ON MA.idMovtoAlmacen=MR.idMovtoAlmacen "
//                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=" + idTipo + " AND M.estatus BETWEEN 0 AND " + estatus + " AND CONVERT(date, M.fecha) <= '" + format.format(fechaInicial) + "'";
        String strSQL = "SELECT M.*"
                + "     , ISNULL(MA.idMovtoAlmacen, 0) AS idMovtoAlmacen, MA.folio AS folioAlmacen, ISNULL(MA.fecha, GETDATE()) AS fechaAlmacen"
                + "     , ISNULL(MA.idUsuario, 0) AS idUsuarioAlmacen, ISNULL(MA.estatus, 0) AS statusAlmacen "
                + "FROM movimientos M "
                + "LEFT JOIN movimientosRelacionados MR ON MR.idMovto=M.idMovto "
                + "LEFT JOIN movimientosAlmacen MA ON MA.idMovtoAlmacen=MR.idMovtoAlmacen "
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=" + idTipo + " AND M.estatus BETWEEN 0 AND " + estatus;
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                solicitudes.add(this.construirMovimientoRelacionado(rs));
            }
        } finally {
            cn.close();
        }
        return solicitudes;
    }

    public void grabarTraspasoSolicitud(TOMovimiento m, ArrayList<MovimientoProducto> productos) throws SQLException {
        String strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantOrdenada, cantFacturada, cantSinCargo, cantRecibida, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior) "
                + "VALUES (?, ?, ?, 0, 0, 0, 0, 0, 0, 0, 0, 0, ?, GETDATE(), 0)";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement(); PreparedStatement ps = cn.prepareStatement(strSQL)) {
                m.setFolio(this.obtenerMovimientoFolio(cn, true, m.getIdAlmacen(), 35));
                strSQL = "INSERT INTO movimientos (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, idImpuestoZona, desctoComercial, desctoProntoPago, fecha, idUsuario, idMoneda, tipoCambio, estatus, propietario, idReferencia, referencia) "
                        + "VALUES (35, " + m.getIdCedis() + ", " + m.getIdEmpresa() + ", " + m.getIdAlmacen() + ", " + m.getFolio() + ", 0, " + m.getIdImpuestoZona() + ", 0, 0, GETDATE(), " + this.idUsuario + ", 1, 1, 0, 0, " + m.getIdReferencia() + ", 0)";
                st.executeUpdate(strSQL);
                ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
                if (rs.next()) {
                    m.setIdMovto(rs.getInt("idMovto"));
                }
                m.setFolioAlmacen(this.obtenerMovimientoFolio(cn, false, m.getIdAlmacen(), 35));
                strSQL = "INSERT INTO movimientosAlmacen (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, fecha, idUsuario, propietario, idReferencia, referencia, estatus) "
                        + "VALUES (35, " + m.getIdCedis() + ", " + m.getIdEmpresa() + ", " + m.getIdAlmacen() + ", " + m.getFolioAlmacen() + ", 0, GETDATE(), " + this.idUsuario + ", 0, " + m.getIdReferencia() + ", 0, 0)";
                st.executeUpdate(strSQL);
                rs = st.executeQuery("SELECT @@IDENTITY AS idMovtoAlmacen");
                if (rs.next()) {
                    m.setIdMovtoAlmacen(rs.getInt("idMovtoAlmacen"));
                }
                strSQL = "INSERT INTO movimientosRelacionados (idMovto, idMovtoAlmacen) VALUES (" + m.getIdMovto() + ", " + m.getIdMovtoAlmacen() + ")";
                st.executeUpdate(strSQL);

                for (MovimientoProducto p : productos) {
                    ps.setInt(1, m.getIdMovto());
                    ps.setInt(2, p.getProducto().getIdProducto());
                    ps.setDouble(3, p.getCantOrdenada());
                    ps.setInt(4, p.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());
                    ps.executeUpdate();
                }
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw (e);
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    // ------------------------------- COMPRAS --------------------------------------
    public void grabarCompraAlmacen(TOMovimientoAlmacen m, ArrayList<MovimientoAlmacenProducto> productos) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                m.setFolio(this.obtenerMovimientoFolio(cn, false, m.getIdAlmacen(), 1));

                String lote = "";
                strSQL = "SELECT lote FROM lotes WHERE fecha=CONVERT(date, GETDATE())";
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    lote = rs.getString("lote") + "1";
                } else {
                    throw new SQLException("No se encontro el lote de fecha de hoy");
                }
                strSQL = "INSERT INTO movimientosAlmacen (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, fecha, idUsuario, estatus, idReferencia, referencia, propietario) "
                        + "VALUES (1, " + m.getIdCedis() + ", " + m.getIdEmpresa() + ", " + m.getIdAlmacen() + ", " + m.getFolio() + ", " + m.getIdComprobante() + ", GETDATE(), " + this.idUsuario + ", 1, " + m.getIdReferencia() + ", " + m.getReferencia() + ", 0)";
                st.executeUpdate(strSQL);
                rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
                if (rs.next()) {
                    m.setIdMovto(rs.getInt("idMovto"));
                }
                int idProducto;
                double existenciaAnterior;
                for (MovimientoAlmacenProducto p : productos) {
                    if (p.getCantidad() > 0) {
                        idProducto = p.getProducto().getIdProducto();

                        existenciaAnterior = 0;
                        strSQL = "SELECT saldo FROM almacenesLotes "
                                + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                        rs = st.executeQuery(strSQL);
                        if (rs.next()) {
                            existenciaAnterior = rs.getDouble("saldo");
                            strSQL = "UPDATE almacenesLotes "
                                    + "SET cantidad=cantidad+" + p.getCantidad() + ", saldo=saldo+" + p.getCantidad() + " "
                                    + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                        } else {
                            strSQL = "INSERT INTO almacenesLotes (idAlmacen, idEmpaque, lote, fechaCaducidad, cantidad, saldo, separados, existenciaFisica) "
                                    + "VALUES (" + m.getIdAlmacen() + ", " + idProducto + ", '" + lote + "', DATEADD(DAY, 365, convert(date, GETDATE())), " + p.getCantidad() + ", " + p.getCantidad() + ", 0, 0)";
                        }
                        st.executeUpdate(strSQL);

                        strSQL = "INSERT INTO movimientosDetalleAlmacen (idAlmacen, idMovtoAlmacen, idEmpaque, lote, cantidad, suma, fecha, existenciaAnterior) "
                                + "VALUES (" + m.getIdAlmacen() + ", " + m.getIdMovto() + ", " + idProducto + ", '" + lote + "', " + p.getCantidad() + ", 1, GETDATE(), " + existenciaAnterior + ")";
                        st.executeUpdate(strSQL);
                    }
                }
                if (m.getReferencia() != 0) {
                    strSQL = "UPDATE OCD\n"
                            + "SET OCD.cantRecibidaAlmacen=OCD.cantRecibidaAlmacen+MD.cantidad\n"
                            + "FROM ordenCompraDetalle OCD\n"
                            + "INNER JOIN movimientosDetalleAlmacen MD ON MD.idMovtoAlmacen=" + m.getIdMovto() + " AND MD.idEmpaque=OCD.idEmpaque\n"
                            + "WHERE OCD.idOrdenCompra=" + m.getReferencia();
                    st.executeUpdate(strSQL);

                    strSQL = "SELECT SUM(CASE WHEN OCD.cantRecibidaAlmacen < (OCD.cantOrdenada+OCD.cantOrdenadaSinCargo) THEN 1 ELSE 0 END) AS faltantes\n"
                            + "FROM ordenCompraDetalle OCD\n"
                            + "WHERE OCD.idOrdenCompra=" + m.getReferencia();
                    rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        if (rs.getInt("faltantes") == 0) {
                            strSQL = "UPDATE OC SET OC.estadoAlmacen=3 FROM ordenCompra OC WHERE OC.idOrdenCompra=" + m.getReferencia();
                            st.executeUpdate(strSQL);
                        }
                    } else {
                        throw new SQLException("No se encontro orden de compra");
                    }
                }
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw (e);
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void grabarCompraOficina(TOMovimiento m, ArrayList<MovimientoProducto> productos) throws SQLException {
        boolean nueva = false;
        int idEmpaque, idImpuestoGrupo;
        double existenciaAnterior;
        ArrayList<ImpuestosProducto> impuestos;

        String strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantFacturada, cantSinCargo, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, cantOrdenada, cantRecibida, idImpuestoGrupo, fecha, existenciaAnterior) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, getdate(), ?)";

        String strSQL1 = "UPDATE movimientosDetalle "
                + "SET costo=?, desctoProducto1=?, desctoProducto2=?, desctoConfidencial=?, unitario=?, cantFacturada=?, cantSinCargo=?, existenciaAnterior=? "
                + "WHERE idMovto=" + m.getIdMovto() + " AND idEmpaque=?";

        String strSQL2 = "UPDATE movimientosDetalleImpuestos "
                + "SET importe=? "
                + "WHERE idMovto=" + m.getIdMovto() + " AND idEmpaque=?";

//        String strSQL3="INSERT INTO kardexOficina (idAlmacen, idMovto, idTipoMovto, idEmpaque, fecha, existenciaAnterior, cantidad) " +
//                    "VALUES ("+m.getIdAlmacen()+", "+m.getIdMovto()+", 1, ?, GETDATE(), ?, ?)";
//        PreparedStatement ps3=cn.prepareStatement(strSQL3);

        String strSQL4 = "INSERT INTO almacenesEmpaques (idAlmacen, idEmpaque, existenciaOficina, existenciaMinima, existenciaMaxima) "
                + "VALUES (?, ?, ?, 0, 0)";

        String strSQL5 = "UPDATE almacenesEmpaques "
                + "SET existenciaOficina=existenciaOficina+? "
                + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=?";

        String strSQL6 = "INSERT INTO empresasEmpaques (idEmpresa, idEmpaque, costoUnitarioPromedio, existenciaOficina, idMovtoUltimaEntrada) "
                + "VALUES (" + m.getIdEmpresa() + ", ?, ?, ?, ?)";

        String strSQL7 = "UPDATE empresasEmpaques "
                + "SET existenciaOficina=existenciaOficina+?"
                + ", costoUnitarioPromedio=(existenciaOficina*costoUnitarioPromedio+?*?)/(existenciaOficina+?)"
                + ", idMovtoUltimaEntrada=? "
                + "WHERE idEmpresa=" + m.getIdEmpresa() + " AND idEmpaque=?";

        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement();
                    PreparedStatement ps = cn.prepareStatement(strSQL);
                    PreparedStatement ps1 = cn.prepareStatement(strSQL1);
                    PreparedStatement ps2 = cn.prepareStatement(strSQL2);
                    PreparedStatement ps4 = cn.prepareStatement(strSQL4);
                    PreparedStatement ps5 = cn.prepareStatement(strSQL5);
                    PreparedStatement ps6 = cn.prepareStatement(strSQL6);
                    PreparedStatement ps7 = cn.prepareStatement(strSQL7)) {
                ResultSet rs;
                st.executeUpdate("BEGIN TRANSACTION");

                if (m.getIdMovto() == 0) {
                    nueva = true;
                    m.setFolio(this.obtenerMovimientoFolio(cn, true, m.getIdAlmacen(), 1));

                    strSQL = "INSERT INTO movimientos (idTipo, idCedis, folio, idEmpresa, idAlmacen, idComprobante, idImpuestoZona, idMoneda, tipoCambio, desctoComercial, desctoProntoPago, idUsuario, fecha, estatus, idReferencia, referencia, propietario) "
                            + "VALUES (1, " + m.getIdCedis() + ", " + m.getFolio() + ", " + m.getIdEmpresa() + ", " + m.getIdAlmacen() + ", " + m.getIdComprobante() + ", " + m.getIdImpuestoZona() + ", " + m.getIdMoneda() + ", " + m.getTipoDeCambio() + ", " + m.getDesctoComercial() + ", " + m.getDesctoProntoPago() + ", " + this.idUsuario + ", getdate(), 1, " + m.getIdReferencia() + ", " + m.getReferencia() + ", 0)";
                    st.executeUpdate(strSQL);

                    rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
                    if (rs.next()) {
                        m.setIdMovto(rs.getInt("idMovto"));
                    }
                } else {
                    strSQL = "SELECT estatus FROM movimientos where idMovto=" + m.getIdMovto();
                    rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        if (rs.getBoolean("estatus")) {
                            throw new SQLException("Ya se ha capturado y cerrado la entrada");
                        } else {
                            strSQL = "UPDATE movimientos SET estatus=1 WHERE idMovto=" + m.getIdMovto();
                            st.executeUpdate(strSQL);
                        }
                    } else {
                        throw new SQLException("No se encontro el movimiento");
                    }
                    st.executeUpdate("UPDATE movimientos "
                            + "SET idMoneda=" + m.getIdMoneda() + ", tipoCambio=" + m.getTipoDeCambio() + " "
                            + ", desctoComercial=" + m.getDesctoComercial() + ", desctoProntoPago=" + m.getDesctoProntoPago() + " "
                            + ", fecha=GETDATE(), estatus=1 "
                            + "WHERE idMovto=" + m.getIdMovto());
                }

                //rs=st.executeQuery("select DATEPART(weekday, getdate()-1) AS DIA, DATEPART(week, GETDATE()) AS SEM, DATEPART(YEAR, GETDATE())%10 AS ANIO");
                //lote=""+rs.getInt("DIA")+String.format("%02d", rs.getInt("SEM"))+rs.getInt("ANIO")+"1";

                for (MovimientoProducto p : productos) {
                    idEmpaque = p.getProducto().getIdProducto();

                    if (p.getCantFacturada() > 0 || p.getCantSinCargo() > 0) {
                        rs = st.executeQuery("SELECT existenciaOficina "
                                + "FROM almacenesEmpaques "
                                + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=" + idEmpaque);
                        if (rs.next()) {
                            existenciaAnterior = rs.getDouble("existenciaOficina");

                            ps5.setDouble(1, p.getCantFacturada() + p.getCantSinCargo());
                            ps5.setInt(2, idEmpaque);
                            ps5.executeUpdate();

                            ps7.setDouble(1, p.getCantFacturada() + p.getCantSinCargo());
                            ps7.setDouble(2, p.getCantFacturada() + p.getCantSinCargo());
                            ps7.setDouble(3, p.getCostoPromedio());
                            ps7.setDouble(4, p.getCantFacturada() + p.getCantSinCargo());
                            ps7.setInt(5, m.getIdMovto());
                            ps7.setInt(6, idEmpaque);
                            ps7.executeUpdate();
                        } else {
                            existenciaAnterior = 0;

                            ps4.setInt(1, m.getIdAlmacen());
                            ps4.setInt(2, idEmpaque);
                            ps4.setDouble(3, p.getCantFacturada() + p.getCantSinCargo());
                            ps4.executeUpdate();

                            rs = st.executeQuery("SELECT existenciaOficina "
                                    + "FROM empresasEmpaques "
                                    + "WHERE idEmpresa=" + m.getIdEmpresa() + " AND idEmpaque=" + idEmpaque);
                            if (rs.next()) {
                                ps7.setDouble(1, p.getCantFacturada() + p.getCantSinCargo());
                                ps7.setDouble(2, p.getCantFacturada() + p.getCantSinCargo());
                                ps7.setDouble(3, p.getCostoPromedio());
                                ps7.setDouble(4, p.getCantFacturada() + p.getCantSinCargo());
                                ps7.setInt(5, m.getIdMovto());
                                ps7.setInt(6, idEmpaque);
                                ps7.executeUpdate();
                            } else {
                                ps6.setInt(1, idEmpaque);
                                ps6.setDouble(2, p.getCostoPromedio());
                                ps6.setDouble(3, p.getCantFacturada() + p.getCantSinCargo());
                                ps6.setInt(4, m.getIdMovto());
                                ps6.executeUpdate();
                            }
                        }
//                    ps3.setInt(1, idEmpaque);
//                    ps3.setDouble(2, existenciaAnterior);
//                    ps3.setDouble(3, p.getCantFacturada()+p.getCantSinCargo());
//                    ps3.executeUpdate();
                        if (nueva) {
                            ps.setInt(1, m.getIdMovto());
                            ps.setInt(2, idEmpaque);
                            ps.setDouble(3, p.getCantFacturada());
                            ps.setDouble(4, p.getCantSinCargo());
                            ps.setDouble(5, p.getCostoPromedio());
                            ps.setDouble(6, p.getCosto());
                            ps.setDouble(7, p.getDesctoProducto1());
                            ps.setDouble(8, p.getDesctoProducto2());
                            ps.setDouble(9, p.getDesctoConfidencial());
                            ps.setDouble(10, p.getUnitario());
                            ps.setDouble(11, p.getCantOrdenada());
                            ps.setDouble(12, 0);
                            ps.setInt(13, p.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());
                            ps.setDouble(14, existenciaAnterior);
                            ps.executeUpdate();

                            idImpuestoGrupo = p.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo();
                            this.agregarImpuestosProducto(cn, m.getIdMovto(), idEmpaque, idImpuestoGrupo, m.getIdImpuestoZona());
                            this.calculaImpuestosProducto(cn, m.getIdMovto(), idEmpaque, p.getUnitario());
                        } else {
                            ps1.setDouble(1, p.getCosto());
                            ps1.setDouble(2, p.getDesctoProducto1());
                            ps1.setDouble(3, p.getDesctoProducto2());
                            ps1.setDouble(4, p.getDesctoConfidencial());
                            ps1.setDouble(5, p.getUnitario());
                            ps1.setDouble(6, p.getCantFacturada());
                            ps1.setDouble(7, p.getCantSinCargo());
                            ps1.setDouble(8, existenciaAnterior);
                            ps1.setInt(9, idEmpaque);
                            ps1.executeUpdate();
                        }
                        impuestos = p.getImpuestos();
                        for (ImpuestosProducto i : impuestos) {
                            ps2.setDouble(1, i.getImporte());
                            ps2.setInt(2, idEmpaque);
                            ps2.executeUpdate();
                        }
                    }
                }
                if (m.getReferencia() != 0) {
                    strSQL = "UPDATE OCD\n"
                            + "SET OCD.cantRecibidaOficina=OCD.cantRecibidaOficina+(MD.cantFacturada+MD.cantSinCargo)\n"
                            + "FROM ordenCompraDetalle OCD\n"
                            + "INNER JOIN movimientosDetalle MD ON MD.idMovto=" + m.getIdMovto() + " AND MD.idEmpaque=OCD.idEmpaque\n"
                            + "WHERE OCD.idOrdenCompra=" + m.getReferencia();
                    st.executeUpdate(strSQL);

                    strSQL = "SELECT SUM(CASE WHEN OCD.cantRecibidaOficina < (OCD.cantOrdenada+OCD.cantOrdenadaSinCargo) THEN 1 ELSE 0 END) AS faltantes\n"
                            + "FROM ordenCompraDetalle OCD\n"
                            + "WHERE OCD.idOrdenCompra=" + m.getReferencia();
                    rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        if (rs.getInt("faltantes") == 0) {
                            strSQL = "UPDATE OC SET estado=3 FROM ordenCompra OC WHERE OC.idOrdenCompra=" + m.getReferencia();
                            st.executeUpdate(strSQL);
                        }
                    } else {
                        throw new SQLException("No se encontro orden de compra");
                    }
                }
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw (e);
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private void actualizarExistenciaOficina(Connection cn, int idMovto) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            strSQL = "UPDATE E\n"
                    + "SET E.existenciaOficina=E.existenciaOficina-(D.cantFacturada+D.cantSinCargo)\n"
                    + "FROM almacenesEmpaques E\n"
                    + "INNER JOIN movimientosDetalle D ON D.idMovto=" + idMovto + " AND D.idEmpaque=E.idEmpaque\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "WHERE E.idAlmacen=M.idAlmacen";
            st.executeUpdate(strSQL);

            strSQL = "UPDATE E\n"
                    + "SET E.existenciaOficina=E.existenciaOficina-(D.cantFacturada+D.cantSinCargo)\n"
                    + "FROM empresasEmpaques E\n"
                    + "INNER JOIN movimientosDetalle D ON D.idMovto=" + idMovto + " AND D.idEmpaque=E.idEmpaque\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "WHERE E.idEmpresa=M.idEmpresa";
            st.executeUpdate(strSQL);

            strSQL = "UPDATE E\n"
                    + "SET E.costoUnitarioPromedio=0\n"
                    + "FROM empresasEmpaques E\n"
                    + "INNER JOIN movimientosDetalle D ON D.idMovto=" + idMovto + " AND D.idEmpaque=E.idEmpaque\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "WHERE E.idEmpresa=M.idEmpresa AND E.existenciaOficina=0";
            st.executeUpdate(strSQL);
        }
    }

    private void validarExistenciaOficina(Connection cn, int idMovto) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            strSQL = "SELECT E.*, E.existenciaOficina-E.separados-(D.cantFacturada+D.cantSinCargo) AS disponibles\n"
                    + "FROM almacenesEmpaques E\n"
                    + "INNER JOIN movimientosDetalle D ON D.idMovto=" + idMovto + " AND D.idEmpaque=E.idEmpaque\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "WHERE E.idAlmacen=M.idAlmacen AND E.existenciaOficina-E.separados<(D.cantFacturada-D.cantSinCargo)";
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                throw new SQLException("Existencia insuficiente en almacen para realizar movimiento !!!");
            }
            strSQL = "SELECT E.*, E.existenciaOficina-(D.cantFacturada+D.cantSinCargo) AS disponibles\n"
                    + "FROM empresasEmpaques E\n"
                    + "INNER JOIN movimientosDetalle D ON D.idMovto=" + idMovto + " AND D.idEmpaque=E.idEmpaque\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "WHERE E.idEmpresa=M.idEmpresa AND E.existenciaOficina<(D.cantFacturada-D.cantSinCargo)";
            rs = st.executeQuery(strSQL);
            if (rs.next()) {
                throw new SQLException("Existencia insuficiente en empresa para realizar movimiento !!!");
            }
        }
    }

    public void cancelarCompraAlmacen(int idMovto, int idAlmacen, int idOrdenDeCompra) throws SQLException {
        String strSQL;
        int idMovtoTipo = 34;
        int idMovtoCancelacion;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "SELECT M.idAlmacen, MD.idEmpaque, MD.lote, L.saldo-L.separados AS saldo, MD.cantidad\n"
                        + "FROM movimientosDetalleAlmacen MD\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=MD.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes L ON L.idAlmacen=M.idAlmacen AND L.idEmpaque=MD.idEmpaque AND L.lote=MD.lote\n"
                        + "WHERE MD.idMovtoAlmacen=" + idMovto + " AND L.saldo-L.separados < MD.cantidad";
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    throw new SQLException("Existencia insuficiente en almacen para realizar movimiento !!!");
                }
                strSQL = "UPDATE movimientosAlmacen SET estatus=3 WHERE idMovtoAlmacen=" + idMovto;
                st.executeUpdate(strSQL);

                int folio = this.obtenerMovimientoFolio(cn, false, idAlmacen, idMovtoTipo);
                strSQL = "INSERT INTO movimientosAlmacen (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, fecha, idUsuario, idReferencia, referencia, propietario, estatus)\n"
                        + "SELECT " + idMovtoTipo + ", idCedis, idEmpresa, idAlmacen, " + folio + ", idComprobante, GETDATE(), " + this.idUsuario + ", idReferencia, referencia, 0, 1\n"
                        + "FROM movimientosAlmacen WHERE idMovtoAlmacen=" + idMovto;
                st.executeUpdate(strSQL);

                idMovtoCancelacion = 0;
                rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
                if (rs.next()) {
                    idMovtoCancelacion = rs.getInt("idMovto");
                }
                strSQL = "INSERT INTO movimientosDetalleAlmacen\n"
                        + "SELECT " + idMovtoCancelacion + ", MD.idEmpaque, MD.lote, M.idAlmacen, MD.cantidad, 0, GETDATE(), L.saldo\n"
                        + "FROM movimientosDetalleAlmacen MD\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=MD.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes L ON L.idAlmacen=M.idAlmacen AND L.idEmpaque=MD.idEmpaque AND L.lote=MD.lote\n"
                        + "WHERE MD.idMovtoAlmacen=" + idMovto;
                st.executeUpdate(strSQL);

                strSQL = "UPDATE L\n"
                        + "SET L.saldo=L.saldo-MD.cantidad\n"
                        + "FROM movimientosDetalleAlmacen MD\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=MD.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes L ON L.idAlmacen=M.idAlmacen AND L.idEmpaque=MD.idEmpaque AND L.lote=MD.lote\n"
                        + "WHERE MD.idMovtoAlmacen=" + idMovto;
                st.executeUpdate(strSQL);

                if (idOrdenDeCompra != 0) {
                    strSQL = "UPDATE OCD\n"
                            + "SET OCD.cantRecibidaAlmacen=OCD.cantRecibidaAlmacen-MD.cantidad\n"
                            + "FROM ordenCompraDetalle OCD\n"
                            + "INNER JOIN movimientosDetalleAlmacen MD ON MD.idMovto=" + idMovto + " AND MD.idEmpaque=OCD.idEmpaque\n"
                            + "WHERE OCD.idOrdenCompra=" + idOrdenDeCompra;
                    st.executeUpdate(strSQL);

                    strSQL = "SELECT SUM(CASE WHEN OCD.cantRecibidaAlmacen < (OCD.cantOrdenada+OCD.cantOrdenadaSinCargo) THEN 1 ELSE 0 END) AS faltantes\n"
                            + "FROM ordenCompraDetalle OCD\n"
                            + "WHERE OCD.idOrdenCompra=" + idOrdenDeCompra;
                    rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        if (rs.getInt("faltantes") == 0) {
                            strSQL = "UPDATE OC SET estado=2 FROM ordenCompra OC WHERE OC.idOrdenCompra=" + idOrdenDeCompra;
                            st.executeUpdate(strSQL);
                        }
                    } else {
                        throw new SQLException("No se encontro orden de compra");
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

    public void cancelarCompra(int idMovto, int idAlmacen, int idOrdenDeCompra) throws SQLException {
        String strSQL;
        int idMovtoTipo = 34;
        int idMovtoCancelacion;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                this.validarExistenciaOficina(cn, idMovto);

                strSQL = "UPDATE movimientos SET estatus=3 WHERE idMovto=" + idMovto;
                st.executeUpdate(strSQL);

                int folio = this.obtenerMovimientoFolio(cn, true, idAlmacen, idMovtoTipo);
                strSQL = "INSERT INTO movimientos (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, idImpuestoZona, desctoComercial, desctoProntoPago, fecha, idUsuario, idMoneda, tipoCambio, idReferencia, referencia, propietario, estatus)\n"
                        + "SELECT " + idMovtoTipo + ", idCedis, idEmpresa, idAlmacen, " + folio + ", idComprobante, idImpuestoZona, desctoComercial, desctoProntoPago, GETDATE(), " + this.idUsuario + ", idMoneda, tipoCambio, idReferencia, referencia, 0, 1\n"
                        + "FROM movimientos WHERE idMovto=" + idMovto;
                st.executeUpdate(strSQL);

                idMovtoCancelacion = 0;
                ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
                if (rs.next()) {
                    idMovtoCancelacion = rs.getInt("idMovto");
                }
                strSQL = "INSERT INTO movimientosDetalle\n"
                        + "SELECT " + idMovtoCancelacion + ", D.idEmpaque, D.cantOrdenada, D.cantFacturada, D.cantSinCargo, D.cantRecibida, EE.costoUnitarioPromedio, D.costo, D.desctoProducto1, D.desctoProducto2, D.desctoConfidencial, D.unitario, D.idImpuestoGrupo, GETDATE(), E.existenciaOficina\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN almacenesEmpaques E ON E.idAlmacen=M.idAlmacen AND E.idEmpaque=D.idEmpaque\n"
                        + "INNER JOIN empresasEmpaques EE ON EE.idEmpresa=M.idEmpresa AND EE.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + idMovto;
                st.executeUpdate(strSQL);

                strSQL = "INSERT INTO movimientosDetalleImpuestos\n"
                        + "SELECT " + idMovtoCancelacion + ", idEmpaque, idImpuesto, impuesto, valor, aplicable, modo, acreditable, importe, acumulable\n"
                        + "FROM movimientosDetalleImpuestos WHERE idMovto=" + idMovto;
                st.executeUpdate(strSQL);

                this.actualizarExistenciaOficina(cn, idMovto);

                if (idOrdenDeCompra != 0) {
                    strSQL = "UPDATE OCD\n"
                            + "SET OCD.cantRecibidaOficina=OCD.cantRecibidaOficina-(MD.cantFacturada+MD.cantSinCargo)\n"
                            + "FROM ordenCompraDetalle OCD\n"
                            + "INNER JOIN movimientosDetalle MD ON MD.idMovto=" + idMovto + " AND MD.idEmpaque=OCD.idEmpaque\n"
                            + "WHERE OCD.idOrdenCompra=" + idOrdenDeCompra;
                    st.executeUpdate(strSQL);

                    strSQL = "SELECT SUM(CASE WHEN OCD.cantRecibidaOficina < (OCD.cantOrdenada+OCD.cantOrdenadaSinCargo) THEN 1 ELSE 0 END) AS faltantes\n"
                            + "FROM ordenCompraDetalle OCD\n"
                            + "WHERE OCD.idOrdenCompra=" + idOrdenDeCompra;
                    rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        if (rs.getInt("faltantes") == 0) {
                            strSQL = "UPDATE OC SET estado=2 FROM ordenCompra OC WHERE OC.idOrdenCompra=" + idOrdenDeCompra;
                            st.executeUpdate(strSQL);
                        }
                    } else {
                        throw new SQLException("No se encontro orden de compra");
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

//  ==========================  MOVIMIENTOS ALMACEN  ====================================
    public TOMovimientoAlmacenProducto construirDetalleAlmacen(ResultSet rs) throws SQLException {
        TOMovimientoAlmacenProducto to = new TOMovimientoAlmacenProducto();
        to.setIdMovtoAlmacen(rs.getInt("idMovtoAlmacen"));
        to.setIdProducto(rs.getInt("idEmpaque"));
        to.setCantOrdenada(rs.getDouble("cantOrdenada"));
        to.setCantRecibida(rs.getDouble("cantRecibida"));
        to.setCantidad(rs.getDouble("cantidad"));
        return to;
    }

    public ArrayList<TOMovimientoAlmacenProducto> obtenerDetalleAlmacen(int idMovtoAlmacen) throws SQLException {
        ArrayList<TOMovimientoAlmacenProducto> productos = new ArrayList<>();
        String strSQL = "SELECT *, 0 AS cantOrdenada, 0 AS cantRecibida FROM movimientosDetalleAlmacen WHERE idMovtoAlmacen=" + idMovtoAlmacen;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    productos.add(this.construirDetalleAlmacen(rs));
                }
            }
        }
        return productos;
    }

    public ArrayList<TOMovimientoAlmacenProducto> obtenerDetalleAlmacenPorEmpaque(int idMovtoAlmacen) throws SQLException {
        ArrayList<TOMovimientoAlmacenProducto> lista = new ArrayList<>();
        String strSQL = "SELECT idEmpaque, SUM(cantidad) AS cantidad "
                + "FROM movimientosDetalleAlmacen k "
                + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " "
                + "GROUP BY idEmpaque";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                TOMovimientoAlmacenProducto to;
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    to = new TOMovimientoAlmacenProducto();
                    to.setIdMovtoAlmacen(idMovtoAlmacen);
                    to.setIdProducto(rs.getInt("idEmpaque"));
                    to.setCantidad(rs.getDouble("cantidad"));
                    lista.add(to);
                }
            }
        }
        return lista;
    }
    
    private TOMovimientoAlmacen construirMovimientoAlmacen(ResultSet rs) throws SQLException {
        TOMovimientoAlmacen to = new TOMovimientoAlmacen();
        to.setIdMovto(rs.getInt("idMovtoAlmacen"));
        to.setIdTipo(rs.getInt("idTipo"));
        to.setFolio(rs.getInt("folio"));
        to.setIdCedis(rs.getInt("idCedis"));
        to.setIdEmpresa(rs.getInt("idEmpresa"));
        to.setIdAlmacen(rs.getInt("idAlmacen"));
        to.setIdComprobante(rs.getInt("idComprobante"));
        to.setFecha(new java.util.Date(rs.getDate("fecha").getTime()));
        to.setIdUsuario(rs.getInt("idUsuario"));
        to.setEstatus(rs.getInt("estatus"));
        to.setIdReferencia(rs.getInt("idReferencia"));
        to.setReferencia(rs.getInt("referencia"));
        return to;
    }

    public ArrayList<TOMovimientoAlmacen> obtenerMovimientosAlmacen(int idComprobante) throws SQLException {
        ArrayList<TOMovimientoAlmacen> tos = new ArrayList<>();
        String strSQL = "SELECT * FROM movimientosAlmacen WHERE idTipo=1 AND idComprobante=" + idComprobante;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    tos.add(construirMovimientoAlmacen(rs));
                }
            }
        }
        return tos;
    }

    // ========================== MOVIMIENTOS OFICINA ====================================
    public TOMovimientoProducto construirDetalle(ResultSet rs) throws SQLException {
        TOMovimientoProducto to = new TOMovimientoProducto();
        to.setIdMovto(rs.getInt("idMovto"));
        to.setIdProducto(rs.getInt("idEmpaque"));
        to.setCantOrdenada(rs.getDouble("cantOrdenada"));
//        to.setCantOrdenadaSinCargo(rs.getDouble("cantOrdenadaSinCargo"));
        to.setCantRecibida(rs.getDouble("cantRecibida"));
//        to.setCantRecibidaSinCargo(rs.getDouble("cantRecibidaSinCargo"));
        to.setCantFacturada(rs.getDouble("cantFacturada"));
        to.setCantSinCargo(rs.getDouble("cantSinCargo"));
        to.setCostoPromedio(rs.getDouble("costoPromedio"));
        to.setCosto(rs.getDouble("costo"));
        to.setDesctoProducto1(rs.getDouble("desctoProducto1"));
        to.setDesctoProducto2(rs.getDouble("desctoProducto2"));
        to.setDesctoConfidencial(rs.getDouble("desctoConfidencial"));
        to.setUnitario(rs.getDouble("unitario"));
        return to;
    }

    public ArrayList<TOMovimientoProducto> obtenerDetalle(int idMovto) throws SQLException {
        ArrayList<TOMovimientoProducto> productos = new ArrayList<>();
        String strSQL = "SELECT * FROM movimientosDetalle WHERE idMovto=" + idMovto;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    productos.add(this.construirDetalle(rs));
                }
            }
        }
        return productos;
    }

    private TOMovimiento construirMovimientoRelacionado(ResultSet rs) throws SQLException {
        TOMovimiento to = new TOMovimiento();
        to.setIdMovto(rs.getInt("idMovto"));
        to.setIdTipo(rs.getInt("idTipo"));
        to.setIdCedis(rs.getInt("idCedis"));
        to.setIdEmpresa(rs.getInt("idEmpresa"));
        to.setIdAlmacen(rs.getInt("idAlmacen"));
        to.setFolio(rs.getInt("folio"));
        to.setIdComprobante(rs.getInt("idComprobante"));
        to.setIdImpuestoZona(rs.getInt("idImpuestoZona"));
        to.setDesctoComercial(rs.getDouble("desctoComercial"));
        to.setDesctoProntoPago(rs.getDouble("desctoprontoPago"));
        to.setFecha(new java.util.Date(rs.getDate("fecha").getTime()));
        to.setIdUsuario(rs.getInt("idUsuario"));
        to.setIdMoneda(rs.getInt("idMoneda"));
        to.setTipoDeCambio(rs.getDouble("tipoCambio"));
        to.setIdReferencia(rs.getInt("idReferencia"));
        to.setReferencia(rs.getInt("referencia"));
        to.setPropietario(rs.getInt("propietario"));
        to.setEstatus(rs.getInt("estatus"));
        to.setIdMovtoAlmacen(rs.getInt("idMovtoAlmacen"));
        to.setFolioAlmacen(rs.getInt("folioAlmacen"));
        return to;
    }

    public TOMovimiento obtenerMovimientoRelacionado(int idMovto) throws SQLException {
        TOMovimiento to = null;
        String strSQL = "SELECT M.*"
                + ", ISNULL(MA.idMovtoAlmacen, 0) AS idMovtoAlmacen, MA.folio AS folioAlmacen, ISNULL(MA.fecha, GETDATE()) AS fechaAlmacen"
                + ", ISNULL(MA.idUsuario, 0) AS idUsuarioAlmacen, ISNULL(MA.estatus, 0) AS statusAlmacen "
                + "FROM movimientos M "
                + "LEFT JOIN movimientosRelacionados MR ON MR.idMovto=M.idMovto "
                + "LEFT JOIN movimientosAlmacen MA ON MA.idMovtoAlmacen=MR.idMovtoAlmacen "
                + "WHERE M.idMovto=" + idMovto;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    to = construirMovimientoRelacionado(rs);
                }
            }
        }
        return to;
    }

    public ArrayList<TOMovimiento> obtenerMovimientosRelacionados(int idComprobante) throws SQLException {
        ArrayList<TOMovimiento> tos = new ArrayList<>();
        String strSQL = "SELECT M.*"
                + ", ISNULL(MA.idMovtoAlmacen, 0) AS idMovtoAlmacen, MA.folio AS folioAlmacen, ISNULL(MA.fecha, GETDATE()) AS fechaAlmacen"
                + ", ISNULL(MA.idUsuario, 0) AS idUsuarioAlmacen, ISNULL(MA.estatus, 0) AS statusAlmacen "
                + "FROM movimientos M "
                + "LEFT JOIN movimientosRelacionados MR ON MR.idMovto=M.idMovto "
                + "LEFT JOIN movimientosAlmacen MA ON MA.idMovtoAlmacen=MR.idMovtoAlmacen "
                + "WHERE M.idTipo=1 AND M.idComprobante=" + idComprobante;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    tos.add(construirMovimientoRelacionado(rs));
                }
            }
        }
        return tos;
    }

    private int obtenerMovimientoFolio(Connection cn, boolean oficina, int idAlmacen, int idTipo) throws SQLException {
        int folio;
        String tabla = "movimientosFoliosAlmacen";
        if (oficina) {
            tabla = "movimientosFolios";
        }
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT folio FROM " + tabla + " WHERE idAlmacen=" + idAlmacen + " AND idTipo=" + idTipo);
            if (rs.next()) {
                folio = rs.getInt("folio");
                st.executeUpdate("UPDATE " + tabla + " SET folio=folio+1 WHERE idAlmacen=" + idAlmacen + " AND idTipo=" + idTipo);
            } else {
                folio = 1;
                st.executeUpdate("INSERT INTO " + tabla + " (idAlmacen, idTipo, folio) VALUES (" + idAlmacen + ", " + idTipo + ", 2)");
            }
        }
        return folio;
    }

//  ================================  ORDEN DE COMPRA  ===================================
    public void cerrarOrdenDeCompra(boolean oficina, int idOrdenDeCompra) throws SQLException {
        String strSQL;
        if (oficina) {
            strSQL = "UPDATE ordenCompra SET estado=3 WHERE idOrdenCompra=" + idOrdenDeCompra;
        } else {
            strSQL = "UPDATE ordenCompra SET estadoAlmacen=3 WHERE idOrdenCompra=" + idOrdenDeCompra;
        }
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);
            }
        }
    }

    public ArrayList<TOMovimientoAlmacenProducto> obtenerOrdenDeCompraDetalleAlmacen(int idOrdenDeCompra) throws SQLException {
        ArrayList<TOMovimientoAlmacenProducto> productos = new ArrayList<>();
        String strSQL = "SELECT 0 AS idMovtoAlmacen, OCD.idEmpaque, OCD.cantOrdenada+OCD.cantOrdenadaSinCargo AS cantOrdenada, OCD.costoOrdenado\n"
                + "       , ISNULL(MD.cantRecibida,0) AS cantRecibida, 0 AS cantidad, '' AS lote\n"
                + "FROM (SELECT MD.idEmpaque, SUM(MD.cantidad) AS cantRecibida\n"
                + "		FROM movimientosDetalleAlmacen MD\n"
                + "		INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=MD.idMovtoAlmacen\n"
                + "		WHERE M.referencia=" + idOrdenDeCompra + "\n"
                + "		GROUP BY MD.idEmpaque) MD\n"
                + "RIGHT JOIN ordenCompraDetalle OCD ON OCD.idEmpaque=MD.idEmpaque\n"
                + "WHERE OCD.idOrdenCompra=" + idOrdenDeCompra;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    if (rs.getDouble("cantRecibida") < rs.getDouble("cantOrdenada")) {
                        productos.add(this.construirDetalleAlmacen(rs));
                    }
                }
            }
        }
        return productos;
    }

    public ArrayList<TOMovimientoProducto> obtenerOrdenDeCompraDetalle(int idOrdenDeCompra) throws SQLException {
        String strSQL;
        ArrayList<TOMovimientoProducto> productos = new ArrayList<>();
            strSQL = "SELECT 0 AS idMovto, OCD.idEmpaque, OCD.cantOrdenada, OCD.cantOrdenadaSinCargo, OCD.costoOrdenado AS costo\n"
                    + "       , ISNULL(MD.cantRecibida,0) AS cantRecibida, ISNULL(MD.cantRecibidaSinCargo, 0) AS cantRecibidaSinCargo\n"
                    + "       , 0 AS cantFacturada, 0 AS cantSinCargo\n"
                    + "       , OCD.descuentoProducto AS desctoProducto1, OCD.descuentoProducto2 AS desctoProducto2\n"
                    + "       , OCD.desctoConfidencial, 0 AS unitario, 0 AS costoPromedio\n"
                    + "FROM (SELECT MD.idEmpaque, SUM(MD.cantFacturada) AS cantRecibida, SUM(MD.cantSinCargo) AS cantRecibidaSinCargo\n"
                    + "		FROM movimientosDetalle MD\n"
                    + "		INNER JOIN movimientos M ON M.idMovto=MD.idMovto\n"
                    + "		WHERE M.referencia=" + idOrdenDeCompra + "\n"
                    + "		GROUP BY MD.idEmpaque) MD\n"
                    + "RIGHT JOIN ordenCompraDetalle OCD ON OCD.idEmpaque=MD.idEmpaque\n"
                    + "WHERE OCD.idOrdenCompra=" + idOrdenDeCompra;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    if (rs.getDouble("cantRecibida") + rs.getDouble("cantRecibidaSinCargo") < rs.getDouble("cantOrdenada") + rs.getDouble("cantOrdenadaSinCargo")) {
                        productos.add(this.construirDetalle(rs));
                    }
                }
            }
        }
        return productos;
    }

    public double obtenerPrecioUltimaCompra(int idEmpresa, int idEmpaque) throws SQLException {
        double precioLista = 0;
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "SELECT idMovtoUltimaEntrada FROM empresasEmpaques "
                        + "WHERE idEmpresa=" + idEmpresa + " AND idEmpaque=" + idEmpaque;
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
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw (e);
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return precioLista;
    }

//  ===============================  IMPUESTOS  =========================================================
    public double obtenerImpuestosProducto(int idMovto, int idEmpaque, ArrayList<ImpuestosProducto> impuestos) throws SQLException {
        double importeImpuestos = 0;
        ImpuestosProducto impuesto;
        String strSQL = "select idImpuesto, impuesto, valor, aplicable, modo, acreditable, importe, acumulable\n"
                + "from movimientosDetalleImpuestos\n"
                + "where idMovto=" + idMovto + " and idEmpaque=" + idEmpaque + "\n"
                + "order by acumulable";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    impuesto = construirImpuestosProducto(rs);
                    importeImpuestos += impuesto.getImporte();
                    impuestos.add(impuesto);
                }
            }
        }
        return importeImpuestos;
    }

    private void calculaImpuestosProducto(Connection cn, int idMovto, int idEmpaque, double unitario) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
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
        }
    }

    private void agregarImpuestosProducto(Connection cn, int idMovto, int idEmpaque, int idImpuestoGrupo, int idZona) throws SQLException {
        String strSQL = "insert into movimientosDetalleImpuestos (idMovto, idEmpaque, idImpuesto, impuesto, valor, aplicable, modo, acreditable, importe, acumulable) "
                + "select " + idMovto + ", " + idEmpaque + ", id.idImpuesto, i.impuesto, id.valor, i.aplicable, i.modo, i.acreditable, 0.00 as importe, i.acumulable "
                + "from impuestosDetalle id "
                + "inner join impuestos i on i.idImpuesto=id.idImpuesto "
                + "where id.idGrupo=" + idImpuestoGrupo + " and id.idZona=" + idZona + " and GETDATE() between fechaInicial and fechaFinal";

        try (Statement st = cn.createStatement()) {
            if (st.executeUpdate(strSQL) == 0) {
                throw (new SQLException("No se generaron impuestos !!!"));
            }
        }
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

    public ArrayList<ImpuestosProducto> generarImpuestosProducto(int idImpuestoGrupo, int idZona) throws SQLException {
        ArrayList<ImpuestosProducto> impuestos = new ArrayList<>();
        String strSQL = "SELECT id.idImpuesto, i.impuesto, id.valor, i.aplicable, i.modo, i.acreditable, 0.00 as importe, i.acumulable\n"
                + "FROM impuestosDetalle id\n"
                + "INNER JOIN impuestos i ON i.idImpuesto=id.idImpuesto\n"
                + "WHERE id.idGrupo=" + idImpuestoGrupo + " and id.idZona=" + idZona + " and GETDATE() between id.fechaInicial and id.fechaFinal\n"
                + "ORDER BY i.acumulable";
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                impuestos.add(this.construirImpuestosProducto(rs));
            }
            if (impuestos.isEmpty()) {
                throw new SQLException("No se generaron impuestos !!!");
            }
        } finally {
            cn.close();
        }
        return impuestos;
    }
}
