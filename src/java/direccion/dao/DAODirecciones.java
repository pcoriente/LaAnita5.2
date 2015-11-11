package direccion.dao;

import direccion.dominio.Direccion;
import java.sql.Connection;
import java.sql.SQLException;
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
 * @author Julio
 */
public class DAODirecciones {

    private DataSource ds;

    public DAODirecciones() throws NamingException {
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

    public Direccion obtener(int idDireccion) throws SQLException {
        Direccion d;
        try (Connection cn = this.ds.getConnection()) {
            d = direccion.Direcciones.obtener(cn, idDireccion);
        }
        return d;
    }

    public void eliminar(int idDireccion) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            direccion.Direcciones.eliminar(cn, idDireccion);
        }
    }

//    public void modificar(int idDireccion, String calle, String numeroExterior, String numeroInterior, String referencia, int idPais, String codigoPostal, String estado, String municipio, String localidad, String colonia, String numeroLocalizacion) throws SQLException {
    public void modificar(Direccion d) throws SQLException {
        try (Connection cn = this.ds.getConnection()) {
            direccion.Direcciones.modificar(cn, d);
        }
    }

//    public int agregar(String calle, String numeroExterior, String numeroInterior, String referencia, int idPais, String codigoPostal, String estado, String municipio, String localidad, String colonia, String numeroLocalizacion) throws SQLException {
    public int agregar(Direccion d) throws SQLException {
        int idDireccion = 0;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try {
                direccion.Direcciones.agregar(cn, d);
                idDireccion = d.getIdDireccion();

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw (ex);
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return idDireccion;
    }
}
