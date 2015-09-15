package reportesInventarios.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import reportesInventarios.to.TOProductoKardex;
import reportesInventarios.to.TOProductoKardexDetalle;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAORepInventarios {

    int idUsuario, idCedis;
    private DataSource ds = null;

    public DAORepInventarios() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
        this.idUsuario = usuarioSesion.getUsuario().getId();
        this.idCedis = usuarioSesion.getUsuario().getIdCedis();

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
    }

    public TOProductoKardex obtenerProductoKardexAlmacen(int idAlmacen, int idEmpaque) throws SQLException {
        String strSQL;
        TOProductoKardex producto = null;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                strSQL = "SELECT idAlmacen, idEmpaque, SUM(existencia) AS existencia\n"
                        + "	, 0 AS separados, 0 AS existenciaMinima, 0 AS existenciaMaxima\n"
                        + "FROM almacenesLotes\n"
                        + "WHERE idAlmacen="+idAlmacen+" AND idEmpaque="+idEmpaque+"\n"
                        + "GROUP BY idAlmacen, idEmpaque";
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    producto = this.construirProductoKardex(rs);
                } else {
                    producto = new TOProductoKardex();
                    producto.setIdAlmacen(idAlmacen);
                    producto.setIdEmpaque(idEmpaque);
                }
            }
        }
        return producto;
    }

    public ArrayList<TOProductoKardexDetalle> obtenerDetalleKardexAlmacen(int idAlmacen, int idEmpaque, Date fecIni, Date fecFin) throws SQLException {
        String strSQL = "";
        ArrayList<TOProductoKardexDetalle> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                strSQL = "SELECT D.fecha, T.tipo, M.folio\n"
                        + "		, CASE WHEN M.idComprobante=0 THEN '' ELSE CASE C.tipo WHEN 1 THEN 'Interno ' WHEN 2 THEN 'Remision ' ELSE 'Factura ' END END\n"
                        + "		+' '+CASE WHEN M.idComprobante=0 THEN '' WHEN C.serie='' THEN C.numero ELSE C.serie+'-'+C.numero END AS comprobante\n"
                        + "		, D.lote, D.existenciaAnterior, CASE WHEN T.suma=1 THEN '+' ELSE '-' END AS operacion, D.cantidad\n"
                        + "		, D.existenciaAnterior+CASE WHEN T.suma=1 THEN 1 ELSE -1 END*D.cantidad AS saldo\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN movimientosTipos T ON T.idTipo=M.idTipo\n"
                        + "LEFT JOIN comprobantes C ON C.idComprobante=M.idComprobante\n"
                        + "WHERE M.idAlmacen=" + idAlmacen + " AND M.estatus!=0 AND D.idEmpaque=" + idEmpaque + " AND CONVERT(date, D.fecha) BETWEEN '" + new java.sql.Date(fecIni.getTime()) + "' AND '" + new java.sql.Date(fecFin.getTime()) + "'\n"
                        + "ORDER BY D.lote, D.fecha";
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    detalle.add(this.construirProductoKardexDetalle(rs));
                }
            }
        }
        return detalle;
    }

    private TOProductoKardex construirProductoKardex(ResultSet rs) throws SQLException {
        TOProductoKardex toProd = new TOProductoKardex();
        toProd.setIdAlmacen(rs.getInt("idAlmacen"));
        toProd.setIdEmpaque(rs.getInt("idEmpaque"));
        toProd.setExistencia(rs.getDouble("existencia"));
        toProd.setSeparados(rs.getDouble("separados"));
        toProd.setMinimo(rs.getDouble("existenciaMinima"));
        toProd.setMaximo(rs.getDouble("existenciaMaxima"));
        return toProd;
    }

    public TOProductoKardex obtenerProductoKardexOficina(int idAlmacen, int idEmpaque) throws SQLException {
        String strSQL;
        TOProductoKardex producto = null;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                strSQL = "SELECT * FROM almacenesEmpaques WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idEmpaque;
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    producto = this.construirProductoKardex(rs);
                } else {
                    producto = new TOProductoKardex();
                    producto.setIdAlmacen(idAlmacen);
                    producto.setIdEmpaque(idEmpaque);
                }
            }
        }
        return producto;
    }

    private TOProductoKardexDetalle construirProductoKardexDetalle(ResultSet rs) throws SQLException {
        TOProductoKardexDetalle toProd = new TOProductoKardexDetalle();
        toProd.setFecha(new java.util.Date(rs.getTimestamp("fecha").getTime()));
        toProd.setTipo(rs.getString("tipo"));
        toProd.setFolio(rs.getInt("folio"));
        toProd.setComprobante(rs.getString("comprobante"));
        toProd.setLote(rs.getString("lote"));
        toProd.setSaldo(rs.getDouble("existenciaAnterior"));
        toProd.setOperacion(rs.getString("operacion"));
        toProd.setCantidad(rs.getDouble("cantidad"));
        return toProd;
    }

    public ArrayList<TOProductoKardexDetalle> obtenerDetalleKardexOficina(int idAlmacen, int idEmpaque, Date fecIni, Date fecFin) throws SQLException {
        String strSQL = "";
        ArrayList<TOProductoKardexDetalle> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                strSQL = "SELECT D.fecha, T.tipo, M.folio\n"
                        + "		, CASE WHEN M.idComprobante=0 THEN '' ELSE CASE C.tipo WHEN 1 THEN 'Interno ' WHEN 2 THEN 'Remision ' ELSE 'Factura ' END END\n"
                        + "		+' '+CASE WHEN M.idComprobante=0 THEN '' WHEN C.serie='' THEN C.numero ELSE C.serie+'-'+C.numero END AS comprobante\n"
                        + "		, '' AS lote, D.existenciaAnterior, CASE WHEN T.suma=1 THEN '+' ELSE '-' END AS operacion, D.cantFacturada+D.cantSinCargo AS cantidad\n"
                        + "		, D.existenciaAnterior+CASE WHEN T.suma=1 THEN 1 ELSE -1 END*(D.cantFacturada+D.cantSinCargo) AS saldo\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN movimientosTipos T ON T.idTipo=M.idTipo\n"
                        + "LEFT JOIN comprobantes C ON C.idComprobante=M.idComprobante\n"
                        + "WHERE M.idAlmacen=" + idAlmacen + " AND M.estatus!=0 AND D.idEmpaque=" + idEmpaque + " AND CONVERT(date, D.fecha) BETWEEN '" + new java.sql.Date(fecIni.getTime()) + "' AND '" + new java.sql.Date(fecFin.getTime()) + "'\n"
                        + "ORDER BY D.fecha";
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    detalle.add(this.construirProductoKardexDetalle(rs));
                }
            }
        }
        return detalle;
    }
}
