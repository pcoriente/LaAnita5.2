package compras.dao;

import compras.to.TOProductoCompraOficina;
import impuestos.dominio.ImpuestosProducto;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import movimientos.to.TOMovimientoOficina;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOComprasOficina {

    int idUsuario, idCedis;
    private DataSource ds = null;

    public DAOComprasOficina() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
        this.idUsuario = usuarioSesion.getUsuario().getId();
        this.idCedis = usuarioSesion.getUsuario().getIdCedis();

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
    }

    public ArrayList<TOProductoCompraOficina> obtenerComprobanteDetalle(int idComprobante) throws SQLException {
        String strSQL;
        ArrayList<TOProductoCompraOficina> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                strSQL = "SELECT S.cantOrdenada, S.cantOrdenadaSinCargo, S.costoOrdenado\n"
                        + "     , M.folio AS idMovto, D.idEmpaque, D.cantFacturada, D.cantSinCargo, D.costoPromedio\n"
                        + "     , D.costo, D.desctoProducto1, D.desctoProducto2, D.desctoConfidencial, D.unitario\n"
                        + "     , D.idImpuestoGrupo, D.fecha, D.existenciaAnterior\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M on M.idMovto=D.idMovto\n"
                        + "INNER JOIN ordenCompraSurtido S ON S.idOrdenCompra=M.referencia AND S.idEmpaque=D.idEmpaque\n"
                        + "WHERE M.idComprobante=" + idComprobante + " AND estatus=7\n"
                        + "ORDER BY M.referencia, M.folio";
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    detalle.add(this.construirProductoCompra(rs));
                }
            }
        }
        return detalle;
    }

    public void cerrarOrdenDeCompra(int idOrdenDeCompra) throws SQLException {
        String strSQL = "UPDATE ordenCompra SET estado=7, fechaCierreOficina=GETDATE() WHERE idOrdenCompra=" + idOrdenDeCompra;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);
            }
        }
    }

    private void validarExistenciaOficina(Connection cn, int idMovto) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            strSQL = "SELECT D.*\n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "LEFT JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                    + "WHERE D.idMovto=" + idMovto + " AND ISNULL(A.existencia, 0)-ISNULL(A.separados, 0) < (D.cantFacturada-D.cantSinCargo)";
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                throw new SQLException("Existencia insuficiente en almacen para realizar movimiento !!!");
            }
            strSQL = "SELECT D.*\n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "LEFT JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                    + "WHERE D.idMovto=" + idMovto + " AND ISNULL(E.existencia, 0) < (D.cantFacturada-D.cantSinCargo)";
            rs = st.executeQuery(strSQL);
            if (rs.next()) {
                throw new SQLException("Existencia insuficiente en empresa para realizar movimiento !!!");
            }
        }
    }

    public void eliminarCompra(int idMovto) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "DELETE FROM movimientosDetalleImpuestos WHERE idMovto=" + idMovto;
                st.executeUpdate(strSQL);

                strSQL = "UPDATE S\n"
                        + "SET separadosOficina=S.separadosOficina-D.cantFacturada\n"
                        + "	, separadosOficinaSinCargo=S.separadosOficinaSinCargo-D.cantSinCargo\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN ordenCompraSurtido S ON S.idOrdenCompra=M.referencia AND S.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + idMovto;
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalle WHERE idMovto=" + idMovto;
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientos WHERE idMovto=" + idMovto;
                st.executeUpdate(strSQL);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void devolverCompra(int idMovto, int idOrdenDeCompra) throws SQLException {
        String strSQL;
        TOMovimientoOficina toMov;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                this.validarExistenciaOficina(cn, idMovto);

                strSQL = "UPDATE movimientos SET estatus=8 WHERE idMovto=" + idMovto;
                st.executeUpdate(strSQL);

                toMov = movimientos.Movimientos.obtenMovimientoOficina(cn, idMovto);

                toMov.setFolio(0);
                toMov.setIdTipo(34);
                toMov.setIdUsuario(this.idUsuario);
                toMov.setPropietario(0);
                toMov.setEstatus(7);
                movimientos.Movimientos.agregaMovimientoOficina(cn, toMov, true);

                strSQL = "INSERT INTO movimientosDetalle\n"
                        + "SELECT " + toMov.getIdMovto() + ", idEmpaque, cantFacturada, cantSinCargo, 0, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, '', 0\n"
                        + "FROM movimientosDetalle WHERE idMovto=" + idMovto;
                st.executeUpdate(strSQL);

                strSQL = "INSERT INTO movimientosDetalleImpuestos\n"
                        + "SELECT " + toMov.getIdMovto() + ", idEmpaque, idImpuesto, impuesto, valor, aplicable, modo, acreditable, importe, acumulable\n"
                        + "FROM movimientosDetalleImpuestos WHERE idMovto=" + idMovto;
                st.executeUpdate(strSQL);

                movimientos.Movimientos.actualizaDetalleOficina(cn, toMov.getIdMovto(), toMov.getIdTipo(), false);

                strSQL = "INSERT INTO devoluciones (idMovto, idDevolucion) VALUES (" + idMovto + ", " + toMov.getIdMovto() + ")";
                st.executeUpdate(strSQL);

                if (idOrdenDeCompra != 0) {
                    strSQL = "UPDATE S\n"
                            + "SET surtidosOficina=S.surtidosOficina-D.cantFacturada\n"
                            + "     , surtidosOficinaSinCargo=S.surtidosOficinaSinCargo-D.cantSinCargo\n"
                            + "FROM movimientosDetalle D\n"
                            + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                            + "INNER JOIN ordenCompraSurtido S ON S.idOrdenCompra=M.referencia AND S.idEmpaque=D.idEmpaque\n"
                            + "WHERE D.idMovto=" + idMovto;
                    st.executeUpdate(strSQL);

                    strSQL = "SELECT idEmpaque\n"
                            + "FROM ordenCompraSurtido\n"
                            + "WHERE idOrdenCompra=" + idOrdenDeCompra + "\n"
                            + "         AND (surtidosOficina < cantOrdenada OR surtidosOficinaSinCargo < cantOrdenadaSinCargo)";
                    ResultSet rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        strSQL = "UPDATE ordenCompra SET estado=5, fechaCierreOficina='' WHERE idOrdenCompra=" + idOrdenDeCompra;
                        st.executeUpdate(strSQL);
                    }
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void grabarCompraOficina(TOMovimientoOficina toMov) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                toMov.setEstatus(7);
                toMov.setIdUsuario(this.idUsuario);
                toMov.setFolio(movimientos.Movimientos.obtenMovimientoFolioOficina(cn, toMov.getIdAlmacen(), toMov.getIdTipo()));
                movimientos.Movimientos.grabaMovimientoOficina(cn, toMov);

                movimientos.Movimientos.actualizaDetalleOficina(cn, toMov.getIdMovto(), toMov.getIdTipo(), true);

                if (toMov.getReferencia() != 0) {
                    strSQL = "UPDATE S\n"
                            + "SET surtidosOficina=S.surtidosOficina+D.cantFacturada\n"
                            + "	, separadosOficina=S.separadosOficina-D.cantFacturada\n"
                            + "	, surtidosOficinaSinCargo=S.surtidosOficinaSinCargo+D.cantSinCargo\n"
                            + "	, separadosOficinaSinCargo=S.separadosOficinaSinCargo-D.cantSinCargo\n"
                            + "FROM movimientosDetalle D\n"
                            + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                            + "INNER JOIN ordenCompraSurtido S ON S.idOrdenCompra=M.referencia AND S.idEmpaque=D.idEmpaque\n"
                            + "WHERE D.idMovto=" + toMov.getIdMovto();
                    st.executeUpdate(strSQL);

                    strSQL = "SELECT idEmpaque\n"
                            + "FROM ordenCompraSurtido\n"
                            + "WHERE idOrdenCompra=" + toMov.getReferencia() + "\n"
                            + "         AND (surtidosOficina < cantOrdenada OR surtidosOficinaSinCargo < cantOrdenadaSinCargo)";
                    ResultSet rs = st.executeQuery(strSQL);
                    if (!rs.next()) {
                        strSQL = "UPDATE ordenCompra SET estado=7, fechaCierreOficina=GETDATE() WHERE idOrdenCompra=" + toMov.getReferencia();
                        st.executeUpdate(strSQL);
                    }
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void inicializarCompra(TOMovimientoOficina toMov) throws SQLException {
        String strSQL = "";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "DELETE FROM movimientosDetalleImpuestos WHERE idMovto=" + toMov.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE S\n"
                        + "SET separadosOficina=S.separadosOficina-D.cantFacturada\n"
                        + "	, separadosOficinaSinCargo=S.separadosOficinaSinCargo-D.cantSinCargo\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN ordenCompraSurtido S ON S.idOrdenCompra=M.referencia AND S.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toMov.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalle WHERE idMovto=" + toMov.getIdMovto();
                st.executeUpdate(strSQL);

                toMov.setDesctoComercial(0);
                toMov.setDesctoProntoPago(0);
                toMov.setTipoDeCambio(1);
                movimientos.Movimientos.grabaMovimientoOficina(cn, toMov);

                strSQL = "UPDATE movimientos SET referencia=0 WHERE idMovto=" + toMov.getIdMovto();
                st.executeUpdate(strSQL);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void eliminarProducto(int idMovto, int idProducto) throws SQLException {
        String strSQL = "";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "UPDATE S\n"
                        + "SET separadosOficina=S.separadosOficina-D.cantFacturada\n"
                        + "	, separadosOficinaSinCargo=S.separadosOficinaSinCargo-D.cantSinCargo\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN ordenCompraSurtido S ON S.idOrdenCompra=M.referencia AND S.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + idMovto + " AND D.idEmpaque=" + idProducto;
                st.executeUpdate(strSQL);

                movimientos.Movimientos.eliminaProductoOficina(cn, idMovto, idProducto);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void liberarSinCargo(int idMovto, int idEmpaque, double cantLiberar, int idOrdenCompra) throws SQLException {
        String strSQL = "";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {

                strSQL = "UPDATE ordenCompraSurtido\n"
                        + "SET separadosOficinaSinCargo=separadosOficinaSinCargo-" + cantLiberar + "\n"
                        + "WHERE idOrdenCompra=" + idOrdenCompra + " AND idEmpaque=" + idEmpaque;
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosDetalle\n"
                        + "SET cantSinCargo=cantSinCargo+" + cantLiberar + "\n"
                        + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + idEmpaque;
                st.executeUpdate(strSQL);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public double separarSinCargo(int idMovto, int idEmpaque, double cantSeparar, int idOrdenCompra) throws SQLException {
        double disponibles = 0;
        String strSQL = "SELECT cantOrdenadaSinCargo - surtidosOficinaSinCargo - separadosOficinaSinCargo AS disponibles\n"
                + "FROM ordenCompraSurtido \n"
                + "WHERE idOrdenCompra=" + idOrdenCompra + " AND idEmpaque=" + idEmpaque;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    disponibles = rs.getDouble("disponibles");
                }
                if (disponibles < cantSeparar) {
                    cantSeparar = disponibles;
                }
                strSQL = "UPDATE ordenCompraSurtido\n"
                        + "SET separadosOficinaSinCargo=separadosOficinaSinCargo+" + cantSeparar + "\n"
                        + "WHERE idOrdenCompra=" + idOrdenCompra + " AND idEmpaque=" + idEmpaque;
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosDetalle\n"
                        + "SET cantSinCargo=cantSinCargo+" + cantSeparar + "\n"
                        + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + idEmpaque;
                st.executeUpdate(strSQL);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return cantSeparar;
    }

    public void liberar(int idMovto, int idEmpaque, double cantLiberar, int idOrdenCompra) throws SQLException {
        String strSQL = "";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "UPDATE ordenCompraSurtido\n"
                        + "SET separadosOficina=separadosOficina-" + cantLiberar + "\n"
                        + "WHERE idOrdenCompra=" + idOrdenCompra + " AND idEmpaque=" + idEmpaque;
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosDetalle\n"
                        + "SET cantFacturada=cantFacturada-" + cantLiberar + "\n"
                        + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + idEmpaque;
                st.executeUpdate(strSQL);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public double separar(int idMovto, int idEmpaque, double cantSeparar, int idOrdenCompra) throws SQLException {
//        double disponibles = 0;
//        String strSQL = "SELECT cantOrdenada - surtidosOficina - separadosOficina AS disponibles\n"
//                + "FROM ordenCompraSurtido\n"
//                + "WHERE idOrdenCompra=" + idOrdenCompra + " AND idEmpaque=" + idEmpaque;
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
//                ResultSet rs = st.executeQuery(strSQL);
//                if (rs.next()) {
//                    disponibles = rs.getDouble("disponibles");
//                }
//                if (disponibles < cantSeparar) {
//                    cantSeparar = disponibles;
//                }
                strSQL = "UPDATE ordenCompraSurtido\n"
                        + "SET separadosOficina=separadosOficina+" + cantSeparar + "\n"
                        + "WHERE idOrdenCompra=" + idOrdenCompra + " AND idEmpaque=" + idEmpaque;
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosDetalle\n"
                        + "SET cantFacturada=cantFacturada+" + cantSeparar + "\n"
                        + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + idEmpaque;
                st.executeUpdate(strSQL);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return cantSeparar;
    }

    public ArrayList<TOProductoCompraOficina> actualizarCompraPrecios(TOMovimientoOficina toMov, double tipoDeCambioOld) throws SQLException {
        String strSQL;
        ArrayList<TOProductoCompraOficina> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "UPDATE movimientos\n"
                        + "SET tipoDeCambio=" + toMov.getTipoDeCambio() + ", desctoComercial=" + toMov.getDesctoComercial() + "\n"
                        + "WHERE idMovto=" + toMov.getIdMovto();
                st.executeUpdate(strSQL);

                if (toMov.getTipoDeCambio() != tipoDeCambioOld) {
                    strSQL = "UPDATE D\n"
                            + "SET costo=ROUND(D.costo*M.tipoDeCambio/" + tipoDeCambioOld + ", 6)\n"
                            + "FROM movimientosDetalle D\n"
                            + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                            + "WHERE D.idMovto=" + toMov.getIdMovto();
                    st.executeUpdate(strSQL);
                }

                movimientos.Movimientos.calculaUnitario(cn, toMov.getIdMovto(), 0);

                detalle = this.obtenCompraDetalle(cn, toMov.getIdMovto());

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return detalle;
    }

    public ArrayList<TOProductoCompraOficina> cargarCompraDetalle(int idMovto, int estatus) throws SQLException {
        ArrayList<TOProductoCompraOficina> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                if (estatus == 0) {
                    this.actualizaCostoUltimaCompraProveedor(cn, idMovto);
                    movimientos.Movimientos.calculaUnitario(cn, idMovto, 0);
                }
                detalle = this.obtenCompraDetalle(cn, idMovto);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return detalle;
    }

//    private void actualizaCostoUltimaCompraProveedor(Connection cn, int idMovto) throws SQLException {
//        String strSQL = "UPDATE D\n"
//                + "SET costo=DD.unitario\n"
//                + "FROM movimientosDetalle D\n"
//                + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
//                + "INNER JOIN proveedoresProductos P ON P.idEmpresa=M.idEmpresa AND P.idProveedor=M.idReferencia AND P.idEquivalencia=D.idEmpaque\n"
//                + "INNER JOIN movimientosDetalle DD ON DD.idMovto=P.idMovtoUltimaCompra AND DD.idEmpaque=D.idEmpaque\n"
//                + "WHERE D.idMovto=" + idMovto;
//        try (Statement st = cn.createStatement()) {
//            st.executeUpdate(strSQL);
//        }
//    }

    private void actualizaCostoUltimaCompraProveedor(Connection cn, int idMovto) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            strSQL = "SELECT idEmpresa, idReferencia FROM movimientos WHERE idMovto=" + idMovto;
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                strSQL = "UPDATE D\n"
                        + "SET costo=CASE WHEN M.referencia!=0 THEN D.costo\n"
                        + "               WHEN DD.costo IS NULL THEN 0\n"
                        + "               WHEN D.costo=ROUND(D.costoPromedio*M.tipoDeCambio, 6) THEN DD.costo\n"
                        + "               ELSE D.costo END\n"
                        + "     , costoPromedio=ISNULL(DD.costo, 0)\n"
                        + "FROM (SELECT MAX(DD.idMovto) AS idMovto, DD.idEmpaque\n"
                        + "      FROM movimientosDetalle DD \n"
                        + "      INNER JOIN movimientos MM ON MM.idMovto=DD.idMovto\n"
                        + "      WHERE MM.idEmpresa=" + rs.getInt("idEmpresa") + " AND MM.idTipo=1\n"
                        + "                 AND MM.idReferencia=" + rs.getInt("idReferencia") + " AND MM.estatus=7\n"
                        + "      GROUP BY DD.idEmpaque) UU\n"
                        + "RIGHT JOIN movimientosDetalle D ON D.idEmpaque=UU.idEmpaque\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN movimientosDetalle DD ON DD.idMovto=UU.idMovto AND DD.idEmpaque=UU.idEmpaque\n"
                        + "WHERE D.idMovto=" + idMovto;
                st.executeUpdate(strSQL);
            }
        }
    }
    
    public ArrayList<TOProductoCompraOficina> crearOrdenDeCompraDetalle(TOMovimientoOficina toMov, boolean definitivo) throws SQLException {
        ArrayList<TOProductoCompraOficina> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                toMov.setEstatus(0);
                toMov.setIdUsuario(this.idUsuario);
                toMov.setPropietario(this.idUsuario);
                movimientos.Movimientos.agregaMovimientoOficina(cn, toMov, definitivo);
                this.cargaOrdenDeCompraDetalle(cn, toMov.getReferencia(), toMov.getIdMovto());
                detalle = this.obtenCompraDetalle(cn, toMov.getIdMovto());
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return detalle;
    }

    private void cargaOrdenDeCompraDetalle(Connection cn, int idOrdenCompra, int idMovto) throws SQLException {
        String strSQL = "";
        try (Statement st = cn.createStatement()) {
            strSQL = "SELECT TOP 1 * FROM ordenCompraSurtido WHERE idOrdenCompra=" + idOrdenCompra;
            ResultSet rs = st.executeQuery(strSQL);
            if (!rs.next()) {
                strSQL = "INSERT INTO ordenCompraSurtido (idOrdenCompra, idEmpaque, cantOrdenada, cantOrdenadaSinCargo, costoOrdenado, surtidosOficina, separadosOficina, surtidosOficinaSinCargo, separadosOficinaSinCargo, surtidosAlmacen, separadosAlmacen)\n"
                        + "SELECT idOrdenCompra, idEmpaque, cantOrdenada, cantOrdenadaSinCargo, costoOrdenado, 0, 0, 0, 0, 0, 0\n"
                        + "FROM ordenCompraDetalle\n"
                        + "WHERE idOrdenCompra=" + idOrdenCompra;
                st.executeUpdate(strSQL);
            }
            strSQL = "UPDATE movimientos SET referencia=" + idOrdenCompra + " WHERE idMovto=" + idMovto;
            st.executeUpdate(strSQL);

            strSQL = "UPDATE M\n"
                    + "SET desctoComercial=OC.desctoComercial, desctoProntoPago=OC.desctoProntoPago\n"
                    + "FROM movimientos M\n"
                    + "INNER JOIN ordenCompra OC ON OC.idOrdenCompra=M.referencia\n"
                    + "WHERE M.idMovto=" + idMovto;
            st.executeUpdate(strSQL);

            strSQL = "INSERT INTO movimientosDetalle\n"
                    + "SELECT " + idMovto + " AS idMovto, OCD.idEmpaque\n"
                    + "        , CASE WHEN DA.cantidad IS NULL THEN OCD.cantOrdenada - OCS.surtidosOficina - OCS.separadosOficina\n"
                    + "               WHEN DA.cantidad + OCS.surtidosOficina + OCS.separadosOficina > OCS.cantOrdenada \n"
                    + "                     THEN OCD.cantOrdenada - OCS.surtidosOficina - OCS.separadosOficina\n"
                    + "               ELSE DA.cantidad END AS cantFacturada, 0 AS cantSinCargo\n"
                    + "	   , 0 AS costoPromedio, OCD.costoOrdenado AS costo\n"
                    + "	   , OCD.descuentoProducto AS desctoProducto1, OCD.descuentoProducto2 AS desctoProducto2\n"
                    + "	   , OCD.desctoConfidencial, 0 AS unitario, OCD.idImpuestosGrupo AS idImpuestoGrupo\n"
                    + "	   , '' AS fecha, 0 AS existenciaAnterior\n"
                    + "FROM (SELECT M.referencia, MAD.idEmpaque, SUM(MAD.cantidad) AS cantidad\n"
                    + "      FROM movimientos M\n"
                    + "      INNER JOIN movimientosAlmacen MA ON MA.idAlmacen=M.idAlmacen AND MA.idTipo=M.idTipo AND MA.idReferencia=M.idReferencia AND MA.referencia=M.referencia AND MA.idComprobante=M.idComprobante\n"
                    + "      INNER JOIN movimientosDetalleAlmacen MAD ON MAD.idMovtoAlmacen=MA.idMovtoAlmacen\n"
                    + "      WHERE M.idMovto=" + idMovto + "\n"
                    + "      GROUP BY M.referencia, MAD.idEmpaque) DA\n"
                    + "RIGHT JOIN ordenCompraDetalle OCD ON OCD.idOrdenCompra=DA.referencia AND OCD.idEmpaque=DA.idEmpaque\n"
                    + "INNER JOIN ordenCompraSurtido OCS ON OCS.idOrdenCompra=OCD.idOrdenCompra AND OCS.idEmpaque=OCD.idEmpaque\n"
                    + "WHERE OCD.idOrdenCompra=" + idOrdenCompra;
            st.executeUpdate(strSQL);

            this.actualizaCostoUltimaCompraProveedor(cn, idMovto);

            strSQL = "INSERT INTO movimientosDetalleImpuestos (idMovto, idEmpaque, idImpuesto, impuesto, valor, aplicable, modo, acreditable, importe, acumulable)\n"
                    + "SELECT D.idMovto, D.idEmpaque, I.idImpuesto, I.impuesto, ID.valor, I.aplicable, I.modo, I.acreditable, 0 AS importe, I.acumulable\n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "INNER JOIN impuestosDetalle ID ON ID.idGrupo=D.idImpuestoGrupo AND ID.idZona=M.idImpuestoZona\n"
                    + "INNER JOIN impuestos I ON I.idImpuesto=ID.idImpuesto\n"
                    + "WHERE D.idMovto=" + idMovto + " AND CONVERT(date, GETDATE()) between ID.fechaInicial AND ID.fechaFinal";
            st.executeUpdate(strSQL);

            strSQL = "UPDATE S\n"
                    + "SET separadosOficina=S.separadosOficina+D.cantFacturada\n"
                    + "     , separadosOficinaSinCargo=S.separadosOficinaSinCargo+D.cantSinCargo\n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "INNER JOIN ordenCompraSurtido S ON S.idOrdenCompra=M.referencia AND S.idEmpaque=D.idEmpaque\n"
                    + "WHERE D.idMovto=" + idMovto;
            st.executeUpdate(strSQL);

            movimientos.Movimientos.calculaUnitario(cn, idMovto, 0);

//            strSQL = "UPDATE D\n"
//                    + "SET costoPromedio=D.unitario*S.cantOrdenada/(S.cantOrdenada + S.cantOrdenadaSinCargo)\n"
//                    + "FROM movimientosDetalle D\n"
//                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
//                    + "INNER JOIN ordenCompraSurtido S ON S.idOrdenCompra=M.referencia AND S.idEmpaque=D.idEmpaque\n"
//                    + "WHERE D.idMovto=" + idMovto + " AND S.cantOrdenada+S.cantOrdenadaSinCargo > 0";
//            st.executeUpdate(strSQL);
        }

    }

    public ArrayList<TOProductoCompraOficina> cargarOrdenDeCompraDetalle(int idOrdenCompra, int idMovto) throws SQLException {
        ArrayList<TOProductoCompraOficina> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                this.cargaOrdenDeCompraDetalle(cn, idOrdenCompra, idMovto);
                detalle = this.obtenCompraDetalle(cn, idMovto);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return detalle;
    }

    private TOProductoCompraOficina construirProductoCompra(ResultSet rs) throws SQLException {
        TOProductoCompraOficina toProd = new TOProductoCompraOficina();
        toProd.setCantOrdenada(rs.getDouble("cantOrdenada"));
        toProd.setCantOrdenadaSinCargo(rs.getDouble("cantOrdenadaSinCargo"));
        toProd.setCostoOrdenado(rs.getDouble("costoOrdenado"));
        movimientos.Movimientos.construirProductoOficina(rs, toProd);
        return toProd;
    }

    private ArrayList<TOProductoCompraOficina> obtenCompraDetalle(Connection cn, int idMovto) throws SQLException {
        String strSQL = "";
        ArrayList<TOProductoCompraOficina> detalle = new ArrayList<>();
        try (Statement st = cn.createStatement()) {
            strSQL = "SELECT D.*, ISNULL(OCD.cantOrdenadaSinCargo, 0) AS cantOrdenadaSinCargo\n"
                    + "     , ISNULL(OCD.costoOrdenado, 0) AS costoOrdenado, ISNULL(OCD.cantOrdenada, 0) AS cantOrdenada\n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "LEFT JOIN ordenCompraDetalle OCD ON OCD.idOrdenCompra=M.referencia AND OCD.idEmpaque=D.idEmpaque\n"
                    + "WHERE D.idMovto=" + idMovto;
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                detalle.add(this.construirProductoCompra(rs));
            }
        }
        return detalle;
    }

    public ArrayList<TOProductoCompraOficina> obtenerCompraDetalle(int idMovto) throws SQLException {
        ArrayList<TOProductoCompraOficina> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            detalle = this.obtenCompraDetalle(cn, idMovto);
        }
        return detalle;
    }

    public ArrayList<ImpuestosProducto> agregarProductoCompra(TOMovimientoOficina toMov, TOProductoCompraOficina toProd) throws SQLException {
        ArrayList<ImpuestosProducto> impuestos = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                // El campo costoPromedio=costoUltimaDeCompra muentras el movimiento estatus=0
                toProd.setCostoPromedio(movimientos.Movimientos.obtenCostoUltimaCompraProveedor(cn, toMov.getIdEmpresa(), toMov.getIdReferencia(), toProd.getIdProducto()));
                toProd.setCosto(toProd.getCostoPromedio() * toMov.getTipoDeCambio());

                movimientos.Movimientos.agregaProductoOficina(cn, toProd, toMov.getIdImpuestoZona());
                toProd.setUnitario(movimientos.Movimientos.grabaProductoCambios(cn, toProd));

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return impuestos;
    }
//    public int agregarMovimientoOficina(TOMovimientoOficina to, boolean definitivo) throws SQLException {
//        int idMovto = 0;
//        try (Connection cn = this.ds.getConnection()) {
//            cn.setAutoCommit(false);
//            try {
//                to.setEstatus(0);
//                to.setIdUsuario(this.idUsuario);
//                to.setPropietario(this.idUsuario);
//                movimientos.Movimientos.agregaMovimientoOficina(cn, to, definitivo);
//                idMovto = to.getIdMovto();
//                cn.commit();
//            } catch (SQLException ex) {
//                cn.rollback();
//                throw ex;
//            } finally {
//                cn.setAutoCommit(true);
//            }
//        }
//        return idMovto;
//    }
}
