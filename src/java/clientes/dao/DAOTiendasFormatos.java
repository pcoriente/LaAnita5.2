package clientes.dao;

import clientes.dominio.TiendaFormato;
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
 * @author jesc
 */
public class DAOTiendasFormatos {
    int idUsuario;
    private DataSource ds = null;

    public DAOTiendasFormatos() throws NamingException {
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
    
    public ArrayList<TiendaFormato> obtenerFormatos(int idGrupoClte) throws SQLException {
        ArrayList<TiendaFormato> lstFormatos = null;
        String sql = "SELECT * FROM  clientesFormatos WHERE idGrupoCte  = '" + idGrupoClte + "'";
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(sql);
            lstFormatos = new ArrayList<TiendaFormato>();
            while (rs.next()) {
                lstFormatos.add(this.construir(rs));
            }
        } finally {
            cn.close();
        }
        return lstFormatos;
    }

    public TiendaFormato obtenerFormato(int id) throws SQLException {
        TiendaFormato formato = new TiendaFormato();
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        String sql = "SELECT * FROM clientesFormato WHERE idFormato = " + id;
        ResultSet rs = st.executeQuery(sql);
        if(rs.next()) {
            formato=this.construir(rs);
        }
        return formato;
    }
    
    private TiendaFormato construir(ResultSet rs) throws SQLException {
        TiendaFormato to=new TiendaFormato();
        to.setIdFormato(rs.getInt("idFormato"));
        to.setFormato(rs.getString("formato"));
        to.setIdGrupoCte(rs.getInt("idGrupoCte"));
        return to;
    }
    
     public void modificar(TiendaFormato formato) throws SQLException {
        String sql = "UPDATE clientesFormatos set formato = '" + formato.getFormato() + "' WHERE idFormato ='" + formato.getIdFormato() + "'";
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate(sql);
        } finally {
            cn.close();
        }
    }
    
     public int agregar(TiendaFormato formato) throws SQLException {
        int idFormato=0;
        String strSQL = "INSERT INTO clientesFormato (formato, idGrupoCte) VALUES ('" + formato.getFormato() + "', '" + formato.getIdGrupoCte() + "')";
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");
            
            st.executeUpdate(strSQL);
            ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idFormato");
            if(rs.next()) {
                idFormato=rs.getInt("idFormato");
            }
            st.executeUpdate("COMMIT TRANSACTION");
        } catch(SQLException e) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw e;
        } finally {
            cn.close();
        }
        return idFormato;
    }
}
