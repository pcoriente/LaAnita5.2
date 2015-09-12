package movimientos.dao;

import compras.to.TOProductoCompraOficina;
import compras.dominio.ProductoCompraOficina;
import movimientos.dominio.MovimientoAlmacenProducto;
import impuestos.dominio.ImpuestosProducto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import movimientos.to1.Lote1;
import movimientos.dominio.MovimientoTipo;
import movimientos.to.TOMovimientoOficina;
import movimientos.to.TOMovimientoAlmacen;
import movimientos.to.TOMovimientoAlmacenProducto;
import movimientos.to1.TOMovimientoProducto;
import movimientos.to.TOProductoAlmacen;
import movimientos.to.TOProductoOficina;
import pedidos.to.TOPedido;
import pedidos.to.TOProductoPedido;
import traspasos.to.TORecepcion;
import traspasos.to.TORecepcionProducto;
import traspasos.to.TOTraspaso;
import traspasos.to.TOTraspasoProducto;
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

    public void actualizaEntradaAlmacen(int idMovto, TOProductoAlmacen toProd, double separados) throws SQLException {
        String strSQL = "UPDATE movimientosDetalleAlmacen\n"
                + "SET cantidad=" + toProd.getCantidad() + "\n"
                + "WHERE idMovtoAlmacen=" + toProd.getIdMovtoAlmacen() + " AND idEmpaque=" + toProd.getIdProducto() + " AND lote='" + toProd.getLote() + "'";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosDetalle\n"
                        + "SET cantFacturada=cantFacturada-" + separados + "+" + toProd.getCantidad() + "\n"
                        + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private TORecepcion construirRecepcion(ResultSet rs) throws SQLException {
        TORecepcion to = new TORecepcion();
        to.setIdSolicitud(rs.getInt("idSolicitud"));
        to.setSolicitudFolio(rs.getInt("solicitudFolio"));
        to.setSolicitudFecha(new java.util.Date(rs.getTimestamp("solicitudFecha").getTime()));
        to.setTraspasoFolio(rs.getInt("traspasoFolio"));
        to.setTraspasoFecha(new java.util.Date(rs.getTimestamp("traspasoFecha").getTime()));
        this.construirMovimiento(rs, to);
        return to;
    }

    public ArrayList<TORecepcion> obtenerRecepciones(int idAlmacen, int estatus, Date fechaInicial) throws SQLException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TORecepcion> movimientos = new ArrayList<>();
        String strSQL = "SELECT M.*, R.idMovtoAlmacen\n"
                + "     , T.folio AS traspasoFolio, T.fecha AS traspasoFecha\n"
                + "     , S.idSolicitud, S.folio AS solicitudFolio, S.fecha AS solicitudFecha\n"
                + "FROM movimientos M\n"
                + "INNER JOIN movimientosRelacionados R ON R.idMovto=M.idMovto\n"
                + "INNER JOIN movimientos T ON T.idMovto=M.referencia\n"
                + "INNER JOIN solicitudes S ON S.idSolicitud=T.referencia\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=9 AND M.estatus=" + estatus + "\n"
                + "         AND CONVERT(date, M.fecha) <= '" + format.format(fechaInicial) + "'\n"
                + "ORDER BY M.fecha";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    movimientos.add(this.construirRecepcion(rs));
                }
            }
        }
        return movimientos;
    }

//    public ArrayList<TOTraspasoProducto> obtenerSolicitudDetalle() {
//        
//    }
//    public ArrayList<TOTraspaso> obtenerSolicitudes(int idAlmacen, int estatus, Date fechaInicial) throws SQLException {
//        ArrayList<TOTraspaso> solicitudes = new ArrayList<>();
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        String strSQL = "SELECT S.folio AS solicitudFolio, S.fecha AS solicitudFecha, S.idUsuario AS solicitudIdUsuario\n"
//                + "     , S.propietario AS solicitudPropietario, S.estatus AS solicitudEstatus\n"
//                + "     , M.*, R.idMovtoAlmacen\n"
//                + "FROM movimientos M\n"
//                + "INNER JOIN movimientosRelacionados R ON R.idMovto=M.idMovto\n"
//                + "INNER JOIN solicitudes S ON S.idSolicitud=M.referencia\n"
//                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=35 AND M.estatus=" + estatus + "\n"
//                + "         AND CONVERT(date, M.fecha) <= '" + format.format(fechaInicial) + "'\n"
//                + "ORDER BY M.fecha DESC";
//        try (Connection cn = this.ds.getConnection()) {
//            try (Statement st = cn.createStatement()) {
//                ResultSet rs = st.executeQuery(strSQL);
//                while (rs.next()) {
//                    solicitudes.add(construirTraspaso(rs));
//                }
//            }
//        }
//        return solicitudes;
//    }
    public void eliminarTraspasoSolicitud(TOTraspaso to) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "DELETE FROM movimimientos WHERE idMovto=" + to.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosAlmacen WHERE idMovtoAlmacen=" + to.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM solicitudes WHERE idSolicitud=" + to.getReferencia();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM solicitudesDetalle WHERE idSolicitud=" + to.getReferencia();
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

    public void grabarTraspasoSolicitud(TOTraspaso to) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                to.setSolicitudEstatus(1);
                to.setSolicitudIdUsuario(this.idUsuario);
                to.setSolicitudFolio(this.obtenerMovimientoFolio(cn, true, to.getIdReferencia(), 53));

                String strSQL = "UPDATE solicitudes\n"
                        + "SET folio=" + to.getSolicitudFolio() + ", fecha=GETDATE(), idUsuario=" + to.getSolicitudIdUsuario() + ", estatus=" + to.getSolicitudEstatus() + "\n"
                        + "WHERE idSolicitud=" + to.getReferencia();
                st.executeUpdate(strSQL);

                strSQL = "SELECT fecha FROM solicitudes WHERE idSolicitud=" + to.getReferencia();
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    to.setSolicitudFecha(new java.util.Date(rs.getDate("fecha").getTime()));
                }
                to.setEstatus(1);
//                this.agregarMovimientoRelacionado(to, false);
                strSQL = "UPDATE movimientos SET estatus=" + to.getEstatus() + " WHERE idMovto=" + to.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosAlmacen SET estatus=" + to.getEstatus() + " WHERE idMovtoAlmacen=" + to.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM solicitudesDetalle WHERE idSolicitud=" + to.getReferencia() + " AND cantSolicitada=0";
                st.executeUpdate(strSQL);

                strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantFacturada, cantSinCargo, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior)\n"
                        + "SELECT " + to.getIdMovto() + ", SD.idEmpaque, 0, 0, 0, 0, 0, 0, 0, 0, P.idImpuesto, '', 0 \n"
                        + "FROM solicitudesDetalle SD\n"
                        + "INNER JOIN empaques E ON E.idEmpaque=SD.idEmpaque\n"
                        + "INNER JOIN productos P ON P.idProducto=E.idProducto\n"
                        + "WHERE SD.idSolicitud=" + to.getReferencia();
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

    public void modificarSolicitudProducto(TOTraspasoProducto to) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                strSQL = "UPDATE solicitudesDetalle\n"
                        + "SET cantSolicitada=" + to.getCantSolicitada() + "\n"
                        + "WHERE idSolicitud=" + to.getIdSolicitud() + " AND idEmpaque=" + to.getIdProducto();
                st.executeUpdate(strSQL);
            }
        }
    }

    public void agregarSolicitudProducto(TOTraspasoProducto to) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                strSQL = "INSERT INTO solicitudesDetalle (idSolicitud, idEmpaque, cantSolicitada)\n"
                        + "VALUES (" + to.getIdSolicitud() + ", " + to.getIdProducto() + ", " + to.getCantSolicitada() + ")";
                st.executeUpdate(strSQL);
            }
        }
    }

    public void agregarSolicitud(TOTraspaso to) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "INSERT INTO solicitudes (folio, fecha, idUsuario, propietario, estatus)\n"
                        + "VALUES (0, GETDATE(), " + this.idUsuario + ", " + this.idUsuario + ", 0)";
                st.executeUpdate(strSQL);

                ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idSolicitud");
                if (rs.next()) {
                    to.setReferencia(rs.getInt("idSolicitud"));
                }
                rs = st.executeQuery("SELECT fecha FROM solicitudes WHERE idSolicitud=" + to.getReferencia());
                if (rs.next()) {
                    to.setSolicitudFecha(new java.util.Date(rs.getDate("fecha").getTime()));
                    to.setSolicitudIdUsuario(this.idUsuario);
                }
                this.agregarMovimientoRelacionado(to, false);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private TOTraspaso construirTraspaso(ResultSet rs) throws SQLException {
        TOTraspaso to = new TOTraspaso();
        to.setSolicitudFolio(rs.getInt("solicitudFolio"));
        to.setSolicitudFecha(new java.util.Date(rs.getTimestamp("solicitudFecha").getTime()));
        to.setSolicitudIdUsuario(rs.getInt("solicitudIdUsuario"));
        to.setSolicitudProietario(rs.getInt("solicitudPropietario"));
        to.setSolicitudEstatus(rs.getInt("solicitudEstatus"));
//        if(to.getSolicitudEstatus()!=0) {
        this.construirMovimiento(rs, to);
//        }
        return to;
    }

    public ArrayList<TOTraspaso> obtenerTraspasos(int idAlmacen, int estatus, Date fechaInicial) throws SQLException {
        ArrayList<TOTraspaso> traspasos = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String strSQL = "SELECT S.folio AS solicitudFolio, S.fecha AS solicitudFecha, S.idUsuario AS solicitudIdUsuario\n"
                + "     , S.propietario AS solicitudPropietario, S.estatus AS solicitudEstatus\n"
                + "     , M.*, R.idMovtoAlmacen\n"
                + "FROM movimientos M\n"
                + "INNER JOIN solicitudes S ON S.idSolicitud=M.referencia\n"
                + "INNER JOIN movimientosRelacionados R ON R.idMovto=M.idMovto\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=35 AND M.estatus=" + estatus + "\n"
                + "         AND CONVERT(date, M.fecha) <= '" + format.format(fechaInicial) + "'\n"
                + "ORDER BY M.fecha DESC";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    traspasos.add(construirTraspaso(rs));
                }
            }
        }
        return traspasos;
    }

    private TOProductoAlmacen construirProductoAlmacen(ResultSet rs) throws SQLException {
        TOProductoAlmacen toProd = new TOProductoAlmacen();
        toProd.setIdMovtoAlmacen(rs.getInt("idMovtoalmacen"));
        toProd.setIdProducto(rs.getInt("idEmpaque"));
        toProd.setLote(rs.getString("lote"));
        toProd.setCantidad(rs.getDouble("cantidad"));
        toProd.setFechaCaducidad(new java.util.Date(rs.getDate("fechaCaducidad").getTime()));
        return toProd;
    }

    private ArrayList<TOProductoAlmacen> obtenLotes(Connection cn, int idMovtoAlmacen, int idProducto) throws SQLException {
        ArrayList<TOProductoAlmacen> lotes = new ArrayList<>();
        String strSQL = "SELECT DISTINCT D.idMovtoAlmacen, D.idEmpaque, D.lote, D.cantidad, DATEADD(DAY, 365, L.fecha) AS fechaCaducidad\n"
                + "FROM movimientosDetalleAlmacen D\n"
                + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                + "INNER JOIN lotes L ON SUBSTRING(L.lote, 1, 4)=SUBSTRING(D.lote, 1, 4)\n"
                + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen + " AND D.idEmpaque=" + idProducto + "\n"
                + "ORDER BY fechaCaducidad";
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                lotes.add(this.construirProductoAlmacen(rs));
            }
        }
        return lotes;
    }

    public ArrayList<TOProductoAlmacen> obtenerLotes(int idMovtoAlmacen, int idProducto) throws SQLException {
        ArrayList<TOProductoAlmacen> lotes = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            lotes = this.obtenLotes(cn, idMovtoAlmacen, idProducto);
        }
        return lotes;
    }

    private Lote1 construirLote(ResultSet rs) throws SQLException {
        Lote1 lote = new Lote1();
        lote.setIdAlmacen(rs.getInt("idAlmacen"));
        lote.setIdProducto(rs.getInt("idEmpaque"));
        lote.setLote(rs.getString("lote"));
        lote.setSaldo(rs.getDouble("saldo"));
        lote.setCantidad(rs.getDouble("cantidad"));
        lote.setSeparados(rs.getDouble("cantidad"));
        lote.setFechaCaducidad(new java.util.Date(rs.getDate("fechaCaducidad").getTime()));
        return lote;
    }

    public ArrayList<Lote1> obtenerLotes(int idAlmacen, int idMovtoAlmacen, int idProducto) throws SQLException {
        ArrayList<Lote1> lotes = new ArrayList<>();
        String strSQL = "SELECT L.idAlmacen, L.idEmpaque, L.lote, L.fechaCaducidad, L.saldo-L.separados AS saldo\n"
                + "	, ISNULL(D.cantidad, 0) AS cantidad\n"
                + "FROM (SELECT M.idAlmacen, D.idEmpaque, D.lote, D.cantidad\n"
                + "		FROM movimientosDetalleAlmacen D\n"
                + "		inner join movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                + "		WHERE D.idMovtoAlmacen=" + idMovtoAlmacen + ") D\n"
                + "RIGHT JOIN almacenesLotes L ON L.idAlmacen=D.idAlmacen AND L.idEmpaque=D.idEmpaque AND L.lote=D.lote\n"
                + "WHERE L.idAlmacen=" + idAlmacen + " AND L.idEmpaque=" + idProducto + " AND (L.saldo-L.separados > 0 OR ISNULL(D.cantidad, 0) > 0)";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    lotes.add(this.construirLote(rs));
                }
            }
        }
        return lotes;
    }

    // -------------------------------------- TRASPASOS --------------------------------
    private TORecepcionProducto construirRecepcionProducto(ResultSet rs) throws SQLException {
        TORecepcionProducto to = new TORecepcionProducto();
        to.setCantSolicitada(rs.getInt("cantSolicitada"));
        to.setCantEnviada(rs.getDouble("cantEnviada"));
        this.construirProducto(rs, to);
        return to;
    }

    public ArrayList<TORecepcionProducto> obtenerRecepcionDetalle(int idMovto) throws SQLException {
        ArrayList<TORecepcionProducto> productos = new ArrayList<>();
        String strSQL = "SELECT D.*, TD.cantFacturada AS cantEnviada, S.cantSolicitada\n"
                + "FROM movimientosDetalle D\n"
                + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                + "INNER JOIN movimientosDetalle TD ON TD.idMovto=M.referencia AND TD.idEmpaque=D.idEmpaque\n"
                + "INNER JOIN movimientos T ON T.idMovto=TD.idMovto\n"
                + "INNER JOIN solicitudesDetalle S ON S.idSolicitud=T.referencia AND S.idEmpaque=D.idEmpaque\n"
                + "WHERE D.idMovto=" + idMovto;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    productos.add(this.construirRecepcionProducto(rs));
                }
            }
        }
        return productos;
    }

    private TOTraspasoProducto construirTraspasoProducto(ResultSet rs) throws SQLException {
        TOTraspasoProducto to = new TOTraspasoProducto();
        to.setIdSolicitud(rs.getInt("idSolicitud"));
        to.setCantSolicitada(rs.getInt("cantSolicitada"));
        this.construirProducto(rs, to);
        to.setIdProducto(rs.getInt("solicitudIdEmpaque"));
        return to;
    }

    public ArrayList<TOTraspasoProducto> obtenerTraspasoDetalle(int idSolicitud) throws SQLException {
        ArrayList<TOTraspasoProducto> productos = new ArrayList<>();
        String strSQL = "SELECT SD.idSolicitud, SD.idEmpaque AS solicitudIdEmpaque, SD.cantSolicitada, MD.*\n"
                + "FROM movimientosDetalle MD\n"
                + "INNER JOIN movimientos M ON M.idMovto=MD.idMovto\n"
                + "RIGHT JOIN solicitudesDetalle SD ON SD.idSolicitud=M.referencia AND SD.idEmpaque=MD.idEmpaque\n"
                + "WHERE SD.idSolicitud=" + idSolicitud;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    productos.add(this.construirTraspasoProducto(rs));
                }
            }
        }
        return productos;
    }

    // -------------------------------------- VENTAS -----------------------------------
    public void tranferirSinCargo(int idAlmacen, int idMovto, int idMovtoAlmacen, TOProductoPedido toOrigen, TOProductoPedido toDestino, double cantidad, int idZonaImpuestos) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
