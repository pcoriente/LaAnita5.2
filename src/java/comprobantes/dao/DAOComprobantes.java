package comprobantes.dao;

import comprobantes.to.TOComprobante;
import java.sql.Connection;
import java.sql.Date;
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
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOComprobantes {

    int idUsuario;
    private DataSource ds = null;

    public DAOComprobantes() throws NamingException {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
            this.idUsuario = usuarioSesion.getUsuario().getId();

            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
        } catch (NamingException ex) {
            throw (ex);
        }
    }

    public void eliminar(int idComprobante) throws SQLException {
        String strSQL = "SELECT M.idMovto\n"
                + "FROM movimientos M\n"
                + "INNER JOIN comprobantes C ON C.idComprobante=M.idComprobante\n"
                + "WHERE C.idComprobante=" + idComprobante;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    throw new SQLException("El comprobante no puede ser eliminado, esta siendo utilizado !!!");
                }
                strSQL = "SELECT M.idMovtoAlmacen\n"
                        + "FROM movimientosAlmacen M\n"
                        + "INNER JOIN comprobantes C ON C.idComprobante=M.idComprobante\n"
                        + "WHERE C.idComprobante=" + idComprobante;
                rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    throw new SQLException("El comprobante no puede ser eliminado, utilizado en movimientos de almacen !!!");
                }
                strSQL="DELETE FROM comprobantes WHERE idComprobante=" + idComprobante;
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

    public void liberaComprobante(int idComprobante) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                st.executeUpdate("UPDATE comprobantes SET propietario=0 WHERE idComprobante=" + idComprobante);
            }
        }
    }

    public boolean asegurarComprobante(int idComprobante) throws SQLException {
        boolean ok = true;
        int propietario = 0;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery("SELECT propietario, estatus FROM comprobantes WHERE idComprobante=" + idComprobante);
                if (rs.next()) {
//                    if (rs.getInt("estatus") == 7) {
//                        ok = false;
//                    } else {
                        propietario = rs.getInt("propietario");
                        if (propietario == 0) {
                            st.executeUpdate("UPDATE comprobantes SET propietario=" + this.idUsuario + " WHERE idComprobante=" + idComprobante);
                        } else if (propietario != this.idUsuario) {
                            ok = false;
                        }
//                    }
                } else {
                    throw new SQLException("No se encotro el comprobante");
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return ok;
    }

    public void modificar(TOComprobante to) throws SQLException {
        to.setIdUsuario(this.idUsuario);
        Date fechaFactura = new java.sql.Date(to.getFecha().getTime());
        String strSQL = "UPDATE comprobantes "
                + "SET serie='" + to.getSerie() + "', numero='" + to.getNumero() + "', fecha='" + fechaFactura.toString() + "', idUsuario=" + to.getIdUsuario() + "\n"
                + "WHERE idComprobante=" + to.getIdComprobante();
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);
            }
        }
    }

    public int agregar(TOComprobante to) throws SQLException {
        int idComprobante = 0;
        to.setEstatus(5);
        to.setIdUsuario(this.idUsuario);
        to.setPropietario(this.idUsuario);
        Date fechaFactura = new java.sql.Date(to.getFecha().getTime());
        String strSQL = "INSERT INTO comprobantes (idTipoMovto, idReferencia, tipo, serie, numero, fecha, idMoneda, idUsuario, propietario, estatus) "
                + "VALUES (" + to.getIdTipoMovto() + ", " + to.getIdReferencia() + ", " + to.getTipo() + ", '" + to.getSerie() + "', '" + to.getNumero() + "', '" + fechaFactura.toString() + "', " + to.getIdMoneda() + ", " + to.getIdUsuario() + ", " + to.getPropietario() + ", " + to.getEstatus() + ")";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);
                ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idComprobante");
                if (rs.next()) {
                    idComprobante = rs.getInt("idComprobante");
                }
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                if (e.getErrorCode() == 2601) {
                    throw new SQLException("El comprobante ya existe !!!");
                } else {
                    throw (e);
                }
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return idComprobante;
    }

    public ArrayList<TOComprobante> completeComprobantes(int idTipoMovto, int idReferencia, String strComprobante) throws SQLException {
        ArrayList<TOComprobante> comprobantes = new ArrayList<>();
        String strSQL = "SELECT *\n"
                + "FROM comprobantes\n"
                + "WHERE idTipoMovto=" + idTipoMovto + " AND idReferencia=" + idReferencia + " AND numero like '%" + strComprobante + "%'\n"
                + "ORDER BY numero";
        try (Connection cn = ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    comprobantes.add(this.construir(rs));
                }
            }
        }
        return comprobantes;
    }

    public ArrayList<TOComprobante> obtenerComprobantes(int idTipoMovto, int idReferencia) throws SQLException {
        ArrayList<TOComprobante> comprobantes = new ArrayList<>();
        String strSQL = "SELECT * FROM comprobantes\n"
                + "WHERE idTipoMovto=" + idTipoMovto + " AND idReferencia=" + idReferencia + "\n"
                + "ORDER BY comprobante";
        try (Connection cn = ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    comprobantes.add(this.construir(rs));
                }
            }
        }
        return comprobantes;
    }

