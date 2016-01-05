package movimientos;

import impuestos.dominio.ImpuestosProducto;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import movimientos.dominio.ProductoAlmacen;
import movimientos.dominio.MovimientoAlmacen;
import movimientos.dominio.MovimientoOficina;
import movimientos.dominio.MovimientoTipo;
import movimientos.dominio.ProductoLotes;
import movimientos.dominio.ProductoOficina;
import movimientos.to.TOMovimientoAlmacen;
import movimientos.to.TOMovimientoOficina;
import movimientos.to.TOMovimientoProductoAlmacen;
import movimientos.to.TOProductoAlmacen;
import movimientos.to.TOProductoLotes;
import movimientos.to.TOProductoOficina;

/**
 *
 * @author jesc
 */
public class Movimientos {

//    ====================================== ALMACEN =======================================
    public static void eliminaProductoAlmacen(Connection cn, int idMovtoAlmacen, int idProducto) throws SQLException {
        try (Statement st = cn.createStatement()) {
            String strSQL = "DELETE D\n"
                    + "FROM movimientosDetalleAlmacen D\n"
                    + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                    + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen + " AND D.idEmpaque=" + idProducto + " AND M.referencia=0";
            st.executeUpdate(strSQL);
        }
    }

    public static void liberar(Connection cn, int idAlmacen, TOProductoAlmacen toProd, double cantLiberar) throws SQLException {
        String strSQL = "UPDATE almacenesLotes\n"
                + "SET separados=separados-" + cantLiberar + "\n"
                + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + toProd.getIdProducto() + " AND lote='" + toProd.getLote() + "'";
        try (Statement st = cn.createStatement()) {
            st.executeUpdate(strSQL);

            toProd.setCantidad(toProd.getCantidad() - cantLiberar);
            grabaProductoAlmacen(cn, toProd);
        }
    }

    public static double separar(Connection cn, int idAlmacen, TOProductoAlmacen toProd, double cantSeparar, boolean total) throws SQLException {
        double disponibles = 0;
        String strSQL = "SELECT lote,  existencia-separados AS disponibles\n"
                + "FROM almacenesLotes\n"
                + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + toProd.getIdProducto() + " AND lote='" + toProd.getLote() + "'";
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                disponibles = rs.getDouble("disponibles");
            } else {
                throw new SQLException("No se encontró el lote solicitado !!!");
            }
            if (disponibles <= 0) {
                throw new SQLException("No hay unidades disponibles del lote solicitado !!!");
            } else if (disponibles < cantSeparar) {
                if (total) {
                    throw new SQLException("No hay existencia total para separar !!!");
                }
                cantSeparar = disponibles;
            }
            strSQL = "UPDATE almacenesLotes\n"
                    + "SET separados=separados+" + cantSeparar + "\n"
                    + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + toProd.getIdProducto() + " AND lote='" + toProd.getLote() + "'";
            st.executeUpdate(strSQL);

            toProd.setCantidad(toProd.getCantidad() + cantSeparar);
            movimientos.Movimientos.grabaProductoAlmacen(cn, toProd);

            cn.commit();
        }
        return cantSeparar;
    }

    public static void convertir(ProductoAlmacen prod, TOProductoAlmacen toProd) {
        toProd.setIdMovtoAlmacen(prod.getIdMovtoAlmacen());
        toProd.setIdProducto(prod.getProducto().getIdProducto());
        toProd.setLote(prod.getLote());
        toProd.setCantidad(prod.getCantidad());
    }

    public static TOProductoAlmacen convertir(ProductoAlmacen prod) {
        TOProductoAlmacen toProd = new TOProductoAlmacen();
        movimientos.Movimientos.convertir(prod, toProd);
        return toProd;
    }

    public static void convertir(TOProductoAlmacen toProd, ProductoAlmacen prod) {
        prod.setIdMovtoAlmacen(toProd.getIdMovtoAlmacen());
        prod.setLote(toProd.getLote());
        prod.setCantidad(toProd.getCantidad());
    }

    public static ProductoAlmacen convertir(TOProductoAlmacen toProd) {
        ProductoAlmacen prod = new ProductoAlmacen();
        convertir(toProd, prod);
        return prod;
    }

    public static void convertir(TOProductoLotes toProd, ProductoLotes prod) {
        prod.setCantidad(toProd.getCantidad());
        prod.setLotes(toProd.getLotes());
//        for (TOProductoAlmacen toLote : toProd.getLotes()) {
//            prod.getLotes().add(convertir(toLote));
//        }
    }

    public static TOMovimientoProductoAlmacen construirLote(ResultSet rs) throws SQLException {
        TOMovimientoProductoAlmacen lote = new TOMovimientoProductoAlmacen();
        construirProductoAlmacen(rs, lote);
        lote.setSeparados(lote.getCantidad());
        lote.setFechaCaducidad(new java.util.Date(rs.getDate("fechaCaducidad").getTime()));
        return lote;
    }

