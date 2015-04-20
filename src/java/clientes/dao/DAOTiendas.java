package clientes.dao;

import clientes.dominio.MiniTienda;
import clientes.to.TOTienda;
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
public class DAOTiendas {
    private DataSource ds = null;

    public DAOTiendas() throws NamingException {
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
    
    public TOTienda obtenerTienda(int idTienda) throws SQLException {
        TOTienda tienda=null;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        String strSQL="SELECT idCliente, codigoTienda, nombreComercial, idDireccion "
                + "FROM clientes "
                + "WHERE idCliente="+idTienda;
        try {
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                tienda=this.construir(rs);
            }
        } finally {
            cn.close();
        }
        return tienda;
    }
    
    public ArrayList<TOTienda> obtenerTiendas(int idCliente) throws SQLException {
        ArrayList<TOTienda> tiendas=new ArrayList<TOTienda>();
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        String strSQL="SELECT idCliente, codigoTienda, nombreComercial, idDireccion "
                + "FROM clientes "
                + "WHERE idContribuyente="+idCliente+" "
                + "ORDER BY nombreComercial";
        try {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                tiendas.add(this.construir(rs));
            }
        } finally {
            cn.close();
        }
        return tiendas;
    }
    
    private TOTienda construir(ResultSet rs) throws SQLException {
        TOTienda to=new TOTienda();
        to.setIdTienda(rs.getInt("idCliente"));
        to.setCodigoTienda(rs.getString("codigoTienda"));
        to.setTienda(rs.getString("nombreComercial"));
        to.setIdDireccion(rs.getInt("idDireccion"));
        return to;
    }
    
    public ArrayList<MiniTienda> obtenerMiniTiendas(int idCliente) throws SQLException {
        ArrayList<MiniTienda> tiendas=new ArrayList<MiniTienda>();
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        String strSQL="SELECT idCliente, codigoTienda, nombreComercial, idDireccion "
                + "FROM clientes "
                + "WHERE idContribuyente="+idCliente+" "
                + "ORDER BY nombreComercial";
        try {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                tiendas.add(this.construirMini(rs));
            }
        } finally {
            cn.close();
        }
        return tiendas;
    }
    
    public MiniTienda obtenerMiniTienda(int idTienda) throws SQLException {
        MiniTienda tienda=null;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        String strSQL="SELECT idCliente, codigoTienda, nombreComercial, idDireccion "
                + "FROM clientes "
                + "WHERE idCliente="+idTienda;
        try {
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                tienda=this.construirMini(rs);
            }
        } finally {
            cn.close();
        }
        return tienda;
    }
    
    private MiniTienda construirMini(ResultSet rs) throws SQLException {
        MiniTienda to=new MiniTienda();
        to.setIdTienda(rs.getInt("idCliente"));
        to.setCodigoTienda(rs.getString("codigoTienda"));
        to.setTienda(rs.getString("nombreComercial"));
        return to;
    }
}
