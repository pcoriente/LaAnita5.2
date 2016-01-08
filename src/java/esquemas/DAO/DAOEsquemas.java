/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esquemas.DAO;

import esquemas.Dominio.EsquemaNegociacion;
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
 * @author Torres
 */
public class DAOEsquemas {

    private DataSource ds = null;
    int idUsuario;

    public DAOEsquemas() throws NamingException {
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

    public ArrayList<EsquemaNegociacion> dameEsquemas() throws SQLException {
        ArrayList<EsquemaNegociacion> lst = new ArrayList<>();
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        String sql ="SELECT * FROM esquemaNegociacion";
        ResultSet rs = st.executeQuery(sql);
        while(rs.next()){
            EsquemaNegociacion esquema = new EsquemaNegociacion();
            esquema.setIdEsquema(rs.getInt("idEsquema"));
            esquema.setEsquema(rs.getString("esquema"));
            lst.add(esquema);
        }
        return lst;
    }

}
