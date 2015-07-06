package movimientos.dao;

import entradas.dominio.MovimientoAlmacenProducto;
import entradas.dominio.MovimientoProducto;
import entradas.to.TOEntradaProducto;
import impuestos.dominio.ImpuestosProducto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import movimientos.dominio.Lote;
import movimientos.dominio.MovimientoTipo;
import movimientos.to.TOMovimiento;
import movimientos.to.TOMovimientoAlmacen;
import movimientos.to.TOMovimientoAlmacenProducto;
import movimientos.to.TOMovimientoProducto;
import pedidos.to.TOPedido;
import pedidos.to.TOPedidoProducto;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOMovimientos {

    int idUsuario, idCedis;
    private DataSource ds = null;

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

    // -------------------------------------- PEDIDOS ----------------------------------
    private void agregaMovimientoRelacionado(Connection cn, TOMovimiento to) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            to.setFolio(this.obtenerMovimientoFolio(cn, true, to.getIdAlmacen(), to.getIdTipo()));
            strSQL = "INSERT INTO movimientos (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, idImpuestoZona, desctoComercial, desctoProntoPago, fecha, idUsuario, idMoneda, tipoCambio, idReferencia, referencia, estatus, propietario) "
                    + "VALUES(" + to.getIdTipo() + ", " + this.idCedis + ", " + to.getIdEmpresa() + ", " + to.getIdAlmacen() + ", "+to.getFolio()+", "+to.getIdComprobante()+", " + to.getIdImpuestoZona() + ", 0, 0, GETDATE(), " + to.getIdUsuario() + ", " + to.getIdMoneda() + ", " + to.getTipoDeCambio() + ", " + to.getIdReferencia() + ", " + to.getReferencia() + ", " + to.getEstatus() + ", " + to.getIdUsuario() + ")";
            st.executeUpdate(strSQL);
            ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
            if (rs.next()) {
                to.setIdMovto(rs.getInt("idMovto"));
            }
            to.setFolioAlmacen(this.obtenerMovimientoFolio(cn, false, to.getIdAlmacen(), to.getIdTipo()));
            strSQL = "INSERT INTO movimientosAlmacen (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, fecha, idReferencia, referencia, idUsuario, estatus, propietario) "
                    + "VALUES (" + to.getIdTipo() + ", " + this.idCedis + ", " + to.getIdEmpresa() + ", " + to.getIdAlmacen() + ", "+to.getFolioAlmacen()+", "+to.getIdComprobante()+", GETDATE(), " + to.getIdReferencia() + ", " + to.getReferencia() + ", " + to.getIdUsuario() + ", " + to.getEstatus() + ", " + to.getIdUsuario() + ")";
            st.executeUpdate(strSQL);
            rs = st.executeQuery("SELECT @@IDENTITY AS idMovtoAlmacen");
            if (rs.next()) {
                to.setIdMovtoAlmacen(rs.getInt("idMovtoAlmacen"));
            }
            strSQL = "INSERT INTO movimientosRelacionados (idMovto, idMovtoAlmacen) VALUES (" + to.getIdMovto() + ", " + to.getIdMovtoAlmacen() + ")";
            st.executeUpdate(strSQL);
        }
    }
    
    public void cerrarPedido(int idPedido) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL= "SELECT A.idEmpresa, A.idAlmacen, PT.idTienda, T.idImpuestoZona\n" +
                        "FROM pedidosOCTienda PT\n" +
                        "INNER JOIN pedidosOC P ON P.idPedidoOC=PT.idPedidoOC\n" +
                        "INNER JOIN clientesTiendas T ON T.idTienda=PT.idTienda\n" +
                        "INNER JOIN almacenes A ON A.idAlmacen=P.idAlmacen\n" +
                        "WHERE PT.idPedido=" + idPedido;
                ResultSet rs = st.executeQuery(strSQL);
                if(rs.next()) {
                    TOMovimiento toMv = new TOMovimiento();
                    toMv.setIdTipo(28);
                    toMv.setIdEmpresa(rs.getInt("idEmpresa"));
                    toMv.setIdAlmacen(rs.getInt("idAlmacen"));
                    toMv.setIdImpuestoZona(rs.getInt("idImpuestoZona"));
                    toMv.setIdMoneda(1);
                    toMv.setTipoDeCambio(1);
                    toMv.setIdReferencia(rs.getInt("idTienda"));
                    toMv.setReferencia(idPedido);
                    toMv.setIdUsuario(this.idUsuario);

                    this.agregaMovimientoRelacionado(cn, toMv);

                    TOMovimientoProducto toProd;
                    strSQL = "SELECT * FROM pedidosOCTiendaDetalle WHERE idPedido=" + idPedido;
                    rs = st.executeQuery(strSQL);
                    while (rs.next()) {
                        toProd = new TOMovimientoProducto();
                        toProd.setIdProducto(rs.getInt("idEmpaque"));
                        toProd.setCantOrdenada(rs.getDouble("cantFacturada"));
                        toProd.setIdImpuestoGrupo(rs.getInt("idImpuestoGrupo"));

                        this.agregarProductoOficina(cn, toMv.getIdEmpresa(), toProd, toMv.getIdImpuestoZona(), toMv.getIdReferencia());
                    }
                    strSQL = "UPDATE movimientos SET propietario=0 WHERE idMovto=" + toMv.getIdMovto();
                    st.executeUpdate(strSQL);

                    strSQL = "UPDATE movimientosAlmacen SET propietario=0 WHERE idMovtoAlmacen=" + toMv.getIdMovtoAlmacen();
                    st.executeUpdate(strSQL);

                    strSQL = "UPDATE pedidos SET estatus=1 WHERE idPedido=" + idPedido;
                    st.executeUpdate(strSQL);
                    
                    cn.commit();
                } else {
                    throw new SQLException("No se encontro cabecero del pedido !!!");
                }
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public TOPedidoProducto obtenerPedidoProducto(int idPedido, int idProducto) throws SQLException {
        TOPedidoProducto to = null;
        String strSQL = "SELECT * FROM pedidosOCTiendaDetalle WHERE idPedido=" + idPedido + " AND idEmpaque=" + idProducto;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    to = this.construirProducto(rs);
                }
            }
        }
        return to;
    }

    public ArrayList<TOPedidoProducto> obtenerSimilaresPedido(int idPedido, int idProducto) throws SQLException {
        ArrayList<TOPedidoProducto> productos = new ArrayList<>();
        String strSQL = "SELECT CASE WHEN S.idEmpaque=S.idSimilar THEN 1 ELSE 0 END AS principal\n"
                + "	, ISNULL(D.idPedido, 0) AS idPedido, ISNULL(D.idEmpaque,S.idEmpaque) AS idEmpaque\n"
                + "	, ISNULL(D.cantFacturada, 0) AS cantFacturada, ISNULL(D.cantSinCargo, 0) AS cantSinCargo\n"
                + "	, ISNULL(D.unitario, 0) AS unitario, P.idImpuesto AS idImpuestoGrupo\n"
                + "FROM empaquesSimilares S\n"
                + "LEFT JOIN (SELECT * FROM pedidosOCTiendaDetalle WHERE idPedido=" + idPedido + ") D ON D.idEmpaque=S.idEmpaque\n"
                + "INNER JOIN empaques E ON E.idEmpaque=S.idEmpaque\n"
                + "INNER JOIN productos P ON P.idProducto=E.idProducto\n"
                + "WHERE S.idSimilar=" + idProducto + " AND S.idSimilar!=S.idEmpaque\n"
                + "ORDER BY principal DESC, idPedido DESC";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    productos.add(this.construirProducto(rs));
                }
            }
        }
        return productos;
    }

    public ArrayList<TOPedidoProducto> obtenerPedidoSimilares(int idPedido, int idProducto) throws SQLException {
        ArrayList<TOPedidoProducto> productos = new ArrayList<>();
        String strSQL = "SELECT D.*\n"
                + "FROM empaquesSimilares S\n"
                + "INNER JOIN pedidosOCTiendaDetalle D ON D.idEmpaque=S.idEmpaque\n"
                + "WHERE D.idPedido=" + idPedido + " AND S.idSimilar=" + idProducto;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    productos.add(this.construirProducto(rs));
                }
            }
        }
        return productos;
    }

    private ArrayList<Double> obtenerBoletinSinCargo(Connection cn, int idEmpresa, int idProducto, int idTienda) throws SQLException {
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
                        + "WHERE B.idEmpresa=" + idEmpresa + "\n"
                        + "		AND ((B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=0 AND B.idFormato=0 AND B.idTienda=0)\n"
                        + "			 OR (B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=" + idCliente + " AND B.idFormato=0 AND B.idTienda=0)\n"
                        + "			 OR (B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=" + idCliente + " AND B.idFormato=" + idFormato + " AND B.idTienda=0)\n"
                        + "			 OR (B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=" + idCliente + " AND B.idFormato=" + idFormato + " AND B.idTienda=" + idTienda + "))\n"
                        + "		AND ((B.idGrupo=" + idGrupo + " AND B.idSubGrupo=0 AND B.idEmpaque=0) \n"
                        + "				OR (B.idGrupo=" + idGrupo + " AND B.idSubGrupo=" + idSubGrupo + " AND B.idEmpaque=0) \n"
                        + "				OR (B.idGrupo=" + idGrupo + " AND B.idSubGrupo=" + idSubGrupo + " AND B.idEmpaque=" + idProducto + "))\n"
                        + "		AND CONVERT(date, GETDATE()) BETWEEN B.iniVigencia AND B.finVigencia";
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    boletin.set(0, rs.getDouble("conCargo"));
                    boletin.set(1, rs.getDouble("sinCargo"));
                }
            } else {
                throw (new SQLException("No se encontro producto en detalle !!!"));
            }
        }
        return boletin;
    }

    public boolean grabarPedidoDetalle(TOPedido ped, int idImpuestoZona, TOPedidoProducto prod, double cantFacturadaOld) throws SQLException {
        double cantSolicitada, cantSeparada, cantLiberar, cantLiberada;
        double cantSinCargo, boletinConCargo, boletinSinCargo;
        int idProducto = prod.getIdProducto();
        boolean similares = false;
        String strSQL;

        ResultSet rs;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                if (prod.getCantFacturada() > cantFacturadaOld) {
                    cantSolicitada = prod.getCantFacturada() - cantFacturadaOld;

                    cantSeparada = cantSolicitada;
                    strSQL = "UPDATE pedidosOCTiendaDetalle "
                            + "SET cantFacturada=cantFacturada+" + cantSeparada + " "
                            + "WHERE idPedido=" + ped.getIdPedido() + " AND idEmpaque=" + idProducto;
                } else {
                    cantLiberar = cantFacturadaOld - prod.getCantFacturada();

                    cantLiberada = cantLiberar;
                    strSQL = "UPDATE pedidosOCTiendaDetalle "
                            + "SET cantFacturada=cantFacturada-" + cantLiberada + " "
                            + "WHERE idPedido=" + ped.getIdPedido() + " AND idEmpaque=" + idProducto;
                }
                st.executeUpdate(strSQL);

                ArrayList<Double> boletin = this.obtenerBoletinSinCargo(cn, ped.getIdEmpresa(), prod.getIdProducto(), ped.getIdTienda());
                boletinConCargo = boletin.get(0);
                boletinSinCargo = boletin.get(1);
                if (boletinConCargo > 0 && boletinSinCargo > 0) {
                    strSQL = "SELECT ISNULL(SUM(D.cantFacturada),0) AS cantFacturada, ISNULL(SUM(D.cantSinCargo),0) AS cantSinCargo\n"
                            + "FROM pedidosOCTiendaDetalle D\n"
                            + "INNER JOIN empaquesSimilares S ON S.idEmpaque=D.idEmpaque\n"
                            + "WHERE D.idPedido=" + ped.getIdPedido() + " AND S.idSimilar=" + prod.getIdProducto();
                    rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        similares = true;
                        cantSinCargo = ((int) (rs.getDouble("cantFacturada") / boletinConCargo)) * boletinSinCargo;
                        double cantSinCargoHay = rs.getDouble("cantSinCargo");
                        if (cantSinCargo > cantSinCargoHay) {
                            cantSolicitada = cantSinCargo - cantSinCargoHay;

                            cantSeparada = cantSolicitada;
                            strSQL = "UPDATE pedidosOCTiendaDetalle "
                                    + "SET cantSinCargo=cantSinCargo+" + cantSeparada + " "
                                    + "WHERE idPedido=" + ped.getIdPedido() + " AND idEmpaque=" + idProducto;
                            st.executeUpdate(strSQL);
                        } else if (cantSinCargo < cantSinCargoHay) {
                            double disponibles;
                            cantLiberar = cantSinCargoHay - cantSinCargo;

                            strSQL = "SELECT P.principal, P.idEmpaque, P.cantFacturada, P.cantSinCargo, P.unitario, P.idImpuesto\n"
                                    + "FROM (SELECT CASE WHEN D.idEmpaque=S.idSimilar THEN 1 ELSE 0 END AS principal\n"
                                    + "           , ISNULL(D.idEmpaque,S.idEmpaque) AS idEmpaque\n"
                                    + "           , ISNULL(D.cantFacturada, 0) AS cantFacturada, ISNULL(D.cantSinCargo, 0) AS cantSinCargo\n"
                                    + "           , ISNULL(D.unitario, 0) AS unitario, P.idImpuesto\n"
                                    + "       FROM (SELECT * FROM pedidosOCTiendaDetalle WHERE idPedido=" + ped.getIdPedido() + ") D\n"
                                    + "	RIGHT JOIN empaquesSimilares S ON S.idEmpaque=D.idEmpaque\n"
                                    + "	INNER JOIN empaques E ON E.idEmpaque=S.idEmpaque\n"
                                    + "	INNER JOIN productos P ON P.idProducto=E.idProducto\n"
                                    + "	WHERE S.idSimilar=" + idProducto + " AND D.idPedido IS NOT NULL AND D.cantSinCargo > 0) P\n"
                                    + "ORDER BY P.principal DESC, P.idEmpaque";
                            rs = st.executeQuery(strSQL);
                            while (rs.next()) {
                                cantSinCargo = ((int) (rs.getDouble("cantFacturada") / boletinConCargo)) * boletinSinCargo;
                                if (rs.getDouble("cantSinCargo") > cantSinCargo) {           // Si los que hay, son mas que los que debieran haber
                                    disponibles = rs.getDouble("cantSinCargo") - cantSinCargo;  // Entonces si hay disponibles

                                    if (disponibles <= cantLiberar) {
                                        cantLiberada = disponibles;
                                    } else {
                                        cantLiberada = cantLiberar;
                                    }
                                    strSQL = "UPDATE pedidosOCTiendaDetalle "
                                            + "SET cantSinCargo=cantSinCargo-" + cantLiberada + " "
                                            + "WHERE idPedido=" + ped.getIdPedido() + " AND idEmpaque=" + rs.getInt("idEmpaque");
                                    st.executeUpdate(strSQL);

                                    cantLiberar -= cantLiberada;
                                    if (cantLiberar == 0) {
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        cantSinCargo = ((int) (prod.getCantFacturada() / boletinConCargo)) * boletinSinCargo;
                        if (cantSinCargo > prod.getCantSinCargo()) {
                            cantSolicitada = cantSinCargo - prod.getCantSinCargo();

                            cantSeparada = cantSolicitada;
                            strSQL = "UPDATE pedidosOCTiendaDetalle "
                                    + "SET cantSinCargo=cantSinCargo+" + cantSeparada + " "
                                    + "WHERE idPedido=" + ped.getIdPedido() + " AND idEmpaque=" + idProducto;
                            st.executeUpdate(strSQL);

                            prod.setCantSinCargo(prod.getCantSinCargo() + cantSeparada);
                        } else if (prod.getCantSinCargo() < cantSinCargo) {
                            cantLiberar = prod.getCantSinCargo() - cantSinCargo;

                            cantLiberada = cantLiberar;
                            strSQL = "UPDATE pedidosOCTiendaDetalle "
                                    + "SET cantSinCargo=cantSinCargo-" + cantLiberada + " "
                                    + "WHERE idPedido=" + ped.getIdPedido() + " AND idEmpaque=" + rs.getInt("idEmpaque");
                            st.executeUpdate(strSQL);

                            prod.setCantSinCargo(prod.getCantSinCargo() - cantLiberada);
                        }
                    }
                }
                cn.commit();
            } catch (SQLException ex) {
                prod.setCantFacturada(cantFacturadaOld);
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return similares;
    }

    public double obtenerImpuestosPedidoProducto(int idPediedo, int idEmpaque, ArrayList<ImpuestosProducto> impuestos) throws SQLException {
        double importeImpuestos = 0;
        ImpuestosProducto impuesto;
        String strSQL = "select idImpuesto, impuesto, valor, aplicable, modo, acreditable, importe, acumulable\n"
                + "from pedidosOCDetalleImpuestos\n"
                + "where idPedido=" + idPediedo + " and idEmpaque=" + idEmpaque + "\n"
                + "order by acumulable";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    impuesto = construirImpuestosProducto(rs);
                    importeImpuestos += impuesto.getImporte();
                    impuestos.add(impuesto);
                }
            }
        }
        return importeImpuestos;
    }

    public TOPedidoProducto construirProducto(ResultSet rs) throws SQLException {
        TOPedidoProducto to = new TOPedidoProducto();
        to.setIdPedido(rs.getInt("idPedido"));
        to.setIdProducto(rs.getInt("idEmpaque"));
        to.setCantFacturada(rs.getDouble("cantFacturada"));
        to.setCantSinCargo(rs.getDouble("cantSinCargo"));
        to.setCostoPromedio(rs.getDouble("costoPromedio"));
        to.setCosto(rs.getDouble("costo"));
        to.setDesctoProducto1(rs.getDouble("desctoProducto1"));
        to.setDesctoProducto2(0);
        to.setDesctoConfidencial(0);
        to.setUnitario(rs.getDouble("unitario"));
        to.setIdImpuestoGrupo(rs.getInt("idImpuestoGrupo"));
        return to;
    }

    public ArrayList<TOPedidoProducto> obtenerPedidoDetalle(int idPedido) throws SQLException {
        ArrayList<TOPedidoProducto> productos = new ArrayList<>();
        String strSQL = "SELECT * FROM pedidosOCTiendaDetalle WHERE idPedido=" + idPedido;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    productos.add(this.construirProducto(rs));
                }
            }
        }
        return productos;
    }
    
    public void actualizarPedido(int idEmpresa, int idTienda, int idPedido) throws SQLException {
        int idProducto;
        double unitario, desctoProducto1, costo;
        String strSQL= "SELECT idProducto FROM pedidosOCTiendaDetalle WHERE idPedido="+idPedido;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while(rs.next()) {
                    idProducto=rs.getInt("idEmpaque");
                    
                    ArrayList<Double> precio = this.calcularPrecioNeto(cn, idEmpresa, idTienda, idProducto);
                    unitario=precio.get(0).doubleValue();
                    desctoProducto1=precio.get(1).doubleValue();
                    costo=precio.get(2).doubleValue();
                    
                    strSQL = "UPDATE pedidosOCTiendaDetalle\n"
                        + "SET unitario=" + unitario + ", desctoProducto1=" + desctoProducto1 + ", costo=" + costo + "\n"
                        + "WHERE idPedido=" + idPedido + " AND idEmpaque=" + idProducto;
                    st.executeUpdate(strSQL);
                    
                    this.calculaImpuestosPedidoProducto(cn, idPedido, idProducto, unitario);
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

    private void calculaImpuestosPedidoProducto(Connection cn, int idPedido, int idEmpaque, double unitario) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            strSQL = "UPDATE d\n"
                    + "SET d.importe=CASE WHEN d.aplicable=0 THEN 0 WHEN d.modo=1 THEN " + unitario + "*valor/100.00 ELSE e.piezas*valor END\n"
                    + "FROM pedidosOCDetalleImpuestos d\n"
                    + "INNER JOIN empaques e ON e.idEmpaque=d.idEmpaque\n"
                    + "WHERE d.idPedido=" + idPedido + " AND d.idEmpaque=" + idEmpaque + " AND d.acumulable=1";
            st.executeUpdate(strSQL);

            strSQL = "UPDATE d\n"
                    + "SET importe=CASE WHEN aplicable=0 THEN 0 WHEN modo=1 THEN (" + unitario + "+COALESCE(a.acumulable, 0))*valor/100.00 ELSE e.piezas*valor END\n"
                    + "FROM pedidosOCDetalleImpuestos d\n"
                    + "INNER JOIN empaques e on e.idEmpaque=d.idEmpaque\n"
                    + "LEFT JOIN (SELECT idPedido, idEmpaque, SUM(importe) AS acumulable\n"
                    + "             FROM pedidosOCDetalleImpuestos\n"
                    + "             WHERE idPedido=" + idPedido + " AND idEmpaque=" + idEmpaque + " AND acumulable=1\n"
                    + "             GROUP BY idPedido, idEmpaque) a ON a.idPedido=d.idPedido AND a.idEmpaque=d.idEmpaque\n"
                    + "WHERE d.idPedido=" + idPedido + " AND d.idEmpaque=" + idEmpaque + " AND d.acumulable=0";
            st.executeUpdate(strSQL);
        }
    }

    private ArrayList<Double> calcularPrecioNeto(Connection cn, int idEmpresa, int idTienda, int idProducto) throws SQLException {
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
                        + "FROM clientesListasPrecios B\n"
                        + "WHERE B.idEmpresa=" + idEmpresa + "\n"
                        + "		AND ((B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=0 AND B.idFormato=0 AND B.idTienda=0)\n"
                        + "			 OR (B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=" + idCliente + " AND B.idFormato=0 AND B.idTienda=0)\n"
                        + "			 OR (B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=" + idCliente + " AND B.idFormato=" + idFormato + " AND B.idTienda=0)\n"
                        + "			 OR (B.idGrupoCte=" + idGrupoCte + " AND B.idCliente=" + idCliente + " AND B.idFormato=" + idFormato + " AND B.idTienda=" + idTienda + "))\n"
                        + "		AND ((B.idGrupo=" + idGrupo + " AND B.idSubGrupo=0 AND B.idEmpaque=0) \n"
                        + "				OR (B.idGrupo=" + idGrupo + " AND B.idSubGrupo=" + idSubGrupo + " AND B.idEmpaque=0) \n"
                        + "				OR (B.idGrupo=" + idGrupo + " AND B.idSubGrupo=" + idSubGrupo + " AND B.idEmpaque=" + idProducto + "))\n"
                        + "		AND CONVERT(date, GETDATE()) BETWEEN B.iniVigencia AND B.finVigencia";
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    if (rs.getDouble("precioVenta") == 0) {
                        throw (new SQLException("No tiene precio de lista vigente !!!"));
                    } else {
                        precioUnitario = rs.getDouble("precioVenta");
                        if (!rs.getString("descuentos").equals("")) {
                            double descuento = 1.00;
                            for (String str : rs.getString("descuentos").split(",")) {
                                descuento = descuento * (1 - Double.parseDouble(str) / 100.00);
                            }
                            desctoProducto1 = (1.00 - descuento) * 100.00;
                        } else {
                            desctoProducto1 = 0.00;
                        }
                        precioLista = (precioUnitario / (1 - desctoProducto1 / 100.00));

                        precio.add(precioUnitario);
                        precio.add(desctoProducto1);
                        precio.add(precioLista);
                    }
                } else {
                    throw (new SQLException("No se encontro precio de venta !!!"));
                }
            } else {
                throw new SQLException("No se encotro el detalle de la tienda para obtener precio !!!");
            }
        }
        return precio;
    }

    private void agregarImpuestosPedidoProducto(Connection cn, int idPedido, int idEmpaque, int idImpuestoGrupo, int idImpuestoZona) throws SQLException {
        String strSQL = "insert into pedidosOCDetalleImpuestos (idPedido, idEmpaque, idImpuesto, impuesto, valor, aplicable, modo, acreditable, importe, acumulable) "
                + "select " + idPedido + ", " + idEmpaque + ", id.idImpuesto, i.impuesto, id.valor, i.aplicable, i.modo, i.acreditable, 0.00 as importe, i.acumulable "
                + "from impuestosDetalle id "
                + "inner join impuestos i on i.idImpuesto=id.idImpuesto "
                + "where id.idGrupo=" + idImpuestoGrupo + " and id.idZona=" + idImpuestoZona + " and GETDATE() between fechaInicial and fechaFinal";
        try (Statement st = cn.createStatement()) {
            if (st.executeUpdate(strSQL) == 0) {
                throw (new SQLException("No se generaron impuestos !!!"));
            }
        }
    }

    public void agregarPedidoProducto(int idEmpresa, int idTienda, int idImpuestoZona, TOPedidoProducto to) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "INSERT INTO pedidosOCTiendaDetalle (idPedido, idEmpaque, cantFacturada, cantSinCargo, costoPromedio, costo, desctoProducto1, unitario, idImpuestoGrupo)\n"
                        + "VALUES (" + to.getIdPedido() + ", " + to.getIdProducto() + ", " + to.getCantFacturada() + ", " + to.getCantSinCargo() + ", " + to.getUnitario() + ", " + to.getCosto() + ", " + to.getDesctoProducto1() + ", " + to.getUnitario() + ", " + to.getIdImpuestoGrupo() + ")";
                st.executeUpdate(strSQL);

                this.agregarImpuestosPedidoProducto(cn, to.getIdPedido(), to.getIdProducto(), to.getIdImpuestoGrupo(), idImpuestoZona);

                ArrayList<Double> precio = this.calcularPrecioNeto(cn, idEmpresa, idTienda, to.getIdProducto());
                to.setUnitario(precio.get(0).doubleValue());
                to.setDesctoProducto1(precio.get(1).doubleValue());
                to.setCosto(precio.get(2).doubleValue());

                strSQL = "UPDATE pedidosOCTiendaDetalle\n"
                        + "SET unitario=" + to.getUnitario() + ", desctoProducto1=" + to.getDesctoProducto1() + ", costo=" + to.getCosto() + "\n"
                        + "WHERE idPedido=" + to.getIdPedido() + " AND idEmpaque=" + to.getIdProducto();
                st.executeUpdate(strSQL);

                this.calculaImpuestosPedidoProducto(cn, to.getIdPedido(), to.getIdProducto(), to.getUnitario());

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    // ----------------------------- VENTAS -----------------------------------------
    private void agregarProductoOficina(Connection cn, int idEmpresa, TOMovimientoProducto to, int idZonaImpuestos, int idTienda) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantOrdenada, cantFacturada, cantSinCargo, cantRecibida, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior, costoPromedio) "
                    + "VALUES (" + to.getIdMovto() + ", " + to.getIdProducto() + ", " + to.getCantOrdenada() + ", " + to.getCantFacturada() + ", " + to.getCantSinCargo() + ", " + to.getCantRecibida() + ", " + to.getCosto() + ", " + to.getDesctoProducto1() + ", " + to.getDesctoProducto2() + ", " + to.getDesctoConfidencial() + ", " + to.getUnitario() + ", " + to.getIdImpuestoGrupo() + ", GETDATE(), 0, 0)";
            st.executeUpdate(strSQL);

            strSQL = "INSERT INTO movimientosDetalleImpuestos (idMovto, idEmpaque, idImpuesto, impuesto, valor, aplicable, modo, acreditable, importe, acumulable) "
                    + "SELECT " + to.getIdMovto() + ", " + to.getIdProducto() + ", ID.idImpuesto, I.impuesto, ID.valor, I.aplicable, I.modo, I.acreditable, 0.00 as importe, I.acumulable "
                    + "FROM impuestosDetalle ID "
                    + "INNER JOIN impuestos I on I.idImpuesto=ID.idImpuesto "
                    + "WHERE ID.idGrupo=" + to.getIdImpuestoGrupo() + " AND ID.idZona=" + idZonaImpuestos + " AND GETDATE() BETWEEN ID.fechaInicial AND ID.fechaFinal";
            if (st.executeUpdate(strSQL) == 0) {
                throw (new SQLException("No se insertaron impuestos !!!"));
            }
            ArrayList<Double> precio = this.calcularPrecioNeto(cn, idEmpresa, idTienda, to.getIdProducto());
            to.setUnitario(precio.get(0).doubleValue());
            to.setDesctoProducto1(precio.get(1).doubleValue());
            to.setCosto(precio.get(2).doubleValue());

            strSQL = "UPDATE movimientosDetalle\n"
                    + "SET costo=" + to.getCosto() + ",desctoProducto1=" + to.getDesctoProducto1() + ",unitario=" + to.getUnitario() + ",fecha=GETDATE()\n"
                    + "WHERE idMovto=" + to.getIdMovto() + " AND idEmpaque=" + to.getIdProducto();
            st.executeUpdate(strSQL);
        }
    }

    private ArrayList<TOMovimientoProducto> grabaMovimientoDetalle(Connection cn, boolean esVenta, int idEmpresa, int idAlmacen, int idMovto, int idMovtoAlmacen, TOMovimientoProducto to, int idTienda, double separados, int idZonaImpuestos) throws SQLException {
        ArrayList<TOMovimientoProducto> agregados = new ArrayList<>();
        double cantSolicitada, cantSeparada, cantLiberar, cantLiberada;
        int idProducto;
        String strSQL;
        try (Statement st = cn.createStatement()) {
            if (to.getIdMovto() == 0) {
                to.setIdMovto(idMovto);
                this.agregarProductoOficina(cn, idEmpresa, to, idZonaImpuestos, idTienda);
            } else if (to.getCantFacturada() + to.getCantSinCargo() != separados) {
                idProducto = to.getIdProducto();
                if (to.getCantFacturada() > (separados - to.getCantSinCargo())) {
                    cantSolicitada = to.getCantFacturada() - (separados - to.getCantSinCargo());
                    if (esVenta) {
                        // Falta validar si es con o sin pedido
//                        cantSeparada = this.separaRelacionados(idAlmacen, idMovtoAlmacen, idProducto, cantSolicitada, true);
                        cantSeparada = 0;
                        strSQL = "UPDATE movimientosDetalle\n"
                                + "SET cantFacturada=cantFacturada+" + cantSeparada + "\n"
                                + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + to.getIdProducto();
                    } else {
                        cantSeparada = cantSolicitada;
                        strSQL = "UPDATE pedidosOCTiendaDetalle "
                                + "SET cantFacturada=cantFacturada+" + cantSeparada + " "
                                + "WHERE idPedido=" + idMovto + " AND idEmpaque=" + idProducto;
                    }
                    st.executeUpdate(strSQL);
                } else {
                    cantLiberar = (separados - to.getCantSinCargo()) - to.getCantFacturada();
                    if (esVenta) {
                        // Falta validar si es con o sin pedido
//                        this.liberaRelacionados(idAlmacen, idMovtoAlmacen, idProducto, cantLiberar);
                        strSQL = "UPDATE movimientosDetalle\n"
                                + "SET cantFacturada=cantFacturada-" + cantLiberar + "\n"
                                + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + idProducto;
                    } else {
                        cantSeparada = cantLiberar;
                        strSQL = "UPDATE pedidosOCTiendaDetalle "
                                + "SET cantFacturada=cantFacturada-" + cantSeparada + " "
                                + "WHERE idPedido=" + idMovto + " AND idEmpaque=" + idProducto;
                    }
                    st.executeUpdate(strSQL);
                }
                ArrayList<Double> boletin = this.obtenerBoletinSinCargo(cn, idEmpresa, to.getIdProducto(), idTienda);
                double boletinConCargo = boletin.get(0);
                double boletinSinCargo = boletin.get(1);
                if (boletinConCargo > 0 && boletinSinCargo > 0) {
                    if (esVenta) {
                        // Falta validar si es con o sin pedido
                        strSQL = "SELECT SUM(D.cantFacturada) AS cantFacturada, SUM(D.cantSinCargo) AS cantSinCargo\n"
                                + "FROM (SELECT idEmpaque FROM empaquesSimilares WHERE idSimilar=" + idProducto + ") S\n"
                                + "INNER JOIN (SELECT * FROM movimientosDetalle WHERE idMovto=" + idMovto + ") D ON D.idEmpaque=S.idEmpaque";
                    } else {
                        strSQL = "SELECT SUM(D.cantFacturada) AS cantFacturada, SUM(D.cantSinCargo) AS cantSinCargo\n"
                                + "FROM (SELECT idEmpaque FROM empaquesSimilares WHERE idSimilar=" + idProducto + ") S\n"
                                + "INNER JOIN (SELECT * FROM pedidosOCTiendaDetalle WHERE idPedido=" + idMovto + ") D ON D.idEmpaque=S.idEmpaque";
                    }
                    ResultSet rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        double cantSinCargoSimilares = ((int) (rs.getDouble("cantFacturada") / boletinConCargo)) * boletinSinCargo;
                        if (cantSinCargoSimilares != rs.getDouble("cantSinCargo")) {
                            if (cantSinCargoSimilares > rs.getDouble("cantSinCargo")) {
                                cantSolicitada = cantSinCargoSimilares - rs.getDouble("cantSinCargo");
                                if (esVenta) {
                                    // Falta validar si es con o sin pedido
                                    strSQL = "SELECT P.principal, P.idPedido, P.idEmpaque, P.cantFacturada, P.cantSinCargo\n"
                                            + "       , P.unitario, P.idImpuesto, L.fechaCaducidad, L.saldo-L.separados AS disponibles\n"
                                            + "FROM (SELECT CASE WHEN D.idEmpaque=S.idSimilar THEN 1 ELSE 0 END AS principal\n"
                                            + "         , ISNULL(D.idPedido, 0) AS idPedido, ISNULL(D.idEmpaque,S.idEmpaque) AS idEmpaque\n"
                                            + "         , ISNULL(D.cantFacturada, 0) AS cantFacturada, ISNULL(D.cantSinCargo, 0) AS cantSinCargo\n"
                                            + "         , ISNULL(D.unitario, 0) AS unitario, P.idImpuesto\n"
                                            + "     FROM (SELECT * FROM pedidosOCTiendaDetalle WHERE idPedido=" + idMovto + ") D\n"
                                            + "     RIGHT JOIN empaquesSimilares S ON S.idEmpaque=D.idEmpaque\n"
                                            + "     INNER JOIN empaques E ON E.idEmpaque=S.idEmpaque\n"
                                            + "     INNER JOIN productos P ON P.idProducto=E.idProducto\n"
                                            + "     WHERE S.idSimilar=" + idProducto + ") P\n"
                                            + "LEFT JOIN almacenesLotes L ON L.idAlmacen=" + idAlmacen + " AND L.idEmpaque=P.idEmpaque\n"
                                            + "WHERE L.idAlmacen IS NOT NULL AND L.saldo-L.separados > 0\n"
                                            + "ORDER BY P.principal DESC, P.idPedido DESC, L.fechaCaducidad";
                                } else {
                                    strSQL = "SELECT 1 AS principal, idPedido, idEmpaque, cantFacturada, cantSinCargo, unitario, idImpuesto\n"
                                            + "FROM pedidosOCTiendaDetalle\n"
                                            + "WHERE idPedido=" + idMovto + " AND idEmpaque=" + idProducto;
                                }
                                rs = st.executeQuery(strSQL);
                                while (rs.next()) {
                                    if (esVenta) {
                                        // Falta validar si es con o sin pedido
//                                        cantSeparada = this.separaRelacionados(idAlmacen, idMovtoAlmacen, idProducto, cantSolicitada, true);
                                        cantSeparada = 0;
                                        strSQL = "UPDATE movimientosDetalle\n"
                                                + "SET cantFacturada=cantFacturada+" + cantSeparada + "\n"
                                                + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + rs.getInt("idEmpaque");
                                    } else {
                                        cantSeparada = cantSolicitada;
                                        strSQL = "UPDATE pedidosOCTiendaDetalle "
                                                + "SET cantSinCargo=cantSinCargo+" + cantSeparada + " "
                                                + "WHERE idPedido=" + idMovto + " AND idEmpaque=" + rs.getInt("idEmpaque");

                                    }
                                    st.executeUpdate(strSQL);
                                    cantSolicitada -= cantSeparada;
                                    if (cantSolicitada == 0) {
                                        break;
                                    }
                                }
                            } else {
                                cantLiberar = rs.getDouble("cantSinCargo") - cantSinCargoSimilares;
                                if (esVenta) {
                                } else {
                                    strSQL = "SELECT P.principal, P.idEmpaque, P.cantFacturada, P.cantSinCargo, P.unitario, P.idImpuesto*\n"
                                            + "FROM (SELECT CASE WHEN D.idEmpaque=S.idSimilar THEN 1 ELSE 0 END AS principal\n"
                                            + "		, ISNULL(D.idEmpaque,S.idEmpaque) AS idEmpaque\n"
                                            + "		, ISNULL(D.cantFacturada, 0) AS cantFacturada, ISNULL(D.cantSinCargo, 0) AS cantSinCargo\n"
                                            + "		, ISNULL(D.unitario, 0) AS unitario, P.idImpuesto\n"
                                            + "	FROM (SELECT * FROM pedidosOCTiendaDetalle WHERE idPedido=" + idMovto + ") D\n"
                                            + "	RIGHT JOIN empaquesSimilares S ON S.idEmpaque=D.idEmpaque\n"
                                            + "	INNER JOIN empaques E ON E.idEmpaque=S.idEmpaque\n"
                                            + "	INNER JOIN productos P ON P.idProducto=E.idProducto\n"
                                            + "	WHERE S.idSimilar=837 AND D.idPedido IS NOT NULL AND D.cantSinCargo > 0) P\n"
                                            + "ORDER BY P.principal DESC, P.idEmpaque";
                                }
                                rs = st.executeQuery(strSQL);
                                while (rs.next()) {
                                    if (esVenta) {
                                    } else {
                                        if (rs.getDouble("cantSinCargo") <= cantLiberar) {
                                            cantLiberada = rs.getDouble("cantSinCargo");
                                        } else {
                                            cantLiberada = cantLiberar;
                                        }
                                        strSQL = "UPDATE pedidosOCTiendaDetalle "
                                                + "SET cantSinCargo=cantSinCargo" + cantLiberada + " "
                                                + "WHERE idPedido=" + idMovto + " AND idEmpaque=" + rs.getInt("idEmpaque");
                                        cantLiberar -= cantLiberada;
                                        if (cantLiberar == 0) {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return agregados;
    }

    private void surteOrdenDeCompra(Connection cn, int idAlmacen, int idEmpresa, int idMovto, int idMovtoAlmacen, int idTienda, int idZonaImpuestos) throws SQLException {
        int idx;
        double separados;
        TOMovimientoProducto agregado;
        ArrayList<TOMovimientoProducto> agregados = new ArrayList<>();

        ArrayList<TOMovimientoProducto> detalle = this.obtenDetalle(cn, idMovto);
        for (TOMovimientoProducto to : detalle) {
            separados = to.getCantFacturada() + to.getCantSinCargo();
            if (separados != 0) {
                to.setCantFacturada(0);
            } else {
                to.setCantFacturada(to.getCantOrdenada());
            }
            for (TOMovimientoProducto toAgr : this.grabaMovimientoDetalle(cn, true, idAlmacen, idEmpresa, idMovto, idMovtoAlmacen, to, idTienda, separados, idZonaImpuestos)) {
                if ((idx = agregados.indexOf(toAgr)) != -1) {
                    agregado = agregados.get(idx);
                    agregado.setCantFacturada(agregado.getCantFacturada() + toAgr.getCantFacturada());
                    agregado.setCantSinCargo(agregado.getCantSinCargo() + toAgr.getCantSinCargo());
                } else {
                    agregados.add(toAgr);
                }
            }
        }
    }

    public void surtirOrdenDeCompra(boolean esVenta, int idAlmacen, int idMovto, int idMovtoAlmacen, int idZonaImpuestos) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                String strSQL;
                if (esVenta) {
                    strSQL = "SELECT A.idEmpresa, M.idReferencia AS idTienda\n"
                            + "FROM movimientos M\n"
                            + "INNER JOIN almacenes A ON A.idAlmacen=M.idAlmacen\n"
                            + "WHERE M.idMovto=" + idMovto;
                } else {
                    strSQL = "SELECT A.idEmpresa, P.idTienda\n"
                            + "FROM pedidosOC P\n"
                            + "INNER JOIN almacenes A ON A.idAlmacen=P.idAlmacen\n"
                            + "WHERE P.idPedido=" + idMovto;
                }
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    int idEmpresa = rs.getInt("idEmpresa");
                    int idTienda = rs.getInt("idTienda");
                    this.surteOrdenDeCompra(cn, idAlmacen, idEmpresa, idMovto, idMovtoAlmacen, idTienda, idZonaImpuestos);
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

    // ----------------------------- MOVIMIENTOS DE INVENTARIO ----------------------
    public void eliminarProductoEntradaAlmacen(int idMovtoAlmacen, int idProducto) throws SQLException {
        String strSQL = "DELETE FROM movimientosDetalleAlmacen\n"
                + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idProducto;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);
            }
        }
    }

    public void cancelarSalidaAlmacen(int idMovto) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "UPDATE l "
                        + "SET l.separados=l.separados-k.cantidad "
                        + "FROM movimientosDetalleAlmacen k "
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=k.idMovtoAlmacen "
                        + "INNER JOIN almacenesLotes l ON l.idAlmacen=M.idAlmacen AND l.idEmpaque=k.idEmpaque AND l.lote=k.lote "
                        + "WHERE k.idMovtoAlmacen=" + idMovto;
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalleAlmacen where idMovtoAlmacen=" + idMovto;
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosAlmacen WHERE idMovtoAlmacen=" + idMovto;
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

    public void grabarSalidaAlmacen(TOMovimiento to) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement(); Statement st1 = cn.createStatement()) {
                ResultSet rs1;
                to.setIdUsuario(this.idUsuario);
                to.setFolio(this.obtenerMovimientoFolio(cn, false, to.getIdAlmacen(), to.getIdTipo()));

                strSQL = "UPDATE movimientosAlmacen SET fecha=GETDATE(), estatus=1, folio=" + to.getFolio() + ", idUsuario=" + this.idUsuario + " "
                        + "WHERE idMovtoAlmacen=" + to.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "SELECT * FROM movimientosDetalleAlmacen "
                        + "WHERE idMovtoAlmacen=" + to.getIdMovto() + " "
                        + "ORDER BY idEmpaque";
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    strSQL = "SELECT saldo, separados FROM almacenesLotes "
                            + "WHERE idAlmacen=" + to.getIdAlmacen() + " AND idEmpaque=" + rs.getInt("idEmpaque") + " AND lote='" + rs.getString("lote") + "'";
                    rs1 = st1.executeQuery(strSQL);
                    if (rs1.next()) {
                        if (rs1.getDouble("saldo") < rs.getDouble("cantidad")) {
                            throw new SQLException("El saldo del lote es menor a la cantidad separada en kardex del movimiento");
                        } else if (rs1.getDouble("separados") < rs.getDouble("cantidad")) {
                            throw new SQLException("La cantidad separada del lote es menor a la cantidad separada en kardex del movimiento");
                        }
                    } else {
                        throw new SQLException("No se encontro lote(" + rs.getString("lote") + ") del empaque(" + rs.getInt("idEmpaque") + ") en tabla almacenesLotes");
                    }
                    strSQL = "UPDATE movimientosDetalleAlmacen "
                            + "SET fecha=GETDATE(), existenciaAnterior=" + rs1.getDouble("saldo") + " "
                            + "WHERE idMovtoAlmacen=" + to.getIdMovto() + " AND idEmpaque=" + rs.getInt("idEmpaque") + " AND lote='" + rs.getString("lote") + "'";
                    st1.executeUpdate(strSQL);

                    strSQL = "UPDATE almacenesLotes "
                            + "SET saldo=saldo-" + rs.getDouble("cantidad") + ", separados=separados-" + rs.getDouble("cantidad") + " "
                            + "WHERE idAlmacen=" + to.getIdAlmacen() + " AND idEmpaque=" + rs.getInt("idEmpaque") + " AND lote='" + rs.getString("lote") + "'";
                    st1.executeUpdate(strSQL);
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

    public void cancelarSalidaOficina(int idMovto) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "UPDATE e "
                        + "SET e.separados=e.separados-d.cantFacturada "
                        + "FROM (SELECT m.idAlmacen, d.* "
                        + "		FROM movimientosDetalle d "
                        + "		INNER JOIN movimientos m ON m.idMovto=d.idMovto "
                        + "		WHERE d.idMovto=" + idMovto + ") d "
                        + "INNER JOIN almacenesEmpaques e ON e.idAlmacen=d.idAlmacen AND e.idEmpaque=d.idEmpaque";
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalle where idMovto=" + idMovto;
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

    public void grabarSalidaOficina(TOMovimiento to) throws SQLException {
        String strSQL;
//        ArrayList<TOMovimientoProducto> detalle = new ArrayList<>();

        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                to.setIdUsuario(this.idUsuario);
                to.setFolio(this.obtenerMovimientoFolio(cn, true, to.getIdAlmacen(), to.getIdTipo()));

                strSQL = "UPDATE movimientos "
                        + "SET fecha=GETDATE(), estatus=1, folio=" + to.getFolio() + ", idUsuario=" + this.idUsuario + " "
                        + "WHERE idMovto=" + to.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalle "
                        + "WHERE idMovto=" + to.getIdMovto() + " AND cantFacturada=0";
                st.executeUpdate(strSQL);

                strSQL = "UPDATE d "
                        + "SET d.costoPromedio=e.costoUnitarioPromedio, d.costo=e.costoUnitarioPromedio, d.unitario=e.costoUnitarioPromedio"
                        + "     , d.fecha=GETDATE(), d.existenciaAnterior=a.existenciaOficina "
                        + "FROM movimientosDetalle d "
                        + "INNER JOIN movimientos m ON m.idMovto=d.idMovto "
                        + "INNER JOIN almacenesEmpaques a ON a.idAlmacen=m.idAlmacen AND a.idEmpaque=d.idEmpaque "
                        + "INNER JOIN empresasEmpaques e ON e.idEmpaque=d.idEmpaque "
                        + "WHERE e.idEmpresa=m.idEmpresa AND d.idMovto=" + to.getIdMovto();
                int n = st.executeUpdate(strSQL);

                strSQL = "SELECT idEmpaque "
                        + "FROM movimientosDetalle "
                        + "WHERE idMovto=" + to.getIdMovto() + " AND unitario <= 0";
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    throw new SQLException("(idMovto=" + to.getIdMovto() + ") Producto id=" + rs.getInt("idEmpaque") + " Sin costo unitario !!!");
                }
                strSQL = "UPDATE e "
                        + "SET e.separados=e.separados-d.cantFacturada, e.existenciaOficina=e.existenciaOficina-d.cantFacturada "
                        + "FROM (SELECT m.idAlmacen, d.* "
                        + "		FROM movimientosDetalle d "
                        + "		INNER JOIN movimientos m ON m.idMovto=d.idMovto "
                        + "		WHERE d.idMovto=" + to.getIdMovto() + ") d "
                        + "INNER JOIN almacenesEmpaques e ON e.idAlmacen=d.idAlmacen AND e.idEmpaque=d.idEmpaque "
                        + "WHERE e.separados >= d.cantFacturada AND e.existenciaOficina >= d.cantFacturada";
                if (st.executeUpdate(strSQL) != n) {
                    throw new SQLException("(idMovto=" + to.getIdMovto() + ") No se permite valor negativo en separados o existencia de almacen");
                }
                strSQL = "UPDATE e "
                        + "SET e.existenciaOficina=e.existenciaOficina-d.cantFacturada "
                        + "FROM (SELECT m.idEmpresa, d.* "
                        + "		FROM movimientosDetalle d "
                        + "		INNER JOIN movimientos m ON m.idMovto=d.idMovto "
                        + "		WHERE d.idMovto=" + to.getIdMovto() + ") d "
                        + "INNER JOIN empresasEmpaques e ON e.idEmpresa=d.idEmpresa AND e.idEmpaque=d.idEmpaque "
                        + "WHERE e.existenciaOficina >= d.cantFacturada";
                if (st.executeUpdate(strSQL) != n) {
                    throw new SQLException("(idMovto=" + to.getIdMovto() + ") No se permite valor negativo en separados o existencia de empresa");
                }
                strSQL = "UPDATE e "
                        + "SET e.costoUnitarioPromedio=0 "
                        + "FROM (SELECT m.idEmpresa, d.* "
                        + "		FROM movimientosDetalle d "
                        + "		INNER JOIN movimientos m ON m.idMovto=d.idMovto "
                        + "		WHERE d.idMovto=" + to.getIdMovto() + ") d "
                        + "INNER JOIN empresasEmpaques e ON e.idEmpresa=d.idEmpresa AND e.idEmpaque=d.idEmpaque "
                        + "WHERE e.existenciaOficina=0";
                st.executeUpdate(strSQL);

                cn.commit();
//                
//                strSQL="SELECT * FROM movimientosDetalle WHERE idMovto="+to.getIdMovto();
//                rs=st.executeQuery(strSQL);
//                while(rs.next()) {
//                    detalle.add(this.construirDetalle(rs));
//                }
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
//        return detalle;
    }

    public void agregarProductoSalidaOficina(int idMovto, TOMovimientoProducto to) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        String strSQL;
        try {
            strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantOrdenada, cantFacturada, cantSinCargo, cantRecibida, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior) "
                    + "VALUES (" + idMovto + ", " + to.getIdProducto() + ", 0, " + to.getCantFacturada() + ", 0, 0, 0, 0, 0, 0, 0, 0, 0, GETDATE(), 0)";
            st.executeUpdate(strSQL);
        } finally {
            st.close();
            cn.close();
        }
    }

    public void cancelarEntradaOficina(int idMovto) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "DELETE FROM movimientosDetalle where idMovto=" + idMovto;
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

    public void grabarEntradaOficina(TOMovimiento to) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                to.setIdUsuario(this.idUsuario);
                to.setFolio(this.obtenerMovimientoFolio(cn, true, to.getIdAlmacen(), to.getIdTipo()));

                strSQL = "UPDATE movimientos SET fecha=GETDATE(), estatus=1, folio=" + to.getFolio() + ", idUsuario=" + this.idUsuario + " "
                        + "WHERE idMovto=" + to.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalle WHERE idMovto=" + to.getIdMovto() + " AND cantFacturada=0";
                st.executeUpdate(strSQL);

                strSQL = "UPDATE d "
                        + "SET d.costoPromedio=dd.unitario, d.costo=dd.unitario, d.unitario=dd.unitario "
                        + "     , d.fecha=GETDATE(), d.existenciaAnterior=a.existenciaOficina "
                        + "FROM movimientosDetalle d "
                        + "INNER JOIN movimientos m ON m.idMovto=d.idMovto "
                        + "INNER JOIN almacenesEmpaques a ON a.idAlmacen=m.idAlmacen AND a.idEmpaque=d.idEmpaque "
                        + "INNER JOIN empresasEmpaques e ON e.idEmpresa=m.idEmpresa AND e.idEmpaque=d.idEmpaque "
                        + "INNER JOIN movimientosDetalle dd ON dd.idMovto=e.idMovtoUltimaEntrada AND dd.idEmpaque=d.idEmpaque "
                        + "WHERE d.idMovto=" + to.getIdMovto();
                int n = st.executeUpdate(strSQL);

                strSQL = "SELECT idEmpaque FROM movimientosDetalle WHERE idMovto=" + to.getIdMovto() + " AND unitario=0";
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    throw new SQLException("(idMovto=" + to.getIdMovto() + "), Producto id=" + rs.getInt("idEmpaque") + " Sin costo unitario !!!");
                }
                strSQL = "UPDATE a "
                        + "SET a.existenciaOficina=a.existenciaOficina+d.cantFacturada "
                        + "FROM (SELECT m.idAlmacen, d.* "
                        + "		FROM movimientosDetalle d "
                        + "		INNER JOIN movimientos m ON m.idMovto=d.idMovto "
                        + "		WHERE d.idMovto=" + to.getIdMovto() + ") d "
                        + "INNER JOIN almacenesEmpaques a ON a.idAlmacen=d.idAlmacen AND a.idEmpaque=d.idEmpaque";
                if (st.executeUpdate(strSQL) != n) {
                    throw new SQLException("No se encontro empaque en almacen !!!");
                }
                strSQL = "UPDATE e "
                        + "SET e.costoUnitarioPromedio=(e.existenciaOficina*e.costoUnitarioPromedio+d.cantFacturada*d.unitario)/(e.existenciaOficina+d.cantFacturada) "
                        + "    , e.existenciaOficina=e.existenciaOficina+d.cantFacturada "
                        + "FROM (SELECT m.idEmpresa, d.* "
                        + "		FROM movimientosDetalle d "
                        + "		INNER JOIN movimientos m ON m.idMovto=d.idMovto "
                        + "		WHERE d.idMovto=" + to.getIdMovto() + ") d "
                        + "INNER JOIN empresasEmpaques e ON e.idEmpresa=d.idEmpresa AND e.idEmpaque=d.idEmpaque "
                        + "WHERE e.existenciaOficina >= d.cantFacturada";
                if (st.executeUpdate(strSQL) != n) {
                    throw new SQLException("No se encontro empaque en empresa !!!");
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

    public double actualizaEntrada(int idMovto, int idProducto, double cantidad) throws SQLException {
        String strSQL = "UPDATE movimientosDetalle "
                + "SET cantFacturada=" + cantidad + " "
                + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + idProducto;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);
            }
        }
        return cantidad;
    }

    public void agregarProductoEntradaOficina(int idMovto, TOMovimientoProducto to) throws SQLException {
        String strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantOrdenada, cantFacturada, cantSinCargo, cantRecibida, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior) "
                + "VALUES (" + idMovto + ", " + to.getIdProducto() + ", 0, " + to.getCantFacturada() + ", 0, 0, 0, 0, 0, 0, 0, 0, 0, GETDATE(), 0)";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);
            }
        }
    }

    public int agregarMovimientoOficina(TOMovimiento to) throws SQLException {
        int idMovto = 0;
        String strSQL = "INSERT INTO movimientos (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, idImpuestoZona, desctoComercial, desctoProntoPago, fecha, idUsuario, idMoneda, tipoCambio, estatus, idReferencia, referencia, propietario) "
                + "VALUES(" + to.getIdTipo() + ", " + to.getIdCedis() + ", " + to.getIdEmpresa() + ", " + to.getIdAlmacen() + ", 0, 0, 0, 0, 0, GETDATE(), " + this.idUsuario + ", 1, 1, 0, 0, 0, 0)";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {

                st.executeUpdate(strSQL);
                ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
                if (rs.next()) {
                    idMovto = rs.getInt("idMovto");
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return idMovto;
    }

    public void cancelarEntradaAlmacen(int idMovto) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "DELETE FROM movimientosDetalleAlmacen where idMovtoAlmacen=" + idMovto;
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosAlmacen WHERE idMovtoAlmacen=" + idMovto;
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

    public void grabarEntradaAlmacen(TOMovimiento to) throws SQLException {
        double saldo;
        String strSQL;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 1900);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement(); Statement st1 = cn.createStatement()) {
                ResultSet rs, rs1;

                to.setIdUsuario(this.idUsuario);
                to.setFolio(this.obtenerMovimientoFolio(cn, false, to.getIdAlmacen(), to.getIdTipo()));

                strSQL = "UPDATE movimientosAlmacen SET fecha=GETDATE(), estatus=1, folio=" + to.getFolio() + ", idUsuario=" + this.idUsuario + " "
                        + "WHERE idMovtoAlmacen=" + to.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "SELECT D.*, ISNULL(L.fecha, '1900-01-01') AS fechaCaducidad \n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN lotes L ON L.lote=SUBSTRING(D.lote,1,4)\n"
                        + "WHERE D.idMovtoAlmacen=" + to.getIdMovto() + "\n"
                        + "ORDER BY D.idEmpaque, D.lote";
                rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    strSQL = "SELECT saldo FROM almacenesLotes "
                            + "WHERE idAlmacen=" + to.getIdAlmacen() + " AND idEmpaque=" + rs.getInt("idEmpaque") + " AND lote='" + rs.getString("lote") + "'";
                    rs1 = st1.executeQuery(strSQL);
                    if (rs1.next()) {
                        saldo = rs1.getDouble("saldo");
                        strSQL = "UPDATE almacenesLotes "
                                + "SET cantidad=cantidad+" + rs.getDouble("cantidad") + ", saldo=saldo+" + rs.getDouble("cantidad") + " "
                                + "WHERE idAlmacen=" + to.getIdAlmacen() + " AND idEmpaque=" + rs.getInt("idEmpaque") + " AND lote='" + rs.getString("lote") + "'";
                    } else if (rs.getDate("fechaCaducidad").equals(cal.getTime())) {
                        throw new SQLException("No se encontro lote='" + rs.getString("lote") + "' en catalogo de Lotes !!!");
                    } else {
                        saldo = 0;
                        strSQL = "INSERT INTO almacenesLotes (idAlmacen, idEmpaque, lote, fechaCaducidad, cantidad, saldo, separados, existenciaFisica) "
                                + "VALUES (" + to.getIdAlmacen() + ", " + rs.getInt("idEmpaque") + ", '" + rs.getString("lote") + "', '" + rs.getDate("fechaCaducidad") + "', " + rs.getDouble("cantidad") + ", " + rs.getDouble("cantidad") + ", 0, 0)";
                    }
                    st1.executeUpdate(strSQL);

                    strSQL = "UPDATE movimientosDetalleAlmacen "
                            + "SET fecha=GETDATE(), existenciaAnterior=" + saldo + " "
                            + "WHERE idMovtoAlmacen=" + to.getIdMovto() + " AND idEmpaque=" + rs.getInt("idEmpaque") + " AND lote='" + rs.getString("lote") + "'";
                    st1.executeUpdate(strSQL);
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

//    public ArrayList<TOMovimientoAlmacen> movimientosPendientesAlmacen(int entrada) throws SQLException {
//        ArrayList<TOMovimientoAlmacen> lista = new ArrayList<>();
//        String strSQL = "SELECT m.* "
//                + "FROM movimientosAlmacen m "
//                + "INNER JOIN movimientosTipos t ON t.idTipo=m.idTipo "
//                + "WHERE m.idCedis=" + this.idCedis + " AND m.estatus=0 AND t.eliminable=1 AND t.suma=" + entrada + " "
//                + "ORDER BY m.idAlmacen";
//        try (Connection cn = this.ds.getConnection()) {
//            try (Statement st = cn.createStatement()) {
//                ResultSet rs = st.executeQuery(strSQL);
//                while (rs.next()) {
//                        lista.add(this.construirMovimientoAlmacen(rs));
//                }
//            }
//        }
//        return lista;
//    }
//    
//    public ArrayList<TOMovimiento> movimientosPendientes(int entrada) throws SQLException {
//        ArrayList<TOMovimiento> lista = new ArrayList<>();
//        String strSQL = "SELECT m.* "
//                + "FROM movimientos m "
//                + "INNER JOIN movimientosTipos t ON t.idTipo=m.idTipo "
//                + "WHERE m.idCedis=" + this.idCedis + " AND m.estatus=0 AND t.eliminable=1 AND t.suma=" + entrada + " "
//                + "ORDER BY m.idAlmacen";
//        try (Connection cn = this.ds.getConnection()) {
//            try (Statement st = cn.createStatement()) {
//                ResultSet rs = st.executeQuery(strSQL);
//                while (rs.next()) {
//                        lista.add(this.construirMovimientoRelacionado(rs));
//                }
//            }
//        }
//        return lista;
//    }
    public int agregarMovimientoAlmacen(TOMovimiento to) throws SQLException {
        int idMovtoAlmacen = 0;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                String strSQL = "INSERT INTO movimientosAlmacen (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, fecha, idUsuario, estatus, idReferencia, referencia, propietario) "
                        + "VALUES (" + to.getIdTipo() + ", " + to.getIdCedis() + ", " + to.getIdEmpresa() + ", " + to.getIdAlmacen() + ", 0, 0, GETDATE(), " + this.idUsuario + ", 0, 0, 0, 0)";
                st.executeUpdate(strSQL);

                ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idMovtoAlmacen");
                if (rs.next()) {
                    idMovtoAlmacen = rs.getInt("idMovtoAlmacen");
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return idMovtoAlmacen;
    }

    public MovimientoTipo obtenerMovimientoTipo(int idTipo) throws SQLException {
        MovimientoTipo t = null;
        String strSQL = "SELECT tipo FROM movimientosTipos WHERE idTipo=" + idTipo;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    t = new MovimientoTipo(idTipo, rs.getString("tipo"));
                }
            }
        }
        return t;
    }

    public ArrayList<MovimientoTipo> obtenerMovimientosTipos(boolean suma) throws SQLException {
        ArrayList<MovimientoTipo> lst = new ArrayList<>();
        String strSQL = "SELECT idTipo, tipo FROM movimientosTipos WHERE suma=" + (suma ? 1 : 0) + " AND eliminable=1";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    lst.add(new MovimientoTipo(rs.getInt("idTipo"), rs.getString("tipo")));
                }
            }
        }
        return lst;
    }

    // ----------------------------- TRASPASOS --------------------------------------
    private void generaRechazoTraspaso(Connection cn, TOMovimiento to) throws SQLException {
        int idMovtoTraspaso = to.getReferencia(); // DE LA RECEPCION
        String strSQL = "SELECT MS.idAlmacen, DS.idEmpaque, DS.cantFacturada, DE.cantFacturada\n"
                + "FROM movimientosDetalle DS\n"
                + "INNER JOIN movimientos MS ON MS.idMovto=DS.idMovto\n"
                + "LEFT JOIN movimientosDetalle DE ON DE.idMovto=MS.referencia AND DE.idEmpaque=DS.idEmpaque\n"
                + "WHERE DS.idMovto=" + idMovtoTraspaso + " AND ISNULL(DE.cantFacturada,0) < DS.cantFacturada";
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                int idMovtoAlmacenTraspaso = 0;
                strSQL = "SELECT idMovtoAlmacen FROM movimientosRelacionados WHERE idMovto=" + idMovtoTraspaso;
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    idMovtoAlmacenTraspaso = rs.getInt("idMovtoAlmacen");
                } else {
                    throw new SQLException("No se encontro movimiento de almacen reclacionado !!!");
                }
                int idMovto = 0;
                int folio = this.obtenerMovimientoFolio(cn, true, to.getIdAlmacen(), 53);
                strSQL = "INSERT INTO movimientos (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, idImpuestoZona, desctoComercial, desctoProntoPago, fecha, idUsuario, idMoneda, tipoCambio, estatus, idReferencia, referencia, propietario) "
                        + "SELECT 53, M.idCedis, M.idEmpresa, M.idAlmacen, " + folio + ", 0, 0, 0, 0, GETDATE(), " + this.idUsuario + ", 1, 1, 1, M.idReferencia, M.idMovto, 0"
                        + "FROM movimientos M WHERE M.idMovto=" + idMovtoTraspaso;
                st.executeUpdate(strSQL);
                rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
                if (rs.next()) {
                    idMovto = rs.getInt("idMovto");
                }
                int idMovtoAlmacen = 0;
                int folioAlmacen = this.obtenerMovimientoFolio(cn, false, to.getIdAlmacen(), 53);
                strSQL = "INSERT INTO movimientosAlmacen (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, fecha, idUsuario, estatus, idReferencia, referencia, propietario) "
                        + "SELECT 53, M.idCedis, M.idEmpresa, M.idAlmacen, " + folioAlmacen + ", 0, GETDATE(), " + this.idUsuario + ", 1, M.idReferencia, idMovtoAlmacen, 0"
                        + "FROM movimientosAlmacen M WHERE M.idMovtoAlmacen=" + idMovtoAlmacenTraspaso;
                st.executeUpdate(strSQL);
                rs = st.executeQuery("SELECT @@IDENTITY AS idMovtoAlmacen");
                if (rs.next()) {
                    idMovtoAlmacen = rs.getInt("idMovtoAlmacen");
                }
                strSQL = "INSERT INTO movimientosRelacionados (idMovto, idMovtoAlmacen) VALUES (" + idMovto + ", " + idMovtoAlmacen + ")";
                st.executeUpdate(strSQL);

                strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantOrdenada, cantRecibida, cantFacturada, cantSinCargo, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior)\n"
                        + "SELECT " + idMovto + ", DS.idEmpaque, 0, 0, DS.cantFacturada-ISNULL(DE.cantFacturada,0) AS cantFacturada, 0, DS.unitario, DS.costo, 0, 0, 0, DS.unitario, DS.idImpuestoGrupo, GETDATE(), 0\n"
                        + "FROM movimientosDetalle DS\n"
                        + "INNER JOIN movimientos MS ON MS.idMovto=DS.idMovto\n"
                        + "LEFT JOIN movimientosDetalle DE ON DE.idMovto=MS.referencia AND DE.idEmpaque=DS.idEmpaque\n"
                        + "WHERE DS.idMovto=" + idMovtoTraspaso + " AND ISNULL(DE.cantFacturada,0) < DS.cantFacturada";
                int n = st.executeUpdate(strSQL);

                strSQL = "UPDATE D\n"
                        + "SET D.fecha=GETDATE(), D.existenciaAnterior=A.existenciaOficina\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + idMovto;
                if (st.executeUpdate(strSQL) != n) {
                    throw new SQLException("(idMovto=" + idMovto + ") No se encontro empaque en almacen !!!");
                }
                strSQL = "UPDATE A\n"
                        + "SET A.existenciaOficina=A.existenciaOficina+D.cantFacturada\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + idMovto;
                st.executeUpdate(strSQL);

                strSQL = "UPDATE E\n"
                        + "SET E.costoUnitarioPromedio=(E.costoUnitarioPromedio*E.existenciaOficina+D.costoPromedio*D.cantFacturada)/(E.existenciaOficina+D.cantFacturada)\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + idMovto;
                if (st.executeUpdate(strSQL) != n) {
                    throw new SQLException("(idMovto=" + idMovto + ") No se encontro empaque en empresa !!!");
                }
                strSQL = "UPDATE E\n"
                        + "SET E.existenciaOficina=E.existenciaOficina+D.cantFacturada\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + idMovto;
                st.executeUpdate(strSQL);

                strSQL = "INSERT INTO movimientosDetalleAlmacen (idMovtoAlmacen, idEmpaque, lote, cantidad, fecha, existenciaAnterior)\n"
                        + "SELECT " + idMovtoAlmacen + ", DS.idEmpaque, DS.lote, DS.cantidad-ISNULL(DE.cantidad,0) AS cantidad, GETDATE(), 0\n"
                        + "FROM movimientosDetalleAlmacen DS\n"
                        + "INNER JOIN movimientosAlmacen MS ON MS.idMovtoAlmacen=DS.idMovtoAlmacen\n"
                        + "LEFT JOIN movimientosDetalleAlmacen DE ON DE.idMovtoAlmacen=MS.referencia AND DE.idEmpaque=DS.idEmpaque AND DE.lote=DS.lote\n"
                        + "WHERE DS.idMovtoAlmacen=" + idMovtoAlmacenTraspaso + " AND ISNULL(DE.cantidad,0) < DS.cantidad";
                n = st.executeUpdate(strSQL);

                strSQL = "UPDATE D\n"
                        + "SET D.fecha=GETDATE(), D.existenciaAnterior=A.saldo\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                        + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen;
                if (st.executeUpdate(strSQL) != n) {
                    throw new SQLException("(idMovtoAlmacen=" + idMovtoAlmacen + ") No se encontro empaque-lote en almacen !!!");
                }
                strSQL = "UPDATE A\n"
                        + "SET A.saldo=A.saldo+D.cantidad\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                        + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen;
                st.executeUpdate(strSQL);
            }
        }
    }

    public void grabarTraspasoRecepcion(TOMovimiento m, ArrayList<TOEntradaProducto> detalle) throws SQLException {
        String strSQL;
        double existenciaAnterior, promedioPonderado, existenciaOficina, saldo;

        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs;
                m.setIdUsuario(this.idUsuario);

                strSQL = "UPDATE movimientosAlmacen "
                        + "SET fecha=GETDATE(), estatus=1 "
                        + "WHERE idMovtoAlmacen=" + m.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientos "
                        + "SET fecha=GETDATE(), estatus=1 "
                        + "WHERE idMovto=" + m.getIdMovto();
                st.executeUpdate(strSQL);

                for (TOEntradaProducto to : detalle) {
                    strSQL = "SELECT existenciaOficina "
                            + "FROM almacenesEmpaques "
                            + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=" + to.getIdProducto();
                    rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        existenciaAnterior = rs.getDouble("existenciaOficina");
                        strSQL = "UPDATE almacenesEmpaques SET existenciaOficina=existenciaOficina+" + to.getCantFacturada() + " "
                                + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=" + to.getIdProducto();
                    } else {
                        existenciaAnterior = 0;
                        strSQL = "INSERT INTO almacenesEmpaques (idAlmacen, idEmpaque, existenciaOficina, separados, existenciaAlmacen, existenciaMinima, existenciaMaxima) "
                                + "VALUES (" + m.getIdAlmacen() + ", " + to.getIdProducto() + ", " + to.getCantFacturada() + ", 0, 0, 0, 0)";
                    }
                    st.executeUpdate(strSQL);

                    strSQL = "UPDATE movimientosDetalle "
                            + "SET cantFacturada=" + to.getCantFacturada() + ", existenciaAnterior=" + existenciaAnterior + ", fecha=GETDATE() "
                            + "WHERE idMovto=" + m.getIdMovto() + " AND idEmpaque=" + to.getIdProducto();
                    st.executeUpdate(strSQL);

                    strSQL = "SELECT costoUnitarioPromedio, existenciaOficina\n"
                            + "FROM empresasEmpaques\n"
                            + "WHERE idEmpresa=" + m.getIdEmpresa() + " AND idEmpaque=" + to.getIdProducto();
                    rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        promedioPonderado = rs.getDouble("costoUnitarioPromedio");
                        existenciaOficina = rs.getDouble("existenciaOficina");
                        promedioPonderado = (promedioPonderado * existenciaOficina + to.getUnitario() * to.getCantFacturada()) / (existenciaOficina + to.getCantFacturada());
                        strSQL = "UPDATE empresasEmpaques "
                                + "SET costoUnitarioPromedio=" + promedioPonderado + ", existenciaOficina=existenciaOficina+" + to.getCantFacturada() + " "
                                + "WHERE idEmpresa=" + m.getIdEmpresa() + " AND idEmpaque=" + to.getIdProducto();
                    } else {
                        strSQL = "INSERT INTO empresasEmpaques (idEmpresa, idEmpaque, costoUnitarioPromedio, existenciaOficina, idMovtoUltimaEntrada) "
                                + "VALUES (" + m.getIdEmpresa() + ", " + to.getIdProducto() + ", " + to.getUnitario() + ", " + to.getCantFacturada() + ", 0)";
                    }
                    st.executeUpdate(strSQL);

                    for (Lote l : to.getLotes()) {
                        strSQL = "SELECT saldo FROM almacenesLotes "
                                + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=" + to.getIdProducto() + " AND lote='" + l.getLote() + "'";
                        rs = st.executeQuery(strSQL);
                        if (rs.next()) {
                            saldo = rs.getDouble("saldo");
                            strSQL = "UPDATE almacenesLotes "
                                    + "SET saldo=saldo+" + l.getSeparados() + ", cantidad=cantidad+" + l.getSeparados() + " "
                                    + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=" + to.getIdProducto() + " AND lote='" + l.getLote() + "'";
                        } else {
                            saldo = 0;
                            strSQL = "INSERT INTO almacenesLotes (idAlmacen, idEmpaque, lote, fechaCaducidad, cantidad, saldo, separados, existenciaFisica) "
                                    + "VALUES (" + m.getIdAlmacen() + ", " + to.getIdProducto() + ", '" + l.getLote() + "', '" + new java.sql.Date(l.getFechaCaducidad().getTime()) + "', " + l.getSeparados() + ", " + l.getSeparados() + ", 0, 0)";
                        }
                        st.executeUpdate(strSQL);

                        strSQL = "UPDATE movimientosDetalleAlmacen "
                                + "SET cantidad=" + l.getSeparados() + ", existenciaAnterior=" + saldo + ", fecha=GETDATE() "
                                + "WHERE idMovtoAlmacen=" + m.getIdMovtoAlmacen() + " AND idEmpaque=" + to.getIdProducto() + " AND lote='" + l.getLote() + "'";
                        st.executeUpdate(strSQL);
                    }
                }
                this.generaRechazoTraspaso(cn, m);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void grabarTraspasoEnvio(TOMovimiento to, ArrayList<TOMovimientoProducto> detalle) throws SQLException {
        String strSQL;
        double sumaLotes, costoPromedio;

        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement(); Statement st1 = cn.createStatement();) {
                ResultSet rs;
                to.setIdUsuario(this.idUsuario);

                strSQL = "UPDATE movimientosAlmacen "
                        + "SET fecha=GETDATE(), idUsuario=" + this.idUsuario + ", estatus=1 "
                        + "WHERE idMovtoAlmacen=" + to.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "INSERT INTO solicitudes (idMovto, idUsuario, fecha) "
                        + "SELECT idMovto, idUsuario, fecha FROM movimientos "
                        + "WHERE idMovto=" + to.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientos "
                        + "SET fecha=GETDATE(), idUsuario=" + this.idUsuario + ", estatus=1 "
                        + "WHERE idMovto=" + to.getIdMovto();
                st.executeUpdate(strSQL);

                for (TOMovimientoProducto d : detalle) {
                    sumaLotes = 0;
                    strSQL = "SELECT K.lote, K.cantidad, ISNULL(L.saldo, 0) AS saldo "
                            + "FROM movimientosDetalleAlmacen K "
                            + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=K.idMovtoAlmacen "
                            + "LEFT JOIN almacenesLotes L ON L.idAlmacen=M.idAlmacen AND L.idEmpaque=K.idEmpaque AND L.lote=K.lote "
                            + "WHERE K.idMovtoAlmacen=" + to.getIdMovtoAlmacen() + " AND K.idEmpaque=" + d.getIdProducto();
                    rs = st.executeQuery(strSQL);
                    while (rs.next()) {
                        if (rs.getDouble("saldo") < rs.getDouble("cantidad")) {
                            throw new SQLException("No hay saldo o No se encontro el lote(" + rs.getString("lote") + ") del producto(" + d.getIdProducto() + ") en el almacen");
                        }
                        strSQL = "UPDATE movimientosDetalleAlmacen "
                                + "SET existenciaAnterior=" + rs.getDouble("saldo") + ", fecha=GETDATE() "
                                + "WHERE idMovtoAlmacen=" + to.getIdMovtoAlmacen() + " AND idEmpaque=" + d.getIdProducto() + " AND lote='" + rs.getString("lote") + "'";
                        st1.executeUpdate(strSQL);

                        strSQL = "UPDATE almacenesLotes "
                                + "SET saldo=saldo-" + rs.getDouble("cantidad") + ", separados=separados-" + rs.getDouble("cantidad") + " "
                                + "WHERE idAlmacen=" + to.getIdAlmacen() + " AND idEmpaque=" + d.getIdProducto() + " AND lote='" + rs.getString("lote") + "'";
                        st1.executeUpdate(strSQL);

                        sumaLotes += rs.getDouble("cantidad");
                    }
                    if (sumaLotes != d.getCantFacturada()) {
                        throw new SQLException("Diferencia entre lotes y cantidad Facturada del producto(" + d.getIdProducto() + ")");
                    } else if (sumaLotes > 0) {
                        strSQL = "SELECT AE.existenciaOficina AS saldo, ISNULL(EE.existenciaOficina, 0) AS existencia, ISNULL(EE.costoUnitarioPromedio, 0) AS costo "
                                + "FROM almacenesEmpaques AE "
                                + "INNER JOIN almacenes A ON A.idAlmacen=AE.idAlmacen "
                                + "LEFT JOIN empresasEmpaques EE ON EE.idEmpresa=A.idEmpresa AND EE.idEmpaque=AE.idEmpaque "
                                + "WHERE AE.idAlmacen=" + to.getIdAlmacen() + " AND AE.idEmpaque=" + d.getIdProducto();
                        rs = st.executeQuery(strSQL);
                        if (rs.next()) {
                            if (rs.getDouble("existencia") < d.getCantFacturada()) {
                                throw new SQLException("No hay capas de costos suficientes o No se encontro el producto(" + d.getIdProducto() + ") en la empresa");
                            } else if (rs.getDouble("costo") < 0) {
                                throw new SQLException("Costo no valido (menor que cero)");
                            }
                        } else {
                            throw new SQLException("No se encontro producto(" + d.getIdProducto() + ") en el almacen");
                        }
                        d.setCostoPromedio(rs.getDouble("costo"));
                        d.setCosto(rs.getDouble("costo"));
                        d.setUnitario(rs.getDouble("costo"));
                        costoPromedio = d.getCostoPromedio();
                        if (d.getCantFacturada() == rs.getDouble("existencia")) {
                            costoPromedio = 0;    // Cuando ya no hay existencia, el costo promedio de la empresa se hace cero
                        }
                        strSQL = "UPDATE movimientosDetalle "
                                + "SET costoPromedio=" + d.getCostoPromedio() + ", costo=" + d.getCosto() + ", unitario=" + d.getUnitario() + ", cantFacturada=" + d.getCantFacturada() + ", existenciaAnterior=" + rs.getDouble("saldo") + ", fecha=GETDATE() "
                                + "WHERE idMovto=" + to.getIdMovto() + " AND idEmpaque=" + d.getIdProducto();
                        st.executeUpdate(strSQL);

                        strSQL = "UPDATE almacenesEmpaques "
                                + "SET existenciaOficina=existenciaOficina-" + d.getCantFacturada() + ", separados=separados-" + d.getCantFacturada() + " "
                                + "WHERE idAlmacen=" + to.getIdAlmacen() + " AND idEmpaque=" + d.getIdProducto();
                        st.executeUpdate(strSQL);

                        strSQL = "UPDATE empresasEmpaques SET existenciaOficina=existenciaOficina-" + d.getCantFacturada() + ", costoUnitarioPromedio=" + costoPromedio + " "
                                + "WHERE idEmpresa=" + to.getIdEmpresa() + " AND idEmpaque=" + d.getIdProducto();
                        st.executeUpdate(strSQL);
                    }
                }
                // ----------------------- SECCION: CREAR ENLACE ENVIO-RECEPCION ------------------

                int folioRecepcion = this.obtenerMovimientoFolio(cn, true, to.getIdAlmacen(), 9);

                int folioRecepcionAlmacen = this.obtenerMovimientoFolio(cn, false, to.getIdAlmacen(), 9);

                // ------------------------- SECCION: CREAR RECEPCION ---------------------

                int idCedisDestino = 0;
                strSQL = "SELECT idCedis FROM almacenes WHERE idAlmacen=" + to.getIdReferencia();
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    idCedisDestino = rs.getInt("idCedis");
                } else {
                    throw new SQLException("No se encontro almacen=" + to.getIdReferencia());
                }
                int idMovto = 0;
                strSQL = "INSERT INTO movimientos (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, idImpuestoZona, desctoComercial, desctoProntoPago, fecha, idUsuario, idMoneda, tipoCambio, estatus, idReferencia, referencia, propietario) "
                        + "VALUES(9, " + idCedisDestino + ", " + to.getIdEmpresa() + ", " + to.getIdReferencia() + ", " + folioRecepcion + ", 0, 0, 0, 0, GETDATE(), " + this.idUsuario + ", 1, 1, 0, " + to.getIdAlmacen() + ", " + to.getIdMovto() + ", 0)";
                st.executeUpdate(strSQL);
                rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
                if (rs.next()) {
                    idMovto = rs.getInt("idMovto");
                }
                int idMovtoAlmacen = 0;
                strSQL = "INSERT INTO movimientosAlmacen (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, fecha, idUsuario, estatus, idReferencia, referencia, propietario) "
                        + "VALUES (9, " + idCedisDestino + ", " + to.getIdEmpresa() + ", " + to.getIdReferencia() + ", " + folioRecepcionAlmacen + ", 0, GETDATE(), " + this.idUsuario + ", 0, " + to.getIdAlmacen() + ", " + to.getIdMovtoAlmacen() + ", 0)";
                st.executeUpdate(strSQL);
                rs = st.executeQuery("SELECT @@IDENTITY AS idMovtoAlmacen");
                if (rs.next()) {
                    idMovtoAlmacen = rs.getInt("idMovtoAlmacen");
                }
                strSQL = "INSERT INTO movimientosRelacionados (idMovto, idMovtoAlmacen) VALUES (" + idMovto + ", " + idMovtoAlmacen + ")";
                st.executeUpdate(strSQL);

                strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantOrdenada, cantFacturada, cantSinCargo, cantRecibida, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior)\n"
                        + "SELECT " + idMovto + ", idEmpaque, cantFacturada, cantFacturada, cantSinCargo, cantRecibida, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, GETDATE(), 0\n"
                        + "FROM movimientosDetalle WHERE idMovto=" + to.getIdMovto() + " AND cantFacturada > 0";
                st.executeUpdate(strSQL);

                strSQL = "INSERT INTO movimientosDetalleAlmacen (idMovtoAlmacen, idEmpaque, lote, cantidad, fecha, existenciaAnterior)\n"
                        + "SELECT " + idMovtoAlmacen + ", MD.idEmpaque, MD.lote, MD.cantidad, GETDATE(), 0 AS existenciaAnterior\n"
                        + "FROM movimientosDetalleAlmacen MD\n"
                        + "WHERE MD.idMovtoAlmacen=" + to.getIdMovtoAlmacen() + " AND MD.cantidad > 0";
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientos SET referencia=" + idMovto + " WHERE idMovto=" + to.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosAlmacen SET referencia=" + idMovtoAlmacen + " WHERE idMovtoAlmacen=" + to.getIdMovtoAlmacen();
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

    public ArrayList<TOMovimientoAlmacen> obtenerMovimientosAlmacen(int idAlmacen, int idTipo, int estatus) throws SQLException {
        String condicion = "=";
        if (estatus != 0) {
            condicion = "!=";
        }
        ArrayList<TOMovimientoAlmacen> solicitudes = new ArrayList<>();
        String strSQL = "SELECT M.* FROM movimientosAlmacen M "
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=" + idTipo + " AND M.estatus" + condicion + "0 "
                + "ORDER BY M.fecha DESC";
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                solicitudes.add(this.construirMovimientoAlmacen(rs));
            }
        } finally {
            cn.close();
        }
        return solicitudes;
    }

    public ArrayList<TOMovimiento> obtenerMovimientos(int idAlmacen, int idTipo, int estatus) throws SQLException {
        String condicion = "=";
        if (estatus != 0) {
            condicion = "!=";
        }
        ArrayList<TOMovimiento> solicitudes = new ArrayList<>();
        String strSQL = "SELECT M.*"
                + "     , ISNULL(MA.idMovtoAlmacen, 0) AS idMovtoAlmacen, MA.folio AS folioAlmacen, ISNULL(MA.fecha, GETDATE()) AS fechaAlmacen"
                + "     , ISNULL(MA.idUsuario, 0) AS idUsuarioAlmacen, ISNULL(MA.estatus, 0) AS statusAlmacen "
                + "FROM movimientos M "
                + "LEFT JOIN movimientosRelacionados MR ON MR.idMovto=M.idMovto "
                + "LEFT JOIN movimientosAlmacen MA ON MA.idMovtoAlmacen=MR.idMovtoAlmacen "
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=" + idTipo + " AND M.estatus" + condicion + "0 "
                + "ORDER BY M.fecha DESC";
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                solicitudes.add(this.construirMovimientoRelacionado(rs));
            }
        } finally {
            cn.close();
        }
        return solicitudes;
    }

    public void grabarTraspasoSolicitud(TOMovimiento to, ArrayList<MovimientoProducto> productos) throws SQLException {
        String strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantOrdenada, cantFacturada, cantSinCargo, cantRecibida, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior) "
                + "VALUES (?, ?, ?, 0, 0, 0, 0, 0, 0, 0, 0, 0, ?, GETDATE(), 0)";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (PreparedStatement ps = cn.prepareStatement(strSQL)) {
                to.setIdUsuario(this.idUsuario);
                
                this.agregaMovimientoRelacionado(cn, to);

                for (MovimientoProducto p : productos) {
                    ps.setInt(1, to.getIdMovto());
                    ps.setInt(2, p.getProducto().getIdProducto());
                    ps.setDouble(3, p.getCantOrdenada());
                    ps.setInt(4, p.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());
                    ps.executeUpdate();
                }
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw (e);
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    // ------------------------------- COMPRAS --------------------------------------
    public void grabarCompraAlmacen(TOMovimientoAlmacen m, ArrayList<MovimientoAlmacenProducto> productos) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                m.setFolio(this.obtenerMovimientoFolio(cn, false, m.getIdAlmacen(), 1));

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
                for (MovimientoAlmacenProducto p : productos) {
                    if (p.getCantidad() > 0) {
                        idProducto = p.getProducto().getIdProducto();

                        existenciaAnterior = 0;
                        strSQL = "SELECT saldo FROM almacenesLotes "
                                + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                        rs = st.executeQuery(strSQL);
                        if (rs.next()) {
                            existenciaAnterior = rs.getDouble("saldo");
                            strSQL = "UPDATE almacenesLotes "
                                    + "SET cantidad=cantidad+" + p.getCantidad() + ", saldo=saldo+" + p.getCantidad() + " "
                                    + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=" + idProducto + " AND lote='" + lote + "'";
                        } else {
                            strSQL = "INSERT INTO almacenesLotes (idAlmacen, idEmpaque, lote, fechaCaducidad, cantidad, saldo, separados, existenciaFisica) "
                                    + "VALUES (" + m.getIdAlmacen() + ", " + idProducto + ", '" + lote + "', DATEADD(DAY, 365, convert(date, GETDATE())), " + p.getCantidad() + ", " + p.getCantidad() + ", 0, 0)";
                        }
                        st.executeUpdate(strSQL);

                        strSQL = "INSERT INTO movimientosDetalleAlmacen (idMovtoAlmacen, idEmpaque, lote, cantidad, fecha, existenciaAnterior) "
                                + "VALUES (" + m.getIdMovto() + ", " + idProducto + ", '" + lote + "', " + p.getCantidad() + ", GETDATE(), " + existenciaAnterior + ")";
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
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw (e);
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void grabarCompraOficina(TOMovimiento m, ArrayList<MovimientoProducto> productos) throws SQLException {
        boolean nueva = false;
        int idEmpaque, idImpuestoGrupo;
        double existenciaAnterior;
        ArrayList<ImpuestosProducto> impuestos;

        String strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantFacturada, cantSinCargo, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, cantOrdenada, cantRecibida, idImpuestoGrupo, fecha, existenciaAnterior) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, getdate(), ?)";

        String strSQL1 = "UPDATE movimientosDetalle "
                + "SET costo=?, desctoProducto1=?, desctoProducto2=?, desctoConfidencial=?, unitario=?, cantFacturada=?, cantSinCargo=?, existenciaAnterior=? "
                + "WHERE idMovto=" + m.getIdMovto() + " AND idEmpaque=?";

        String strSQL2 = "UPDATE movimientosDetalleImpuestos "
                + "SET importe=? "
                + "WHERE idMovto=" + m.getIdMovto() + " AND idEmpaque=?";

//        String strSQL3="INSERT INTO kardexOficina (idAlmacen, idMovto, idTipoMovto, idEmpaque, fecha, existenciaAnterior, cantidad) " +
//                    "VALUES ("+m.getIdAlmacen()+", "+m.getIdMovto()+", 1, ?, GETDATE(), ?, ?)";
//        PreparedStatement ps3=cn.prepareStatement(strSQL3);

        String strSQL4 = "INSERT INTO almacenesEmpaques (idAlmacen, idEmpaque, existenciaOficina, existenciaMinima, existenciaMaxima) "
                + "VALUES (?, ?, ?, 0, 0)";

        String strSQL5 = "UPDATE almacenesEmpaques "
                + "SET existenciaOficina=existenciaOficina+? "
                + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=?";

        String strSQL6 = "INSERT INTO empresasEmpaques (idEmpresa, idEmpaque, costoUnitarioPromedio, existenciaOficina, idMovtoUltimaEntrada) "
                + "VALUES (" + m.getIdEmpresa() + ", ?, ?, ?, ?)";

        String strSQL7 = "UPDATE empresasEmpaques "
                + "SET existenciaOficina=existenciaOficina+?"
                + ", costoUnitarioPromedio=(existenciaOficina*costoUnitarioPromedio+?*?)/(existenciaOficina+?)"
                + ", idMovtoUltimaEntrada=? "
                + "WHERE idEmpresa=" + m.getIdEmpresa() + " AND idEmpaque=?";

        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement();
                    PreparedStatement ps = cn.prepareStatement(strSQL);
                    PreparedStatement ps1 = cn.prepareStatement(strSQL1);
                    PreparedStatement ps2 = cn.prepareStatement(strSQL2);
                    PreparedStatement ps4 = cn.prepareStatement(strSQL4);
                    PreparedStatement ps5 = cn.prepareStatement(strSQL5);
                    PreparedStatement ps6 = cn.prepareStatement(strSQL6);
                    PreparedStatement ps7 = cn.prepareStatement(strSQL7)) {
                ResultSet rs;
                st.executeUpdate("BEGIN TRANSACTION");

                if (m.getIdMovto() == 0) {
                    nueva = true;
                    m.setFolio(this.obtenerMovimientoFolio(cn, true, m.getIdAlmacen(), 1));

                    strSQL = "INSERT INTO movimientos (idTipo, idCedis, folio, idEmpresa, idAlmacen, idComprobante, idImpuestoZona, idMoneda, tipoCambio, desctoComercial, desctoProntoPago, idUsuario, fecha, estatus, idReferencia, referencia, propietario) "
                            + "VALUES (1, " + m.getIdCedis() + ", " + m.getFolio() + ", " + m.getIdEmpresa() + ", " + m.getIdAlmacen() + ", " + m.getIdComprobante() + ", " + m.getIdImpuestoZona() + ", " + m.getIdMoneda() + ", " + m.getTipoDeCambio() + ", " + m.getDesctoComercial() + ", " + m.getDesctoProntoPago() + ", " + this.idUsuario + ", getdate(), 1, " + m.getIdReferencia() + ", " + m.getReferencia() + ", 0)";
                    st.executeUpdate(strSQL);

                    rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
                    if (rs.next()) {
                        m.setIdMovto(rs.getInt("idMovto"));
                    }
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
                        rs = st.executeQuery("SELECT existenciaOficina "
                                + "FROM almacenesEmpaques "
                                + "WHERE idAlmacen=" + m.getIdAlmacen() + " AND idEmpaque=" + idEmpaque);
                        if (rs.next()) {
                            existenciaAnterior = rs.getDouble("existenciaOficina");

                            ps5.setDouble(1, p.getCantFacturada() + p.getCantSinCargo());
                            ps5.setInt(2, idEmpaque);
                            ps5.executeUpdate();

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
                            this.agregarImpuestosProducto(cn, m.getIdMovto(), idEmpaque, idImpuestoGrupo, m.getIdImpuestoZona());
                            this.calculaImpuestosProducto(cn, m.getIdMovto(), idEmpaque, p.getUnitario());
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
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw (e);
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private void actualizarExistenciaOficina(Connection cn, int idMovto) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
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
        }
    }

    private void validarExistenciaOficina(Connection cn, int idMovto) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
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
        }
    }

    public void cancelarCompraAlmacen(int idMovto, int idAlmacen, int idOrdenDeCompra) throws SQLException {
        String strSQL;
        int idMovtoTipo = 34;
        int idMovtoCancelacion;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
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

                int folio = this.obtenerMovimientoFolio(cn, false, idAlmacen, idMovtoTipo);
                strSQL = "INSERT INTO movimientosAlmacen (idTipo, idCedis, idEmpresa, idAlmacen, folio, idComprobante, fecha, idUsuario, idReferencia, referencia, propietario, estatus)\n"
                        + "SELECT " + idMovtoTipo + ", idCedis, idEmpresa, idAlmacen, " + folio + ", idComprobante, GETDATE(), " + this.idUsuario + ", idReferencia, referencia, 0, 1\n"
                        + "FROM movimientosAlmacen WHERE idMovtoAlmacen=" + idMovto;
                st.executeUpdate(strSQL);

                idMovtoCancelacion = 0;
                rs = st.executeQuery("SELECT @@IDENTITY AS idMovto");
                if (rs.next()) {
                    idMovtoCancelacion = rs.getInt("idMovto");
                }
                strSQL = "INSERT INTO movimientosDetalleAlmacen (idMovtoAlmacen, idEmpaque, lote, cantidad, fecha, existenciaAnterior)\n"
                        + "SELECT " + idMovtoCancelacion + ", MD.idEmpaque, MD.lote, MD.cantidad, GETDATE(), L.saldo\n"
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
                            + "INNER JOIN movimientosDetalleAlmacen MD ON MD.idMovtoAlmacen=" + idMovto + " AND MD.idEmpaque=OCD.idEmpaque\n"
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
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void cancelarCompra(int idMovto, int idAlmacen, int idOrdenDeCompra) throws SQLException {
        String strSQL;
        int idMovtoTipo = 34;
        int idMovtoCancelacion;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                this.validarExistenciaOficina(cn, idMovto);

                strSQL = "UPDATE movimientos SET estatus=3 WHERE idMovto=" + idMovto;
                st.executeUpdate(strSQL);

                int folio = this.obtenerMovimientoFolio(cn, true, idAlmacen, idMovtoTipo);
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

                this.actualizarExistenciaOficina(cn, idMovto);

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
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

//  ==========================  MOVIMIENTOS ALMACEN  ====================================
    public TOMovimientoAlmacenProducto construirDetalleAlmacen(ResultSet rs) throws SQLException {
        TOMovimientoAlmacenProducto to = new TOMovimientoAlmacenProducto();
        to.setIdMovtoAlmacen(rs.getInt("idMovtoAlmacen"));
        to.setIdProducto(rs.getInt("idEmpaque"));
        to.setCantOrdenada(rs.getDouble("cantOrdenada"));
        to.setCantRecibida(rs.getDouble("cantRecibida"));
        to.setCantidad(rs.getDouble("cantidad"));
        return to;
    }

    public ArrayList<TOMovimientoAlmacenProducto> obtenerDetalleAlmacen(int idMovtoAlmacen) throws SQLException {
        ArrayList<TOMovimientoAlmacenProducto> productos = new ArrayList<>();
        String strSQL = "SELECT *, 0 AS cantOrdenada, 0 AS cantRecibida FROM movimientosDetalleAlmacen WHERE idMovtoAlmacen=" + idMovtoAlmacen;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    productos.add(this.construirDetalleAlmacen(rs));
                }
            }
        }
        return productos;
    }

    public ArrayList<TOMovimientoAlmacenProducto> obtenerDetalleAlmacenPorEmpaque(int idMovtoAlmacen) throws SQLException {
        ArrayList<TOMovimientoAlmacenProducto> lista = new ArrayList<>();
        String strSQL = "SELECT idEmpaque, SUM(cantidad) AS cantidad\n"
                + "FROM movimientosDetalleAlmacen k\n"
                + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + "\n"
                + "GROUP BY idEmpaque";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                TOMovimientoAlmacenProducto to;
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    to = new TOMovimientoAlmacenProducto();
                    to.setIdMovtoAlmacen(idMovtoAlmacen);
                    to.setIdProducto(rs.getInt("idEmpaque"));
                    to.setCantidad(rs.getDouble("cantidad"));
                    lista.add(to);
                }
            }
        }
        return lista;
    }

    private TOMovimientoAlmacen construirMovimientoAlmacen(ResultSet rs) throws SQLException {
        TOMovimientoAlmacen to = new TOMovimientoAlmacen();
        to.setIdMovto(rs.getInt("idMovtoAlmacen"));
        to.setIdTipo(rs.getInt("idTipo"));
        to.setFolio(rs.getInt("folio"));
        to.setIdCedis(rs.getInt("idCedis"));
        to.setIdEmpresa(rs.getInt("idEmpresa"));
        to.setIdAlmacen(rs.getInt("idAlmacen"));
        to.setIdComprobante(rs.getInt("idComprobante"));
        to.setFecha(new java.util.Date(rs.getDate("fecha").getTime()));
        to.setIdUsuario(rs.getInt("idUsuario"));
        to.setEstatus(rs.getInt("estatus"));
        to.setIdReferencia(rs.getInt("idReferencia"));
        to.setReferencia(rs.getInt("referencia"));
        return to;
    }

    public ArrayList<TOMovimientoAlmacen> obtenerMovimientosAlmacen(int idComprobante) throws SQLException {
        ArrayList<TOMovimientoAlmacen> tos = new ArrayList<>();
        String strSQL = "SELECT * FROM movimientosAlmacen WHERE idTipo=1 AND idComprobante=" + idComprobante;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    tos.add(construirMovimientoAlmacen(rs));
                }
            }
        }
        return tos;
    }

    // ========================== MOVIMIENTOS OFICINA ====================================
    public TOMovimientoProducto construirDetalle(ResultSet rs) throws SQLException {
        TOMovimientoProducto to = new TOMovimientoProducto();
        to.setIdMovto(rs.getInt("idMovto"));
        to.setIdProducto(rs.getInt("idEmpaque"));
        to.setCantOrdenada(rs.getDouble("cantOrdenada"));
//        to.setCantOrdenadaSinCargo(rs.getDouble("cantOrdenadaSinCargo"));
        to.setCantRecibida(rs.getDouble("cantRecibida"));
//        to.setCantRecibidaSinCargo(rs.getDouble("cantRecibidaSinCargo"));
        to.setCantFacturada(rs.getDouble("cantFacturada"));
        to.setCantSinCargo(rs.getDouble("cantSinCargo"));
        to.setCostoPromedio(rs.getDouble("costoPromedio"));
        to.setCosto(rs.getDouble("costo"));
        to.setDesctoProducto1(rs.getDouble("desctoProducto1"));
        to.setDesctoProducto2(rs.getDouble("desctoProducto2"));
        to.setDesctoConfidencial(rs.getDouble("desctoConfidencial"));
        to.setUnitario(rs.getDouble("unitario"));
        return to;
    }

    private ArrayList<TOMovimientoProducto> obtenDetalle(Connection cn, int idMovto) throws SQLException {
        ArrayList<TOMovimientoProducto> productos = new ArrayList<>();
        String strSQL = "SELECT * FROM movimientosDetalle WHERE idMovto=" + idMovto;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                productos.add(this.construirDetalle(rs));
            }
        }
        return productos;
    }

    public ArrayList<TOMovimientoProducto> obtenerDetalle(int idMovto) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            return this.obtenDetalle(cn, idMovto);
        }
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
        to.setIdMovtoAlmacen(rs.getInt("idMovtoAlmacen"));
        to.setFolioAlmacen(rs.getInt("folioAlmacen"));
        return to;
    }

    public TOMovimiento obtenerMovimientoRelacionado(int idMovto) throws SQLException {
        TOMovimiento to = null;
        String strSQL = "SELECT M.*"
                + ", ISNULL(MA.idMovtoAlmacen, 0) AS idMovtoAlmacen, MA.folio AS folioAlmacen, ISNULL(MA.fecha, GETDATE()) AS fechaAlmacen"
                + ", ISNULL(MA.idUsuario, 0) AS idUsuarioAlmacen, ISNULL(MA.estatus, 0) AS statusAlmacen "
                + "FROM movimientos M "
                + "LEFT JOIN movimientosRelacionados MR ON MR.idMovto=M.idMovto "
                + "LEFT JOIN movimientosAlmacen MA ON MA.idMovtoAlmacen=MR.idMovtoAlmacen "
                + "WHERE M.idMovto=" + idMovto;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    to = construirMovimientoRelacionado(rs);
                }
            }
        }
        return to;
    }

    public ArrayList<TOMovimiento> obtenerMovimientosRelacionados(int idComprobante) throws SQLException {
        ArrayList<TOMovimiento> tos = new ArrayList<>();
        String strSQL = "SELECT M.*"
                + ", ISNULL(MA.idMovtoAlmacen, 0) AS idMovtoAlmacen, MA.folio AS folioAlmacen, ISNULL(MA.fecha, GETDATE()) AS fechaAlmacen"
                + ", ISNULL(MA.idUsuario, 0) AS idUsuarioAlmacen, ISNULL(MA.estatus, 0) AS statusAlmacen "
                + "FROM movimientos M "
                + "LEFT JOIN movimientosRelacionados MR ON MR.idMovto=M.idMovto "
                + "LEFT JOIN movimientosAlmacen MA ON MA.idMovtoAlmacen=MR.idMovtoAlmacen "
                + "WHERE M.idTipo=1 AND M.idComprobante=" + idComprobante;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    tos.add(construirMovimientoRelacionado(rs));
                }
            }
        }
        return tos;
    }

    private int obtenerMovimientoFolio(Connection cn, boolean oficina, int idAlmacen, int idTipo) throws SQLException {
        int folio;
        String tabla = "movimientosFoliosAlmacen";
        if (oficina) {
            tabla = "movimientosFolios";
        }
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT folio FROM " + tabla + " WHERE idAlmacen=" + idAlmacen + " AND idTipo=" + idTipo);
            if (rs.next()) {
                folio = rs.getInt("folio");
                st.executeUpdate("UPDATE " + tabla + " SET folio=folio+1 WHERE idAlmacen=" + idAlmacen + " AND idTipo=" + idTipo);
            } else {
                folio = 1;
                st.executeUpdate("INSERT INTO " + tabla + " (idAlmacen, idTipo, folio) VALUES (" + idAlmacen + ", " + idTipo + ", 2)");
            }
        }
        return folio;
    }