//    public static boolean construirProducto(ResultSet rs, TOProductoLotes toProd) throws SQLException {
//        boolean fin = false;
//        int idEmpaque=rs.getInt("idEmpaque");
//        do {
//            toProd.setIdProducto(idEmpaque);
//            do {
//                toProd.getLotes().add(construirLote(rs));
//                if (rs.next()) {
//                    idEmpaque=rs.getInt("idEmpaque");
//                } else {
//                    fin=true;
//                    idEmpaque=0;
//                }
//            } while (!fin && toProd.getIdProducto() == idEmpaque);
//        } while (!fin);
//        return fin;
//    }
    public static void grabaProductoAlmacen(Connection cn, TOProductoAlmacen toProd) throws SQLException {
        String strSQL = "UPDATE movimientosDetalleAlmacen\n"
                + "SET cantidad=" + toProd.getCantidad() + "\n"
                + "WHERE idMovtoAlmacen=" + toProd.getIdMovtoAlmacen() + " AND idEmpaque=" + toProd.getIdProducto() + " AND lote='" + toProd.getLote() + "'";
        try (Statement st = cn.createStatement()) {
            st.executeUpdate(strSQL);
        }
    }

    public static void construirProductoAlmacen(ResultSet rs, TOProductoAlmacen lote) throws SQLException {
        lote.setIdMovtoAlmacen(rs.getInt("idMovtoAlmacen"));
        lote.setIdProducto(rs.getInt("idEmpaque"));
        lote.setLote(rs.getString("lote"));
        lote.setCantidad(rs.getDouble("cantidad"));
    }

    public static ArrayList<TOMovimientoProductoAlmacen> obtenerDetalleProducto(Connection cn, int idMovtoAlmacen, int idProducto) throws SQLException {
        String strSQL = "SELECT D.*, ISNULL(A.fechaCaducidad, DATEADD(DAY, 365, L.fecha)) AS fechaCaducidad\n"
                + "FROM movimientosDetalleAlmacen D\n"
                + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                + "INNER JOIN lotes L ON L.lote=SUBSTRING(D.lote, 1, 4)\n"
                + "LEFT JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen + " AND D.idEmpaque=" + idProducto + "\n"
                + "ORDER BY L.fecha";
        ArrayList<TOMovimientoProductoAlmacen> producto = new ArrayList<>();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                producto.add(movimientos.Movimientos.construirLote(rs));
            }
        }
        return producto;
    }

    public static TOProductoAlmacen construirProductoAlmacen(ResultSet rs) throws SQLException {
        TOProductoAlmacen lote = new TOProductoAlmacen();
        construirProductoAlmacen(rs, lote);
        return lote;
    }

    public static void agregaProductoAlmacen(Connection cn, TOProductoAlmacen toProd) throws SQLException {
        String strSQL = "INSERT INTO movimientosDetalleAlmacen (idMovtoAlmacen, idEmpaque, lote, cantidad, fecha, existenciaAnterior)\n"
                + "VALUES (" + toProd.getIdMovtoAlmacen() + ", " + toProd.getIdProducto() + ", '" + toProd.getLote() + "', " + toProd.getCantidad() + ", '', 0)";
        try (Statement st = cn.createStatement()) {
            st.executeUpdate(strSQL);
        }
    }

    public static TOMovimientoAlmacen construirMovimientoAlmacen(ResultSet rs) throws SQLException {
        TOMovimientoAlmacen toMov = new TOMovimientoAlmacen();
        toMov.setIdMovtoAlmacen(rs.getInt("idMovtoAlmacen"));
        toMov.setIdTipo(rs.getInt("idTipo"));
        toMov.setIdEmpresa(rs.getInt("idEmpresa"));
        toMov.setIdAlmacen(rs.getInt("idAlmacen"));
        toMov.setFolio(rs.getInt("folio"));
        toMov.setIdComprobante(rs.getInt("idComprobante"));
        toMov.setFecha(new java.util.Date(rs.getTimestamp("fecha").getTime()));
        toMov.setIdReferencia(rs.getInt("idReferencia"));
        toMov.setReferencia(rs.getInt("referencia"));
        toMov.setIdUsuario(rs.getInt("idUsuario"));
        toMov.setPropietario(rs.getInt("propietario"));
        toMov.setEstatus(rs.getInt("estatus"));
        return toMov;
    }

    public static TOMovimientoAlmacen obtenMovimientoAlmacen(Connection cn, int idMovtoAlmacen) throws SQLException {
        String strSQL;
        TOMovimientoAlmacen toMov = null;
        try (Statement st = cn.createStatement()) {
            strSQL = "SELECT * FROM movimientosAlmacen WHERE idMovtoAlmacen=" + idMovtoAlmacen;
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                toMov = construirMovimientoAlmacen(rs);
            }
        }
        return toMov;
    }

    public static int obtenMovimientoFolioAlmacen(Connection cn, int idAlmacen, int idTipo) throws SQLException {
        int folio;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT folio FROM movimientosFoliosAlmacen WHERE idAlmacen=" + idAlmacen + " AND idTipo=" + idTipo);
            if (rs.next()) {
                folio = rs.getInt("folio");
                st.executeUpdate("UPDATE movimientosFoliosAlmacen SET folio=folio+1 WHERE idAlmacen=" + idAlmacen + " AND idTipo=" + idTipo);
            } else {
                folio = 1;
                st.executeUpdate("INSERT INTO movimientosFoliosAlmacen (idAlmacen, idTipo, folio) VALUES (" + idAlmacen + ", " + idTipo + ", 2)");
            }
        }
        return folio;
    }

    public static double sumaPiezasAlmacen(ArrayList<ProductoLotes> detalle) {
        double piezas = 0;
        for (ProductoLotes p : detalle) {
            piezas += p.getCantidad();
        }
        return piezas;
    }

    public static void convertir(TOMovimientoAlmacen toMov, MovimientoAlmacen mov) {
        mov.setIdMovtoAlmacen(toMov.getIdMovtoAlmacen());
        mov.setFolio(toMov.getFolio());
        mov.setFecha(toMov.getFecha());
        mov.setIdUsuario(toMov.getIdUsuario());
        mov.setPropietario(toMov.getPropietario());
        mov.setEstatus(toMov.getEstatus());
    }

    public static void convertir(MovimientoAlmacen mov, TOMovimientoAlmacen toMov) {
        toMov.setIdMovtoAlmacen(mov.getIdMovtoAlmacen());
        toMov.setIdTipo(mov.getTipo().getIdTipo());
        toMov.setFolio(mov.getFolio());
        toMov.setIdEmpresa(mov.getAlmacen().getIdEmpresa());
        toMov.setIdAlmacen(mov.getAlmacen().getIdAlmacen());
        toMov.setFecha(mov.getFecha());
        toMov.setIdUsuario(mov.getIdUsuario());
        toMov.setPropietario(mov.getPropietario());
        toMov.setEstatus(mov.getEstatus());
    }

    public static void agregaMovimientoAlmacen(Connection cn, TOMovimientoAlmacen to, boolean definitivo) throws SQLException {
        try (Statement st = cn.createStatement()) {
            if (definitivo) {
                to.setFolio(obtenMovimientoFolioAlmacen(cn, to.getIdAlmacen(), to.getIdTipo()));
            }
            String strSQL = "INSERT INTO movimientosAlmacen (idTipo, idEmpresa, idAlmacen, folio, idComprobante, fecha, idReferencia, referencia, idUsuario, estatus, propietario) "
                    + "VALUES (" + to.getIdTipo() + ", " + to.getIdEmpresa() + ", " + to.getIdAlmacen() + ", " + to.getFolio() + ", " + to.getIdComprobante() + ", GETDATE(), " + to.getIdReferencia() + ", " + to.getReferencia() + ", " + to.getIdUsuario() + ", " + to.getEstatus() + ", " + to.getPropietario() + ")";
            st.executeUpdate(strSQL);

            ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idMovtoAlmacen");
            if (rs.next()) {
                to.setIdMovtoAlmacen(rs.getInt("idMovtoAlmacen"));
            }
            rs = st.executeQuery("SELECT fecha FROM movimientosAlmacen WHERE idMovtoAlmacen=" + to.getIdMovtoAlmacen());
            if (rs.next()) {
                to.setFecha(new java.util.Date(rs.getTimestamp("fecha").getTime()));
            }
        }
    }