//                this.separaRelacionados(idAlmacen, idMovtoAlmacen, toDestino.getIdProducto(), cantidad, true);
                if (toDestino.getIdMovto() == 0) {
                    strSQL = "SELECT idEmpresa, idReferencia AS idTienda FROM movimientos WHERE idMovto=" + idMovto;
                    ResultSet rs = st.executeQuery(strSQL);
                    if (rs.next()) {
//                        this.agregarProductoOficina(rs.getInt("idEmpresa"), toDestino, idZonaImpuestos, rs.getInt("idTienda"));
                        toDestino.setIdMovto(idMovto);
                    }
                }
                strSQL = "UPDATE movimientosDetalle\n"
                        + "SET cantSinCargo=cantSinCargo+" + cantidad + "\n"
                        + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + toDestino.getIdProducto();
                st.executeUpdate(strSQL);

//                this.liberaRelacionados(idAlmacen, idMovtoAlmacen, toOrigen.getIdProducto(), cantidad);
                strSQL = "UPDATE movimientosDetalle\n"
                        + "SET cantSinCargo=cantSinCargo-" + cantidad + "\n"
                        + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + toOrigen.getIdProducto();
                st.executeUpdate(strSQL);

                toDestino.setCantSinCargo(toDestino.getCantSinCargo() + cantidad);
                toOrigen.setCantSinCargo(toOrigen.getCantSinCargo() - cantidad);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw (ex);
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    // -------------------------------------- PEDIDOS ----------------------------------
    private void liberaRelacionados(Connection cn, TOMovimientoOficina toMov, TOProductoOficina toProd, double solicitados) throws SQLException {
        String lote;
        double separadosOficina = 0, cantAlmacen = 0, liberados = 0, liberar;
        String strSQL = "SELECT D.idMovto, DA.idMovtoAlmacen, M.idAlmacen, L.lote\n"
                + "     , D.cantFacturada+D.cantSinCargo AS cantOficina, E.separados AS separadosOficina\n"
                + "	, DA.cantidad AS cantAlmacen, L.separados AS separadosAlmacen\n"
                + "FROM movimientosDetalle D\n"
                + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                + "INNER JOIN almacenesEmpaques E ON E.idAlmacen=M.idAlmacen AND E.idEmpaque=D.idEmpaque\n"
                + "INNER JOIN movimientosRelacionados R ON R.idMovto=M.idMovto\n"
                + "INNER JOIN movimientosDetalleAlmacen DA ON DA.idMovtoAlmacen=R.idMovtoAlmacen AND DA.idEmpaque=D.idEmpaque\n"
                + "INNER JOIN almacenesLotes L ON L.idAlmacen=M.idAlmacen AND L.idEmpaque=D.idEmpaque AND L.lote=DA.lote\n"
                + "WHERE D.idMovto=" + toMov.getIdMovto() + " AND D.idEmpaque=" + toProd.getIdProducto() + "\n"
                + "ORDER BY L.fechaCaducidad DESC";
        try (Statement st = cn.createStatement(); Statement st1 = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                lote = rs.getString("lote");
                separadosOficina = rs.getDouble("separadosOficina");
                if (rs.getDouble("separadosAlmacen") < rs.getDouble("cantAlmacen")) {
                    throw new SQLException("Descuadre de separados lote='" + lote + "', producto (id=" + toProd.getIdProducto() + ")");
                } else {
                    cantAlmacen = rs.getDouble("cantAlmacen");

                    liberar = solicitados - liberados;
                    if (liberar < cantAlmacen) {
                        strSQL = "UPDATE movimientosDetalleAlmacen SET cantidad=cantidad-" + liberar + "\n"
                                + "WHERE idMovtoAlmacen=" + toMov.getIdMovtoAlmacen() + " AND idEmpaque=" + toProd.getIdProducto() + " AND lote='" + lote + "'";
                    } else {
                        liberar = cantAlmacen;
                        strSQL = "DELETE FROM movimientosDetalleAlmacen\n"
                                + "WHERE idMovtoAlmacen=" + toMov.getIdMovtoAlmacen() + " AND idEmpaque=" + toProd.getIdProducto() + " AND lote='" + lote + "'";
                    }
                    st1.executeUpdate(strSQL);

                    strSQL = "UPDATE almacenesLotes SET separados=separados-" + liberar + "\n"
                            + "WHERE idAlmacen=" + toMov.getIdAlmacen() + " AND idEmpaque=" + toProd.getIdProducto() + " AND lote='" + lote + "'";
                    st1.executeUpdate(strSQL);
                }
                liberados += liberar;
                if (liberados == solicitados) {
                    break;
                }
            }
            if (solicitados > separadosOficina) {
                throw new SQLException("Descuadre de separados producto (id=" + toProd.getIdProducto() + ")");
            } else {
                strSQL = "UPDATE almacenesEmpaques SET separados=separados-" + solicitados + "\n"
                        + "WHERE idAlmacen=" + toMov.getIdAlmacen() + " AND idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosDetalle\n"
                        + "SET cantFacturada=" + toProd.getCantFacturada() + ", cantSinCargo=" + toProd.getCantSinCargo() + "\n"
                        + "WHERE idMovto=" + toMov.getIdMovto() + " AND idEmpaque=" + toProd.getIdProducto();
                st.executeUpdate(strSQL);
            }
        }
    }

