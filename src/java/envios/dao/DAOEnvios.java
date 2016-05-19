package envios.dao;

import envios.Envios;
import envios.dominio.EnvioTraspasoPojo;
import envios.to.TOEnvio;
import envios.to.TOEnvioProducto;
import envios.to.TOEnvioTraspaso;
import impuestos.dominio.ImpuestosProducto;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import movimientos.Movimientos;
import movimientos.to.TOMovimientoOficina;
import pedidos.Pedidos;
import pedidos.to.TOPedido;
import pedidos.to.TOPedidoProducto;
import solicitudes.Solicitudes;
import solicitudes.to.TOSolicitud;
import solicitudes.to.TOSolicitudProducto;
import traspasos.Traspasos;
import traspasos.to.TOTraspaso;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOEnvios {

    private int idUsuario, idCedis, idZona;
    private DataSource ds;

    public DAOEnvios() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
        this.idUsuario = usuarioSesion.getUsuario().getId();
        this.idZona = usuarioSesion.getUsuario().getIdCedisZona();
        this.idCedis = usuarioSesion.getUsuario().getIdCedis();

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
    }

    public void grabarTraspaso(TOEnvio toEnvio, TOEnvioTraspaso toTraspaso) throws SQLException {
        String strSQL = "";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                toTraspaso.setSolicitudFolio(movimientos.Movimientos.obtenMovimientoFolioOficina(cn, toTraspaso.getIdReferencia(), 53));

                strSQL = "UPDATE solicitudes\n"
                        + "SET estatus=" + toTraspaso.getSolicitudEstatus() + ", folio=" + toTraspaso.getSolicitudFolio() + "\n"
                        + "     , fecha=GETDATE(), idUsuarioOrigen=" + this.idUsuario + "\n"
                        + "WHERE idSolicitud=" + toTraspaso.getReferencia();
                st.executeUpdate(strSQL);

                strSQL = "SELECT fecha FROM solicitudes WHERE idSolicitud=" + toTraspaso.getReferencia();
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    toTraspaso.setSolicitudFecha(new java.util.Date(rs.getTimestamp("fecha").getTime()));
                }
                strSQL = "DELETE DT FROM movimientosDetalle DT INNER JOIN movimientos T ON T.idMovto=DT.idMovto\n"
                        + "INNER JOIN solicitudesDetalle SD ON SD.idSolicitud=T.referencia AND SD.idEmpaque=DT.idEmpaque\n"
                        + "WHERE DT.idMovto=" + toTraspaso.getIdMovto() + " AND SD.cantSolicitada=0";
                st.executeUpdate(strSQL);

                strSQL = "SELECT S.idSolicitud\n"
                        + "FROM solicitudes S\n"
                        + "INNER JOIN enviosSolicitudes ES ON ES.idSolicitud=S.idSolicitud\n"
                        + "INNER JOIN envios E ON E.idEnvio=ES.idEnvio\n"
                        + "WHERE E.idEnvio=" + toTraspaso.getIdEnvio() + " AND S.estatus=0";
                rs = st.executeQuery(strSQL);
                if (!rs.next()) {
                    this.cierraEnvio(cn, toEnvio);
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

    private void cierraEnvio(Connection cn, TOEnvio toEnvio) throws SQLException {
        toEnvio.setEstatus(7);
        try (Statement st = cn.createStatement()) {
            toEnvio.setFolioEnvio(movimientos.Movimientos.obtenMovimientoFolioOficina(cn, 0, 1055));
            String strSQL = "UPDATE envios\n"
                    + "SET folio=" + toEnvio.getFolioEnvio() + ", estatus=" + toEnvio.getEstatus() + "\n"
                    + "WHERE idEnvio=" + toEnvio.getIdEnvio();
            st.executeUpdate(strSQL);
        }
    }

    public ArrayList<TOEnvioTraspaso> eliminarTraspaso(TOEnvio toEnvio, TOEnvioTraspaso toTraspaso) throws SQLException {
        ArrayList<TOEnvioTraspaso> traspasos = new ArrayList<>();
        String strSQL = "SELECT COUNT(*) AS traspasos FROM enviosSolicitudes ES INNER JOIN envios E ON E.idEnvio=ES.idEnvio WHERE E.idEnvio=" + toEnvio.getIdEnvio();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "DELETE FROM solicitudes WHERE idSolicitud=" + toTraspaso.getReferencia();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM solicitudesDetalle WHERE idSolicitud=" + toTraspaso.getReferencia();
                st.executeUpdate(strSQL);

                strSQL = "DELETE MA FROM movimientos M INNER JOIN movimientosAlmacen MA ON MA.idMovtoAlmacen=M.idMovtoAlmacen\n"
                        + "WHERE M.idMovto=" + toTraspaso.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientos WHERE idMovto=" + toTraspaso.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalle WHERE idMovto=" + toTraspaso.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalleImpuestos WHERE idMovto=" + toTraspaso.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM enviosSolicitudes WHERE idSolicitud=" + toTraspaso.getReferencia();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM enviosSolicitudesDetalle WHERE idSolicitud=" + toTraspaso.getReferencia();
                st.executeUpdate(strSQL);

                strSQL = "DELETE EPD FROM enviosPedidosDetalle EPD INNER JOIN enviosPedidos EP ON EP.idVenta=EPD.idVenta\n"
                        + "WHERE EP.idSolicitudEnvio=" + toTraspaso.getReferencia() + " AND EP.idEnvio=" + toEnvio.getIdEnvio();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM enviosPedidos\n"
                        + "WHERE idSolicitudEnvio=" + toTraspaso.getReferencia() + " AND idEnvio=" + toEnvio.getIdEnvio();
                st.executeUpdate(strSQL);

                traspasos = this.obtenTraspasos(cn, toEnvio.getIdEnvio());
                if (traspasos.isEmpty()) {
                    strSQL = "DELETE FROM envios WHERE idEnvio=" + toEnvio.getIdEnvio();
                    st.executeUpdate(strSQL);
                } else {
                    boolean cierraEnvio = true;
                    for (TOEnvioTraspaso to : traspasos) {
                        if (to.getSolicitudEstatus() == 0) {
                            cierraEnvio = false;
                            break;
                        }
                    }
                    if (cierraEnvio) {
                        this.cierraEnvio(cn, toEnvio);
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
        return traspasos;
    }

    public ArrayList<Double> grabarEnviadaSinCargo(TOEnvioTraspaso toTraspaso, int directoIdSolicitud, TOPedidoProducto toProd) throws SQLException {
        String strSQL;
        ArrayList<Double> pesos = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "UPDATE ESD\n"
                        + "SET fincados=CASE WHEN EP.directo=1 THEN ESD.fincados ELSE ESD.fincados-EPD.cantEnviarSinCargo/E.piezas END\n"
                        + "     , directos=CASE WHEN EP.directo=1 THEN ESD.directos-EPD.cantEnviarSinCargo/E.piezas ELSE ESD.directos END\n"
                        + "FROM  enviosPedidosDetalle EPD\n"
                        + "INNER JOIN enviosPedidos EP ON EP.idVenta=EPD.idVenta AND EP.idEnvio=EPD.idEnvio\n"
                        + "INNER JOIN enviosSolicitudesDetalle ESD ON ESD.idSolicitud=EP.idSolicitudEnvio AND ESD.idEmpaque=EPD.idEmpaque\n"
                        + "INNER JOIN empaques E ON E.idEmpaque=ESD.idEmpaque\n"
                        + "WHERE ESD.idSolicitud=" + toTraspaso.getReferencia() + " AND ESD.idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE SD\n"
                        + "SET cantSolicitada=SD.cantSolicitada-(EPD.cantEnviar+EPD.cantEnviarSinCargo)\n"
                        + "FROM enviosPedidosDetalle EPD INNER JOIN enviosPedidos EP ON EP.idVenta=EPD.idVenta AND EP.idEnvio=EPD.idEnvio\n"
                        + "INNER JOIN solicitudesDetalle SD ON SD.idSolicitud=EP.idSolicitud AND SD.idEmpaque=EPD.idEmpaque\n"
                        + "WHERE SD.idSolicitud=" + directoIdSolicitud + " AND SD.idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE enviosPedidosDetalle\n"
                        + "SET cantEnviarSinCargo=" + toProd.getCantEnviarSinCargo() + "\n"
                        + "WHERE idVenta=" + toProd.getIdMovto() + " AND idEnvio=" + toTraspaso.getIdEnvio() + " AND idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE ESD\n"
                        + "SET fincados=CASE WHEN EP.directo=1 THEN ESD.fincados ELSE ESD.fincados+EPD.cantEnviarSinCargo/E.piezas END\n"
                        + "     , directos=CASE WHEN EP.directo=1 THEN ESD.directos+EPD.cantEnviarSinCargo/E.piezas ELSE ESD.directos END\n"
                        + "FROM  enviosPedidosDetalle EPD\n"
                        + "INNER JOIN enviosPedidos EP ON EP.idVenta=EPD.idVenta AND EP.idEnvio=EPD.idEnvio\n"
                        + "INNER JOIN enviosSolicitudesDetalle ESD ON ESD.idSolicitud=EP.idSolicitudEnvio AND ESD.idEmpaque=EPD.idEmpaque\n"
                        + "INNER JOIN empaques E ON E.idEmpaque=ESD.idEmpaque\n"
                        + "WHERE ESD.idSolicitud=" + toTraspaso.getReferencia() + " AND ESD.idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE SD\n"
                        + "SET cantSolicitada=SD.cantSolicitada+(EPD.cantEnviar+EPD.cantEnviarSinCargo)\n"
                        + "FROM enviosPedidosDetalle EPD INNER JOIN enviosPedidos EP ON EP.idVenta=EPD.idVenta AND EP.idEnvio=EPD.idEnvio\n"
                        + "INNER JOIN solicitudesDetalle SD ON SD.idSolicitud=EP.idSolicitud AND SD.idEmpaque=EPD.idEmpaque\n"
                        + "WHERE SD.idSolicitud=" + directoIdSolicitud + " AND SD.idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);

                this.calculaSolicitada(cn, toTraspaso, toProd.getIdProducto());
                pesos = Envios.obtenerTraspasoEnvioPeso(cn, toTraspaso.getReferencia(), toTraspaso.getIdEnvio());

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.close();
            }
        }
        return pesos;
    }

    public ArrayList<Double> grabarEnviada(TOEnvioTraspaso toTraspaso, int directoIdSolicitud, TOPedidoProducto toProd) throws SQLException {
        String strSQL;
        ArrayList<Double> pesos = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "UPDATE ESD\n"
                        + "SET fincados=CASE WHEN EP.directo=1 THEN ESD.fincados ELSE ESD.fincados-EPD.cantEnviar/E.piezas END\n"
                        + "     , directos=CASE WHEN EP.directo=1 THEN ESD.directos-EPD.cantEnviar/E.piezas ELSE ESD.directos END\n"
                        + "FROM  enviosPedidosDetalle EPD\n"
                        + "INNER JOIN enviosPedidos EP ON EP.idVenta=EPD.idVenta AND EP.idEnvio=EPD.idEnvio\n"
                        + "INNER JOIN enviosSolicitudesDetalle ESD ON ESD.idSolicitud=EP.idSolicitudEnvio AND ESD.idEmpaque=EPD.idEmpaque\n"
                        + "INNER JOIN empaques E ON E.idEmpaque=ESD.idEmpaque\n"
                        + "WHERE ESD.idSolicitud=" + toTraspaso.getReferencia() + " AND ESD.idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE SD\n"
                        + "SET cantSolicitada=SD.cantSolicitada-(EPD.cantEnviar+EPD.cantEnviarSinCargo)\n"
                        + "FROM enviosPedidosDetalle EPD INNER JOIN enviosPedidos EP ON EP.idVenta=EPD.idVenta AND EP.idEnvio=EPD.idEnvio\n"
                        + "INNER JOIN solicitudesDetalle SD ON SD.idSolicitud=EP.idSolicitud AND SD.idEmpaque=EPD.idEmpaque\n"
                        + "WHERE SD.idSolicitud=" + directoIdSolicitud + " AND SD.idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE enviosPedidosDetalle\n"
                        + "SET cantEnviar=" + toProd.getCantEnviar() + "\n"
                        + "WHERE idVenta=" + toProd.getIdMovto() + " AND idEnvio=" + toTraspaso.getIdEnvio() + " AND idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE ESD\n"
                        + "SET fincados=CASE WHEN EP.directo=1 THEN ESD.fincados ELSE ESD.fincados+EPD.cantEnviar/E.piezas END\n"
                        + "     , directos=CASE WHEN EP.directo=1 THEN ESD.directos+EPD.cantEnviar/E.piezas ELSE ESD.directos END\n"
                        + "FROM  enviosPedidosDetalle EPD\n"
                        + "INNER JOIN enviosPedidos EP ON EP.idVenta=EPD.idVenta AND EP.idEnvio=EPD.idEnvio\n"
                        + "INNER JOIN enviosSolicitudesDetalle ESD ON ESD.idSolicitud=EP.idSolicitudEnvio AND ESD.idEmpaque=EPD.idEmpaque\n"
                        + "INNER JOIN empaques E ON E.idEmpaque=ESD.idEmpaque\n"
                        + "WHERE ESD.idSolicitud=" + toTraspaso.getReferencia() + " AND ESD.idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE SD\n"
                        + "SET cantSolicitada=SD.cantSolicitada+(EPD.cantEnviar+EPD.cantEnviarSinCargo)\n"
                        + "FROM enviosPedidosDetalle EPD INNER JOIN enviosPedidos EP ON EP.idVenta=EPD.idVenta AND EP.idEnvio=EPD.idEnvio\n"
                        + "INNER JOIN solicitudesDetalle SD ON SD.idSolicitud=EP.idSolicitud AND SD.idEmpaque=EPD.idEmpaque\n"
                        + "WHERE SD.idSolicitud=" + directoIdSolicitud + " AND SD.idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);

                this.calculaSolicitada(cn, toTraspaso, toProd.getIdProducto());
                pesos = Envios.obtenerTraspasoEnvioPeso(cn, toTraspaso.getReferencia(), toTraspaso.getIdEnvio());

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return pesos;
    }

    public void grabarOrden(int idEnvio, TOPedido toPed) throws SQLException {
        int ordenOld = 1;
        String strSQL = "SELECT MAX(orden) AS orden FROM enviosPedidos WHERE idEnvio=" + idEnvio + " AND idVenta!=" + toPed.getReferencia();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    ordenOld = rs.getInt("orden") + 1;
                }
                if (toPed.getOrden() > ordenOld) {
                    toPed.setOrden(ordenOld);
                }
                strSQL = "SELECT idVenta FROM enviosPedidos WHERE idEnvio=" + idEnvio + " AND orden=" + toPed.getOrden();
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    strSQL = "SELECT orden FROM enviosPedidos WHERE idEnvio=" + idEnvio + " AND idVenta=" + toPed.getReferencia();
                    rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        ordenOld = rs.getInt("orden");
                        if (ordenOld == 0) {
                            strSQL = "UPDATE enviosPedidos SET orden=orden+1 WHERE idEnvio=" + idEnvio + " AND orden >= " + toPed.getOrden();
                            st.executeUpdate(strSQL);
                        } else if (toPed.getOrden() == 0) {
                            strSQL = "UPDATE enviosPedidos SET orden=orden-1 WHERE idEnvio=" + idEnvio + " AND orden > " + ordenOld;
                            st.executeUpdate(strSQL);
                        } else if (toPed.getOrden() < ordenOld) {
                            strSQL = "UPDATE enviosPedidos SET orden=orden+1\n"
                                    + "WHERE idEnvio=" + idEnvio + " AND orden between " + toPed.getOrden() + " AND " + (ordenOld - 1);
                            st.executeUpdate(strSQL);
                        } else if (toPed.getOrden() > ordenOld) {
                            strSQL = "UPDATE enviosPedidos SET orden=orden-1\n"
                                    + "WHERE idEnvio=" + idEnvio + " AND orden between " + (ordenOld + 1) + " AND " + toPed.getOrden();
                            st.executeUpdate(strSQL);
                        }
                    }
                }
                strSQL = "UPDATE enviosPedidos SET orden=" + toPed.getOrden() + "\n"
                        + "WHERE idVenta=" + toPed.getReferencia() + " AND idEnvio=" + idEnvio;
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

    public ArrayList<Double> grabarDirecto(TOPedido toPedido, TOEnvioTraspaso toTraspaso) throws SQLException {
        String strSQL;
        ArrayList<Double> pesos = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                if (toPedido.getDirecto() != 0) {
                    // De fincado a directo
                    if (toPedido.getIdSolicitud() == 0) {
                        // Si no tiene solicitud el pedido, le crea una
                        toPedido.setIdSolicitud(Solicitudes.agregarSolicitudDirecto(cn, toPedido.getIdAlmacen(), toTraspaso.getIdAlmacen(), this.idUsuario));
                    }
                    // Actualiza el fincado con el id generado o ya existente
                    strSQL = "UPDATE enviosPedidos\n"
                            + "SET directo=" + toPedido.getDirecto() + ", idSolicitud=" + toPedido.getIdSolicitud() + "\n"
                            + "WHERE idVenta=" + toPedido.getReferencia() + " AND idEnvio=" + toPedido.getIdEnvio();
                    st.executeUpdate(strSQL);

                    // Agrega los productos del pedido fincado a la solicitud del pedido directo
                    strSQL = "INSERT INTO solicitudesDetalle (idSolicitud, idEmpaque, cantSolicitada)\n"
                            + "SELECT " + toPedido.getIdSolicitud() + ", idEmpaque, cantEnviar+cantEnviarSinCargo\n"
                            + "FROM enviosPedidosDetalle\n"
                            + "WHERE idVenta=" + toPedido.getReferencia() + " AND idEnvio=" + toPedido.getIdEnvio();
                    st.executeUpdate(strSQL);

                    // Resta la cantidad de fincados y la suma a directos de la solicitud del traspaso envio
                    strSQL = "UPDATE ESD\n"
                            + "SET fincados=ESD.fincados-(EPD.cantEnviar+EPD.cantEnviarSinCargo)/E.piezas\n"
                            + "     , directos=ESD.directos+(EPD.cantEnviar+EPD.cantEnviarSinCargo)/E.piezas\n"
                            + "FROM (SELECT * FROM enviosPedidosDetalle\n"
                            + "         WHERE idVenta=" + toPedido.getReferencia() + " AND idEnvio=" + toPedido.getIdEnvio() + ") EPD\n"
                            + "INNER JOIN enviosSolicitudesDetalle ESD ON ESD.idEmpaque=EPD.idEmpaque\n"
                            + "INNER JOIN empaques E ON E.idEmpaque=ESD.idEmpaque\n"
                            + "WHERE ESD.idEnvio=" + toPedido.getIdEnvio() + " AND ESD.idSolicitud=" + toTraspaso.getReferencia();
                    st.executeUpdate(strSQL);

                } else { // De directo a fincado
                    // Borra el detalle de la solicitud del pedido directo
                    strSQL = "DELETE FROM solicitudesDetalle WHERE idSolicitud=" + toPedido.getIdSolicitud();
                    st.executeUpdate(strSQL);

                    // Desmarca el pedido de directo sin eliminar la solicitud
                    strSQL = "UPDATE enviosPedidos\n"
                            + "SET directo=" + toPedido.getDirecto() + ", orden=0\n"
                            + "WHERE idVenta=" + toPedido.getReferencia() + " AND idEnvio=" + toPedido.getIdEnvio();
                    st.executeUpdate(strSQL);

                    toPedido.setOrden(0);

                    // Resta del envio traspaso la cantidad directa
                    strSQL = "UPDATE ESD\n"
                            + "SET directos=ESD.directos-(EPD.cantEnviar+EPD.cantEnviarSinCargo)/E.piezas\n"
                            + "FROM (SELECT * FROM enviosPedidosDetalle WHERE idVenta=" + toPedido.getReferencia() + " AND idEnvio=" + toPedido.getIdEnvio() + ") EPD\n"
                            + "INNER JOIN enviosSolicitudesDetalle ESD ON ESD.idEmpaque=EPD.idEmpaque\n"
                            + "INNER JOIN empaques E ON E.idEmpaque=ESD.idEmpaque\n"
                            + "WHERE ESD.idEnvio=" + toPedido.getIdEnvio() + " AND ESD.idSolicitud=" + toTraspaso.getReferencia();
                    st.executeUpdate(strSQL);

                    // Actualiza las cantidades del fincado con lo no surtido del pedido original
                    strSQL = "UPDATE EPD\n"
                            + "SET cantEnviar=VD.cantOrdenada-VD.cantSurtida\n"
                            + "     , cantEnviarSinCargo=VD.cantOrdenadaSinCargo-VD.cantSurtidaSinCargo\n"
                            + "FROM enviosPedidosDetalle EPD\n"
                            + "INNER JOIN enviosPedidos EP ON EP.idVenta=EPD.idVenta AND EP.idEnvio=EPD.idEnvio\n"
                            + "INNER JOIN ventasDetalle VD ON VD.idVenta=EP.idVenta AND VD.idEmpaque=EPD.idEmpaque\n"
                            + "WHERE EPD.idVenta=" + toPedido.getReferencia() + " AND EPD.idEnvio=" + toTraspaso.getIdEnvio() + " AND EP.idSolicitudEnvio=" + toTraspaso.getReferencia();
                    st.executeUpdate(strSQL);

                    // Actualiza los fincados del envio traspaso con las cantidades del pedido original
                    strSQL = "UPDATE ESD\n"
                            + "SET fincados=ESD.fincados+(EPD.cantEnviar+EPD.cantEnviarSinCargo)/E.piezas\n"
                            + "FROM (SELECT * FROM enviosPedidosDetalle WHERE idVenta=" + toPedido.getReferencia() + " AND idEnvio=" + toPedido.getIdEnvio() + ") EPD\n"
                            + "INNER JOIN enviosSolicitudesDetalle ESD ON ESD.idEmpaque=EPD.idEmpaque\n"
                            + "INNER JOIN empaques E ON E.idEmpaque=ESD.idEmpaque\n"
                            + "WHERE ESD.idEnvio=" + toPedido.getIdEnvio() + " AND ESD.idSolicitud=" + toTraspaso.getReferencia();
                    st.executeUpdate(strSQL);
                }
                this.calculaSolicitada(cn, toTraspaso, 0);
                pesos = Envios.obtenerTraspasoEnvioPeso(cn, toTraspaso.getReferencia(), toTraspaso.getIdEnvio());

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return pesos;
    }

    public ArrayList<Double> agregarFincado(TOPedido toPedido, TOEnvioTraspaso toTraspaso) throws SQLException {
        ArrayList<Double> pesos = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                this.agregaFincado(cn, toPedido, toTraspaso);

                this.calculaSolicitada(cn, toTraspaso, 0);
                pesos = Envios.obtenerTraspasoEnvioPeso(cn, toTraspaso.getReferencia(), toTraspaso.getIdEnvio());

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
//        return detalle;
        return pesos;
    }

    public void liberarFincado(TOPedido toPedido) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                Pedidos.liberarPedido(cn, toPedido, this.idUsuario);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public ArrayList<Double> eliminarFincado(TOPedido toPedido, TOEnvioTraspaso toTraspaso) throws SQLException {
        String strSQL;
        ArrayList<Double> pesos = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                if (toPedido.getIdSolicitud() != 0) {
                    strSQL = "DELETE FROM solicitudes WHERE idSolicitud=" + toPedido.getIdSolicitud();
                    st.executeUpdate(strSQL);

                    toPedido.setIdSolicitud(0);
                }
                strSQL = "UPDATE ESD\n"
                        + "SET fincados=ESD.fincados-(EPD.cantEnviar+EPD.cantEnviarSinCargo)/E.piezas\n"
                        + "FROM (SELECT * FROM enviosPedidosDetalle WHERE idVenta=" + toPedido.getReferencia() + " AND idEnvio=" + toPedido.getIdEnvio() + ") EPD\n"
                        + "INNER JOIN enviosSolicitudesDetalle ESD ON ESD.idEmpaque=EPD.idEmpaque\n"
                        + "INNER JOIN empaques E ON E.idEmpaque=ESD.idEmpaque\n"
                        + "WHERE ESD.idEnvio=" + toPedido.getIdEnvio() + " AND ESD.idSolicitud=" + toTraspaso.getReferencia();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE SD\n"
                        + "SET cantSolicitada=SD.cantSolicitada-EPD.cantEnviar-EPD.cantEnviarSinCargo\n"
                        + "FROM (SELECT * FROM enviosPedidosDetalle\n"
                        + "         WHERE idVenta=" + toPedido.getReferencia() + " AND idEnvio=" + toPedido.getIdEnvio() + ") EPD\n"
                        + "INNER JOIN solicitudesDetalle SD ON SD.idEmpaque=EPD.idEmpaque\n"
                        + "WHERE SD.idSolicitud=" + toTraspaso.getReferencia();
                st.executeUpdate(strSQL);

                this.calculaSolicitada(cn, toTraspaso, 0);
                pesos = Envios.obtenerTraspasoEnvioPeso(cn, toTraspaso.getReferencia(), toTraspaso.getIdEnvio());

                strSQL = "DELETE FROM enviosPedidosDetalle WHERE idVenta=" + toPedido.getReferencia() + " AND idEnvio=" + toPedido.getIdEnvio();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM enviosPedidos WHERE idVenta=" + toPedido.getReferencia() + " AND idEnvio=" + toPedido.getIdEnvio();
                st.executeUpdate(strSQL);

                toPedido.setIdEnvio(0);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return pesos;
    }

    private void agregaFincado(Connection cn, TOPedido toPedido, TOEnvioTraspaso toTraspaso) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            strSQL = "INSERT INTO enviosPedidos (idVenta, idSolicitudEnvio, idEnvio, directo, idSolicitud, orden)\n"
                    + "VALUES (" + toPedido.getIdMovto() + ", " + toTraspaso.getReferencia() + ", " + toTraspaso.getIdEnvio() + ", " + toPedido.getDirecto() + ", " + toPedido.getIdSolicitud() + ", 0)";
            st.executeUpdate(strSQL);

            strSQL = "INSERT INTO enviosPedidosDetalle (idVenta, idEnvio, idEmpaque, cantEnviar, cantEnviarSinCargo, agregado)\n"
                    + "SELECT " + toPedido.getIdMovto() + ", " + toPedido.getIdEnvio() + ", D.idEmpaque"
                    + "         , D.cantOrdenada-D.cantSurtida, D.cantOrdenadaSinCargo-D.cantSurtidaSinCargo, 1\n"
                    + "FROM pedidosDetalle D\n"
                    + "WHERE D.idPedido=" + toPedido.getReferencia() + "\n"
                    + "         AND D.cantOrdenada+D.cantOrdenadaSinCargo > D.cantSurtida+D.cantSurtidaSinCargo";
            st.executeUpdate(strSQL);

            strSQL = "INSERT INTO solicitudesDetalle (idSolicitud, idEmpaque, cantSolicitada)\n"
                    + "SELECT " + toTraspaso.getReferencia() + " AS idSolicitud, PD.idEmpaque, 0 AS cantSolicitada\n"
                    + "FROM pedidosDetalle PD\n"
                    + "LEFT JOIN (SELECT * FROM solicitudesDetalle\n"
                    + "             WHERE idSolicitud=" + toTraspaso.getReferencia() + ") SD ON SD.idEmpaque=PD.idEmpaque\n"
                    + "WHERE PD.idPedido=" + toPedido.getReferencia() + "\n"
                    + "         AND PD.cantOrdenada+PD.cantOrdenadaSinCargo > PD.cantSurtida+PD.cantSurtidaSinCargo\n"
                    + "         AND SD.idEmpaque IS NULL";
            st.executeUpdate(strSQL);

            strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantFacturada, cantSinCargo, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior, ctoPromAnterior)\n"
                    + "SELECT " + toTraspaso.getIdMovto() + " AS idMovto, D.idEmpaque, 0, 0, 0, 0, 0, 0, 0, 0, D.idImpuestoGrupo, '', 0, 0\n"
                    + "FROM movimientosDetalle D INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "INNER JOIN pedidosDetalle PD ON PD.idPedido=M.referencia AND PD.idEmpaque=D.idEmpaque\n"
                    + "LEFT JOIN (SELECT idEmpaque FROM movimientosDetalle WHERE idMovto=" + toTraspaso.getIdMovto() + ") S ON S.idEmpaque=D.idEmpaque\n"
                    + "WHERE D.idMovto=" + toPedido.getIdMovto() + "\n"
                    + "		AND PD.cantOrdenada+PD.cantOrdenadaSinCargo > PD.cantSurtida+PD.cantSurtidaSinCargo\n"
                    + "		AND S.idEmpaque IS NULL";
            st.executeUpdate(strSQL);

            strSQL = "INSERT INTO enviosSolicitudesDetalle (idSolicitud, idEnvio, idEmpaque, estadistica, sugerido, diasInventario, banCajas, idUsuario, fincados, directos)\n"
                    + "SELECT " + toTraspaso.getReferencia() + " AS idSolicitud, " + toPedido.getIdEnvio() + " AS idEnvio, EPD.idEmpaque, 0 AS estadistica, 0 AS sugerido, 0 AS diasInventario, 1 AS banCajas, " + this.idUsuario + ", 0 AS fincados, 0 AS directos\n"
                    + "FROM enviosPedidosDetalle EPD\n"
                    + "LEFT JOIN (SELECT * FROM enviosSolicitudesDetalle\n"
                    + "             WHERE idSolicitud=" + toTraspaso.getReferencia() + " AND idEnvio=" + toPedido.getIdEnvio() + ") ESD ON EPD.idEmpaque=ESD.idEmpaque\n"
                    + "WHERE EPD.idVenta=" + toPedido.getIdMovto() + " AND EPD.idEnvio=" + toPedido.getIdEnvio() + "\n"
                    + "         AND ESD.idEmpaque IS NULL";
            st.executeUpdate(strSQL);

            strSQL = "UPDATE SD\n"
                    + "SET cantSolicitada=SD.cantSolicitada+EPD.cantEnviar+EPD.cantEnviarSinCargo\n"
                    + "FROM (SELECT * FROM enviosPedidosDetalle\n"
                    + "         WHERE idVenta=" + toPedido.getIdMovto() + " AND idEnvio=" + toPedido.getIdEnvio() + ") EPD\n"
                    + "INNER JOIN solicitudesDetalle SD ON SD.idEmpaque=EPD.idEmpaque\n"
                    + "WHERE SD.idSolicitud=" + toPedido.getIdSolicitud();
            st.executeUpdate(strSQL);

            strSQL = "UPDATE ESD\n"
                    + "SET fincados=ESD.fincados+(EPD.cantEnviar+EPD.cantEnviarSinCargo)/E.piezas\n"
                    + "FROM (SELECT * FROM enviosPedidosDetalle WHERE idVenta=" + toPedido.getReferencia() + " AND idEnvio=" + toPedido.getIdEnvio() + ") EPD\n"
                    + "INNER JOIN enviosSolicitudesDetalle ESD ON ESD.idEmpaque=EPD.idEmpaque\n"
                    + "INNER JOIN empaques E ON E.idEmpaque=ESD.idEmpaque\n"
                    + "WHERE ESD.idEnvio=" + toPedido.getIdEnvio() + " AND ESD.idSolicitud=" + toTraspaso.getReferencia();
            st.executeUpdate(strSQL);
        }
    }

    public double obtenerImpuestosProducto(int idMovto, int idEmpaque, ArrayList<ImpuestosProducto> impuestos) throws SQLException {
        double importeImpuestos = 0;
        try (Connection cn = this.ds.getConnection()) {
            importeImpuestos = movimientos.Movimientos.obtenImpuestosProducto(cn, idMovto, idEmpaque, impuestos);
        }
        return importeImpuestos;
    }

    private ArrayList<TOPedidoProducto> obtenDetalleFincado(Connection cn, TOPedido toPed) throws SQLException {
        ArrayList<TOPedidoProducto> detalle = new ArrayList<>();
        String strSQL = Pedidos.sqlObtenProducto() + "\n"
                + "WHERE M.idMovto=" + toPed.getIdMovto();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                detalle.add(Pedidos.construirProducto(rs));
            }
        }
        return detalle;
    }

    public ArrayList<TOPedidoProducto> obtenerDetalleFincado(TOPedido toPed) throws SQLException {
        ArrayList<TOPedidoProducto> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            detalle = this.obtenDetalleFincado(cn, toPed);
            movimientos.Movimientos.bloquearMovimientoOficina(cn, toPed, this.idUsuario);
        }
        return detalle;
    }

    private TOPedido construirFincado(ResultSet rs) throws SQLException {
        TOPedido toPedido = new TOPedido();
//        toPedido.setIdEnvio(rs.getInt("idEnvio"));
//        toPedido.setOrden(rs.getInt("orden"));
//        toPedido.setEnvioEstatus(rs.getInt("envioEstatus"));
        Pedidos.construyePedido(toPedido, rs);
        return toPedido;
    }

    private ArrayList<TOPedido> obtenFincados(Connection cn, TOEnvio toEnvio, TOEnvioTraspaso toTraspaso, boolean agregar) throws SQLException {
        ArrayList<TOPedido> pedidos = new ArrayList<>();
        String strSQL = "SELECT " + Pedidos.sqlPedidos() + "\n"
                + "WHERE M.idAlmacen=" + toTraspaso.getIdReferencia() + " AND M.idTipo=28 AND (ISNULL(EP.idEnvio, 0)=" + toEnvio.getIdEnvio() + " OR (" + toEnvio.getEstatus() + "=0 AND P.estatus IN (1,3,5) AND M.estatus=0))\n"
                + "ORDER BY M.fecha";
        try (Statement st = cn.createStatement()) {
            TOPedido toPed;
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                toPed = this.construirFincado(rs);
                if (toPed.getIdEnvio() == 0 && agregar) {
                    toPed.setIdEnvio(toEnvio.getIdEnvio());
                    this.agregaFincado(cn, toPed, toTraspaso);
                }
                pedidos.add(toPed);
            }
        }
        return pedidos;
    }

    public ArrayList<TOPedido> obtenerFincados(TOEnvio toEnvio, TOEnvioTraspaso toTraspaso) throws SQLException {
        ArrayList<TOPedido> pedidos = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                pedidos = this.obtenFincados(cn, toEnvio, toTraspaso, false);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return pedidos;
    }

    public void agregarProducto(TOEnvioTraspaso toTraspaso, TOEnvioProducto toProd) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                toProd.setExistencia(0);
                strSQL = "SELECT existencia-separados AS existencia FROM almacenesEmpaques\n"
                        + "WHERE idAlmacen=" + toTraspaso.getIdAlmacen() + " AND idEmpaque=" + toProd.getIdProducto();
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    toProd.setExistencia(rs.getDouble("existencia"));
                }
                strSQL = "INSERT INTO enviosSolicitudesDetalle (idSolicitud, idEnvio, idEmpaque, estadistica, sugerido, diasInventario, banCajas, idUsuario, fincados, directos, existencia)\n"
                        + "VALUES (" + toProd.getIdSolicitud() + ", " + toProd.getIdEnvio() + ", " + toProd.getIdProducto() + ", 0, 0, 0, 1, " + this.idUsuario + ", 0, 0, " + toProd.getExistencia() + ")";
                st.executeUpdate(strSQL);

                strSQL = "INSERT INTO solicitudesDetalle (idSolicitud, idEmpaque, cantSolicitada)\n"
                        + "VALUES (" + toProd.getIdSolicitud() + ", " + toProd.getIdProducto() + ", 0)";
                st.executeUpdate(strSQL);

                Movimientos.agregarProductoOficina(cn, toProd, toTraspaso.getIdImpuestoZona());
