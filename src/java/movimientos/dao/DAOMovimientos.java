package movimientos.dao;

import entradas.dominio.MovimientoProducto;
import impuestos.dominio.ImpuestosProducto;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
import movimientos.to.TOMovimiento;
import movimientos.to.TOMovimientoProducto;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOMovimientos {

    int idUsuario;
    int idCedis;
    private DataSource ds = null;
    private Connection cnx;

    public DAOMovimientos() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
        this.idUsuario = usuarioSesion.getUsuario().getId();
        this.idCedis = usuarioSesion.getUsuario().getIdCedis();

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
    }

    public void grabarTraspasoSolicitud(TOMovimiento m, ArrayList<MovimientoProducto> productos) throws SQLException {
        String strSQL;

        this.cnx = this.ds.getConnection();
        Statement st = this.cnx.createStatement();
        try {
            st.execute("BEGIN TRANSACTION");

            m.setFolio(this.obtenerMovimientoFolio(true, m.getIdAlmacen(), 35));
            strSQL = "INSERT INTO movimientos (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, idImpuestoZona, desctoComercial, desctoProntoPago, fecha, idUsuario, idMoneda, tipoCambio, estatus, propietario, idReferencia, referencia) "
                    + "VALUES (35, " + m.getIdCedis() + ", " + m.getIdEmpresa() + ", " + m.getIdAlmacen() + ", " + m.getFolio() + ", 0, " + m.getIdImpuestoZona() + ", 0, 0, GETDATE(), " + this.idUsuario + ", 1, 1, 0, 0, " + m.getIdReferencia() + ", 0)";
            st.executeUpdate(strSQL);
            ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
            if (rs.next()) {
                m.setIdMovto(rs.getInt("idMovto"));
            }
            m.setFolioAlmacen(this.obtenerMovimientoFolio(false, m.getIdAlmacen(), 35));
            strSQL = "INSERT INTO movimientosAlmacen (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, fecha, idUsuario, propietario, idReferencia, referencia, estatus) "
                    + "VALUES (35, " + m.getIdCedis() + ", " + m.getIdEmpresa() + ", " + m.getIdAlmacen() + ", " + m.getFolioAlmacen() + ", 0, GETDATE(), " + this.idUsuario + ", 0, " + m.getIdReferencia() + ", 0, 0)";
            st.executeUpdate(strSQL);
            rs = st.executeQuery("SELECT @@IDENTITY AS idMovtoAlmacen");
            if (rs.next()) {
                m.setIdMovtoAlmacen(rs.getInt("idMovtoAlmacen"));
            }
            strSQL = "INSERT INTO movimientosRelacionados (idMovto, idMovtoAlmacen) VALUES (" + m.getIdMovto() + ", " + m.getIdMovtoAlmacen() + ")";
            st.executeUpdate(strSQL);

            strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantOrdenada, cantFacturada, cantSinCargo, cantRecibida, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior) "
                    + "VALUES (" + m.getIdMovto() + ", ?, ?, 0, 0, 0, 0, 0, 0, 0, 0, 0, ?, GETDATE(), 0)";
            PreparedStatement ps = this.cnx.prepareStatement(strSQL);
            for (MovimientoProducto p : productos) {
                ps.setInt(1, p.getProducto().getIdProducto());
                ps.setDouble(2, p.getCantOrdenada());
                ps.setInt(3, p.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());
                ps.executeUpdate();
            }
            st.execute("COMMIT TRANSACTION");
        } catch (SQLException e) {
            st.execute("ROLLBACK TRANSACTION");
            throw (e);
        } finally {
            st.close();
            this.cnx.close();
        }
    }

    public void grabarCompraAlmacen(TOMovimiento m, ArrayList<MovimientoProducto> productos) throws SQLException {
        String strSQL;
        this.cnx = this.ds.getConnection();
        Statement st = this.cnx.createStatement();
        try {
            st.execute("BEGIN TRANSACTION");
//            strSQL = "SELECT statusAlmacen FROM comprobantes where idComprobante=" + m.getIdComprobante();
//            ResultSet rs = st.executeQuery(strSQL);
//            if (rs.next()) {
//                if (rs.getBoolean("statusAlmacen")) {
//                    throw new SQLException("Ya se ha capturado y cerrado la entrada");
//                } else {
//                    strSQL = "UPDATE comprobantes SET statusAlmacen=1 WHERE idComprobante=" + m.getIdComprobante();
//                    st.executeUpdate(strSQL);
//                }
//            } else {
//                throw new SQLException("No se encontro el comprobante");
//            }
            m.setFolio(this.obtenerMovimientoFolio(false, m.getIdAlmacen(), 1));

            String lote = "";
            strSQL = "SELECT lote FROM lotes WHERE fecha=CONVERT(date, GETDATE())";
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                lote = rs.getString("lote") + "1";
            } else {
                throw new SQLException("No se encontro el lote de fecha de hoy");
            }
            strSQL = "INSERT INTO movimientosAlmacen (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, fecha, idUsuario, estatus, idReferencia, referencia, propietario) "
                    + "VALUES (1, " + m.getIdCedis() + ", " + m.getIdEmpresa() + ", " + m.getIdAlmacen() + ", " + m.getFolio() + ", " + m.getIdComprobante() + ", GETDATE(), " + this.idUsuario + ", 1, " + m.getIdReferencia() + ", " + m.getReferencia() + ", 0)";
            st.executeUpdate(strSQL);
            rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
            if (rs.next()) {
                m.setIdMovto(rs.getInt("idMovto"));
            }
            int idProducto;
            double existenciaAnterior;
            for (MovimientoProducto p : productos) {
                if (p.getCantFacturada() > 0) {
                    idProducto = p.getProducto().getIdProducto();

                    existenciaAnterior = 0;
                    strSQL = "SELECT saldo FROM almacenesLotes "
                            + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                    rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        existenciaAnterior = rs.getDouble("saldo");
                        strSQL = "UPDATE almacenesLotes "
                                + "SET cantidad=cantidad+" + p.getCantFacturada() + ", saldo=saldo+" + p.getCantFacturada() + " "
                                + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                    } else {
                        strSQL = "INSERT INTO almacenesLotes (idAlmacen, idEmpaque, lote, fechaCaducidad, cantidad, saldo, separados, existenciaFisica) "
                                + "VALUES (" + m.getIdAlmacen() + ", " + idProducto + ", '" + lote + "', DATEADD(DAY, 365, convert(date, GETDATE())), " + p.getCantFacturada() + ", " + p.getCantFacturada() + ", 0, 0)";
                    }
                    st.executeUpdate(strSQL);

                    strSQL = "INSERT INTO movimientosDetalleAlmacen (idAlmacen, idMovtoAlmacen, idEmpaque, lote, cantidad, suma, fecha, existenciaAnterior) "
                            + "VALUES (" + m.getIdAlmacen() + ", " + m.getIdMovto() + ", " + idProducto + ", '" + lote + "', " + p.getCantFacturada() + ", 1, GETDATE(), " + existenciaAnterior + ")";
                    st.executeUpdate(strSQL);
                }
            }
            if (m.getReferencia() != 0) {
                strSQL = "UPDATE OCD\n"
                        + "SET OCD.cantRecibidaAlmacen=OCD.cantRecibidaAlmacen+MD.cantidad\n"
                        + "FROM ordenCompraDetalle OCD\n"
                        + "INNER JOIN movimientosDetalleAlmacen MD ON MD.idMovtoAlmacen=" + m.getIdMovto() + " AND MD.idEmpaque=OCD.idEmpaque\n"
                        + "WHERE OCD.idOrdenCompra=" + m.getReferencia();
                st.executeUpdate(strSQL);

                strSQL = "SELECT SUM(CASE WHEN OCD.cantRecibidaAlmacen < (OCD.cantOrdenada+OCD.cantOrdenadaSinCargo) THEN 1 ELSE 0 END) AS faltantes\n"
                        + "FROM ordenCompraDetalle OCD\n"
                        + "WHERE OCD.idOrdenCompra=" + m.getReferencia();
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    if (rs.getInt("faltantes") == 0) {
                        strSQL = "UPDATE OC SET OC.estadoAlmacen=3 FROM ordenCompra OC WHERE OC.idOrdenCompra=" + m.getReferencia();
                        st.executeUpdate(strSQL);
                    }
                } else {
                    throw new SQLException("No se encontro orden de compra");
                }
            }
            st.execute("COMMIT TRANSACTION");
        } catch (SQLException e) {
            st.execute("ROLLBACK TRANSACTION");
            throw (e);
        } finally {
            st.close();
            this.cnx.close();
        }
    }

    public void grabarCompraOficina(TOMovimiento m, ArrayList<MovimientoProducto> productos) throws SQLException {
//        int capturados;
//        boolean ok = false;
        boolean nueva = false;
        ArrayList<ImpuestosProducto> impuestos;

        this.cnx = this.ds.getConnection();
        String strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantFacturada, cantSinCargo, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, cantOrdenada, cantRecibida, idImpuestoGrupo, fecha, existenciaAnterior) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, getdate(), ?)";
        PreparedStatement ps = this.cnx.prepareStatement(strSQL);

        String strSQL1 = "UPDATE movimientosDetalle "
                + "SET costo=?, desctoProducto1=?, desctoProducto2=?, desctoConfidencial=?, unitario=?, cantFacturada=?, cantSinCargo=?, existenciaAnterior=? "
                + "WHERE idMovto=" + m.getIdMovto() + " AND idEmpaque=?";
        PreparedStatement ps1 = this.cnx.prepareStatement(strSQL1);

        String strSQL2 = "UPDATE movimientosDetalleImpuestos "
                + "SET importe=? "
                + "WHERE idMovto=" + m.getIdMovto() + " AND idEmpaque=?";
        PreparedStatement ps2 = this.cnx.prepareStatement(strSQL2);

