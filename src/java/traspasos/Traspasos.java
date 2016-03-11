package traspasos;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import producto2.dominio.Producto;
import traspasos.dominio.Traspaso;
import traspasos.dominio.TraspasoProducto;
import traspasos.to.TOTraspaso;
import traspasos.to.TOTraspasoProducto;

/**
 *
 * @author jesc
 */
public class Traspasos {

    public static void liberarTraspaso(Connection cn, TOTraspaso toTraspaso, int idUsuario) throws SQLException {
        movimientos.Movimientos.liberarMovimientoAlmacen(cn, toTraspaso.getIdMovtoAlmacen(), idUsuario);
        movimientos.Movimientos.liberarMovimientoOficina(cn, toTraspaso.getIdMovto(), idUsuario);
        toTraspaso.setPropietario(0);
    }

    public static void convertir(TraspasoProducto prod, TOTraspasoProducto toProd) {
        toProd.setCantSolicitada(prod.getCantSolicitada());
        toProd.setCantTraspasada(prod.getCantTraspasada());
        movimientos.Movimientos.convertir(prod, toProd);
    }

    public static TOTraspasoProducto convertir(TraspasoProducto prod) {
        TOTraspasoProducto toProd = new TOTraspasoProducto();
        convertir(prod, toProd);
        return toProd;
    }

    public static void convertir(TOTraspasoProducto toProd, TraspasoProducto prod, Producto prod2) {
        prod.setCantSolicitada(toProd.getCantSolicitada());
        prod.setCantTraspasada(toProd.getCantTraspasada());
        movimientos.Movimientos.convertir(toProd, prod, prod2);
    }

    public static TraspasoProducto convertir(TOTraspasoProducto toProd, Producto prod2) throws SQLException {
        TraspasoProducto prod = new TraspasoProducto();
        convertir(toProd, prod, prod2);
        return prod;
    }

    public static void convertir(TOTraspaso toTraspaso, Traspaso traspaso) {
        traspaso.setSolicitudFolio(toTraspaso.getSolicitudFolio());
        traspaso.setSolicitudFecha(toTraspaso.getSolicitudFecha());
        traspaso.setSolicitudIdUsuario(toTraspaso.getSolicitudIdUsuario());
        traspaso.setSolicitudEstatus(toTraspaso.getSolicitudEstatus());
        movimientos.Movimientos.convertir(toTraspaso, traspaso);
        traspaso.setIdSolicitud(toTraspaso.getReferencia());
    }

    public static TOTraspaso convertir(Traspaso traspaso) {
        TOTraspaso to = new TOTraspaso();
        to.setSolicitudFolio(traspaso.getSolicitudFolio());
        to.setSolicitudFecha(traspaso.getSolicitudFecha());
        to.setSolicitudIdUsuario(traspaso.getSolicitudIdUsuario());
        to.setSolicitudProietario(traspaso.getSolicitudProietario());
        to.setSolicitudEstatus(traspaso.getSolicitudEstatus());
        movimientos.Movimientos.convertir(traspaso, to);
        to.setIdReferencia(traspaso.getAlmacenDestino().getIdAlmacen());
        to.setReferencia(traspaso.getIdSolicitud());
        return to;
    }

    public static void construir(TOTraspasoProducto toProd, ResultSet rs) throws SQLException {
        toProd.setCantSolicitada(rs.getDouble("cantSolicitada"));
        toProd.setCantTraspasada(rs.getDouble("cantTraspasada"));
        movimientos.Movimientos.construirProductoOficina(rs, toProd);
    }

    public static TOTraspasoProducto construirProducto(ResultSet rs) throws SQLException {
        TOTraspasoProducto toProd = new TOTraspasoProducto();
        construir(toProd, rs);
        return toProd;
    }

    public static TOTraspaso construir(ResultSet rs) throws SQLException {
        TOTraspaso toTraspaso = new TOTraspaso();
        toTraspaso.setSolicitudFolio(rs.getInt("solicitudFolio"));
        toTraspaso.setSolicitudFecha(new java.util.Date(rs.getTimestamp("solicitudFecha").getTime()));
        toTraspaso.setSolicitudIdUsuario(rs.getInt("solicitudIdUsuario"));
        toTraspaso.setSolicitudEstatus(rs.getInt("solicitudEstatus"));
        movimientos.Movimientos.construirMovimientoOficina(rs, toTraspaso);
        return toTraspaso;
    }

    public static String sqlTraspaso() {
        String strSQL = "SELECT S.folio AS solicitudFolio, S.fecha AS solicitudFecha, S.idUsuario AS solicitudIdUsuario\n"
                + "     , S.estatus AS solicitudEstatus, M.*\n"
                + "     , ISNULL(ES.diasInventario, 0) AS diasInventario, ISNULL(ES.fechaProduccion, '1900-01-01') AS fechaProduccion\n"
                + "FROM movimientos M\n"
                + "INNER JOIN solicitudes S ON S.idSolicitud=M.referencia\n"
                + "LEFT JOIN enviosSolicitudes ES ON ES.idSolicitud=S.idSolicitud";
        return strSQL;
    }

    public static TOTraspaso obtenerTraspaso(Connection cn, int idSolicitud) throws SQLException {
        TOTraspaso toTraspaso = new TOTraspaso();
        String strSQL = sqlTraspaso() + "\n"
                + "WHERE S.idSolicitud=" + idSolicitud;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                toTraspaso = construir(rs);
            }
        }
        return toTraspaso;
    }
}
