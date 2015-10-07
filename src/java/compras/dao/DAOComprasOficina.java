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
                strSQL="SELECT S.cantOrdenada, S.cantOrdenadaSinCargo, S.costoOrdenado\n"
                        + "     , M.folio AS idMovto, D.idEmpaque, D.cantFacturada, D.cantSinCargo, D.costoPromedio\n"
                        + "     , D.costo, D.desctoProducto1, D.desctoProducto2, D.desctoConfidencial, D.unitario\n"
                        + "     , D.idImpuestoGrupo, D.fecha, D.existenciaAnterior\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M on M.idMovto=D.idMovto\n"
                        + "INNER JOIN ordenCompraSurtido S ON S.idOrdenCompra=M.referencia AND S.idEmpaque=D.idEmpaque\n"
                        + "WHERE M.idComprobante="+idComprobante+" AND estatus=5\n"
                        + "ORDER BY M.referencia, M.folio";
                ResultSet rs=st.executeQuery(strSQL);
                while(rs.next()) {
                    detalle.add(this.construirProductoCompra(rs));
                }
            }
        }
        return detalle;
    }

    public void cerrarOrdenDeCompra(int idOrdenDeCompra) throws SQLException {
        String strSQL = "UPDATE ordenCompra SET estado=6, fechaCierreOficina=GETDATE() WHERE idOrdenCompra=" + idOrdenDeCompra;
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

    public void cancelarCompra(int idMovto, int idAlmacen, int idOrdenDeCompra) throws SQLException {
        String strSQL;
        TOMovimientoOficina toMov;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                this.validarExistenciaOficina(cn, idMovto);

                strSQL = "UPDATE movimientos SET estatus=7 WHERE idMovto=" + idMovto;
                st.executeUpdate(strSQL);

                toMov = movimientos.Movimientos.obtenMovimientoOficina(cn, idMovto);

                toMov.setFolio(0);
                toMov.setIdTipo(34);
                toMov.setIdUsuario(this.idUsuario);
                toMov.setPropietario(0);
                toMov.setEstatus(5);
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
                        strSQL = "UPDATE ordenCompra SET estado=5 WHERE idOrdenCompra=" + idOrdenDeCompra;
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
                toMov.setEstatus(5);
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
                        strSQL = "UPDATE ordenCompra SET estado=6 WHERE idOrdenCompra=" + toMov.getReferencia();
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

//    public void grabarCompraOficina(int idEmpresa, TOMovimientoOficina m, int idImpuestoZona, ArrayList<ProductoCompra> productos) throws SQLException {
//        boolean nueva = false;
//        int idEmpaque, idImpuestoGrupo;
//        double existenciaAnterior;
//        ArrayList<ImpuestosProducto> impuestos;
//
//        String strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantFacturada, cantSinCargo, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior) "
//                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, getdate(), ?)";
//
//        String strSQL1 = "UPDATE movimientosDetalle "
//                + "SET costo=?, desctoProducto1=?, desctoProducto2=?, desctoConfidencial=?, unitario=?, cantFacturada=?, cantSinCargo=?, existenciaAnterior=? "
//                + "WHERE idMovto=" + m.getIdMovto() + " AND idEmpaque=?";
//
//        String strSQL2 = "UPDATE movimientosDetalleImpuestos "
//                + "SET importe=? "
//                + "WHERE idMovto=" + m.getIdMovto() + " AND idEmpaque=?";
//
////        String strSQL3="INSERT INTO kardexOficina (idAlmacen, idMovto, idTipoMovto, idEmpaque, fecha, existenciaAnterior, cantidad) " +
////                    "VALUES ("+m.getIdAlmacen()+", "+m.getIdMovto()+", 1, ?, GETDATE(), ?, ?)";
////        PreparedStatement ps3=cn.prepareStatement(strSQL3);
//
//        String strSQL4 = "INSERT INTO almacenesEmpaques (idAlmacen, idEmpaque, existenciaOficina, existenciaMinima, existenciaMaxima) "
//                + "VALUES (?, ?, ?, 0, 0)";
//
//        String strSQL5 = "UPDATE almacenesEmpaques "
//                + "SET existenciaOficina=existenciaOficina+? "
//                + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=?";
//
//        String strSQL6 = "INSERT INTO empresasEmpaques (idEmpresa, idEmpaque, costoUnitarioPromedio, existenciaOficina, idMovtoUltimaEntrada) "
//                + "VALUES (" + idEmpresa + ", ?, ?, ?, ?)";
//
//        String strSQL7 = "UPDATE empresasEmpaques "
//                + "SET existenciaOficina=existenciaOficina+?"
//                + ", costoUnitarioPromedio=(existenciaOficina*costoUnitarioPromedio+?*?)/(existenciaOficina+?)"
//                + ", idMovtoUltimaEntrada=? "
//                + "WHERE idEmpresa=" + idEmpresa + " AND idEmpaque=?";
//
//        try (Connection cn = this.ds.getConnection()) {
//            cn.setAutoCommit(false);
//            try (Statement st = cn.createStatement();
//                    PreparedStatement ps = cn.prepareStatement(strSQL);
//                    PreparedStatement ps1 = cn.prepareStatement(strSQL1);
//                    PreparedStatement ps2 = cn.prepareStatement(strSQL2);
//                    PreparedStatement ps4 = cn.prepareStatement(strSQL4);
//                    PreparedStatement ps5 = cn.prepareStatement(strSQL5);
//                    PreparedStatement ps6 = cn.prepareStatement(strSQL6);
//                    PreparedStatement ps7 = cn.prepareStatement(strSQL7)) {
//                ResultSet rs;
//                st.executeUpdate("BEGIN TRANSACTION");
//
//                if (m.getIdMovto() == 0) {
//                    nueva = true;
//                    m.setFolio(movimientos.Movimientos.obtenMovimientoFolio(cn, m.getIdAlmacen(), 1));
//
//                    strSQL = "INSERT INTO movimientos (idTipo, folio, idAlmacen, idComprobante, tipoDeCambio, desctoComercial, desctoProntoPago, idUsuario, fecha, estatus, idReferencia, referencia, propietario) "
//                            + "VALUES (1, " + m.getFolio() + ", " + m.getIdAlmacen() + ", " + m.getIdComprobante() + ", " + m.getTipoDeCambio() + ", " + m.getDesctoComercial() + ", " + m.getDesctoProntoPago() + ", " + this.idUsuario + ", getdate(), 2, " + m.getIdReferencia() + ", " + m.getReferencia() + ", 0)";
//                    st.executeUpdate(strSQL);
//
//                    rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
//                    if (rs.next()) {
//                        m.setIdMovto(rs.getInt("idMovto"));
//                    }
//                } else {
//                    strSQL = "SELECT estatus FROM movimientos where idMovto=" + m.getIdMovto();
//                    rs = st.executeQuery(strSQL);
//                    if (rs.next()) {
//                        if (rs.getBoolean("estatus")) {
//                            throw new SQLException("Ya se ha capturado y cerrado la entrada");
//                        } else {
//                            strSQL = "UPDATE movimientos SET estatus=2 WHERE idMovto=" + m.getIdMovto();
//                            st.executeUpdate(strSQL);
//                        }
//                    } else {
//                        throw new SQLException("No se encontro el movimiento");
//                    }
//                    st.executeUpdate("UPDATE movimientos "
//                            + "SET tipoDeCambio=" + m.getTipoDeCambio() + " "
//                            + ", desctoComercial=" + m.getDesctoComercial() + ", desctoProntoPago=" + m.getDesctoProntoPago() + " "
//                            + ", fecha=GETDATE(), estatus=2 "
//                            + "WHERE idMovto=" + m.getIdMovto());
//                }
//
//                //rs=st.executeQuery("select DATEPART(weekday, getdate()-1) AS DIA, DATEPART(week, GETDATE()) AS SEM, DATEPART(YEAR, GETDATE())%10 AS ANIO");
//                //lote=""+rs.getInt("DIA")+String.format("%02d", rs.getInt("SEM"))+rs.getInt("ANIO")+"1";
//
//                for (ProductoCompra p : productos) {
//                    idEmpaque = p.getProducto().getIdProducto();
//
//                    if (p.getCantFacturada() > 0 || p.getCantSinCargo() > 0) {
//                        rs = st.executeQuery("SELECT existenciaOficina "
//                                + "FROM almacenesEmpaques "
//                                + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=" + idEmpaque);
//                        if (rs.next()) {
//                            existenciaAnterior = rs.getDouble("existenciaOficina");
//
//                            ps5.setDouble(1, p.getCantFacturada() + p.getCantSinCargo());
//                            ps5.setInt(2, idEmpaque);
//                            ps5.executeUpdate();
//
//                            ps7.setDouble(1, p.getCantFacturada() + p.getCantSinCargo());
//                            ps7.setDouble(2, p.getCantFacturada() + p.getCantSinCargo());
//                            ps7.setDouble(3, p.getCostoPromedio());
//                            ps7.setDouble(4, p.getCantFacturada() + p.getCantSinCargo());
//                            ps7.setInt(5, m.getIdMovto());
//                            ps7.setInt(6, idEmpaque);
//                            ps7.executeUpdate();
//                        } else {
//                            existenciaAnterior = 0;
//
//                            ps4.setInt(1, m.getIdAlmacen());
//                            ps4.setInt(2, idEmpaque);
//                            ps4.setDouble(3, p.getCantFacturada() + p.getCantSinCargo());
//                            ps4.executeUpdate();
//
//                            rs = st.executeQuery("SELECT existenciaOficina "
//                                    + "FROM empresasEmpaques "
//                                    + "WHERE idEmpresa=" + idEmpresa + " AND idEmpaque=" + idEmpaque);
//                            if (rs.next()) {
//                                ps7.setDouble(1, p.getCantFacturada() + p.getCantSinCargo());
//                                ps7.setDouble(2, p.getCantFacturada() + p.getCantSinCargo());
//                                ps7.setDouble(3, p.getCostoPromedio());
//                                ps7.setDouble(4, p.getCantFacturada() + p.getCantSinCargo());
//                                ps7.setInt(5, m.getIdMovto());
//                                ps7.setInt(6, idEmpaque);
//                                ps7.executeUpdate();
//                            } else {
//                                ps6.setInt(1, idEmpaque);
//                                ps6.setDouble(2, p.getCostoPromedio());
//                                ps6.setDouble(3, p.getCantFacturada() + p.getCantSinCargo());
//                                ps6.setInt(4, m.getIdMovto());
//                                ps6.executeUpdate();
//                            }
//                        }
////                    ps3.setInt(1, idEmpaque);
////                    ps3.setDouble(2, existenciaAnterior);
////                    ps3.setDouble(3, p.getCantFacturada()+p.getCantSinCargo());
////                    ps3.executeUpdate();
//                        if (nueva) {
//                            ps.setInt(1, m.getIdMovto());
//                            ps.setInt(2, idEmpaque);
//                            ps.setDouble(3, p.getCantFacturada());
//                            ps.setDouble(4, p.getCantSinCargo());
//                            ps.setDouble(5, p.getCostoPromedio());
//                            ps.setDouble(6, p.getCosto());
//                            ps.setDouble(7, p.getDesctoProducto1());
//                            ps.setDouble(8, p.getDesctoProducto2());
//                            ps.setDouble(9, p.getDesctoConfidencial());
//                            ps.setDouble(10, p.getUnitario());
////                            ps.setDouble(11, p.getCantOrdenada());
////                            ps.setDouble(12, 0);
//                            ps.setInt(11, p.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());
//                            ps.setDouble(12, existenciaAnterior);
//                            ps.executeUpdate();
//
//                            idImpuestoGrupo = p.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo();
////                            this.agregarImpuestosProducto(cn, m.getIdMovto(), idEmpaque, idImpuestoGrupo, idImpuestoZona);
////                            movimientos.Movimientos.calculaImpuestosProducto(cn, m.getIdMovto(), idEmpaque);
//                        } else {
//                            ps1.setDouble(1, p.getCosto());
//                            ps1.setDouble(2, p.getDesctoProducto1());
//                            ps1.setDouble(3, p.getDesctoProducto2());
//                            ps1.setDouble(4, p.getDesctoConfidencial());
//                            ps1.setDouble(5, p.getUnitario());
//                            ps1.setDouble(6, p.getCantFacturada());
//                            ps1.setDouble(7, p.getCantSinCargo());
//                            ps1.setDouble(8, existenciaAnterior);
//                            ps1.setInt(9, idEmpaque);
//                            ps1.executeUpdate();
//                        }
//                        impuestos = p.getImpuestos();
//                        for (ImpuestosProducto i : impuestos) {
//                            ps2.setDouble(1, i.getImporte());
//                            ps2.setInt(2, idEmpaque);
//                            ps2.executeUpdate();
//                        }
//                    }
//                }
//                if (m.getReferencia() != 0) {
//                    strSQL = "UPDATE OCD\n"
//                            + "SET OCD.cantRecibidaOficina=OCD.cantRecibidaOficina+(MD.cantFacturada+MD.cantSinCargo)\n"
//                            + "FROM ordenCompraDetalle OCD\n"
//                            + "INNER JOIN movimientosDetalle MD ON MD.idMovto=" + m.getIdMovto() + " AND MD.idEmpaque=OCD.idEmpaque\n"
//                            + "WHERE OCD.idOrdenCompra=" + m.getReferencia();
//                    st.executeUpdate(strSQL);
//
//                    strSQL = "SELECT SUM(CASE WHEN OCD.cantRecibidaOficina < (OCD.cantOrdenada+OCD.cantOrdenadaSinCargo) THEN 1 ELSE 0 END) AS faltantes\n"
//                            + "FROM ordenCompraDetalle OCD\n"
//                            + "WHERE OCD.idOrdenCompra=" + m.getReferencia();
//                    rs = st.executeQuery(strSQL);
//                    if (rs.next()) {
//                        if (rs.getInt("faltantes") == 0) {
//                            strSQL = "UPDATE OC SET estado=3 FROM ordenCompra OC WHERE OC.idOrdenCompra=" + m.getReferencia();
//                            st.executeUpdate(strSQL);
//                        }
//                    } else {
//                        throw new SQLException("No se encontro orden de compra");
//                    }
//                }
//                cn.commit();
//            } catch (SQLException e) {
//                cn.rollback();
//                throw (e);
//            } finally {
//                cn.setAutoCommit(true);
//            }
//        }
//    }
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
                
                strSQL="UPDATE movimientos SET referencia=0 WHERE idMovto="+toMov.getIdMovto();
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
        double disponibles = 0;
        String strSQL = "SELECT cantOrdenada - surtidosOficina - separadosOficina AS disponibles\n"
                + "FROM ordenCompraSurtido\n"
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
                        + "                 AND MM.idReferencia=" + rs.getInt("idReferencia") + " AND MM.estatus=5\n"
                        + "      GROUP BY DD.idEmpaque) UU\n"
                        + "RIGHT JOIN movimientosDetalle D ON D.idEmpaque=UU.idEmpaque\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN movimientosDetalle DD ON DD.idMovto=UU.idMovto AND DD.idEmpaque=UU.idEmpaque\n"
                        + "WHERE D.idMovto=" + idMovto;
                st.executeUpdate(strSQL);
            }
        }
    }

    public ArrayList<TOProductoCompraOficina> cargarOrdenDeCompraDetalle(int idOrdenCompra, int idMovto) throws SQLException {
        String strSQL = "";
        ArrayList<TOProductoCompraOficina> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
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

//                strSQL = "UPDATE D\n"
//                        + "SET costoPromedio=D.unitario*S.cantOrdenada/(S.cantOrdenada + S.cantOrdenadaSinCargo)\n"
//                        + "FROM movimientosDetalle D\n"
//                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
//                        + "INNER JOIN ordenCompraSurtido S ON S.idOrdenCompra=M.referencia AND S.idEmpaque=D.idEmpaque\n"
//                        + "WHERE D.idMovto=" + idMovto + " AND S.cantOrdenada+S.cantOrdenadaSinCargo > 0";
//                st.executeUpdate(strSQL);

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
        movimientos.Movimientos.construirProducto(rs, toProd);
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
