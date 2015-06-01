package tiendas.dao;

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
import tiendas.to.TOTienda;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOTiendas {

    private DataSource ds;

    public DAOTiendas() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
    }

    public void modificar(TOTienda to) throws SQLException {
        String strSQL = "UPDATE clientesTiendasCodigos SET codigoTienda=" + to.getCodigoTienda() + " "
                + "WHERE idCliente=" + to.getIdCliente() + " AND idFormato=" + to.getIdFormato() + " AND idTienda=" + to.getIdTienda();
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");
            
            int nRegs = st.executeUpdate(strSQL);
            if (nRegs == 0) {
                strSQL=this.agregarCodigo(to);
                st.executeUpdate(strSQL);
            }
            strSQL = "UPDATE clientesTiendas "
                    + "SET tienda='" + to.getTienda() + "', idAgente=" + to.getIdAgente() + ", idRuta=" + to.getIdRuta() + ", idImpuestoZona=" + to.getIdImpuestoZona()
                    + "WHERE idTienda=" + to.getIdTienda();
            st.executeUpdate(strSQL);
            
            st.executeUpdate("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw (ex);
        } finally {
            st.close();
            cn.close();
        }
    }

    private String agregarCodigo(TOTienda to) {
        return "INSERT INTO clientesTiendasCodigos (idCliente, idFormato, idTienda, codigoTienda) "
                + "VALUES(" + to.getIdCliente() + ", " + to.getIdFormato() + ", " + to.getIdTienda() + ", " + to.getCodigoTienda() + ")";
    }

    public int agregar(TOTienda to) throws SQLException {
        String strSQL = "INSERT INTO clientesTiendas (tienda, idDireccion, idCliente, idFormato, idAgente, idRuta, idImpuestoZona, codigoCliente, estado) "
                + "VALUES('" + to.getTienda() + "', " + to.getIdDireccion() + ", " + to.getIdCliente() + ", " + to.getIdFormato() + ", " + to.getIdAgente() + ", " + to.getIdRuta() + ", " + to.getIdImpuestoZona() + ", " + to.getCodigoTienda() + ", " + to.getEstado() + ")";
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");
            
            st.executeUpdate(strSQL);
            ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idTienda");
            if (rs.next()) {
                to.setIdTienda(rs.getInt("idTienda"));
            }
            if (to.getCodigoTienda() != 0) {
                st.executeUpdate(this.agregarCodigo(to));
            }
            st.executeUpdate("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw (ex);
        } finally {
            st.close();
            cn.close();
        }
        return to.getIdTienda();
    }

    public ArrayList<TOTienda> obtenerTiendasFormato(int idFormato) throws SQLException {
        ArrayList<TOTienda> tos = new ArrayList<TOTienda>();
        String strSQL = "SELECT T.*, Y.contribuyente, ISNULL(TC.codigoTienda, 0) AS codigoTienda\n"
                + "FROM clientesTiendas T\n"
                + "INNER JOIN clientes C ON C.idCliente=T.idCliente\n"
                + "INNER JOIN contribuyentes Y ON Y.idContribuyente=C.idContribuyente\n"
                + "LEFT JOIN clientesTiendasCodigos TC ON TC.idTienda=T.idTienda\n"
                + "WHERE T.idFormato=" + idFormato;
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                tos.add(this.construir(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return tos;
    }

    private TOTienda construir(ResultSet rs) throws SQLException {
        TOTienda to = new TOTienda();
        to.setIdTienda(rs.getInt("idTienda"));
        to.setTienda(rs.getString("tienda"));
        to.setIdDireccion(rs.getInt("idDireccion"));
        to.setIdCliente(rs.getInt("idCliente"));
        to.setContribuyente(rs.getString("contribuyente"));
        to.setIdFormato(rs.getInt("idFormato"));
        to.setIdAgente(rs.getInt("idAgente"));
        to.setIdRuta(rs.getInt("idRuta"));
        to.setIdImpuestoZona(rs.getInt("idImpuestoZona"));
        to.setCodigoTienda(rs.getInt("codigoTienda"));
        to.setEstado(rs.getInt("estado"));
        return to;
    }

    public TOTienda obtenerTienda(int idTienda) throws SQLException {
        TOTienda to = null;
        String strSQL = "SELECT T.*, Y.contribuyente, ISNULL(TC.codigoTienda, 0) AS codigoTienda\n"
                + "FROM clientesTiendas T\n"
                + "INNER JOIN clientes C ON C.idCliente=T.idCliente\n"
                + "INNER JOIN contribuyentes Y ON Y.idContribuyente=C.idContribuyente\n"
                + "LEFT JOIN clientesTiendasCodigos TC ON TC.idTienda=T.idTienda\n"
                + "WHERE T.idTienda=" + idTienda;
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                to = this.construir(rs);
            }
        } finally {
            st.close();
            cn.close();
        }
        return to;
    }
}
