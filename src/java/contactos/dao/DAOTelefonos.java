package contactos.dao;

import contactos.dominio.Telefono;
import contactos.dominio.TelefonoTipo;
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
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jsolis
 */
public class DAOTelefonos {

    private DataSource ds;

    public DAOTelefonos() throws NamingException {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");

            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
        } catch (NamingException ex) {
            throw (ex);
        }
    }

    public void eliminarTipo(int idTipo) throws SQLException, Exception {
        int total = 0;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        String strSQL = "DELETE FROM telefonosTipos WHERE idTipo=" + idTipo;
        try {
            st.executeUpdate("begin transaction");
            ResultSet rs = st.executeQuery("SELECT count(*) AS total FROM telefonos WHERE idTipo=" + idTipo);
            if (rs.next()) {
                total = rs.getInt("total");
            }
            if (total == 0) {
                st.executeUpdate(strSQL);
            } else {
                throw new Exception("No se puede eliminar, existen teléfonos de este tipo");
            }
            st.executeUpdate("commit transaction");
        } catch (Exception ex) {
            st.executeUpdate("rollback transaction");
            throw (ex);
        } finally {
            cn.close();
        }
    }

    public void modificarTipo(TelefonoTipo t) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        String strSQL = "UPDATE telefonosTipos "
                + "SET tipo='" + t.getTipo() + "'"
                + "WHERE idTipo=" + t.getIdTipo();
        try {
            st.executeUpdate(strSQL);
        } finally {
            cn.close();
        }
    }

    public int agregarTipo(TelefonoTipo t) throws SQLException {
        int idTipo = 0;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        String strSQL = "INSERT INTO telefonosTipos (tipo, celular) "
                + "VALUES ('" + t.getTipo() + "', " + (t.isCelular() ? 1 : 0) + ")";
        try {
            st.executeUpdate("begin transaction");
            st.executeUpdate(strSQL);
            strSQL = "SELECT @@IDENTITY AS idTipo";
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                idTipo = rs.getInt("idTipo");
            }
            st.executeUpdate("commit transaction");
        } catch (SQLException ex) {
            st.executeUpdate("rollback transaction");
            throw (ex);
        } finally {
            cn.close();
        }
        return idTipo;
    }

    public ArrayList<TelefonoTipo> obtenerTipos(boolean cel) throws SQLException {
        ArrayList<TelefonoTipo> ts = new ArrayList<TelefonoTipo>();
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        String strSQL = "SELECT * FROM telefonosTipos WHERE celular=" + (cel ? 1 : 0);
        try {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                ts.add(construirTipo(rs));
            }
        } finally {
            cn.close();
        }
        return ts;
    }

    public TelefonoTipo obtenerTipo(int idTipo) throws SQLException {
        TelefonoTipo tipo = null;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        String strSQL = "SELECT * FROM telefonosTipos WHERE idTipo=" + idTipo;
        try {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                tipo = construirTipo(rs);
            }
        } finally {
            cn.close();
        }
        return tipo;
    }

    private TelefonoTipo construirTipo(ResultSet rs) throws SQLException {
        TelefonoTipo tipo = new TelefonoTipo(rs.getBoolean("celular"));
        tipo.setIdTipo(rs.getInt("idTipo"));
        tipo.setTipo(rs.getString("tipo"));
        return tipo;
    }

    public void eliminar(int idTelefono) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        String strSQL = "DELETE FROM telefonos WHERE idTelefono=" + idTelefono;
        try {
            st.executeUpdate(strSQL);
        } finally {
            cn.close();
        }
    }

    public void modificar(Telefono t) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        String strSQL = "UPDATE telefonos "
                + "SET lada='" + t.getLada() + "', telefono='" + t.getTelefono() + "', extension='" + t.getExtension() + "', idTipo=" + t.getTipo().getIdTipo() + " "
                + "WHERE idTelefono=" + t.getIdTelefono();
        try {
            st.executeUpdate(strSQL);
        } finally {
            cn.close();
        }
    }

    public int agregar(Telefono t, int idContacto) throws SQLException {
        int idTelefono = 0;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        String strSQL = "INSERT INTO telefonos (lada, telefono, extension, idTipo, idContacto) "
                + "VALUES ('" + t.getLada() + "', '" + t.getTelefono() + "', '" + t.getExtension() + "', " + t.getTipo().getIdTipo() + ", " + idContacto + ")";
        try {
            st.executeUpdate("begin transaction");
            st.executeUpdate(strSQL);
            strSQL = "SELECT @@IDENTITY AS idTelefono";
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                idTelefono = rs.getInt("idTelefono");
            }
            st.executeUpdate("commit transaction");
        } catch (SQLException ex) {
            st.executeUpdate("rollback transaction");
            throw (ex);
        } finally {
            cn.close();
        }
        return idTelefono;
    }

    public int cuentaTelefonos(int idTipo) throws SQLException {
        int total = 0;
        Connection cn = ds.getConnection();
        String strSQL = "SELECT count(*) AS total FROM telefonos WHERE idTipo=" + idTipo;
        try {
            Statement sentencia = cn.createStatement();
            ResultSet rs = sentencia.executeQuery(strSQL);
            if (rs.next()) {
                total = rs.getInt("total");
            }
        } finally {
            cn.close();
        }
        return total;
    }

    public ArrayList<Telefono> obtenerTelefonos(int idContacto) throws SQLException {
        ArrayList<Telefono> cs = new ArrayList<Telefono>();
        Connection cn = ds.getConnection();
        String strSQL = "SELECT t.idTelefono, t.lada, t.telefono, t.extension"
                + ", tt.idTipo, tt.tipo, tt.celular "
                + "FROM telefonos t "
                + "INNER JOIN telefonosTipos tt ON tt.idTipo=t.idTipo "
                + "WHERE idContacto=" + idContacto;
        try {
            Statement sentencia = cn.createStatement();
            ResultSet rs = sentencia.executeQuery(strSQL);
            while (rs.next()) {
                cs.add(construir(rs));
            }
        } finally {
            cn.close();
        }
        return cs;
    }

    public Telefono obtenerTelefono(int idTelefono) throws SQLException {
        Telefono t = null;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        String strSQL = "SELECT t.idTelefono, t.lada, t.telefono, t.extension"
                + ", tt.idTipo, tt.tipo, tt.celular "
                + "FROM telefonos t "
                + "INNER JOIN telefonosTipos tt ON tt.idTipo=t.idTipo "
                + "WHERE idTelefono=" + idTelefono;
        try {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                t = construir(rs);
            }
        } finally {
            cn.close();
        }
        return t;
    }

    private Telefono construir(ResultSet rs) throws SQLException {
        Telefono t = new Telefono();
        t.setIdTelefono(rs.getInt("idTelefono"));
        t.setLada(rs.getString("lada"));
        t.setTelefono(rs.getString("telefono"));
        t.setExtension(rs.getString("extension"));
        TelefonoTipo tipo = new TelefonoTipo(rs.getBoolean("celular"));
        tipo.setIdTipo(rs.getInt("idTipo"));
        tipo.setTipo(rs.getString("tipo"));
        t.setTipo(tipo);
        return t;
    }

    public void guardarTelefonos(ArrayList<Telefono> listaTelefonos, int idContacto) throws SQLException {
        PreparedStatement ps = null;
        Connection cn = ds.getConnection();
        for (Telefono telefonos : listaTelefonos) {
            int idTipo = 0;
            if (telefonos.getTipo().isCelular() == false) {
                idTipo = 2;
            } else {
                idTipo = 1;
            }
            String sql = "INSERT INTO telefonos (lada, telefono, extension, idTipo, idContacto) VALUES ("
                    + "'" + telefonos.getLada() + "','" + telefonos.getTelefono() + "','" + telefonos.getExtension() + "','" + idTipo + "','" + idContacto + "') ";
            ps = cn.prepareStatement(sql);
            ps.executeUpdate();
        }
        ps.close();
    }

    public void guardarTelefono(Telefono telefonos, int idContacto) throws SQLException {
        int idTipo = 0;
        Connection cn = ds.getConnection();
        Statement st =  cn.createStatement();
        if (telefonos.getTipo().isCelular() == false) {
            idTipo = 2;
        } else {
            idTipo = 1;
        }
        String sql = "INSERT INTO telefonos (lada, telefono, extension, idTipo, idContacto) VALUES ("
                + "'" + telefonos.getLada() + "','" + telefonos.getTelefono() + "','" + telefonos.getExtension() + "','" + idTipo + "','" + idContacto + "') ";
        st.executeUpdate(sql);
    }
}
