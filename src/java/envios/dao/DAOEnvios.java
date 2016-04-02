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
import movimientos.to.TOMovimiento;
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

    public ArrayList<TOEnvioTraspaso> eliminarTraspaso(int idEnvio, TOEnvioTraspaso toTraspaso) throws SQLException {
        ArrayList<TOEnvioTraspaso> traspasos = new ArrayList<>();
        String strSQL = "SELECT COUNT(*) AS traspasos FROM enviosSolicitudes ES INNER JOIN envios E ON E.idEnvio=ES.idEnvio WHERE E.idEnvio=" + idEnvio;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement(); Statement st1 = cn.createStatement()) {
                strSQL = "DELETE FROM solicitudes WHERE idSolicitud=" + toTraspaso.getReferencia();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM solicitudesDetalle WHERE idSolicitud=" + toTraspaso.getReferencia();
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

                strSQL = "SELECT EP.idVenta FROM enviosPedidos EP INNER JOIN envios E ON E.idEnvio=EP.idEnvio WHERE E.idEnvio=" + idEnvio;
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    strSQL = "DELETE FROM enviosPedidos WHERE idVenta=" + rs.getInt("idVenta");
                    st1.executeUpdate(strSQL);

                    strSQL = "DELETE FROM enviosPedidosDetalle WHERE idVenta=" + rs.getInt("idVenta");
                    st1.executeUpdate(strSQL);
                }
                traspasos = this.obtenTraspasos(cn, idEnvio);
                if (traspasos.isEmpty()) {
                    strSQL = "DELETE FROM envios WHERE idEnvio=" + idEnvio;
                    st.executeUpdate(strSQL);
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

    public void grabarEnviada(TOPedido toPed, TOPedidoProducto toProd) throws SQLException {
        String strSQL = "UPDATE enviosPedidosDetalle SET cantEnviada=" + toProd.getCantEnviada() + "\n"
                + "WHERE idVenta=" + toProd.getIdVenta() + " AND idEnvio=" + toProd.getIdEnvio() + " AND idEmpaque=" + toProd.getIdProducto();
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            int n = st.executeUpdate(strSQL);
            if (n != 0) {
                if (toPed.getDirecto() != 0) {
                    strSQL = "UPDATE solicitudesDetalle SET cantSolicitada=" + toProd.getCantEnviada() + "\n"
                            + "WHERE idSolicitud=" + toPed.getIdSolicitud() + " AND idEmpaque=" + toProd.getIdProducto();
                    st.executeUpdate(strSQL);
                }
            } else {
                strSQL = "INSERT INTO enviosPedidosDetalle (idVenta, idEnvio, idEmpaque, cantEnviada, agregado)\n"
                        + "VALUES (" + toPed.getReferencia() + ", " + toPed.getIdEnvio() + ", " + toProd.getIdProducto() + ", " + toProd.getCantEnviada() + ", 1)";
                st.executeUpdate(strSQL);

                if (toPed.getDirecto() != 0) {
                    TOSolicitudProducto to = new TOSolicitudProducto();
                    to.setIdSolicitud(toPed.getIdSolicitud());
                    to.setIdProducto(toProd.getIdProducto());
                    to.setCantSolicitada(toProd.getCantEnviada());
                    Solicitudes.agregarProducto(cn, to);
                }
            }
        } finally {
            cn.close();
        }
    }

    public void grabarOrden(int idEnvio, int idPedido, Integer orden) throws SQLException {
        int ordenOld = 1;
        String strSQL = "SELECT MAX(orden) AS orden FROM enviosPedidos WHERE idEnvio=" + idEnvio;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    ordenOld = rs.getInt("orden") + 1;
                }
                if (orden > ordenOld) {
                    orden = ordenOld;
                }
                strSQL = "SELECT idPedido FROM enviosPedidos WHERE idEnvio=" + idEnvio + " AND orden=" + orden;
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    strSQL = "SELECT orden FROM enviosPedidos WHERE idEnvio=" + idEnvio + " AND idPedido=" + idPedido;
                    rs = st.executeQuery(strSQL);
                    ordenOld = rs.getInt("orden");
                    if (ordenOld == 0) {
                        strSQL = "UPDATE enviosPedidos SET orden=orden+1 WHERE idEnvio=" + idEnvio + " AND orden >= " + orden;
                        st.executeUpdate(strSQL);
                    } else if (orden == 0) {
                        strSQL = "UPDATE enviosPedidos SET orden=orden-1 WHERE idEnvio=" + idEnvio + " AND orden > " + ordenOld;
                        st.executeUpdate(strSQL);
                    } else if (orden < ordenOld) {
                        strSQL = "UPDATE enviosPedidos SET orden=orden+1\n"
                                + "WHERE idEnvio=" + idEnvio + " AND orden between " + orden + " AND " + (ordenOld - 1);
                        st.executeUpdate(strSQL);
                    } else if (orden > ordenOld) {
                        strSQL = "UPDATE enviosPedidos SET orden=orden-1\n"
                                + "WHERE idEnvio=" + idEnvio + " AND orden between " + (ordenOld + 1) + " AND " + orden;
                        st.executeUpdate(strSQL);
                    }
                }
                strSQL = "UPDATE enviosPedidos SET orden=" + orden + "\n"
                        + "WHERE idPedido=" + idPedido + " AND idEnvio=" + idEnvio;
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

    public void grabarDirecto(int idEnvio, TOPedido toPedido, int idAlmacenOrigen, boolean directo) throws SQLException {
        String strSQL;
        toPedido.setDirecto(directo ? 1 : 0);
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            if (directo) {
                toPedido.setIdSolicitud(Solicitudes.agregarSolicitudDirecto(cn, toPedido.getIdAlmacen(), idAlmacenOrigen, this.idUsuario));

                strSQL = "UPDATE ventas\n"
                        + "SET directo=" + toPedido.getDirecto() + ", idSolicitud=" + toPedido.getIdSolicitud() + "\n"
                        + "WHERE idVenta=" + toPedido.getReferencia();
                st.executeUpdate(strSQL);

                strSQL = "SELECT * FROM enviosPedidos WHERE idVenta=" + toPedido.getReferencia() + " AND idEnvio=" + idEnvio;
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    TOSolicitudProducto toProd = new TOSolicitudProducto();

                    strSQL = "SELECT * FROM enviosPedidosDetalle WHERE idVenta=" + toPedido.getReferencia() + " AND idEnvio=" + idEnvio;
                    rs = st.executeQuery(strSQL);
                    while (rs.next()) {
                        toProd.setIdSolicitud(toPedido.getIdSolicitud());
                        toProd.setIdProducto(rs.getInt("idEmpaque"));
                        toProd.setCantSolicitada(rs.getDouble("cantEnviada"));
                        Solicitudes.agregarProducto(cn, toProd);
                    }
                }
            } else {
                strSQL = "UPDATE solicitudes SET estatus=6 WHERE idSolicitud=" + toPedido.getIdSolicitud();
                st.executeUpdate(strSQL);

                toPedido.setIdSolicitud(0);

                strSQL = "UPDATE ventas\n"
                        + "SET directo=" + toPedido.getDirecto() + ", idSolicitud=" + toPedido.getIdSolicitud() + "\n"
                        + "WHERE idVenta=" + toPedido.getReferencia();
                st.executeUpdate(strSQL);
            }
        } finally {
            cn.close();
        }
    }

    public void grabarAgregado(int idEnvio, TOPedido toPedido, int idAlmacenOrigen, boolean agregar) throws SQLException {
        String strSQL;
        int idSolicitud = 0;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                if (!agregar) {
                    strSQL = "DELETE FROM enviosPedidosDetalle WHERE idVenta=" + toPedido.getReferencia() + " AND idEnvio=" + idEnvio;
                    st.executeUpdate(strSQL);

                    if (toPedido.getDirecto() != 0 && toPedido.getIdSolicitud() != 0) {
                        strSQL = "DELETE FROM solicitudesDetalle WHERE idSolicitud=" + toPedido.getIdSolicitud();
                        st.executeUpdate(strSQL);
                    }
                    strSQL = "DELETE FROM enviosPedidos WHERE idVenta=" + toPedido.getReferencia() + " AND idEnvio=" + idEnvio;
                    st.executeUpdate(strSQL);
                } else {
                    if (toPedido.getDirecto() != 0 && toPedido.getIdSolicitud() == 0) {
                        toPedido.setIdSolicitud(Solicitudes.agregarSolicitudDirecto(cn, toPedido.getIdAlmacen(), idAlmacenOrigen, this.idUsuario));

                        strSQL = "UPDATE ventas\n"
                                + "SET idSolicitud=" + toPedido.getIdSolicitud() + "\n"
                                + "WHERE idVenta=" + toPedido.getReferencia();
                        st.executeUpdate(strSQL);
                    }
                    strSQL = "INSERT INTO enviosPedidos (idVenta, idEnvio, orden)\n"
                            + "VALUES (" + toPedido.getReferencia() + ", " + idEnvio + ", 0)";
                    st.executeUpdate(strSQL);
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

    public double obtenerImpuestosProducto(int idMovto, int idEmpaque, ArrayList<ImpuestosProducto> impuestos) throws SQLException {
        double importeImpuestos = 0;
        try (Connection cn = this.ds.getConnection()) {
            importeImpuestos = movimientos.Movimientos.obtenImpuestosProducto(cn, idMovto, idEmpaque, impuestos);
        }
        return importeImpuestos;
    }

    public ArrayList<TOPedidoProducto> obtenerDetalleFincado(int idEnvio, TOPedido toPed) throws SQLException {
        ArrayList<TOPedidoProducto> detalle = new ArrayList<>();
        String strSQL = Pedidos.sqlObtenProducto() + "\n"
                + "WHERE M.idMovto=" + toPed.getIdMovto();
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                detalle.add(Pedidos.construirProducto(rs));
            }
            movimientos.Movimientos.bloquearMovimientoOficina(cn, toPed, this.idUsuario);
        } finally {
            cn.close();
        }
        return detalle;
    }

    private TOPedido construirFincado(ResultSet rs) throws SQLException {
        TOPedido toPedido = new TOPedido();
        toPedido.setIdEnvio(rs.getInt("idEnvio"));
//        toPedido.setPeso(rs.getDouble("peso"));
        toPedido.setOrden(rs.getInt("orden"));
        toPedido.setEnvioEstatus(rs.getInt("envioEstatus"));
        Pedidos.construyePedido(toPedido, rs);
        return toPedido;
    }

    public ArrayList<TOPedido> obtenerFincados(int idEnvio, TOEnvioTraspaso toTraspaso) throws SQLException {
        ArrayList<TOPedido> pedidos = new ArrayList<>();
////        String strSQL = "SELECT P.idEnvio, P.orden, E.estatus AS envioEstatus\n"
////                + "     , " + Pedidos.sqlPedidos() + "\n"
////                + "INNER JOIN enviosPedidos EP ON EP.idVenta=P.idVenta\n"
////                + "INNER JOIN envios E ON E.idEnvio=EP.idEnvio\n"
////                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=28 AND P.estatus IN (1, 3)\n"
////                + "ORDER BY P.fecha";
//        String strSQL = "SELECT EP.idEnvio, EP.orden, E.estatus AS envioEstatus\n"
//                + "     , " + Pedidos.sqlPedidos() + "\n"
//                + "INNER JOIN enviosPedidos EP ON EP.idVenta=V.idVenta\n"
//                + "INNER JOIN envios E ON E.idEnvio=EP.idEnvio\n"
//                + "WHERE E.idEnvio=" + idEnvio + " AND M.idAlmacen=" + toTraspaso.getIdAlmacen() + "\n"
//                + "ORDER BY V.fecha";
        String strSQL = "SELECT ISNULL(EP.idEnvio, 0) AS idEnvio, ISNULL(EP.orden, 0) AS orden\n"
                + "     , ISNULL(E.estatus, 0) AS envioEstatus\n"
                + "     , " + Pedidos.sqlPedidos() + "\n"
                + "LEFT JOIN enviosPedidos EP ON EP.idVenta=V.idVenta\n"
                + "LEFT JOIN envios E ON E.idEnvio=EP.idEnvio\n"
                + "WHERE M.idAlmacen=" + toTraspaso.getIdReferencia() + " AND M.idTipo=28 AND M.estatus=0 AND P.estatus IN (1, 3, 5) AND ISNULL(EP.idEnvio, 0) IN (0, " + idEnvio + ")\n"
                + "ORDER BY V.fecha";
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                pedidos.add(this.construirFincado(rs));
            }
        } finally {
            cn.close();
        }
        return pedidos;
    }

    public void agregarProducto(TOEnvioTraspaso toTraspaso, TOEnvioProducto toProd) throws SQLException {
        String strSQL = "INSERT INTO enviosSolicitudesDetalle (idSolicitud, idEnvio, idEmpaque, estadistica, sugerido, diasInventario, banCajas, idUsuario)\n"
                + "VALUES (" + toProd.getIdSolicitud() + ", " + toProd.getIdEnvio() + ", " + toProd.getIdProducto() + ", 0, 0, 0, 1, " + this.idUsuario + ")";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);

                strSQL = "INSERT INTO solicitudesDetalle (idSolicitud, idEmpaque, cantSolicitada)\n"
                        + "VALUES (" + toProd.getIdSolicitud() + ", " + toProd.getIdProducto() + ", 0)";
                st.executeUpdate(strSQL);

                movimientos.Movimientos.agregaProductoOficina(cn, toProd, toTraspaso.getIdImpuestoZona());
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

    public ArrayList<TOEnvioProducto> calcularPesoGeneral(TOEnvioTraspaso toTraspaso, double pesoMaximo) throws SQLException {
        boolean ban = false;
        int diasInventario, intervalo;
        double peso, pesoFincados = 0;
        ArrayList<TOEnvioProducto> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                intervalo = 10;
                diasInventario = 0;
                do {
                    toTraspaso.setDiasInventario(diasInventario);
                    this.calculaDiasInventarioGeneral(cn, toTraspaso);
                    peso = this.calculaPeso(cn, toTraspaso.getIdReferencia(), toTraspaso.getIdEnvio(), toTraspaso.getReferencia());
                    if (peso > pesoMaximo) {
                        if (diasInventario != 0) {
                            diasInventario -= 1;
                            ban = true;
                        } else {
                            throw new SQLException("El peso de los fincados excede el peso especificado");
                        }
                    } else if (peso < pesoMaximo) {
                        if (diasInventario == 0) {
                            pesoFincados = peso;
//                        } else if (diasInventario == 1) {
//                            if (peso > pesoFincados) {
//                                diasInventario = (int) ((pesoMaximo - pesoFincados) / (peso - pesoFincados));
//                                if (diasInventario == 1) {
//                                    break;
//                                }
//                            }
                        } else if (ban) {
                            break;
                        }
                        diasInventario += 1;
                    } else {
                        break;
                    }
                } while (diasInventario >= 0);
                toTraspaso.setDiasInventario(diasInventario);
                detalle = this.obtenDetalle(cn, toTraspaso, 0);

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

            this.calcularSolicitada(cn, toTraspaso, 0);
        }
    }

    public ArrayList<TOEnvioProducto> calcularDiasInventarioGeneral(TOEnvioTraspaso toTraspaso) throws SQLException {
        ArrayList<TOEnvioProducto> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                this.calculaDiasInventarioGeneral(cn, toTraspaso);
                detalle = this.obtenDetalle(cn, toTraspaso, 0);

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

                this.calcularSolicitada(cn, toTraspaso, toProd.getIdProducto());
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
    private void calcularSolicitada(Connection cn, TOEnvioTraspaso toTraspaso, int idProducto) throws SQLException {
        String condicion = "";
        String condicion1 = "";
        String condicion2 = "";
        String condicion3 = "";
        if (idProducto != 0) {
            condicion1 = " AND EPD.idEmpaque=" + idProducto;
            condicion2 = " AND ESD.idEmpaque=" + idProducto;
            condicion3 = " AND idEmpaque=" + idProducto;
        } else {
            condicion = " AND ESD.banCajas=0 AND ESD.diasInventario=ES.diasInventario";
        }
        String strSQL = "UPDATE S\n"
                + "SET cantSolicitada=CEILING(CASE WHEN E.banCajas=0\n"
                + "				THEN CASE WHEN E.estadistica*E.diasInventario + E.fincado + E.directo < E.existencia THEN 0\n"
                + "						ELSE E.estadistica*E.diasInventario + E.fincado + E.directo - E.existencia END\n"
                + "			WHEN E.cantSolicitada < E.fincado + E.directo - E.existencia\n"
                + "				THEN CASE WHEN E.fincado + E.directo < E.existencia THEN 0\n"
                + "						ELSE E.fincado + E.directo - E.existencia END\n"
                + "			ELSE E.cantSolicitada END/E.piezas)*E.piezas\n"
                + "FROM (SELECT P.*, E.piezas, ISNULL(A.existencia, 0) AS existencia\n"
                + "	FROM (SELECT ISNULL(ESD.idEmpaque, P.idEmpaque) AS idEmpaque, ISNULL(ESD.estadistica, 0) AS estadistica\n"
                + "		, ISNULL(ESD.banCajas, 1)  AS banCajas, ISNULL(ESD.diasInventario, 0) AS diasInventario\n"
                + "		, ISNULL(SD.cantSolicitada, 0) AS cantSolicitada\n"
                + "		, ISNULL(P.fincado, 0) AS fincado, ISNULL(P.directo, 0) AS directo\n"
                + "	FROM movimientos M\n"
                + "	INNER JOIN solicitudesDetalle SD ON SD.idSolicitud=M.referencia\n"
                + "	INNER JOIN enviosSolicitudes ES ON ES.idSolicitud=SD.idSolicitud\n"
                + "	INNER JOIN enviosSolicitudesDetalle ESD ON ESD.idSolicitud=SD.idSolicitud AND ESD.idEmpaque=SD.idEmpaque\n"
                + "	FULL OUTER JOIN (SELECT VD.idEmpaque\n"
                + "                             , SUM(CASE WHEN V.directo=1 THEN EPD.cantEnviada ELSE 0 END) AS directo\n"
                + "                             , SUM(CASE WHEN V.directo=0 THEN EPD.cantEnviada ELSE 0 END) AS fincado\n"
                + "			FROM movimientos M\n"
                + "			INNER JOIN ventasDetalle VD ON VD.idVenta=M.referencia\n"
                + "			INNER JOIN ventas V ON V.idVenta=VD.idVenta\n"
                + "			INNER JOIN enviosPedidos EP ON EP.idVenta=V.idVenta\n"
                + "			INNER JOIN enviosPedidosDetalle EPD ON EPD.idVenta=V.idVenta AND EPD.idEmpaque=VD.idEmpaque\n"
                + "			WHERE EP.idEnvio=" + toTraspaso.getIdEnvio() + " AND M.idAlmacen=" + toTraspaso.getIdReferencia() + condicion1 + "\n"
                + "			GROUP BY VD.idEmpaque) P ON P.idEmpaque=ESD.idEmpaque\n"
                + "	WHERE SD.idSolicitud=" + toTraspaso.getReferencia() + condicion + condicion2 + ") P\n"
                + "INNER JOIN empaques E ON P.idEmpaque=E.idEmpaque\n"
                + "LEFT JOIN (SELECT idEmpaque, SUM(existencia-separados) AS existencia\n"
                + "		FROM almacenesLotes\n"
                + "		WHERE idAlmacen=" + toTraspaso.getIdReferencia() + condicion3 + "\n"
                + "		GROUP BY idEmpaque) A ON A.idEmpaque=E.idEmpaque) E\n"
                + "INNER JOIN solicitudesDetalle S ON S.idEmpaque=E.idEmpaque\n"
                + "WHERE S.idSolicitud=" + toTraspaso.getReferencia();
        try (Statement st = cn.createStatement()) {
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
                        + "SET diasInventario=0, banCajas=" + toProd.getBanCajas() + ", idUsuario=" + this.idUsuario + "\n"
                        + "WHERE idSolicitud=" + toProd.getIdSolicitud() + " AND idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE solicitudesDetalle\n"
                        + "SET cantSolicitada=" + toProd.getCantSolicitada() + "\n"
                        + "WHERE idSolicitud=" + toProd.getIdSolicitud() + " AND idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);

                this.calcularSolicitada(cn, toTraspaso, toProd.getIdProducto());

                if (toProd.getEstadistica() != 0) {
                    strSQL = "UPDATE ESD\n"
                            + "SET diasInventario=CAST((SD.cantSolicitada+ISNULL(A.existencia, 0)-ISNULL(P.directo, 0)-ISNULL(P.fincado,0))/ESD.estadistica AS Integer)\n"
                            + "FROM enviosSolicitudesDetalle ESD\n"
                            + "INNER JOIN solicitudesDetalle SD ON SD.idSolicitud=ESD.idSolicitud AND SD.idEmpaque=ESD.idEmpaque\n"
                            + "LEFT JOIN (SELECT idEmpaque, SUM(existencia-separados) AS existencia\n"
                            + "             FROM almacenesLotes\n"
                            + "             WHERE idAlmacen=" + toTraspaso.getIdReferencia() + " AND idEmpaque=" + toProd.getIdProducto() + "\n"
                            + "             GROUP BY idEmpaque) A ON A.idEmpaque=ESD.idEmpaque\n"
                            + "LEFT JOIN (SELECT VD.idEmpaque\n"
                            + "			, SUM(CASE WHEN V.directo=1 THEN EPD.cantEnviada ELSE 0 END) AS directo\n"
                            + "			, SUM(CASE WHEN V.directo=0 THEN EPD.cantEnviada ELSE 0 END) AS fincado\n"
                            + "             FROM movimientos M\n"
                            + "             INNER JOIN ventasDetalle VD ON VD.idVenta=M.referencia\n"
                            + "             INNER JOIN ventas V ON V.idPedido=VD.idVenta\n"
                            + "             INNER JOIN enviosPedidos EP ON EP.idVenta=V.idVenta\n"
                            + "             INNER JOIN enviosPedidosDetalle EPD ON EPD.idVenta=V.idVenta AND EPD.idEmpaque=VD.idEmpaque\n"
                            + "             WHERE EP.idEnvio=" + toTraspaso.getIdEnvio() + " AND M.idAlmacen=" + toTraspaso.getIdReferencia() + " AND EPD.idEmpaque=" + toProd.getIdProducto() + "\n"
                            + "             GROUP BY VD.idEmpaque) P ON P.idEmpaque=ESD.idEmpaque\n"
                            + "WHERE ESD.idSolicitud=" + toProd.getIdSolicitud() + " AND ESD.idEmpaque=" + toProd.getIdProducto();
                    st.executeUpdate(strSQL);
                }
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
        toProd.setExistencia(rs.getDouble("existencia"));
        toProd.setSugerido(rs.getDouble("sugerido"));
        toProd.setDiasInventario(rs.getInt("diasInventario"));
        toProd.setBanCajas(rs.getInt("banCajas"));
//        toProd.setSolicitada(rs.getDouble("solicitada"));
        toProd.setFincada(rs.getDouble("fincada"));
        toProd.setDirecta(rs.getDouble("directa"));
        Traspasos.construir(toProd, rs);
        return toProd;
    }

    private String sqlDetalle(TOEnvioTraspaso toTraspaso, int idProducto) {
        String condicion = "";
        if (idProducto != 0) {
            condicion = " AND VD.idEmpaque=" + idProducto;
        }
        return "SELECT ET.*, E.piezas, ISNULL(A.existencia, 0) AS existencia\n"
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
                + "	FULL OUTER JOIN (SELECT VD.idEmpaque AS idEmpaque\n"
                + "                             , SUM(CASE WHEN V.directo=1 THEN EPD.cantEnviada ELSE 0 END) AS directo\n"
                + "                             , SUM(CASE WHEN V.directo=0 THEN EPD.cantEnviada ELSE 0 END) AS fincado\n"
                + "                         FROM movimientos M\n"
                + "                         INNER JOIN ventasDetalle VD ON VD.idVenta=M.referencia\n"
                + "                         INNER JOIN ventas V ON V.idVenta=VD.idVenta\n"
                + "                         INNER JOIN enviosPedidosDetalle EPD ON EPD.idVenta=VD.idVenta AND EPD.idEmpaque=VD.idEmpaque\n"
                + "                         INNER JOIN envios E ON E.idEnvio=EPD.idEnvio\n"
                + "                         WHERE E.idEnvio=" + toTraspaso.getIdEnvio() + " AND M.idAlmacen=" + toTraspaso.getIdReferencia() + condicion + "\n"
                + "                         GROUP BY VD.idEmpaque) P ON P.idEmpaque=T.idEmpaque) ET\n"
                + "INNER JOIN empaques E ON E.idEmpaque=ET.idEmpaque\n"
                + "LEFT JOIN (SELECT ED.idEmpaque, SUM(ED.existencia-ED.separados) AS existencia\n"
                + "             FROM almacenesLotes ED\n"
                + "             WHERE ED.idAlmacen=" + toTraspaso.getIdReferencia() + "\n"
                + "             GROUP BY idEmpaque) A ON A.idEmpaque=E.idEmpaque";
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

    public ArrayList<TOEnvioProducto> obtenerDetalle(int idEnvio, TOEnvioTraspaso toTraspaso) throws SQLException {
        ArrayList<TOEnvioProducto> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                detalle = this.obtenDetalle(cn, toTraspaso, 0);
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
        toTraspaso.setIdEnvio(rs.getInt("idEnvio"));
        toTraspaso.setDiasInventario(rs.getInt("diasInventario"));
        toTraspaso.setFechaProduccion(new java.util.Date(rs.getDate("fechaProduccion").getTime()));
        toTraspaso.setDirecto(rs.getInt("directo"));
        Traspasos.construir(rs, toTraspaso);
        return toTraspaso;
    }

    private ArrayList<TOEnvioTraspaso> obtenTraspasos(Connection cn, int idEnvio) throws SQLException {
        ArrayList<TOEnvioTraspaso> traspasos = new ArrayList<>();
        String strSQL = "SELECT SE.idEnvio, SE.diasInventario, SE.fechaProduccion, SE.directo\n"
                + "     , " + Traspasos.sqlTraspaso() + "\n"
                + "INNER JOIN enviosSolicitudes SE ON SE.idSolicitud=S.idSolicitud\n"
                + "INNER JOIN envios E ON E.idEnvio=SE.idEnvio\n"
                + "WHERE E.idEnvio=" + idEnvio + " AND M.idTipo=35";
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

//    private TOEnvioTraspaso obtenTraspaso(Connection cn, int idSolicitud) throws SQLException {
//        TOEnvioTraspaso toTraspaso = null;
//        String strSQL = Traspasos.sqlTraspaso() + "\n"
//                + "WHERE S.idSolicitud=" + idSolicitud;
//        try (Statement st = cn.createStatement()) {
//            ResultSet rs = st.executeQuery(strSQL);
//            if (rs.next()) {
//                toTraspaso = this.construirTraspaso(rs);
//            }
//        }
//        return toTraspaso;
//    }
    private String sqlCrearEnvio(int idEnvio, int idSolicitud, int idAlmacen, String hoy, String anioAnterior) {
        String strSQL = "INSERT INTO enviosSolicitudesDetalle (idEnvio, idSolicitud, idEmpaque, estadistica, sugerido, diasInventario, banCajas, idUsuario)\n"
                + "SELECT " + idEnvio + ", " + idSolicitud + ", EE.idEmpaque, CEILING(EE.estadistica), 0, 0, 0, " + this.idUsuario + "\n"
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
                        toSolicitud.setEstatus(3);
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

//                        traspasos.add(this.obtenTraspaso(cn, toSolicitud.getIdSolicitud()));
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