//    ==================================== OFICINA ===================================
    public static ArrayList<Double> obtenerBoletinSinCargo(Connection cn, int idEmpresa, int idTienda, int idProducto) throws SQLException {
        ArrayList<Double> boletin;
        String strSQL = "SELECT G.idGrupoCte, C.idCliente, F.idFormato, T.idTienda, P.idGrupo, P.idSubGrupo\n"
                + "FROM clientesTiendas T\n"
                + "INNER JOIN clientesFormatos F ON F.idFormato=T.idFormato\n"
                + "INNER JOIN clientesGrupos G ON G.idGrupoCte=F.idGrupoCte\n"
                + "INNER JOIN clientes C ON C.idCliente=T.idCliente\n"
                + "INNER JOIN empaques E ON E.idEmpaque=" + idProducto + "\n"
                + "INNER JOIN productos P ON P.idProducto=E.idProducto\n"
                + "WHERE T.idTienda=" + idTienda;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                int idGrupoCte = rs.getInt("idGrupoCte");
                int idCliente = rs.getInt("idCliente");
                int idFormato = rs.getInt("idFormato");
                int idGrupo = rs.getInt("idGrupo");
                int idSubGrupo = rs.getInt("idSubGrupo");
                boletin = new ArrayList<>();
                boletin.add(0.0);
                boletin.add(0.0);
                strSQL = "SELECT B.* \n"
                        + "FROM clientesBoletinesDetalle B\n"
                        + "INNER JOIN clientesBoletines L ON L.idBoletin=B.idBoletin\n"
                        + "WHERE L.idEmpresa=" + idEmpresa + "\n"
                        + "		AND ((B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=0 AND B.idFormato=0 AND B.idTienda=0)\n"
                        + "			 OR (B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=" + idCliente + " AND B.idFormato=0 AND B.idTienda=0)\n"
                        + "			 OR (B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=" + idCliente + " AND B.idFormato=" + idFormato + " AND B.idTienda=0)\n"
                        + "			 OR (B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=" + idCliente + " AND B.idFormato=" + idFormato + " AND B.idTienda=" + idTienda + "))\n"
                        + "		AND ((B.idGrupo=" + idGrupo + " AND B.idSubGrupo=0 AND B.idEmpaque=0) \n"
                        + "				OR (B.idGrupo=" + idGrupo + " AND B.idSubGrupo=" + idSubGrupo + " AND B.idEmpaque=0) \n"
                        + "				OR (B.idGrupo=" + idGrupo + " AND B.idSubGrupo=" + idSubGrupo + " AND B.idEmpaque=" + idProducto + "))\n"
                        + "		AND CONVERT(date, GETDATE()) BETWEEN B.iniVigencia AND CASE WHEN B.finVigencia='1900-01-01' THEN CONVERT(date, GETDATE()) ELSE B.finVigencia END";
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    if (rs.getDouble("conCargo") > 0 && rs.getDouble("sinCargo") > 0) {
                        boletin.set(0, rs.getDouble("conCargo"));
                        boletin.set(1, rs.getDouble("sinCargo"));
                    }
                }
            } else {
                throw (new SQLException("No se encontro producto id=" + idProducto + " en detalle de tienda id=" + idTienda + " !!!"));
            }
        }
        return boletin;
    }

    private static ArrayList<Double> obtenerPrecioUnitario(Connection cn, int idEmpresa, int idTienda, double desctoComercial, int idProducto) throws SQLException {
        ArrayList<Double> precio = new ArrayList<>();
        double precioUnitario, desctoProducto1, precioLista;
        String strSQL = "SELECT G.idGrupoCte, C.idCliente, F.idFormato, T.idTienda, P.idGrupo, P.idSubGrupo\n"
                + "FROM clientesTiendas T\n"
                + "INNER JOIN clientesFormatos F ON F.idFormato=T.idFormato\n"
                + "INNER JOIN clientesGrupos G ON G.idGrupoCte=F.idGrupoCte\n"
                + "INNER JOIN clientes C ON C.idCliente=T.idCliente\n"
                + "INNER JOIN empaques E ON E.idEmpaque=" + idProducto + "\n"
                + "INNER JOIN productos P ON P.idProducto=E.idProducto\n"
                + "WHERE T.idTienda=" + idTienda;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                int idGrupoCte = rs.getInt("idGrupoCte");
                int idCliente = rs.getInt("idCliente");
                int idFormato = rs.getInt("idFormato");
                int idGrupo = rs.getInt("idGrupo");
                int idSubGrupo = rs.getInt("idSubGrupo");
                strSQL = "SELECT B.*\n"
                        + "FROM clientesListasDetalle B\n"
                        + "INNER JOIN clientesListas L ON L.idClienteLista=B.idClienteLista\n"
                        + "WHERE L.idEmpresa=" + idEmpresa + "\n"
                        + "		AND ((B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=0 AND B.idFormato=0 AND B.idTienda=0)\n"
                        + "			 OR (B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=" + idCliente + " AND B.idFormato=0 AND B.idTienda=0)\n"
                        + "			 OR (B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=" + idCliente + " AND B.idFormato=" + idFormato + " AND B.idTienda=0)\n"
                        + "			 OR (B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=" + idCliente + " AND B.idFormato=" + idFormato + " AND B.idTienda=" + idTienda + "))\n"
                        + "		AND ((B.idGrupo=" + idGrupo + " AND B.idSubGrupo=0 AND B.idEmpaque=0) \n"
                        + "				OR (B.idGrupo=" + idGrupo + " AND B.idSubGrupo=" + idSubGrupo + " AND B.idEmpaque=0) \n"
                        + "				OR (B.idGrupo=" + idGrupo + " AND B.idSubGrupo=" + idSubGrupo + " AND B.idEmpaque=" + idProducto + "))\n"
                        + "		AND CONVERT(date, GETDATE()) BETWEEN B.iniVigencia AND CASE WHEN B.finVigencia='1900-01-01' THEN CONVERT(date, GETDATE()) ELSE B.finVigencia END";
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    if (rs.getDouble("precioVenta") == 0) {
                        throw (new SQLException("El producto id=" + idProducto + ", No tiene precio de lista vigente !!!"));
                    } else {
//                        precioUnitario = rs.getDouble("precioVenta");
                        precioLista = rs.getDouble("precioVenta");
//                        if (!rs.getString("descuentos").equals("")) {
//                            double descuento = 1.00;
//                            for (String str : rs.getString("descuentos").split(",")) {
//                                descuento = descuento * (1 - Double.parseDouble(str) / 100.00);
//                            }
//                            desctoProducto1 = (1.00 - descuento) * 100.00;
//                        } else {
//                            desctoProducto1 = 0.00;
//                        }
//                        precioLista = (precioUnitario / (1 - desctoProducto1 / 100.00));
//                        precioLista = (precioLista / (1 - desctoComercial / 100.00));
                        precioUnitario = (precioLista * (1 - desctoComercial / 100.00));

                        precio.add(precioUnitario);
//                        precio.add(desctoProducto1);
                        precio.add(0.0);
                        precio.add(precioLista);
                    }
                } else {
                    throw (new SQLException("No se encontro precio de venta para el producto id=" + idProducto + " !!!"));
                }
            } else {
                throw new SQLException("No se encotro el detalle de la tienda id=" + idTienda + " para obtener precio del producto id=" + idProducto + " !!!");
            }
        }
        return precio;
    }

    public static void actualizaProductoPrecio(Connection cn, TOMovimientoOficina toMov, TOProductoOficina toProd) throws SQLException {
        ArrayList<Double> precio = obtenerPrecioUnitario(cn, toMov.getIdEmpresa(), toMov.getIdReferencia(), toMov.getDesctoComercial(), toProd.getIdProducto());
        toProd.setUnitario((double) Math.round(precio.get(0) * 1000000) / 1000000);
        toProd.setDesctoProducto1((double) Math.round(precio.get(1) * 1000000000) / 1000000000);
        toProd.setCosto((double) Math.round(precio.get(2) * 1000000) / 1000000);
        grabaProductoCambios(cn, toProd);
//        calculaUnitario(cn, toMov.getIdMovto(), toProd.getIdProducto());
    }

    public static double sumaPiezasOficina(ArrayList<ProductoOficina> detalle) {
        double piezas = 0;
        for (ProductoOficina p : detalle) {
            piezas += (p.getCantFacturada() + p.getCantSinCargo());
        }
        return piezas;
    }

    public static void restaTotales(ProductoOficina prod, MovimientoOficina mov) {
        double resta;
        resta = prod.getCosto() * prod.getCantFacturada();
        mov.setSubTotal(mov.getSubTotal() - Math.round(resta * 1000000.00) / 1000000.00);

        resta = prod.getCosto() - prod.getUnitario();
        resta = resta * prod.getCantFacturada();
        mov.setDescuento(mov.getDescuento() - Math.round(resta * 1000000.00) / 1000000.00);

        resta = prod.getNeto() - prod.getUnitario();
        resta = resta * prod.getCantFacturada();
        mov.setImpuesto(mov.getImpuesto() - Math.round(resta * 1000000.00) / 1000000.00);

        resta = prod.getNeto() * prod.getCantFacturada();
        mov.setTotal(mov.getTotal() - Math.round(resta * 1000000.00) / 1000000.00);
    }

    public static void sumaTotales(ProductoOficina prod, MovimientoOficina mov) {
        double suma;
        suma = prod.getCosto() * prod.getCantFacturada();   // Calcula el subTotal
        mov.setSubTotal(mov.getSubTotal() + Math.round(suma * 1000000.00) / 1000000.00);    // Suma el importe el subtotal

        suma = prod.getCosto() - prod.getUnitario();   // Obtiene el descuento por diferencia.
        suma = suma * prod.getCantFacturada();                           // Calcula el importe de descuento
        mov.setDescuento(mov.getDescuento() + Math.round(suma * 1000000.00) / 1000000.00);  // Suma el descuento

        suma = prod.getNeto() - prod.getUnitario();     // Obtiene el impuesto por diferencia
        suma = suma * prod.getCantFacturada();                           // Calcula el importe de impuestos
        mov.setImpuesto(mov.getImpuesto() + Math.round(suma * 1000000.00) / 1000000.00);    // Suma los impuestos

        suma = prod.getNeto() * prod.getCantFacturada(); // Calcula el importe total
        mov.setTotal(mov.getTotal() + Math.round(suma * 1000000.00) / 1000000.00);          // Suma el importe al total
    }

    public static void liberar(Connection cn, TOMovimientoOficina toMov, int idProducto, double cantSolicitada) throws SQLException {
        String lote;
        double cantLiberar;
        double cantLiberada = 0;
        String strSQL = "SELECT D.lote, D.cantidad, ISNULL(A.separados, 0) AS separados\n"
                + "FROM movimientosDetalleAlmacen D\n"
                + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                + "LEFT JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                + "WHERE D.idMovtoAlmacen=" + toMov.getIdMovtoAlmacen() + " AND D.idEmpaque=" + idProducto + "\n"
                + "ORDER BY A.fechaCaducidad DESC";
        try (Statement st = cn.createStatement(); Statement st1 = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                lote = rs.getString("lote");
                if (rs.getDouble("separados") < rs.getDouble("cantidad")) {
                    throw new SQLException("Inconsistencia: lote separados menor que producto cantidad. Producto (id=" + idProducto + "), lote: " + lote + " !!!");
                }
                cantLiberar = cantSolicitada;
                if (rs.getDouble("cantidad") <= cantSolicitada) {
                    cantLiberar = rs.getDouble("cantidad");

                    strSQL = "DELETE FROM movimientosDetalleAlmacen\n"
                            + "WHERE idMovtoAlmacen=" + toMov.getIdMovtoAlmacen() + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                } else {
                    strSQL = "UPDATE movimientosDetalleAlmacen\n"
                            + "SET cantidad=cantidad-" + cantLiberar + "\n"
                            + "WHERE idMovtoAlmacen=" + toMov.getIdMovtoAlmacen() + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                }
                st1.executeUpdate(strSQL);

                strSQL = "UPDATE almacenesLotes\n"
                        + "SET separados=separados-" + cantLiberar + "\n"
                        + "WHERE idAlmacen=" + toMov.getIdAlmacen() + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                st1.executeUpdate(strSQL);

                cantSolicitada -= cantLiberar;
                cantLiberada += cantLiberar;
                if (cantSolicitada == 0) {
                    break;
                }
            }
            if (cantSolicitada != 0) {
                throw new SQLException("Inconsistencia de separados para liberar producto (id=" + idProducto + ") almacen !!!");
            }
            strSQL = "SELECT separados\n"
                    + "FROM almacenesEmpaques\n"
                    + "WHERE idAlmacen=" + toMov.getIdAlmacen() + " AND idEmpaque=" + idProducto;
            rs = st.executeQuery(strSQL);
            if (rs.next()) {
                if (rs.getDouble("separados") < cantLiberada) {
                    throw new SQLException("Inconsistencia de separados para liberar en el producto (id=" + idProducto + ") oficina !!!");
                }
                strSQL = "UPDATE almacenesEmpaques\n"
                        + "SET separados=separados-" + cantLiberada + "\n"
                        + "WHERE idAlmacen=" + toMov.getIdAlmacen() + " AND idEmpaque=" + idProducto;
                st1.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosDetalle\n"
                        + "SET cantFacturada=cantFacturada-" + cantLiberada + "\n"
                        + "WHERE idMovto=" + toMov.getIdMovto() + " AND idEmpaque=" + idProducto;
                st.executeUpdate(strSQL);
            } else {
                throw new SQLException("No se encontro producto (id=" + idProducto + ") en almacenesEmpaques !!!");
            }
        }
    }

    public static double separar(Connection cn, TOMovimientoOficina toMov, int idProducto, double cantSolicitada, boolean total) throws SQLException, Exception {
        int n;
        String lote;
        double disponibles, cantSeparar;
        String strSQL = "SELECT E.idAlmacen, E.idEmpaque\n"
                + "     , E.existencia-E.separados AS disponiblesOficina\n"
                + "     , L.disponibles AS disponiblesAlmacen\n"
                + "FROM (SELECT idAlmacen, idEmpaque, SUM(existencia-separados) AS disponibles\n"
                + "		FROM almacenesLotes\n"
                + "		WHERE idAlmacen=" + toMov.getIdAlmacen() + " AND idEmpaque=" + idProducto + "\n"
                + "		GROUP BY idAlmacen, idEmpaque) L\n"
                + "INNER JOIN almacenesEmpaques E ON E.idAlmacen=L.idAlmacen AND E.idEmpaque=L.idEmpaque";
        try (Statement st = cn.createStatement(); Statement st1 = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                disponibles = rs.getDouble("disponiblesOficina");
                if (rs.getDouble("disponiblesAlmacen") < disponibles) {
                    disponibles = rs.getDouble("disponiblesAlmacen");
                }
            } else {
                disponibles = 0;
            }
            if (disponibles != 0) {
                if (disponibles < cantSolicitada) {
                    if (total) {
                        throw new Exception("No hay existencia total disponible !!!");
                    }
                    cantSolicitada = disponibles;
                }
                strSQL = "UPDATE almacenesEmpaques\n"
                        + "SET separados=separados+" + cantSolicitada + "\n"
                        + "WHERE idAlmacen=" + toMov.getIdAlmacen() + " AND idEmpaque=" + idProducto;
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosDetalle\n"
                        + "SET cantFacturada=cantFacturada+" + cantSolicitada + "\n"
                        + "WHERE idMovto=" + toMov.getIdMovto() + " AND idEmpaque=" + idProducto;
                st.executeUpdate(strSQL);

                strSQL = "SELECT lote, existencia-separados AS disponibles\n"
                        + "FROM almacenesLotes\n"
                        + "WHERE idAlmacen=" + toMov.getIdAlmacen() + " AND idEmpaque=" + idProducto + " AND existencia > separados\n"
                        + "ORDER BY fechaCaducidad";
                rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    lote = rs.getString("lote");
                    cantSeparar = rs.getDouble("disponibles");
                    if (cantSolicitada < cantSeparar) {
                        cantSeparar = cantSolicitada;
                    }
                    strSQL = "UPDATE almacenesLotes\n"
                            + "SET separados=separados+" + cantSeparar + "\n"
                            + "WHERE idAlmacen=" + toMov.getIdAlmacen() + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                    st1.executeUpdate(strSQL);

                    strSQL = "UPDATE movimientosDetalleAlmacen\n"
                            + "SET cantidad=cantidad+" + cantSeparar + "\n"
                            + "WHERE idMovtoAlmacen=" + toMov.getIdMovtoAlmacen() + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                    n = st1.executeUpdate(strSQL);
                    if (n == 0) {
                        strSQL = "INSERT INTO movimientosDetalleAlmacen (idMovtoAlmacen, idEmpaque, lote, cantidad, fecha, existenciaAnterior)\n"
                                + "VALUES (" + toMov.getIdMovtoAlmacen() + ", " + idProducto + ", '" + lote + "', " + cantSeparar + ", '', 0)";
                        st1.executeUpdate(strSQL);
                    }
                    cantSolicitada -= cantSeparar;
                    if (cantSolicitada == 0) {
                        break;
                    }
                }
            } else if (total) {
                throw new Exception("No hay existencia !!!");
            }
        }
        return disponibles;
    }

    public static void convertir(TOMovimientoOficina toMov, MovimientoOficina mov) {
        mov.setIdMovto(toMov.getIdMovto());
        mov.setFolio(toMov.getFolio());
        mov.setDesctoComercial(toMov.getDesctoComercial());
        mov.setDesctoProntoPago(toMov.getDesctoProntoPago());
        mov.setFecha(toMov.getFecha());
        mov.setIdUsuario(toMov.getIdUsuario());
        mov.setTipoDeCambio(toMov.getTipoDeCambio());
        mov.setPropietario(toMov.getPropietario());
        mov.setEstatus(toMov.getEstatus());
        mov.setIdMovtoAlmacen(toMov.getIdMovtoAlmacen());
    }

    public static void convertir(MovimientoOficina mov, TOMovimientoOficina toMov) {
        toMov.setIdMovto(mov.getIdMovto());
        toMov.setIdEmpresa(mov.getAlmacen().getIdEmpresa());
        toMov.setIdAlmacen(mov.getAlmacen().getIdAlmacen());
        toMov.setIdTipo(mov.getTipo().getIdTipo());
        toMov.setFolio(mov.getFolio());
        toMov.setDesctoComercial(mov.getDesctoComercial());
        toMov.setDesctoProntoPago(mov.getDesctoProntoPago());
        toMov.setFecha(mov.getFecha());
        toMov.setIdUsuario(mov.getIdUsuario());
        toMov.setTipoDeCambio(mov.getTipoDeCambio());
        toMov.setPropietario(mov.getPropietario());
        toMov.setEstatus(mov.getEstatus());
        toMov.setIdMovtoAlmacen(mov.getIdMovtoAlmacen());
    }

    public static void convertir(TOProductoOficina toProd, ProductoOficina prod) {
        prod.setIdMovto(toProd.getIdMovto());
        prod.setCantFacturada(toProd.getCantFacturada());
        prod.setCantSinCargo(toProd.getCantSinCargo());
        prod.setSeparados(toProd.getCantFacturada() + toProd.getCantSinCargo());
        prod.setCostoPromedio(toProd.getCostoPromedio());
        prod.setCosto(toProd.getCosto());
        prod.setDesctoProducto1(toProd.getDesctoProducto1());
        prod.setDesctoProducto2(toProd.getDesctoProducto2());
        prod.setDesctoConfidencial(toProd.getDesctoConfidencial());
        prod.setUnitario(toProd.getUnitario());
        prod.setImporte(toProd.getUnitario() * toProd.getCantFacturada());
    }

    public static void convertir(ProductoOficina prod, TOProductoOficina toProd) {
        toProd.setIdMovto(prod.getIdMovto());
        toProd.setIdProducto(prod.getProducto().getIdProducto());
        toProd.setCantFacturada(prod.getCantFacturada());
        toProd.setCantSinCargo(prod.getCantSinCargo());
        toProd.setCostoPromedio(prod.getCostoPromedio());
        toProd.setCosto(prod.getCosto());
        toProd.setDesctoProducto1(prod.getDesctoProducto1());
        toProd.setDesctoProducto2(prod.getDesctoProducto2());
        toProd.setDesctoConfidencial(prod.getDesctoConfidencial());
        toProd.setUnitario(prod.getUnitario());
        toProd.setIdImpuestoGrupo(prod.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());
    }

    public static TOProductoOficina construirProductoOficina(ResultSet rs) throws SQLException {
        TOProductoOficina to = new TOProductoOficina();
        construirProductoOficina(rs, to);
        return to;
    }

    public static void construirProductoOficina(ResultSet rs, TOProductoOficina to) throws SQLException {
        to.setIdMovto(rs.getInt("idMovto"));
        to.setIdProducto(rs.getInt("idEmpaque"));
        to.setCantFacturada(rs.getDouble("cantFacturada"));
        to.setCantSinCargo(rs.getDouble("cantSinCargo"));
        to.setCostoPromedio(rs.getDouble("costoPromedio"));
        to.setCosto(rs.getDouble("costo"));
        to.setDesctoProducto1(rs.getDouble("desctoProducto1"));
        to.setDesctoProducto2(rs.getDouble("desctoProducto2"));
        to.setDesctoConfidencial(rs.getDouble("desctoConfidencial"));
        to.setUnitario(rs.getDouble("unitario"));
        to.setIdImpuestoGrupo(rs.getInt("idImpuestoGrupo"));
    }

    public static void eliminaProductoOficina(Connection cn, int idMovto, int idProducto) throws SQLException {
        try (Statement st = cn.createStatement()) {
            String strSQL = "DELETE D\n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "WHERE D.idMovto=" + idMovto + " AND D.idEmpaque=" + idProducto + " AND M.referencia=0";
            st.executeUpdate(strSQL);

            strSQL = "DELETE D\n"
                    + "FROM movimientosDetalleImpuestos D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "WHERE D.idMovto=" + idMovto + " AND D.idEmpaque=" + idProducto + " AND M.referencia=0";
            st.executeUpdate(strSQL);
        }
    }

    public static void liberarMovimientoAlmacen(Connection cn, int idMovtoAlmacen, int idUsuario) throws SQLException {
        String strSQL = "SELECT propietario FROM movimientosAlmacen WHERE idMovtoAlmacen=" + idMovtoAlmacen;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                if (rs.getInt("propietario") == idUsuario) {
                    strSQL = "UPDATE movimientosAlmacen SET propietario=0 WHERE idMovtoAlmacen=" + idMovtoAlmacen;
                    st.executeUpdate(strSQL);
                }
            }
        }
    }

    public static void bloquearMovimientoAlmacen(Connection cn, TOMovimientoAlmacen toMov, int idUsuario) throws SQLException {
        toMov.setIdUsuario(idUsuario);
        String strSQL = "SELECT propietario, estatus FROM movimientosAlmacen WHERE idMovtoAlmacen=" + toMov.getIdMovtoAlmacen();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                toMov.setEstatus(rs.getInt("estatus"));
                int propietario = rs.getInt("propietario");
                if (propietario == 0) {
                    strSQL = "UPDATE movimientosAlmacen SET propietario=" + idUsuario + " WHERE idMovtoAlmacen=" + toMov.getIdMovtoAlmacen();
                    st.executeUpdate(strSQL);
                    toMov.setPropietario(idUsuario);
                } else {
                    toMov.setPropietario(propietario);
                }
            } else {
                toMov.setPropietario(0);
            }
        }
    }

    public static void actualizaDetalleAlmacen(Connection cn, int idMovtoAlmacen, boolean suma) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            strSQL = "DELETE FROM movimientosDetalleAlmacen WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND cantidad=0";
            st.executeUpdate(strSQL);

            if (suma) {
                strSQL = "INSERT INTO almacenesLotes\n"
                        + "SELECT M.idAlmacen, D.idEmpaque, D.lote, DATEADD(DAY, 365, L.fecha), 0, 0, 0\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN lotes L ON L.lote=SUBSTRING(D.lote, 1, 4)\n"
                        + "LEFT JOIN almacenesLotes AL ON AL.idAlmacen=M.idAlmacen AND AL.idEmpaque=D.idEmpaque AND AL.lote=D.lote\n"
                        + "WHERE M.idMovtoAlmacen=" + idMovtoAlmacen + " AND AL.idAlmacen IS NULL";
                st.executeUpdate(strSQL);
            }
            strSQL = "UPDATE D\n"
                    + "SET fecha=GETDATE(), existenciaAnterior=A.existencia\n"
                    + "FROM movimientosDetalleAlmacen D\n"
                    + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                    + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                    + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen;
            st.executeUpdate(strSQL);

            strSQL = "UPDATE A\n"
                    + "SET existencia=A.existencia" + (suma ? "+" : "-") + "D.cantidad\n"
                    + "     , separados=A.separados" + (suma ? "" : "-D.cantidad") + "\n"
                    + "FROM movimientosDetalleAlmacen D\n"
                    + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                    + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                    + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen;
            st.executeUpdate(strSQL);
        }
    }

    public static void bloquearMovimientoOficina(Connection cn, TOMovimientoOficina toMov, int idUsuario) throws SQLException {
        toMov.setIdUsuario(idUsuario);
        String strSQL = "SELECT propietario, estatus FROM movimientos WHERE idMovto=" + toMov.getIdMovto();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                toMov.setEstatus(rs.getInt("estatus"));
                int propietario = rs.getInt("propietario");
                if (propietario == 0) {
                    strSQL = "UPDATE movimientos SET propietario=" + idUsuario + " WHERE idMovto=" + toMov.getIdMovto();
                    st.executeUpdate(strSQL);
                    toMov.setPropietario(idUsuario);
                } else {
                    toMov.setPropietario(propietario);
                }
            } else {
                toMov.setPropietario(0);
            }
        }
    }

    public static void liberarMovimientoOficina(Connection cn, int idMovto, int idUsuario) throws SQLException {
        String strSQL = "SELECT propietario FROM movimientos WHERE idMovto=" + idMovto;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                if (rs.getInt("propietario") == idUsuario) {
                    strSQL = "UPDATE movimientos SET propietario=0 WHERE idMovto=" + idMovto;
                    st.executeUpdate(strSQL);
                }
            }
        }
    }

    public static void actualizaDetalleOficina(Connection cn, int idMovto, int idTipo, boolean suma) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            ResultSet rs;
            boolean borra = true;
            if (idTipo == 28) {
                rs = st.executeQuery("SELECT referencia FROM movimientos WHERE idMovto=" + idMovto);
                if (rs.next()) {
                    if (rs.getInt("referencia") != 0) {
                        borra = false;
                    }
                }
            }
            if (borra) {
                strSQL = "DELETE I\n"
                        + "FROM movimientosDetalleImpuestos I\n"
                        + "INNER JOIN movimientosDetalle D ON D.idMovto=I.idMovto AND D.idEmpaque=I.idEmpaque\n"
                        + "WHERE I.idMovto=" + idMovto + " AND D.cantFacturada+D.cantSinCargo=0";
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalle WHERE idMovto=" + idMovto + " AND cantFacturada+cantSinCargo=0";
                st.executeUpdate(strSQL);
            }
            if (suma) {
                strSQL = "INSERT INTO empresasEmpaques (idEmpresa, idEmpaque, costoUnitarioPromedio, existencia, idMovtoUltimaCompra)\n"
                        + "SELECT M.idEmpresa, D.idEmpaque, 0, 0, 0\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "LEFT JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + idMovto + " AND E.idEmpresa IS NULL";
                st.executeUpdate(strSQL);

                strSQL = "INSERT INTO almacenesEmpaques (idAlmacen, idEmpaque, existencia, separados, existenciaMinima, existenciaMaxima)\n"
                        + "SELECT M.idAlmacen, D.idEmpaque, 0, 0, 0, 0\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "LEFT JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + idMovto + " AND A.idAlmacen IS NULL";
                st.executeUpdate(strSQL);

                if (idTipo == 1) { // Compra con o sin orden de compra
                    strSQL = "UPDATE D\n"
                            + "SET costoPromedio=ROUND(D.unitario*D.cantFacturada/(D.cantFacturada+D.cantSinCargo), 6)\n"
                            + "FROM movimientosDetalle D\n"
                            + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                            + "LEFT JOIN ordenCompraSurtido S ON S.idOrdenCompra=M.referencia AND S.idEmpaque=D.idEmpaque\n"
                            + "WHERE D.idMovto=" + idMovto;
                    st.executeUpdate(strSQL);

                    strSQL = "INSERT INTO proveedoresProductos (idEmpresa, idProveedor, idEmpaque, sku, idUnidadEmpaque, piezas, idMarca, idParte, descripcion, idPresentacion, contenido, idUnidadMedida, idUnidadMedida2, idImpuestosGrupo, diasEntrega, idMovtoUltimaCompra)\n"
                            + "SELECT M.idEmpresa, M.idReferencia, D.idEmpaque, '', 0, 0, 0, 0, '', 0, 0, 0, 0, 0, 0, 0\n"
                            + "FROM movimientosDetalle D\n"
                            + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                            + "LEFT JOIN proveedoresProductos P ON P.idEmpresa=M.idEmpresa AND P.idProveedor=M.idReferencia AND P.idEmpaque=D.idEmpaque\n"
                            + "WHERE D.idMovto=" + idMovto + " AND P.idEmpresa IS NULL";
                    st.executeUpdate(strSQL);

                    strSQL = "UPDATE P\n"
                            + "SET idMovtoUltimaCompra=M.idMovto\n"
                            + "FROM movimientosDetalle D\n"
                            + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                            + "INNER JOIN proveedoresProductos P ON P.idEmpresa=M.idEmpresa AND P.idProveedor=M.idReferencia AND P.idEmpaque=D.idEmpaque\n"
                            + "WHERE D.idMovto=" + idMovto;
                    st.executeUpdate(strSQL);
                } else if (idTipo == 3 || idTipo == 18) {    // Entrada de producto terminado y semiterminado
                    strSQL = "UPDATE MD\n" // Toma el costo de la formula
                            + "SET MD.costoPromedio=F.costoUnitarioPromedio, MD.costo=F.costoUnitarioPromedio, MD.unitario=F.costoUnitarioPromedio\n"
                            + "FROM movimientosDetalle MD\n"
                            + "INNER JOIN movimientos M ON M.idMovto=MD.idMovto\n"
                            + "INNER JOIN formulas F ON F.idEmpresa=M.idEmpresa AND F.idEmpaque=MD.idEmpaque\n"
                            + "WHERE MD.idMovto=" + idMovto;
                    st.executeUpdate(strSQL);
                } else if (idTipo != 9) { // Las recepciones ya traen el costo del traspaso
                    // Todos los demas movimientos, se actualizan con el costo promedio de la empresa
                    strSQL = "UPDATE D\n"
                            + "SET costoPromedio=E.costoUnitarioPromedio, costo=E.costoUnitarioPromedio, unitario=E.costoUnitarioPromedio\n"
                            + "FROM movimientosDetalle D\n"
                            + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                            + "LEFT JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                            + "WHERE D.idMovto=" + idMovto;
                    st.executeUpdate(strSQL);
                }
                strSQL = "SELECT E.cod_pro\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN empaques E ON E.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + idMovto + " AND D.costo=0";
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    throw new SQLException("El empaque (sku=" + rs.getInt("cod_pro") + ") no tiene costo !!!");
                }
                strSQL = "UPDATE E\n"
                        + "SET costoUnitarioPromedio=ROUND(((E.costoUnitarioPromedio*E.existencia + D.costoPromedio*(D.cantFacturada+D.cantSinCargo))/(E.existencia+D.cantFacturada+D.cantSinCargo)),6)\n"
                        + "	, idMovtoUltimaCompra=CASE WHEN M.idTipo=1 THEN M.idMovto ELSE E.idMovtoUltimaCompra END\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + idMovto;
                st.executeUpdate(strSQL);
            } else {
                if (idTipo == 28) { // Venta
                    strSQL = "UPDATE D\n"
                            + "SET D.costoPromedio=E.costoUnitarioPromedio\n"
                            + "FROM movimientosDetalle D\n"
                            + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                            + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                            + "WHERE D.idMovto=" + idMovto;
                    st.executeUpdate(strSQL);
                } else if (idTipo == 34) { // Cancelacion de compra
                    strSQL = "UPDATE E\n"
                            + "SET costoUnitarioPromedio=ROUND(((E.costoUnitarioPromedio*E.existencia - D.costoPromedio*(D.cantFacturada+D.cantSinCargo))/(E.existencia-D.cantFacturada-D.cantSinCargo)),6)\n"
                            + "	, idMovtoUltimaCompra=CASE WHEN M.idTipo=1 THEN M.idMovto ELSE E.idMovtoUltimaCompra END\n"
                            + "FROM movimientosDetalle D\n"
                            + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                            + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                            + "WHERE D.idMovto=" + idMovto;
                    st.executeUpdate(strSQL);
                } else { // Traspaso y todos los demas
                    strSQL = "UPDATE D\n"
                            + "SET costoPromedio=EE.costoUnitarioPromedio, costo=EE.costoUnitarioPromedio, unitario=EE.costoUnitarioPromedio\n"
                            + "FROM movimientosDetalle D\n"
                            + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                            + "INNER JOIN empresasEmpaques EE ON EE.idEmpresa=M.idEmpresa AND EE.idEmpaque=D.idEmpaque\n"
                            + "WHERE D.idMovto=" + idMovto;
                    st.executeUpdate(strSQL);
                }
            }
            strSQL = "UPDATE D\n"
                    + "SET fecha=GETDATE(), existenciaAnterior=A.existencia\n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                    + "WHERE D.idMovto=" + idMovto;
            st.executeUpdate(strSQL);

            strSQL = "UPDATE A\n"
                    + "SET existencia=A.existencia" + (suma ? "+" : "-") + "(D.cantFacturada+D.cantSinCargo)\n"
                    + "     , separados=A.separados" + (suma ? "" : "-(D.cantFacturada+D.cantSinCargo)") + "\n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                    + "WHERE D.idMovto=" + idMovto;
            st.executeUpdate(strSQL);

            strSQL = "UPDATE E\n"
                    + "SET E.existencia=E.existencia" + (suma ? "+" : "-") + "(D.cantFacturada+D.cantSinCargo)\n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                    + "WHERE D.idMovto=" + idMovto;
            st.executeUpdate(strSQL);

            if (!suma) {
                strSQL = "UPDATE E\n"
                        + "SET E.costoUnitarioPromedio=0\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + idMovto + " AND E.existencia=0";
                st.executeUpdate(strSQL);
            }
        }
    }

    public static ArrayList<ImpuestosProducto> obtenerImpuestosProducto(Connection cn, int idImpuestoGrupo, int idZona) throws SQLException {
        ArrayList<ImpuestosProducto> impuestos = new ArrayList<>();
        String strSQL = "SELECT id.idImpuesto, i.impuesto, id.valor, i.aplicable, i.modo, i.acreditable, 0.00 as importe, i.acumulable\n"
                + "FROM impuestosDetalle id\n"
                + "INNER JOIN impuestos i ON i.idImpuesto=id.idImpuesto\n"
                + "WHERE id.idGrupo=" + idImpuestoGrupo + " and id.idZona=" + idZona + " and GETDATE() between fechaInicial and fechaFinal";
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                impuestos.add(construirImpuestosProducto(rs));
            }
        }
        return impuestos;
    }

    public static ImpuestosProducto construirImpuestosProducto(ResultSet rs) throws SQLException {
        ImpuestosProducto ip = new ImpuestosProducto();
        ip.setIdImpuesto(rs.getInt("idImpuesto"));
        ip.setImpuesto(rs.getString("impuesto"));
        ip.setValor(rs.getDouble("valor"));
        ip.setAplicable(rs.getBoolean("aplicable"));
        ip.setModo(rs.getInt("modo"));
        ip.setAcreditable(rs.getBoolean("acreditable"));
        ip.setImporte(rs.getDouble("importe"));
        ip.setAcumulable(rs.getBoolean("acumulable"));
        return ip;
    }

    public static double obtenImpuestosProducto(Connection cn, int idMovto, int idEmpaque, ArrayList<ImpuestosProducto> impuestos) throws SQLException {
        double importeImpuestos = 0;
        ImpuestosProducto impuesto;
        String strSQL = "select idImpuesto, impuesto, valor, aplicable, modo, acreditable, importe, acumulable\n"
                + "from movimientosDetalleImpuestos\n"
                + "where idMovto=" + idMovto + " and idEmpaque=" + idEmpaque + "\n"
                + "order by acumulable";
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                impuesto = construirImpuestosProducto(rs);
                importeImpuestos += impuesto.getImporte();
                impuestos.add(impuesto);
            }
        }
        return importeImpuestos;
    }

    public static void calculaUnitario(Connection cn, int idMovto, int idEmpaque) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            strSQL = "UPDATE D\n"
                    + "SET unitario=ROUND(D.costo*(1-D.desctoProducto1/100)*(1-D.desctoProducto2/100)*(1-D.desctoConfidencial/100)*(1-M.desctoComercial/100), 6)\n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "WHERE D.idMovto=" + idMovto + (idEmpaque == 0 ? "" : " AND D.idEmpaque=" + idEmpaque);
            st.executeUpdate(strSQL);

            strSQL = "UPDATE DI\n"
                    + "SET importe=CASE WHEN DI.aplicable=0 THEN 0 WHEN DI.modo=1 THEN D.unitario*DI.valor/100.00 ELSE E.piezas*DI.valor END\n"
                    + "FROM movimientosDetalleImpuestos DI\n"
                    + "INNER JOIN empaques E ON E.idEmpaque=DI.idEmpaque\n"
                    + "INNER JOIN movimientosDetalle D ON D.idMovto=DI.idMovto AND D.idEmpaque=DI.idEmpaque\n"
                    + "WHERE DI.idMovto=" + idMovto + (idEmpaque == 0 ? "" : " AND D.idEmpaque=" + idEmpaque) + " AND DI.acumulable=1";
            st.executeUpdate(strSQL);

            strSQL = "UPDATE DI\n"
                    + "SET importe=CASE WHEN DI.aplicable=0 THEN 0 WHEN DI.modo=1 THEN (D.unitario+ISNULL(A.acumulable, 0))*DI.valor/100.00 ELSE E.piezas*DI.valor END\n"
                    + "FROM movimientosDetalleImpuestos DI\n"
                    + "INNER JOIN empaques E on E.idEmpaque=DI.idEmpaque\n"
                    + "INNER JOIN movimientosDetalle D ON D.idMovto=DI.idMovto AND D.idEmpaque=DI.idEmpaque\n"
                    + "LEFT JOIN (SELECT idMovto, idEmpaque, SUM(importe) AS acumulable\n"
                    + "           FROM movimientosDetalleImpuestos\n"
                    + "           WHERE idMovto=" + idMovto + " AND acumulable=1\n"
                    + "           GROUP BY idMovto, idEmpaque) A ON A.idMovto=DI.idMovto AND A.idEmpaque=DI.idEmpaque\n"
                    + "WHERE DI.idMovto=" + idMovto + (idEmpaque == 0 ? "" : " AND D.idEmpaque=" + idEmpaque) + " AND DI.acumulable=0";
            st.executeUpdate(strSQL);
        }
    }

    public static double grabaProductoCambios(Connection cn, TOProductoOficina toProd) throws SQLException {
        String strSQL;
        double unitario = 0;
        try (Statement st = cn.createStatement()) {
            strSQL = "UPDATE movimientosDetalle\n"
                    + "SET costo=" + toProd.getCosto() + ", desctoProducto1=" + toProd.getDesctoProducto1() + "\n"
                    + "     , desctoProducto2=" + toProd.getDesctoProducto2() + ", desctoConfidencial=" + toProd.getDesctoConfidencial() + "\n"
                    + "WHERE idMovto=" + toProd.getIdMovto() + " AND idEmpaque=" + toProd.getIdProducto();
            st.executeUpdate(strSQL);
        }
        calculaUnitario(cn, toProd.getIdMovto(), toProd.getIdProducto());

        try (Statement st = cn.createStatement()) {
            strSQL = "SELECT unitario FROM movimientosDetalle WHERE idMovto=" + toProd.getIdMovto() + " AND idEmpaque=" + toProd.getIdProducto();
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                unitario = rs.getDouble("unitario");
            }
        }
        return unitario;
    }

    public static void grabaProductoCantidad(Connection cn, TOProductoOficina toProd) throws SQLException {
        String strSQL = "UPDATE movimientosDetalle\n"
                + "SET cantFacturada=" + toProd.getCantFacturada() + ", cantSinCargo=" + toProd.getCantSinCargo() + "\n"
                + "WHERE idMovto=" + toProd.getIdMovto() + " AND idEmpaque=" + toProd.getIdProducto();
        try (Statement st = cn.createStatement()) {
            st.executeUpdate(strSQL);
        }
    }

    public static void agregaProductoOficina(Connection cn, TOProductoOficina to, int idZonaImpuestos) throws SQLException {
        String strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantFacturada, cantSinCargo, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior) "
                + "VALUES (" + to.getIdMovto() + ", " + to.getIdProducto() + ", " + to.getCantFacturada() + ", " + to.getCantSinCargo() + ", " + to.getCostoPromedio() + ", " + to.getCosto() + ", " + to.getDesctoProducto1() + ", " + to.getDesctoProducto2() + ", " + to.getDesctoConfidencial() + ", " + to.getUnitario() + ", " + to.getIdImpuestoGrupo() + ", '', 0)";
        try (Statement st = cn.createStatement()) {
            st.executeUpdate(strSQL);

            if (idZonaImpuestos != 0) {
                strSQL = "INSERT INTO movimientosDetalleImpuestos (idMovto, idEmpaque, idImpuesto, impuesto, valor, aplicable, modo, acreditable, importe, acumulable) "
                        + "SELECT " + to.getIdMovto() + ", " + to.getIdProducto() + ", ID.idImpuesto, I.impuesto, ID.valor, I.aplicable, I.modo, I.acreditable, 0.00 as importe, I.acumulable "
                        + "FROM impuestosDetalle ID "
                        + "INNER JOIN impuestos I on I.idImpuesto=ID.idImpuesto "
                        + "WHERE ID.idGrupo=" + to.getIdImpuestoGrupo() + " AND ID.idZona=" + idZonaImpuestos + " AND GETDATE() BETWEEN ID.fechaInicial AND ID.fechaFinal";
                if (st.executeUpdate(strSQL) == 0) {
                    throw (new SQLException("No se insertaron impuestos !!!"));
                }
            }
        }
    }

    public static double obtenCostoUltimaCompraProveedor(Connection cn, int idEmpresa, int idProveedor, int idEmpaque) throws SQLException {
        double precioLista = 0;
        try (Statement st = cn.createStatement()) {
            String strSQL = "SELECT top 1 D.costo\n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "WHERE M.idEmpresa=" + idEmpresa + " AND M.idTipo=1 AND M.idReferencia=" + idProveedor + " AND M.estatus=5 AND D.idEmpaque=" + idEmpaque + "\n"
                    + "ORDER BY D.fecha DESC";
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                precioLista = rs.getDouble("costo");
            }
        }
        return precioLista;
    }

    public static void grabaMovimientoAlmacen(Connection cn, TOMovimientoAlmacen toMov) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            strSQL = "UPDATE movimientosAlmacen\n"
                    + "SET folio=" + toMov.getFolio() + ", fecha=GETDATE(), idUsuario=" + toMov.getIdUsuario() + ", estatus=" + toMov.getEstatus() + "\n"
                    + "WHERE idMovtoAlmacen=" + toMov.getIdMovtoAlmacen();
            st.executeUpdate(strSQL);

            strSQL = "SELECT fecha FROM movimientosAlmacen WHERE idMovtoAlmacen=" + toMov.getIdMovtoAlmacen();
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                toMov.setFecha(new java.util.Date(rs.getTimestamp("fecha").getTime()));
            }
        }
    }

    public static void grabaMovimientoOficina(Connection cn, TOMovimientoOficina toMov) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            strSQL = "UPDATE movimientos\n"
                    + "SET folio=" + toMov.getFolio() + ", desctoComercial=" + toMov.getDesctoComercial() + "\n"
                    + "     , desctoProntoPago=" + toMov.getDesctoProntoPago() + ", tipoDeCambio=" + toMov.getTipoDeCambio() + "\n"
                    + "     , fecha=GETDATE(), idUsuario=" + toMov.getIdUsuario() + ", estatus=" + toMov.getEstatus() + "\n"
                    + "WHERE idMovto=" + toMov.getIdMovto();
            st.executeUpdate(strSQL);

            strSQL = "SELECT fecha FROM movimientos WHERE idMovto=" + toMov.getIdMovto();
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                toMov.setFecha(new java.util.Date(rs.getTimestamp("fecha").getTime()));
            }
        }
    }

    public static void construirMovimientoOficina(ResultSet rs, TOMovimientoOficina toMov) throws SQLException {
        toMov.setIdMovto(rs.getInt("idMovto"));
        toMov.setIdTipo(rs.getInt("idTipo"));
        toMov.setIdEmpresa(rs.getInt("idEmpresa"));
        toMov.setIdAlmacen(rs.getInt("idAlmacen"));
        toMov.setFolio(rs.getInt("folio"));
        toMov.setIdComprobante(rs.getInt("idComprobante"));
        toMov.setIdImpuestoZona(rs.getInt("idImpuestoZona"));
        toMov.setDesctoComercial(rs.getDouble("desctoComercial"));
        toMov.setDesctoProntoPago(rs.getDouble("desctoprontoPago"));
        toMov.setFecha(new java.util.Date(rs.getTimestamp("fecha").getTime()));
        toMov.setIdUsuario(rs.getInt("idUsuario"));
        toMov.setTipoDeCambio(rs.getDouble("tipoDeCambio"));
        toMov.setIdReferencia(rs.getInt("idReferencia"));
        toMov.setReferencia(rs.getInt("referencia"));
        toMov.setPropietario(rs.getInt("propietario"));
        toMov.setEstatus(rs.getInt("estatus"));
        toMov.setIdMovtoAlmacen(rs.getInt("idMovtoAlmacen"));
    }

    public static TOMovimientoOficina construirMovimientoOficina(ResultSet rs) throws SQLException {
        TOMovimientoOficina toMov = new TOMovimientoOficina();
        construirMovimientoOficina(rs, toMov);
        return toMov;
    }

    public static TOMovimientoOficina obtenMovimientoOficina(Connection cn, int idMovto) throws SQLException {
        String strSQL;
        TOMovimientoOficina toMov = null;
        try (Statement st = cn.createStatement()) {
            strSQL = "SELECT * FROM movimientos WHERE idMovto=" + idMovto;
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                toMov = construirMovimientoOficina(rs);
            }
        }
        return toMov;
    }

    public static int obtenMovimientoFolioOficina(Connection cn, int idAlmacen, int idTipo) throws SQLException {
        int folio;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT folio FROM movimientosFolios WHERE idAlmacen=" + idAlmacen + " AND idTipo=" + idTipo);
            if (rs.next()) {
                folio = rs.getInt("folio");
                st.executeUpdate("UPDATE movimientosFolios SET folio=folio+1 WHERE idAlmacen=" + idAlmacen + " AND idTipo=" + idTipo);
            } else {
                folio = 1;
                st.executeUpdate("INSERT INTO movimientosFolios (idAlmacen, idTipo, folio) VALUES (" + idAlmacen + ", " + idTipo + ", 2)");
            }
        }
        return folio;
    }

    public static void agregaMovimientoOficina(Connection cn, TOMovimientoOficina to, boolean definitivo) throws SQLException {
        try (Statement st = cn.createStatement()) {
            if (definitivo) {
                to.setFolio(obtenMovimientoFolioOficina(cn, to.getIdAlmacen(), to.getIdTipo()));
            }
            String strSQL = "INSERT INTO movimientos (idTipo, idEmpresa, idAlmacen, folio, idComprobante, desctoComercial, desctoProntoPago, idImpuestoZona, fecha, idUsuario, tipoDeCambio, idReferencia, referencia, estatus, propietario, idMovtoAlmacen) "
                    + "VALUES(" + to.getIdTipo() + ", " + to.getIdEmpresa() + ", " + to.getIdAlmacen() + ", " + to.getFolio() + ", " + to.getIdComprobante() + ", " + to.getDesctoComercial() + ", " + to.getDesctoProntoPago() + ", " + to.getIdImpuestoZona() + ", GETDATE(), " + to.getIdUsuario() + ", " + to.getTipoDeCambio() + ", " + to.getIdReferencia() + ", " + to.getReferencia() + ", " + to.getEstatus() + ", " + to.getPropietario() + ", " + to.getIdMovtoAlmacen() + ")";
            st.executeUpdate(strSQL);

            ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
            if (rs.next()) {
                to.setIdMovto(rs.getInt("idMovto"));
            }
            rs = st.executeQuery("SELECT fecha FROM movimientos WHERE idMovto=" + to.getIdMovto());
            if (rs.next()) {
                to.setFecha(new java.util.Date(rs.getTimestamp("fecha").getTime()));
            }
        }
    }

    public static ArrayList<MovimientoTipo> obtenMovimientosTipos(Connection cn, boolean suma) throws SQLException {
        ArrayList<MovimientoTipo> lst = new ArrayList<>();
        String strSQL = "SELECT idTipo, tipo FROM movimientosTipos WHERE suma=" + (suma ? 1 : 0) + " AND eliminable=1";
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                lst.add(new MovimientoTipo(rs.getInt("idTipo"), rs.getString("tipo")));
            }
        }
        return lst;
    }
}
