package mvEntradas.dao;

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
import movimientos.to.TOMovimientoOficina;
import movimientos.to.TOMovimientoProductoAlmacen;
import movimientos.to.TOProductoOficina;
import mvEntradas.to.TOEntradaProduccionProducto;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOEntradasProduccion {

    int idUsuario, idCedis;
    private DataSource ds = null;

    public DAOEntradasProduccion() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
        this.idUsuario = usuarioSesion.getUsuario().getId();
        this.idCedis = usuarioSesion.getUsuario().getIdCedis();

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
    }

    public void liberarEntrada(int idMovto, int idMovtoAlmacen) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                movimientos.Movimientos.liberarMovimientoOficina(cn, idMovto, this.idUsuario);
                movimientos.Movimientos.liberarMovimientoAlmacen(cn, idMovtoAlmacen, this.idUsuario);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public ArrayList<TOProductoOficina> grabar(TOMovimientoOficina toMov) throws SQLException {
        ArrayList<TOProductoOficina> productos = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                toMov.setEstatus(7);

                toMov.setFolio(movimientos.Movimientos.obtenMovimientoFolioAlmacen(cn, toMov.getIdAlmacen(), toMov.getIdTipo()));
                movimientos.Movimientos.grabaMovimientoAlmacen(cn, toMov);

                movimientos.Movimientos.actualizaDetalleAlmacen(cn, toMov.getIdMovtoAlmacen(), true);

                toMov.setFolio(movimientos.Movimientos.obtenMovimientoFolioOficina(cn, toMov.getIdAlmacen(), toMov.getIdTipo()));
                movimientos.Movimientos.grabaMovimientoOficina(cn, toMov);

                movimientos.Movimientos.actualizaDetalleOficina(cn, toMov.getIdMovto(), toMov.getIdTipo(), true);
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

    public void cancelarMovimiento(int idMovto, int idMovtoAlmacen) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "DELETE FROM movimientosDetalleAlmacen where idMovtoAlmacen=" + idMovtoAlmacen;
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosAlmacen WHERE idMovtoAlmacen=" + idMovtoAlmacen;
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

    public void eliminarProducto(int idMovto, int idMovtoAlmacen, int idEmpaque) throws SQLException {
        String strSQL = "DELETE FROM movimientosDetalleAlmacen\n"
                + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idEmpaque;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalle\n"
                        + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + idEmpaque;
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

    public void grabarProductoCantidad(TOProductoOficina toProd, TOMovimientoProductoAlmacen toLote) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                movimientos.Movimientos.grabaProductoAlmacen(cn, toLote);

                toProd.setCantFacturada(toProd.getCantFacturada() - toLote.getSeparados() + toLote.getCantidad());
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

    public ArrayList<TOMovimientoProductoAlmacen> obtenerProductoDetalle(int idMovtoAlmacen, int idProducto) throws SQLException {
        ArrayList<TOMovimientoProductoAlmacen> productos = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            productos = movimientos.Movimientos.obtenerDetalleProducto(cn, idMovtoAlmacen, idProducto);
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

    public void agregarProducto(TOProductoOficina toProd) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                movimientos.Movimientos.agregaProductoOficina(cn, toProd, 0);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public ArrayList<TOMovimientoOficina> obtenerEntradas(int idAlmacen, int idTipo, int estatus, Date fechaInicial) throws SQLException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TOMovimientoOficina> entradas = new ArrayList<>();
        String strSQL = "SELECT M.*\n"
                + "FROM movimientos M\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=" + idTipo + " AND M.estatus=" + estatus + "\n";
        if (estatus == 7) {
            strSQL += "         AND CONVERT(date, M.fecha) >= '" + format.format(fechaInicial) + "'\n";
        }
        strSQL += "ORDER BY M.fecha DESC";

        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                entradas.add(movimientos.Movimientos.construirMovimientoOficina(rs));
            }
        } finally {
            cn.close();
        }
        return entradas;
    }

    public void crearEntrada(TOMovimientoOficina toMov) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                toMov.setIdUsuario(this.idUsuario);
                toMov.setPropietario(this.idUsuario);
                toMov.setEstatus(0);

                movimientos.Movimientos.agregaMovimientoAlmacen(cn, toMov, false);
                movimientos.Movimientos.agregaMovimientoOficina(cn, toMov, false);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }
}
