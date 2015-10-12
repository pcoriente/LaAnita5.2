package recepciones.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import movimientos.to.TOMovimientoOficina;
import movimientos.to.TOProductoAlmacen;
import recepciones.to.TORecepcion;
import recepciones.to.TORecepcionProducto;
import recepciones.to.TORecepcionProductoAlmacen;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAORecepciones {
    int idUsuario, idCedis;
    private DataSource ds = null;

    public DAORecepciones() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
        this.idUsuario = usuarioSesion.getUsuario().getId();
        this.idCedis = usuarioSesion.getUsuario().getIdCedis();

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
    }
    
    public void cancelar(TORecepcion mov) throws SQLException {
        String strSQL="";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                mov.setEstatus(6);
                mov.setIdUsuario(this.idUsuario);
                
                movimientos.Movimientos.grabaMovimientoAlmacen(cn, mov);
                movimientos.Movimientos.grabaMovimientoOficina(cn, mov);
                
                strSQL="UPDATE movimientosDetalleAlmacen SET cantidad=0 WHERE idMovtoAlmacen="+mov.getIdMovtoAlmacen();
                st.executeUpdate(strSQL);
                
                strSQL="UPDATE movimientosDetalle SET cantFacturada=0 WHERE idMovto="+mov.getIdMovto();
                st.executeUpdate(strSQL);
                
                this.generaRechazo(cn, mov);
                
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }
    
    private void generaRechazo(Connection cn, TORecepcion mov) throws SQLException {
        String strSQL = "SELECT M.idAlmacen, DE.idEmpaque, DE.cantFacturada, DS.cantFacturada\n"
                + "FROM movimientosDetalle DE\n"
                + "INNER JOIN movimientos M ON M.idMovto=DE.idMovto\n"
                + "INNER JOIN movimientosDetalle DS ON DS.idMovto=M.referencia AND DS.idEmpaque=DE.idEmpaque\n"
                + "WHERE DE.idMovto=" + mov.getIdMovto() + " AND DE.cantFacturada < DS.cantFacturada";
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                TOMovimientoOficina toRechazo = new TOMovimientoOficina();
                toRechazo.setIdTipo(54);
                toRechazo.setIdEmpresa(mov.getIdEmpresa());
                toRechazo.setIdAlmacen(mov.getIdReferencia());
                toRechazo.setIdUsuario(mov.getIdUsuario());
                toRechazo.setTipoDeCambio(mov.getTipoDeCambio());
                toRechazo.setIdReferencia(mov.getIdAlmacen());
                toRechazo.setEstatus(5);
                
                toRechazo.setReferencia(mov.getIdMovtoAlmacen());
                toRechazo.setFolio(movimientos.Movimientos.obtenMovimientoFolioAlmacen(cn, toRechazo.getIdAlmacen(), toRechazo.getIdTipo()));
                movimientos.Movimientos.agregaMovimientoAlmacen(cn, toRechazo, true);
                
                toRechazo.setReferencia(mov.getIdMovto());
                toRechazo.setFolio(movimientos.Movimientos.obtenMovimientoFolioOficina(cn, toRechazo.getIdAlmacen(), toRechazo.getIdTipo()));
                movimientos.Movimientos.agregaMovimientoOficina(cn, toRechazo, true);

                strSQL = "INSERT INTO movimientosDetalle (idMovto, idEmpaque, cantFacturada, cantSinCargo, costoPromedio, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, idImpuestoGrupo, fecha, existenciaAnterior)\n"
                        + "SELECT " + toRechazo.getIdMovto() + ", DS.idEmpaque, DS.cantFacturada-DE.cantFacturada AS cantFacturada, 0, DS.unitario, DS.costo, 0, 0, 0, DS.unitario, DS.idImpuestoGrupo, '', 0\n"
                        + "FROM movimientosDetalle DE\n"
                        + "INNER JOIN movimientos M ON M.idMovto=DE.idMovto\n"
                        + "INNER JOIN movimientosDetalle DS ON DS.idMovto=M.referencia AND DS.idEmpaque=DE.idEmpaque\n"
                        + "WHERE DE.idMovto=" + mov.getIdMovto() + " AND DE.cantFacturada < DS.cantFacturada";
                int n = st.executeUpdate(strSQL);

                strSQL = "UPDATE D\n"
                        + "SET D.fecha=GETDATE(), D.existenciaAnterior=A.existencia\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + toRechazo.getIdMovto();
                if (st.executeUpdate(strSQL) != n) {
                    throw new SQLException("(idMovto=" + mov.getIdMovto() + ") No se encontro empaque en almacen !!!");
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
                    throw new SQLException("(idMovto=" + mov.getIdMovto() + ") No se encontro empaque en empresa !!!");
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
                        + "WHERE DE.idMovtoAlmacen=" + mov.getIdMovtoAlmacen() + " AND DE.cantidad < DS.cantidad";
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
                
                strSQL = "DELETE FROM movimientosDetalle WHERE idMovto=" + mov.getIdMovto() + " AND cantFacturada=0";
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM movimientosDetalleAlmacen WHERE idMovtoAlmacen=" + mov.getIdMovtoAlmacen() + " AND cantidad=0";
                st.executeUpdate(strSQL);
            }
        }
    }
    
    public void grabar(TORecepcion mov) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                this.generaRechazo(cn, mov);
                
                mov.setEstatus(5);
                mov.setIdUsuario(this.idUsuario);

                mov.setFolio(movimientos.Movimientos.obtenMovimientoFolioAlmacen(cn, mov.getIdAlmacen(), mov.getIdTipo()));
                movimientos.Movimientos.grabaMovimientoAlmacen(cn, mov);
                
                mov.setFolio(movimientos.Movimientos.obtenMovimientoFolioOficina(cn, mov.getIdAlmacen(), mov.getIdTipo()));
                movimientos.Movimientos.grabaMovimientoOficina(cn, mov);

                strSQL = "INSERT INTO almacenesEmpaques (idAlmacen, idEmpaque, existencia, separados, existenciaMinima, existenciaMaxima)\n"
                        + "SELECT M.idAlmacen, D.idEmpaque, 0, 0, 0, 0\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "LEFT JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + mov.getIdMovto() + " AND D.cantFacturada > 0 AND A.idAlmacen IS NULL";
                st.executeUpdate(strSQL);

                strSQL = "UPDATE D\n"
                        + "SET fecha=GETDATE(), existenciaAnterior=A.existencia\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + mov.getIdMovto() + " AND D.cantFacturada > 0";
                st.executeUpdate(strSQL);

                strSQL = "UPDATE A\n"
                        + "SET existencia=A.existencia+D.cantFacturada\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN almacenesEmpaques A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + mov.getIdMovto() + " AND D.cantFacturada > 0";
                st.executeUpdate(strSQL);

                strSQL = "UPDATE E\n"
                        + "SET costoUnitarioPromedio=(E.costoUnitarioPromedio*E.existencia+D.costoPromedio*D.cantFacturada)/(E.existencia+D.cantFacturada)\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + mov.getIdMovto() + " AND D.cantFacturada > 0";
                st.executeUpdate(strSQL);

                strSQL = "UPDATE E\n"
                        + "SET existencia=E.existencia+D.cantFacturada\n"
                        + "FROM movimientosDetalle D\n"
                        + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                        + "INNER JOIN empresasEmpaques E ON E.idEmpresa=M.idEmpresa AND E.idEmpaque=D.idEmpaque\n"
                        + "WHERE D.idMovto=" + mov.getIdMovto() + " AND D.cantFacturada > 0";
                st.executeUpdate(strSQL);

                strSQL = "INSERT INTO almacenesLotes (idAlmacen, idEmpaque, lote, fechaCaducidad, existencia, separados, existenciaFisica)\n"
                        + "SELECT DISTINCT M.idAlmacen, D.idEmpaque, D.lote, DATEADD(DAY, 365, L.fecha), 0, 0, 0\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN lotes L ON SUBSTRING(L.lote, 1, 4)=SUBSTRING(D.lote, 1, 4)\n"
                        + "LEFT JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                        + "WHERE D.idMovtoAlmacen=" + mov.getIdMovtoAlmacen() + " AND D.cantidad > 0 AND A.idAlmacen IS NULL\n"
                        + "ORDER BY D.idEmpaque, D.lote";
                st.executeUpdate(strSQL);

                strSQL = "UPDATE D\n"
                        + "SET fecha=GETDATE(), existenciaAnterior=A.existencia\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                        + "WHERE D.idMovtoAlmacen=" + mov.getIdMovtoAlmacen() + " AND D.cantidad > 0";
                st.executeUpdate(strSQL);

                strSQL = "UPDATE A\n"
                        + "SET existencia=A.existencia+D.cantidad\n"
                        + "FROM movimientosDetalleAlmacen D\n"
                        + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                        + "INNER JOIN almacenesLotes A ON A.idAlmacen=M.idAlmacen AND A.idEmpaque=D.idEmpaque AND A.lote=D.lote\n"
                        + "WHERE D.idMovtoAlmacen=" + mov.getIdMovtoAlmacen() + " AND D.cantidad > 0";
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
    
    public void actualizarCantidad(int idMovto, TOProductoAlmacen toProd, double separados) throws SQLException {
        String strSQL = "";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                movimientos.Movimientos.grabaProductoAlmacen(cn, toProd);

                strSQL = "UPDATE movimientosDetalle\n"
                        + "SET cantFacturada=cantFacturada-" + separados + "+" + toProd.getCantidad() + "\n"
                        + "WHERE idMovto=" + idMovto + " AND idEmpaque=" + toProd.getIdProducto();
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
    
    public TORecepcionProductoAlmacen construirProductoAlmacen(ResultSet rs) throws SQLException {
        TORecepcionProductoAlmacen toProd = new TORecepcionProductoAlmacen();
//        toProd.setCantSolicitada(rs.getDouble("cantSolicitada"));
        toProd.setCantTraspasada(rs.getDouble("cantTraspasada"));
        movimientos.Movimientos.construirProductoAlmacen(rs, toProd);
        return toProd;
    }

    public ArrayList<TORecepcionProductoAlmacen> obtenerDetalleProducto(int idMovtoAlmacen, int idProducto) throws SQLException {
        String strSQL = "SELECT DT.cantidad AS cantTraspasada, D.*, A.fechaCaducidad\n"
                + "FROM movimientosDetalleAlmacen D\n"
                + "INNER JOIN movimientosAlmacen M ON M.idMovtoAlmacen=D.idMovtoAlmacen\n"
                + "INNER JOIN movimientosAlmacen T ON T.idMovtoAlmacen=M.referencia\n"
                + "LEFT JOIN movimientosDetalleAlmacen DT ON DT.idMovtoAlmacen=T.idMovtoAlmacen AND DT.idEmpaque=D.idEmpaque AND DT.lote=D.lote\n"
                + "INNER JOIN almacenesLotes A ON A.idAlmacen=T.idAlmacen AND A.idEmpaque=DT.idEmpaque AND A.lote=DT.lote\n"
                + "WHERE D.idMovtoAlmacen=" + idMovtoAlmacen + " AND D.idEmpaque=" + idProducto;
        ArrayList<TORecepcionProductoAlmacen> detalle = new ArrayList<>();
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    detalle.add(this.construirProductoAlmacen(rs));
                }
            }
        }
        return detalle;
    }
    
    private TORecepcionProducto construirProducto(ResultSet rs) throws SQLException {
        TORecepcionProducto to = new TORecepcionProducto();
        to.setCantSolicitada(rs.getInt("cantSolicitada"));
        to.setCantTraspasada(rs.getDouble("cantTraspasada"));
        movimientos.Movimientos.construirProductoOficina(rs, to);
        return to;
    }

    public ArrayList<TORecepcionProducto> obtenerDetalle(int idMovto) throws SQLException {
        ArrayList<TORecepcionProducto> productos = new ArrayList<>();
        String strSQL = "SELECT D.*, TD.cantFacturada AS cantTraspasada, S.cantSolicitada\n"
                + "FROM movimientosDetalle D\n"
                + "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                + "INNER JOIN movimientosDetalle TD ON TD.idMovto=M.referencia AND TD.idEmpaque=D.idEmpaque\n"
                + "INNER JOIN movimientos T ON T.idMovto=TD.idMovto\n"
                + "INNER JOIN solicitudesDetalle S ON S.idMovto=T.referencia AND S.idEmpaque=D.idEmpaque\n"
                + "WHERE D.idMovto=" + idMovto;
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
    
    private TORecepcion construir(ResultSet rs) throws SQLException {
        TORecepcion toMov = new TORecepcion();
        toMov.setTraspasoFolio(rs.getInt("traspasoFolio"));
        toMov.setTraspasoFecha(new java.util.Date(rs.getTimestamp("traspasoFecha").getTime()));
        toMov.setSolicitudFolio(rs.getInt("solicitudFolio"));
        toMov.setSolicitudFecha(new java.util.Date(rs.getTimestamp("solicitudFecha").getTime()));
        movimientos.Movimientos.construirMovimientoOficina(rs, toMov);
        return toMov;
    }
    
    public ArrayList<TORecepcion> obtenerRecepciones(int idAlmacen, int estatus, Date fechaInicial) throws SQLException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TORecepcion> movimientos = new ArrayList<>();
        String strSQL = "SELECT M.*\n"
                + "     , T.folio AS traspasoFolio, T.fecha AS traspasoFecha\n"
                + "     , S.folio AS solicitudFolio, S.fecha AS solicitudFecha\n"
                + "FROM movimientos M\n"
                + "INNER JOIN movimientos T ON T.idMovto=M.referencia\n"
                + "INNER JOIN solicitudes S ON S.idMovto=T.referencia\n"
                + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=9 AND M.estatus=" + estatus + "\n"
                + "         AND CONVERT(date, M.fecha) <= '" + format.format(fechaInicial) + "'\n"
                + "ORDER BY M.fecha";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    movimientos.add(this.construir(rs));
                }
            }
        }
        return movimientos;
    }
}