//        String strSQL3="INSERT INTO kardexOficina (idAlmacen, idMovto, idTipoMovto, idEmpaque, fecha, existenciaAnterior, cantidad) " +
//                    "VALUES ("+m.getIdAlmacen()+", "+m.getIdMovto()+", 1, ?, GETDATE(), ?, ?)";
//        PreparedStatement ps3=cn.prepareStatement(strSQL3);

        String strSQL4 = "INSERT INTO almacenesEmpaques (idAlmacen, idEmpaque, existenciaOficina, existenciaMinima, existenciaMaxima) "
                + "VALUES (?, ?, ?, 0, 0)";
        PreparedStatement ps4 = this.cnx.prepareStatement(strSQL4);

        String strSQL5 = "UPDATE almacenesEmpaques "
                + "SET existenciaOficina=existenciaOficina+? "
                + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=?";
        PreparedStatement ps5 = this.cnx.prepareStatement(strSQL5);

        String strSQL6 = "INSERT INTO empresasEmpaques (idEmpresa, idEmpaque, costoUnitarioPromedio, existenciaOficina, idMovtoUltimaEntrada) "
                + "VALUES (" + m.getIdEmpresa() + ", ?, ?, ?, ?)";
        PreparedStatement ps6 = this.cnx.prepareStatement(strSQL6);

        String strSQL7 = "UPDATE empresasEmpaques "
                + "SET existenciaOficina=existenciaOficina+?"
                + ", costoUnitarioPromedio=(existenciaOficina*costoUnitarioPromedio+?*?)/(existenciaOficina+?)"
                + ", idMovtoUltimaEntrada=? "
                + "WHERE idEmpresa=" + m.getIdEmpresa() + " AND idEmpaque=?";
        PreparedStatement ps7 = this.cnx.prepareStatement(strSQL7);

        ResultSet rs;
        int idEmpaque, idImpuestoGrupo;
        double existenciaAnterior;
        Statement st = this.cnx.createStatement();
        try {
//            capturados = 0;
            st.executeUpdate("BEGIN TRANSACTION");

            if (m.getIdMovto() == 0) {
                nueva = true;
                m.setFolio(this.obtenerMovimientoFolio(true, m.getIdAlmacen(), 1));

                strSQL = "INSERT INTO movimientos (idTipo, idCedis, folio, idEmpresa, idAlmacen, idComprobante, idImpuestoZona, idMoneda, tipoCambio, desctoComercial, desctoProntoPago, idUsuario, fecha, estatus, idReferencia, referencia, propietario) "
                        + "VALUES (1, " + m.getIdCedis() + ", " + m.getFolio() + ", " + m.getIdEmpresa() + ", " + m.getIdAlmacen() + ", " + m.getIdComprobante() + ", " + m.getIdImpuestoZona() + ", " + m.getIdMoneda() + ", " + m.getTipoDeCambio() + ", " + m.getDesctoComercial() + ", " + m.getDesctoProntoPago() + ", " + this.idUsuario + ", getdate(), 1, " + m.getIdReferencia() + ", " + m.getReferencia() + ", 0)";
                st.executeUpdate(strSQL);

                rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
                if (rs.next()) {
                    m.setIdMovto(rs.getInt("idMovto"));
                }
//                if (m.getReferencia() != 0) {
//                    strSQL = "UPDATE ordenCompra SET propietario=0, estado=2 WHERE idOrdenCompra=" + m.getReferencia();
//                    st.executeUpdate(strSQL);
//                }
            } else {
                strSQL = "SELECT estatus FROM movimientos where idMovto=" + m.getIdMovto();
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    if (rs.getBoolean("estatus")) {
                        throw new SQLException("Ya se ha capturado y cerrado la entrada");
                    } else {
                        strSQL = "UPDATE movimientos SET estatus=1 WHERE idMovto=" + m.getIdMovto();
                        st.executeUpdate(strSQL);
                    }
                } else {
                    throw new SQLException("No se encontro el movimiento");
                }
                st.executeUpdate("UPDATE movimientos "
                        + "SET idMoneda=" + m.getIdMoneda() + ", tipoCambio=" + m.getTipoDeCambio() + " "
                        + ", desctoComercial=" + m.getDesctoComercial() + ", desctoProntoPago=" + m.getDesctoProntoPago() + " "
                        + ", fecha=GETDATE(), estatus=1 "
                        + "WHERE idMovto=" + m.getIdMovto());
            }

            //rs=st.executeQuery("select DATEPART(weekday, getdate()-1) AS DIA, DATEPART(week, GETDATE()) AS SEM, DATEPART(YEAR, GETDATE())%10 AS ANIO");
            //lote=""+rs.getInt("DIA")+String.format("%02d", rs.getInt("SEM"))+rs.getInt("ANIO")+"1";

            for (MovimientoProducto p : productos) {
                idEmpaque = p.getProducto().getIdProducto();

                if (p.getCantFacturada() > 0 || p.getCantSinCargo() > 0) {
//                    capturados++;
                    rs = st.executeQuery("SELECT existenciaOficina "
                            + "FROM almacenesEmpaques "
                            + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=" + idEmpaque);
                    if (rs.next()) {
                        existenciaAnterior = rs.getDouble("existenciaOficina");

                        ps5.setDouble(1, p.getCantFacturada() + p.getCantSinCargo());
                        ps5.setInt(2, idEmpaque);
                        ps5.executeUpdate();

//                        String strSQL7 = "UPDATE empresasEmpaques "
//                                        + "SET existenciaOficina=existenciaOficina+?"
//                                        + ", costoUnitarioPromedio=(existenciaOficina*costoUnitarioPromedio+?*?)/(existenciaOficina+?)"
//                                        + ", idMovtoUltimaEntrada=? "
//                                        + "WHERE idEmpresa=" + m.getIdEmpresa() + " AND idEmpaque=?";

                        ps7.setDouble(1, p.getCantFacturada() + p.getCantSinCargo());
                        ps7.setDouble(2, p.getCantFacturada() + p.getCantSinCargo());
                        ps7.setDouble(3, p.getCostoPromedio());
                        ps7.setDouble(4, p.getCantFacturada() + p.getCantSinCargo());
                        ps7.setInt(5, m.getIdMovto());
                        ps7.setInt(6, idEmpaque);
                        ps7.executeUpdate();
                    } else {
                        existenciaAnterior = 0;

                        ps4.setInt(1, m.getIdAlmacen());
                        ps4.setInt(2, idEmpaque);
                        ps4.setDouble(3, p.getCantFacturada() + p.getCantSinCargo());
                        ps4.executeUpdate();

                        rs = st.executeQuery("SELECT existenciaOficina "
                                + "FROM empresasEmpaques "
                                + "WHERE idEmpresa=" + m.getIdEmpresa() + " AND idEmpaque=" + idEmpaque);
                        if (rs.next()) {
                            ps7.setDouble(1, p.getCantFacturada() + p.getCantSinCargo());
                            ps7.setDouble(2, p.getCantFacturada() + p.getCantSinCargo());
                            ps7.setDouble(3, p.getCostoPromedio());
                            ps7.setDouble(4, p.getCantFacturada() + p.getCantSinCargo());
                            ps7.setInt(5, m.getIdMovto());
                            ps7.setInt(6, idEmpaque);
                            ps7.executeUpdate();
                        } else {
                            ps6.setInt(1, idEmpaque);
                            ps6.setDouble(2, p.getCostoPromedio());
                            ps6.setDouble(3, p.getCantFacturada() + p.getCantSinCargo());
                            ps6.setInt(4, m.getIdMovto());
                            ps6.executeUpdate();
                        }
                    }
//                    ps3.setInt(1, idEmpaque);
//                    ps3.setDouble(2, existenciaAnterior);
//                    ps3.setDouble(3, p.getCantFacturada()+p.getCantSinCargo());
//                    ps3.executeUpdate();
                    if (nueva) {
                        ps.setInt(1, m.getIdMovto());
                        ps.setInt(2, idEmpaque);
                        ps.setDouble(3, p.getCantFacturada());
                        ps.setDouble(4, p.getCantSinCargo());
                        ps.setDouble(5, p.getCostoPromedio());
                        ps.setDouble(6, p.getCosto());
                        ps.setDouble(7, p.getDesctoProducto1());
                        ps.setDouble(8, p.getDesctoProducto2());
                        ps.setDouble(9, p.getDesctoConfidencial());
                        ps.setDouble(10, p.getUnitario());
                        ps.setDouble(11, p.getCantOrdenada());
                        ps.setDouble(12, 0);
                        ps.setInt(13, p.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());
                        ps.setDouble(14, existenciaAnterior);
                        ps.executeUpdate();

                        idImpuestoGrupo = p.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo();
                        this.agregarImpuestosProducto(m.getIdMovto(), idEmpaque, idImpuestoGrupo, m.getIdImpuestoZona());
                        this.calculaImpuestosProducto(m.getIdMovto(), idEmpaque, p.getUnitario());
                    } else {
                        ps1.setDouble(1, p.getCosto());
                        ps1.setDouble(2, p.getDesctoProducto1());
                        ps1.setDouble(3, p.getDesctoProducto2());
                        ps1.setDouble(4, p.getDesctoConfidencial());
                        ps1.setDouble(5, p.getUnitario());
                        ps1.setDouble(6, p.getCantFacturada());
                        ps1.setDouble(7, p.getCantSinCargo());
                        ps1.setDouble(8, existenciaAnterior);
                        ps1.setInt(9, idEmpaque);
                        ps1.executeUpdate();
                    }
                    impuestos = p.getImpuestos();
                    for (ImpuestosProducto i : impuestos) {
                        ps2.setDouble(1, i.getImporte());
                        ps2.setInt(2, idEmpaque);
                        ps2.executeUpdate();
                    }
                }
            }
            if (m.getReferencia() != 0) {
                strSQL = "UPDATE OCD\n"
                        + "SET OCD.cantRecibidaOficina=OCD.cantRecibidaOficina+(MD.cantFacturada+MD.cantSinCargo)\n"
                        + "FROM ordenCompraDetalle OCD\n"
                        + "INNER JOIN movimientosDetalle MD ON MD.idMovto=" + m.getIdMovto() + " AND MD.idEmpaque=OCD.idEmpaque\n"
                        + "WHERE OCD.idOrdenCompra=" + m.getReferencia();
                st.executeUpdate(strSQL);

                strSQL = "SELECT SUM(CASE WHEN OCD.cantRecibidaOficina < (OCD.cantOrdenada+OCD.cantOrdenadaSinCargo) THEN 1 ELSE 0 END) AS faltantes\n"
                        + "FROM ordenCompraDetalle OCD\n"
                        + "WHERE OCD.idOrdenCompra=" + m.getReferencia();
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    if (rs.getInt("faltantes") == 0) {
                        strSQL = "UPDATE OC SET estado=3 FROM ordenCompra OC WHERE OC.idOrdenCompra=" + m.getReferencia();
                        st.executeUpdate(strSQL);
                    }
                } else {
                    throw new SQLException("No se encontro orden de compra");
                }
            }
