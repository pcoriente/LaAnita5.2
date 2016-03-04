package pedidos;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import pedidos.dominio.Pedido;
import pedidos.dominio.PedidoProducto;
import pedidos.to.TOPedido;
import pedidos.to.TOProductoPedido;

/**
 *
 * @author jesc
 */
public class Pedidos {

    public static void liberarPedido(Connection cn, TOPedido toPedido, int idUsuario) throws SQLException {
        movimientos.Movimientos.liberarMovimientoOficina(cn, toPedido.getIdMovto(), idUsuario);
        toPedido.setPropietario(0);
    }

    public static TOProductoPedido convertir(PedidoProducto p) {
        TOProductoPedido to = new TOProductoPedido();
        to.setIdPedido(p.getIdPedido());
        to.setIdMovto(p.getIdMovto());
        to.setIdProducto(p.getProducto().getIdProducto());
        to.setCantOrdenada(p.getCantOrdenada());
        to.setCantOrdenadaSinCargo(p.getCantOrdenadaSinCargo());
//        to.setSimilar(p.isSimilar());
        to.setPiezas(p.getProducto().getPiezas());
        to.setCantFacturada(p.getCantFacturada());
        to.setCantSinCargo(p.getCantSinCargo());
        to.setCosto(p.getCosto());
        to.setDesctoProducto1(p.getDesctoProducto1());
        to.setUnitario(p.getUnitario());
        to.setIdImpuestoGrupo(p.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());
        return to;
    }

    public static void convertir(TOProductoPedido toProd, PedidoProducto prod) throws SQLException {
        prod.setIdEnvio(toProd.getIdEnvio());
        prod.setCantEnviada(toProd.getCantEnviada());
        prod.setCantEnviada2(toProd.getCantEnviada());
        prod.setIdPedido(toProd.getIdPedido());
        prod.setCantOrdenada(toProd.getCantOrdenada());
        prod.setCantOrdenadaSinCargo(toProd.getCantOrdenadaSinCargo());
        movimientos.Movimientos.convertir(toProd, prod);
        prod.setCantFacturada(prod.getCantOrdenada());
        prod.setCantSinCargo(prod.getCantOrdenadaSinCargo());
        prod.setCajas(prod.getCantOrdenada() / prod.getProducto().getPiezas());
        prod.setCajasSinCargo(prod.getCantOrdenadaSinCargo() / prod.getProducto().getPiezas());
    }

    public static TOProductoPedido construirProducto(ResultSet rs) throws SQLException {
        TOProductoPedido to = new TOProductoPedido();
        to.setIdEnvio(rs.getInt("idEnvio"));
        to.setCantEnviada(rs.getDouble("cantEnviada"));
        to.setIdPedido(rs.getInt("idPedido"));
        to.setCantOrdenada(rs.getDouble("cantOrdenada"));
        to.setCantOrdenadaSinCargo(rs.getDouble("cantOrdenadaSinCargo"));
        to.setPiezas(rs.getInt("piezas"));
        movimientos.Movimientos.construirProductoOficina(rs, to);
        return to;
    }

    public static void convertirPedido(Pedido ped, TOPedido toPed) {
        toPed.setIdPedidoOC(ped.getIdPedidoOC());
        toPed.setPedidoFolio(ped.getPedidoFolio());
        toPed.setPedidoFecha(ped.getPedidoFecha());
        toPed.setDiasCredito(ped.getDiasCredito());
        toPed.setEspecial(ped.isEspecial() ? 1 : 0);
        toPed.setPedidoIdUsuario(ped.getPedidoIdUsuario());
        toPed.setCanceladoMotivo(ped.getCanceladoMotivo());
        toPed.setDirecto(ped.isDirecto() ? 1 : 0);
        toPed.setPeso(ped.getPeso());
        toPed.setIdEnvio(ped.getIdEnvio());
        toPed.setOrden(ped.getOrden());
        toPed.setEnvioEstatus(ped.getEnvioEstatus());
        toPed.setPedidoEstatus(ped.getPedidoEstatus());
        toPed.setElectronico(ped.getElectronico());
        toPed.setOrdenDeCompra(ped.getOrdenDeCompra());
        toPed.setOrdenDeCompraFecha(ped.getOrdenDeCompraFecha());
        movimientos.Movimientos.convertir(ped, toPed);
        toPed.setIdComprobante(ped.getComprobante().getIdComprobante());
        toPed.setIdImpuestoZona(ped.getTienda().getIdImpuestoZona());
        toPed.setIdReferencia(ped.getTienda().getIdTienda());
        toPed.setReferencia(ped.getIdPedido());
    }

