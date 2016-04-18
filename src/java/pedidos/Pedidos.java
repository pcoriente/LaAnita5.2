package pedidos;

import comprobantes.to.TOComprobante;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import pedidos.dominio.Pedido;
import pedidos.dominio.PedidoProducto;
import pedidos.to.TOPedido;
import pedidos.to.TOPedidoProducto;

/**
 *
 * @author jesc
 */
public class Pedidos {

    public static void cierraVentaAlmacen(Connection cn, TOPedido toPed) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            toPed.setFolio(movimientos.Movimientos.obtenMovimientoFolioAlmacen(cn, toPed.getIdAlmacen(), toPed.getIdTipo()));
            movimientos.Movimientos.grabaMovimientoAlmacen(cn, toPed);
            movimientos.Movimientos.actualizaDetalleAlmacen(cn, toPed.getIdMovtoAlmacen(), false);
            movimientos.Movimientos.liberarMovimientoAlmacen(cn, toPed.getIdMovtoAlmacen(), toPed.getIdUsuario());

            movimientos.Movimientos.actualizaDetalleOficina(cn, toPed.getIdMovto(), toPed.getIdTipo(), false);
            strSQL = "UPDATE movimientos SET estatus=" + toPed.getEstatus() + " WHERE idMovto=" + toPed.getIdMovto();
            st.executeUpdate(strSQL);
            movimientos.Movimientos.liberarMovimientoOficina(cn, toPed.getIdMovto(), toPed.getIdUsuario());
        }
    }

    public static void generarPedidoVenta(Connection cn, int idPedido, int idUsuario) throws SQLException {
        int idMovto;
        String strSQL;
        try (Statement st = cn.createStatement()) {
            strSQL = "SELECT " + Pedidos.sqlPedidos() + "\n"
                    + "WHERE P.idPedido=" + idPedido + " AND M.estatus=5\n"
                    + "ORDER BY M.idMovto";
            ResultSet rs = st.executeQuery(strSQL);
            if (!rs.next()) {
                throw new SQLException("No se encontró el pedido !!!");
            }
            TOPedido toPed = Pedidos.construirPedido(rs);
            idMovto = toPed.getIdMovto();
            toPed.setIdUsuario(idUsuario);
            toPed.setPropietario(0);
            toPed.setEstatus(0);
            toPed.setFolio(0);

            strSQL = "SELECT idMoneda FROM comprobantes WHERE idComprobante=" + toPed.getIdComprobante();
            rs = st.executeQuery(strSQL);
            if (!rs.next()) {
                throw new SQLException("No se encontró el comprobante !!!");
            }
            TOComprobante to = new TOComprobante(toPed.getIdTipo(), toPed.getIdEmpresa(), toPed.getIdReferencia(), rs.getInt("idMoneda"));
            to.setTipo(1);
            to.setNumero("");
            to.setIdUsuario(idUsuario);
            to.setPropietario(0);
            comprobantes.Comprobantes.agregar(cn, to);

            toPed.setIdComprobante(to.getIdComprobante());
            movimientos.Movimientos.agregaMovimientoAlmacen(cn, toPed, false);
            movimientos.Movimientos.agregaMovimientoOficina(cn, toPed, false);

            strSQL = "UPDATE comprobantes SET numero=" + String.valueOf(toPed.getIdMovto()) + " WHERE idComprobante=" + toPed.getIdComprobante();
            st.executeUpdate(strSQL);

            strSQL = "INSERT INTO movimientosDetalle\n"
                    + "SELECT " + toPed.getIdMovto() + " AS idMovto, D.idEmpaque, 0 AS cantFacturada, 0 AS cantSinCargo, D.costoPromedio, D.costo\n"
                    + "     , D.desctoProducto1, D.desctoProducto2, D.desctoConfidencial, D.unitario, D.idImpuestoGrupo, '' AS fecha"
                    + "     , 0 AS existenciaAnterior, 0 AS ctoPromAnterior\n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "INNER JOIN ventasDetalle PD ON PD.idVenta=M.referencia AND PD.idEmpaque=D.idEmpaque\n"
                    + "WHERE D.idMovto=" + idMovto + " AND (PD.cantSurtida<PD.cantOrdenada OR PD.cantSurtidaSinCargo<PD.cantOrdenadaSinCargo)";
            st.executeUpdate(strSQL);

            strSQL = "INSERT INTO movimientosDetalleImpuestos\n"
                    + "SELECT D.idMovto, I.idEmpaque, I.idImpuesto, I.impuesto, I.valor, I.aplicable, I.modo, I.acreditable, I.importe, I.acumulable\n"
                    + "FROM (SELECT * FROM movimientosDetalleImpuestos WHERE idMovto=" + idMovto + ") I\n"
                    + "INNER JOIN movimientosDetalle D ON D.idEmpaque=I.idEmpaque\n"
                    + "WHERE D.idMovto=" + toPed.getIdMovto();
            st.executeUpdate(strSQL);
        }
    }

    public static void cerrarVenta(Connection cn, TOPedido toPed) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            strSQL = "UPDATE movimientosAlmacen SET estatus=" + toPed.getEstatus() + " WHERE idMovtoAlmacen=" + toPed.getIdMovtoAlmacen();
            st.executeUpdate(strSQL);
            movimientos.Movimientos.liberarMovimientoAlmacen(cn, toPed.getIdMovtoAlmacen(), toPed.getIdUsuario());

            toPed.setFolio(movimientos.Movimientos.obtenMovimientoFolioOficina(cn, toPed.getIdAlmacen(), toPed.getIdTipo()));
            movimientos.Movimientos.grabaMovimientoOficina(cn, toPed);
            movimientos.Movimientos.liberarMovimientoOficina(cn, toPed.getIdMovto(), toPed.getIdUsuario());

            strSQL = "UPDATE comprobantes\n"
                    + "SET fechaFactura=GETDATE(), tipo=2, numero='" + String.valueOf(toPed.getFolio()) + "'\n"
                    + "WHERE idComprobante=" + toPed.getIdComprobante();
            st.executeUpdate(strSQL);

            strSQL = "UPDATE PD\n"
                    + "SET cantSurtida=PD.cantSurtida+D.cantFacturada, cantSurtidaSinCargo=PD.cantSurtidaSinCargo+D.cantSinCargo\n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "INNER JOIN ventasDetalle PD ON PD.idVenta=M.referencia AND PD.idEmpaque=D.idEmpaque\n"
                    + "WHERE D.idMovto=" + toPed.getIdMovto();
            st.executeUpdate(strSQL);

            strSQL = "UPDATE ventas\n"
                    + "SET diasCredito=" + toPed.getDiasCredito() + "\n"
                    + "WHERE idVenta=" + toPed.getReferencia();
            st.executeUpdate(strSQL);

            if (toPed.getIdPedido() != 0) {
                strSQL = "SELECT * FROM ventasDetalle\n"
                        + "WHERE idVenta=" + toPed.getReferencia() + " AND (cantSurtida<cantOrdenada OR cantSurtidaSinCargo<cantOrdenadaSinCargo)";
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    generarPedidoVenta(cn, toPed.getIdPedido(), toPed.getIdUsuario());
                    toPed.setPedidoEstatus(5);
                } else {
                    toPed.setPedidoEstatus(7);
                }
                strSQL = "UPDATE pedidos\n"
                        + "SET estatus=" + toPed.getPedidoEstatus() + "\n"
                        + "WHERE idPedido=" + toPed.getIdPedido();
                st.executeUpdate(strSQL);
            }
        }
    }

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
            strSQL = "INSERT INTO ventas (fecha, diasCredito, especial, idUsuario, idPedido)\n"
                    + "VALUES (GETDATE(), " + toPed.getDiasCredito() + ", " + toPed.getEspecial() + ", " + toPed.getPedidoIdUsuario() + ", " + toPed.getIdPedido() + ")";
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
                    + "VALUES (" + toPed.getPedidoFolio() + ", '', " + toPed.getPedidoEstatus() + ", '" + toPed.getEntregaFolio() + "', '" + new java.sql.Date(toPed.getEntregaFecha().getTime()) + "', '" + new java.sql.Date(toPed.getEntregaFechaMaxima().getTime()) + "', '" + toPed.getElectronico() + "', '" + toPed.getOrdenDeCompra() + "', '" + fechaOrden + "')";
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
        to.setIdVenta(p.getIdVenta());
        to.setCantEnviar(p.getCantEnviar());
        to.setCantEnviarSinCargo(p.getCantEnviarSinCargo());
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
        prod.setIdVenta(toProd.getIdVenta());

        prod.setEnviar(toProd.getCantEnviar() / prod.getProducto().getPiezas());
        prod.setEnviar2(prod.getEnviar());
        prod.setEnviarSinCargo(toProd.getCantEnviarSinCargo() / prod.getProducto().getPiezas());
        prod.setEnviarSinCargo2(prod.getCantEnviarSinCargo());
        prod.setCantEnviar(toProd.getCantEnviar());
        prod.setCantEnviarSinCargo(toProd.getCantEnviarSinCargo());
        prod.setCantOrdenada(toProd.getCantOrdenada());
        prod.setCantOrdenadaSinCargo(toProd.getCantOrdenadaSinCargo());
        movimientos.Movimientos.convertir(toProd, prod);
        prod.setCantFacturada(prod.getCantOrdenada());
        prod.setCantSinCargo(prod.getCantOrdenadaSinCargo());
    }

    public static TOPedidoProducto construirProducto(ResultSet rs) throws SQLException {
        TOPedidoProducto to = new TOPedidoProducto();
        to.setIdEnvio(rs.getInt("idEnvio"));
        to.setIdVenta(rs.getInt("idVenta"));
        to.setCantEnviar(rs.getDouble("cantEnviar"));
        to.setCantEnviarSinCargo(rs.getDouble("cantEnviarSinCargo"));
        to.setCantOrdenada(rs.getDouble("cantOrdenada"));
        to.setCantOrdenadaSinCargo(rs.getDouble("cantOrdenadaSinCargo"));
        to.setPiezas(rs.getInt("piezas"));
        movimientos.Movimientos.construirProductoOficina(rs, to);
        return to;
    }

    public static String sqlObtenProducto() {
        // LEFT JOIN con ventasDetalle por los posibles productos agregados (SIMILARES) por cantidad sin cargo
        return "SELECT ISNULL(EPD.idEnvio, 0) AS idEnvio, ISNULL(EPD.cantEnviada, 0) AS cantEnviada\n"
                + "         , PD.idVenta, PD.cantOrdenada-PD.cantSurtida AS cantOrdenada\n"
                + "         , PD.cantOrdenadaSinCargo-PD.cantSurtidaSinCargo AS cantOrdenadaSinCargo, D.*, E.piezas\n"
                + "FROM movimientosDetalle D\n"
                + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                + "INNER JOIN empaques E ON E.idEmpaque=D.idEmpaque\n"
                + "INNER JOIN ventasDetalle PD ON PD.idVenta=M.referencia AND PD.idEmpaque=D.idEmpaque\n"
                + "LEFT JOIN enviosPedidos EP ON EP.idVenta=M.referencia\n"
                + "LEFT JOIN enviosPedidosDetalle EPD ON EPD.idEnvio=EP.idEnvio AND EPD.idVenta=EP.idVenta AND EPD.idEmpaque=PD.idEmpaque";
    }

    public static void convertirPedido(Pedido ped, TOPedido toPed) {
        toPed.setIdEnvio(ped.getIdEnvio());
        toPed.setDirecto(ped.isDirecto() ? 1 : 0);
        toPed.setIdSolicitud(ped.getIdSolicitud());
        toPed.setOrden(ped.getOrden());

        toPed.setDiasCredito(ped.getDiasCredito());
        toPed.setEspecial(ped.isEspecial() ? 1 : 0);

        toPed.setIdPedido(ped.getIdPedido());
        toPed.setPedidoFolio(ped.getPedidoFolio());
        toPed.setPedidoFecha(ped.getPedidoFecha());
        toPed.setPedidoIdUsuario(ped.getPedidoIdUsuario());
        toPed.setCanceladoMotivo(ped.getCanceladoMotivo());
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
        ped.setAgregado(toPed.getIdEnvio() != 0);
        ped.setDirecto(toPed.getDirecto() != 0);
        ped.setIdSolicitud(toPed.getIdSolicitud());
        ped.setOrden(toPed.getOrden());
        ped.setOrden2(toPed.getOrden());

        ped.setEspecial((toPed.getEspecial() != 0));
        ped.setDiasCredito(toPed.getDiasCredito());

        ped.setIdPedido(toPed.getIdPedido());
        ped.setPedidoFolio(toPed.getPedidoFolio());
        ped.setPedidoFecha(toPed.getPedidoFecha()); //
        ped.setPedidoIdUsuario(toPed.getPedidoIdUsuario()); //
        ped.setEntregaFolio(toPed.getEntregaFolio());
        ped.setEntregaFecha(toPed.getEntregaFecha());
        ped.setEntregaFechaMaxima(toPed.getEntregaFechaMaxima());
        ped.setElectronico(toPed.getElectronico());
        ped.setCanceladoMotivo(toPed.getCanceladoMotivo());
        ped.setOrdenDeCompra(toPed.getOrdenDeCompra());
        ped.setOrdenDeCompraFecha(toPed.getOrdenDeCompraFecha());
        ped.setPedidoEstatus(toPed.getPedidoEstatus());
        movimientos.Movimientos.convertir(toPed, ped);
        ped.setIdVenta(toPed.getReferencia());
    }

    public static TOPedido construirPedido(ResultSet rs) throws SQLException {
        TOPedido toPed = new TOPedido();
        construyePedido(toPed, rs);
        return toPed;
    }

    public static void construyePedido(TOPedido toPed, ResultSet rs) throws SQLException {
        toPed.setIdEnvio(rs.getInt("idEnvio"));
        toPed.setDirecto(rs.getInt("directo"));
        toPed.setIdSolicitud(rs.getInt("idSolicitud"));
        toPed.setOrden(rs.getInt("orden"));

        toPed.setEspecial(rs.getInt("especial"));
        toPed.setDiasCredito(rs.getInt("diasCredito"));
//        toPed.setPedidoFecha(new java.util.Date(rs.getTimestamp("ventaFecha").getTime()));
//        toPed.setPedidoIdUsuario(rs.getInt("ventaIdUsuario"));

        toPed.setIdPedido(rs.getInt("idPedido"));
        toPed.setPedidoFolio(rs.getInt("pedidoFolio"));
        toPed.setEntregaFolio(rs.getString("entregaFolio"));
        toPed.setEntregaFecha(new java.util.Date(rs.getDate("entregaFecha").getTime()));
        toPed.setEntregaFechaMaxima(new java.util.Date(rs.getDate("entregaCancelacion").getTime()));
        toPed.setElectronico(rs.getString("electronico"));
        toPed.setCanceladoMotivo(rs.getString("canceladoMotivo"));
        toPed.setOrdenDeCompra(rs.getString("ordenDeCompra"));
        toPed.setOrdenDeCompraFecha(new java.util.Date(rs.getTimestamp("ordenDeCompraFecha").getTime()));
        toPed.setPedidoEstatus(rs.getInt("pedidoEstatus"));
        movimientos.Movimientos.construirMovimientoOficina(rs, toPed);
    }

    public static String sqlPedidos() {
        return "ISNULL(P.folio, 0) AS pedidoFolio\n"
                + "     , ISNULL(P.entregaFolio, '') AS entregaFolio, ISNULL(P.entregaFecha, '1900-01-01') AS entregaFecha\n"
                + "     , ISNULL(P.entregaCancelacion, '1900-01-01') AS entregaCancelacion\n"
                + "     , ISNULL(P.electronico, '') AS electronico, ISNULL(P.canceladoMotivo, '') AS canceladoMotivo\n"
                + "     , ISNULL(P.ordenDeCompra, '') AS ordenDeCompra, ISNULL(P.ordenDeCompraFecha, '') AS ordenDeCompraFecha\n"
                + "     , ISNULL(P.estatus, 0) AS pedidoEstatus, ISNULL(EP.idEnvio, 0) AS idEnvio\n"
                + "     , ISNULL(EP.directo,0) AS directo, ISNULL(EP.idSolicitud, 0) AS idSolicitud, ISNULL(EP.orden, 0) AS orden\n"
                + "     , V.idPedido, V.especial, V.diasCredito, V.fecha AS ventaFecha, V.idUsuario AS ventaIdUsuario, M.*\n"
                + "FROM movimientos M\n"
                + "INNER JOIN ventas V ON V.idVenta=M.referencia\n"
                + "LEFT JOIN enviosPedidos EP ON EP.idVenta=V.idVenta\n"
                + "LEFT JOIN pedidos P ON P.idPedido=V.idPedido";
    }
}
