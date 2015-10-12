package rechazos.dao;

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
import rechazos.to.TORechazo;
import rechazos.to.TORechazoProducto;
import rechazos.to.TORechazoProductoAlmacen;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAORechazos {

    int idUsuario, idCedis;
    private DataSource ds = null;

    public DAORechazos() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
        this.idUsuario = usuarioSesion.getUsuario().getId();
        this.idCedis = usuarioSesion.getUsuario().getIdCedis();

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
    }

    public TORechazoProductoAlmacen construirProductoAlmacen(ResultSet rs) throws SQLException {
        TORechazoProductoAlmacen toProd = new TORechazoProductoAlmacen();
        toProd.setCantTraspasada(rs.getDouble("cantTraspasada"));
        toProd.setCantRecibida(rs.getDouble("cantRecibida"));
        movimientos.Movimientos.construirProductoAlmacen(rs, toProd);
        return toProd;
    }

    public ArrayList<TORechazoProductoAlmacen> obtenerDetalleProducto(int idMovtoAlmacen, int idProducto) throws SQLException {
        String strSQL = "SELECT DT.cantidad AS cantTraspasada, DR.cantidad AS cantRecibida, D.*, A.fechaCaducidad\n"
                + "FROM movimientosDetalleAlmacen D\n"
                + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                + "INNER JOIN movimientosAlmacen R ON R.idMovtoAlmacen=M.referencia\n"
                + "LEFT JOIN movimientosDetalleAlmacen DR ON DR.idMovtoAlmacen=R.idMovtoAlmacen AND DR.idEmpaque=D.idEmpaque AND DR.lote=D.lote\n"
                + "INNER JOIN movimientosAlmacen T ON T.idMovtoAlmacen=R.referencia\n"
                + "INNER JOIN movimientosDetalleAlmacen DT ON DT.idMovtoAlmacen=T.idMovtoAlmacen AND DT.idEmpaque=D.idEmpaque AND DT.lote=D.lote\n"
                + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen + " AND D.idEmpaque=" + idProducto;
        ArrayList<TORechazoProductoAlmacen> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    detalle.add(this.construirProductoAlmacen(rs));
                }
            }
        }
        return detalle;
    }

    private TORechazoProducto construirProducto(ResultSet rs) throws SQLException {
        TORechazoProducto toProd = new TORechazoProducto();
        toProd.setCantTraspasada(rs.getDouble("cantTraspasada"));
        toProd.setCantRecibida(rs.getDouble("cantRecibida"));
        movimientos.Movimientos.construirProductoOficina(rs, toProd);
        return toProd;
    }

    public ArrayList<TORechazoProducto> obtenerDetalle(int idMovto) throws SQLException {
        ArrayList<TORechazoProducto> detalle = new ArrayList<>();
        String strSQL = "SELECT ISNULL(DR.cantFacturada, 0) AS cantRecibida, DT.cantFacturada AS cantTraspasada, D.*\n"
                + "FROM movimientosDetalle D\n"
                + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                + "INNER JOIN movimientos R ON R.idMovto=M.referencia\n"
                + "LEFT JOIN movimientosDetalle DR ON DR.idMovto=R.idMovto AND DR.idEmpaque=D.idEmpaque\n"
                + "INNER JOIN movimientos T ON T.idMovto=R.referencia\n"
                + "INNER JOIN movimientosDetalle DT ON DT.idMovto=T.idMovto AND DT.idEmpaque=D.idEmpaque\n"
                + "WHERE D.idMovto=" + idMovto;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    detalle.add(this.construirProducto(rs));
                }
            }
        }
        return detalle;
    }

    private TORechazo construir(ResultSet rs) throws SQLException {
        TORechazo toMov = new TORechazo();
        toMov.setRecepcionFolio(rs.getInt("recepcionFolio"));
        toMov.setRecepcionFecha(new java.util.Date(rs.getTimestamp("recepcionFecha").getTime()));
        toMov.setTraspasoFolio(rs.getInt("traspasoFolio"));
        toMov.setTraspasoFecha(new java.util.Date(rs.getTimestamp("traspasoFecha").getTime()));
        movimientos.Movimientos.construirMovimientoOficina(rs, toMov);
        return toMov;
    }

    public ArrayList<TORechazo> obtenerRechazos(int idAlmacen, Date fechaInicial) throws SQLException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TORechazo> rechazos = new ArrayList<>();
        String strSQL = "SELECT M.*\n"
                + "     , R.folio AS recepcionFolio, R.fecha AS recepcionFecha\n"
                + "     , T.folio AS traspasoFolio, T.fecha AS traspasoFecha\n"
                + "FROM movimientos M\n"
                + "INNER JOIN movimientos R ON R.idMovto=M.referencia\n"
                + "INNER JOIN movimientos T ON T.idMovto=R.referencia\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=54 AND M.estatus=5\n"
                + "         AND CONVERT(date, M.fecha) <= '" + format.format(fechaInicial) + "'\n"
                + "ORDER BY M.fecha";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    rechazos.add(this.construir(rs));
                }
            }
        }
        return rechazos;
    }
}
