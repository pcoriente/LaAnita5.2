package movimientos;

import impuestos.dominio.ImpuestosProducto;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import movimientos.dominio.MovimientoOficina;
import movimientos.dominio.ProductoOficina;
import movimientos.to.TOMovimientoAlmacen;
import movimientos.to.TOMovimientoOficina;
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

    public static TOMovimientoAlmacen obtenerMovimientoAlmacen(Connection cn, int idMovtoAlmacen) throws SQLException {
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

    public static void convertir(TOMovimientoOficina toMov, MovimientoOficina mov) {
        mov.setIdMovto(toMov.getIdMovto());
        mov.setIdTipo(toMov.getIdTipo());
        mov.setFolio(toMov.getFolio());
//        mov.setIdImpuestoZona(toMov.getIdImpuestoZona());
        mov.setDesctoComercial(toMov.getDesctoComercial());
        mov.setDesctoProntoPago(toMov.getDesctoProntoPago());
        mov.setFecha(toMov.getFecha());
        mov.setIdUsuario(toMov.getIdUsuario());
//        mov.setIdMoneda(toMov.getIdMoneda());
        mov.setTipoDeCambio(toMov.getTipoDeCambio());
        mov.setPropietario(toMov.getPropietario());
        mov.setEstatus(toMov.getEstatus());
        mov.setIdMovtoAlmacen(toMov.getIdMovtoAlmacen());
    }

    public static void convertir(MovimientoOficina mov, TOMovimientoOficina toMov) {
        toMov.setIdMovto(mov.getIdMovto());
//        toMov.setIdCedis(mov.getAlmacen().getIdCedis());
        toMov.setIdEmpresa(mov.getAlmacen().getIdEmpresa());
        toMov.setIdAlmacen(mov.getAlmacen().getIdAlmacen());
        toMov.setIdTipo(mov.getIdTipo());
        toMov.setFolio(mov.getFolio());
//        toMov.setIdImpuestoZona(mov.getIdImpuestoZona());
        toMov.setDesctoComercial(mov.getDesctoComercial());
        toMov.setDesctoProntoPago(mov.getDesctoProntoPago());
        toMov.setFecha(mov.getFecha());
        toMov.setIdUsuario(mov.getIdUsuario());
//        toMov.setIdMoneda(mov.getMoneda().getIdMoneda());
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

    public static void construirProducto(ResultSet rs, TOProductoOficina to) throws SQLException {
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

    public static void actualizaDetalleAlmacen(Connection cn, int idMovtoAlmacen, boolean suma) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            strSQL="DELETE FROM movimientosDetalleAlmacen WHERE idMovtoAlmacen="+idMovtoAlmacen+" AND cantidad=0";
            st.executeUpdate(strSQL);
            
            strSQL = "INSERT INTO almacenesLotes\n"
                    + "SELECT M.idAlmacen, D.idEmpaque, D.lote, DATEADD(DAY, 365, L.fecha), 0, 0, 0\n"
                    + "FROM movimientosDetalleAlmacen D\n"
                    + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                    + "INNER JOIN lotes L ON L.lote=SUBSTRING(D.lote, 1, 4)\n"
                    + "LEFT JOIN almacenesLotes AL ON AL.idAlmacen=M.idAlmacen AND AL.idEmpaque=D.idEmpaque AND AL.lote=D.lote\n"
                    + "WHERE M.idMovtoAlmacen=" + idMovtoAlmacen + " AND AL.idAlmacen IS NULL";
            st.executeUpdate(strSQL);

            strSQL = "UPDATE D\n"
                    + "SET fecha=GETDATE(), existenciaAnterior=A.existencia\n"
                    + "FROM movimientosDetalleAlmacen D\n"
                    + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                    + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                    + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen;
            st.executeUpdate(strSQL);

            strSQL = "UPDATE A\n"
                    + "SET A.existencia=A.existencia" + (suma ? "+" : "-") + "D.cantidad\n"
                    + "FROM movimientosDetalleAlmacen D\n"
                    + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                    + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                    + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen;
            st.executeUpdate(strSQL);
        }
    }

    public static void actualizaDetalle(Connection cn, int idMovto, int idTipo, boolean suma) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            strSQL = "DELETE I\n"
                    + "FROM movimientosDetalleImpuestos I\n"
                    + "INNER JOIN movimientosDetalle D ON D.idMovto=I.idMovto AND D.idEmpaque=I.idEmpaque\n"
                    + "WHERE I.idMovto=" + idMovto + " AND D.cantFacturada+D.cantSinCargo=0";
            st.executeUpdate(strSQL);

            strSQL = "DELETE FROM movimientosDetalle WHERE idMovto=" + idMovto + " AND cantFacturada+cantSinCargo=0";
            st.executeUpdate(strSQL);

            strSQL = "INSERT INTO almacenesEmpaques (idAlmacen, idEmpaque, existencia, separados, existenciaMinima, existenciaMaxima)\n"
                    + "SELECT M.idAlmacen, D.idEmpaque, 0, 0, 0, 0\n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "LEFT JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                    + "WHERE D.idMovto=" + idMovto + " AND A.idAlmacen IS NULL";
            st.executeUpdate(strSQL);

            strSQL = "INSERT INTO empresasEmpaques (idEmpresa, idEmpaque, costoUnitarioPromedio, existencia, idMovtoUltimaCompra)\n"
                    + "SELECT M.idEmpresa, D.idEmpaque, 0, 0, 0\n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "LEFT JOIN empresasEmpaques E ON E.idEmpresa=M.idAlmacen AND E.idEmpaque=D.idEmpaque\n"
                    + "WHERE D.idMovto=" + idMovto + " AND E.idEmpresa IS NULL";
            st.executeUpdate(strSQL);

            if (suma) {
                strSQL = "UPDATE D\n"
                        + "SET costoPromedio=CASE WHEN M.referencia=0\n"
                        + "                         THEN ROUND(D.unitario*D.cantFacturada/(D.cantFacturada+D.cantSinCargo), 6)\n"
                        + "                       WHEN M.idTipo=1\n"
                        + "                         THEN ROUND(D.unitario*S.cantOrdenada/(S.cantOrdenada+S.cantOrdenadaSinCargo), 6)\n"
                        + "                       ELSE D.costoPromedio END\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "LEFT JOIN ordenCompraSurtido S ON S.idOrdenCompra=M.referencia AND S.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + idMovto;
                st.executeUpdate(strSQL);

                strSQL = "UPDATE E\n"
                        + "SET costoUnitarioPromedio=ROUND(((E.costoUnitarioPromedio*E.existencia + D.costoPromedio*(D.cantFacturada+D.cantSinCargo))/(E.existencia+D.cantFacturada+D.cantSinCargo)),6)\n"
                        + "	, idMovtoUltimaCompra=CASE WHEN M.idTipo=1 THEN M.idMovto ELSE E.idMovtoUltimaCompra END\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + idMovto;
                st.executeUpdate(strSQL);
            } else if (idTipo == 34) {
                strSQL = "UPDATE D\n"
                        + "SET D.costoPromedio=E.costoUnitarioPromedio\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + idMovto;
                st.executeUpdate(strSQL);
            } else {
                strSQL = "UPDATE D\n"
                        + "SET D.costoPromedio=E.costoUnitarioPromedio\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + idMovto;
                st.executeUpdate(strSQL);
            }
            strSQL = "UPDATE D\n"
                    + "SET fecha=GETDATE(), existenciaAnterior=A.existencia\n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                    + "WHERE D.idMovto=" + idMovto;
            st.executeUpdate(strSQL);

            strSQL = "UPDATE A\n"
                    + "SET A.existencia=A.existencia" + (suma ? "+" : "-") + "(D.cantFacturada+D.cantSinCargo)\n"
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
        String strSQL;
        try (Statement st = cn.createStatement()) {
            strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantFacturada, cantSinCargo, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior) "
                    + "VALUES (" + to.getIdMovto() + ", " + to.getIdProducto() + ", " + to.getCantFacturada() + ", " + to.getCantSinCargo() + ", " + to.getCostoPromedio() + ", " + to.getCosto() + ", " + to.getDesctoProducto1() + ", " + to.getDesctoProducto2() + ", " + to.getDesctoConfidencial() + ", " + to.getUnitario() + ", " + to.getIdImpuestoGrupo() + ", '', 0)";
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

    public static void grabarMovimientoAlmacen(Connection cn, TOMovimientoAlmacen toMov) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            strSQL = "UPDATE movimientosAlmacen\n"
                    + "SET folio=" + toMov.getFolio() + ", fecha=GETDATE(), idUsuario=" + toMov.getIdUsuario() + ", estatus=" + toMov.getEstatus() + "\n"
                    + "WHERE idMovtoAlmacen=" + toMov.getIdMovtoAlmacen();
            st.executeUpdate(strSQL);
        }
    }

    public static void grabarMovimiento(Connection cn, TOMovimientoOficina toMov) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            strSQL = "UPDATE movimientos\n"
                    + "SET folio=" + toMov.getFolio() + ", desctoComercial=" + toMov.getDesctoComercial() + "\n"
                    + "     , desctoProntoPago=" + toMov.getDesctoProntoPago() + ", tipoDeCambio=" + toMov.getTipoDeCambio() + "\n"
                    + "     , fecha=GETDATE(), idUsuario=" + toMov.getIdUsuario() + ", estatus=" + toMov.getEstatus() + "\n"
                    + "WHERE idMovto=" + toMov.getIdMovto();
            st.executeUpdate(strSQL);
        }
    }

    public static TOMovimientoOficina construirMovimiento(ResultSet rs) throws SQLException {
        TOMovimientoOficina to = new TOMovimientoOficina();
        to.setIdMovto(rs.getInt("idMovto"));
        to.setIdTipo(rs.getInt("idTipo"));
        to.setIdEmpresa(rs.getInt("idEmpresa"));
        to.setIdAlmacen(rs.getInt("idAlmacen"));
        to.setFolio(rs.getInt("folio"));
        to.setIdComprobante(rs.getInt("idComprobante"));
        to.setIdImpuestoZona(rs.getInt("idImpuestoZona"));
        to.setDesctoComercial(rs.getDouble("desctoComercial"));
        to.setDesctoProntoPago(rs.getDouble("desctoprontoPago"));
        to.setFecha(new java.util.Date(rs.getTimestamp("fecha").getTime()));
        to.setIdUsuario(rs.getInt("idUsuario"));
        to.setTipoDeCambio(rs.getDouble("tipoDeCambio"));
        to.setIdReferencia(rs.getInt("idReferencia"));
        to.setReferencia(rs.getInt("referencia"));
        to.setPropietario(rs.getInt("propietario"));
        to.setEstatus(rs.getInt("estatus"));
        to.setIdMovtoAlmacen(rs.getInt("idMovtoAlmacen"));
        return to;
    }

    public static TOMovimientoOficina obtenerMovimiento(Connection cn, int idMovto) throws SQLException {
        String strSQL;
        TOMovimientoOficina toMov = null;
        try (Statement st = cn.createStatement()) {
            strSQL = "SELECT * FROM movimientos WHERE idMovto=" + idMovto;
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                toMov = construirMovimiento(rs);
            }
        }
        return toMov;
    }

    public static int obtenMovimientoFolio(Connection cn, int idAlmacen, int idTipo) throws SQLException {
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
                to.setFolio(obtenMovimientoFolio(cn, to.getIdAlmacen(), to.getIdTipo()));
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
}
