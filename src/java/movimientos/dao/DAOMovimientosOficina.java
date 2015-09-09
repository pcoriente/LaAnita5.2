package movimientos.dao;

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

    public ArrayList<TOMovimientoOficina> obtenerMovimientosOficina(int idAlmacen, int idTipo, int idComprobante) throws SQLException {
        ArrayList<TOMovimientoOficina> tos = new ArrayList<>();
        String strSQL = "SELECT * FROM movimientos M\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=" + idTipo + " AND M.idComprobante=" + idComprobante;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    tos.add(movimientos.Movimientos.construirMovimiento(rs));
                }
            }
        }
        return tos;
    }

    public int agregarMovimiento(TOMovimientoOficina to, boolean definitivo) throws SQLException {
        int idMovto = 0;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                to.setEstatus(0);
                to.setIdUsuario(this.idUsuario);
                to.setPropietario(this.idUsuario);
                movimientos.Movimientos.agregaMovimientoOficina(cn, to, definitivo);
                idMovto = to.getIdMovto();
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
}
