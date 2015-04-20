/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientesBancos.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import leyenda.dominio.ClienteBanco;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author Usuario
 */
public class DAOClientesBancos {

    private DataSource ds = null;

    public DAOClientesBancos() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");

            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
        } catch (NamingException ex) {
            Logger.getLogger(clientesBancos.dao.DAOClientesBancos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void guardarClientesBancos(ClienteBanco clientesBancos) throws SQLException {
        Connection cn = ds.getConnection();
        String sql = "INSERT INTO clientesBancos (codigoCliente, idBanco, numCtaPago, medioPago) "
                + "VALUES('" + clientesBancos.getCodigoCliente() + "', '" + clientesBancos.getIdBanco() + "', '" + clientesBancos.getNumCtaPago() + "', '" + clientesBancos.getMedioPago() + "' )";
        Statement st = cn.createStatement();
        try {
            st.executeUpdate(sql);
        } finally {
            st.close();
            cn.close();
        }
    }

    public ArrayList<ClienteBanco> dameBancos(int codigoCliente) throws SQLException {
        ArrayList<ClienteBanco> lstClientesBancos = new ArrayList<ClienteBanco>();
        String sql = "SELECT * FROM clientesBancos cb "
                + "INNER JOIN bancosSat bs "
                + "on bs.idBanco = cb.idBanco "
                + "WHERE codigoCliente = '" + codigoCliente + "'";
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                ClienteBanco clientes = new ClienteBanco();
                clientes.setIdClienteBanco(rs.getInt("idClienteBanco"));
                clientes.setCodigoCliente(rs.getInt("codigoCliente"));
                clientes.getBancoLeyenda().setIdBanco(rs.getInt("idBanco"));
                clientes.setNumCtaPago(rs.getString("numCtaPago"));
                clientes.setMedioPago(rs.getString("medioPago"));
                clientes.getBancoLeyenda().setNombreCorto(rs.getString("nombreCorto"));
                lstClientesBancos.add(clientes);
            }
        } finally {
            st.close();
            cn.close();
        }
        return lstClientesBancos;
    }

    public ClienteBanco dameClientesBancos(int clienteBanco) throws SQLException {
        ClienteBanco cliente = new ClienteBanco();
        String slq = "SELECT * FROM clientesBancos WHERE idClienteBanco = '" + clienteBanco + "'";
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(slq);
            if (rs.next()) {
                cliente.setCodigoCliente(rs.getInt("codigoCliente"));
                cliente.setIdClienteBanco(rs.getInt("idClienteBanco"));
                cliente.getBancoLeyenda().setIdBanco(rs.getInt("idBanco"));
                cliente.setNumCtaPago(rs.getString("numCtaPago"));
                cliente.setMedioPago(rs.getString("medioPago"));
            }
        } finally {
            st.close();
            cn.close();
        }
        return cliente;

    }

    public void actualizarClientesBancos(ClienteBanco clientesBancos) throws SQLException {
        String sqlActualizar = "UPDATE clientesBancos SET codigoCliente= '" + clientesBancos.getCodigoCliente() + "', idBanco ='" + clientesBancos.getIdBanco() + "', numCtaPago = '" + clientesBancos.getNumCtaPago() + "', medioPago = '" + clientesBancos.getMedioPago() + "'  WHERE idClienteBanco = '" + clientesBancos.getIdClienteBanco() + "'";
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate(sqlActualizar);
        } finally {
            cn.close();
            st.close();
        }
    }
    
    // PARTE QUE SE TRASPASA INICIO, DE DAOClientes de paquete: clientes
    