//    private void libera1(Connection cn, int idAlmacen, int idProducto, double solicitados) throws SQLException {
//        String strSQL = "SELECT AE.separados "
//                + "FROM almacenesEmpaques AE "
//                + "WHERE AE.idAlmacen=" + idAlmacen + " AND AE.idEmpaque=" + idProducto;
//        try (Statement st = cn.createStatement()) {
//            ResultSet rs = st.executeQuery(strSQL);
//            if (rs.next()) {
//                if (rs.getDouble("separados") < solicitados) {
//                    throw (new SQLException("Descuadre de existencia del producto (id=" + idProducto + ") !!!"));
//                }
//            } else {
//                throw (new SQLException("No se encontro el producto (" + idProducto + ") !!!"));
//            }
//            strSQL = "UPDATE almacenesEmpaques SET separados=separados-" + solicitados + " "
//                    + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto;
//            st.executeUpdate(strSQL);
//        }
//    }
//    private void liberaLotes(Connection cn, int idAlmacen, int idMovtoAlmacen, int idProducto, double solicitados) throws SQLException {
//        double liberar;
//        String strSQL = "SELECT K.lote, K.cantidad, L.separados\n"
//                + "FROM movimientosDetalleAlmacen K\n"
//                + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=K.idMovtoAlmacen\n"
//                + "INNER JOIN almacenesLotes L ON L.idAlmacen=M.idAlmacen AND L.idEmpaque=K.idEmpaque AND L.lote=K.lote\n"
//                + "WHERE K.idMovtoAlmacen=" + idMovtoAlmacen + " AND K.idEmpaque=" + idProducto + "\n"
//                + "ORDER BY L.fechaCaducidad DESC";
//        try (Statement st = cn.createStatement(); Statement st1 = cn.createStatement()) {
//            ResultSet rs = st1.executeQuery(strSQL);
//            while (rs.next()) {
//                liberar = rs.getDouble("cantidad");
//                if (solicitados < liberar) {
//                    liberar = solicitados;
//                    strSQL = "UPDATE movimientosDetalleAlmacen SET cantidad=cantidad-" + liberar + " "
//                            + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + rs.getString("lote") + "'";
//                } else {
//                    strSQL = "DELETE FROM movimientosDetalleAlmacen "
//                            + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + "AND idEmpaque=" + idProducto + " AND lote='" + rs.getString("lote") + "'";
//                }
//                st.executeUpdate(strSQL);
//
//                if (rs.getDouble("separados") < liberar) {
//                    throw (new SQLException("Descuedre de lote (" + rs.getString("lote") + ") del producto (id=" + idProducto + ") en almacen !!!"));
//                } else {
//                    strSQL = "UPDATE almacenesLotes SET separados=separados-" + liberar + " "
//                            + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + rs.getString("lote") + "'";
//                    st.executeUpdate(strSQL);
//                }
//                solicitados -= liberar;
//                if (solicitados == 0) {
//                    break;
//                }
//            }
//        }
//    }
    private void liberaMovimientoSeparados(Connection cn, int idMovto, int idMovtoAlmacen) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement(); Statement st1 = cn.createStatement()) {
            strSQL = "SELECT M.idAlmacen, D.idEmpaque, D.cantFacturada+D.cantSinCargo AS liberar\n"
                    + "FROM movimientosDetalle D\n"
                    + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                    + "WHERE D.idMovto=" + idMovto + " AND D.cantFacturada+D.cantSinCargo > 0";
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                strSQL = "UPDATE almacenesEmpaques\n"
                        + "SET separados=separados-" + rs.getDouble("liberar") + "\n"
                        + "WHERE idAlmacen=" + rs.getInt("idAlmacen") + " AND idEmpaque=" + rs.getInt("idEmpaque");
                st1.executeUpdate(strSQL);
            }
            strSQL = "UPDATE movimientosDetalle\n"
                    + "SET cantFacturada=0, cantSinCargo=0\n"
                    + "WHERE idMovto=" + idMovto;
            st.executeUpdate(strSQL);

            strSQL = "SELECT M.idAlmacen, D.idEmpaque, D.lote, D.cantidad AS liberar\n"
                    + "FROM movimientosDetalleAlmacen D\n"
                    + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                    + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen + " AND D.cantidad > 0";
            rs = st.executeQuery(strSQL);
            while (rs.next()) {
                strSQL = "UPDATE almacenesLotes\n"
                        + "SET separados=separados-" + rs.getDouble("liberar") + "\n"
                        + "WHERE idAlmacen=" + rs.getInt("idAlmacen") + " AND idEmpaque=" + rs.getDouble("idEmpaque") + " AND lote='" + rs.getString("lote") + "'";
                st1.executeUpdate(strSQL);
            }
            strSQL = "UPDATE movimientosDetalleAlmacen\n"
                    + "SET cantidad=0\n"
                    + "WHERE idMovtoAlmacen=" + idMovtoAlmacen;
            st.executeUpdate(strSQL);
        }
    }

    public void cancelarPedido(TOPedido toPed) throws SQLException {
        String strSQL = "";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                this.liberaMovimientoSeparados(cn, toPed.getIdMovto(), toPed.getIdMovtoAlmacen());

                strSQL = "UPDATE pedidos\n"
                        + "SET estatus=3, canceladoMotivo='" + toPed.getCanceladoMotivo() + "', canceladoFecha=GETDATE()\n"
                        + "WHERE idPedido=" + toPed.getReferencia();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientos\n"
                        + "SET estatus=3\n"
                        + "WHERE idMovto=" + toPed.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosAlmacen\n"
                        + "SET estatus=3\n"
                        + "WHERE idMovtoAlmacen=" + toPed.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw (e);
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void eliminarPedido(TOPedido toPed) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "DELETE FROM pedidos WHERE idPedido=" + toPed.getReferencia();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM pedidosDetalle WHERE idPedido=" + toPed.getReferencia();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientos WHERE idMovto=" + toPed.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalle WHERE idMovto=" + toPed.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalleImpuestos WHERE idMovto=" + toPed.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosRelacionados WHERE idMovto=" + toPed.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosAlmacen WHERE idMovtoAlmacen=" + toPed.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalleAlmacen WHERE idMovtoAlmacen=" + toPed.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw (e);
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private void agregaMovimientoAlmacen(Connection cn, TOMovimientoAlmacen to, boolean definitivo) throws SQLException {
        to.setEstatus(0);
        to.setIdUsuario(this.idUsuario);
        to.setPropietario(this.idUsuario);
        try (Statement st = cn.createStatement()) {
            if (definitivo) {
                to.setFolio(this.obtenerMovimientoFolio(cn, false, to.getIdAlmacen(), to.getIdTipo()));
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

    public void agregarMovimientoAlmacen(TOMovimientoAlmacen to, boolean definitivo) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                this.agregaMovimientoAlmacen(cn, to, definitivo);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private void agregaMovimientoRelacionado(Connection cn, TOMovimientoOficina toOfi, boolean definitivo) throws SQLException {
        toOfi.setEstatus(0);
        toOfi.setIdUsuario(this.idUsuario);
        toOfi.setPropietario(this.idUsuario);
        
        TOMovimientoAlmacen toAlm = new TOMovimientoAlmacen(toOfi.getIdTipo());
        toAlm.setIdEmpresa(toOfi.getIdEmpresa());
        toAlm.setIdAlmacen(toOfi.getIdAlmacen());
        toAlm.setIdComprobante(toOfi.getIdComprobante());
        toAlm.setIdReferencia(toOfi.getIdReferencia());
        toAlm.setReferencia(toOfi.getReferencia());
        toAlm.setIdUsuario(toOfi.getIdUsuario());
        toAlm.setPropietario(toOfi.getPropietario());
        toAlm.setEstatus(toOfi.getEstatus());
        this.agregaMovimientoAlmacen(cn, toAlm, definitivo);
        
        toOfi.setIdMovtoAlmacen(toAlm.getIdMovtoAlmacen());
        movimientos.Movimientos.agregaMovimientoOficina(cn, toOfi, definitivo);
    }

    public void agregarMovimientoRelacionado(TOMovimientoOficina to, boolean definitivo) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                this.agregaMovimientoRelacionado(cn, to, definitivo);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void cerrarPedido(TOPedido toPed) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "DELETE FROM pedidosDetalle\n"
                        + "WHERE idPedido=" + toPed.getReferencia() + " AND cantOrdenada+cantOrdenadaSinCargo=0";
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientos\n"
                        + "SET desctoComercial=" + toPed.getDesctoComercial() + ", propietario=0, estatus=1\n"
                        + "WHERE idMovto=" + toPed.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE MD\n"
                        + "FROM movimientosDetalle MD\n"
                        + "INNER JOIN movimientos M ON M.idMovto=MD.idMovto\n"
                        + "LEFT JOIN pedidosDetalle PD ON PD.idPedido=M.referencia AND PD.idEmpaque=MD.idEmpaque\n"
                        + "WHERE MD.idMovto=" + toPed.getIdMovto() + " AND PD.idPedido IS NULL";
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientosAlmacen\n"
                        + "SET propietario=0, estatus=1\n"
                        + "WHERE idMovtoAlmacen=" + toPed.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public TOProductoPedido obtenerProductoPedido(int idPedido, int idProducto) throws SQLException {
        TOProductoPedido to = null;
        String strSQL = "SELECT * FROM pedidosOCTiendaDetalle WHERE idPedido=" + idPedido + " AND idEmpaque=" + idProducto;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    to = this.construirProductoPedido(rs);
                }
            }
        }
        return to;
    }

    public ArrayList<TOProductoPedido> obtenerSimilaresPedido(int idPedido, int idProducto) throws SQLException {
        ArrayList<TOProductoPedido> productos = new ArrayList<>();
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
                    productos.add(this.construirProductoPedido(rs));
                }
            }
        }
        return productos;
    }

    private ArrayList<Double> obtenerBoletinSinCargo(Connection cn, int idEmpresa, int idTienda, int idProducto) throws SQLException {
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

    public void trasferirSinCargo(int idPedido, int idProdOrigen, TOProductoPedido to, int idImpuestoZona, double cantidad) throws SQLException {
        String strSQL = "";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                if (to.getIdPedido() == 0) {
                    strSQL = "SELECT idEmpresa, idTienda FROM pedidosOC WHERE idPedido=" + idPedido;
                    ResultSet rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        TOPedido toPed = new TOPedido();
                        int idEmpresa = rs.getInt("idEmpresa");
                        toPed.setIdReferencia(rs.getInt("idTienda"));

                        to.setIdPedido(idPedido);
                        to.setCantSinCargo(cantidad);

                        this.agregarProductoPedido(idEmpresa, toPed, idImpuestoZona, to);
                    }
                } else {
                    to.setCantSinCargo(to.getCantSinCargo() + cantidad);

                    strSQL = "UPDATE pedidosOCTiendaDetalle\n"
                            + "SET cantSinCargo=cantSinCargo+" + cantidad + "\n"
                            + "WHERE idPedido=" + idPedido + " AND idEmpaque=" + to.getIdProducto();
                    st.executeUpdate(strSQL);
                }
                strSQL = "UPDATE pedidosOCTiendaDetalle SET cantSinCargo=cantSinCargo-" + cantidad + "\n"
                        + "WHERE idPedido=" + idPedido + " AND idEmpaque=" + idProdOrigen;
                st.executeUpdate(strSQL);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.close();
            }
        }
    }

    public ArrayList<TOProductoPedido> obtenerPedidoSimilares(int idPedido, int idProducto) throws SQLException {
        ArrayList<TOProductoPedido> productos = new ArrayList<>();
        String strSQL = "SELECT D.*\n"
                + "FROM empaquesSimilares S\n"
                + "INNER JOIN pedidosOCTiendaDetalle D ON D.idEmpaque=S.idEmpaque\n"
                + "WHERE D.idPedido=" + idPedido + " AND S.idSimilar=" + idProducto;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    productos.add(this.construirProductoPedido(rs));
                }
            }
        }
        return productos;
    }

    public void grabarPedidoDetalle(int idEmpresa, TOPedido toPed, TOProductoPedido toProd) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                this.actualizaProductoCantidadPedido(cn, idEmpresa, toPed.getIdReferencia(), toPed.getReferencia(), toProd);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public boolean grabarPedidoDetalle1(int idEmpresa, TOPedido toPed, TOProductoPedido prod, double cantFacturadaOld) throws SQLException {
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
                    strSQL = "UPDATE pedidosDetalle "
                            + "SET cantFacturada=cantFacturada+" + cantSeparada + " "
                            + "WHERE idPedido=" + toPed.getReferencia() + " AND idEmpaque=" + idProducto;
                } else {
                    cantLiberar = cantFacturadaOld - prod.getCantFacturada();

                    cantLiberada = cantLiberar;
                    strSQL = "UPDATE pedidosDetalle "
                            + "SET cantFacturada=cantFacturada-" + cantLiberada + " "
                            + "WHERE idPedido=" + toPed.getReferencia() + " AND idEmpaque=" + idProducto;
                }
                st.executeUpdate(strSQL);

                ArrayList<Double> boletin = this.obtenerBoletinSinCargo(cn, idEmpresa, toPed.getIdReferencia(), prod.getIdProducto());
                boletinConCargo = boletin.get(0);
                boletinSinCargo = boletin.get(1);
                if (boletinConCargo > 0) {
                    strSQL = "SELECT ISNULL(SUM(D.cantFacturada),0) AS cantFacturada, ISNULL(SUM(D.cantSinCargo),0) AS cantSinCargo\n"
                            + "FROM pedidosDetalle D\n"
                            + "INNER JOIN empaquesSimilares S ON S.idEmpaque=D.idEmpaque\n"
                            + "WHERE D.idPedido=" + toPed.getReferencia() + " AND S.idSimilar=" + prod.getIdProducto();
                    rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        similares = true;
                        cantSinCargo = ((int) (rs.getDouble("cantFacturada") / boletinConCargo)) * boletinSinCargo;
                        double cantSinCargoHay = rs.getDouble("cantSinCargo");
                        if (cantSinCargo > cantSinCargoHay) {
                            cantSolicitada = cantSinCargo - cantSinCargoHay;

                            cantSeparada = cantSolicitada;
                            strSQL = "UPDATE pedidosDetalle "
                                    + "SET cantSinCargo=cantSinCargo+" + cantSeparada + " "
                                    + "WHERE idPedido=" + toPed.getReferencia() + " AND idEmpaque=" + idProducto;
                            st.executeUpdate(strSQL);
                        } else if (cantSinCargo < cantSinCargoHay) {
                            double disponibles;
                            cantLiberar = cantSinCargoHay - cantSinCargo;

                            strSQL = "SELECT P.principal, P.idEmpaque, P.cantFacturada, P.cantSinCargo, P.unitario, P.idImpuesto\n"
                                    + "FROM (SELECT CASE WHEN D.idEmpaque=S.idSimilar THEN 1 ELSE 0 END AS principal\n"
                                    + "           , ISNULL(D.idEmpaque,S.idEmpaque) AS idEmpaque\n"
                                    + "           , ISNULL(D.cantFacturada, 0) AS cantFacturada, ISNULL(D.cantSinCargo, 0) AS cantSinCargo\n"
                                    + "           , ISNULL(D.unitario, 0) AS unitario, P.idImpuesto\n"
                                    + "       FROM (SELECT * FROM pedidosDetalle WHERE idPedido=" + toPed.getReferencia() + ") D\n"
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
                                    strSQL = "UPDATE pedidosDetalle "
                                            + "SET cantSinCargo=cantSinCargo-" + cantLiberada + " "
                                            + "WHERE idPedido=" + toPed.getReferencia() + " AND idEmpaque=" + rs.getInt("idEmpaque");
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
                            strSQL = "UPDATE pedidosDetalle "
                                    + "SET cantSinCargo=cantSinCargo+" + cantSeparada + " "
                                    + "WHERE idPedido=" + toPed.getReferencia() + " AND idEmpaque=" + idProducto;
                            st.executeUpdate(strSQL);

                            prod.setCantSinCargo(prod.getCantSinCargo() + cantSeparada);
                        } else if (prod.getCantSinCargo() < cantSinCargo) {
                            cantLiberar = prod.getCantSinCargo() - cantSinCargo;

                            cantLiberada = cantLiberar;
                            strSQL = "UPDATE pedidosDetalle "
                                    + "SET cantSinCargo=cantSinCargo-" + cantLiberada + " "
                                    + "WHERE idPedido=" + toPed.getReferencia() + " AND idEmpaque=" + rs.getInt("idEmpaque");
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

    public boolean liberarPedido(int idMovto) throws SQLException, Exception {
        boolean liberado = true;
        String strSQL = "SELECT propietario FROM movimientos WHERE idMovto=" + idMovto;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    int propietario = rs.getInt("propietario");
                    if (propietario == this.idUsuario) {
                        strSQL = "UPDATE movimientos SET propietario=0 WHERE idMovto=" + idMovto;
                        st.executeUpdate(strSQL);
                    }
//                } else {
//                    throw new Exception("No se encontro el pedido !!!");
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } catch (Exception ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
            return liberado;
        }
    }

    public void construirProducto(ResultSet rs, TOProductoOficina to) throws SQLException {
        to.setIdMovto(rs.getInt("idMovto"));
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
    }

    public TOProductoPedido construirProductoPedido(ResultSet rs) throws SQLException {
        TOProductoPedido to = new TOProductoPedido();
        to.setIdPedido(rs.getInt("idPedido"));
        to.setCantOrdenada(rs.getDouble("cantOrdenada"));
        to.setCantOrdenadaSinCargo(rs.getDouble("cantOrdenadaSinCargo"));
        this.construirProducto(rs, to);
        return to;
    }

    public ArrayList<TOProductoPedido> obtenerPedidoDetalle(int idMovto) throws SQLException {
        ArrayList<TOProductoPedido> productos = new ArrayList<>();
        String strSQL = "SELECT MD.*\n"
                + "         , ISNULL(PD.idPedido, 0) AS idPedido, ISNULL(PD.cantOrdenada, 0) AS cantOrdenada"
                + "         , ISNULL(PD.cantOrdenadaSinCargo, 0) AS cantOrdenadaSinCargo\n"
                + "FROM movimientosDetalle MD\n"
                + "INNER JOIN movimientos M ON M.idMovto=MD.idMovto\n"
                + "LEFT JOIN pedidosDetalle PD ON PD.idPedido=M.referencia AND PD.idEmpaque=MD.idEmpaque\n"
                + "WHERE MD.idMovto=" + idMovto;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    productos.add(this.construirProductoPedido(rs));
                }
            }
        }
        return productos;
    }

    private ArrayList<Double> obtenerPrecioUnitario(Connection cn, int idEmpresa, int idTienda, double desctoComercial, int idProducto) throws SQLException {
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
                        throw (new SQLException("El producto id=" + idProducto + ", No tiene precio de lista vigente !!!"));
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
                        precioLista = (precioLista / (1 - desctoComercial / 100.00));

                        precio.add(precioUnitario);
                        precio.add(desctoProducto1);
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

    public void agregarProductoPedido(int idEmpresa, TOPedido toPed, int idImpuestoZona, TOProductoPedido to) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {

//                this.agregaProductoOficina(cn, to, idImpuestoZona);
                movimientos.Movimientos.agregaProductoOficina(cn, to, idImpuestoZona);

                this.actualizaProductoPrecio(cn, idEmpresa, toPed, to);

                strSQL = "INSERT INTO pedidosDetalle (idPedido, idEmpaque, cantOrdenada, cantOrdenadaSinCargo)\n"
                        + "VALUES (" + to.getIdPedido() + ", " + to.getIdProducto() + ", " + to.getCantOrdenada() + ", " + to.getCantOrdenadaSinCargo() + ")";
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

    
    private ArrayList<Double> actualizaProductoCantidad(Connection cn, int idEmpresa, int idTienda, TOProductoPedido toProd) throws SQLException {
        ArrayList<Double> boletin = this.obtenerBoletinSinCargo(cn, idEmpresa, idTienda, toProd.getIdProducto());
        if (boletin.get(0) > 0) {
            toProd.setCantSinCargo((int) (toProd.getCantFacturada() / boletin.get(0)) * boletin.get(1));
        } else {
            toProd.setCantSinCargo(0);
        }
//        ArrayList<Double> boletin = actualizaProductoCantidad(cn, idEmpresa, idTienda, toProd);
        try (Statement st = cn.createStatement()) {
            String strSQL = "UPDATE movimientosDetalle\n"
                    + "SET cantFacturada=" + toProd.getCantFacturada() + ", cantSinCargo=" + toProd.getCantSinCargo() + "\n"
                    + "WHERE idMovto=" + toProd.getIdMovto() + " AND idEmpaque=" + toProd.getIdProducto();
            st.executeUpdate(strSQL);
        }
        return boletin;
    }

    private void actualizaProductoCantidadPedido(Connection cn, int idEmpresa, int idTienda, int idPedido, TOProductoPedido toProd) throws SQLException {
        ArrayList<Double> boletin = this.obtenerBoletinSinCargo(cn, idEmpresa, idTienda, toProd.getIdProducto());
        if (boletin.get(0) > 0) {
            toProd.setCantOrdenadaSinCargo((int) (toProd.getCantOrdenada() / boletin.get(0)) * boletin.get(1));
        } else {
            toProd.setCantOrdenadaSinCargo(0);
        }
//        actualizaProductoCantidad(cn, idEmpresa, idTienda, toProd);
        try (Statement st = cn.createStatement()) {
            String strSQL = "UPDATE pedidosDetalle\n"
                    + "SET cantOrdenada=" + toProd.getCantOrdenada() + ", cantOrdenadaSinCargo=" + toProd.getCantOrdenadaSinCargo() + "\n"
                    + "WHERE idPedido=" + idPedido + " AND idEmpaque=" + toProd.getIdProducto();
            st.executeUpdate(strSQL);
        }
    }

//    private ArrayList<Double> actualizaProductoCantidad(Connection cn, int idEmpresa, int idTienda, TOProductoPedido toProd) throws SQLException {
//        ArrayList<Double> boletin = this.obtenerBoletinSinCargo(cn, idEmpresa, idTienda, toProd.getIdProducto());
//        double boletinConCargo = boletin.get(0);
//        double boletinSinCargo = boletin.get(1);
//        if (boletinConCargo > 0) {
//            if (toProd.getIdPedido() != 0) {
//                toProd.setCantOrdenadaSinCargo((int) (toProd.getCantOrdenada() / boletinConCargo) * boletinSinCargo);
//            } else {
//                toProd.setCantSinCargo((int) (toProd.getCantFacturada() / boletinConCargo) * boletinSinCargo);
//            }
//        } else if (toProd.getIdPedido() != 0) {
//            toProd.setCantOrdenadaSinCargo(0);
//        } else {
//            toProd.setCantSinCargo(0);
//        }
//        return boletin;
//    }
    private void actualizaProductoPrecio(Connection cn, int idEmpresa, TOMovimientoOficina toMov, TOProductoOficina toProd) throws SQLException {
        ArrayList<Double> precio = this.obtenerPrecioUnitario(cn, idEmpresa, toMov.getIdReferencia(), toMov.getDesctoComercial(), toProd.getIdProducto());
        toProd.setUnitario(precio.get(0));
        toProd.setDesctoProducto1(precio.get(1));
        toProd.setCosto(precio.get(2));

//        try (Statement st = cn.createStatement()) {
//            String strSQL = "UPDATE movimientosDetalle\n"
//                    + "SET unitario=" + toProd.getUnitario() + ", desctoProducto1=" + toProd.getDesctoProducto1() + ", costo=" + toProd.getCosto() + "\n"
//                    + "WHERE idMovto=" + toProd.getIdMovto() + " AND idEmpaque=" + toProd.getIdProducto();
//            st.executeUpdate(strSQL);
//        }
        movimientos.Movimientos.grabaProductoCambios(cn, toProd);
        movimientos.Movimientos.calculaUnitario(cn, toMov.getIdMovto(), toProd.getIdProducto());
//        movimientos.Movimientos.calculaImpuestosProducto(cn, toProd.getIdMovto(), toProd.getIdProducto());
    }

    private void actualizaProductoPedido(Connection cn, int idEmpresa, TOMovimientoOficina toMov, TOProductoPedido to) throws SQLException {
        this.actualizaProductoPrecio(cn, idEmpresa, toMov, to);
        this.actualizaProductoCantidadPedido(cn, idEmpresa, toMov.getIdReferencia(), to.getIdPedido(), to);
    }

    public void actualizarPedido(int idEmpresa, TOMovimientoOficina toMov, ArrayList<TOProductoPedido> tos) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                for (TOProductoPedido toProd : tos) {
                    this.actualizaProductoPedido(cn, idEmpresa, toMov, toProd);
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

    public boolean asegurarPedido(int idMovto) throws SQLException, Exception {
        boolean asegurado = true;
        String strSQL = "SELECT propietario FROM movimientos WHERE idMovto=" + idMovto;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    int propietario = rs.getInt("propietario");
                    if (propietario == 0) {
                        strSQL = "UPDATE movimientos SET propietario=" + this.idUsuario + " WHERE idMovto=" + idMovto;
                        st.executeUpdate(strSQL);
                    } else if (propietario != this.idUsuario) {
                        asegurado = false;
                        strSQL = "SELECT * FROM webSystem.dbo.usuarios WHERE idUsuario=" + propietario;
                        rs = st.executeQuery(strSQL);
                        if (rs.next()) {
                            strSQL = rs.getString("usuario");
                        } else {
                            strSQL = "";
                        }
                        throw new Exception("No se puede asegurar el movimiento, lo tiene el usuario(id=" + propietario + "): " + strSQL + " !!!");
                    }
                } else {
                    throw new Exception("No se encontro el pedido !!!");
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } catch (Exception ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return asegurado;
    }

    public void agregarPedido(TOPedido toPed) throws SQLException {
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery("SELECT GETDATE() AS fecha");
                rs.next();
                toPed.setFecha(new Date(rs.getDate("fecha").getTime()));

                strSQL = "INSERT INTO pedidosOC (fecha, ordenDeCompra, ordenDeCompraFecha, embarqueFecha, entregaFolio, entregaFecha)\n"
                        + "VALUES (GETDATE(), '" + toPed.getOrdenDeCompra() + "', '1900-01-01', '1900-01-01', '', '1900-01-01')";
                st.executeUpdate(strSQL);

                rs = st.executeQuery("SELECT @@IDENTITY AS idPedidoOC");
                if (rs.next()) {
                    toPed.setIdPedidoOC(rs.getInt("idPedidoOC"));
                }
                strSQL = "INSERT INTO pedidos (idPedidoOC, canceladoMotivo, canceladoFecha)\n"
                        + "VALUES (" + toPed.getIdPedidoOC() + ", '', '1900-01-01')";
                st.executeUpdate(strSQL);
                rs = st.executeQuery("SELECT @@IDENTITY AS idPedido");
                if (rs.next()) {
                    toPed.setReferencia(rs.getInt("idPedido"));
                }
//                TOMovimiento toMov = new TOMovimiento();
//                toMov.setIdTipo(28);
//                toMov.setIdCedis(this.idCedis);
//                toMov.setIdEmpresa(toPed.getIdEmpresa());
//                toMov.setIdAlmacen(toPed.getIdAlmacen());
//                toMov.setDesctoComercial(toPed.getDesctoComercial());
//                toMov.setDesctoProntoPago(toPed.getDesctoProntoPago());
//                toMov.setIdImpuestoZona(toPed.getIdImpuestoZona());
//                toMov.setIdUsuario(this.idUsuario);
//                toMov.setIdMoneda(1);
//                toMov.setTipoDeCambio(1);
//                toMov.setIdReferencia(toPed.getIdReferencia());
//                toMov.setReferencia(toPed.getReferencia());

                this.agregaMovimientoRelacionado(cn, toPed, true);
//                toPed.setIdMovto(toMov.getIdMovto());
//                toPed.setIdMovtoAlmacen(toMov.getIdMovtoAlmacen());

                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw (e);
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private TOPedido construirPedido(ResultSet rs) throws SQLException {
        TOPedido to = new TOPedido();
        to.setIdPedidoOC(rs.getInt("idPedidoOC"));
        to.setOrdenDeCompra(rs.getString("ordenDeCompra"));
        to.setOrdenDeCompraFecha(new java.util.Date(rs.getTimestamp("ordenDeCompraFecha").getTime()));
        to.setCanceladoMotivo(rs.getString("canceladoMotivo"));
        to.setCanceladoFecha(new java.util.Date(rs.getDate("canceladoFecha").getTime()));
        this.construirMovimiento(rs, to);
        return to;
    }

    public ArrayList<TOPedido> obtenerPedidos(int idAlmacen, int estatus, Date fechaInicial) throws SQLException {
        if (fechaInicial == null) {
            fechaInicial = new Date();
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TOPedido> pedidos = new ArrayList<>();
        String strSQL = "SELECT P.idPedidoOC, P.canceladoMotivo, P.canceladoFecha\n"
                + "     , ISNULL(OC.ordenDeCompra, '') AS ordenDeCompra, ISNULL(OC.ordenDeCompraFecha, '1900-01-01') AS ordenDeCompraFecha\n"
                + "     , M.*, R.idMovtoAlmacen\n"
                + "FROM movimientos M\n"
                + "INNER JOIN pedidos P ON P.idPedido=M.referencia\n"
                + "INNER JOIN movimientosRelacionados R ON R.idMovto=M.idMovto\n"
                + "LEFT JOIN pedidosOC OC ON OC.idPedidoOC=P.idPedidoOC\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=28 AND M.referencia!=0\n"
                + "         AND CONVERT(date, M.fecha) <= '" + format.format(fechaInicial) + "' AND M.estatus=" + estatus;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    pedidos.add(this.construirPedido(rs));
                }
            }
        }
        return pedidos;
    }

    // ----------------------------------------- PEDIDOS ----------------------------------------
    // ----------------------------------------- VENTAS -----------------------------------------
    public void agregarProductoOficina(TOProductoOficina to, int idImpuestoZona) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
//                this.agregaProductoOficina(cn, to, idImpuestoZona);
                movimientos.Movimientos.agregaProductoOficina(cn, to, idImpuestoZona);
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

//    private void agregarProductoOficina(Connection cn, int idMovto, TOProductoOficina to, int idZonaImpuestos) throws SQLException {
//        String strSQL;
//        try (Statement st = cn.createStatement()) {
//            strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantFacturada, cantSinCargo, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior, costoPromedio) "
//                    + "VALUES (" + idMovto + ", " + to.getIdProducto() + ", " + to.getCantFacturada() + ", " + to.getCantSinCargo() + ", " + to.getCosto() + ", " + to.getDesctoProducto1() + ", " + to.getDesctoProducto2() + ", " + to.getDesctoConfidencial() + ", " + to.getUnitario() + ", " + to.getIdImpuestoGrupo() + ", GETDATE(), 0, 0)";
//            st.executeUpdate(strSQL);
//
//            strSQL = "INSERT INTO movimientosDetalleImpuestos (idMovto, idEmpaque, idImpuesto, impuesto, valor, aplicable, modo, acreditable, importe, acumulable) "
//                    + "SELECT " + idMovto + ", " + to.getIdProducto() + ", ID.idImpuesto, I.impuesto, ID.valor, I.aplicable, I.modo, I.acreditable, 0.00 as importe, I.acumulable "
//                    + "FROM impuestosDetalle ID "
//                    + "INNER JOIN impuestos I on I.idImpuesto=ID.idImpuesto "
//                    + "WHERE ID.idGrupo=" + to.getIdImpuestoGrupo() + " AND ID.idZona=" + idZonaImpuestos + " AND GETDATE() BETWEEN ID.fechaInicial AND ID.fechaFinal";
//            if (st.executeUpdate(strSQL) == 0) {
//                throw (new SQLException("No se insertaron impuestos !!!"));
//            }
//            to.setIdMovto(idMovto);
//        }
//    }
//    private double separaRelacionados(Connection cn, int idAlmacen, int idMovtoAlmacen, int idProducto, double solicitados, boolean total) throws SQLException {
//        double cantSeparada = this.separaLotes(cn, idAlmacen, idMovtoAlmacen, idProducto, solicitados, total);
//        if (cantSeparada > 0) {
//            this.separa1(cn, idAlmacen, idProducto, cantSeparada, true);
//        }
//        return cantSeparada;
//    }
//    private double separa1(Connection cn, int idAlmacen, int idProducto, double solicitados, boolean total) throws SQLException {
//        double separados = 0;
//        String strSQL = "SELECT AE.existenciaOficina-AE.separados AS disponibles "
//                + "FROM almacenesEmpaques AE "
//                + "WHERE AE.idAlmacen=" + idAlmacen + " AND AE.idEmpaque=" + idProducto;
//        try (Statement st = cn.createStatement()) {
//            ResultSet rs = st.executeQuery(strSQL);
//            if (rs.next()) {
//                double disponibles = rs.getDouble("disponibles");
//                if (disponibles <= 0) {
//                    throw (new SQLException("No hay existencia del producto (id=" + idProducto + ") !!!"));
//                } else if (total && disponibles < solicitados) {
//                    throw (new SQLException("No hay existencia suficiente, solo hay " + disponibles + " disponibles del producto (id=" + idProducto + ") !!!"));
//                } else {
//                    separados = solicitados;
//                }
//            } else {
//                throw (new SQLException("No se encontro producto(id=" + idProducto + ") en almacen !!!"));
//            }
//            strSQL = "UPDATE almacenesEmpaques SET separados=separados+" + separados + " "
//                    + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto;
//            st.executeUpdate(strSQL);
//        }
//        return separados;
//    }
    private double separaLotes(Connection cn, int idAlmacen, int idMovtoAlmacen, int idProducto, double solicitados) throws SQLException {
        double separar;
        double separados = 0;
        String strSQL = "SELECT ISNULL(K.idMovtoAlmacen, 0) AS idMovtoAlmacen, L.lote, L.saldo, L.saldo-L.separados AS disponibles\n"
                + "FROM (SELECT D.*, M.idAlmacen\n"
                + "     FROM movimientosDetalleAlmacen D\n"
                + "     INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                + "     WHERE D.idMovtoAlmacen=" + idMovtoAlmacen + ") K\n"
                + "RIGHT JOIN almacenesLotes L ON L.idAlmacen=K.idAlmacen AND L.idEmpaque=K.idEmpaque AND L.lote=K.lote\n"
                + "WHERE L.idAlmacen=" + idAlmacen + " AND L.idEmpaque=" + idProducto + " AND L.saldo-L.separados > 0\n"
                + "ORDER BY L.fechaCaducidad";
        try (Statement st = cn.createStatement(); Statement st1 = cn.createStatement()) {
            ResultSet rs = st1.executeQuery(strSQL);
            while (rs.next()) {
                separar = rs.getDouble("disponibles");
                if (solicitados - separados < separar) {
                    separar = solicitados - separados;
                }
                strSQL = "UPDATE almacenesLotes SET separados=separados+" + separar + " "
                        + "WHERE idAlmacen=" + idAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + rs.getString("lote") + "'";
                st.executeUpdate(strSQL);

                if (rs.getInt("idMovtoAlmacen") == 0) {
                    strSQL = "INSERT INTO movimientosDetalleAlmacen (idMovtoAlmacen, idEmpaque, lote, cantidad, fecha, existenciaAnterior) "
                            + "VALUES (" + idMovtoAlmacen + ", " + idProducto + ", '" + rs.getString("lote") + "', " + separar + ", GETDATE(), " + rs.getDouble("saldo") + ")";
                } else {
                    strSQL = "UPDATE movimientosDetalleAlmacen "
                            + "SET cantidad=cantidad+" + separar + ", fecha=GETDATE(), existenciaAnterior=" + rs.getDouble("saldo") + " "
                            + "WHERE idMovtoAlmacen=" + idMovtoAlmacen + " AND idEmpaque=" + idProducto + " AND lote='" + rs.getString("lote") + "'";
                }
                st.executeUpdate(strSQL);

                separados += separar;
                if (solicitados == separados) {
                    break;
                }
            }
        }
        return separados;
    }

//    private double separaLotes(Connection cn, int idAlmacen, int idMovtoAlmacen, int idProducto, double solicitados, boolean total) throws SQLException {
//        double separados = this.separaLotes(cn, idAlmacen, idMovtoAlmacen, idProducto, solicitados);
//        if (total && solicitados != separados) {
//            throw (new SQLException("No hay suficientes lotes del producto (id=" + idProducto + ") !!!"));
//        }
//        return separados;
//    }
    private void separaRelacionados(Connection cn, TOMovimientoOficina toMov, TOProductoOficina toProd, ArrayList<Double> boletin, double cantSeparada, double cantSolicitada, boolean total) throws SQLException {
        String strSQL;
        double disponibles = 0;
        strSQL = "SELECT Almacen.idAlmacen, Almacen.idEmpaque, Almacen.disponiblesAlmacen\n"
                + "     , ISNULL(Oficina.existenciaOficina, 0)-ISNULL(Oficina.separados, 0) AS disponiblesOficina\n"
                + "FROM (SELECT idAlmacen, idEmpaque, SUM(saldo-separados) AS disponiblesAlmacen\n"
                + "     FROM almacenesLotes\n"
                + "     WHERE idAlmacen=" + toMov.getIdAlmacen() + " AND idEmpaque=" + toProd.getIdProducto() + "\n"
                + "     GROUP BY idAlmacen, idEmpaque) Almacen\n"
                + "LEFT JOIN almacenesEmpaques Oficina\n"
                + "       ON Oficina.idAlmacen=Almacen.idAlmacen AND Oficina.idEmpaque=Almacen.idEmpaque";
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                double disponiblesOficina = rs.getDouble("disponiblesOficina");
                double disponiblesAlmacen = rs.getDouble("disponiblesAlmacen");
                if (total) {
                    if (cantSolicitada > disponiblesOficina) {
                        throw new SQLException("No hay existencia suficiente del producto (id=" + toProd.getIdProducto() + ")");
                    } else if (cantSolicitada > disponiblesAlmacen) {
                        throw new SQLException("No hay lotes suficientes del producto (id=" + toProd.getIdProducto() + ")");
                    }
                } else if (disponiblesAlmacen <= 0) {
                    throw new SQLException("No hay lotes del producto (id=" + toProd.getIdProducto() + ")");
                } else if (disponiblesOficina <= 0) {
                    throw new SQLException("No hay existencia del producto (id=" + toProd.getIdProducto() + ")");
                } else if (disponiblesAlmacen < disponiblesOficina) {
                    disponibles = disponiblesAlmacen;
                } else {
                    disponibles = disponiblesOficina;
                }
                if (cantSolicitada != disponibles) {
                    if (disponibles < cantSolicitada) {
                        toProd.setCantSinCargo((int) ((cantSeparada + disponibles) / (boletin.get(0) + boletin.get(1))) * boletin.get(1));
                        toProd.setCantFacturada(cantSeparada + disponibles - toProd.getCantSinCargo());
                    } else {
                        disponibles = cantSolicitada;
                    }
                }
                this.separaLotes(cn, toMov.getIdAlmacen(), toMov.getIdMovtoAlmacen(), toProd.getIdProducto(), disponibles);
            } else {
                throw new SQLException("No se encontraron lotes del producto (id=" + toProd.getIdProducto() + ")");
            }
            strSQL = "UPDATE almacenesEmpaques\n"
                    + "SET separados=separados+" + disponibles + "\n"
                    + "WHERE idAlmacen=" + toMov.getIdAlmacen() + " AND idEmpaque=" + toProd.getIdProducto();
            st.executeUpdate(strSQL);

            strSQL = "UPDATE movimientosDetalle\n"
                    + "SET cantFacturada=" + toProd.getCantFacturada() + ", cantSinCargo=" + toProd.getCantSinCargo() + "\n"
                    + "WHERE idMovto=" + toMov.getIdMovto() + " AND idEmpaque=" + toProd.getIdProducto();
            st.executeUpdate(strSQL);
        }
    }

    public ArrayList<TOProductoAlmacen> grabarMovimientoDetalle(int idEmpresa, TOMovimientoOficina toMov, TOProductoOficina toProd, double cantSeparada, boolean total) throws SQLException {
        ArrayList<Double> boletin;
        ArrayList<TOProductoAlmacen> lotes;
        double cantSolicitada, cantLiberar;

        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                if (toMov.getIdTipo() == 28) {
                    boletin = this.obtenerBoletinSinCargo(cn, idEmpresa, toMov.getIdReferencia(), toProd.getIdProducto());
                    if (boletin.get(0) > 0) {
                        toProd.setCantSinCargo((int) (toProd.getCantFacturada() / boletin.get(0)) * boletin.get(1));
                    } else {
                        toProd.setCantSinCargo(0);
                    }
                } else {
                    boletin = new ArrayList<>();
                    boletin.add(0.0);
                    boletin.add(0.0);
                }
                if (toProd.getCantFacturada() + toProd.getCantSinCargo() > cantSeparada) {
                    cantSolicitada = (toProd.getCantFacturada() + toProd.getCantSinCargo()) - cantSeparada;
                    this.separaRelacionados(cn, toMov, toProd, boletin, cantSeparada, cantSolicitada, total);
                } else {
                    cantLiberar = cantSeparada - (toProd.getCantFacturada() + toProd.getCantSinCargo());
                    this.liberaRelacionados(cn, toMov, toProd, cantLiberar);
                }
                lotes = this.obtenLotes(cn, toMov.getIdMovtoAlmacen(), toProd.getIdProducto());
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return lotes;
    }

