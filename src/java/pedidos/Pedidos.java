package pedidos;

import comprobantes.to.TOComprobante;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import pedidos.dominio.Pedido;
import pedidos.dominio.PedidoProducto;
import pedidos.to.TOPedido;
import pedidos.to.TOPedidoProducto;

/**
 *
 * @author jesc
 */
public class Pedidos {
    
    public static void agregaProductoPedido(Connection cn, TOPedido toPed, TOPedidoProducto toProd) throws SQLException {
        String strSQL = "INSERT INTO ventasDetalle (idVenta, idEmpaque, cantOrdenada, cantOrdenadaSinCargo, cantSurtida, cantSurtidaSinCargo)\n"
                + "VALUES (" + toProd.getIdVenta() + ", " + toProd.getIdProducto() + ", " + toProd.getCantOrdenada() + ", " + toProd.getCantOrdenadaSinCargo() + ", 0, 0)";
        try (Statement st = cn.createStatement()) {
            movimientos.Movimientos.agregaProductoOficina(cn, toProd, toPed.getIdImpuestoZona());
            movimientos.Movimientos.actualizaProductoPrecio(cn, toPed, toProd, toPed.getOrdenDeCompraFecha());

            st.executeUpdate(strSQL);
        }
    }
    
    public static void agregarVenta(Connection cn, TOPedido toPed, int idMoneda) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            strSQL = "INSERT INTO ventas (fecha, diasCredito, directo, idSolicitud, especial, idUsuario, idPedido)\n"
                    + "VALUES (GETDATE(), "+toPed.getDiasCredito()+", " + toPed.getDirecto() + ", " + toPed.getIdSolicitud() + ", " + toPed.getEspecial() + ", "+toPed.getPedidoIdUsuario()+", "+toPed.getIdPedido()+")";
            st.executeUpdate(strSQL);
            ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idVenta");
            if (rs.next()) {
                toPed.setReferencia(rs.getInt("idVenta"));
            }
            TOComprobante toComprobante = new TOComprobante();
            toComprobante.setIdTipoMovto(toPed.getIdTipo());
            toComprobante.setIdEmpresa(toPed.getIdEmpresa());
            toComprobante.setIdReferencia(toPed.getIdReferencia());
            toComprobante.setTipo(1);
            toComprobante.setSerie("");
            toComprobante.setNumero("");
            toComprobante.setFechaFactura(toPed.getFecha());
            toComprobante.setIdMoneda(idMoneda);
            toComprobante.setIdUsuario(toPed.getIdUsuario());
            toComprobante.setPropietario(0);
            toComprobante.setEstatus(5);
            toComprobante.setCerradoAlmacen(false);
            toComprobante.setCerradoOficina(false);
            comprobantes.Comprobantes.agregar(cn, toComprobante);

            toPed.setEstatus(0);
            toPed.setIdComprobante(toComprobante.getIdComprobante());
            movimientos.Movimientos.agregaMovimientoAlmacen(cn, toPed, false);
            movimientos.Movimientos.agregaMovimientoOficina(cn, toPed, false);

