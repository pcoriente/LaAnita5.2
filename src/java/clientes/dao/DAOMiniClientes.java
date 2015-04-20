package clientes.dao;

import clientes.dominio.MiniCliente;
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
public class DAOMiniClientes {
    private int idCedis;
    private DataSource ds = null;

    public DAOMiniClientes() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
        this.idCedis=usuarioSesion.getUsuario().getIdCedis();
    }
    
    public ArrayList<MiniCliente> obtenerClientesGrupo(int idGrupo) throws SQLException {
        ArrayList<MiniCliente> tos=new ArrayList<MiniCliente>();
//        String strSQL="SELECT DISTINCT C.idCliente, Y.idContribuyente, Y.contribuyente, F.idFormato, F.formato " +
//                    "FROM clientes C " +
//                    "INNER JOIN contribuyentes Y ON Y.idContribuyente=C.idContribuyente " +
//                    "INNER JOIN clientesFormatos F ON F.idCliente=C.idCliente " +
//                    "WHERE C.idGrupoCte="+idGrupo;
        String strSQL="SELECT DISTINCT C.idCliente, Y.idContribuyente, Y.contribuyente " +
                    "FROM clientes C " +
                    "INNER JOIN contribuyentes Y ON Y.idContribuyente=C.idContribuyente " +
                    "WHERE C.idGrupoCte="+idGrupo;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                tos.add(this.construir(rs));
            }
        } finally {
            cn.close();
        }
        return tos;
    }
    
    public MiniCliente obtenerCliente(int idCliente) throws SQLException {
        MiniCliente to=null;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
//        String strSQL="SELECT C.idCliente, Y.idContribuyente, Y.contribuyente, F.idFormato, F.formato " +
//                    "FROM clientes C " +
//                    "INNER JOIN contribuyentes Y ON Y.idContribuyente=C.idContribuyente " +
//                    "INNER JOIN clientesFormatos F ON F.idCliente=C.idCliente " +
//                    "WHERE C.idCliente="+idCliente;
        String strSQL="SELECT C.idCliente, Y.idContribuyente, Y.contribuyente " +
                    "FROM clientes C " +
                    "INNER JOIN contribuyentes Y ON Y.idContribuyente=C.idContribuyente " +
                    "WHERE C.idCliente="+idCliente;
        try {
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                to=this.construir(rs);
            }
        } finally {
            cn.close();
        }
        return to;
    }
    
    public ArrayList<MiniCliente> obtenerClientesCedis() throws SQLException {
        ArrayList<MiniCliente> tos=new ArrayList<MiniCliente>();
//        String strSQL="SELECT DISTINCT C.idCliente, Y.idContribuyente, Y.contribuyente, F.idFormato, F.formato " +
//                    "FROM clientes C " +
//                    "INNER JOIN contribuyentes Y ON Y.idContribuyente=C.idContribuyente " +
//                    "INNER JOIN clientesFormatos F ON F.idCliente=C.idCliente " +
//                    "INNER JOIN clientesTiendas T ON T.idCliente=C.idCliente" +
//                    "INNER JOIN agentes A ON A.idAgente=T.idAgente " +
//                    "WHERE A.idCedis="+this.idCedis;
        String strSQL="SELECT DISTINCT C.idCliente, Y.idContribuyente, Y.contribuyente " +
                    "FROM clientes C " +
                    "INNER JOIN contribuyentes Y ON Y.idContribuyente=C.idContribuyente " +
                    "INNER JOIN clientesTiendas T ON T.idCliente=C.idCliente" +
                    "INNER JOIN agentes A ON A.idAgente=T.idAgente " +
                    "WHERE A.idCedis="+this.idCedis;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                tos.add(this.construir(rs));
            }
        } finally {
            cn.close();
        }
        return tos;
    }
    
    public MiniCliente construir(ResultSet rs) throws SQLException {
        MiniCliente mini=new MiniCliente();
        mini.setIdCliente(rs.getInt("idCliente"));
        mini.setIdContribuyente(rs.getInt("idContribuyente"));
        mini.setContribuyente(rs.getString("contribuyente"));
//        mini.setIdFormato(rs.getInt("idFormato"));
//        mini.setFormato(rs.getString("formato"));
        return mini;
    }
}