//    public TOComprobante obtenerComprobante(int tipo, String serie, String numero) throws SQLException {
//        TOComprobante c = null;
//        String strSQL = "SELECT * FROM comprobantes WHERE tipo=" + tipo + " AND serie='" + serie + "' AND numero='" + numero + "'";
//        try (Connection cn = this.ds.getConnection()) {
//            try (Statement st = cn.createStatement()) {
//                ResultSet rs = st.executeQuery(strSQL);
//                if (rs.next()) {
//                    c = construir(rs);
//                }
//            }
//        }
//        return c;
//    }
    public TOComprobante obtenerComprobante(int idComprobante) throws SQLException {
        TOComprobante f = null;
        String strSQL = "SELECT * FROM comprobantes WHERE idComprobante=" + idComprobante;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    f = construir(rs);
                }
            }
        }
        return f;
    }

//    public ArrayList<TOComprobante> obtenerComprobantes(int tipo, Date fechaInicial, Date fechaFinal) throws SQLException {
//        ArrayList<TOComprobante> comprobantes = new ArrayList<>();
//        String strSQL = "SELECT *\n"
//                + "FROM comprobantes\n"
//                + "WHERE tipo=" + tipo + " AND FECHA BETWEEN '" + fechaInicial + "' AND '" + fechaFinal + "'\n"
//                + "ORDER BY FECHA DESC";
//        try (Connection cn = this.ds.getConnection()) {
//            try (Statement st = cn.createStatement()) {
//                ResultSet rs = st.executeQuery(strSQL);
//                while (rs.next()) {
//                    comprobantes.add(construir(rs));
//                }
//            }
//        }
//        return comprobantes;
//    }
    private TOComprobante construir(ResultSet rs) throws SQLException {
        TOComprobante to = new TOComprobante();
        to.setIdComprobante(rs.getInt("idComprobante"));
        to.setIdTipoMovto(rs.getInt("idTipoMovto"));
        to.setIdReferencia(rs.getInt("idReferencia"));
        to.setTipo(rs.getInt("tipo"));
        to.setSerie(rs.getString("serie"));
        to.setNumero(rs.getString("numero"));
        to.setFecha(new java.util.Date(rs.getTimestamp("fecha").getTime()));
        to.setIdMoneda(rs.getInt("idMoneda"));
        to.setIdUsuario(this.idUsuario);
        to.setPropietario(rs.getInt("propietario"));
        to.setEstatus(rs.getInt("estatus"));
        return to;
    }

    //    public ArrayList<TOComprobante> obtenerSolicitudes(int idAlmacen) throws SQLException {
    //        ArrayList<TOComprobante> comprobantes = new ArrayList<>();
    //        String strSQL = "SELECT c.*\n"
    //                + "FROM movimientos m\n"
    //                + "INNER JOIN comprobantes c ON c.idComprobante=m.idComprobante\n"
    //                + "WHERE m.idTipo=2 AND m.status=1 AND c.idAlmacen=" + idAlmacen + "\n"
    //                + "ORDER BY c.fecha DESC";
    //        try (Connection cn = this.ds.getConnection()) {
    //            try (Statement st = cn.createStatement()) {
    //                ResultSet rs = st.executeQuery(strSQL);
    //                while (rs.next()) {
    //                    comprobantes.add(construir(rs));
    //                }
    //            }
    //        }
    //    }
    //    }
    
    public int getIdUsuario() {
        return idUsuario;
    }
}
