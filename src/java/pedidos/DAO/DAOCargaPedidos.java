/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pedidos.DAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import leyenda.dao.DAOBancosLeyendas;
import pedidos.dominio.EntregasWallMart;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author Usuario
 */
public class DAOCargaPedidos {

    protected static DataSource ds;

    public DAOCargaPedidos() {

        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");

            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
        } catch (NamingException ex) {
            Logger.getLogger(DAOBancosLeyendas.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public HashMap leeEntregasWallMart() throws SQLException {
        Connection cn = ds.getConnection();
        HashMap glns = new HashMap();
        Statement st = cn.createStatement();
        String sql = "SELECT  * FROM entregasWallMart";
        try {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                EntregasWallMart entregasWallMart = new EntregasWallMart();
                entregasWallMart.setIdGln(rs.getString("idGln"));
                entregasWallMart.setIdTienda(rs.getString("idTienda"));
                String cIdGln = entregasWallMart.getIdGln().substring(8, 13);
                String cIdTienda = entregasWallMart.getIdTienda();
                glns.put(cIdGln, cIdTienda);
            }
        } finally {
            cn.close();
        }
        return glns;
    }

}
