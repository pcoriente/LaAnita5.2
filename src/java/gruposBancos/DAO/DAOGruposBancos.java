/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruposBancos.DAO;

import gruposBancos.Dominio.GruposBancos;
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
 * @author Usuario
 */
public class DAOGruposBancos {

    private DataSource ds = null;

    public DAOGruposBancos() throws NamingException {
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

    public ArrayList<GruposBancos> dameGruposBancos(int idGrupoCte) throws SQLException {
        ArrayList<GruposBancos> lst = new ArrayList<GruposBancos>();
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        String sql = "SELECT * FROM gruposBancos WHERE idGrupoCte = '" + idGrupoCte + "'";
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            GruposBancos grupos = new GruposBancos();
            grupos.setIdGrupoBanco(rs.getInt("idGrupoCte"));
            grupos.setMedioPago(rs.getString("medioPago"));
            grupos.setNumCtaPago(rs.getString("numCtaPago"));
            lst.add(grupos);
        }
        return lst;
    }

}
