package pedidos;

import comprobantes.to.TOComprobante;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import movimientos.Movimientos;
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
            strSQL = "SELECT D.idEmpaque\n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "LEFT JOIN (SELECT D.idEmpaque, SUM(D.cantidad) AS cantidad\n"
                    + "			FROM movimientosDetalleAlmacen D\n"
                    + "			INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                    + "			WHERE D.idMovtoAlmacen=" + toPed.getIdMovtoAlmacen() + "\n"
                    + "			GROUP BY D.idEmpaque) A ON A.idEmpaque=D.idEmpaque\n"
                    + "WHERE D.idMovto=" + toPed.getIdMovto() + " AND (A.idEmpaque IS NULL OR D.cantFacturada+D.cantSinCargo != A.cantidad)";
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                throw new SQLException("Lotes incompletos !!!");
            }
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

    public static void generarPedidoVenta(Connection cn, TOPedido toPedido, int idUsuario) throws SQLException {
        int idMovto;
        String strSQL;
        try (Statement st = cn.createStatement()) {
            strSQL = "SELECT " + Pedidos.sqlPedidos() + "\n"
                    + "WHERE P.idPedido=" + toPedido.getReferencia() + " AND M.idAlmacen=" + toPedido.getIdAlmacen() + " AND M.idTipo=28 AND M.estatus=5\n"
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

            strSQL = "INSERT INTO ventas (idMovto, diasCredito, especial)\n"
                    + "VALUES (" + toPed.getIdMovto() + ", " + toPed.getDiasCredito() + ", " + toPed.getEspecial() + ")";
            st.executeUpdate(strSQL);

            strSQL = "UPDATE comprobantes SET numero=" + String.valueOf(toPed.getIdMovto()) + " WHERE idComprobante=" + toPed.getIdComprobante();
            st.executeUpdate(strSQL);

            strSQL = "INSERT INTO movimientosDetalle\n"
                    + "SELECT " + toPed.getIdMovto() + " AS idMovto, D.idEmpaque, 0 AS cantFacturada, 0 AS cantSinCargo, D.costoPromedio, D.costo\n"
                    + "     , D.desctoProducto1, D.desctoProducto2, D.desctoConfidencial, D.unitario, D.idImpuestoGrupo, '' AS fecha"
                    + "     , 0 AS existenciaAnterior, 0 AS ctoPromAnterior\n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "INNER JOIN pedidosDetalle PD ON PD.idPedido=M.referencia AND PD.idEmpaque=D.idEmpaque\n"
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
                    + "INNER JOIN pedidosDetalle PD ON PD.idPedido=M.referencia AND PD.idEmpaque=D.idEmpaque\n"
                    + "WHERE D.idMovto=" + toPed.getIdMovto();
            st.executeUpdate(strSQL);

            strSQL = "UPDATE ventas\n"
                    + "SET diasCredito=" + toPed.getDiasCredito() + "\n"
                    + "WHERE idMovto=" + toPed.getIdMovto();
            st.executeUpdate(strSQL);

            if (toPed.getReferencia() != 0) {
                strSQL = "SELECT * FROM pedidosDetalle\n"
                        + "WHERE idPedido=" + toPed.getReferencia() + " AND (cantSurtida<cantOrdenada OR cantSurtidaSinCargo<cantOrdenadaSinCargo)";
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    generarPedidoVenta(cn, toPed, toPed.getIdUsuario());
                    toPed.setPedidoEstatus(5);
                } else {
                    toPed.setPedidoEstatus(7);
                }
                strSQL = "UPDATE pedidos\n"
                        + "SET estatus=" + toPed.getPedidoEstatus() + "\n"
                        + "WHERE idPedido=" + toPed.getReferencia();
                st.executeUpdate(strSQL);
            }
        }
    }

    public static void agregaProducto(Connection cn, TOPedidoProducto toProd) throws SQLException {
        String strSQL = "INSERT INTO pedidosDetalle (idPedido, idEmpaque, cantOrdenada, cantOrdenadaSinCargo, cantSurtida, cantSurtidaSinCargo)\n"
                + "VALUES (" + toProd.getIdPedido() + ", " + toProd.getIdProducto() + ", " + toProd.getCantOrdenada() + ", " + toProd.getCantOrdenadaSinCargo() + ", 0, 0)";
        try (Statement st = cn.createStatement()) {
            st.executeUpdate(strSQL);
        }
    }

    public static void agregaProductoPedido(Connection cn, TOPedido toPed, TOPedidoProducto toProd) throws SQLException {
        Movimientos.agregarProductoVenta(cn, toPed, toProd, toPed.getOrdenDeCompraFecha());
        agregaProducto(cn, toProd);
    }

    private static void agregarVenta(Connection cn, TOPedido toPed, int idMoneda) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
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

            strSQL = "INSERT INTO ventas (idMovto, diasCredito, especial)\n"
                    + "VALUES (" + toPed.getIdMovto() + ", " + toPed.getDiasCredito() + ", " + toPed.getEspecial() + ")";
            st.executeUpdate(strSQL);
        }
    }

    public static void agregarPedido(Connection cn, TOPedido toPed, int idMoneda) throws SQLException {
        String strSQL = "INSERT INTO pedidos (folio, fecha, idUsuario, estatus, idMovto, idOrden)\n"
                + "VALUES (0, GETDATE(), " + toPed.getIdUsuario() + ", 0, 0, " + toPed.getIdOrden() + ")";
        try (Statement st = cn.createStatement()) {
            st.executeUpdate(strSQL);

            ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idPedido");
            if (rs.next()) {
                toPed.setReferencia(rs.getInt("idPedido"));
            }
            agregarVenta(cn, toPed, idMoneda);

            strSQL = "UPDATE pedidos SET idMovto=" + toPed.getIdMovto() + " WHERE idPedido=" + toPed.getReferencia();
            st.executeUpdate(strSQL);
        }
    }

    public static void agregarPedidoConOrden(Connection cn, TOPedido toPed, int idMoneda) throws SQLException {
        String strSQL, fechaOrden = "";
        try (Statement st = cn.createStatement()) {
            if (!toPed.getOrdenDeCompra().isEmpty()) {
                fechaOrden = new java.sql.Date(toPed.getOrdenDeCompraFecha().getTime()).toString();
            }
            strSQL = "INSERT INTO pedidosOrdenes (canceladoMotivo, entregaFolio, entregaFecha, entregaCancelacion, electronico, ordenDeCompra, ordenDeCompraFecha)\n"
                    + "VALUES ('', '" + toPed.getEntregaFolio() + "', '" + new java.sql.Date(toPed.getEntregaFecha().getTime()) + "', '" + new java.sql.Date(toPed.getEntregaFechaMaxima().getTime()) + "', '" + toPed.getElectronico() + "', '" + toPed.getOrdenDeCompra() + "', '" + fechaOrden + "')";
            st.executeUpdate(strSQL);

            ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idOrden");
            if (rs.next()) {
                toPed.setIdOrden(rs.getInt("idOrden"));
            }
            agregarPedido(cn, toPed, idMoneda);
        }
    }

    public static void liberarPedido(Connection cn, TOPedido toPedido, int idUsuario) throws SQLException {
        movimientos.Movimientos.liberarMovimientoOficina(cn, toPedido.getIdMovto(), idUsuario);
        toPedido.setPropietario(0);
    }

    public static TOPedidoProducto convertir(PedidoProducto prod) {
        TOPedidoProducto toProd = new TOPedidoProducto();
        toProd.setIdEnvio(prod.getIdEnvio());
        toProd.setIdPedido(prod.getIdPedido());
        toProd.setIdMovto(prod.getIdMovto());
        toProd.setIdProducto(prod.getProducto().getIdProducto());
        toProd.setPiezas(prod.getProducto().getPiezas());

        toProd.setCantEnviar(prod.getEnviar() * toProd.getPiezas());
        toProd.setCantEnviarSinCargo(prod.getEnviarSinCargo() * toProd.getPiezas());
        toProd.setCantOrdenada(prod.getOrdenada() * toProd.getPiezas());
        toProd.setCantOrdenadaSinCargo(prod.getOrdenadaSinCargo() * toProd.getPiezas());
        toProd.setCantFacturada(prod.getCantFacturada());
        toProd.setCantSinCargo(prod.getCantSinCargo());
        toProd.setCosto(prod.getCosto());
        toProd.setDesctoProducto1(prod.getDesctoProducto1());
        toProd.setUnitario(prod.getUnitario());
        toProd.setIdImpuestoGrupo(prod.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());
        return toProd;
    }

    public static void convertir(TOPedidoProducto toProd, PedidoProducto prod) throws SQLException {
        prod.setIdEnvio(toProd.getIdEnvio());
        prod.setEnviar(toProd.getCantEnviar() / prod.getProducto().getPiezas());
//        prod.setRespaldo(prod.getEnviar());
        prod.setEnviarSinCargo(toProd.getCantEnviarSinCargo() / prod.getProducto().getPiezas());
//        prod.setRespaldoSinCargo(prod.getEnviarSinCargo());
//        prod.setEnviar2(prod.getEnviar());
//        prod.setEnviarSinCargo2(prod.getEnviarSinCargo());
//        prod.setCantEnviar(toProd.getCantEnviar() / prod.getProducto().getPiezas());
//        prod.setCantEnviarSinCargo(toProd.getCantEnviarSinCargo());
//        prod.setCantOrdenada(toProd.getCantOrdenada());
//        prod.setCantOrdenadaSinCargo(toProd.getCantOrdenadaSinCargo());
        prod.setIdPedido(toProd.getIdPedido());
        prod.setOrdenada(toProd.getCantOrdenada() / prod.getProducto().getPiezas());
        prod.setOrdenadaSinCargo(toProd.getCantOrdenadaSinCargo() / prod.getProducto().getPiezas());
        movimientos.Movimientos.convertir(toProd, prod);
//        prod.setCantFacturada(prod.getCantOrdenada());
//        prod.setCantSinCargo(prod.getCantOrdenadaSinCargo());
    }

    public static TOPedidoProducto construirProducto(ResultSet rs) throws SQLException {
        TOPedidoProducto to = new TOPedidoProducto();
        to.setIdEnvio(rs.getInt("idEnvio"));
        to.setCantEnviar(rs.getDouble("cantEnviar"));
        to.setCantEnviarSinCargo(rs.getDouble("cantEnviarSinCargo"));
        to.setIdPedido(rs.getInt("idPedido"));
        to.setCantOrdenada(rs.getDouble("cantOrdenada"));
        to.setCantOrdenadaSinCargo(rs.getDouble("cantOrdenadaSinCargo"));
        to.setPiezas(rs.getInt("piezas"));
        movimientos.Movimientos.construirProductoOficina(rs, to);
        return to;
    }

    public static String sqlObtenProducto() {
        // LEFT JOIN con ventasDetalle por los posibles productos agregados (SIMILARES) por cantidad sin cargo
        return "SELECT ISNULL(EPD.idEnvio, 0) AS idEnvio, ISNULL(EPD.cantEnviar, 0) AS cantEnviar, ISNULL(EPD.cantEnviarSinCargo, 0) AS cantEnviarSinCargo\n"
                + "         , PD.idPedido, PD.cantOrdenada-PD.cantSurtida AS cantOrdenada\n"
                + "         , PD.cantOrdenadaSinCargo-PD.cantSurtidaSinCargo AS cantOrdenadaSinCargo, D.*, E.piezas\n"
                + "FROM movimientosDetalle D\n"
                + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                + "INNER JOIN empaques E ON E.idEmpaque=D.idEmpaque\n"
                + "INNER JOIN pedidosDetalle PD ON PD.idPedido=M.referencia AND PD.idEmpaque=D.idEmpaque\n"
                + "LEFT JOIN enviosPedidos EP ON EP.idVenta=M.idMovto\n"
                + "LEFT JOIN enviosPedidosDetalle EPD ON EPD.idEnvio=EP.idEnvio AND EPD.idVenta=M.idMovto AND EPD.idEmpaque=PD.idEmpaque";
    }

    public static String sqlObtenProductoPedido() {
        // LEFT JOIN con ventasDetalle por los posibles productos agregados (SIMILARES) por cantidad sin cargo
        return "SELECT ISNULL(EPD.idEnvio, 0) AS idEnvio, ISNULL(EPD.cantEnviar, 0) AS cantEnviar, ISNULL(EPD.cantEnviarSinCargo, 0) AS cantEnviarSinCargo\n"
                + "         , PD.idPedido, PD.cantOrdenada, PD.cantOrdenadaSinCargo, D.*, E.piezas\n"
                + "FROM movimientosDetalle D\n"
                + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                + "INNER JOIN empaques E ON E.idEmpaque=D.idEmpaque\n"
                + "INNER JOIN pedidosDetalle PD ON PD.idPedido=M.referencia AND PD.idEmpaque=D.idEmpaque\n"
                + "LEFT JOIN enviosPedidos EP ON EP.idVenta=M.idMovto\n"
                + "LEFT JOIN enviosPedidosDetalle EPD ON EPD.idEnvio=EP.idEnvio AND EPD.idVenta=M.idMovto AND EPD.idEmpaque=PD.idEmpaque";
    }

    public static void convertirPedido(Pedido ped, TOPedido toPed) {
        toPed.setIdEnvio(ped.getIdEnvio());
        toPed.setDirecto(ped.isDirecto() ? 1 : 0);
        toPed.setIdSolicitud(ped.getIdSolicitud());
        toPed.setOrden(ped.getOrden());

        toPed.setIdOrden(ped.getIdOrden());
        toPed.setOrdenDeCompra(ped.getOrdenDeCompra());
        toPed.setOrdenDeCompraFecha(ped.getOrdenDeCompraFecha());
        toPed.setElectronico(ped.getElectronico());
        toPed.setEntregaFolio(ped.getEntregaFolio());
        toPed.setEntregaFecha(ped.getEntregaFecha());
        toPed.setEntregaFechaMaxima(ped.getEntregaFechaMaxima());
        toPed.setCanceladoMotivo(ped.getCanceladoMotivo());

//        toPed.setIdPedido(ped.getIdPedido());
        toPed.setPedidoFolio(ped.getPedidoFolio());
        toPed.setPedidoFecha(ped.getPedidoFecha());
        toPed.setPedidoIdUsuario(ped.getPedidoIdUsuario());
        toPed.setPedidoEstatus(ped.getPedidoEstatus());

        toPed.setDiasCredito(ped.getDiasCredito());
        toPed.setEspecial(ped.isEspecial() ? 1 : 0);
        movimientos.Movimientos.convertir(ped, toPed);
        toPed.setIdComprobante(ped.getComprobante().getIdComprobante());
        toPed.setIdImpuestoZona(ped.getTienda().getIdImpuestoZona());
        toPed.setIdReferencia(ped.getTienda().getIdTienda());
        toPed.setReferencia(ped.getIdPedido());
    }

    public static void convertirPedido(TOPedido toPed, Pedido ped) {
        ped.setIdEnvio(toPed.getIdEnvio());
        ped.setAgregado(toPed.getIdEnvio() != 0);
        ped.setDirecto(toPed.getDirecto() != 0);
        ped.setIdSolicitud(toPed.getIdSolicitud());
        ped.setOrden(toPed.getOrden());
        ped.setOrden2(toPed.getOrden());

        ped.setIdOrden(toPed.getIdOrden());
        ped.setOrdenDeCompra(toPed.getOrdenDeCompra());
        ped.setOrdenDeCompraFecha(toPed.getOrdenDeCompraFecha());
        ped.setElectronico(toPed.getElectronico());
        ped.setEntregaFolio(toPed.getEntregaFolio());
        ped.setEntregaFecha(toPed.getEntregaFecha());
        ped.setEntregaFechaMaxima(toPed.getEntregaFechaMaxima());
        ped.setCanceladoMotivo(toPed.getCanceladoMotivo());

        ped.setPedidoFolio(toPed.getPedidoFolio());
        ped.setPedidoFecha(toPed.getPedidoFecha());
        ped.setPedidoIdUsuario(toPed.getPedidoIdUsuario());
        ped.setPedidoEstatus(toPed.getPedidoEstatus());

        ped.setDiasCredito(toPed.getDiasCredito());
        ped.setEspecial((toPed.getEspecial() != 0));
        movimientos.Movimientos.convertir(toPed, ped);
        ped.setIdPedido(toPed.getReferencia());
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

        toPed.setIdOrden(rs.getInt("idOrden"));
        toPed.setOrdenDeCompra(rs.getString("ordenDeCompra"));
        toPed.setOrdenDeCompraFecha(new java.util.Date(rs.getTimestamp("ordenDeCompraFecha").getTime()));
        toPed.setElectronico(rs.getString("electronico"));
        toPed.setEntregaFolio(rs.getString("entregaFolio"));
        toPed.setEntregaFecha(new java.util.Date(rs.getDate("entregaFecha").getTime()));
        toPed.setEntregaFechaMaxima(new java.util.Date(rs.getDate("entregaCancelacion").getTime()));
        toPed.setCanceladoMotivo(rs.getString("canceladoMotivo"));

//        toPed.setIdPedido(rs.getInt("idPedido"));
        toPed.setPedidoFolio(rs.getInt("pedidoFolio"));
        toPed.setPedidoFecha(new java.util.Date(rs.getTimestamp("pedidoFecha").getTime()));
        toPed.setPedidoIdUsuario(rs.getInt("pedidoIdUsuario"));
        toPed.setPedidoEstatus(rs.getInt("pedidoEstatus"));

        toPed.setEspecial(rs.getInt("especial"));
        toPed.setDiasCredito(rs.getInt("diasCredito"));
        movimientos.Movimientos.construirMovimientoOficina(rs, toPed);
    }

    public static String sqlVentas() {
        return "ISNULL(EP.idEnvio, 0) AS idEnvio, ISNULL(EP.directo,0) AS directo, ISNULL(EP.idSolicitud, 0) AS idSolicitud, ISNULL(EP.orden, 0) AS orden\n"
                + "     , ISNULL(PO.idOrden, 0) AS idOrden, ISNULL(PO.ordenDeCompra, '') AS ordenDeCompra, ISNULL(PO.ordenDeCompraFecha, '1900-01-01') AS ordenDeCompraFecha\n"
                + "     , ISNULL(PO.electronico, '') AS electronico, ISNULL(PO.entregaFolio, '') AS entregaFolio, ISNULL(PO.entregaFecha, '1900-01-01') AS entregaFecha\n"
                + "	, ISNULL(PO.canceladoMotivo, '') AS canceladoMotivo, ISNULL(PO.entregaCancelacion, '1900-01-01') AS entregaCancelacion\n"
                + "     , ISNULL(P.folio, 0) AS pedidoFolio, ISNULL(P.fecha, '1900-01-01') AS pedidoFecha, ISNULL(P.idUsuario, 0) AS pedidoIdUsuario\n"
                + "     , ISNULL(P.estatus, 0) AS pedidoEstatus, V.diasCredito, V.especial, M.*\n"
                + "FROM movimientos M INNER JOIN ventas V ON V.idMovto=M.idMovto\n"
                + "LEFT JOIN pedidos P ON P.idPedido=M.referencia LEFT JOIN pedidosOrdenes PO ON PO.idOrden=P.idOrden\n"
                + "LEFT JOIN enviosPedidos EP ON EP.idVenta=P.idMovto";
    }

    public static String sqlPedidos() {
        /*
         "ISNULL(P.folio, 0) AS pedidoFolio\n"
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
         */
        return "ISNULL(EP.idEnvio, 0) AS idEnvio, ISNULL(EP.directo,0) AS directo, ISNULL(EP.idSolicitud, 0) AS idSolicitud, ISNULL(EP.orden, 0) AS orden\n"
                + "     , ISNULL(PO.idOrden, 0) AS idOrden, ISNULL(PO.ordenDeCompra, '') AS ordenDeCompra, ISNULL(PO.ordenDeCompraFecha, '1900-01-01') AS ordenDeCompraFecha\n"
                + "     , ISNULL(PO.electronico, '') AS electronico, ISNULL(PO.entregaFolio, '') AS entregaFolio, ISNULL(PO.entregaFecha, '1900-01-01') AS entregaFecha\n"
                + "	, ISNULL(PO.canceladoMotivo, '') AS canceladoMotivo, ISNULL(PO.entregaCancelacion, '1900-01-01') AS entregaCancelacion\n"
                + "     , P.folio AS pedidoFolio, P.fecha AS pedidoFecha, P.idUsuario AS pedidoIdUsuario, P.estatus AS pedidoEstatus\n"
                + "     , V.diasCredito, V.especial, M.*\n"
                + "FROM pedidos P INNER JOIN movimientos M ON M.idMovto=P.idMovto INNER JOIN ventas V ON V.idMovto=M.idMovto\n"
                + "LEFT JOIN pedidosOrdenes PO ON PO.idOrden=P.idOrden LEFT JOIN enviosPedidos EP ON EP.idVenta=P.idMovto";
    }
}
