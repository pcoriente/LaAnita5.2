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
import movimientos.to.TOMovimientoProductoAlmacen;
import movimientos.to.TOProductoOficina;
import mvEntradas.to.TOEntradaProduccion;
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

    public ArrayList<TOProductoOficina> grabar(TOEntradaProduccion toMov) throws SQLException {
        String strSQL;
        ArrayList<TOProductoOficina> productos = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                toMov.setEstatus(7);

                toMov.setFolio(movimientos.Movimientos.obtenMovimientoFolioAlmacen(cn, toMov.getIdAlmacen(), toMov.getIdTipo()));
                movimientos.Movimientos.grabaMovimientoAlmacen(cn, toMov);
                
                strSQL="UPDATE D\n"
                        + "SET lote=CONCAT(lote, E.sufijo)\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN empaques E ON E.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovtoAlmacen=" + toMov.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

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

    public void eliminar(int idMovto, int idMovtoAlmacen) throws SQLException {
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
                
                strSQL = "DELETE FROM entradasProduccion WHERE idMovto=" + idMovto;
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

    public ArrayList<TOProductoOficina> obtenerDetalle(TOEntradaProduccion toMov) throws SQLException {
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
    
    private TOEntradaProduccion construir(ResultSet rs) throws SQLException {
        TOEntradaProduccion toEnt = new TOEntradaProduccion();
        toEnt.setFechaReporte(new java.util.Date(rs.getDate("fechaReporte").getTime()));
        movimientos.Movimientos.construirMovimientoOficina(rs, toEnt);
        return toEnt;
    }

    public ArrayList<TOEntradaProduccion> obtenerEntradas(int idAlmacen, int estatus, Date fechaInicial) throws SQLException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TOEntradaProduccion> entradas = new ArrayList<>();
        String strSQL = "SELECT M.*, P.fechaReporte\n"
                + "FROM movimientos M\n"
                + "INNER JOIN entradasProduccion P ON P.idMovto=M.idMovto\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo IN (3,18) AND M.estatus=" + estatus + "\n";
        if (estatus == 7) {
            strSQL += "         AND CONVERT(date, M.fecha) >= '" + format.format(fechaInicial) + "'\n";
        }
        strSQL += "ORDER BY M.fecha DESC";

        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                entradas.add(construir(rs));
            }
        } finally {
            cn.close();
        }
        return entradas;
    }

    public void crearEntrada(TOEntradaProduccion toEnt) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                toEnt.setIdUsuario(this.idUsuario);
                toEnt.setPropietario(this.idUsuario);
                toEnt.setEstatus(0);

                movimientos.Movimientos.agregaMovimientoAlmacen(cn, toEnt, false);
                movimientos.Movimientos.agregaMovimientoOficina(cn, toEnt, false);
                
                java.sql.Date fechaReporte = new java.sql.Date(toEnt.getFechaReporte().getTime());
                strSQL="INSERT INTO entradasProduccion (idMovto, fechaReporte)\n"
                        + "VALUES ("+toEnt.getIdMovto()+", '"+fechaReporte.toString()+"')";
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
}