//                movimientos.Movimientos.actualizaProductoPrecio(cn, toTraspaso, toProd, null);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private double calculaPeso(Connection cn, int idAlmacenDestino, int idEnvio, int idSolicitud) throws SQLException {
        double peso = 0;
        String strSQL = "SELECT SUM((P.cantSolicitada+P.fincado+P.directo)*E.peso) AS peso\n"
                + "FROM (SELECT ISNULL(ESD.idEmpaque, P.idEmpaque) AS idEmpaque, ISNULL(ESD.estadistica, 0) AS estadistica\n"
                + "		, ISNULL(ESD.banCajas, 1)  AS banCajas, ISNULL(ESD.diasInventario, 0) AS diasInventario\n"
                + "		, ISNULL(SD.cantSolicitada, 0) AS cantSolicitada\n"
                + "		, ISNULL(P.fincado, 0) AS fincado, ISNULL(P.directo, 0) AS directo\n"
                + "	FROM movimientos M\n"
                + "	INNER JOIN solicitudesDetalle SD ON SD.idSolicitud=M.referencia\n"
                + "	INNER JOIN enviosSolicitudes ES ON ES.idSolicitud=SD.idSolicitud\n"
                + "	INNER JOIN enviosSolicitudesDetalle ESD ON ESD.idSolicitud=SD.idSolicitud AND ESD.idEmpaque=SD.idEmpaque\n"
                + "	FULL OUTER JOIN (SELECT PD.idEmpaque\n"
                + "				, SUM(CASE WHEN P.directo=1 THEN EPD.cantEnviada ELSE 0 END) AS directo\n"
                + "				, SUM(CASE WHEN P.directo=0 THEN EPD.cantEnviada ELSE 0 END) AS fincado\n"
                + "			FROM movimientos M\n"
                + "			INNER JOIN pedidosDetalle PD ON PD.idPedido=M.referencia\n"
                + "			INNER JOIN pedidos P ON P.idPedido=PD.idPedido\n"
                + "			INNER JOIN enviosPedidos EP ON EP.idPedido=P.idPedido\n"
                + "			INNER JOIN enviosPedidosDetalle EPD ON EPD.idPedido=P.idPedido AND EPD.idEmpaque=PD.idEmpaque\n"
                + "			WHERE EP.idEnvio=" + idEnvio + " AND M.idAlmacen=" + idAlmacenDestino + "\n"
                + "			GROUP BY PD.idEmpaque) P ON P.idEmpaque=ESD.idEmpaque\n"
                + "	WHERE SD.idSolicitud=" + idSolicitud + ") P\n"
                + "INNER JOIN empaques E ON P.idEmpaque=E.idEmpaque";
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                peso = rs.getDouble("peso");
            }
        }
        return peso;
    }

    public ArrayList<Double> obtenerEnvioPeso(int idEnvio) throws SQLException {
        ArrayList<Double> pesos = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            pesos = Envios.obtenerEnvioPeso(cn, idEnvio);
        }
        return pesos;
    }

    public ArrayList<TOEnvioTraspaso> calcularPesoGeneral(int idEnvio, int diasInventario, double pesoMaximo) throws SQLException {
        int direccion = 0;
        ArrayList<Double> pesos;
        ArrayList<TOEnvioTraspaso> traspasos;
        boolean sePaso = false, banUltimo = false;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                traspasos = this.obtenTraspasos(cn, idEnvio);
                do {
                    for (TOEnvioTraspaso toTraspaso : traspasos) {
                        if (toTraspaso.getSolicitudEstatus() == 0) {
                            toTraspaso.setDiasInventario(diasInventario);
                            this.calculaDiasInventarioGeneral(cn, toTraspaso);
                        }
                    }
                    pesos = Envios.obtenerEnvioPeso(cn, idEnvio);
                    if (pesos.get(0) + pesos.get(1) == pesoMaximo) {
                        sePaso = true;
                        direccion = 0;
                        banUltimo = false;
                    } else if (pesos.get(0) + pesos.get(1) < pesoMaximo) {
                        if (direccion == 0) {
                            direccion = 1;
                        } else if (direccion == -1) {
                            sePaso = true;
                            direccion = 0;
                            banUltimo = false;
                        }
                    } else {
                        if (direccion == 0) {
                            direccion = -1;
                        } else if (direccion == 1) {
                            sePaso = true;
                            direccion = -1;
                            banUltimo = true;
                        }
                    }
                    diasInventario += direccion;
                } while (!sePaso || banUltimo);
                traspasos = this.obtenTraspasos(cn, idEnvio);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return traspasos;
    }

    private void calculaDiasInventarioGeneral(Connection cn, TOEnvioTraspaso toTraspaso) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            strSQL = "UPDATE D\n"
                    + "SET diasInventario=" + toTraspaso.getDiasInventario() + "\n"
                    + "FROM enviosSolicitudesDetalle D\n"
                    + "INNER JOIN enviosSolicitudes S ON S.idSolicitud=D.idSolicitud\n"
                    + "WHERE D.idSolicitud=" + toTraspaso.getReferencia() + " AND banCajas=0 AND D.diasInventario=S.diasInventario";
            st.executeUpdate(strSQL);

            strSQL = "UPDATE enviosSolicitudes\n"
                    + "SET diasInventario=" + toTraspaso.getDiasInventario() + "\n"
                    + "WHERE idSolicitud=" + toTraspaso.getReferencia();
            st.executeUpdate(strSQL);

            this.calculaSolicitada(cn, toTraspaso, 0);
        }
    }

    public void calcularDiasInventarioGeneral(TOEnvioTraspaso toTraspaso) throws SQLException {
//        ArrayList<TOEnvioProducto> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                this.calculaDiasInventarioGeneral(cn, toTraspaso);
//                detalle = this.obtenDetalle(cn, toTraspaso, 0);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
//        return detalle;
    }

    public void grabarDiasInventario(TOEnvioTraspaso toTraspaso, TOEnvioProducto toProd) throws SQLException {
        TOEnvioProducto to = null;
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "UPDATE enviosSolicitudesDetalle\n"
                        + "SET diasInventario=" + toProd.getDiasInventario() + ", banCajas=0, idUsuario=" + this.idUsuario + "\n"
                        + "WHERE idSolicitud=" + toProd.getIdSolicitud() + " AND idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);

                this.calculaSolicitada(cn, toTraspaso, toProd.getIdProducto());
                to = this.obtenProducto(cn, toTraspaso, toProd.getIdProducto());
                toProd.setCantSolicitada(to.getCantSolicitada());
                toProd.setExistencia(to.getExistencia());

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private TOEnvioProducto obtenProducto(Connection cn, TOEnvioTraspaso toTraspaso, int idProducto) throws SQLException {
        TOEnvioProducto toProd = null;
        String strSQL = this.sqlDetalle(toTraspaso, idProducto);
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                toProd = this.construirProducto(rs);
            }
        }
        return toProd;
    }