//            if (capturados == 0) {
//                st.executeUpdate("UPDATE comprobantes SET statusOficina=0 WHERE idComprobante=" + m.getIdComprobante());
//            }
            st.executeUpdate("COMMIT TRANSACTION");
//            ok = true;
        } catch (SQLException e) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw (e);
        } finally {
            st.close();
            this.cnx.close();
        }
//        return ok;
    }

    private void actualizarExistenciaOficina(int idMovto) throws SQLException {
        String strSQL;
        Statement st = this.cnx.createStatement();
        try {
            strSQL = "UPDATE E\n"
                    + "SET E.existenciaOficina=E.existenciaOficina-(D.cantFacturada+D.cantSinCargo)\n"
                    + "FROM almacenesEmpaques E\n"
                    + "INNER JOIN movimientosDetalle D ON D.idMovto=" + idMovto + " AND D.idEmpaque=E.idEmpaque\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "WHERE E.idAlmacen=M.idAlmacen";
            st.executeUpdate(strSQL);

            strSQL = "UPDATE E\n"
                    + "SET E.existenciaOficina=E.existenciaOficina-(D.cantFacturada+D.cantSinCargo)\n"
                    + "FROM empresasEmpaques E\n"
                    + "INNER JOIN movimientosDetalle D ON D.idMovto=" + idMovto + " AND D.idEmpaque=E.idEmpaque\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "WHERE E.idEmpresa=M.idEmpresa";
            st.executeUpdate(strSQL);

            strSQL = "UPDATE E\n"
                    + "SET E.costoUnitarioPromedio=0\n"
                    + "FROM empresasEmpaques E\n"
                    + "INNER JOIN movimientosDetalle D ON D.idMovto=" + idMovto + " AND D.idEmpaque=E.idEmpaque\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "WHERE E.idEmpresa=M.idEmpresa AND E.existenciaOficina=0";
            st.executeUpdate(strSQL);
        } catch (SQLException ex) {
            st.close();
            throw ex;
        }
    }

    private void validarExistenciaOficina(int idMovto) throws SQLException {
        String strSQL;
        Statement st = this.cnx.createStatement();
        try {
            strSQL = "SELECT E.*, E.existenciaOficina-E.separados-(D.cantFacturada+D.cantSinCargo) AS disponibles\n"
                    + "FROM almacenesEmpaques E\n"
                    + "INNER JOIN movimientosDetalle D ON D.idMovto=" + idMovto + " AND D.idEmpaque=E.idEmpaque\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "WHERE E.idAlmacen=M.idAlmacen AND E.existenciaOficina-E.separados<(D.cantFacturada-D.cantSinCargo)";
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                throw new SQLException("Existencia insuficiente en almacen para realizar movimiento !!!");
            }
            strSQL = "SELECT E.*, E.existenciaOficina-(D.cantFacturada+D.cantSinCargo) AS disponibles\n"
                    + "FROM empresasEmpaques E\n"
                    + "INNER JOIN movimientosDetalle D ON D.idMovto=" + idMovto + " AND D.idEmpaque=E.idEmpaque\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "WHERE E.idEmpresa=M.idEmpresa AND E.existenciaOficina<(D.cantFacturada-D.cantSinCargo)";
            rs = st.executeQuery(strSQL);
            if (rs.next()) {
                throw new SQLException("Existencia insuficiente en empresa para realizar movimiento !!!");
            }
        } catch (SQLException ex) {
            st.close();
            throw ex;
        }
    }

    public void cancelarCompraAlmacen(int idMovto, int idAlmacen, int idOrdenDeCompra) throws SQLException {
        String strSQL;
        int idMovtoTipo = 34;
        int idMovtoCancelacion;
        this.cnx = this.ds.getConnection();
        Statement st = this.cnx.createStatement();
        try {
            st.execute("BEGIN TRANSACTION");

            strSQL = "SELECT M.idAlmacen, MD.idEmpaque, MD.lote, L.saldo-L.separados AS saldo, MD.cantidad\n"
                    + "FROM movimientosDetalleAlmacen MD\n"
                    + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=MD.idMovtoAlmacen\n"
                    + "INNER JOIN almacenesLotes L ON L.idAlmacen=M.idAlmacen AND L.idEmpaque=MD.idEmpaque AND L.lote=MD.lote\n"
                    + "WHERE MD.idMovtoAlmacen=" + idMovto + " AND L.saldo-L.separados < MD.cantidad";
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                throw new SQLException("Existencia insuficiente en almacen para realizar movimiento !!!");
            }
            strSQL = "UPDATE movimientosAlmacen SET estatus=3 WHERE idMovtoAlmacen=" + idMovto;
            st.executeUpdate(strSQL);

            int folio = this.obtenerMovimientoFolio(false, idAlmacen, idMovtoTipo);
            strSQL = "INSERT INTO movimientosAlmacen (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, fecha, idUsuario, idReferencia, referencia, propietario, estatus)\n"
                    + "SELECT " + idMovtoTipo + ", idCedis, idEmpresa, idAlmacen, " + folio + ", idComprobante, GETDATE(), " + this.idUsuario + ", idReferencia, referencia, 0, 1\n"
                    + "FROM movimientosAlmacen WHERE idMovtoAlmacen=" + idMovto;
            st.executeUpdate(strSQL);

            idMovtoCancelacion = 0;
            rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
            if (rs.next()) {
                idMovtoCancelacion = rs.getInt("idMovto");
            }
            strSQL = "INSERT INTO movimientosDetalleAlmacen\n"
                    + "SELECT " + idMovtoCancelacion + ", MD.idEmpaque, MD.lote, M.idAlmacen, MD.cantidad, 0, GETDATE(), L.saldo\n"
                    + "FROM movimientosDetalleAlmacen MD\n"
                    + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=MD.idMovtoAlmacen\n"
                    + "INNER JOIN almacenesLotes L ON L.idAlmacen=M.idAlmacen AND L.idEmpaque=MD.idEmpaque AND L.lote=MD.lote\n"
                    + "WHERE MD.idMovtoAlmacen=" + idMovto;
            st.executeUpdate(strSQL);

            strSQL = "UPDATE L\n"
                    + "SET L.saldo=L.saldo-MD.cantidad\n"
                    + "FROM movimientosDetalleAlmacen MD\n"
                    + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=MD.idMovtoAlmacen\n"
                    + "INNER JOIN almacenesLotes L ON L.idAlmacen=M.idAlmacen AND L.idEmpaque=MD.idEmpaque AND L.lote=MD.lote\n"
                    + "WHERE MD.idMovtoAlmacen=" + idMovto;
            st.executeUpdate(strSQL);

            if (idOrdenDeCompra != 0) {
                strSQL = "UPDATE OCD\n"
                        + "SET OCD.cantRecibidaAlmacen=OCD.cantRecibidaAlmacen-MD.cantidad\n"
                        + "FROM ordenCompraDetalle OCD\n"
                        + "INNER JOIN movimientosDetalleAlmacen MD ON MD.idMovto=" + idMovto + " AND MD.idEmpaque=OCD.idEmpaque\n"
                        + "WHERE OCD.idOrdenCompra=" + idOrdenDeCompra;
                st.executeUpdate(strSQL);

                strSQL = "SELECT SUM(CASE WHEN OCD.cantRecibidaAlmacen < (OCD.cantOrdenada+OCD.cantOrdenadaSinCargo) THEN 1 ELSE 0 END) AS faltantes\n"
                        + "FROM ordenCompraDetalle OCD\n"
                        + "WHERE OCD.idOrdenCompra=" + idOrdenDeCompra;
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    if (rs.getInt("faltantes") == 0) {
                        strSQL = "UPDATE OC SET estado=2 FROM ordenCompra OC WHERE OC.idOrdenCompra=" + idOrdenDeCompra;
                        st.executeUpdate(strSQL);
                    }
                } else {
                    throw new SQLException("No se encontro orden de compra");
                }
            }
            st.execute("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.execute("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            this.cnx.close();
        }
    }

    public void cancelarCompra(int idMovto, int idAlmacen, int idOrdenDeCompra) throws SQLException {
        String strSQL;
        int idMovtoTipo = 34;
        int idMovtoCancelacion;
        this.cnx = this.ds.getConnection();
        Statement st = this.cnx.createStatement();
        try {
            st.execute("BEGIN TRANSACTION");

            this.validarExistenciaOficina(idMovto);

            strSQL = "UPDATE movimientos SET estatus=3 WHERE idMovto=" + idMovto;
            st.executeUpdate(strSQL);

            int folio = this.obtenerMovimientoFolio(true, idAlmacen, idMovtoTipo);
            strSQL = "INSERT INTO movimientos (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, idImpuestoZona, desctoComercial, desctoProntoPago, fecha, idUsuario, idMoneda, tipoCambio, idReferencia, referencia, propietario, estatus)\n"
                    + "SELECT " + idMovtoTipo + ", idCedis, idEmpresa, idAlmacen, " + folio + ", idComprobante, idImpuestoZona, desctoComercial, desctoProntoPago, GETDATE(), " + this.idUsuario + ", idMoneda, tipoCambio, idReferencia, referencia, 0, 1\n"
                    + "FROM movimientos WHERE idMovto=" + idMovto;
            st.executeUpdate(strSQL);

            idMovtoCancelacion = 0;
            ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
            if (rs.next()) {
                idMovtoCancelacion = rs.getInt("idMovto");
            }
            strSQL = "INSERT INTO movimientosDetalle\n"
                    + "SELECT " + idMovtoCancelacion + ", D.idEmpaque, D.cantOrdenada, D.cantFacturada, D.cantSinCargo, D.cantRecibida, EE.costoUnitarioPromedio, D.costo, D.desctoProducto1, D.desctoProducto2, D.desctoConfidencial, D.unitario, D.idImpuestoGrupo, GETDATE(), E.existenciaOficina\n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "INNER JOIN almacenesEmpaques E ON E.idAlmacen=M.idAlmacen AND E.idEmpaque=D.idEmpaque\n"
                    + "INNER JOIN empresasEmpaques EE ON EE.idEmpresa=M.idEmpresa AND EE.idEmpaque=D.idEmpaque\n"
                    + "WHERE D.idMovto=" + idMovto;
            st.executeUpdate(strSQL);

            strSQL = "INSERT INTO movimientosDetalleImpuestos\n"
                    + "SELECT " + idMovtoCancelacion + ", idEmpaque, idImpuesto, impuesto, valor, aplicable, modo, acreditable, importe, acumulable\n"
                    + "FROM movimientosDetalleImpuestos WHERE idMovto=" + idMovto;
            st.executeUpdate(strSQL);

            this.actualizarExistenciaOficina(idMovto);

            if (idOrdenDeCompra != 0) {
                strSQL = "UPDATE OCD\n"
                        + "SET OCD.cantRecibidaOficina=OCD.cantRecibidaOficina-(MD.cantFacturada+MD.cantSinCargo)\n"
                        + "FROM ordenCompraDetalle OCD\n"
                        + "INNER JOIN movimientosDetalle MD ON MD.idMovto=" + idMovto + " AND MD.idEmpaque=OCD.idEmpaque\n"
                        + "WHERE OCD.idOrdenCompra=" + idOrdenDeCompra;
                st.executeUpdate(strSQL);

                strSQL = "SELECT SUM(CASE WHEN OCD.cantRecibidaOficina < (OCD.cantOrdenada+OCD.cantOrdenadaSinCargo) THEN 1 ELSE 0 END) AS faltantes\n"
                        + "FROM ordenCompraDetalle OCD\n"
                        + "WHERE OCD.idOrdenCompra=" + idOrdenDeCompra;
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    if (rs.getInt("faltantes") == 0) {
                        strSQL = "UPDATE OC SET estado=2 FROM ordenCompra OC WHERE OC.idOrdenCompra=" + idOrdenDeCompra;
                        st.executeUpdate(strSQL);
                    }
                } else {
                    throw new SQLException("No se encontro orden de compra");
                }
            }
            st.execute("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.execute("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            this.cnx.close();
        }
    }

