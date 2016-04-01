package devoluciones.dao;

import comprobantes.to.TOComprobante;
import devoluciones.to.TODevolucionProducto;
import devoluciones.to.TODevolucionProductoAlmacen;
import impuestos.dominio.ImpuestosProducto;
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
import movimientos.to.TOMovimientoOficina;
import pedidos.Pedidos;
import pedidos.to.TOPedido;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAODevoluciones {

    int idUsuario, idCedis;
    private DataSource ds = null;

    public DAODevoluciones() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
        this.idUsuario = usuarioSesion.getUsuario().getId();
        this.idCedis = usuarioSesion.getUsuario().getIdCedis();

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
    }
    
    public void eliminar(TOMovimientoOficina toDev) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "DELETE FROM movimientosDetalleAlmacen WHERE idMovtoAlmacen=" + toDev.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosAlmacen WHERE idMovtoAlmacen=" + toDev.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);
                
                strSQL = "DELETE FROM movimientosDetalle WHERE idMovto=" + toDev.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientos WHERE idMovto=" + toDev.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalleImpuestos WHERE idMovto=" + toDev.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM comprobantes WHERE idComprobante=" + toDev.getIdComprobante();
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
    
    public void grabar(TOMovimientoOficina toDev) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                toDev.setIdUsuario(this.idUsuario);
                toDev.setPropietario(0);
                toDev.setEstatus(7);
                
                toDev.setFolio(movimientos.Movimientos.obtenMovimientoFolioAlmacen(cn, toDev.getIdAlmacen(), toDev.getIdTipo()));
                movimientos.Movimientos.grabaMovimientoAlmacen(cn, toDev);
                movimientos.Movimientos.actualizaDetalleAlmacen(cn, toDev.getIdMovtoAlmacen(), true);
                
                toDev.setFolio(movimientos.Movimientos.obtenMovimientoFolioOficina(cn, toDev.getIdAlmacen(), toDev.getIdTipo()));
                movimientos.Movimientos.grabaMovimientoOficina(cn, toDev);
                movimientos.Movimientos.actualizaDetalleOficina(cn, toDev.getIdMovto(), toDev.getIdTipo(), true);
                movimientos.Movimientos.liberarMovimientoOficina(cn, toDev.getIdMovto(), this.idUsuario);
                
                strSQL = "UPDATE comprobantes\n"
                        + "SET fechaFactura=GETDATE(), tipo=2, numero='" + String.valueOf(toDev.getFolio()) + "'\n"
                        + "WHERE idComprobante=" + toDev.getIdComprobante();
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

    public void liberar(TOMovimientoOficina toDev) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                movimientos.Movimientos.liberarMovimientoOficina(cn, toDev.getIdMovto(), this.idUsuario);
                toDev.setPropietario(0);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public ArrayList<TODevolucionProducto> obtenerDetalle(TOMovimientoOficina toDev) throws SQLException {
        ArrayList<TODevolucionProducto> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                detalle = this.obtenDetalle(cn, toDev);
                movimientos.Movimientos.bloquearMovimientoOficina(cn, toDev, this.idUsuario);
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

    public ArrayList<TOMovimientoOficina> obtenerDevoluciones(int idAlmacen, int idComprobante) throws SQLException {
        ArrayList<TOMovimientoOficina> lista = new ArrayList<>();
        String strSQL = "SELECT Dev.*\n"
                + "FROM movimientos Dev\n"
                + "INNER JOIN movimientos M ON M.idMovto=Dev.referencia\n"
                + "INNER JOIN comprobantes C ON C.idComprobante=M.idComprobante\n"
                + "WHERE Dev.idAlmacen=" + idAlmacen + " AND Dev.idTipo=2 AND C.idComprobante=" + idComprobante;
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                lista.add(movimientos.Movimientos.construirMovimientoOficina(rs));
            }
        } finally {
            cn.close();
        }
        return lista;
    }

    public void gestionar(int idMovto, TODevolucionProductoAlmacen toProd, double separados) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "UPDATE movimientosDetalleAlmacen SET cantidad=" + toProd.getCantidad() + "\n"
                        + "WHERE idMovtoAlmacen=" + toProd.getIdMovtoAlmacen() + " AND idEmpaque=" + toProd.getIdProducto() + " AND lote='" + toProd.getLote() + "'";
                st.executeUpdate(strSQL);

                if (toProd.getCantidad() > separados) {
                    strSQL = "UPDATE movimientosDetalle SET cantFacturada=cantFacturada+" + (toProd.getCantidad() - separados) + "\n"
                            + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + toProd.getIdProducto();
                } else {
                    strSQL = "UPDATE movimientosDetalle SET cantFacturada=cantFacturada-" + (separados - toProd.getCantidad()) + "\n"
                            + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + toProd.getIdProducto();
                }
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

    private TODevolucionProductoAlmacen construirProductoAlmacen(ResultSet rs) throws SQLException {
        TODevolucionProductoAlmacen toProd = new TODevolucionProductoAlmacen();
        toProd.setCantVendida(rs.getDouble("cantVendida"));
        toProd.setCantDevuelta(rs.getDouble("cantDevuelta"));
        movimientos.Movimientos.construirProductoAlmacen(rs, toProd);
        return toProd;
    }

    public ArrayList<TODevolucionProductoAlmacen> obtenerDetalleAlmacen(int idMovtoAlmacenVta, int idMovtoAlmacenDev, int idProducto) throws SQLException {
        ArrayList<TODevolucionProductoAlmacen> detalle = new ArrayList<>();
        String strSQL = "SELECT Vta.cantVendida, Vta.cantDevuelta, Dev.*\n"
                + "FROM (SELECT Vta.idEmpaque, Vta.lote, ISNULL(Dev.cantDevuelta, 0) AS cantDevuelta, Vta.cantidad AS cantVendida\n"
                + "	  FROM (SELECT V.idMovtoAlmacen, MD.idEmpaque, MD.lote, SUM(MD.cantidad) AS cantDevuelta\n"
                + "			FROM movimientosDetalleAlmacen MD\n"
                + "			INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=MD.idMovtoAlmacen\n"
                + "			INNER JOIN movimientosAlmacen V ON V.idMovtoAlmacen=M.referencia\n"
                + "			WHERE V.idMovtoAlmacen=" + idMovtoAlmacenVta + " AND M.idTipo=2 AND M.estatus=7\n"
                + "			GROUP BY V.idMovtoAlmacen, MD.idEmpaque, MD.lote) Dev\n"
                + "	  RIGHT JOIN movimientosDetalleAlmacen Vta ON Vta.idMovtoAlmacen=Dev.idMovtoAlmacen AND Vta.idEmpaque=Dev.idEmpaque AND Vta.lote=Dev.lote\n"
                + "	  WHERE Vta.idMovtoAlmacen=" + idMovtoAlmacenVta + ") Vta\n"
                + "INNER JOIN movimientosDetalleAlmacen Dev ON Dev.idEmpaque=Vta.idEmpaque AND Dev.lote=Vta.lote\n"
                + "WHERE Dev.idMovtoAlmacen=" + idMovtoAlmacenDev + " AND Dev.idEmpaque=" + idProducto;
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                detalle.add(this.construirProductoAlmacen(rs));
            }
        } finally {
            cn.close();
        }
        return detalle;
    }

    private TODevolucionProducto construirProducto(ResultSet rs) throws SQLException {
        TODevolucionProducto toProd = new TODevolucionProducto();
        toProd.setCantVendida(rs.getDouble("cantVendida"));
        toProd.setCantVendidaSinCargo(rs.getDouble("cantVendidaSinCargo"));
        toProd.setCantDevuelta(rs.getDouble("cantDevuelta"));
        movimientos.Movimientos.construirProductoOficina(rs, toProd);
        return toProd;
    }

    private ArrayList<TODevolucionProducto> obtenDetalle(Connection cn, TOMovimientoOficina toDev) throws SQLException {
        ArrayList<TODevolucionProducto> detalle = new ArrayList<>();
        String strSQL = "SELECT Vta.*, Dev.*\n"
                + "FROM (SELECT Vta.idEmpaque, ISNULL(Dev.cantDevuelta, 0) AS cantDevuelta\n"
                + "			, Vta.cantFacturada AS cantVendida, Vta.cantSinCargo AS cantVendidaSinCargo\n"
                + "	  FROM (SELECT V.idMovto, MD.idEmpaque, SUM(MD.cantFacturada) AS cantDevuelta\n"
                + "			FROM movimientosDetalle MD\n"
                + "			INNER JOIN movimientos M ON M.idMovto=MD.idMovto\n"
                + "			INNER JOIN movimientos V ON V.idMovto=M.referencia\n"
                + "			WHERE V.idMovto=" + toDev.getReferencia() + " AND M.idTipo=2 AND M.estatus=7\n"
                + "			GROUP BY V.idMovto, MD.idEmpaque) Dev\n"
                + "	  RIGHT JOIN movimientosDetalle Vta ON Vta.idMovto=Dev.idMovto AND Vta.idEmpaque=Dev.idEmpaque\n"
                + "	  WHERE Vta.idMovto=" + toDev.getReferencia() + ") Vta\n"
                + "INNER JOIN movimientosDetalle Dev ON Dev.idEmpaque=Vta.idEmpaque\n"
                + "WHERE Dev.idMovto=" + toDev.getIdMovto();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                detalle.add(this.construirProducto(rs));
            }
        }
        return detalle;
    }

    public double obtenerImpuestosProducto(int idMovto, int idEmpaque, ArrayList<ImpuestosProducto> impuestos) throws SQLException {
        double importeImpuestos = 0;
        try (Connection cn = this.ds.getConnection()) {
            importeImpuestos = movimientos.Movimientos.obtenImpuestosProducto(cn, idMovto, idEmpaque, impuestos);
        }
        return importeImpuestos;
    }

    public ArrayList<TODevolucionProducto> crear(TOMovimientoOficina toDev, int idMovtoAlmacen, int idMoneda) throws SQLException {
        String strSQL;
        ArrayList<TODevolucionProducto> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                toDev.setEstatus(0);
                toDev.setIdUsuario(this.idUsuario);
                toDev.setPropietario(this.idUsuario);

                TOComprobante to = new TOComprobante(toDev.getIdTipo(), toDev.getIdEmpresa(), toDev.getIdReferencia(), idMoneda);
                to.setTipo(1);
                to.setNumero("");
                to.setIdUsuario(this.idUsuario);
                to.setPropietario(0);
                comprobantes.Comprobantes.agregar(cn, to);

                toDev.setIdComprobante(to.getIdComprobante());
                movimientos.Movimientos.agregaMovimientoAlmacen(cn, toDev, false);
                movimientos.Movimientos.agregaMovimientoOficina(cn, toDev, false);

                strSQL = "UPDATE comprobantes SET numero=" + String.valueOf(toDev.getIdMovto()) + "\n"
                        + "WHERE idComprobante=" + toDev.getIdComprobante();
                st.executeUpdate(strSQL);

                strSQL = "INSERT INTO movimientosDetalle\n"
                        + "SELECT " + toDev.getIdMovto() + ", Vta.idEmpaque, 0, 0, Vta.costoPromedio, Vta.unitario*Vta.cantFacturada/(Vta.cantFacturada+Vta.cantSinCargo), 0, 0, 0, Vta.unitario*Vta.cantFacturada/(Vta.cantFacturada+Vta.cantSinCargo), Vta.idImpuestoGrupo, '1900-01-01', 0, 0\n"
                        + "FROM (SELECT V.idMovto, MD.idEmpaque, SUM(MD.cantFacturada+MD.cantSinCargo) AS cantDevuelta\n"
                        + "	FROM movimientosDetalle MD\n"
                        + "	INNER JOIN movimientos M ON M.idMovto=MD.idMovto\n"
                        + "	INNER JOIN movimientos V ON V.idMovto=M.referencia\n"
                        + "	WHERE V.idMovto=" + toDev.getReferencia() + " AND M.idTipo=2 AND M.estatus=7\n"
                        + "	GROUP BY V.idMovto, MD.idEmpaque) Dev\n"
                        + "RIGHT JOIN movimientosDetalle Vta ON Vta.idMovto=Dev.idMovto AND Vta.idEmpaque=Dev.idEmpaque\n"
                        + "WHERE Vta.idMovto=" + toDev.getReferencia() + " AND Vta.cantFacturada+Vta.cantSinCargo > 0 AND Vta.cantFacturada+Vta.cantSinCargo > ISNULL(Dev.cantDevuelta, 0)";
                int n = st.executeUpdate(strSQL);
                if (n == 0) {
                    throw new SQLException("La venta ya ha sido devuelta completamente !!!");
                }
                strSQL = "INSERT INTO movimientosDetalleImpuestos\n"
                        + "SELECT DevD.idMovto, I.idEmpaque, I.idImpuesto, I.impuesto, I.valor, I.aplicable, I.modo, I.acreditable, I.importe, I.acumulable\n"
                        + "FROM movimientosDetalle DevD\n"
                        + "INNER JOIN movimientos Dev ON Dev.idMovto=DevD.idMovto\n"
                        + "INNER JOIN movimientosDetalleImpuestos I ON I.idMovto=Dev.referencia AND I.idEmpaque=DevD.idEmpaque\n"
                        + "WHERE DevD.idMovto=" + toDev.getIdMovto();
                st.executeUpdate(strSQL);

                movimientos.Movimientos.calculaUnitario(cn, toDev.getIdMovto(), 0);

                strSQL = "INSERT INTO movimientosDetalleAlmacen\n"
                        + "SELECT " + toDev.getIdMovtoAlmacen() + ", Vta.idEmpaque, Vta.lote, 0, '1900-01-01', 0\n"
                        + "FROM (SELECT V.idMovtoAlmacen, D.idEmpaque, D.lote, SUM(D.cantidad) AS cantidad\n"
                        + "	  FROM movimientos V\n"
                        + "	  INNER JOIN movimientosDetalleAlmacen D ON D.idMovtoAlmacen=V.idMovtoAlmacen\n"
                        + "	  INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "	  WHERE V.idMovto=" + toDev.getReferencia() + " AND M.idTipo=2 AND M.estatus=7\n"
                        + "	  GROUP BY V.idMovtoAlmacen, D.idEmpaque, D.lote) Dev\n"
                        + "RIGHT JOIN movimientosDetalleAlmacen Vta ON Vta.idMovtoAlmacen=Dev.idMovtoAlmacen AND Vta.idEmpaque=Dev.idEmpaque AND Vta.lote=Dev.lote\n"
                        + "WHERE Vta.idMovtoAlmacen=" + idMovtoAlmacen + " AND Vta.cantidad > ISNULL(Dev.cantidad, 0)";
                st.executeUpdate(strSQL);

                detalle = this.obtenDetalle(cn, toDev);

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
    
    public void liberarVentaOficina(TOPedido toPed) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                Pedidos.liberarPedido(cn, toPed, this.idUsuario);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }
    
    public TOPedido obtenerVentaOficina(int idComprobante) throws SQLException {
        String strSQL = "SELECT " + Pedidos.sqlPedidos() + "\n"
                + "INNER JOIN comprobantes C ON C.idComprobante=M.idComprobante\n"
                + "WHERE C.idComprobante=" + idComprobante;
        TOPedido toPed = new TOPedido();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    toPed = Pedidos.construirPedido(rs);
                }
                movimientos.Movimientos.bloquearMovimientoOficina(cn, toPed, this.idUsuario);
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw (e);
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return toPed;
    }
}
