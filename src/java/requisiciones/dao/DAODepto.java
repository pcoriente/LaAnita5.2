/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package requisiciones.dao;

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
import requisiciones.dominio.Depto;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author daap
 */
public class DAODepto {

    private DataSource ds = null;

    public DAODepto() throws NamingException {
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

    public ArrayList<Depto> obtenerDeptos() throws SQLException {
        ArrayList<Depto> dp = new ArrayList<Depto>();
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery("SELECT * FROM empleadosDeptos");
            while (rs.next()) {
                dp.add(construirDepto(rs));
            }
        } finally {
            cn.close();
        }
        return dp;
    }

    public Depto obtenerDeptos(int idDepto) throws SQLException {
        Depto dp = null;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery("SELECT * FROM empleadosDeptos WHERE idDepto=" + idDepto);
            if (rs.next()) {
                dp = construirDepto(rs);
            }
        } finally {
            cn.close();
        }
        return dp;
    }

    private Depto construirDepto(ResultSet rs) throws SQLException {
        Depto dp = new Depto();
        dp.setIdDepto(rs.getInt("idDepto"));
        dp.setDepto(rs.getString("depto"));

        return dp;
    }

    public Depto obtenerDeptoConverter(int idDepto) throws SQLException {
        Depto dep = null;
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery("SELECT * FROM empleadosDeptos WHERE idDepto=" + idDepto);
            if (rs.next()) {
                dep = construirDepto(rs);
            }
        } finally {
            cn.close();
        }
        return dep;
    }

    
}
