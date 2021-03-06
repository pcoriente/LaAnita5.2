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

    public static String sqlTraspasoDetalle(TOTraspaso toTraspaso, int idProducto) {
        String condicion = "";
        if (idProducto != 0) {
            condicion = " AND D.idEmpaque=" + idProducto;
        }
        return "SELECT SD.cantSolicitada, ISNULL(SS.cantTraspasada, 0) AS cantTraspasada, M.referencia AS idSolicitud, D.*, E.cod_pro\n"
                + "FROM movimientosDetalle D\n"
                + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                + "INNER JOIN solicitudesDetalle SD ON SD.idSolicitud=M.referencia AND SD.idEmpaque=D.idEmpaque\n"
                + "LEFT JOIN (SELECT S.idSolicitud, D.idEmpaque, SUM(D.cantFacturada) AS cantTraspasada\n"
                + "             FROM movimientos M\n"
                + "             INNER JOIN solicitudes S ON S.idSolicitud=M.referencia\n"
                + "             INNER JOIN movimientosDetalle D ON D.idMovto=M.idMovto\n"
                + "             WHERE S.idSolicitud=" + toTraspaso.getReferencia() + " AND M.idTipo=35 AND M.estatus=7\n"
                + "             GROUP BY S.idSolicitud, D.idEmpaque) SS ON SS.idSolicitud=SD.idSolicitud AND SS.idEmpaque=SD.idEmpaque\n"
                + "INNER JOIN empaques E ON E.idEmpaque=D.idEmpaque\n"
                + "WHERE D.idMovto=" + toTraspaso.getIdMovto() + condicion;
    }

    public static void convertir(TOTraspaso toTraspaso, Traspaso traspaso) {
        traspaso.setIdEnvio(toTraspaso.getIdEnvio());
        traspaso.setEnvioFolio(toTraspaso.getEnvioFolio());
        traspaso.setPedidoFolio(toTraspaso.getPedidoFolio());
        traspaso.setEnvio(toTraspaso.getEnvio() != 0);
        traspaso.setDirecto(toTraspaso.getDirecto() != 0);
        traspaso.setSolicitudFolio(toTraspaso.getSolicitudFolio());
        traspaso.setSolicitudFecha(toTraspaso.getSolicitudFecha());
        traspaso.setSolicitudIdUsuario(toTraspaso.getSolicitudIdUsuario());
        traspaso.setSolicitudEstatus(toTraspaso.getSolicitudEstatus());
        movimientos.Movimientos.convertir(toTraspaso, traspaso);
        traspaso.setIdSolicitud(toTraspaso.getReferencia());
    }

    public static void convertir(Traspaso traspaso, TOTraspaso toTraspaso) {
        toTraspaso.setIdEnvio(traspaso.getIdEnvio());
        toTraspaso.setEnvioFolio(traspaso.getEnvioFolio());
        toTraspaso.setPedidoFolio(traspaso.getPedidoFolio());
        toTraspaso.setEnvio(traspaso.isEnvio() ? 1 : 0);
        toTraspaso.setDirecto(traspaso.isDirecto() ? 1 : 0);
        toTraspaso.setSolicitudFolio(traspaso.getSolicitudFolio());
        toTraspaso.setSolicitudFecha(traspaso.getSolicitudFecha());
        toTraspaso.setSolicitudIdUsuario(traspaso.getSolicitudIdUsuario());
        toTraspaso.setSolicitudProietario(traspaso.getSolicitudProietario());
        toTraspaso.setSolicitudEstatus(traspaso.getSolicitudEstatus());
        movimientos.Movimientos.convertir(traspaso, toTraspaso);
        toTraspaso.setIdReferencia(traspaso.getAlmacenDestino().getIdAlmacen());
        toTraspaso.setReferencia(traspaso.getIdSolicitud());
    }

    public static TOTraspaso convertir(Traspaso traspaso) {
        TOTraspaso toTraspaso = new TOTraspaso();
        convertir(traspaso, toTraspaso);
        return toTraspaso;
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
    
    public static TOTraspaso obtenerTraspaso(Connection cn, int idSolicitud) throws SQLException {
        TOTraspaso toTraspaso = new TOTraspaso();
        String strSQL = "SELECT " + sqlTraspasoYY() + "\n"
                + "WHERE S.idSolicitud=" + idSolicitud;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                toTraspaso = Traspasos.construir(rs);
            }
        }
        return toTraspaso;
    }

    public static void construir(ResultSet rs, TOTraspaso toTraspaso) throws SQLException {
        toTraspaso.setIdEnvio(rs.getInt("idEnvio"));
        toTraspaso.setEnvioFolio(rs.getInt("envioFolio"));
        toTraspaso.setPedidoFolio(rs.getInt("pedidoFolio"));
        toTraspaso.setEnvio(toTraspaso.getIdEnvio() != 0 ? toTraspaso.getPedidoFolio()==0 ? 1 : 0 : 0);
        toTraspaso.setDirecto(toTraspaso.getPedidoFolio()!=0 ? 1 : 0);
        toTraspaso.setSolicitudFolio(rs.getInt("solicitudFolio"));
        toTraspaso.setSolicitudFecha(new java.util.Date(rs.getTimestamp("solicitudFecha").getTime()));
        toTraspaso.setSolicitudIdUsuario(rs.getInt("solicitudIdUsuario"));
        toTraspaso.setSolicitudEstatus(rs.getInt("solicitudEstatus"));
        movimientos.Movimientos.construirMovimientoOficina(rs, toTraspaso);
    }

    public static TOTraspaso construir(ResultSet rs) throws SQLException {
        TOTraspaso toTraspaso = new TOTraspaso();
        construir(rs, toTraspaso);
        return toTraspaso;
    }

    public static String sqlTraspasoYY() {
        return "ISNULL(ES.idEnvio, ISNULL(EP.idEnvio, 0)) AS idEnvio, ISNULL(E1.folio, ISNULL(E2.folio, 0)) AS envioFolio\n"
                + "	, ISNULL(P.folio, 0) AS pedidoFolio\n"
                + "     , " + Traspasos.sqlTraspasoBase() + "\n"
                + "LEFT JOIN enviosSolicitudes ES ON S.idSolicitud=ES.idSolicitud LEFT JOIN envios E1 ON E1.idEnvio=ES.idEnvio\n"
                + "LEFT JOIN enviosPedidos EP ON S.idSolicitud=EP.idSolicitud LEFT JOIN envios E2 ON E2.idEnvio=EP.idEnvio\n"
                + "LEFT JOIN movimientos V ON V.idMovto=EP.idVenta LEFT JOIN pedidos P ON P.idPedido=V.referencia";
    }

    public static String sqlTraspasoBase() {
        return "S.folio AS solicitudFolio, S.fecha AS solicitudFecha, S.idUsuario AS solicitudIdUsuario, S.estatus AS solicitudEstatus, M.*\n"
                + "FROM movimientos M INNER JOIN solicitudes S ON S.idSolicitud=M.referencia";
    }
}
