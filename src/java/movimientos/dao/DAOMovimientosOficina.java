package movimientos.dao;

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
import movimientos.dominio.MovimientoTipo;
import movimientos.to.TOMovimientoOficina;
import movimientos.to.TOProductoOficina;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOMovimientosOficina {

    int idUsuario, idCedis;
    private DataSource ds = null;

    public DAOMovimientosOficina() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
        this.idUsuario = usuarioSesion.getUsuario().getId();
        this.idCedis = usuarioSesion.getUsuario().getIdCedis();

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
    }

    public void cancelarProducto(int idMovto, int idProducto, boolean suma) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                if (!suma) {
                    strSQL = "UPDATE A\n"
                            + "SET separados=A.separados-(D.cantFacturada+D.cantSinCargo)\n"
                            + "FROM movimientosDetalle D\n"
                            + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                            + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=M.idEmpaque\n"
                            + "WHERE D.idMovto=" + idMovto + " AND D.idEmpaque=" + idProducto;
                    st.executeUpdate(strSQL);
                }
                strSQL = "DELETE FROM movimientosDetalleImpuestos WHERE idMovto=" + idMovto + " AND D.idEmpaque=" + idProducto;
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalle where idMovto=" + idMovto + " AND D.idEmpaque=" + idProducto;
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

    public void cancelarMovimiento(int idMovto, boolean suma) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                if (!suma) {
                    strSQL = "UPDATE A\n"
                            + "SET separados=A.separados-(D.cantFacturada+D.cantSinCargo)\n"
                            + "FROM movimientosDetalle D\n"
                            + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                            + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=M.idEmpaque\n"
                            + "WHERE D.idMovto=" + idMovto;
                    st.executeUpdate(strSQL);
                }
                strSQL = "DELETE FROM movimientosDetalleImpuestos WHERE idMovto=" + idMovto;
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
    
    public ArrayList<TOProductoOficina> obtenerDetalle(int idMovto) throws SQLException {
        ArrayList<TOProductoOficina> productos = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            productos = this.obtenDetalle(cn, idMovto);
        }
        return productos;
    }
    
    private ArrayList<TOProductoOficina> obtenDetalle(Connection cn, int idMovto) throws SQLException {
        ArrayList<TOProductoOficina> productos = new ArrayList<>();
        String strSQL = "SELECT * FROM movimientosDetalle WHERE idMovto=" + idMovto;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                productos.add(movimientos.Movimientos.construirProductoOficina(rs));
            }
        }
        return productos;
    }

    public ArrayList<TOProductoOficina> obtenerDetalle(TOMovimientoOficina toMov) throws SQLException {
        ArrayList<TOProductoOficina> productos = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                productos = this.obtenDetalle(cn, toMov.getIdMovto());
                movimientos.Movimientos.bloquearMovimientoOficina(cn, toMov, this.idUsuario);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return productos;
    }

    public void liberarMovimiento(int idMovto) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                movimientos.Movimientos.liberarMovimientoOficina(cn, idMovto, this.idUsuario);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public ArrayList<TOProductoOficina> grabarDetalle(TOMovimientoOficina toMov, boolean suma) throws SQLException {
        ArrayList<TOProductoOficina> productos = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                toMov.setEstatus(7);
                
                toMov.setFolio(movimientos.Movimientos.obtenMovimientoFolioOficina(cn, toMov.getIdAlmacen(), toMov.getIdTipo()));
                movimientos.Movimientos.grabaMovimientoOficina(cn, toMov);

                movimientos.Movimientos.actualizaDetalleOficina(cn, toMov.getIdMovto(), toMov.getIdTipo(), suma);
                productos = this.obtenDetalle(cn, toMov.getIdMovto());

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return productos;
    }

    public void liberar(int idAlmacen, TOProductoOficina toProd, double separados) throws SQLException {
        String strSQL = "";
        double cantLiberar = separados - toProd.getCantFacturada();
        if (cantLiberar < 0) {
            throw new SQLException("No se puede liberar una cantidad menor que cero !!!");
        }
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "UPDATE almacenesEmpaques\n"
                        + "SET separados=separados-" + cantLiberar + "\n"
                        + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);

                toProd.setCantFacturada(separados - cantLiberar);
                movimientos.Movimientos.grabaProductoCantidad(cn, toProd);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public double separar(int idAlmacen, TOProductoOficina toProd, double separados) throws SQLException {
        double disponibles = 0;
        double cantSeparar = toProd.getCantFacturada() - separados;
        if (cantSeparar < 0) {
            throw new SQLException("No se puede separar una cantidad menor que cero !!!");
        }
        String strSQL = "SELECT existencia-separados AS disponibles\n"
                + "FROM almacenesEmpaques\n"
                + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + toProd.getIdProducto();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    disponibles = rs.getDouble("disponibles");
                } else {
                    throw new SQLException("No se encontró el empaque solicitado !!!");
                }
                if (disponibles <= 0) {
                    throw new SQLException("No hay unidades disponibles del empaque solicitado !!!");
                } else if (disponibles < cantSeparar) {
                    cantSeparar = disponibles;
                }
                strSQL = "UPDATE almacenesEmpaques\n"
                        + "SET separados=separados+" + cantSeparar + "\n"
                        + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);

                toProd.setCantFacturada(separados + cantSeparar);
                movimientos.Movimientos.grabaProductoCantidad(cn, toProd);

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

    public double obtenerImpuestosProducto(int idMovto, int idProducto, ArrayList<ImpuestosProducto> impuestos) throws SQLException {
        double importeImpuestos = 0;
        try (Connection cn = this.ds.getConnection()) {
            importeImpuestos = movimientos.Movimientos.obtenImpuestosProducto(cn, idMovto, idProducto, impuestos);
        }
        return importeImpuestos;
    }

    public void grabarProducto(TOProductoOficina toProd) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                movimientos.Movimientos.grabaProductoCantidad(cn, toProd);
                toProd.setUnitario(movimientos.Movimientos.grabaProductoCambios(cn, toProd));
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void grabarProductoCambios(TOProductoOficina toProd) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                toProd.setUnitario(movimientos.Movimientos.grabaProductoCambios(cn, toProd));
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void grabarProductoCantidad(TOProductoOficina toProd) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            movimientos.Movimientos.grabaProductoCantidad(cn, toProd);
        }
    }

    public ArrayList<ImpuestosProducto> agregarProducto(TOProductoOficina toProd, int idImpuestosZona) throws SQLException {
        ArrayList<ImpuestosProducto> impuestos = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                movimientos.Movimientos.agregaProductoOficina(cn, toProd, idImpuestosZona);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return impuestos;
    }

    public ArrayList<TOMovimientoOficina> obtenerMovimientos(int idAlmacen, int idTipo, int estatus, Date fechaInicial) throws SQLException {
        if (fechaInicial == null) {
            fechaInicial = new Date();
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TOMovimientoOficina> tos = new ArrayList<>();
        String strSQL = "SELECT M.*\n"
                + "FROM movimientos M\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=" + idTipo;
        if (estatus == 0) {
            strSQL += " AND M.estatus=0\n";
        } else if (estatus == 7) {
            strSQL += " AND M.estatus>=7\n"
                    + "         AND CONVERT(date, M.fecha) >= '" + format.format(fechaInicial) + "'\n";
        }
        strSQL += "ORDER BY M.fecha DESC";

        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                tos.add(movimientos.Movimientos.construirMovimientoOficina(rs));
            }
        } finally {
            cn.close();
        }
        return tos;
    }

    public ArrayList<TOMovimientoOficina> obtenerMovimientos(int idAlmacen, int idTipo, int idComprobante) throws SQLException {
        ArrayList<TOMovimientoOficina> tos = new ArrayList<>();
        String strSQL = "SELECT * FROM movimientos M\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=" + idTipo + " AND M.idComprobante=" + idComprobante;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    tos.add(movimientos.Movimientos.construirMovimientoOficina(rs));
                }
            }
        }
        return tos;
    }

    public int agregarMovimiento(TOMovimientoOficina toMov, boolean definitivo) throws SQLException {
        int idMovto = 0;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                toMov.setEstatus(0);
                toMov.setIdUsuario(this.idUsuario);
                toMov.setPropietario(this.idUsuario);
                movimientos.Movimientos.agregaMovimientoOficina(cn, toMov, definitivo);
                idMovto = toMov.getIdMovto();
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

    public ArrayList<MovimientoTipo> obtenerMovimientosTipos(boolean suma) throws SQLException {
        ArrayList<MovimientoTipo> tipos = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            tipos = movimientos.Movimientos.obtenMovimientosTipos(cn, suma);
        }
        return tipos;
    }
}
