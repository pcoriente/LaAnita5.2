package entradas.dao;

import entradas.to.TOComprobante;
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
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("DELETE FROM comprobantes WHERE idComprobante=" + idComprobante);
        } finally {
            st.close();
            cn.close();
        }
    }

    public void liberaComprobante(int idComprobante) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("UPDATE comprobantes SET propietario=0 WHERE idComprobante=" + idComprobante);
        } finally {
            st.close();
            cn.close();
        }
    }

    public boolean asegurarComprobante(int idComprobante) throws SQLException {
        boolean ok = true;
        int propietario = 0;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.execute("BEGIN TRANSACTION");
            ResultSet rs = st.executeQuery("SELECT propietario FROM comprobantes WHERE idComprobante=" + idComprobante);
            if (rs.next()) {
                propietario = rs.getInt("propietario");
            } else {
                throw new SQLException("No se encotro el comprobante");
            }
            if (propietario == 0) {
                st.executeUpdate("UPDATE comprobantes SET propietario=" + this.idUsuario + " WHERE idComprobante=" + idComprobante);
            } else if (propietario != this.idUsuario) {
                ok = false;
            }
            st.execute("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.execute("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            cn.close();
        }
        return ok;
    }

//    public boolean obtenerEstadoAlmacen(int idComprobante) throws SQLException {
//        boolean cerrada=false;   // 0.-Abierta; 1.-Cerrada
//        Connection cn=this.ds.getConnection();
//        Statement st=cn.createStatement();
//        try {
//            ResultSet rs=st.executeQuery("SELECT statusAlmacen FROM comprobantes WHERE idComprobante="+idComprobante);
//            if(rs.next()) {
//                cerrada=rs.getBoolean("statusAlmacen");
//            }
//        } finally {
//            cn.close();
//        }
//        return cerrada;
//    }
//    
//    public boolean obtenerEstadoOficina(int idComprobante) throws SQLException {
//        boolean cerrada=false;   // 0.-Abierta; 1.-Cerrada
//        Connection cn=this.ds.getConnection();
//        Statement st=cn.createStatement();
//        try {
//            ResultSet rs=st.executeQuery("SELECT statusOficina FROM comprobantes WHERE idComprobante="+idComprobante);
//            if(rs.next()) {
//                cerrada=rs.getBoolean("statusOficina");
//            }
//        } finally {
//            cn.close();
//        }
//        return cerrada;
//    }
    public void modificar(TOComprobante to) throws SQLException {
        Date fechaFactura = new java.sql.Date(to.getFecha().getTime());
        String strSQL = "UPDATE comprobantes "
                + "SET serie='" + to.getSerie() + "', numero='" + to.getNumero() + "', fecha='" + fechaFactura.toString() + "' "
                + "WHERE idComprobante=" + to.getIdComprobante();
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate(strSQL);
        } finally {
            st.close();
            cn.close();
        }
    }

    public int agregar(TOComprobante to) throws SQLException {
        int idComprobante = 0;
        Date fechaFactura = new java.sql.Date(to.getFecha().getTime());
        String strSQL = "INSERT INTO comprobantes (idReferencia, tipo, serie, numero, fecha, propietario, idMovto) "
                + "VALUES (" + to.getIdReferencia() + ", " + to.getTipo() + ", '" + to.getSerie() + "', '" + to.getNumero() + "', '" + fechaFactura.toString() + "', " + this.idUsuario + ", 0)";
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

    public ArrayList<TOComprobante> completeComprobantes(int idReferencia, String strComprobante) throws SQLException {
        ArrayList<TOComprobante> comprobantes = new ArrayList<TOComprobante>();
        String strSQL = "SELECT *\n"
                + "FROM comprobantes\n"
                + "WHERE idReferencia=" + idReferencia + " AND numero like '%" + strComprobante + "%'\n"
                + "ORDER BY numero";
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                comprobantes.add(this.construir(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return comprobantes;
    }

    public ArrayList<TOComprobante> obtenerComprobantes(int idReferencia) throws SQLException {
        ArrayList<TOComprobante> comprobantes = new ArrayList<TOComprobante>();
        String strSQL = "SELECT * FROM comprobantes WHERE idReferencia=" + idReferencia + " ORDER BY comprobante";
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                comprobantes.add(this.construir(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return comprobantes;
    }

    public TOComprobante obtenerComprobante(int tipo, String serie, String numero) throws SQLException {
        TOComprobante c = null;
        String strSQL = "SELECT * FROM comprobantes WHERE tipo=" + tipo + " AND serie='" + serie + "' AND numero='" + numero + "'";
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                c = construir(rs);
            }
        } finally {
            st.close();
            cn.close();
        }
        return c;
    }

    public TOComprobante obtenerComprobante(int idComprobante) throws SQLException {
        TOComprobante f = null;
        String strSQL = "SELECT * FROM comprobantes WHERE idComprobante=" + idComprobante;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                f = construir(rs);
            }
        } finally {
            st.close();
            cn.close();
        }
        return f;
    }

    public ArrayList<TOComprobante> obtenerComprobantes(int tipo, Date fechaInicial, Date fechaFinal) throws SQLException {
        ArrayList<TOComprobante> comprobantes = new ArrayList<TOComprobante>();
        String strSQL = "SELECT *\n"
                + "FROM comprobantes\n"
                + "WHERE tipo=" + tipo + " AND FECHA BETWEEN '" + fechaInicial + "' AND '" + fechaFinal + "'\n"
                + "ORDER BY FECHA DESC";
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                comprobantes.add(construir(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return comprobantes;
    }

//    public ArrayList<Comprobante> obtenerComprobantes(int tipo) throws SQLException {
//        ArrayList<Comprobante> comprobantes=new ArrayList<Comprobante>();
//        String strSQL="SELECT * FROM comprobantes WHERE tipo=" + tipo+ " ORDER BY fecha DESC";
//        Connection cn=this.ds.getConnection();
//        Statement st=cn.createStatement();
//        try {
//            ResultSet rs=st.executeQuery(strSQL);
//            while(rs.next()) {
//                comprobantes.add(construir(rs));
//            }
//        } finally {
//            st.close();
//            cn.close();
//        }
//        return comprobantes;
//    }
    private TOComprobante construir(ResultSet rs) throws SQLException {
        TOComprobante to = new TOComprobante();
        to.setIdComprobante(rs.getInt("idComprobante"));
        to.setIdReferencia(rs.getInt("idReferencia"));
        to.setTipo(rs.getInt("tipo"));
        to.setSerie(rs.getString("serie"));
        to.setNumero(rs.getString("numero"));
        to.setFecha(new java.util.Date(rs.getDate("fecha").getTime()));
        to.setPropietario(rs.getInt("propietario"));
        to.setIdMovto(rs.getInt("idMovto"));
        return to;
    }

    public ArrayList<TOComprobante> obtenerSolicitudes(int idAlmacen) throws SQLException {
        ArrayList<TOComprobante> comprobantes = new ArrayList<TOComprobante>();
        String strSQL = "SELECT c.*\n"
                + "FROM movimientos m\n"
                + "INNER JOIN comprobantes c ON c.idComprobante=m.idComprobante\n"
                + "WHERE m.idTipo=2 AND m.status=1 AND c.idAlmacen=" + idAlmacen + "\n"
                + "ORDER BY c.fecha DESC";
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                comprobantes.add(construir(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return comprobantes;
    }
}
