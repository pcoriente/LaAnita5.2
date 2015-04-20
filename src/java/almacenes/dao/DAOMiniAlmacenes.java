package almacenes.dao;

import almacenes.dominio.MiniAlmacen;
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
public class DAOMiniAlmacenes {
    int idCedis;
    private DataSource ds;

    public DAOMiniAlmacenes() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
        this.idCedis = usuarioSesion.getUsuario().getIdCedis();

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
    }
    
    public MiniAlmacen obtenerAlmacen(int idAlmacen) throws SQLException {
        MiniAlmacen mini = null;
        
        String strSQL = "SELECT A.idAlmacen, A.almacen, E.idEmpresa, E.nombreComercial AS empresa " +
                        "FROM almacenes A " +
                        "INNER JOIN empresasGrupo E ON E.idEmpresa=A.idEmpresa " +
                        "WHERE A.idAlmacen="+idAlmacen;
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                mini=construir(rs);
            }
        } finally {
            st.close();
            cn.close();
        }
        return mini;
    }
    
    public ArrayList<MiniAlmacen> obtenerAlmacenesCedis() throws SQLException {
        ArrayList<MiniAlmacen> lista = new ArrayList<MiniAlmacen>();
        String strSQL = "SELECT A.idAlmacen, A.almacen, E.idEmpresa, E.nombreComercial AS empresa " +
                        "FROM almacenes A " +
                        "INNER JOIN empresasGrupo E ON E.idEmpresa=A.idEmpresa " +
                        "WHERE A.idCedis="+this.idCedis+" "+
                        "ORDER BY A.idEmpresa, A.almacen";
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                lista.add(construir(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return lista;
    }
    
    public ArrayList<MiniAlmacen> obtenerAlmacenesCedis(int idCedis) throws SQLException {
        ArrayList<MiniAlmacen> lista = new ArrayList<MiniAlmacen>();
        String strSQL = "SELECT A.idAlmacen, A.almacen, E.idEmpresa, E.nombreComercial AS empresa " +
                        "FROM almacenes A " +
                        "INNER JOIN empresasGrupo E ON E.idEmpresa=A.idEmpresa " +
                        "WHERE A.idCedis="+idCedis+" "+
                        "ORDER BY A.idEmpresa, A.almacen";
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                lista.add(construir(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return lista;
    }
    
    public ArrayList<MiniAlmacen> obtenerAlmacenes(int idEmpresa, int idCedis) throws SQLException {
        ArrayList<MiniAlmacen> lista = new ArrayList<MiniAlmacen>();
        String strSQL = "SELECT A.idAlmacen, A.almacen, E.idEmpresa, E.nombreComercial AS empresa " +
                        "FROM almacenes A " +
                        "INNER JOIN empresasGrupo E ON E.idEmpresa=A.idEmpresa " +
                        "WHERE A.idCedis="+idCedis+" AND A.idEmpresa="+idEmpresa+" " +
                        "ORDER BY A.almacen";
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                lista.add(construir(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return lista;
    }
    
    private MiniAlmacen construir(ResultSet rs) throws SQLException {
        MiniAlmacen mini=new MiniAlmacen();
        mini.setIdAlmacen(rs.getInt("idAlmacen"));
        mini.setAlmacen(rs.getString("almacen"));
        mini.setIdEmpresa(rs.getInt("idEmpresa"));
        mini.setEmpresa(rs.getString("empresa"));
        return mini;
    } 
}