//    public ArrayList<TOCliente> obtenerCliente() throws SQLException {
//        ArrayList<TOCliente> lista = new ArrayList<TOCliente>();
//        ResultSet rs;
//        Connection cn = ds.getConnection();
//        try {
//            //  String stringSQL = "SELECT * FROM clientesBancos";
//            String stringSQL = "SELECT cb.*, bs.*, cl.* "
//                    + "FROM clientesBancos cb, bancosSat bs, Clientes cl "
//                    + "WHERE cb.idBanco=bs.idBanco "
//                    + "and cl.cod_cli=cb.codigoCliente "
//                    + "ORDER BY nombre";
//            Statement sentencia = cn.createStatement();
//            rs = sentencia.executeQuery(stringSQL);
//            while (rs.next()) {
//                lista.add(construir(rs));
//            }
//        } finally {
//            cn.close();
//        }
//        return lista;
//    }
//
//    public void eliminarUsuario(int id) throws SQLException {
//        Connection cn = null;
//        cn = ds.getConnection();
//        String eliminar = "DELETE FROM clientesBancos WHERE idClienteBanco = ? ";
//        PreparedStatement ps = cn.prepareStatement(eliminar);
//        ps.setInt(1, id);
//        ps.executeUpdate();
//        ps.close();
//    }
//
//    public TOCliente obtenerUnCiente(int idCliente) throws SQLException {
//        TOCliente to = null;
//        Connection cn = this.ds.getConnection();
//        Statement st = cn.createStatement();
//        try {
//            ResultSet rs = st.executeQuery("SELECT cb.*, cl.*,bs.* FROM clientesBancos cb,Clientes cl, bancosSat bs WHERE cb.codigoCliente=cl.cod_cli and cb.idBanco=bs.idBanco and cb.idClienteBanco=" + idCliente);
//            if (rs.next()) {
//                to = construir(rs);
//            }
//        } finally {
//            cn.close();
//        }
//        return to;
//    }
//
//    private TOCliente construir(ResultSet rs) throws SQLException {
//        TOCliente to = new TOCliente();
//        to.setIdCliente(rs.getInt("idClienteBanco"));
//        to.setCodigoCliente(rs.getInt("codigoCliente"));
//        to.setIdBanco(rs.getInt("idBanco"));
////        to.setIdbanco(rs.getInt("idBanco"));
////        to.setNombreCorto(rs.getString("nombreCorto"));
//        to.setNumCtaPago(rs.getString("numCtaPago"));
//        to.setMedioPago(rs.getString("medioPago"));
//        to.setNombre(rs.getString("nombre").trim());
//        return to;
//    }
//
////    private TOCliente construir1(ResultSet rs) throws SQLException {
////        TOCliente to = new TOCliente();
////        to.setIdCliente(rs.getInt("idClienteBanco"));
////        to.setCodigoCliente(rs.getInt("codigoCliente"));
////        to.setIdBanco(rs.getInt("idBanco"));
////        to.setNombre(rs.getString("nombre"));
//////        to.setIdbanco(rs.getInt("idBanco"));
//////        to.setNombreCorto(rs.getString("nombreCorto"));
////        to.setNumCtaPago(rs.getString("numCtaPago"));
////        to.setMedioPago(rs.getString("medioPago"));
////        return to;
////    }
//
//    public int agregar(int codigo, int idBanco, String numCtaPago, String medioPago) throws SQLException {
//        int idEmpresa = 0;
//        Connection cn = this.ds.getConnection();
//        Statement st = cn.createStatement();
//        try {
//            st.executeUpdate("begin Transaction");
//            st.executeUpdate("INSERT INTO clientesBancos (codigoCliente, idBanco, numCtaPago,medioPago) "
//                    + "VALUES(" + codigo + ", " + idBanco + ", '" + numCtaPago + "', '" + medioPago + "')");
//
//            st.executeUpdate("commit Transaction");
//        } catch (SQLException ex) {
//            st.executeUpdate("rollback Transaction");
//            throw (ex);
//        } finally {
//            cn.close();
//        }
//        return idEmpresa;
//    }
//
//    public void modificar(int idCliente, int codigo, int idBanco, String numCtaPago, String medioPago) throws SQLException {
//        Connection cn = this.ds.getConnection();
//        Statement st = cn.createStatement();
//        try {
//            st.executeUpdate("UPDATE clientesBancos "
//                    + "SET codigoCliente=" + codigo + ", idBanco=" + idBanco + ", numCtaPago='" + numCtaPago + "', medioPago='" + medioPago + "'"
//                    + "WHERE idClienteBanco=" + idCliente);
//        } finally {
//            cn.close();
//        }
//    }
    // PARTE QUE SE TRASPASA TERMINA, DE DAOClientes de paquete: clientes
}
