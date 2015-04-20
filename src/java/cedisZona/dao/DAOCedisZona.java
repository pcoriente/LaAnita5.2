/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cedisZona.dao;

import cedisZona.dominio.CedisZona;
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
 * @author PJGT
 */
public class DAOCedisZona {

    private DataSource ds;

    public DAOCedisZona() throws NamingException {
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

    public ArrayList<CedisZona> dameListaCedisZona() throws SQLException {
        ArrayList<CedisZona> lstCedis = new ArrayList<CedisZona>();
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        ResultSet rs = null;
        String sql = "SELECT * FROM cedisZonas WHERE eliminable =1";
        try {
            rs = st.executeQuery(sql);
            while (rs.next()) {
                CedisZona cedis = new CedisZona();
                cedis.setIdZona(rs.getInt("idZona"));
                cedis.setZona(rs.getString("zona"));
                cedis.setEliminable(rs.getInt("eliminable"));
                lstCedis.add(cedis);
            }
        } finally {
            rs.close();
            cn.close();
            st.close();
        }
        return lstCedis;
    }

    public void guardarCedisZona(CedisZona cedisZona) throws SQLException {
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        String sql = "INSERT INTO cedisZonas (zona, eliminable) VALUES('" + cedisZona.getZona() + "', 1) ";
        try {
            st.executeUpdate(sql);
        } finally {
            st.close();
            cn.close();
        }
    }
}