//    private ArrayList<TOProductoPedido> grabaMovimientoDetalle(Connection cn, int idEmpresa, int idAlmacen, int idMovto, int idTienda, TOProductoPedido to, double separados, int idZonaImpuestos) throws SQLException {
////    private ArrayList<TOMovimientoProducto> grabaMovimientoDetalle(Connection cn, boolean esVenta, int idEmpresa, int idAlmacen, int idMovto, int idMovtoAlmacen, TOMovimientoProducto to, int idTienda, double separados, int idZonaImpuestos) throws SQLException {
//        ArrayList<TOProductoPedido> agregados = new ArrayList<>();
//        double cantSolicitada, cantSeparada, cantLiberar;
//        int idProducto;
//        String strSQL;
//        try (Statement st = cn.createStatement()) {
//            if (to.getIdMovto() == 0) {
//                TOPedido toPed = new TOPedido();
//                toPed.setIdEmpresa(idEmpresa);
//                toPed.setIdReferencia(idTienda);
//
//                this.agregarProductoOficina(cn, idMovto, to, idZonaImpuestos);
//                this.actualizaProductoPrecio(cn, toPed, to);
//            } else if (to.getCantFacturada() + to.getCantSinCargo() != separados) {
//                idProducto = to.getIdProducto();
//                if (to.getCantFacturada() > (separados - to.getCantSinCargo())) {
//                    cantSolicitada = to.getCantFacturada() - (separados - to.getCantSinCargo());
////                    if (esVenta) {
//                    // Falta validar si es con o sin pedido
////                        cantSeparada = this.separaRelacionados(idAlmacen, idMovtoAlmacen, idProducto, cantSolicitada, true);
//                    cantSeparada = 0;
//                    strSQL = "UPDATE movimientosDetalle\n"
//                            + "SET cantFacturada=cantFacturada+" + cantSeparada + "\n"
//                            + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + to.getIdProducto();
////                    } else {
////                        cantSeparada = cantSolicitada;
////                        strSQL = "UPDATE pedidosOCTiendaDetalle "
////                                + "SET cantFacturada=cantFacturada+" + cantSeparada + " "
////                                + "WHERE idPedido=" + idMovto + " AND idEmpaque=" + idProducto;
////                    }
//                    st.executeUpdate(strSQL);
//                } else {
//                    cantLiberar = (separados - to.getCantSinCargo()) - to.getCantFacturada();
////                    if (esVenta) {
//                    // Falta validar si es con o sin pedido
////                        this.liberaRelacionados(idAlmacen, idMovtoAlmacen, idProducto, cantLiberar);
//                    strSQL = "UPDATE movimientosDetalle\n"
//                            + "SET cantFacturada=cantFacturada-" + cantLiberar + "\n"
//                            + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + idProducto;
////                    } else {
////                        cantSeparada = cantLiberar;
////                        strSQL = "UPDATE pedidosOCTiendaDetalle "
////                                + "SET cantFacturada=cantFacturada-" + cantSeparada + " "
////                                + "WHERE idPedido=" + idMovto + " AND idEmpaque=" + idProducto;
////                    }
//                    st.executeUpdate(strSQL);
//                }
//                ArrayList<Double> boletin = this.obtenerBoletinSinCargo(cn, idEmpresa, idTienda, to.getIdProducto());
//                double boletinConCargo = boletin.get(0);
//                double boletinSinCargo = boletin.get(1);
//                if (boletinConCargo > 0) {
////                    if (esVenta) {
//                    // Falta validar si es con o sin pedido
//                    strSQL = "SELECT SUM(D.cantFacturada) AS cantFacturada, SUM(D.cantSinCargo) AS cantSinCargo\n"
//                            + "FROM (SELECT idEmpaque FROM empaquesSimilares WHERE idSimilar=" + idProducto + ") S\n"
//                            + "INNER JOIN (SELECT * FROM movimientosDetalle WHERE idMovto=" + idMovto + ") D ON D.idEmpaque=S.idEmpaque";
////                    } else {
////                        strSQL = "SELECT SUM(D.cantFacturada) AS cantFacturada, SUM(D.cantSinCargo) AS cantSinCargo\n"
////                                + "FROM (SELECT idEmpaque FROM empaquesSimilares WHERE idSimilar=" + idProducto + ") S\n"
////                                + "INNER JOIN (SELECT * FROM pedidosOCTiendaDetalle WHERE idPedido=" + idMovto + ") D ON D.idEmpaque=S.idEmpaque";
////                    }
//                    ResultSet rs = st.executeQuery(strSQL);
//                    if (rs.next()) {
//                        double cantSinCargoSimilares = ((int) (rs.getDouble("cantFacturada") / boletinConCargo)) * boletinSinCargo;
//                        if (cantSinCargoSimilares != rs.getDouble("cantSinCargo")) {
//                            if (cantSinCargoSimilares > rs.getDouble("cantSinCargo")) {
//                                cantSolicitada = cantSinCargoSimilares - rs.getDouble("cantSinCargo");
////                                if (esVenta) {
//                                // Falta validar si es con o sin pedido
//                                strSQL = "SELECT P.principal, P.idPedido, P.idEmpaque, P.cantFacturada, P.cantSinCargo\n"
//                                        + "       , P.unitario, P.idImpuesto, L.fechaCaducidad, L.saldo-L.separados AS disponibles\n"
//                                        + "FROM (SELECT CASE WHEN D.idEmpaque=S.idSimilar THEN 1 ELSE 0 END AS principal\n"
//                                        + "         , ISNULL(D.idPedido, 0) AS idPedido, ISNULL(D.idEmpaque,S.idEmpaque) AS idEmpaque\n"
//                                        + "         , ISNULL(D.cantFacturada, 0) AS cantFacturada, ISNULL(D.cantSinCargo, 0) AS cantSinCargo\n"
//                                        + "         , ISNULL(D.unitario, 0) AS unitario, P.idImpuesto\n"
//                                        + "     FROM (SELECT * FROM pedidosOCTiendaDetalle WHERE idPedido=" + idMovto + ") D\n"
//                                        + "     RIGHT JOIN empaquesSimilares S ON S.idEmpaque=D.idEmpaque\n"
//                                        + "     INNER JOIN empaques E ON E.idEmpaque=S.idEmpaque\n"
//                                        + "     INNER JOIN productos P ON P.idProducto=E.idProducto\n"
//                                        + "     WHERE S.idSimilar=" + idProducto + ") P\n"
//                                        + "LEFT JOIN almacenesLotes L ON L.idAlmacen=" + idAlmacen + " AND L.idEmpaque=P.idEmpaque\n"
//                                        + "WHERE L.idAlmacen IS NOT NULL AND L.saldo-L.separados > 0\n"
//                                        + "ORDER BY P.principal DESC, P.idPedido DESC, L.fechaCaducidad";
////                                } else {
////                                    strSQL = "SELECT 1 AS principal, idPedido, idEmpaque, cantFacturada, cantSinCargo, unitario, idImpuesto\n"
////                                            + "FROM pedidosOCTiendaDetalle\n"
////                                            + "WHERE idPedido=" + idMovto + " AND idEmpaque=" + idProducto;
////                                }
//                                rs = st.executeQuery(strSQL);
//                                while (rs.next()) {
////                                    if (esVenta) {
//                                    // Falta validar si es con o sin pedido
////                                        cantSeparada = this.separaRelacionados(idAlmacen, idMovtoAlmacen, idProducto, cantSolicitada, true);
//                                    cantSeparada = 0;
//                                    strSQL = "UPDATE movimientosDetalle\n"
//                                            + "SET cantFacturada=cantFacturada+" + cantSeparada + "\n"
//                                            + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + rs.getInt("idEmpaque");
////                                    } else {
////                                        cantSeparada = cantSolicitada;
////                                        strSQL = "UPDATE pedidosOCTiendaDetalle "
////                                                + "SET cantSinCargo=cantSinCargo+" + cantSeparada + " "
////                                                + "WHERE idPedido=" + idMovto + " AND idEmpaque=" + rs.getInt("idEmpaque");
////
////                                    }
//                                    st.executeUpdate(strSQL);
//                                    cantSolicitada -= cantSeparada;
//                                    if (cantSolicitada == 0) {
//                                        break;
//                                    }
//                                }
//                            } else {
//                                cantLiberar = rs.getDouble("cantSinCargo") - cantSinCargoSimilares;
////                                if (esVenta) {
////                                } else {
////                                    strSQL = "SELECT P.principal, P.idEmpaque, P.cantFacturada, P.cantSinCargo, P.unitario, P.idImpuesto*\n"
////                                            + "FROM (SELECT CASE WHEN D.idEmpaque=S.idSimilar THEN 1 ELSE 0 END AS principal\n"
////                                            + "		, ISNULL(D.idEmpaque,S.idEmpaque) AS idEmpaque\n"
////                                            + "		, ISNULL(D.cantFacturada, 0) AS cantFacturada, ISNULL(D.cantSinCargo, 0) AS cantSinCargo\n"
////                                            + "		, ISNULL(D.unitario, 0) AS unitario, P.idImpuesto\n"
////                                            + "	FROM (SELECT * FROM pedidosOCTiendaDetalle WHERE idPedido=" + idMovto + ") D\n"
////                                            + "	RIGHT JOIN empaquesSimilares S ON S.idEmpaque=D.idEmpaque\n"
////                                            + "	INNER JOIN empaques E ON E.idEmpaque=S.idEmpaque\n"
////                                            + "	INNER JOIN productos P ON P.idProducto=E.idProducto\n"
////                                            + "	WHERE S.idSimilar=837 AND D.idPedido IS NOT NULL AND D.cantSinCargo > 0) P\n"
////                                            + "ORDER BY P.principal DESC, P.idEmpaque";
////                                }
////                                rs = st.executeQuery(strSQL);
////                                while (rs.next()) {
////                                    if (esVenta) {
////                                    } else {
////                                        if (rs.getDouble("cantSinCargo") <= cantLiberar) {
////                                            cantLiberada = rs.getDouble("cantSinCargo");
////                                        } else {
////                                            cantLiberada = cantLiberar;
////                                        }
////                                        strSQL = "UPDATE pedidosOCTiendaDetalle "
////                                                + "SET cantSinCargo=cantSinCargo" + cantLiberada + " "
////                                                + "WHERE idPedido=" + idMovto + " AND idEmpaque=" + rs.getInt("idEmpaque");
////                                        cantLiberar -= cantLiberada;
////                                        if (cantLiberar == 0) {
////                                            break;
////                                        }
////                                    }
////                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return agregados;
//    }
//    private void surteOrdenDeCompra(Connection cn, int idAlmacen, int idEmpresa, int idMovto, int idTienda, int idZonaImpuestos) throws SQLException {
//        int idx;
//        double separados;
//        TOProductoPedido agregado;
//        ArrayList<TOProductoPedido> agregados = new ArrayList<>();
//
//        ArrayList<TOProductoPedido> detalle = this.obtenDetalle(cn, idMovto);
//        for (TOProductoPedido to : detalle) {
//            separados = to.getCantFacturada() + to.getCantSinCargo();
//            if (separados != 0) {
//                to.setCantFacturada(0);
//            } else {
//                to.setCantFacturada(to.getCantOrdenada());
//            }
//            for (TOProductoPedido toAgr : this.grabaMovimientoDetalle(cn, idAlmacen, idEmpresa, idMovto, idTienda, to, separados, idZonaImpuestos)) {
//                if ((idx = agregados.indexOf(toAgr)) != -1) {
//                    agregado = agregados.get(idx);
//                    agregado.setCantFacturada(agregado.getCantFacturada() + toAgr.getCantFacturada());
//                    agregado.setCantSinCargo(agregado.getCantSinCargo() + toAgr.getCantSinCargo());
//                } else {
//                    agregados.add(toAgr);
//                }
//            }
//        }
//    }
//    public void surtirOrdenDeCompra(int idAlmacen, int idMovto, int idMovtoAlmacen, int idZonaImpuestos) throws SQLException {
////    public void surtirOrdenDeCompra(boolean esVenta, int idAlmacen, int idMovto, int idMovtoAlmacen, int idZonaImpuestos) throws SQLException {
//        try (Connection cn = this.ds.getConnection()) {
//            cn.setAutoCommit(false);
//            try (Statement st = cn.createStatement()) {
//                String strSQL;
////                if (esVenta) {
//                strSQL = "SELECT M.idEmpresa, M.idReferencia AS idTienda\n"
//                        + "FROM movimientos M\n"
//                        + "WHERE M.idMovto=" + idMovto;
////                } else {
////                    strSQL = "SELECT A.idEmpresa, P.idTienda\n"
////                            + "FROM pedidosOC P\n"
////                            + "INNER JOIN almacenes A ON A.idAlmacen=P.idAlmacen\n"
////                            + "WHERE P.idPedido=" + idMovto;
////                }
//                ResultSet rs = st.executeQuery(strSQL);
//                if (rs.next()) {
//                    int idEmpresa = rs.getInt("idEmpresa");
//                    int idTienda = rs.getInt("idTienda");
//                    this.surteOrdenDeCompra(cn, idAlmacen, idEmpresa, idMovto, idMovtoAlmacen, idTienda, idZonaImpuestos);
//                }
//                cn.commit();
//            } catch (SQLException ex) {
//                cn.rollback();
//                throw ex;
//            } finally {
//                cn.setAutoCommit(true);
//            }
//        }
//    }
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

    public void grabarSalidaAlmacen(TOMovimientoOficina to) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement(); Statement st1 = cn.createStatement()) {
                ResultSet rs1;
                to.setIdUsuario(this.idUsuario);
                to.setFolio(this.obtenerMovimientoFolio(cn, false, to.getIdAlmacen(), to.getIdTipo()));

                strSQL = "UPDATE movimientosAlmacen SET fecha=GETDATE(), estatus=2, folio=" + to.getFolio() + ", idUsuario=" + this.idUsuario + " "
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

    public void grabarSalidaOficina(TOMovimientoOficina to) throws SQLException {
        String strSQL;
//        ArrayList<TOMovimientoProducto> detalle = new ArrayList<>();

        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                to.setIdUsuario(this.idUsuario);
                to.setFolio(this.obtenerMovimientoFolio(cn, true, to.getIdAlmacen(), to.getIdTipo()));

                strSQL = "UPDATE movimientos "
                        + "SET fecha=GETDATE(), estatus=2, folio=" + to.getFolio() + ", idUsuario=" + this.idUsuario + " "
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
            strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantFacturada, cantSinCargo, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior) "
                    + "VALUES (" + idMovto + ", " + to.getIdProducto() + ", " + to.getCantFacturada() + ", 0, 0, 0, 0, 0, 0, 0, 0, GETDATE(), 0)";
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

    public void grabarEntradaOficina(TOMovimientoOficina to) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                to.setIdUsuario(this.idUsuario);
                to.setFolio(this.obtenerMovimientoFolio(cn, true, to.getIdAlmacen(), to.getIdTipo()));

                strSQL = "UPDATE movimientos SET fecha=GETDATE(), estatus=2, folio=" + to.getFolio() + ", idUsuario=" + this.idUsuario + " "
                        + "WHERE idMovto=" + to.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalle WHERE idMovto=" + to.getIdMovto() + " AND cantFacturada=0";
                st.executeUpdate(strSQL);

                if (to.getIdTipo() == 3 || to.getIdTipo() == 18) {
                    strSQL = "UPDATE MD\n"
                            + "SET MD.costoPromedio=F.costoUnitarioPromedio, MD.costo=F.costoUnitarioPromedio, MD.unitario=F.costoUnitarioPromedio\n"
                            + "FROM movimientosDetalle MD\n"
                            + "INNER JOIN movimientos M ON M.idMovto=MD.idMovto\n"
                            + "INNER JOIN formulas F ON F.idEmpresa=M.idEmpresa AND F.idEmpaque=MD.idEmpaque\n"
                            + "WHERE MD.idMovto=" + to.getIdMovto();
                } else {
                    strSQL = "UPDATE d "
                            + "SET d.costoPromedio=dd.unitario, d.costo=dd.unitario, d.unitario=dd.unitario "
                            + "     , d.fecha=GETDATE(), d.existenciaAnterior=a.existenciaOficina "
                            + "FROM movimientosDetalle d "
                            + "INNER JOIN movimientos m ON m.idMovto=d.idMovto "
                            + "INNER JOIN almacenesEmpaques a ON a.idAlmacen=m.idAlmacen AND a.idEmpaque=d.idEmpaque "
                            + "INNER JOIN empresasEmpaques e ON e.idEmpresa=m.idEmpresa AND e.idEmpaque=d.idEmpaque "
                            + "INNER JOIN movimientosDetalle dd ON dd.idMovto=e.idMovtoUltimaCompra AND dd.idEmpaque=d.idEmpaque "
                            + "WHERE d.idMovto=" + to.getIdMovto();
                }
                int n = st.executeUpdate(strSQL);

                strSQL = "SELECT idEmpaque FROM movimientosDetalle WHERE idMovto=" + to.getIdMovto() + " AND unitario=0";
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    throw new SQLException("(idMovto=" + to.getIdMovto() + "), Producto id=" + rs.getInt("idEmpaque") + " Sin costo unitario !!!");
                }
                strSQL = "INSERT INTO almacenesEmpaques (idAlmacen, idEmpaque, existenciaOficina, separados, existenciaMaxima, existenciaMinima)\n"
                        + "SELECT M.idAlmacen, MD.idEmpaque, 0, 0, 0, 0\n"
                        + "FROM movimientosDetalle MD\n"
                        + "INNER JOIN movimientos M ON M.idMovto=MD.idMovto\n"
                        + "LEFT JOIN almacenesEmpaques AE ON AE.idAlmacen=M.idAlmacen AND AE.idEmpaque=MD.idEmpaque\n"
                        + "WHERE MD.idMovto=" + to.getIdMovto() + " AND AE.idEmpaque IS NULL";
                st.executeUpdate(strSQL);

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
                strSQL = "INSERT INTO empresasEmpaques (idEmpresa, idEmpaque, existenciaOficina, costoUnitarioPromedio, idMovtoUltimaCompra)\n"
                        + "SELECT M.idEmpresa, MD.idEmpaque, 0, 0, 0\n"
                        + "FROM movimientosDetalle MD\n"
                        + "INNER JOIN movimientos M ON M.idMovto=MD.idMovto\n"
                        + "LEFT JOIN empresasEmpaques EE ON EE.idEmpresa=M.idEmpresa AND EE.idEmpaque=MD.idEmpaque\n"
                        + "WHERE MD.idMovto=" + to.getIdMovto() + " AND EE.idEmpaque IS NULL";

                strSQL = "UPDATE e "
                        + "SET e.costoUnitarioPromedio=(e.existenciaOficina*e.costoUnitarioPromedio+d.cantFacturada*d.unitario)/(e.existenciaOficina+d.cantFacturada) "
                        + "    , e.existenciaOficina=e.existenciaOficina+d.cantFacturada "
                        + "FROM (SELECT m.idEmpresa, d.* "
                        + "		FROM movimientosDetalle d "
                        + "		INNER JOIN movimientos m ON m.idMovto=d.idMovto "
                        + "		WHERE d.idMovto=" + to.getIdMovto() + ") d "
                        + "INNER JOIN empresasEmpaques e ON e.idEmpresa=d.idEmpresa AND e.idEmpaque=d.idEmpaque";
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
        String strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantFacturada, cantSinCargo, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior) "
                + "VALUES (" + idMovto + ", " + to.getIdProducto() + ", " + to.getCantFacturada() + ", 0, 0, 0, 0, 0, 0, 0, 0, GETDATE(), 0)";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);
            }
        }
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

    public void grabarEntradaAlmacen(TOMovimientoOficina to) throws SQLException {
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

                strSQL = "UPDATE movimientosAlmacen SET fecha=GETDATE(), estatus=2, folio=" + to.getFolio() + ", idUsuario=" + this.idUsuario + " "
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
    private void generaRechazoTraspaso(Connection cn, TOMovimientoOficina to) throws SQLException {
        String strSQL = "SELECT M.idAlmacen, DE.idEmpaque, DE.cantFacturada, DS.cantFacturada\n"
                + "FROM movimientosDetalle DE\n"
                + "INNER JOIN movimientos M ON M.idMovto=DE.idMovto\n"
                + "INNER JOIN movimientosDetalle DS ON DS.idMovto=M.referencia AND DS.idEmpaque=DE.idEmpaque\n"
                + "WHERE DE.idMovto=" + to.getIdMovto() + " AND DE.cantFacturada < DS.cantFacturada";
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                TOMovimientoOficina toRechazo = new TOMovimientoOficina();
                toRechazo.setIdTipo(54);
//                rs = st.executeQuery("SELECT idCedis FROM almacenes WHERE idAlmacen=" + to.getIdReferencia());
//                if (rs.next()) {
//                    toRechazo.setIdCedis(rs.getInt("idCedis"));
//                }
//                toRechazo.setIdEmpresa(to.getIdEmpresa());
                toRechazo.setIdAlmacen(to.getIdReferencia());
                toRechazo.setIdUsuario(this.idUsuario);
//                toRechazo.setIdMoneda(1);
                toRechazo.setTipoDeCambio(1);
                toRechazo.setIdReferencia(to.getIdAlmacen());
                toRechazo.setReferencia(to.getIdMovto());
                toRechazo.setEstatus(2);

                this.agregaMovimientoRelacionado(cn, toRechazo, true);

                strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantFacturada, cantSinCargo, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior)\n"
                        + "SELECT " + toRechazo.getIdMovto() + ", DS.idEmpaque, DS.cantFacturada-DE.cantFacturada AS cantFacturada, 0, DS.unitario, DS.costo, 0, 0, 0, DS.unitario, DS.idImpuestoGrupo, '', 0\n"
                        + "FROM movimientosDetalle DE\n"
                        + "INNER JOIN movimientos M ON M.idMovto=DE.idMovto\n"
                        + "INNER JOIN movimientosDetalle DS ON DS.idMovto=M.referencia AND DS.idEmpaque=DE.idEmpaque\n"
                        + "WHERE DE.idMovto=" + to.getIdMovto() + " AND DE.cantFacturada < DS.cantFacturada";
                int n = st.executeUpdate(strSQL);

                strSQL = "UPDATE D\n"
                        + "SET D.fecha=GETDATE(), D.existenciaAnterior=A.existenciaOficina\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toRechazo.getIdMovto();
                if (st.executeUpdate(strSQL) != n) {
                    throw new SQLException("(idMovto=" + to.getIdMovto() + ") No se encontro empaque en almacen !!!");
                }
                strSQL = "UPDATE A\n"
                        + "SET A.existenciaOficina=A.existenciaOficina+D.cantFacturada\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toRechazo.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE E\n"
                        + "SET E.costoUnitarioPromedio=(E.costoUnitarioPromedio*E.existenciaOficina+D.costoPromedio*D.cantFacturada)/(E.existenciaOficina+D.cantFacturada)\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toRechazo.getIdMovto();
                if (st.executeUpdate(strSQL) != n) {
                    throw new SQLException("(idMovto=" + to.getIdMovto() + ") No se encontro empaque en empresa !!!");
                }
                strSQL = "UPDATE E\n"
                        + "SET E.existenciaOficina=E.existenciaOficina+D.cantFacturada\n"
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
                        + "WHERE DE.idMovtoAlmacen=" + to.getIdMovtoAlmacen() + " AND DE.cantidad < DS.cantidad";
                n = st.executeUpdate(strSQL);

                strSQL = "UPDATE D\n"
                        + "SET fecha=GETDATE(), existenciaAnterior=A.saldo\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                        + "WHERE D.idMovtoAlmacen=" + toRechazo.getIdMovtoAlmacen();
                if (st.executeUpdate(strSQL) != n) {
                    throw new SQLException("(idMovtoAlmacen=" + toRechazo.getIdMovtoAlmacen() + ") No se encontro empaque-lote en almacen !!!");
                }
                strSQL = "UPDATE A\n"
                        + "SET saldo=A.saldo+D.cantidad\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                        + "WHERE D.idMovtoAlmacen=" + toRechazo.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);
            }
        }
    }

    public void grabarTraspasoRecepcion(TORecepcion m) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                m.setEstatus(2);
                m.setIdUsuario(this.idUsuario);

                strSQL = "UPDATE movimientosAlmacen\n"
                        + "SET fecha=GETDATE(), idUsuario=" + m.getIdUsuario() + ", estatus=" + m.getEstatus() + "\n"
                        + "WHERE idMovtoAlmacen=" + m.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE movimientos\n"
                        + "SET fecha=GETDATE(), idUsuario=" + m.getIdUsuario() + ", estatus=" + m.getEstatus() + "\n"
                        + "WHERE idMovto=" + m.getIdMovto();
                st.executeUpdate(strSQL);

                ResultSet rs = st.executeQuery("SELECT fecha FROM movimientos WHERE idMovto=" + m.getIdMovto());
                if (rs.next()) {
                    m.setFecha(new java.util.Date(rs.getDate("fecha").getTime()));
                }
                strSQL = "INSERT INTO almacenesEmpaques (idAlmacen, idEmpaque, existenciaOficina, separados, existenciaAlmacen, existenciaMinima, existenciaMaxima)\n"
                        + "SELECT M.idAlmacen, D.idEmpaque, 0, 0, 0, 0, 0\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "LEFT JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + m.getIdMovto() + " AND A.idAlmacen IS NULL";
                st.executeUpdate(strSQL);

                strSQL = "UPDATE D\n"
                        + "SET fecha=GETDATE(), existenciaAnterior=A.existenciaOficina\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + m.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE A\n"
                        + "SET existenciaOficina=A.existenciaOficina+D.cantFacturada\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + m.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE E\n"
                        + "SET costoUnitarioPromedio=(E.costoUnitarioPromedio*E.existenciaOficina+D.costoPromedio*D.cantFacturada)/(E.existenciaOficina+D.cantFacturada)\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + m.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE E\n"
                        + "SET existenciaOficina=E.existenciaOficina+D.cantFacturada\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + m.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "INSERT INTO almacenesLotes (idAlmacen, idEmpaque, lote, fechaCaducidad, cantidad, saldo, separados, existenciaFisica)\n"
                        + "SELECT DISTINCT M.idAlmacen, D.idEmpaque, D.lote, DATEADD(DAY, 365, L.fecha), 0, 0, 0, 0\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN lotes L ON SUBSTRING(L.lote, 1, 4)=SUBSTRING(D.lote, 1, 4)\n"
                        + "LEFT JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                        + "WHERE D.idMovtoAlmacen=" + m.getIdMovtoAlmacen() + " AND A.idAlmacen IS NULL\n"
                        + "ORDER BY D.idEmpaque, D.lote";
                st.executeUpdate(strSQL);

                strSQL = "UPDATE D\n"
                        + "SET fecha=GETDATE(), existenciaAnterior=A.saldo\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                        + "WHERE D.idMovtoAlmacen=" + m.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE A\n"
                        + "SET saldo=A.saldo+D.cantidad\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                        + "WHERE D.idMovtoAlmacen=" + m.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                this.generaRechazoTraspaso(cn, m);

                // Se hacen despues del rechazo para asegurar que los registro y por ende costos va a existir para la reentrada
                strSQL = "DELETE FROM movimientosDetalle WHERE idMovto=" + m.getIdMovto() + " AND cantFacturada=0";
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalleAlmacen WHERE idMovtoAlmacen=" + m.getIdMovtoAlmacen() + " AND cantidad=0";
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

    public void grabarTraspasoEnvio(int idEmpresa, TOMovimientoOficina to) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                to.setIdUsuario(this.idUsuario);
                to.setEstatus(2);

                int idFolioAlmacen = this.obtenerMovimientoFolio(cn, false, to.getIdAlmacen(), to.getIdTipo());
                strSQL = "UPDATE movimientosAlmacen\n"
                        + "SET folio=" + idFolioAlmacen + ", fecha=GETDATE(), idUsuario=" + this.idUsuario + ", estatus=2\n"
                        + "WHERE idMovtoAlmacen=" + to.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE solicitudes SET estatus=2 WHERE idSolicitud=" + to.getReferencia();
                st.executeUpdate(strSQL);

                to.setFolio(this.obtenerMovimientoFolio(cn, true, to.getIdAlmacen(), to.getIdTipo()));
                strSQL = "UPDATE movimientos\n"
                        + "SET folio=" + to.getFolio() + ", fecha=GETDATE(), idUsuario=" + this.idUsuario + ", estatus=2\n"
                        + "WHERE idMovto=" + to.getIdMovto();
                st.executeUpdate(strSQL);

                ResultSet rs = st.executeQuery("SELECT fecha FROM movimientos WHERE idMovto=" + to.getIdMovto());
                if (rs.next()) {
                    to.setFecha(new java.util.Date(rs.getDate("fecha").getTime()));
                }
                strSQL = "UPDATE D\n"
                        + "SET fecha=GETDATE(), existenciaAnterior=L.saldo\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes L ON L.idAlmacen=M.idAlmacen AND L.idEmpaque=D.idEmpaque AND L.lote=D.lote\n"
                        + "WHERE D.idMovtoAlmacen=" + to.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE L\n"
                        + "SET saldo=saldo-D.cantidad, separados=separados-D.cantidad\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes L ON L.idAlmacen=M.idAlmacen AND L.idEmpaque=D.idEmpaque AND L.lote=D.lote\n"
                        + "WHERE D.idMovtoAlmacen=" + to.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE D\n"
                        + "SET fecha=GETDATE(), existenciaAnterior=E.existenciaOficina\n"
                        + "     , costoPromedio=EE.costoUnitarioPromedio, costo=EE.costoUnitarioPromedio, unitario=EE.costoUnitarioPromedio\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN almacenesEmpaques E ON E.idAlmacen=M.idAlmacen AND E.idEmpaque=D.idEmpaque\n"
                        + "INNER JOIN empresasEmpaques EE ON EE.idEmpresa=M.idEmpresa AND EE.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + to.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE E\n"
                        + "SET existenciaOficina=existenciaOficina-D.cantFacturada, separados=separados-D.cantFacturada\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN almacenesEmpaques E ON E.idAlmacen=M.idAlmacen AND E.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + to.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE EE\n"
                        + "SET existenciaOficina=existenciaOficina-D.cantFacturada\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN empresasEmpaques EE ON EE.idEmpresa=M.idEmpresa AND EE.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + to.getIdMovto();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE empresasEmpaques\n"
                        + "SET costoUnitarioPromedio=0\n"
                        + "WHERE idEmpresa=" + idEmpresa + " AND existenciaOficina=0";
                st.executeUpdate(strSQL);

                // ----------------------- SECCION: CREAR ENLACE ENVIO-RECEPCION ------------------

                TOMovimientoOficina toEnvio = new TOMovimientoOficina();