    public static void convertirpedido(TOPedido toPed, Pedido ped) {
        ped.setIdPedidoOC(toPed.getIdPedidoOC());
        ped.setPedidoFolio(toPed.getPedidoFolio());
        ped.setPedidoFecha(toPed.getPedidoFecha());
        ped.setDiasCredito(toPed.getDiasCredito());
        ped.setEspecial(toPed.getEspecial() != 0 ? true : false);
        ped.setPedidoIdUsuario(toPed.getPedidoIdUsuario());
        ped.setCanceladoMotivo(toPed.getCanceladoMotivo());
        ped.setDirecto(toPed.getDirecto() != 0 ? true : false);
        ped.setPeso(toPed.getPeso());
        ped.setIdEnvio(toPed.getIdEnvio());
        ped.setOrden(toPed.getOrden());
        ped.setEnvioEstatus(toPed.getEnvioEstatus());
        ped.setPedidoEstatus(toPed.getPedidoEstatus());
        ped.setElectronico(toPed.getElectronico());
        ped.setOrdenDeCompra(toPed.getOrdenDeCompra());
        ped.setOrdenDeCompraFecha(toPed.getOrdenDeCompraFecha());
        movimientos.Movimientos.convertir(toPed, ped);
        ped.setIdPedido(toPed.getReferencia());
    }

//    public static ArrayList<TOPedido> obtenerFincados(Connection cn, int idAlmacen) throws SQLException {
//        ArrayList<TOPedido> pedidos = new ArrayList<>();
//        String strSQL = Pedidos.sqlPedido(idAlmacen) + "\n"
//                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=28 AND P.estatus IN (1, 3)\n"
//                + "ORDER BY P.fecha";
//        try (Statement st = cn.createStatement()) {
//            ResultSet rs = st.executeQuery(strSQL);
//            while (rs.next()) {
//                pedidos.add(this.construirPedido(rs));
//            }
//        }
//        return pedidos;
//    }
//
    public static TOPedido construirPedido(ResultSet rs) throws SQLException {
        TOPedido toPed = new TOPedido();
        construyePedido(toPed, rs);
        return toPed;
    }

    public static void construyePedido(TOPedido to, ResultSet rs) throws SQLException {
        to.setIdPedidoOC(rs.getInt("idPedidoOC"));
        to.setPedidoFolio(rs.getInt("pedidoFolio"));
        to.setPedidoFecha(new java.util.Date(rs.getTimestamp("pedidoFecha").getTime()));
        to.setDiasCredito(rs.getInt("diasCredito"));
        to.setEspecial(rs.getInt("especial"));
        to.setPedidoIdUsuario(rs.getInt("pedidoIdUsuario"));
        to.setCanceladoMotivo(rs.getString("canceladoMotivo"));
        to.setPedidoEstatus(rs.getInt("pedidoEstatus"));
        to.setElectronico(rs.getString("electronico"));
        to.setOrdenDeCompra(rs.getString("ordenDeCompra"));
        to.setOrdenDeCompraFecha(new java.util.Date(rs.getTimestamp("ordenDeCompraFecha").getTime()));
        to.setDirecto(rs.getInt("directo"));
        to.setIdEnvio(rs.getInt("idEnvio"));
        to.setPeso(rs.getDouble("peso"));
        to.setOrden(rs.getInt("orden"));
        to.setEnvioEstatus(rs.getInt("envioEstatus"));
        movimientos.Movimientos.construirMovimientoOficina(rs, to);
    }

    public static String sqlPedido(int idAlmacen) {
        return "SELECT M.*, P.idPedidoOC, P.folio AS pedidoFolio, P.fecha AS pedidoFecha, P.diasCredito, P.especial\n"
                + "     , P.idUsuario AS pedidoIdUsuario, P.canceladoMotivo, P.estatus AS pedidoEstatus\n"
                + "     , ISNULL(OC.electronico, '') AS electronico, ISNULL(OC.ordenDeCompra, '') AS ordenDeCompra\n"
                + "     , ISNULL(OC.ordenDeCompraFecha, '1900-01-01') AS ordenDeCompraFecha, ISNULL(EP.peso, 0) AS peso\n"
                + "     , ISNULL(EP.directo, 0) AS directo, ISNULL(EP.idEnvio, 0) AS idEnvio, ISNULL(EP.orden, 0) AS orden\n"
                + "     , ISNULL(E.estatus, 0) AS envioEstatus\n"
                + "FROM (SELECT P.idPedido, MIN(M.idMovto) AS idPrincipal\n"
                + "		FROM movimientos M\n"
                + "		INNER JOIN pedidos P ON P.idPedido=M.referencia\n"
                + "		WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=28 AND P.estatus=7\n"
                + "		GROUP BY P.idPedido) ID\n"
                + "INNER JOIN movimientos M ON M.idMovto=ID.idPrincipal\n"
                + "INNER JOIN pedidos P ON P.idPedido=M.referencia\n"
                + "LEFT JOIN pedidosOC OC ON OC.idPedidoOC=P.idPedidoOC\n"
                + "LEFT JOIN enviosPedidos EP ON EP.idPedido=P.idPedido\n"
                + "LEFT JOIN envios E ON E.idEnvio=EP.idEnvio";
    }
}
