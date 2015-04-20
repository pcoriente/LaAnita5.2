package main.dao;

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
import main.dominio.Menu;
import usuarios.dominio.UsuarioSesion;
import usuarios.dominio.Accion;

/**
 *
 * @author JULIOS
 */
public class DAOMenu implements Serializable{
    private DataSource ds;
    private int idPerfil=0;
    
    public DAOMenu() throws NamingException {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
            if(usuarioSesion.getUsuario()==null) {
                idPerfil=0;
            } else {
                idPerfil=usuarioSesion.getUsuario().getIdPerfil();
            }
            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/"+usuarioSesion.getJndi());
        } catch (NamingException ex) {
            throw(ex);
        }
    }
    
    public void cargarUsuarioConfig() throws SQLException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
        if(usuarioSesion.getUsuario()!=null) {
            String strSQL="select idCedis, idCedisZona "
                        + "from usuarioConfig "
                        + "where idUsuario="+usuarioSesion.getUsuario().getId();
            Connection cn=this.ds.getConnection();
            Statement st=cn.createStatement();
            try {
                ResultSet rs=st.executeQuery(strSQL);
                while(rs.next()) {
                    usuarioSesion.getUsuario().setIdCedis(rs.getInt("idCedis"));
                    usuarioSesion.getUsuario().setIdCedisZona(rs.getInt("idCedisZona"));
                }
            } finally {
                cn.close();
            }
        }
    }
    
    public ArrayList<Accion> obtenerAcciones(int idModulo) throws SQLException {
        ArrayList<Accion> acciones = new ArrayList<Accion>();
        String strSQL="select a.idAccion, a.accion, a.idBoton\n" +
                        "from usuarioPerfil up\n" +
                        "inner join webSystem.dbo.acciones a on a.idAccion=up.idAccion\n" +
                        "where up.idPerfil="+this.idPerfil+" and up.idModulo="+idModulo;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                acciones.add(new Accion(rs.getInt("idAccion"), rs.getString("accion"), rs.getString("idBoton")));
            }
        } finally {
            cn.close();
        }
        return acciones;
    }
    
    public ArrayList<Menu> obtenermenu() throws SQLException {
        ArrayList<Menu> menuItems=new ArrayList<Menu>();
        String strSQL="select x.idMenu, mm.menu, x.idSubMenu, isnull(ms.subMenu, '') as subMenu, " +
                                "x.idModulo, m.modulo, m.url\n" +
                        "from (select distinct m.idMenu, m.idSubMenu, m.idModulo \n" +
                        "		from usuarioPerfil p\n" +
                        "		inner join webSystem.dbo.modulos m on m.idModulo=p.idModulo\n" +
                        "		where p.idPerfil="+idPerfil+") x\n" +
                        "inner join webSystem.dbo.modulosMenus mm on mm.idMenu=x.idMenu\n" +
                        "left join webSystem.dbo.modulosSubMenus ms on ms.idSubMenu=x.idSubMenu\n" +
                        "inner join webSystem.dbo.modulos m on m.idModulo=x.idModulo "
                + "order by x.idMenu, x.idSubMenu, x.idModulo";
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                menuItems.add(construir(rs));
            }
        } finally {
            cn.close();
        }
        return menuItems;
    }
    
    private Menu construir(ResultSet rs) throws SQLException {
        Menu menu=new Menu();
        menu.setIdMenu(rs.getInt("idMenu"));
        menu.setMenu(rs.getString("menu"));
        menu.setIdSubMenu(rs.getInt("idSubMenu"));
        menu.setSubMenu(rs.getString("subMenu"));
        menu.setIdModulo(rs.getInt("idModulo"));
        menu.setModulo(rs.getString("modulo"));
        menu.setUrl(rs.getString("url"));
        return menu;
        
    }
}
