/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package formatos.dao;

import formatos.dominio.ClienteFormato;
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
public class DAOFormatos {

    int idUsuario;
    private DataSource ds = null;

    public DAOFormatos() throws NamingException {
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
    
    public ArrayList<ClienteFormato> dameFormatosCliente(int idCliente) throws SQLException {
        ArrayList<ClienteFormato> lstFormatos = null;
        String sql = "SELECT * FROM  clientesFormatos WHERE idCliente=" + idCliente;
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(sql);
            lstFormatos = new ArrayList<ClienteFormato>();
            while (rs.next()) {
                lstFormatos.add(this.construir(rs));
            }
        } finally {
            cn.close();
        }
        return lstFormatos;
    }

    public ArrayList<ClienteFormato> dameFormatos(int idGrupoClte) throws SQLException {
        ArrayList<ClienteFormato> lstFormatos = null;
        String sql = "SELECT * FROM  clientesFormatos WHERE idGrupoCte  = '" + idGrupoClte + "'";
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(sql);
            lstFormatos = new ArrayList<ClienteFormato>();
            while (rs.next()) {
                lstFormatos.add(this.construir(rs));
            }
        } finally {
            cn.close();
        }
        return lstFormatos;
    }

    public ClienteFormato obtenerClientesFormato(int id) throws SQLException {
        ClienteFormato formato = new ClienteFormato();
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        String sql = "SELECT * FROM clientesFormatos WHERE idFormato = " + id;
        ResultSet rs = st.executeQuery(sql);
        if(rs.next()) {
            formato=this.construir(rs);
        }
        return formato;
    }
    
    private ClienteFormato construir(ResultSet rs) throws SQLException {
        ClienteFormato to=new ClienteFormato();
        to.setIdFormato(rs.getInt("idFormato"));
        to.setFormato(rs.getString("formato"));
        to.setIdGrupoCte(rs.getInt("idGrupoCte"));
        to.setIdCliente(rs.getInt("idCliente"));
        return to;
    }
    
     public void actualizar(ClienteFormato formato) throws SQLException {
        String sql = "UPDATE clientesFormatos set formato = '" + formato.getFormato() + "' WHERE idFormato ='" + formato.getIdFormato() + "'";
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate(sql);
        } finally {
            cn.close();
        }
    }
    
     public void agregar(ClienteFormato formato) throws SQLException {
        String sql = "INSERT INTO clientesFormatos (formato, idGrupoCte, idCliente) "
                + "VALUES ('" + formato.getFormato() + "', " + formato.getIdGrupoCte() + ", "+formato.getIdCliente()+")";
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate(sql);
        } finally {
            cn.close();
        }
    }
}
