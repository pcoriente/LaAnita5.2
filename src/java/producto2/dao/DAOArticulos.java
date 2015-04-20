package producto2.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import producto2.to.TOArticulo;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOArticulos {

    private String tabla = "productos";
    private DataSource ds;

    public DAOArticulos() throws NamingException {
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

    public void eliminar(int idArticulo) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        ResultSet rs = null;
        try {
            st.execute("BEGIN TRANSACTION");
            rs=st.executeQuery("SELECT COUNT(*) AS total FROM empaques WHERE idProducto="+idArticulo);
            if(rs.next()) {
                if(rs.getInt("total")!=0) {
                    throw new SQLException("El articulo esta contenido en varios empaques, no se puede eliminar");
                }
            }
            st.executeUpdate("DELETE FROM productos WHERE idProducto=" + idArticulo);
            st.execute("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.execute("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            rs.close();
            st.close();
            cn.close();
        }
    }

    public void modificar(TOArticulo articulo) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("UPDATE " + this.tabla + " "
                    + "SET idParte=" + articulo.getIdParte() + ", descripcion='" + articulo.getDescripcion() + "', "
                    + "idTipo=" + articulo.getIdTipo() + ", idGrupo=" + articulo.getIdGrupo() + ", "
                    + "idSubGrupo=" + articulo.getIdSubGrupo() + ", "
                    + "idPresentacion=" + articulo.getIdPresentacion() + ", idMarca=" + articulo.getIdMarca() + ", "
                    + "contenido=" + articulo.getContenido() + ", idUnidadMedida=" + articulo.getIdUnidadMedida() + ", idImpuesto= " + articulo.getIdImpuestoGrupo() + " "
                    + "WHERE idProducto=" + articulo.getIdArticulo());
        } finally {
            cn.close();
        }
    }

    public int agregar(TOArticulo articulo) throws SQLException, NamingException {
        int idProducto = 0;
        String strSQL = ""
                + "INSERT INTO " + this.tabla + " (idParte, descripcion, idTipo, idGrupo, idSubGrupo, idMarca, idPresentacion, contenido, idUnidadMedida, idImpuesto) "
                + "VALUES (" + articulo.getIdParte() + ", '" + articulo.getDescripcion() + "', "
                + articulo.getIdTipo() + ", " + articulo.getIdGrupo() + ", " + articulo.getIdSubGrupo() + ", " + articulo.getIdMarca() + ", "
                + articulo.getIdPresentacion() + ", " + articulo.getContenido() + ", " + articulo.getIdUnidadMedida() + ","
                + articulo.getIdImpuestoGrupo() + ")";
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("begin transaction");
            st.executeUpdate(strSQL);
            ResultSet rs = st.executeQuery("SELECT max(idProducto) AS idProducto FROM productos");
            if (rs.next()) {
                idProducto = rs.getInt("idProducto");
            }
            st.executeUpdate("commit transaction");
        } catch (SQLException ex) {
            st.executeUpdate("rollback transaction");
            throw (ex);
        } finally {
            cn.close();
        }
        return idProducto;
    }
}