//  ================================  ORDEN DE COMPRA  ===================================
    public void cerrarOrdenDeCompra(boolean oficina, int idOrdenDeCompra) throws SQLException {
        String strSQL;
        if (oficina) {
            strSQL = "UPDATE ordenCompra SET estado=3 WHERE idOrdenCompra=" + idOrdenDeCompra;
        } else {
            strSQL = "UPDATE ordenCompra SET estadoAlmacen=3 WHERE idOrdenCompra=" + idOrdenDeCompra;
        }
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);
            }
        }
    }

    public ArrayList<TOMovimientoAlmacenProducto> obtenerOrdenDeCompraDetalleAlmacen(int idOrdenDeCompra) throws SQLException {
        ArrayList<TOMovimientoAlmacenProducto> productos = new ArrayList<>();
        String strSQL = "SELECT 0 AS idMovtoAlmacen, OCD.idEmpaque, OCD.cantOrdenada+OCD.cantOrdenadaSinCargo AS cantOrdenada, OCD.costoOrdenado\n"
                + "       , ISNULL(MD.cantRecibida,0) AS cantRecibida, 0 AS cantidad, '' AS lote\n"
                + "FROM (SELECT MD.idEmpaque, SUM(MD.cantidad) AS cantRecibida\n"
                + "		FROM movimientosDetalleAlmacen MD\n"
                + "		INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=MD.idMovtoAlmacen\n"
                + "		WHERE M.referencia=" + idOrdenDeCompra + "\n"
                + "		GROUP BY MD.idEmpaque) MD\n"
                + "RIGHT JOIN ordenCompraDetalle OCD ON OCD.idEmpaque=MD.idEmpaque\n"
                + "WHERE OCD.idOrdenCompra=" + idOrdenDeCompra;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    if (rs.getDouble("cantRecibida") < rs.getDouble("cantOrdenada")) {
                        productos.add(this.construirDetalleAlmacen(rs));
                    }
                }
            }
        }
        return productos;
    }

    public ArrayList<TOMovimientoProducto> obtenerOrdenDeCompraDetalle(int idOrdenDeCompra) throws SQLException {
        String strSQL;
        ArrayList<TOMovimientoProducto> productos = new ArrayList<>();
        strSQL = "SELECT 0 AS idMovto, OCD.idEmpaque, OCD.cantOrdenada, OCD.cantOrdenadaSinCargo, OCD.costoOrdenado AS costo\n"
                + "       , ISNULL(MD.cantRecibida,0) AS cantRecibida, ISNULL(MD.cantRecibidaSinCargo, 0) AS cantRecibidaSinCargo\n"
                + "       , 0 AS cantFacturada, 0 AS cantSinCargo\n"
                + "       , OCD.descuentoProducto AS desctoProducto1, OCD.descuentoProducto2 AS desctoProducto2\n"
                + "       , OCD.desctoConfidencial, 0 AS unitario, 0 AS costoPromedio\n"
                + "FROM (SELECT MD.idEmpaque, SUM(MD.cantFacturada) AS cantRecibida, SUM(MD.cantSinCargo) AS cantRecibidaSinCargo\n"
                + "		FROM movimientosDetalle MD\n"
                + "		INNER JOIN movimientos M ON M.idMovto=MD.idMovto\n"
                + "		WHERE M.referencia=" + idOrdenDeCompra + "\n"
                + "		GROUP BY MD.idEmpaque) MD\n"
                + "RIGHT JOIN ordenCompraDetalle OCD ON OCD.idEmpaque=MD.idEmpaque\n"
                + "WHERE OCD.idOrdenCompra=" + idOrdenDeCompra;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    if (rs.getDouble("cantRecibida") + rs.getDouble("cantRecibidaSinCargo") < rs.getDouble("cantOrdenada") + rs.getDouble("cantOrdenadaSinCargo")) {
                        productos.add(this.construirDetalle(rs));
                    }
                }
            }
        }
        return productos;
    }

    public double obtenerPrecioUltimaCompra(int idEmpresa, int idEmpaque) throws SQLException {
        double precioLista = 0;
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "SELECT idMovtoUltimaEntrada FROM empresasEmpaques "
                        + "WHERE idEmpresa=" + idEmpresa + " AND idEmpaque=" + idEmpaque;
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
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw (e);
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return precioLista;
    }

