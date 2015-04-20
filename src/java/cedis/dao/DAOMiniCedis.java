package cedis.dao;

import cedis.dominio.MiniCedis;
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
 * @author jsolis
 */
public class DAOMiniCedis {
    private int idZona;
    private int idCedis;
    private DataSource ds;
    
    public DAOMiniCedis() throws NamingException {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
            this.idZona=usuarioSesion.getUsuario().getIdCedisZona();
            this.idCedis=usuarioSesion.getUsuario().getIdCedis();
            
            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/"+usuarioSesion.getJndi());
        } catch (NamingException ex) {
            throw(ex);
        }
    }
    
    public MiniCedis obtenerDefaultMiniCedis() throws SQLException {
        MiniCedis to=null;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery("SELECT idCedis, cedis FROM cedis WHERE idCedis="+this.idCedis);
            if(rs.next()) {
                to=construirMini(rs);
            }
        } finally {
            cn.close();
        }
        return to;
    }
    
    public MiniCedis obtenerMiniCedis(int idCedis) throws SQLException {
        MiniCedis to=null;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery("SELECT idCedis, cedis FROM cedis WHERE idCedis="+idCedis);
            if(rs.next()) {
                to=construirMini(rs);
            }
        } finally {
            cn.close();
        }
        return to;
    }
    
    public ArrayList<MiniCedis> obtenerListaMiniCedisTodos() throws SQLException {
        ArrayList<MiniCedis> lstMiniCedis=new ArrayList<MiniCedis>();
        
        Connection cn=ds.getConnection();
        Statement sentencia = cn.createStatement();
        String strSQL="select * from cedis order by idCedis";
        try {
            ResultSet rs = sentencia.executeQuery(strSQL);
            while(rs.next()) {
                lstMiniCedis.add(construirMini(rs));
            }
        } finally {
            cn.close();
        }
        return lstMiniCedis;
    }
    
    public ArrayList<MiniCedis> obtenerListaMiniCedisZona() throws SQLException {
        ArrayList<MiniCedis> lstMiniCedis=new ArrayList<MiniCedis>();
        
        Connection cn=ds.getConnection();
        Statement sentencia = cn.createStatement();
        String strSQL="select * \n" +
                        "from cedisZonasDetalle z\n" +
                        "inner join cedis c on c.idCedis=z.idCedis\n" +
                        "where z.idZona="+this.idZona;
        try {
            ResultSet rs = sentencia.executeQuery(strSQL);
            while(rs.next()) {
                lstMiniCedis.add(construirMini(rs));
            }
        } finally {
            cn.close();
        }
        return lstMiniCedis;
    }
    
//    public ArrayList<MiniCedis> obtenerTodasListaMiniCedis() throws SQLException {
//        ArrayList<MiniCedis> lstMiniCedis=new ArrayList<MiniCedis>();
//        
//        Connection cn=ds.getConnection();
//        Statement sentencia = cn.createStatement();
//        String strSQL="select * from cedis" ;
//        try {
//            ResultSet rs = sentencia.executeQuery(strSQL);
//            while(rs.next()) {
//                lstMiniCedis.add(construirMini(rs));
//            }
//        } finally {
//            cn.close();
//        }
//        return lstMiniCedis;
//    }
    
    private MiniCedis construirMini(ResultSet rs) throws SQLException {
        MiniCedis to=new MiniCedis();
        to.setIdCedis(rs.getInt("idCedis"));
        to.setCedis(rs.getString("cedis"));
        return to;
    }
}