            strSQL = "UPDATE comprobantes SET numero=" + String.valueOf(toPed.getIdMovto()) + " WHERE idComprobante=" + toPed.getIdComprobante();
            st.executeUpdate(strSQL);
        }
    }

    public static void agregarPedido(Connection cn, TOPedido toPed, int idMoneda) throws SQLException {
        String strSQL, fechaOrden = "";
        try (Statement st = cn.createStatement()) {
            if (!toPed.getOrdenDeCompra().isEmpty()) {
                fechaOrden = new java.sql.Date(toPed.getOrdenDeCompraFecha().getTime()).toString();
            }
            strSQL = "INSERT INTO pedidos (folio, canceladoMotivo, estatus, entregaFolio, entregaFecha, entregaCancelacion, electronico, ordenDeCompra, ordenDeCompraFecha)\n"
                    + "VALUES (" + toPed.getPedidoFolio() + ", '', "+toPed.getPedidoEstatus()+", '"+toPed.getEntregaFolio()+"', '"+new java.sql.Date(toPed.getEntregaFecha().getTime())+"', '"+new java.sql.Date(toPed.getEntregaFechaMaxima().getTime())+"', '" + toPed.getElectronico() + "', '" + toPed.getOrdenDeCompra() + "', '" + fechaOrden + "')";
            st.executeUpdate(strSQL);

            ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idPedido");
            if (rs.next()) {
                toPed.setIdPedido(rs.getInt("idPedido"));
            }
            agregarVenta(cn, toPed, idMoneda);
        }
    }

    public static void liberarPedido(Connection cn, TOPedido toPedido, int idUsuario) throws SQLException {
        movimientos.Movimientos.liberarMovimientoOficina(cn, toPedido.getIdMovto(), idUsuario);
        toPedido.setPropietario(0);
    }

    public static TOPedidoProducto convertir(PedidoProducto p) {
        TOPedidoProducto to = new TOPedidoProducto();
        to.setIdEnvio(p.getIdEnvio());
        to.setCantEnviada(p.getCantEnviada());
        to.setIdVenta(p.getIdVenta());
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

    public static void convertir(TOPedidoProducto toProd, PedidoProducto prod) throws SQLException {
        prod.setIdEnvio(toProd.getIdEnvio());
        prod.setCantEnviada(toProd.getCantEnviada());
        prod.setCantEnviada2(toProd.getCantEnviada());
        prod.setIdVenta(toProd.getIdVenta());
        prod.setCantOrdenada(toProd.getCantOrdenada());
        prod.setCantOrdenadaSinCargo(toProd.getCantOrdenadaSinCargo());
        movimientos.Movimientos.convertir(toProd, prod);
        prod.setCantFacturada(prod.getCantOrdenada());
        prod.setCantSinCargo(prod.getCantOrdenadaSinCargo());
        prod.setCajas(prod.getCantOrdenada() / prod.getProducto().getPiezas());
        prod.setCajasSinCargo(prod.getCantOrdenadaSinCargo() / prod.getProducto().getPiezas());
    }

    public static TOPedidoProducto construirProducto(ResultSet rs) throws SQLException {
        TOPedidoProducto to = new TOPedidoProducto();
        to.setIdEnvio(rs.getInt("idEnvio"));
        to.setCantEnviada(rs.getDouble("cantEnviada"));
        to.setIdVenta(rs.getInt("idVenta"));
        to.setCantOrdenada(rs.getDouble("cantOrdenada"));
        to.setCantOrdenadaSinCargo(rs.getDouble("cantOrdenadaSinCargo"));
        to.setPiezas(rs.getInt("piezas"));
        movimientos.Movimientos.construirProductoOficina(rs, to);
        return to;
    }
    
    public static String sqlObtenProducto() {
        // LEFT JOIN con ventasDetalle por los posibles productos agregados (SIMILARES) por cantidad sin cargo
        return "SELECT ISNULL(EPD.idEnvio, 0) AS idEnvio, ISNULL(EPD.cantEnviada, 0) AS cantEnviada\n"
                + "         , PD.idVenta, PD.cantOrdenada, PD.cantOrdenadaSinCargo, D.*, E.piezas\n"
                + "FROM movimientosDetalle D\n"
                + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                + "INNER JOIN empaques E ON E.idEmpaque=D.idEmpaque\n"
                + "INNER JOIN ventasDetalle PD ON PD.idVenta=M.referencia AND PD.idEmpaque=D.idEmpaque\n"
                + "LEFT JOIN enviosPedidos EP ON EP.idVenta=M.referencia\n"
                + "LEFT JOIN enviosPedidosDetalle EPD ON EPD.idEnvio=EP.idEnvio AND EPD.idVenta=EP.idVenta AND EPD.idEmpaque=PD.idEmpaque";
    }

    public static void convertirPedido(Pedido ped, TOPedido toPed) {
        toPed.setIdEnvio(ped.getIdEnvio());
        toPed.setOrden(ped.getOrden());
        toPed.setEnvioEstatus(ped.getEnvioEstatus());
        
        toPed.setIdPedido(ped.getIdPedido());
        toPed.setPedidoFolio(ped.getPedidoFolio());
        toPed.setPedidoFecha(ped.getPedidoFecha());
        toPed.setDiasCredito(ped.getDiasCredito());
        toPed.setEspecial(ped.isEspecial()?1:0);
        toPed.setPedidoIdUsuario(ped.getPedidoIdUsuario());
        toPed.setCanceladoMotivo(ped.getCanceladoMotivo());
        toPed.setDirecto(ped.isDirecto()?1:0);
        toPed.setIdSolicitud(ped.getIdSolicitud());
        toPed.setPedidoEstatus(ped.getPedidoEstatus());
        toPed.setElectronico(ped.getElectronico());
        toPed.setOrdenDeCompra(ped.getOrdenDeCompra());
        toPed.setOrdenDeCompraFecha(ped.getOrdenDeCompraFecha());
        toPed.setEntregaFolio(ped.getEntregaFolio());
        toPed.setEntregaFecha(ped.getEntregaFecha());
        toPed.setEntregaFechaMaxima(ped.getEntregaFechaMaxima());
        movimientos.Movimientos.convertir(ped, toPed);
        toPed.setIdComprobante(ped.getComprobante().getIdComprobante());
        toPed.setIdImpuestoZona(ped.getTienda().getIdImpuestoZona());
        toPed.setIdReferencia(ped.getTienda().getIdTienda());
        toPed.setReferencia(ped.getIdVenta());
    }

    public static void convertirPedido(TOPedido toPed, Pedido ped) {
        ped.setIdEnvio(toPed.getIdEnvio());
        ped.setOrden(toPed.getOrden());
        ped.setEnvioEstatus(toPed.getEnvioEstatus());
        
        ped.setIdPedido(toPed.getIdPedido());
        ped.setPedidoFolio(toPed.getPedidoFolio());
        ped.setPedidoFecha(toPed.getPedidoFecha());
        ped.setDiasCredito(toPed.getDiasCredito());
        ped.setEspecial((toPed.getEspecial() != 0));
        ped.setPedidoIdUsuario(toPed.getPedidoIdUsuario());
        ped.setCanceladoMotivo(toPed.getCanceladoMotivo());
        ped.setDirecto(toPed.getDirecto()!=0);
        ped.setIdSolicitud(toPed.getIdSolicitud());
        ped.setPedidoEstatus(toPed.getPedidoEstatus());
        ped.setElectronico(toPed.getElectronico());
        ped.setOrdenDeCompra(toPed.getOrdenDeCompra());
        ped.setOrdenDeCompraFecha(toPed.getOrdenDeCompraFecha());
        ped.setEntregaFolio(toPed.getEntregaFolio());
        ped.setEntregaFecha(toPed.getEntregaFecha());
        ped.setEntregaFechaMaxima(toPed.getEntregaFechaMaxima());
        movimientos.Movimientos.convertir(toPed, ped);
        ped.setIdVenta(toPed.getReferencia());
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

    public static void construyePedido(TOPedido toPed, ResultSet rs) throws SQLException {
        toPed.setPedidoFolio(rs.getInt("pedidoFolio"));
        toPed.setDirecto(rs.getInt("directo"));
        toPed.setIdSolicitud(rs.getInt("idSolicitud"));
        toPed.setEspecial(rs.getInt("especial"));
        toPed.setEntregaFolio(rs.getString("entregaFolio"));
        toPed.setEntregaFecha(new java.util.Date(rs.getDate("entregaFecha").getTime()));
        toPed.setEntregaFechaMaxima(new java.util.Date(rs.getDate("entregaCancelacion").getTime()));
        toPed.setElectronico(rs.getString("electronico"));
        toPed.setCanceladoMotivo(rs.getString("canceladoMotivo"));
        toPed.setOrdenDeCompra(rs.getString("ordenDeCompra"));
        toPed.setOrdenDeCompraFecha(new java.util.Date(rs.getTimestamp("ordenDeCompraFecha").getTime()));
        toPed.setIdPedido(rs.getInt("idPedido"));
        toPed.setDiasCredito(rs.getInt("diasCredito"));
        toPed.setPedidoFecha(new java.util.Date(rs.getTimestamp("ventaFecha").getTime()));
        toPed.setPedidoIdUsuario(rs.getInt("ventaIdUsuario"));
        toPed.setPedidoEstatus(rs.getInt("pedidoEstatus"));
        movimientos.Movimientos.construirMovimientoOficina(rs, toPed);
    }
    
    public static String sqlPedidos() {
        return "ISNULL(P.folio, 0) AS pedidoFolio\n"
                + "     , ISNULL(P.entregaFolio, '') AS entregaFolio, ISNULL(P.entregaFecha, '1900-01-01') AS entregaFecha\n"
                + "     , ISNULL(P.entregaCancelacion, '1900-01-01') AS entregaCancelacion\n"
                + "     , ISNULL(P.electronico, '') AS electronico, ISNULL(P.canceladoMotivo, '') AS canceladoMotivo\n"
                + "     , ISNULL(P.ordenDeCompra, '') AS ordenDeCompra, ISNULL(P.ordenDeCompraFecha, '') AS ordenDeCompraFecha\n"
                + "     , ISNULL(P.estatus, 0) AS pedidoEstatus, V.idPedido, V.directo, V.idSolicitud, V.especial\n"
                + "     , V.diasCredito, V.fecha AS ventaFecha, V.idUsuario AS ventaIdUsuario, M.*\n"
                + "FROM movimientos M\n"
                + "INNER JOIN ventas V ON V.idVenta=M.referencia\n"
                + "LEFT JOIN pedidos P ON P.idPedido=V.idPedido";
    }

//    public static String sqlPedi2(int idAlmacen) {
////        return "P.folio AS pedidoFolio, P.fecha AS pedidoFecha, P.diasCredito, P.directo, P.especial\n"
////                + "     , P.idUsuario AS pedidoIdUsuario, P.canceladoMotivo, P.estatus AS pedidoEstatus, P.entregaFolio\n"
////                + "     , P.entregaFecha, P.entregaCancelacion, P.electronico, P.ordenDeCompra, P.ordenDeCompraFecha, M.*\n"
////                + "FROM (SELECT P.idPedido, MIN(M.idMovto) AS idPrincipal\n"
////                + "		FROM movimientos M\n"
////                + "		INNER JOIN pedidos P ON P.idPedido=M.referencia\n"
////                + "		WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=28 AND P.estatus=7\n"
////                + "		GROUP BY P.idPedido) ID\n"
////                + "RIGHT JOIN movimientos M ON M.idMovto=ID.idPrincipal\n"
////                + "INNER JOIN pedidos P ON P.idPedido=M.referencia";
//        return "P.folio AS pedidoFolio, P.fecha AS pedidoFecha, P.diasCredito, P.directo, P.especial\n"
//                + "     , P.idUsuario AS pedidoIdUsuario, P.canceladoMotivo, P.estatus AS pedidoEstatus, P.entregaFolio\n"
//                + "     , P.entregaFecha, P.entregaCancelacion, P.electronico, P.ordenDeCompra, P.ordenDeCompraFecha, M.*\n"
//                + "FROM movimientos M\n"
//                + "INNER JOIN pedidos P ON P.idPedido=M.referencia";
//    }
}
