package salidas.dao;

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
import salidas.to.TOSalidaProductoAlmacen;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOSalidasAlmacen {

    int idUsuario, idCedis;
    private DataSource ds = null;

    public DAOSalidasAlmacen() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
        this.idUsuario = usuarioSesion.getUsuario().getId();
        this.idCedis = usuarioSesion.getUsuario().getIdCedis();

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
    }

    public void agregarProducto(TOSalidaProductoAlmacen toProd) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                movimientos.Movimientos.agregaProductoAlmacen(cn, toProd);

                strSQL = "SELECT DATEADD(DAY, 365, L.fecha) AS fechaCaducidad\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN lotes L ON L.lote=SUBSTRING(D.lote, 1, 4)\n"
                        + "WHERE D.idMovtoAlmacen=" + toProd.getIdMovtoAlmacen() + " AND D.idEmpaque=" + toProd.getIdProducto() + " AND D.lote='" + toProd.getLote() + "'";
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    toProd.setFechaCaducidad(new java.util.Date(rs.getDate("fechaCaducidad").getTime()));
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
}
