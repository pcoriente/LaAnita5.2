package envios.dao;

import envios.dominio.EnvioTraspaso;
import envios.to.TOEnvio;
import envios.to.TOEnvioProducto;
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
import pedidos.to.TOProductoPedido;
import solicitudes.Solicitudes;
import solicitudes.to.TOSolicitud;
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

    public void grabarEnviada(TOProductoPedido toProd) throws SQLException {
        String strSQL = "UPDATE enviosPedidosDetalle SET cantEnviada=" + toProd.getCantEnviada() + "\n"
                + "WHERE idPedido=" + toProd.getIdPedido() + " AND idEnvio=" + toProd.getIdEnvio() + " AND idEmpaque=" + toProd.getIdProducto();
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            st.executeUpdate(strSQL);
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
                    ordenOld = rs.getInt("orden")+1;
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

    public void grabarDirecto(int idEnvio, int idPedido, boolean directo) throws SQLException {
        String strSQL = "UPDATE enviosPedidos SET directo=" + (directo ? 1 : 0) + "\n"
                + "WHERE idPedido=" + idPedido + " AND idEnvio=" + idEnvio;
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            st.executeUpdate(strSQL);
        } finally {
            cn.close();
        }
    }

    public void grabarAgregado(int idEnvio, int idPedido, boolean agregar) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                if (!agregar) {
                    strSQL = "DELETE FROM enviosPedidosDetalle WHERE idPedido=" + idPedido + " AND idEnvio=" + idEnvio;
                    st.executeUpdate(strSQL);

                    strSQL = "DELETE FROM enviosPedidos WHERE idPedido=" + idPedido + " AND idEnvio=" + idEnvio;
                } else {
                    strSQL = "INSERT INTO enviosPedidos (idPedido, idEnvio, directo, peso, orden)\n"
                            + "VALUES (" + idPedido + ", " + idEnvio + ", 0, 0, 0)";
                }
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

    public ArrayList<TOProductoPedido> obtenerDetalleFincado(int idEnvio, TOPedido toPed) throws SQLException {
        ArrayList<TOProductoPedido> detalle = new ArrayList<>();
        String strSQL = Pedidos.sqlPedido(toPed.getIdAlmacen()) + "\n"
                + "WHERE EP.idEnvio=" + idEnvio + " AND EP.idPedido=" + toPed.getReferencia();
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

    public ArrayList<TOPedido> obtenerFincados(int idAlmacen) throws SQLException {
        ArrayList<TOPedido> pedidos = new ArrayList<>();
        String strSQL = Pedidos.sqlPedido(idAlmacen) + "\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=28 AND P.estatus IN (1, 3)\n"
                + "ORDER BY P.fecha";
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                pedidos.add(Pedidos.construirPedido(rs));
            }
        } finally {
            cn.close();
        }
        return pedidos;
    }

    public void grabarSolicitada(TOEnvioProducto toProd) throws SQLException {
        String strSQL = "UPDATE solicitudesDetalle SET cantSolicitada=" + toProd.getSugerido() + "\n"
                + "WHERE idSolicitud=" + toProd.getIdSolicitud() + " AND idEmpaque=" + toProd.getIdProducto();
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            st.executeUpdate(strSQL);
        } finally {
            cn.close();
        }
    }

    public void grabarSugerido(TOEnvioProducto toProd) throws SQLException {
        String strSQL = "UPDATE enviosSolicitudesDetalle SET sugerido=" + toProd.getSugerido() + "\n"
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
        Traspasos.construir(toProd, rs);
        return toProd;
    }

    public ArrayList<TOEnvioProducto> obtenerDetalle(int idEnvio, TOTraspaso toTraspaso) throws SQLException {
        String strSQL = "SELECT ESD.idEnvio, ESD.idSolicitud, ESD.estadistica, ESD.sugerido\n"
                + "     , SD.cantSolicitada, SS.cantTraspasada, D.*\n"
                + "FROM (SELECT SD.idEmpaque, SD.cantSolicitada, ISNULL(SS.cantTraspasada, 0) AS cantTraspasada\n"
                + "	 FROM (SELECT S.idSolicitud, D.idEmpaque, SUM(D.cantFacturada) AS cantTraspasada\n"
                + "		FROM movimientos M\n"
                + "		INNER JOIN solicitudes S ON S.idSolicitud=M.referencia\n"
                + "		INNER JOIN movimientosDetalle D ON D.idMovto=M.idMovto\n"
                + "		WHERE S.idSolicitud=" + toTraspaso.getReferencia() + " AND M.idTipo=35 AND M.estatus=7\n"
                + "		GROUP BY S.idSolicitud, D.idEmpaque) SS\n"
                + "	RIGHT JOIN solicitudesDetalle SD ON SD.idSolicitud=SS.idSolicitud AND SD.idEmpaque=SS.idEmpaque\n"
                + "	WHERE SD.idSolicitud=" + toTraspaso.getReferencia() + ") SS\n"
                + "INNER JOIN movimientosDetalle D ON D.idEmpaque=SS.idEmpaque\n"
                + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                + "INNER JOIN enviosSolicitudes ES ON ES.idSolicitud=M.referencia\n"
                + "INNER JOIN solicitudesDetalle SD ON SD.idSolicitud=M.referencia AND SD.idEmpaque=D.idEmpaque\n"
                + "INNER JOIN enviosSolicitudesDetalle ESD ON ESD.idSolicitud=M.referencia AND ESD.idEnvio=ES.idEnvio\n"
                + "             AND ESD.idEmpaque=D.idEmpaque\n"
                + "WHERE ES.idSolicitud=" + toTraspaso.getReferencia();
        ArrayList<TOEnvioProducto> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    detalle.add(this.construirProducto(rs));
                }
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

    public ArrayList<TOTraspaso> obtenerTraspasos(int idEnvio) throws SQLException {
        ArrayList<TOTraspaso> traspasos = new ArrayList<>();
        String strSQL = Traspasos.sqlTraspaso() + "\n"
                + "INNER JOIN enviosSolicitudes ES ON ES.idSolicitud=S.idSolicitud\n"
                + "WHERE ES.idEnvio=" + idEnvio;
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                traspasos.add(Traspasos.construir(rs));
            }
        } finally {
            cn.close();
        }
        return traspasos;
    }

    public EnvioTraspaso obtenerTraspaso(int idSolicitud) throws SQLException {
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
        EnvioTraspaso envioTraspaso = new EnvioTraspaso();
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

    private String sqlCrearEnvio(int idEnvio, int idSolicitud, int idAlmacen, String hoy, String anioAnterior) {
        String strSQL = "INSERT INTO enviosSolicitudesDetalle (idEnvio, idSolicitud, idEmpaque, estadistica, sugerido)\n"
                + "SELECT " + idEnvio + ", " + idSolicitud + ", EE.idEmpaque, CEILING(EE.estadistica), CEILING(EE.estadistica)\n"
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

    public ArrayList<TOTraspaso> crear(int idCedis, TOEnvio toEnvio) throws SQLException {
        String strSQL;
        int idCedisPlanta;
        toEnvio = new TOEnvio();
        ArrayList<TOTraspaso> traspasos = new ArrayList<>();
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
                strSQL = "INSERT INTO envios (idCedis, folio, fecha, fechaEnvio, fechaFletera, fechaAnita, fechaQuimicos, diasInventario, prioridad, idUsuario, estatus)\n"
                        + "VALUES (" + toEnvio.getIdCedis() + ", 0, GETDATE(), '', '', '', '', 0, 0, " + toEnvio.getIdUsuario() + ", 0)";
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
                strSQL = "SELECT idAlmacen, idEmpresa FROM almacenes WHERE idCedis=" + idCedisPlanta + " AND almacenEnvio=1";
                rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    strSQL = "SELECT idAlmacen FROM almacenes\n"
                            + "WHERE idCedis=" + toEnvio.getIdCedis() + " AND almacenEnvio=1 AND idEmpresa=" + rs.getInt("idEmpresa");
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

                        strSQL = "INSERT INTO enviosSolicitudes (idSolicitud, idEnvio)\n"
                                + "VALUES (" + toSolicitud.getIdSolicitud() + ", " + toEnvio.getIdEnvio() + ")";
                        st1.executeUpdate(strSQL);

                        strSQL = this.sqlCrearEnvio(toEnvio.getIdEnvio(), toSolicitud.getIdSolicitud(), toSolicitud.getIdAlmacen(), hoy, anioAnterior);
                        st1.executeUpdate(strSQL);

                        strSQL = "INSERT INTO solicitudesDetalle (idSolicitud, idEmpaque, cantSolicitada)\n"
                                + "SELECT idSolicitud, idEmpaque, estadistica FROM enviosSolicitudesDetalle\n"
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

                        traspasos.add(Traspasos.obtenerTraspaso(cn, toMov.getIdMovto()));
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
        toEnvio.setFechaAnita(new java.util.Date(rs.getTimestamp("fechaAnita").getTime()));
        toEnvio.setFechaQuimicos(new java.util.Date(rs.getTimestamp("fechaQuimicos").getTime()));
        toEnvio.setDiasInventario(rs.getInt("diasInventario"));
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
        }
        return envios;
    }
}