//  ===============================  IMPUESTOS  =========================================================
    public double obtenerImpuestosProducto(int idMovto, int idEmpaque, ArrayList<ImpuestosProducto> impuestos) throws SQLException {
        double importeImpuestos = 0;
        ImpuestosProducto impuesto;
        String strSQL = "select idImpuesto, impuesto, valor, aplicable, modo, acreditable, importe, acumulable\n"
                + "from movimientosDetalleImpuestos\n"
                + "where idMovto=" + idMovto + " and idEmpaque=" + idEmpaque + "\n"
                + "order by acumulable";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    impuesto = construirImpuestosProducto(rs);
                    importeImpuestos += impuesto.getImporte();
                    impuestos.add(impuesto);
                }
            }
        }
        return importeImpuestos;
    }

    private void calculaImpuestosProducto(Connection cn, int idMovto, int idEmpaque, double unitario) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
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
        }
    }

    private void agregarImpuestosProducto(Connection cn, int idMovto, int idEmpaque, int idImpuestoGrupo, int idZona) throws SQLException {
        String strSQL = "insert into movimientosDetalleImpuestos (idMovto, idEmpaque, idImpuesto, impuesto, valor, aplicable, modo, acreditable, importe, acumulable) "
                + "select " + idMovto + ", " + idEmpaque + ", id.idImpuesto, i.impuesto, id.valor, i.aplicable, i.modo, i.acreditable, 0.00 as importe, i.acumulable "
                + "from impuestosDetalle id "
                + "inner join impuestos i on i.idImpuesto=id.idImpuesto "
                + "where id.idGrupo=" + idImpuestoGrupo + " and id.idZona=" + idZona + " and GETDATE() between fechaInicial and fechaFinal";

        try (Statement st = cn.createStatement()) {
            if (st.executeUpdate(strSQL) == 0) {
                throw (new SQLException("No se generaron impuestos !!!"));
            }
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
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                impuestos.add(this.construirImpuestosProducto(rs));
            }
            if (impuestos.isEmpty()) {
                throw new SQLException("No se generaron impuestos !!!");
            }
        } finally {
            cn.close();
        }
        return impuestos;
    }
}
