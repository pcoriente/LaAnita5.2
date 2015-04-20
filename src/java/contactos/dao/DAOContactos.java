package contactos.dao;

import contactos.dominio.Contacto;
import contactos.dominio.Telefono;
import contactos.dominio.TelefonoTipo;
import java.io.Serializable;
import java.sql.Connection;
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
public class DAOContactos implements Serializable {

    private DataSource ds;

    public DAOContactos() throws NamingException {
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

    public ArrayList<Contacto> obtenerContactos(int idTipo, int idPadre) throws SQLException {
        ArrayList<Contacto> cs = new ArrayList<Contacto>();
        Connection cn = ds.getConnection();
        String strSQL = "SELECT * FROM contactos WHERE idTipo=" + idTipo + " AND idPadre=" + idPadre;
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

    public Contacto obtenerContacto(int idContacto) throws SQLException {
        Contacto c = null;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        String strSQL = "SELECT * FROM contactos WHERE idContacto=" + idContacto;
        try {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                c = construir(rs);
            }
        } finally {
            cn.close();
        }
        return c;
    }

    private Contacto construir(ResultSet rs) throws SQLException {
        Contacto c = new Contacto();
        c.setIdContacto(rs.getInt("idContacto"));
        c.setContacto(rs.getString("contacto"));
        c.setPuesto(rs.getString("puesto"));
        c.setCorreo(rs.getString("correo"));
        return c;
    }

    public void eliminar(int idContacto) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        String strSQL = "DELETE FROM contactos WHERE idContacto=" + idContacto;
        try {
            st.executeUpdate("begin transaction");

            st.executeUpdate(strSQL);
            strSQL = "DELETE FROM telefonos WHERE idContacto=" + idContacto;
            st.executeUpdate(strSQL);
            st.executeUpdate("commit transaction");
        } catch (SQLException ex) {
            st.executeUpdate("rollback transaction");
            throw (ex);
        } finally {
            cn.close();
        }
    }

    public void modificar(Contacto c) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        String strSQL = "UPDATE contactos "
                + "SET contacto='" + c.getContacto() + "', puesto='" + c.getPuesto() + "', correo='" + c.getCorreo() + "' "
                + "WHERE idContacto=" + c.getIdContacto();
        try {
            st.executeUpdate(strSQL);
        } finally {
            cn.close();
        }
    }

    public int agregar(Contacto c, int idPadre, int idTipo) throws SQLException {
        int idContacto = 0;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        String strSQL = "INSERT INTO contactos (contacto, puesto, correo, idTipo, idPadre) "
                + "VALUES ('" + c.getContacto() + "', '" + c.getPuesto() + "', '" + c.getCorreo() + "', " + idTipo + ", " + idPadre + ")";
        try {
            st.executeUpdate("begin transaction");
            st.executeUpdate(strSQL);
            strSQL = "SELECT @@IDENTITY AS idContacto";
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                idContacto = rs.getInt("idContacto");
            }
            st.executeUpdate("commit transaction");
        } catch (SQLException ex) {
            st.executeUpdate("rollback transaction");
            throw (ex);
        } finally {
            cn.close();
        }
        return idContacto;
    }

    public ArrayList<Contacto> obtenerCorreos(int idContacto) throws SQLException {
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        ArrayList<Contacto> lstContacto = new ArrayList<Contacto>();
        String sql = "SELECT idContacto, correo  FROM contactos WHERE idContacto = '" + idContacto+"'";
        try {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Contacto contacto = new Contacto();
                contacto.setIdContacto(rs.getInt("idContacto"));
                contacto.setCorreo(rs.getString("correo"));
                lstContacto.add(contacto);
            }
        } finally {
            cn.close();
        }
        return lstContacto;
    }
}
