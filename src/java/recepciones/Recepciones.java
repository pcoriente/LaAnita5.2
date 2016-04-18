package recepciones;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import movimientos.to.TOMovimientoOficina;
import recepciones.to.TORecepcion;

/**
 *
 * @author jesc
 */
public class Recepciones {
    
    public static void generaRechazo(Connection cn, TORecepcion toRecepcion) throws SQLException {
        String strSQL = "SELECT M.idAlmacen, DE.idEmpaque, DE.cantFacturada, DS.cantFacturada\n"
                + "FROM movimientosDetalle DE\n"
                + "INNER JOIN movimientos M ON M.idMovto=DE.idMovto\n"
                + "INNER JOIN movimientosDetalle DS ON DS.idMovto=M.referencia AND DS.idEmpaque=DE.idEmpaque\n"
                + "WHERE DE.idMovto=" + toRecepcion.getIdMovto() + " AND DE.cantFacturada < DS.cantFacturada";
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                TOMovimientoOficina toRechazo = new TOMovimientoOficina();
                toRechazo.setIdTipo(54);
                toRechazo.setIdEmpresa(toRecepcion.getIdEmpresa());
                toRechazo.setIdAlmacen(toRecepcion.getIdReferencia());
                toRechazo.setTipoDeCambio(toRecepcion.getTipoDeCambio());
                toRechazo.setIdReferencia(toRecepcion.getIdAlmacen());
                toRechazo.setIdUsuario(toRecepcion.getIdUsuario());
                toRechazo.setPropietario(0);
                toRechazo.setEstatus(7);

                toRechazo.setReferencia(toRecepcion.getIdMovtoAlmacen());
                toRechazo.setFolio(movimientos.Movimientos.obtenMovimientoFolioAlmacen(cn, toRechazo.getIdAlmacen(), toRechazo.getIdTipo()));
                movimientos.Movimientos.agregaMovimientoAlmacen(cn, toRechazo, true);

                toRechazo.setReferencia(toRecepcion.getIdMovto());
                toRechazo.setFolio(movimientos.Movimientos.obtenMovimientoFolioOficina(cn, toRechazo.getIdAlmacen(), toRechazo.getIdTipo()));
                movimientos.Movimientos.agregaMovimientoOficina(cn, toRechazo, true);

                strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantFacturada, cantSinCargo, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior, ctoPromAnterior)\n"
                        + "SELECT " + toRechazo.getIdMovto() + ", DS.idEmpaque, DS.cantFacturada-DE.cantFacturada AS cantFacturada, 0, DS.unitario, DS.costo, 0, 0, 0, DS.unitario, DS.idImpuestoGrupo, '', 0, 0\n"
                        + "FROM movimientosDetalle DE\n"
                        + "INNER JOIN movimientos M ON M.idMovto=DE.idMovto\n"
                        + "INNER JOIN movimientosDetalle DS ON DS.idMovto=M.referencia AND DS.idEmpaque=DE.idEmpaque\n"
                        + "WHERE DE.idMovto=" + toRecepcion.getIdMovto() + " AND DE.cantFacturada < DS.cantFacturada";
                int n = st.executeUpdate(strSQL);

                strSQL = "UPDATE D\n"
                        + "SET D.fecha=GETDATE(), D.existenciaAnterior=A.existencia, D.ctoPromAnterior=E.costoUnitarioPromedio\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                        + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toRechazo.getIdMovto();
                if (st.executeUpdate(strSQL) != n) {
                    throw new SQLException("(idMovto=" + toRecepcion.getIdMovto() + ") No se encontro empaque en almacen !!!");
                }
                strSQL = "UPDATE A\n"
                        + "SET existencia=A.existencia+D.cantFacturada\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toRechazo.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE E\n"
                        + "SET E.costoUnitarioPromedio=(E.costoUnitarioPromedio*E.existencia+D.costoPromedio*D.cantFacturada)/(E.existencia+D.cantFacturada)\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toRechazo.getIdMovto();
                if (st.executeUpdate(strSQL) != n) {
                    throw new SQLException("(idMovto=" + toRecepcion.getIdMovto() + ") No se encontro empaque en empresa !!!");
                }
                strSQL = "UPDATE E\n"
                        + "SET existencia=E.existencia+D.cantFacturada\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toRechazo.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "INSERT INTO movimientosDetalleAlmacen (idMovtoAlmacen, idEmpaque, lote, cantidad, fecha, existenciaAnterior)\n"
                        + "SELECT " + toRechazo.getIdMovtoAlmacen() + ", DE.idEmpaque, DE.lote, DS.cantidad-DE.cantidad AS cantidad, '', 0\n"
                        + "FROM movimientosDetalleAlmacen DE\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=DE.idMovtoAlmacen\n"
                        + "LEFT JOIN movimientosDetalleAlmacen DS ON DS.idMovtoAlmacen=M.referencia AND DS.idEmpaque=DE.idEmpaque AND DS.lote=DE.lote\n"
                        + "WHERE DE.idMovtoAlmacen=" + toRecepcion.getIdMovtoAlmacen() + " AND DE.cantidad < DS.cantidad";
                n = st.executeUpdate(strSQL);

                strSQL = "UPDATE D\n"
                        + "SET fecha=GETDATE(), existenciaAnterior=A.existencia\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                        + "WHERE D.idMovtoAlmacen=" + toRechazo.getIdMovtoAlmacen();
                if (st.executeUpdate(strSQL) != n) {
                    throw new SQLException("(idMovtoAlmacen=" + toRechazo.getIdMovtoAlmacen() + ") No se encontro empaque-lote en almacen !!!");
                }
                strSQL = "UPDATE A\n"
                        + "SET existencia=A.existencia+D.cantidad\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                        + "WHERE D.idMovtoAlmacen=" + toRechazo.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalle WHERE idMovto=" + toRecepcion.getIdMovto() + " AND cantFacturada=0";
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalleAlmacen WHERE idMovtoAlmacen=" + toRecepcion.getIdMovtoAlmacen() + " AND cantidad=0";
                st.executeUpdate(strSQL);
            }
        }
    }

    public static void grabar(Connection cn, TORecepcion mov) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            mov.setFolio(movimientos.Movimientos.obtenMovimientoFolioAlmacen(cn, mov.getIdAlmacen(), mov.getIdTipo()));
            movimientos.Movimientos.grabaMovimientoAlmacen(cn, mov);

            strSQL = "UPDATE movimientosAlmacen SET propietario=0 WHERE idMovtoAlmacen=" + mov.getIdMovtoAlmacen();
            st.executeUpdate(strSQL);

            movimientos.Movimientos.actualizaDetalleAlmacen(cn, mov.getIdMovtoAlmacen(), true);

            mov.setFolio(movimientos.Movimientos.obtenMovimientoFolioOficina(cn, mov.getIdAlmacen(), mov.getIdTipo()));
            movimientos.Movimientos.grabaMovimientoOficina(cn, mov);

            strSQL = "UPDATE movimientos SET propietario=0 WHERE idMovto=" + mov.getIdMovto();
            st.executeUpdate(strSQL);

            movimientos.Movimientos.actualizaDetalleOficina(cn, mov.getIdMovto(), mov.getIdTipo(), true);
        }
    }
}