//  ==========================  MOVIMIENTOS  ================================================================
    private ArrayList<TOMovimientoProducto> obtenMovimientoDetalleAlmacen(int idMovtoAlmacen) throws SQLException {
        String strSQL = "SELECT idMovtoAlmacen AS idMovto, idEmpaque\n"
                + "   , 0 AS cantOrdenada, 0 AS cantOrdenadaSinCargo, 0 AS cantRecibida, 0 AS cantRecibidaSinCargo\n"
                + "   , cantidad AS cantFacturada, 0 AS cantSinCargo, 0 AS costoPromedio, 0 AS costo\n"
                + "   , 0 AS desctoConfidencial, 0 AS desctoProducto1, 0 AS desctoProducto2, 0 AS unitario\n"
                + "FROM movimientosDetalleAlmacen\n"
                + "WHERE idMovtoAlmacen=" + idMovtoAlmacen;
        ArrayList<TOMovimientoProducto> productos = new ArrayList<>();
        Statement st = this.cnx.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                productos.add(this.construirDetalle(rs));
            }
        } finally {
            st.close();
        }
        return productos;
    }

    public ArrayList<TOMovimientoProducto> obtenerMovimientoDetalleAlmacen(int idMovto) throws SQLException {
        ArrayList<TOMovimientoProducto> productos = new ArrayList<>();
        this.cnx = this.ds.getConnection();
        try {
            productos = obtenMovimientoDetalleAlmacen(idMovto);
        } finally {
            this.cnx.close();
        }
        return productos;
    }

    public TOMovimientoProducto construirDetalle(ResultSet rs) throws SQLException {
        TOMovimientoProducto to = new TOMovimientoProducto();
        to.setIdMovto(rs.getInt("idMovto"));
        to.setIdProducto(rs.getInt("idEmpaque"));
        to.setCantOrdenada(rs.getDouble("cantOrdenada"));
        to.setCantOrdenadaSinCargo(rs.getDouble("cantOrdenadaSinCargo"));
        to.setCantRecibida(rs.getDouble("cantRecibida"));
        to.setCantRecibidaSinCargo(rs.getDouble("cantRecibidaSinCargo"));
        to.setCantFacturada(rs.getDouble("cantFacturada"));
        to.setCantSinCargo(rs.getDouble("cantSinCargo"));
        to.setCosto(rs.getDouble("costo"));
        to.setDesctoProducto1(rs.getDouble("desctoProducto1"));
        to.setDesctoProducto2(rs.getDouble("desctoProducto2"));
        to.setDesctoConfidencial(rs.getDouble("desctoConfidencial"));
        to.setUnitario(rs.getDouble("unitario"));
        to.setCostoPromedio(rs.getDouble("costoPromedio"));
        return to;
    }

    private ArrayList<TOMovimientoProducto> obtenMovimientoDetalle(int idMovto) throws SQLException {
        String strSQL = "SELECT *, 0 AS cantOrdenadaSinCargo, 0 AS cantRecibidaSinCargo FROM movimientosDetalle WHERE idMovto=" + idMovto;
        ArrayList<TOMovimientoProducto> productos = new ArrayList<>();
        Statement st = this.cnx.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                productos.add(this.construirDetalle(rs));
            }
        } finally {
            st.close();
        }
        return productos;
    }

    public ArrayList<TOMovimientoProducto> obtenerMovimientoDetalle(int idMovto) throws SQLException {
        ArrayList<TOMovimientoProducto> productos = new ArrayList<>();
        this.cnx = this.ds.getConnection();
        try {
            productos = obtenMovimientoDetalle(idMovto);
        } finally {
            this.cnx.close();
        }
        return productos;
    }

    public TOMovimiento obtenerMovimientoRelacionado(int idComprobante) throws SQLException {
        TOMovimiento to = null;
        String strSQL = "SELECT M.*"
                + ", ISNULL(C.tipoComprobante, 0) AS tipoComprobante"
                + ", ISNULL(C.serie, '') AS  serie, ISNULL(C.numero, '') AS numero"
                + ", ISNULL(C.fecha, GETDATE()) AS fechaComprobante, ISNULL(C.propietario, 0) AS propietarioComprobante"
                + ", ISNULL(MA.idMovtoAlmacen, 0) AS idMovtoAlmacen, ISNULL(MA.fecha, GETDATE()) AS fechaAlmacen"
                + ", ISNULL(MA.idUsuario, 0) AS idUsuarioAlmacen, ISNULL(MA.estatus, 0) AS statusAlmacen "
                + "FROM movimientos M "
                + "LEFT JOIN comprobantes C ON C.idComprobante=M.idComprobante "
                + "LEFT JOIN movimientosRelacionados MR ON MR.idMovto=M.idMovto "
                + "INNER JOIN movimientosAlmacen MA ON MA.idMovtoAlmacen=MR.idMovtoAlmacen "
                + "WHERE M.idTipo=1 AND M.idComprobante=" + idComprobante;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                to = construirMovimientoRelacionado(rs);
            }
        } finally {
            st.close();
            cn.close();
        }
        return to;
    }

    private TOMovimiento construirMovimientoRelacionado(ResultSet rs) throws SQLException {
        TOMovimiento to = new TOMovimiento();
        to.setIdMovto(rs.getInt("idMovto"));
        to.setIdTipo(rs.getInt("idTipo"));
        to.setIdCedis(rs.getInt("idCedis"));
        to.setIdEmpresa(rs.getInt("idEmpresa"));
        to.setIdAlmacen(rs.getInt("idAlmacen"));
        to.setFolio(rs.getInt("folio"));
        to.setIdComprobante(rs.getInt("idComprobante"));
        to.setIdImpuestoZona(rs.getInt("idImpuestoZona"));
        to.setDesctoComercial(rs.getDouble("desctoComercial"));
        to.setDesctoProntoPago(rs.getDouble("desctoprontoPago"));
        to.setFecha(new java.util.Date(rs.getDate("fecha").getTime()));
        to.setIdUsuario(rs.getInt("idUsuario"));
        to.setIdMoneda(rs.getInt("idMoneda"));
        to.setTipoDeCambio(rs.getDouble("tipoCambio"));
        to.setIdReferencia(rs.getInt("idReferencia"));
        to.setReferencia(rs.getInt("referencia"));
        to.setPropietario(rs.getInt("propietario"));
        to.setEstatus(rs.getInt("estatus"));
        ///////////////////  DEL ALMACEN  ////////////////////////////////////////////
        to.setIdMovtoAlmacen(rs.getInt("idMovtoAlmacen"));
//        to.setFolioAlmacen(rs.getInt("folioAlmacen"));
//        to.setFechaAlmacen(new java.util.Date(rs.getDate("fechaAlmacen").getTime()));
//        to.setIdUsuarioAlmacen(rs.getInt("idUsuarioAlmacen"));
//        to.setPropietarioAlmacen(rs.getInt("propietarioAlmacen"));
//        to.setStatusAlmacen(rs.getInt("statusAlmacen"));
        ///////////////////  DEL COMPROBANTE  ////////////////////////////////////////////
//        to.setTipoComprobante(rs.getInt("tipoComprobante"));
//        to.setSerie(rs.getString("serie"));
//        to.setNumero(rs.getString("numero"));
//        to.setFechaComprobante(new java.util.Date(rs.getDate("fechaComprobante").getTime()));
        return to;
    }

    public ArrayList<TOMovimiento> obtenerMovimientosAlmacen(int idComprobante) throws SQLException {
        ArrayList<TOMovimiento> tos = new ArrayList<>();
        String strSQL = "SELECT M.*, M.idMovtoAlmacen AS idMovto\n"
                + "   , 0 AS idImpuestoZona, 0 as desctoComercial, 0 as desctoProntoPago, 0 as idMoneda, 1 as tipoCambio\n"
                + "FROM movimientosAlmacen M\n"
                + "WHERE M.idTipo=1 AND M.idComprobante=" + idComprobante;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                tos.add(construirMovimientoRelacionado(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return tos;
    }

    public ArrayList<TOMovimiento> obtenerMovimientosRelacionados(int idComprobante) throws SQLException {
        ArrayList<TOMovimiento> tos = new ArrayList<>();
        String strSQL = "SELECT M.*, ISNULL(MR.idMovtoAlmacen, 0) AS idMovtoAlmacen, ISNULL(MR.folioAlmacen, 0) AS folioAlmacen\n"
                + "	, ISNULL(MR.fechaAlmacen, '') AS fechaAlmacen, ISNULL(MR.idUsuarioAlmacen, 0) AS idUsuarioAlmacen\n"
                + "	, ISNULL(MR.propietarioAlmacen, 0) AS propietarioAlmacen, ISNULL(MR.estatusAlmacen, 0) AS estatusAlmacen\n"
                + "FROM (SELECT MR.idMovto, MA.idMovtoAlmacen, MA.folio AS folioAlmacen, MA.fecha AS fechaAlmacen\n"
                + "           , MA.idUsuario AS idUsuarioAlmacen, MA.propietario AS propietarioAlmacen, MA.estatus AS estatusAlmacen\n"
                + "       FROM movimientosRelacionados MR\n"
                + "       INNER JOIN movimientosAlmacen MA ON MA.idMovtoAlmacen=MR.idMovtoAlmacen) MR\n"
                + "RIGHT JOIN movimientos M ON M.idMovto=MR.idMovto\n"
                + "WHERE M.idTipo=1 AND M.idComprobante=" + idComprobante;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                tos.add(construirMovimientoRelacionado(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return tos;
    }

    private int obtenerMovimientoFolio(boolean oficina, int idAlmacen, int idTipo) throws SQLException {
        int folio;
        String tabla = "movimientosFoliosAlmacen";
        if (oficina) {
            tabla = "movimientosFolios";
        }
        Statement st = this.cnx.createStatement();
        try {
            ResultSet rs = st.executeQuery("SELECT folio FROM " + tabla + " WHERE idAlmacen=" + idAlmacen + " AND idTipo=" + idTipo);
            if (rs.next()) {
                folio = rs.getInt("folio");
                st.executeUpdate("UPDATE " + tabla + " SET folio=folio+1 WHERE idAlmacen=" + idAlmacen + " AND idTipo=" + idTipo);
            } else {
                folio = 1;
                st.executeUpdate("INSERT INTO " + tabla + " (idAlmacen, idTipo, folio) VALUES (" + idAlmacen + ", " + idTipo + ", 2)");
            }
        } finally {
            st.close();
        }
        return folio;
    }

//  ===============================================================================================
    public void cerrarOrdenDeCompra(boolean oficina, int idOrdenDeCompra) throws SQLException {
        String strSQL;
        if (oficina) {
            strSQL = "UPDATE ordenCompra SET estado=3 WHERE idOrdenCompra=" + idOrdenDeCompra;
        } else {
            strSQL = "UPDATE ordenCompra SET estadoAlmacen=3 WHERE idOrdenCompra=" + idOrdenDeCompra;
        }
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate(strSQL);
        } finally {
            st.close();
            cn.close();
        }
    }

    public ArrayList<TOMovimientoProducto> obtenerDetalleOrdenDeCompra(int idOrdenDeCompra, boolean oficina) throws SQLException {
        String strSQL;
        ArrayList<TOMovimientoProducto> productos = new ArrayList<>();
        if (oficina) {
            strSQL = "SELECT 0 AS idMovto, OCD.idEmpaque\n"
                    + "       , OCD.cantOrdenada, OCD.cantOrdenadaSinCargo\n"
                    + "	, ISNULL(MD.cantRecibida,0) AS cantRecibida, ISNULL(MD.cantRecibidaSinCargo, 0) AS cantRecibidaSinCargo\n"
                    + "       , OCD.costoOrdenado AS costo, 0 AS cantFacturada, 0 AS cantSinCargo\n"
                    + "	, OCD.descuentoProducto AS desctoProducto1, OCD.descuentoProducto2 AS desctoProducto2\n"
                    + "       , OCD.desctoConfidencial, 0 AS unitario, 0 AS costoPromedio\n"
                    + "FROM (SELECT MD.idEmpaque, SUM(MD.cantFacturada) AS cantRecibida, SUM(MD.cantSinCargo) AS cantRecibidaSinCargo\n"
                    + "		FROM movimientosDetalle MD\n"
                    + "		INNER JOIN movimientos M ON M.idMovto=MD.idMovto\n"
                    + "		WHERE M.referencia=" + idOrdenDeCompra + "\n"
                    + "		GROUP BY MD.idEmpaque) MD\n"
                    + "RIGHT JOIN ordenCompraDetalle OCD ON OCD.idEmpaque=MD.idEmpaque\n"
                    + "WHERE OCD.idOrdenCompra=" + idOrdenDeCompra;
        } else {
            strSQL = "SELECT 0 AS idMovto, OCD.idEmpaque\n"
                    + "       , OCD.cantOrdenada, OCD.cantOrdenadaSinCargo\n"
                    + "	, ISNULL(MD.cantRecibida,0) AS cantRecibida, ISNULL(MD.cantRecibidaSinCargo, 0) AS cantRecibidaSinCargo\n"
                    + "       , OCD.costoOrdenado AS costo, 0 AS cantFacturada, 0 AS cantSinCargo\n"
                    + "	, OCD.descuentoProducto AS desctoProducto1, OCD.descuentoProducto2 AS desctoProducto2\n"
                    + "       , OCD.desctoConfidencial, 0 AS unitario, 0 AS costoPromedio\n"
                    + "FROM (SELECT MD.idEmpaque, SUM(MD.cantidad) AS cantRecibida, 0 AS cantRecibidaSinCargo\n"
                    + "		FROM movimientosDetalleAlmacen MD\n"
                    + "		INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=MD.idMovtoAlmacen\n"
                    + "		WHERE M.referencia=" + idOrdenDeCompra + "\n"
                    + "		GROUP BY MD.idEmpaque) MD\n"
                    + "RIGHT JOIN ordenCompraDetalle OCD ON OCD.idEmpaque=MD.idEmpaque\n"
                    + "WHERE OCD.idOrdenCompra=" + idOrdenDeCompra;
        }
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                if (rs.getDouble("cantRecibida") + rs.getDouble("cantRecibidaSinCargo") < rs.getDouble("cantOrdenada") + rs.getDouble("cantOrdenadaSinCargo")) {
                    productos.add(this.construirDetalle(rs));
                }
            }
        } finally {
            st.close();
            cn.close();
        }
        return productos;
    }

    public int obtenerIdOrdenCompra(int idComprobante, int idEntrada) throws SQLException {
        int idOrdenCompra = 0;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery("SELECT idOrdenCompra FROM comprobantesOrdenesCompra "
                    + "WHERE idComprobante=" + idComprobante + " AND idEntrada=" + idEntrada);
            if (rs.next()) {
                idOrdenCompra = rs.getInt("idOrdenCompra");
            }
        } finally {
            st.close();
            cn.close();
        }
        return idOrdenCompra;
    }

    public double obtenerPrecioUltimaCompra(int idEmpresa, int idEmpaque) throws SQLException {
        double precioLista = 0;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();

        String strSQL = "SELECT idMovtoUltimaEntrada FROM empresasEmpaques "
                + "WHERE idEmpresa=" + idEmpresa + " AND idEmpaque=" + idEmpaque;
        try {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                int idMovto = rs.getInt("idMovtoUltimaEntrada");

                strSQL = "SELECT costo FROM movimientosDetalle "
                        + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + idEmpaque;
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    precioLista = rs.getDouble("costo");
                }
            }
        } catch (SQLException e) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw (e);
        } finally {
            st.close();
            cn.close();
        }
        return precioLista;
    }