//                strSQL = "SELECT idCedis FROM almacenes WHERE idAlmacen=" + to.getIdReferencia();
//                rs = st.executeQuery(strSQL);
//                if (rs.next()) {
//                    toEnvio.setIdCedis(rs.getInt("idCedis"));
//                } else {
//                    throw new SQLException("No se encontro almacen=" + to.getIdReferencia());
//                }
                toEnvio.setIdTipo(9);
//                toEnvio.setIdEmpresa(to.getIdEmpresa());
                toEnvio.setIdAlmacen(to.getIdReferencia());
                toEnvio.setIdReferencia(to.getIdAlmacen());
                toEnvio.setIdUsuario(this.idUsuario);
//                toEnvio.setIdMoneda(1);
                toEnvio.setTipoDeCambio(1);
                toEnvio.setReferencia(to.getIdMovto());
                toEnvio.setEstatus(1);

                this.agregaMovimientoRelacionado(cn, toEnvio, true);

                // ------------------------- SECCION: CREAR RECEPCION ---------------------

                strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantFacturada, cantSinCargo, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior)\n"
                        + "SELECT " + toEnvio.getIdMovto() + ", idEmpaque, cantFacturada, cantSinCargo, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, '', 0\n"
                        + "FROM movimientosDetalle WHERE idMovto=" + to.getIdMovto() + " AND cantFacturada > 0";
                st.executeUpdate(strSQL);

                strSQL = "INSERT INTO movimientosDetalleAlmacen (idMovtoAlmacen, idEmpaque, lote, cantidad, fecha, existenciaAnterior)\n"
                        + "SELECT " + toEnvio.getIdMovtoAlmacen() + ", MD.idEmpaque, MD.lote, MD.cantidad, '', 0\n"
                        + "FROM movimientosDetalleAlmacen MD\n"
                        + "WHERE MD.idMovtoAlmacen=" + to.getIdMovtoAlmacen() + " AND MD.cantidad > 0";
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
        String condicion = " = 0 ";
        if (estatus != 0) {
            condicion = " > 1 ";
        }
        ArrayList<TOMovimientoAlmacen> solicitudes = new ArrayList<>();
        String strSQL = "SELECT M.* FROM movimientosAlmacen M "
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=" + idTipo + " AND M.estatus" + condicion
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

    public ArrayList<TOMovimientoOficina> obtenerMovimientos(int idAlmacen, int idTipo, int estatus, Date fechaInicial) throws SQLException {
        String condicion = " = 2 ";
        if (estatus != 2) {
            condicion = " <= 1 ";
        }
        if (fechaInicial == null) {
            fechaInicial = new Date();
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TOMovimientoOficina> movimientos = new ArrayList<>();
//        String strSQL = "SELECT M.*"
//                + "     , ISNULL(MA.idMovtoAlmacen, 0) AS idMovtoAlmacen, MA.folio AS folioAlmacen, ISNULL(MA.fecha, GETDATE()) AS fechaAlmacen"
//                + "     , ISNULL(MA.idUsuario, 0) AS idUsuarioAlmacen, ISNULL(MA.estatus, 0) AS statusAlmacen "
//                + "FROM movimientos M "
//                + "LEFT JOIN movimientosRelacionados MR ON MR.idMovto=M.idMovto "
//                + "LEFT JOIN movimientosAlmacen MA ON MA.idMovtoAlmacen=MR.idMovtoAlmacen "
//                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=" + idTipo + " AND M.estatus" + condicion
//                + "ORDER BY M.fecha DESC";
        String strSQL = "SELECT M.*, ISNULL(MR.idMovtoAlmacen, 0) AS idMovtoAlmacen\n"
                + "FROM movimientos M\n"
                + "LEFT JOIN movimientosRelacionados MR ON MR.idMovto=M.idMovto\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=" + idTipo + " AND M.estatus" + condicion + "\n"
                + "         AND CONVERT(date, M.fecha) <= '" + format.format(fechaInicial) + "'\n"
                + "ORDER BY M.fecha DESC";
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                movimientos.add(this.construirMovimiento(rs));
            }
        } finally {
            cn.close();
        }
        return movimientos;
    }

