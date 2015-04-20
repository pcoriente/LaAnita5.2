/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package menuClientesGrupos.dao;

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
import menuClientesGrupos.dominio.ClienteGrupo;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author Usuario
 */
public class DAOClientesGrupo {

    private DataSource ds;
    private int idPerfil = 0;

    public DAOClientesGrupo() throws NamingException {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
            if (usuarioSesion.getUsuario() == null) {
                idPerfil = 0;
            } else {
                idPerfil = usuarioSesion.getUsuario().getIdPerfil();
            }
            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
        } catch (NamingException ex) {
            throw (ex);
        }
    }

    public ArrayList<ClienteGrupo> dameListaClientesGrupos() throws SQLException {
        ArrayList<ClienteGrupo> lstClientesGrupos = new ArrayList();
        ResultSet rs = null;
        String sql = "SELECT * FROM clientesGrupos";
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            rs = st.executeQuery(sql);
            while (rs.next()) {
                ClienteGrupo clientesGrupos = new ClienteGrupo();
                clientesGrupos.setIdGrupoCte(rs.getInt("idGrupoCte"));
                clientesGrupos.setGrupoCte(rs.getString("grupoCte"));
                clientesGrupos.setCodigoGrupo(rs.getString("codigoGrupo"));
                lstClientesGrupos.add(clientesGrupos);
            }
        } finally {
            st.close();
            rs.close();
            cn.close();
        }
        return lstClientesGrupos;
    }

    public void guardarClientesGrupo(ClienteGrupo clientesGrupos) throws SQLException {
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        String sql = "INSERT INTO clientesGrupos (grupoCte,codigoGrupo) VALUES('" + clientesGrupos.getGrupoCte() + "','" + clientesGrupos.getCodigoGrupo() + "')";
        try {
            st.executeUpdate(sql);
        } finally {
            st.close();
            cn.close();
        }
    }

    public void actualizar(ClienteGrupo clientesGrupos) throws SQLException {
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        String sql = "UPDATE clientesGrupos set grupoCte = '" + clientesGrupos.getGrupoCte() + "', codigoGrupo='" + clientesGrupos.getCodigoGrupo() + "' WHERE idGrupoCte = '" + clientesGrupos.getIdGrupoCte() + "'";
        try {
            st.executeUpdate(sql);
        } finally {
            st.close();
            cn.close();
        }
    }

    public ClienteGrupo dameClientesGrupo(int id) throws SQLException {
        ClienteGrupo clientesGrupos = new ClienteGrupo();
        ResultSet rs = null;
        String sql = "SELECT * FROM clientesGrupos WHERE idGrupoCte = '" + id + "'";
        Connection c = ds.getConnection();
        Statement st = c.createStatement();
        try {
            rs = st.executeQuery(sql);
            while (rs.next()) {
                clientesGrupos.setIdGrupoCte(rs.getInt("idGrupoCte"));
                clientesGrupos.setGrupoCte(rs.getString("grupoCte"));
            }
        } finally {
            st.close();
            rs.close();
            c.close();
        }
        return clientesGrupos;
    }
}
