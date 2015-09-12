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
import movimientos.to.TOMovimientoAlmacen;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOMovimientosAlmacen {
    int idUsuario, idCedis;
    private DataSource ds = null;

    public DAOMovimientosAlmacen() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
        this.idUsuario = usuarioSesion.getUsuario().getId();
        this.idCedis = usuarioSesion.getUsuario().getIdCedis();

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
    }
    
    public ArrayList<TOMovimientoAlmacen> obtenerMovimientosAlmacen(int idAlmacen, int idTipo, int idComprobante) throws SQLException {
        ArrayList<TOMovimientoAlmacen> tos = new ArrayList<>();
        String strSQL = "SELECT * FROM movimientosAlmacen M\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=" + idTipo + " AND M.idComprobante=" + idComprobante;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    tos.add(movimientos.Movimientos.construirMovimientoAlmacen(rs));
                }
            }
        }
        return tos;
    }
    
    public int agregarMovimiento(TOMovimientoAlmacen to, boolean definitivo) throws SQLException {
        int idMovto = 0;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                to.setEstatus(0);
                to.setIdUsuario(this.idUsuario);
                to.setPropietario(this.idUsuario);
                movimientos.Movimientos.agregaMovimientoAlmacen(cn, to, definitivo);
                idMovto = to.getIdMovtoAlmacen();
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