//    public void grabarTraspasoSolicitud(TOMovimiento to, ArrayList<SolicitudProducto> productos) throws SQLException {
//        String strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantFacturada, cantSinCargo, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior) "
//                + "VALUES (?, ?, 0, 0, 0, 0, 0, 0, 0, 0, ?, GETDATE(), 0)";
//        try (Connection cn = this.ds.getConnection()) {
//            cn.setAutoCommit(false);
//            try (Statement st = cn.createStatement(); PreparedStatement ps = cn.prepareStatement(strSQL)) {
//                to.setIdUsuario(this.idUsuario);
//
//                strSQL = "SELECT GETDATE() AS fecha";
//                ResultSet rs = st.executeQuery(strSQL);
//                rs.next();
//                to.setFecha(new Date(rs.getDate("fecha").getTime()));
//
//                int idSolicitud = 0;
//                strSQL = "INSERT INTO solicitudes (fecha, idUsuario, propietario, estatus)\n"
//                        + "VALUES (GETDATE(), " + this.idUsuario + ", 0, 2)";
//                st.executeUpdate(strSQL);
//                rs = st.executeQuery("SELECT @@IDENTITY AS idSolicitud");
//                if (rs.next()) {
//                    idSolicitud = rs.getInt("idSolicitud");
//                }
//                to.setReferencia(idSolicitud);
//
//                this.agregaMovimientoRelacionado(cn, to, true);
//
//                for (SolicitudProducto p : productos) {
//                    if (p.getCantSolicitada() != 0) {
//                        strSQL = "INSERT INTO solicitudesDetalle (idSolicitud, idEmpaque, cantSolicitada)\n"
//                                + "VALUES (" + idSolicitud + ", " + p.getProducto().getIdProducto() + ", " + p.getCantSolicitada() + ")";
//                        st.executeUpdate(strSQL);
//
//                        ps.setInt(1, to.getIdMovto());
//                        ps.setInt(2, p.getProducto().getIdProducto());
//                        //                    ps.setDouble(3, p.getCantOrdenada());
//                        ps.setInt(3, p.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());
//                        ps.executeUpdate();
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
                strSQL = "INSERT INTO movimientosAlmacen (idTipo, idAlmacen, folio, idComprobante, fecha, idUsuario, estatus, idReferencia, referencia, propietario) "
                        + "VALUES (1, " + m.getIdAlmacen() + ", " + m.getFolio() + ", " + m.getIdComprobante() + ", GETDATE(), " + this.idUsuario + ", 2, " + m.getIdReferencia() + ", " + m.getReferencia() + ", 0)";
                st.executeUpdate(strSQL);
                rs = st.executeQuery("SELECT @@IDENTITY AS idMovtoAlmacen");
                if (rs.next()) {
                    m.setIdMovtoAlmacen(rs.getInt("idMovtoAlmacen"));
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
                                + "VALUES (" + m.getIdMovtoAlmacen() + ", " + idProducto + ", '" + lote + "', " + p.getCantidad() + ", GETDATE(), " + existenciaAnterior + ")";
                        st.executeUpdate(strSQL);
                    }
                }
                if (m.getReferencia() != 0) {
                    strSQL = "UPDATE OCD\n"
                            + "SET OCD.cantRecibidaAlmacen=OCD.cantRecibidaAlmacen+MD.cantidad\n"
                            + "FROM ordenCompraDetalle OCD\n"
                            + "INNER JOIN movimientosDetalleAlmacen MD ON MD.idMovtoAlmacen=" + m.getIdMovtoAlmacen() + " AND MD.idEmpaque=OCD.idEmpaque\n"
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
                        + "SELECT " + idMovtoTipo + ", idCedis, idEmpresa, idAlmacen, " + folio + ", idComprobante, GETDATE(), " + this.idUsuario + ", idReferencia, referencia, 0, 2\n"
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
        to.setIdMovtoAlmacen(rs.getInt("idMovtoAlmacen"));
        to.setIdTipo(rs.getInt("idTipo"));
        to.setFolio(rs.getInt("folio"));
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

    private void construirMovimiento(ResultSet rs, TOMovimientoOficina to) throws SQLException {
        to.setIdMovto(rs.getInt("idMovto"));
        to.setIdTipo(rs.getInt("idTipo"));
//        to.setIdCedis(rs.getInt("idCedis"));
        to.setIdEmpresa(rs.getInt("idEmpresa"));
        to.setIdAlmacen(rs.getInt("idAlmacen"));
        to.setFolio(rs.getInt("folio"));
        to.setIdComprobante(rs.getInt("idComprobante"));
        to.setIdImpuestoZona(rs.getInt("idImpuestoZona"));
        to.setDesctoComercial(rs.getDouble("desctoComercial"));
        to.setDesctoProntoPago(rs.getDouble("desctoprontoPago"));
        to.setFecha(new java.util.Date(rs.getTimestamp("fecha").getTime()));
        to.setIdUsuario(rs.getInt("idUsuario"));
//        to.setIdMoneda(rs.getInt("idMoneda"));
        to.setTipoDeCambio(rs.getDouble("tipoDeCambio"));
        to.setIdReferencia(rs.getInt("idReferencia"));
        to.setReferencia(rs.getInt("referencia"));
        to.setPropietario(rs.getInt("propietario"));
        to.setEstatus(rs.getInt("estatus"));
        to.setIdMovtoAlmacen(rs.getInt("idMovtoAlmacen"));
    }

    private TOMovimientoOficina construirMovimiento(ResultSet rs) throws SQLException {
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
        to.setFecha(new java.util.Date(rs.getDate("fecha").getTime()));
        to.setIdUsuario(rs.getInt("idUsuario"));
        to.setTipoDeCambio(rs.getDouble("tipoDeCambio"));
        to.setIdReferencia(rs.getInt("idReferencia"));
        to.setReferencia(rs.getInt("referencia"));
        to.setPropietario(rs.getInt("propietario"));
        to.setEstatus(rs.getInt("estatus"));
        to.setIdMovtoAlmacen(rs.getInt("idMovtoAlmacen"));
        return to;
    }

    public TOMovimientoOficina obtenerMovimientoRelacionado(int idMovto) throws SQLException {
        TOMovimientoOficina to = null;
        String strSQL = "SELECT M.*\n"
                + "     , ISNULL(MA.idMovtoAlmacen, 0) AS idMovtoAlmacen, MA.folio AS folioAlmacen, ISNULL(MA.fecha, GETDATE()) AS fechaAlmacen\n"
                + "     , ISNULL(MA.idUsuario, 0) AS idUsuarioAlmacen, ISNULL(MA.estatus, 0) AS statusAlmacen\n"
                + "FROM movimientos M\n"
                + "LEFT JOIN movimientosRelacionados MR ON MR.idMovto=M.idMovto\n"
                + "LEFT JOIN movimientosAlmacen MA ON MA.idMovtoAlmacen=MR.idMovtoAlmacen\n"
                + "WHERE M.idMovto=" + idMovto;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    to = construirMovimiento(rs);
                }
            }
        }
        return to;
    }

    public ArrayList<TOMovimientoOficina> obtenerMovimientosOficina(int idAlmacen, int idTipo, int idComprobante) throws SQLException {
        ArrayList<TOMovimientoOficina> tos = new ArrayList<>();
        String strSQL = "SELECT * FROM movimientos M\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=" + idTipo + " AND M.idComprobante=" + idComprobante;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    tos.add(construirMovimiento(rs));
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

//    public double obtenerCostoUltimaCompraProveedor25(int idEmpresa, int idProveedor, int idEmpaque) throws SQLException {
//        double precioLista = 0;
//        try (Connection cn = this.ds.getConnection()) {
////            obtenCostoUltimaCompraProveedor // Paso como privada a DAOCompras
//            precioLista = movimientos.Movimientos.obtenCostoUltimaCompraProveedor(cn, idEmpresa, idProveedor, idEmpaque);
//        }
//        return precioLista;
//    }

    public double obtenerCostoUltimaCompra(int idEmpresa, int idEmpaque) throws SQLException {
        double precioLista = 0;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                String strSQL = "SELECT U.costo\n"
                        + "FROM empresasEmpaques E\n"
                        + "INNER JOIN movimientosDetalle U ON U.idMovto=E.idMovtoUltimaCompra AND U.idEmpaque=E.idEmpaque\n"
                        + "WHERE idEmpresa=" + idEmpresa + " AND idEmpaque=" + idEmpaque;
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    precioLista = rs.getDouble("costo");
                }
            }
        }
        return precioLista;
    }