//    private void actualizaDiasInventario(Connection cn, TOEnvioProducto toProd) throws SQLException {
//        if (toProd.getEstadistica() != 0) {
//            String strSQL = "UPDATE ESD\n"
//                    + "SET diasInventario=CAST(SD.cantSolicitada/ESD.estadistica AS Integer)\n"
//                    + "FROM enviosSolicitudesDetalle ESD\n"
//                    + "INNER JOIN solicitudesDetalle SD ON SD.idSolicitud=ESD.idSolicitud AND SD.idEmpaque=ESD.idEmpaque\n"
//                    + "WHERE ESD.idSolicitud=" + toProd.getIdSolicitud() + " AND ESD.idEmpaque=" + toProd.getIdProducto();
//            try (Statement st = cn.createStatement()) {
//                st.executeUpdate(strSQL);
//            }
//        }
//    }
//
    private void calculaSolicitada(Connection cn, TOEnvioTraspaso toTraspaso, int idProducto) throws SQLException {
        String strSQL;
//        String condicion = "";
//        String condicion1 = "";
        String condicion2 = "";
//        String condicion3 = "";
        if (idProducto != 0) {
//            condicion1 = " AND EPD.idEmpaque=" + idProducto;
            condicion2 = " AND ESD.idEmpaque=" + idProducto;
//            condicion3 = " AND idEmpaque=" + idProducto;
        } else {
//            condicion = " AND ESD.banCajas=0 AND ESD.diasInventario=ES.diasInventario";
        }
//        strSQL = "UPDATE S\n"
//                + "SET cantSolicitada=CEILING(CASE WHEN E.banCajas=0\n"
//                + "				THEN CASE WHEN E.estadistica*E.diasInventario + E.fincado*E.piezas < E.existencia THEN 0\n"
//                + "						ELSE E.estadistica*E.diasInventario + E.fincado*E.piezas - E.existencia END\n"
//                + "                     WHEN E.fincado*E.piezas < E.cantSolicitada + E.existencia THEN E.cantSolicitada\n"
//                + "			ELSE E.fincado*E.piezas - E.existencia END/E.piezas)*E.piezas\n"
//                + "FROM (SELECT P.*, E.piezas, ISNULL(A.existencia, 0) AS existencia\n"
//                + "	FROM (SELECT ISNULL(ESD.idEmpaque, P.idEmpaque) AS idEmpaque, ISNULL(ESD.estadistica, 0) AS estadistica\n"
//                + "		, ISNULL(ESD.banCajas, 1)  AS banCajas, ISNULL(ESD.diasInventario, 0) AS diasInventario\n"
//                + "		, ISNULL(SD.cantSolicitada, 0) AS cantSolicitada\n"
//                + "		, ISNULL(P.fincado, 0) AS fincado, ISNULL(P.directo, 0) AS directo\n"
//                + "	FROM movimientos M\n"
//                + "	INNER JOIN solicitudesDetalle SD ON SD.idSolicitud=M.referencia\n"
//                + "	INNER JOIN enviosSolicitudes ES ON ES.idSolicitud=SD.idSolicitud\n"
//                + "	INNER JOIN enviosSolicitudesDetalle ESD ON ESD.idSolicitud=SD.idSolicitud AND ESD.idEmpaque=SD.idEmpaque\n"
//                + "	FULL OUTER JOIN (SELECT EPD.idEmpaque\n"
//                + "                             , SUM(CASE WHEN EP.directo=1 THEN EPD.cantEnviar+EPD.cantEnviarSinCargo ELSE 0 END) AS directo\n"
//                + "                             , SUM(CASE WHEN EP.directo=0 THEN EPD.cantEnviar+EPD.cantEnviarSinCargo ELSE 0 END) AS fincado\n"
//                + "			FROM movimientos M\n"
//                + "			INNER JOIN enviosPedidosDetalle EPD ON EPD.idVenta=M.referencia\n"
//                + "			INNER JOIN enviosPedidos EP ON EP.idVenta=EPD.idVenta\n"
//                + "                     INNER JOIN envios E ON E.idEnvio=EP.idEnvio\n"
//                + "			WHERE E.idEnvio=" + toTraspaso.getIdEnvio() + " AND M.idAlmacen=" + toTraspaso.getIdReferencia() + " AND M.idTipo=28 AND M.estatus=0" + condicion1 + "\n"
//                + "			GROUP BY EPD.idEmpaque) P ON P.idEmpaque=ESD.idEmpaque\n"
//                + "	WHERE SD.idSolicitud=" + toTraspaso.getReferencia() + condicion + condicion2 + ") P\n"
//                + "INNER JOIN empaques E ON P.idEmpaque=E.idEmpaque\n"
//                + "LEFT JOIN (SELECT idEmpaque, SUM(existencia-separados) AS existencia\n"
//                + "		FROM almacenesLotes\n"
//                + "		WHERE idAlmacen=" + toTraspaso.getIdReferencia() + condicion3 + "\n"
//                + "		GROUP BY idEmpaque) A ON A.idEmpaque=E.idEmpaque) E\n"
//                + "INNER JOIN solicitudesDetalle S ON S.idEmpaque=E.idEmpaque\n"
//                + "WHERE S.idSolicitud=" + toTraspaso.getReferencia();
//        
        try (Statement st = cn.createStatement()) {
            strSQL = "UPDATE ESD\n"
                    + "SET existencia=ISNULL(A.existencia, 0)\n"
                    + "FROM enviosSolicitudesDetalle ESD\n"
                    + "LEFT JOIN (SELECT idEmpaque, SUM(existencia-separados) AS existencia\n"
                    + "             FROM almacenesLotes\n"
                    + "             WHERE idAlmacen=" + toTraspaso.getIdReferencia() + "\n"
                    + "             GROUP BY idEmpaque) A ON A.idEmpaque=ESD.idEmpaque\n"
                    + "WHERE ESD.idSolicitud=" + toTraspaso.getReferencia() + condicion2;
            st.executeUpdate(strSQL);

            strSQL = "UPDATE SD\n"
                    + "SET cantSolicitada=CEILING(CASE WHEN D.banCajas=0\n"
                    + "				THEN CASE WHEN D.estadistica*D.diasInventario + D.fincados*D.piezas < D.existencia THEN 0\n"
                    + "					ELSE D.estadistica*D.diasInventario + D.fincados*D.piezas - D.existencia END\n"
                    + "                             WHEN D.fincados*D.piezas < D.cantSolicitada + D.existencia THEN D.cantSolicitada\n"
                    + "                             ELSE D.fincados*D.piezas - D.existencia END/D.piezas)*D.piezas\n"
                    + "FROM (SELECT ESD.*, SD.cantSolicitada, E.piezas\n"
                    + "     FROM enviosSolicitudesDetalle ESD\n"
                    + "     INNER JOIN solicitudesDetalle SD ON SD.idSolicitud=ESD.idSolicitud AND SD.idEmpaque=ESD.idEmpaque\n"
                    + "     INNER JOIN empaques E ON E.idEmpaque=ESD.idEmpaque\n"
                    + "     WHERE ESD.idSolicitud=" + toTraspaso.getReferencia() + condicion2 + ") D\n"
                    + "INNER JOIN solicitudesDetalle SD ON SD.idSolicitud=D.idSolicitud AND SD.idEmpaque=D.idEmpaque";
            st.executeUpdate(strSQL);
        }
    }

    public void grabarSolicitada(TOEnvioTraspaso toTraspaso, TOEnvioProducto toProd) throws SQLException {
        String strSQL;
        TOEnvioProducto to;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "UPDATE enviosSolicitudesDetalle\n"
                        + "SET banCajas=" + toProd.getBanCajas() + ", idUsuario=" + this.idUsuario + "\n"
                        + "WHERE idSolicitud=" + toProd.getIdSolicitud() + " AND idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE solicitudesDetalle\n"
                        + "SET cantSolicitada=" + toProd.getCantSolicitada() + "\n"
                        + "WHERE idSolicitud=" + toProd.getIdSolicitud() + " AND idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);

                this.calculaSolicitada(cn, toTraspaso, toProd.getIdProducto());

//                if (toProd.getEstadistica() != 0) {
                strSQL = "UPDATE ESD\n"
                        + "SET diasInventario=CASE WHEN ESD.estadistica=0 THEN 0\n"
                        + "                         WHEN ISNULL(ESD.fincados,0)*E.piezas <= SD.cantSolicitada+ISNULL(A.existencia, 0)\n"
                        + "                             THEN CAST((SD.cantSolicitada+ISNULL(A.existencia, 0)-ISNULL(ESD.fincados,0)*E.piezas)/ESD.estadistica AS Integer)\n"
                        + "                         ELSE -1 END\n"
                        + "FROM enviosSolicitudesDetalle ESD\n"
                        + "INNER JOIN solicitudesDetalle SD ON SD.idSolicitud=ESD.idSolicitud AND SD.idEmpaque=ESD.idEmpaque\n"
                        + "INNER JOIN empaques E ON E.idEmpaque=ESD.idEmpaque\n"
                        + "LEFT JOIN (SELECT idEmpaque, SUM(existencia-separados) AS existencia\n"
                        + "             FROM almacenesLotes\n"
                        + "             WHERE idAlmacen=" + toTraspaso.getIdReferencia() + " AND idEmpaque=" + toProd.getIdProducto() + "\n"
                        + "             GROUP BY idEmpaque) A ON A.idEmpaque=ESD.idEmpaque\n"
                        + "WHERE ESD.idSolicitud=" + toProd.getIdSolicitud() + " AND ESD.idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);
//                }
                to = this.obtenProducto(cn, toTraspaso, toProd.getIdProducto());

                cn.commit();
                toProd.setCantSolicitada(to.getCantSolicitada());
                toProd.setDiasInventario(to.getDiasInventario());
                toProd.setExistencia(to.getExistencia());
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void grabarSugerido(TOEnvioProducto toProd) throws SQLException {
        String strSQL = "UPDATE enviosSolicitudesDetalle\n"
                + "SET sugerido=" + toProd.getSugerido() + "\n"
                + "WHERE idEnvio=" + toProd.getIdEnvio() + " AND idSolicitud=" + toProd.getIdSolicitud() + " AND idEmpaque=" + toProd.getIdProducto();
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            st.executeUpdate(strSQL);
        } finally {
            cn.close();
        }
    }

    public void liberarTraspaso(TOTraspaso toTraspaso) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                Traspasos.liberarTraspaso(cn, toTraspaso, this.idUsuario);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private TOEnvioProducto construirProducto(ResultSet rs) throws SQLException {
        TOEnvioProducto toProd = new TOEnvioProducto();
        toProd.setIdEnvio(rs.getInt("idEnvio"));
        toProd.setIdSolicitud(rs.getInt("idSolicitud"));
        toProd.setEstadistica(rs.getDouble("estadistica"));
        toProd.setSugerido(rs.getDouble("sugerido"));
        toProd.setDiasInventario(rs.getInt("diasInventario"));
        toProd.setBanCajas(rs.getInt("banCajas"));
        toProd.setFincada(rs.getDouble("fincada"));
        toProd.setDirecta(rs.getDouble("directa"));
        toProd.setExistencia(rs.getDouble("existencia"));
        Traspasos.construir(toProd, rs);
        return toProd;
    }

    private String sqlDetalle(TOEnvioTraspaso toTraspaso, int idProducto) {
        String condicion = "";
        if (idProducto != 0) {
            condicion = " AND EPD.idEmpaque=" + idProducto;
        }
        String strSQL = "SELECT ET.*, E.piezas, ISNULL(A.existencia, 0) AS existencia\n"
                + "FROM (SELECT ISNULL(T.idEnvio, 0) AS idEnvio, ISNULL(P.fincado, 0) AS fincada, ISNULL(P.directo, 0) AS directa\n"
                + "		, ISNULL(T.banCajas, 0) AS banCajas, ISNULL(T.estadistica, 0) AS estadistica\n"
                + "             , ISNULL(T.sugerido, 0) AS sugerido, ISNULL(T.diasInventario, 0) AS diasInventario\n"
                + "		, ISNULL(T.cantSolicitada, 0) AS cantSolicitada, ISNULL(T.cantTraspasada, 0) AS cantTraspasada\n"
                + "		, ISNULL(T.idSolicitud, 0) AS idSolicitud, ISNULL(T.idMovto, 0) AS idMovto\n"
                + "		, ISNULL(T.idEmpaque, P.idEmpaque) AS idEmpaque, ISNULL(T.cantFacturada, 0) AS cantFacturada\n"
                + "		, ISNULL(T.cantSinCargo, 0) AS cantSinCargo, ISNULL(T.costoPromedio, 0) AS costoPromedio\n"
                + "		, ISNULL(T.costo, 0) AS costo, ISNULL(T.desctoConfidencial, 0) AS desctoConfidencial\n"
                + "		, ISNULL(T.desctoProducto1, 0) AS desctoProducto1, ISNULL(T.desctoProducto2, 0) AS desctoProducto2\n"
                + "		, ISNULL(T.unitario, 0) AS unitario, ISNULL(T.idImpuestoGrupo, 0) AS idImpuestoGrupo\n"
                + "		, ISNULL(T.fecha, '1900-01-01') AS fecha, ISNULL(T.existenciaAnterior, 0) AS existenciaAnterior\n"
                + "		, ISNULL(T.ctoPromAnterior, 0) AS ctoPromAnterior\n"
                + "	FROM (SELECT ES.idEnvio, ES.estadistica, ES.banCajas, ES.sugerido, ES.diasInventario, TD.*\n"
                + "		FROM (" + Traspasos.sqlTraspasoDetalle(toTraspaso, idProducto) + ") TD\n"
                + "		INNER JOIN enviosSolicitudesDetalle ES ON ES.idSolicitud=TD.idSolicitud AND ES.idEmpaque=TD.idEmpaque) T\n"
                + "	FULL OUTER JOIN (SELECT EPD.idEmpaque AS idEmpaque\n"
                + "                             , SUM(CASE WHEN EP.directo=1 THEN EPD.cantEnviada ELSE 0 END) AS directo\n"
                + "                             , SUM(CASE WHEN EP.directo=0 THEN EPD.cantEnviada ELSE 0 END) AS fincado\n"
                + "                         FROM movimientos M\n"
                + "                         INNER JOIN enviosPedidosDetalle EPD ON EPD.idVenta=M.referencia\n"
                + "                         INNER JOIN enviosPedidos EP ON EP.idVenta=EPD.idVenta\n"
                + "                         INNER JOIN envios E ON E.idEnvio=EP.idEnvio\n"
                + "                         WHERE E.idEnvio=" + toTraspaso.getIdEnvio() + " AND M.idAlmacen=" + toTraspaso.getIdReferencia() + " AND M.idTipo=28 AND M.estatus=0" + condicion + "\n"
                + "                         GROUP BY EPD.idEmpaque) P ON P.idEmpaque=T.idEmpaque) ET\n"
                + "INNER JOIN empaques E ON E.idEmpaque=ET.idEmpaque\n"
                + "LEFT JOIN (SELECT ED.idEmpaque, SUM(ED.existencia-ED.separados) AS existencia\n"
                + "             FROM almacenesLotes ED\n"
                + "             WHERE ED.idAlmacen=" + toTraspaso.getIdReferencia() + "\n"
                + "             GROUP BY idEmpaque) A ON A.idEmpaque=E.idEmpaque";

        strSQL = "SELECT ESD.idEnvio, ESD.estadistica, ESD.sugerido, ESD.diasInventario, ESD.banCajas\n"
                + "         , ESD.fincados AS fincada, ESD.directos AS directa, ESD.existencia, TD.*\n"
                + "FROM (" + Traspasos.sqlTraspasoDetalle(toTraspaso, idProducto) + ") TD\n"
                + "INNER JOIN enviosSolicitudesDetalle ESD ON ESD.idSolicitud=TD.idSolicitud AND ESD.idEmpaque=TD.idEmpaque\n"
                + "ORDER BY RIGHT(TD.cod_pro, 4), LEFT(TD.cod_pro, 3)";
        return strSQL;
    }

    private ArrayList<TOEnvioProducto> obtenDetalle(Connection cn, TOEnvioTraspaso toTraspaso, int idProducto) throws SQLException {
        String strSQL = this.sqlDetalle(toTraspaso, idProducto);
        ArrayList<TOEnvioProducto> detalle = new ArrayList<>();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                detalle.add(this.construirProducto(rs));
            }
        }
        return detalle;
    }

    public TOEnvioProducto obtenerDetalle(TOEnvioTraspaso toTraspaso, int idProducto) throws SQLException {
        TOEnvioProducto toProd = null;
        try (Connection cn = this.ds.getConnection()) {
            ArrayList<TOEnvioProducto> prods = this.obtenDetalle(cn, toTraspaso, idProducto);
            if (!prods.isEmpty()) {
                toProd = prods.get(0);
            }
        }
        return toProd;
    }

    public ArrayList<Double> obtenerEnvioTraspasoPeso(int idSolicitud, int idEnvio) throws SQLException {
        ArrayList<Double> pesos = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            pesos = Envios.obtenerTraspasoEnvioPeso(cn, idSolicitud, idEnvio);
        }
        return pesos;
    }

    public ArrayList<TOEnvioProducto> obtenerDetalle(TOEnvioTraspaso toTraspaso, ArrayList<Double> pesos) throws SQLException {
        ArrayList<Double> p = new ArrayList<>();
        ArrayList<TOEnvioProducto> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                detalle = this.obtenDetalle(cn, toTraspaso, 0);
                p = Envios.obtenerTraspasoEnvioPeso(cn, toTraspaso.getReferencia(), toTraspaso.getIdEnvio());
                pesos.set(0, p.get(0));
                pesos.set(1, p.get(1));
                movimientos.Movimientos.bloquearMovimientoOficina(cn, toTraspaso, this.idUsuario);
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

    public EnvioTraspasoPojo obtenerTraspasoAlmacen(int idSolicitud) throws SQLException {
//        String strSQL = "SELECT ES.idEnvio, ES.idSolicitud, CONCAT(E.nombreComercial, ' - ', A.almacen, ' - ', C.cedis) AS almacen\n"
//                + "FROM solicitudes S\n"
//                + "INNER JOIN almacenes A ON A.idAlmacen=S.idAlmacenOrigen\n"
//                + "INNER JOIN empresasGrupo E ON E.idEmpresa=A.idEmpresa\n"
//                + "INNER JOIN cedis C ON C.idCedis=A.idCedis\n"
//                + "INNER JOIN enviosSolicitudes ES ON ES.idSolicitud=S.idSolicitud\n"
//                + "WHERE S.idSolicitud=" + idSolicitud;
        String strSQL = "SELECT S.idSolicitud, CONCAT(E.nombreComercial, ' - ', A.almacen, ' - ', C.cedis) AS almacen\n"
                + "FROM solicitudes S\n"
                + "INNER JOIN almacenes A ON A.idAlmacen=S.idAlmacenOrigen\n"
                + "INNER JOIN empresasGrupo E ON E.idEmpresa=A.idEmpresa\n"
                + "INNER JOIN cedis C ON C.idCedis=A.idCedis\n"
                + "WHERE S.idSolicitud=" + idSolicitud;
        EnvioTraspasoPojo envioTraspaso = new EnvioTraspasoPojo();
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
//                envioTraspaso.setIdEnvio(rs.getInt("idEnvio"));
                envioTraspaso.setIdSolicitud(rs.getInt("idSolicitud"));
                envioTraspaso.setAlmacen(rs.getString("almacen"));
            }
        } finally {
            cn.close();
        }
        return envioTraspaso;
    }

    private TOEnvioTraspaso construirTraspaso(ResultSet rs) throws SQLException {
        TOEnvioTraspaso toTraspaso = new TOEnvioTraspaso();
//        toTraspaso.setIdEnvio(rs.getInt("idEnvio"));
        toTraspaso.setDiasInventario(rs.getInt("diasInventario"));
        toTraspaso.setFechaProduccion(new java.util.Date(rs.getDate("fechaProduccion").getTime()));
//        toTraspaso.setDirecto(rs.getInt("directo"));
        Traspasos.construir(rs, toTraspaso);
        return toTraspaso;
    }

    private ArrayList<TOEnvioTraspaso> obtenTraspasos(Connection cn, int idEnvio) throws SQLException {
        ArrayList<TOEnvioTraspaso> traspasos = new ArrayList<>();
        String strSQL = this.sqlTraspaso() + "\n"
                + "WHERE M.idTipo=35 AND E.idEnvio=" + idEnvio;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                traspasos.add(this.construirTraspaso(rs));
            }
        }
        return traspasos;
    }

    public ArrayList<TOEnvioTraspaso> obtenerTraspasos(int idEnvio) throws SQLException {
        ArrayList<TOEnvioTraspaso> traspasos = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            traspasos = this.obtenTraspasos(cn, idEnvio);
        }
        return traspasos;
    }

    private String sqlTraspaso() {
        return "SELECT ES.diasInventario, ES.fechaProduccion, ES.idEnvio, E.folio AS envioFolio, 0 AS pedidoFolio\n"
                + "     , " + Traspasos.sqlTraspasoBase() + "\n"
                + "INNER JOIN enviosSolicitudes ES ON ES.idSolicitud=S.idSolicitud INNER JOIN envios E ON E.idEnvio=ES.idEnvio";
    }

    private TOEnvioTraspaso obtenTraspaso(Connection cn, int idSolicitud) throws SQLException {
        TOEnvioTraspaso toTraspaso = null;
        String strSQL = this.sqlTraspaso() + "\n"
                + "WHERE M.idTipo=35 AND S.idSolicitud=" + idSolicitud;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                toTraspaso = this.construirTraspaso(rs);
            }
        }
        return toTraspaso;
    }

    private String sqlCrearEnvio(int idEnvio, int idSolicitud, int idAlmacen, String hoy, String anioAnterior) {
        String strSQL = "INSERT INTO enviosSolicitudesDetalle (idEnvio, idSolicitud, idEmpaque, estadistica, sugerido, diasInventario, banCajas, idUsuario, fincados, directos, existencia)\n"
                + "SELECT " + idEnvio + ", " + idSolicitud + ", EE.idEmpaque, CEILING(EE.estadistica), 0, 0, 0, " + this.idUsuario + ", 0, 0, ISNULL(A.existencia, 0) AS existencia\n"
                + "FROM (SELECT P.cod_pro, P.idEmpaque\n"
                + "			, CASE WHEN P.ten <= 0 THEN 0\n"
                + "					WHEN P.est_p <= 0 THEN P.ten\n"
                + "					WHEN P.p < 0 THEN P.ten*0.10 + P.ma*0.55 + P.est_f2*0.35\n"
                + "					WHEN P.p > 0 THEN P.ten*0.20 + P.ma*0.60 + P.est_f2*0.20\n"
                + "					ELSE 0 END AS estadistica\n"
                + "			, P.piezas, P.peso, P.p1, P.p2, P.p, P.ten, P.est_p, P.ma, P.est_f2\n"
                + "	FROM (SELECT E.cod_pro, E.idEmpaque, E.piezas, E.peso\n"
                + "				, ISNULL(p1.p1, 0) AS p1, ISNULL(p2.p2, 0) AS p2\n"
                + "				, CASE WHEN ISNULL(p2.p2, 0) = 0 THEN 0\n"
                + "						WHEN ISNULL(p1.p1, 0) = ISNULL(p2.p2, 0) THEN -1\n"
                + "						ELSE (ISNULL(p1.p1, 0)-ISNULL(p2.p2, 0))/ISNULL(p2.p2, 0) END AS p\n"
                + "				, ISNULL(ten.ten, 0) AS ten, ISNULL(est_p.est_p, 0) AS est_p\n"
                + "				, ISNULL(ma.ma, 0) AS ma, ISNULL(est_f2.est_f2, 0) AS est_f2\n"
                + "		FROM empaques E\n"
                + "		LEFT JOIN (SELECT idEmpaque, SUM(cantidad)/30 AS p1 FROM estadisticaVentas\n"
                + "					WHERE idAlmacen=" + idAlmacen + " AND fecha BETWEEN DATEADD(DAY,-31, '" + hoy + "') AND DATEADD(DAY, -1, '" + hoy + "')\n"
                + "					GROUP BY idEmpaque) p1 ON p1.idEmpaque=E.idEmpaque\n"
                + "		LEFT JOIN (SELECT idEmpaque, SUM(cantidad)/30 AS p2 FROM estadisticaVentas\n"
                + "					WHERE idAlmacen=" + idAlmacen + " AND fecha BETWEEN DATEADD(DAY,-62, '" + hoy + "') AND DATEADD(DAY,-32, '" + hoy + "')\n"
                + "					GROUP BY idEmpaque) p2 ON p2.idEmpaque=E.idEmpaque\n"
                + "		LEFT JOIN (SELECT idEmpaque, SUM(cantidad)/90 AS ten FROM estadisticaVentas\n"
                + "					WHERE idAlmacen=" + idAlmacen + " AND fecha BETWEEN DATEADD(DAY, -91, '" + hoy + "') AND DATEADD(DAY,-1, '" + hoy + "')\n"
                + "					GROUP BY idEmpaque) ten ON ten.idEmpaque=E.idEmpaque\n"
                + "		LEFT JOIN (SELECT idEmpaque, SUM(cantidad)/90 AS est_p FROM estadisticaVentas\n"
                + "					WHERE idAlmacen=" + idAlmacen + " AND fecha BETWEEN  DATEADD(DAY,-91, '" + anioAnterior + "') AND DATEADD(DAY,-1, '" + anioAnterior + "')\n"
                + "					GROUP BY idEmpaque) est_p ON est_p.idEmpaque=E.idEmpaque\n"
                + "		LEFT JOIN (SELECT idEmpaque, SUM(cantidad)/30 AS ma FROM estadisticaVentas\n"
                + "					WHERE idAlmacen=" + idAlmacen + " AND fecha BETWEEN DATEADD(DAY, 1, '" + anioAnterior + "') AND DATEADD(DAY,30, '" + anioAnterior + "')\n"
                + "					GROUP BY idEmpaque) ma ON ma.idEmpaque=E.idEmpaque\n"
                + "		LEFT JOIN (SELECT idEmpaque, SUM(cantidad)/60 AS est_f2 FROM estadisticaVentas\n"
                + "					WHERE idAlmacen=" + idAlmacen + " AND fecha BETWEEN DATEADD(DAY,31, '" + anioAnterior + "') AND DATEADD(DAY,91, '" + anioAnterior + "')\n"
                + "					GROUP BY idEmpaque) est_f2 ON est_f2.idEmpaque=E.idEmpaque) P) EE\n"
                + "LEFT JOIN (SELECT idEmpaque, SUM(existencia-separados) AS existencia\n"
                + "             FROM almacenesLotes WHERE idAlmacen=" + idAlmacen + " GROUP BY idEmpaque) A ON A.idEmpaque=EE.idEmpaque\n"
                + "WHERE EE.estadistica > 0";
        return strSQL;
    }

    public void crear(int idCedis, TOEnvio toEnvio) throws SQLException {
        String strSQL;
        int idCedisPlanta;
//        toEnvio = new TOEnvio();
//        ArrayList<TOEnvioTraspaso> traspasos = new ArrayList<>();
        Format formatoSQL = new SimpleDateFormat("yyyy-MM-dd");
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement(); Statement st1 = cn.createStatement()) {
                toEnvio.setIdUsuario(this.idUsuario);
//                toEnvio.setPropietario(this.idUsuario);
                toEnvio.setEstatus(0);

                if (idCedis != 0) {
                    toEnvio.setIdCedis(idCedis);
                    idCedisPlanta = this.idCedis;
                } else {
                    toEnvio.setIdCedis(this.idCedis);
                    strSQL = "SELECT idCedisPlanta FROM cedis WHERE idCedis=" + this.idCedis;
                    ResultSet rs = st.executeQuery(strSQL);
                    rs.next();
                    idCedisPlanta = rs.getInt("idCedisPlanta");
                }
                strSQL = "INSERT INTO envios (idCedis, folio, fecha, fechaEnvio, fechaFletera, prioridad, idUsuario, estatus)\n"
                        + "VALUES (" + toEnvio.getIdCedis() + ", 0, GETDATE(), '', '', 0, " + toEnvio.getIdUsuario() + ", 0)";
                st.executeUpdate(strSQL);

                ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idEnvio");
                if (rs.next()) {
                    toEnvio.setIdEnvio(rs.getInt("idEnvio"));
                }
                strSQL = "SELECT GETDATE() AS hoy, DATEADD(YEAR, -1, GETDATE()) AS anioAnterior, *\n"
                        + "FROM envios WHERE idEnvio=" + toEnvio.getIdEnvio();
                rs = st.executeQuery(strSQL);
                rs.next();
                String hoy = formatoSQL.format(rs.getDate("hoy").getTime());
                String anioAnterior = formatoSQL.format(rs.getDate("anioAnterior"));
                toEnvio.setGenerado(new java.util.Date(rs.getTimestamp("fecha").getTime()));
                this.construir(rs);

                ResultSet rs1;
                TOEnvioTraspaso toTraspaso;
//                solicitudes = new ArrayList<>();
                strSQL = "SELECT idAlmacen, idEmpresa FROM almacenes WHERE idCedis=" + idCedisPlanta + " AND pedidoElectronico=1";
                rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    strSQL = "SELECT idAlmacen FROM almacenes\n"
                            + "WHERE idCedis=" + toEnvio.getIdCedis() + " AND pedidoElectronico=1 AND idEmpresa=" + rs.getInt("idEmpresa");
                    rs1 = st1.executeQuery(strSQL);
                    if (rs1.next()) {
                        TOSolicitud toSolicitud = new TOSolicitud();
                        toSolicitud.setIdUsuario(this.idUsuario);
                        toSolicitud.setIdAlmacen(rs1.getInt("idAlmacen"));
                        toSolicitud.setIdAlmacenOrigen(rs.getInt("idAlmacen"));
                        toSolicitud.setEstatus(0);
                        toSolicitud.setEnvio(1);
                        Solicitudes.agrega(cn, toSolicitud);
//                        solicitudes.add(toSolicitud);

                        strSQL = "INSERT INTO enviosSolicitudes (idSolicitud, idEnvio, fechaProduccion, diasInventario, directo)\n"
                                + "VALUES (" + toSolicitud.getIdSolicitud() + ", " + toEnvio.getIdEnvio() + ", '', 0, 0)";
                        st1.executeUpdate(strSQL);

                        strSQL = this.sqlCrearEnvio(toEnvio.getIdEnvio(), toSolicitud.getIdSolicitud(), toSolicitud.getIdAlmacen(), hoy, anioAnterior);
                        st1.executeUpdate(strSQL);

                        strSQL = "INSERT INTO solicitudesDetalle (idSolicitud, idEmpaque, cantSolicitada)\n"
                                + "SELECT idSolicitud, idEmpaque, 0 FROM enviosSolicitudesDetalle\n"
                                + "WHERE idEnvio=" + toEnvio.getIdEnvio() + " AND idSolicitud=" + toSolicitud.getIdSolicitud();
                        st1.executeUpdate(strSQL);

                        TOMovimientoOficina toMov = new TOMovimientoOficina(35);
                        toMov.setIdEmpresa(rs.getInt("idEmpresa"));
                        toMov.setIdAlmacen(toSolicitud.getIdAlmacenOrigen());
                        toMov.setTipoDeCambio(1);
                        toMov.setIdUsuario(this.idUsuario);
                        toMov.setIdReferencia(toSolicitud.getIdAlmacen());
                        toMov.setReferencia(toSolicitud.getIdSolicitud());
                        toMov.setPropietario(0);

                        movimientos.Movimientos.agregaMovimientoAlmacen(cn, toMov, false);
                        movimientos.Movimientos.agregaMovimientoOficina(cn, toMov, false);

                        strSQL = "INSERT INTO movimientosDetalle\n"
                                + "SELECT " + toMov.getIdMovto() + ", D.idEmpaque, 0, 0, 0, 0, 0, 0, 0, 0, P.idImpuesto, '', 0, 0\n"
                                + "FROM enviosSolicitudesDetalle D\n"
                                + "INNER JOIN empaques E ON E.idEmpaque=D.idEmpaque\n"
                                + "INNER JOIN productos P ON P.idProducto=E.idProducto\n"
                                + "WHERE idSolicitud=" + toSolicitud.getIdSolicitud();
                        st1.executeUpdate(strSQL);

                        toTraspaso = this.obtenTraspaso(cn, toSolicitud.getIdSolicitud());
                        this.obtenFincados(cn, toEnvio, toTraspaso, true);
                        this.calculaDiasInventarioGeneral(cn, toTraspaso);
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
//        return traspasos;
    }

    public boolean esPlanta() throws SQLException {
        boolean planta = false;
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT idCedisPlanta FROM cedis WHERE idCedis=" + this.idCedis);
            if (rs.next()) {
                if (rs.getInt("idCedisPlanta") != 0) {
                    planta = false;
                } else {
                    planta = true;
                }
            } else {
                throw new SQLException("No se encontro el cedis configurado !!!");
            }
        } finally {
            cn.close();
        }
        return planta;
    }

    private TOEnvio construir(ResultSet rs) throws SQLException {
        TOEnvio toEnvio = new TOEnvio();
        toEnvio.setIdEnvio(rs.getInt("idEnvio"));
        toEnvio.setIdCedis(rs.getInt("idCedis"));
        toEnvio.setFolioEnvio(rs.getInt("folio"));
        toEnvio.setGenerado(new java.util.Date(rs.getTimestamp("fecha").getTime()));
        toEnvio.setFechaEnvio(new java.util.Date(rs.getTimestamp("fechaEnvio").getTime()));
        toEnvio.setFechaFletera(new java.util.Date(rs.getTimestamp("fechaFletera").getTime()));
//        toEnvio.setFechaAnita(new java.util.Date(rs.getTimestamp("fechaAnita").getTime()));
//        toEnvio.setFechaQuimicos(new java.util.Date(rs.getTimestamp("fechaQuimicos").getTime()));
//        toEnvio.setDiasInventario(rs.getInt("diasInventario"));
        toEnvio.setPrioridad(rs.getInt("prioridad"));
//        toEnvio.setFechaEstatus(new java.util.Date(rs.getTimestamp("fechaEstatus").getTime()));
        toEnvio.setIdUsuario(rs.getInt("idUsuario"));
//        toEnvio.setPropietario(rs.getInt("propietario"));
        toEnvio.setEstatus(rs.getInt("estatus"));
        return toEnvio;
    }

    private ArrayList<TOEnvio> obtenEnvios(Connection cn, boolean esPlanta, int estatus, Date fechaInicial) throws SQLException {
        String condicion = "C.idCedis=" + this.idCedis;
        if (esPlanta) {
            condicion = "C.idCedisPlanta=" + this.idCedis;
        }
        condicion += " AND E.estatus=" + estatus;
        String strSQL = "SELECT E.*\n"
                + "FROM (SELECT DISTINCT E.idEnvio\n"
                + "	FROM solicitudes S\n"
                + "	INNER JOIN almacenes A ON A.idAlmacen=S.idAlmacen\n"
                + "	INNER JOIN cedis C ON C.idCedis=A.idCedis\n"
                + "	INNER JOIN enviosSolicitudes ES ON ES.idSolicitud=S.idSolicitud\n"
                + "	INNER JOIN envios E ON E.idEnvio=ES.idEnvio\n"
                + "	WHERE " + condicion;
        if (estatus != 0) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            strSQL += "\n               AND CONVERT(date, E.fecha) >= '" + format.format(fechaInicial) + "'";
        }
        strSQL += ") U\n"
                + "INNER JOIN envios E ON E.idEnvio=U.idEnvio\n"
                + "ORDER BY E.fecha";
        ArrayList<TOEnvio> envios = new ArrayList<>();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                envios.add(this.construir(rs));
            }
        }
        return envios;
    }

    public ArrayList<TOEnvio> obtenerEnvios(int estatus, Date fechaInicial) throws SQLException {
        String strSQL;
        ArrayList<TOEnvio> envios = new ArrayList<>();
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            strSQL = "SELECT idCedisPlanta FROM cedis WHERE idCedis=" + this.idCedis;
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                envios = this.obtenEnvios(cn, rs.getInt("idCedisPlanta") == 0, estatus, fechaInicial);
            } else {
                throw new SQLException("No existe el cedis configurado !!!");
            }
        } finally {
            cn.close();
        }
        return envios;
    }
}