//  ===============================  IMPUESTOS  =========================================================
    private double obtenerImpuestosProductoPrivado(int idMovto, int idEmpaque, ArrayList<ImpuestosProducto> impuestos) throws SQLException {
        double importeImpuestos = 0.00;
        ImpuestosProducto impuesto;
//        impuestos=new ArrayList<ImpuestosProducto>();
        String strSQL = "select idImpuesto, impuesto, valor, aplicable, modo, acreditable, importe, acumulable\n"
                + "from movimientosDetalleImpuestos\n"
                + "where idMovto=" + idMovto + " and idEmpaque=" + idEmpaque + "\n"
                + "order by acumulable";
        Statement st = this.cnx.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                impuesto = construirImpuestosProducto(rs);
                importeImpuestos += impuesto.getImporte();
                impuestos.add(impuesto);
            }
        } finally {
            st.close();
        }
        return importeImpuestos;
    }

    public double obtenerImpuestosProducto(int idMovto, int idEmpaque, ArrayList<ImpuestosProducto> impuestos) throws SQLException {
        double importeImpuestos;
        this.cnx = this.ds.getConnection();
        try {
            importeImpuestos = this.obtenerImpuestosProductoPrivado(idMovto, idEmpaque, impuestos);
        } finally {
            this.cnx.close();
        }
        return importeImpuestos;
    }

    private void calculaImpuestosProducto(int idMovto, int idEmpaque, double unitario) throws SQLException {
        String strSQL;
        Statement st = this.cnx.createStatement();
        try {
            strSQL = "UPDATE d\n"
                    + "SET d.importe=CASE WHEN d.aplicable=0 THEN 0 WHEN d.modo=1 THEN " + unitario + "*valor/100.00 ELSE e.piezas*valor END\n"
                    + "FROM movimientosDetalleImpuestos d\n"
                    + "INNER JOIN empaques e ON e.idEmpaque=d.idEmpaque\n"
                    + "WHERE d.idMovto=" + idMovto + " AND d.idEmpaque=" + idEmpaque + " AND d.acumulable=1";
            st.executeUpdate(strSQL);

            strSQL = "UPDATE d\n"
                    + "SET importe=CASE WHEN aplicable=0 THEN 0 WHEN modo=1 THEN (" + unitario + "+COALESCE(a.acumulable, 0))*valor/100.00 ELSE e.piezas*valor END\n"
                    + "FROM movimientosDetalleImpuestos d\n"
                    + "INNER JOIN empaques e on e.idEmpaque=d.idEmpaque\n"
                    + "LEFT JOIN (SELECT idMovto, idEmpaque, SUM(importe) AS acumulable\n"
                    + "             FROM movimientosDetalleImpuestos\n"
                    + "             WHERE idMovto=" + idMovto + " AND idEmpaque=" + idEmpaque + " AND acumulable=1\n"
                    + "             GROUP BY idMovto, idEmpaque) a ON a.idMovto=d.idMovto AND a.idEmpaque=d.idEmpaque\n"
                    + "WHERE d.idMovto=" + idMovto + " AND d.idEmpaque=" + idEmpaque + " AND d.acumulable=0";
            st.executeUpdate(strSQL);
        } finally {
            st.close();
        }
    }

    private void agregarImpuestosProducto(int idMovto, int idEmpaque, int idImpuestoGrupo, int idZona) throws SQLException {
        String strSQL = "insert into movimientosDetalleImpuestos (idMovto, idEmpaque, idImpuesto, impuesto, valor, aplicable, modo, acreditable, importe, acumulable) "
                + "select " + idMovto + ", " + idEmpaque + ", id.idImpuesto, i.impuesto, id.valor, i.aplicable, i.modo, i.acreditable, 0.00 as importe, i.acumulable "
                + "from impuestosDetalle id "
                + "inner join impuestos i on i.idImpuesto=id.idImpuesto "
                + "where id.idGrupo=" + idImpuestoGrupo + " and id.idZona=" + idZona + " and GETDATE() between fechaInicial and fechaFinal";
        Statement st = this.cnx.createStatement();
        try {
            if (st.executeUpdate(strSQL) == 0) {
                throw (new SQLException("No se generaron impuestos !!!"));
            }
        } finally {
            st.close();
        }
    }

    private ImpuestosProducto construirImpuestosProducto(ResultSet rs) throws SQLException {
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

    public ArrayList<ImpuestosProducto> generarImpuestosProducto(int idImpuestoGrupo, int idZona) throws SQLException {
        ArrayList<ImpuestosProducto> impuestos = new ArrayList<>();
        String strSQL = "SELECT id.idImpuesto, i.impuesto, id.valor, i.aplicable, i.modo, i.acreditable, 0.00 as importe, i.acumulable\n"
                + "FROM impuestosDetalle id\n"
                + "INNER JOIN impuestos i ON i.idImpuesto=id.idImpuesto\n"
                + "WHERE id.idGrupo=" + idImpuestoGrupo + " and id.idZona=" + idZona + " and GETDATE() between id.fechaInicial and id.fechaFinal\n"
                + "ORDER BY i.acumulable";
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                impuestos.add(this.construirImpuestosProducto(rs));
            }
            if (impuestos.isEmpty()) {
                throw new SQLException("No se generaron impuestos !!!");
            }
        } finally {
            st.close();
            cn.close();
        }
        return impuestos;
    }
}