//  ===============================  IMPUESTOS  =========================================================
    public double obtenerImpuestosProducto(int idMovto, int idEmpaque, ArrayList<ImpuestosProducto> impuestos) throws SQLException {
        double importeImpuestos = 0;
        try (Connection cn = this.ds.getConnection()) {
            importeImpuestos = movimientos.Movimientos.obtenImpuestosProducto(cn, idMovto, idEmpaque, impuestos);
        }
        return importeImpuestos;
    }

//    private void agregarImpuestosProducto(Connection cn, int idMovto, int idEmpaque, int idImpuestoGrupo, int idZona) throws SQLException {
//        String strSQL = "insert into movimientosDetalleImpuestos (idMovto, idEmpaque, idImpuesto, impuesto, valor, aplicable, modo, acreditable, importe, acumulable) "
//                + "select " + idMovto + ", " + idEmpaque + ", id.idImpuesto, i.impuesto, id.valor, i.aplicable, i.modo, i.acreditable, 0.00 as importe, i.acumulable "
//                + "from impuestosDetalle id "
//                + "inner join impuestos i on i.idImpuesto=id.idImpuesto "
//                + "where id.idGrupo=" + idImpuestoGrupo + " and id.idZona=" + idZona + " and GETDATE() between fechaInicial and fechaFinal";
//
//        try (Statement st = cn.createStatement()) {
//            if (st.executeUpdate(strSQL) == 0) {
//                throw (new SQLException("No se generaron impuestos !!!"));
//            }
//        }
//    }
    public ArrayList<ImpuestosProducto> generarImpuestosProducto(int idImpuestoGrupo, int idZona) throws SQLException {
        ArrayList<ImpuestosProducto> impuestos = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            impuestos = this.generaImpuestosProducto(cn, idImpuestoGrupo, idZona);
        }
        return impuestos;
    }
    
    private ArrayList<ImpuestosProducto> generaImpuestosProducto(Connection cn, int idImpuestoGrupo, int idZona) throws SQLException {
        ArrayList<ImpuestosProducto> impuestos = new ArrayList<>();
        String strSQL = "SELECT id.idImpuesto, i.impuesto, id.valor, i.aplicable, i.modo, i.acreditable, 0.00 as importe, i.acumulable\n"
                + "FROM impuestosDetalle id\n"
                + "INNER JOIN impuestos i ON i.idImpuesto=id.idImpuesto\n"
                + "WHERE id.idGrupo=" + idImpuestoGrupo + " and id.idZona=" + idZona + " and GETDATE() between id.fechaInicial and id.fechaFinal\n"
                + "ORDER BY i.acumulable";
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                impuestos.add(movimientos.Movimientos.construirImpuestosProducto(rs));
            }
            if (impuestos.isEmpty()) {
                throw new SQLException("No se generaron impuestos !!!");
            }
        }
        return impuestos;
    }
    
    public int agregarMovimientoOficina(TOMovimientoOficina to, boolean definitivo) throws SQLException {
        int idMovto = 0;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                to.setEstatus(0);
                to.setIdUsuario(this.idUsuario);
                to.setPropietario(this.idUsuario);
                movimientos.Movimientos.agregaMovimientoOficina(cn, to, definitivo);
                idMovto = to.getIdMovto();
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
}
