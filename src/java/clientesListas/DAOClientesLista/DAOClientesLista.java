package clientesListas.DAOClientesLista;

import Message.Mensajes;
import clientesListas.dominio.ClientesListas;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import usuarios.dominio.UsuarioSesion;
import clientes.to.TOClienteLista;
import java.util.ArrayList;

/**
 *
 * @author Usuario
 */
public class DAOClientesLista {

    private DataSource ds;

    public DAOClientesLista() throws NamingException {
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

    public ArrayList<TOClienteLista> cargaListas() throws SQLException {
        ArrayList<TOClienteLista> listas = new ArrayList<>();
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "SELECT cl.idClienteLista,idEmpresa,idGrupoCte,idFormato,idCliente,idTienda\n"
                        + "FROM (SELECT DISTINCT cld.idClienteLista,idGrupoCte,idFormato,idCliente,idTienda\n"
                        + "			FROM clientesListasDetalle cld) cld\n"
                        + "INNER JOIN clientesListas cl ON cl.idClienteLista = cld.idClienteLista";
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    listas.add(construirToLista(rs));
                }
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                Mensajes.MensajeErrorP(e.getMessage());
                //throw (e);
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return listas;
    }

    public static TOClienteLista construirToLista(ResultSet rs) throws SQLException {
        TOClienteLista to = new TOClienteLista();
        to.setIdClienteLista(rs.getInt("idClienteLista"));
        to.setIdEmpresa(rs.getInt("idEmpresa"));
        to.setIdGrupoCte(rs.getInt("idGrupoCte"));
        to.setIdFormato(rs.getInt("idFormato"));
        to.setIdCliente(rs.getInt("idcliente"));
        to.setIdCliente(rs.getInt("idCliente"));
        return to;
    }

    public ClientesListas dameInformacion(int idEmpresa, int idGrupoCte, int idFormato) throws SQLException {
        ClientesListas clientes = new ClientesListas();
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        String sql = "SELECT * FROM clientesListas WHERE idEmpresa ='" + idEmpresa + "' and idGrupoCte = '" + idGrupoCte + "' and idFormato='" + idFormato + "';";
        try {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                clientes.setDescuetos(rs.getString("descuentos"));
                clientes.setMercanciaConCargo(rs.getDouble("mercanciaConCargo"));
                clientes.setMercanciaSinCargo(rs.getDouble("mercanciaSinCargo"));
                clientes.setPorcentajaBoletin(rs.getDouble("porcentajeBoletin"));

                clientes.setNumeroProveedor(rs.getString("numeroProveedor"));

            }
        } finally {
            cn.close();
        }
        return clientes;
    }

    public ClientesListas dameInformacion(int idEmpresa, int idGrupoCte) throws SQLException {
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        ClientesListas clientes = new ClientesListas();
        String sql = "SELECT * FROM clientesListas WHERE idEmpresa ='" + idEmpresa + "' and idGrupoCte = '" + idGrupoCte + "'";
        try {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                clientes.setDescuetos(rs.getString("descuentos"));
                clientes.setMercanciaConCargo(rs.getDouble("mercanciaConCargo"));
                clientes.setMercanciaSinCargo(rs.getDouble("mercanciaSinCargo"));
                clientes.setPorcentajaBoletin(rs.getDouble("porcentajeBoletin"));
                clientes.setNumeroProveedor(rs.getString("numeroProveedor"));

            }
        } finally {
            cn.close();
        }
        return clientes;
    }
}
