package bancos.dao;

import bancos.dominio.Banco;
import clientes2.dominio.Cliente;
import java.sql.*;
import java.util.ArrayList;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import usuarios.dominio.UsuarioSesion;

public class DAOBancos {

    private final DataSource ds;

    public DAOBancos() throws NamingException {
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

    public Banco[] obtenerBancos() throws SQLException {

        System.err.println("Entro a buscar los dato en la base d datos");
        Banco[] bancos = null;
        ResultSet rs = null;

        Connection cn = ds.getConnection();
        String strSQL = "SELECT * FROM bancosSat";
        try {
            Statement sentencia = cn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = sentencia.executeQuery(strSQL);
            if (rs.next()) {
                int i = 0;
                rs.last();
                bancos = new Banco[rs.getRow()];

                rs.beforeFirst();
                while (rs.next()) {
                    bancos[i++] = construir(rs);
                }
            }
        } finally {
            cn.close();
        }
        return bancos;
    }

    public Banco obtener(int idBanco) throws SQLException {
        Banco banco = null;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery("SELECT * FROM bancosSat WHERE idBanco=" + idBanco);
            if (rs.next()) {
                banco = construir(rs);
            }
        } finally {
            cn.close();
        }
        return banco;
    }

    private Banco construir(ResultSet rs) throws SQLException {
        Banco banco = new Banco();
        banco.setIdBanco(rs.getInt("idBanco"));
        banco.setNombreCorto(rs.getString("nombreCorto"));
        return banco;
    }

    public void agregarClientes(Cliente bnClientes) throws SQLException {
        System.out.println("si entro a la sentencia");
        String sql;
        sql = "Insert Into clientesBanco (codigoCliente, idBAnco,numCtaPago, mediopago) Values(?,?,?,?)";
        Connection cn;
        cn = ds.getConnection();
        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setInt(1, bnClientes.getClienteSEA().getCod_cli());
        ps.setInt(2, bnClientes.getBanco().getIdBanco());
        ps.setString(3, bnClientes.getNumCtaPago());
        ps.setString(4, bnClientes.getMedioPago());
        ps.executeUpdate();
        ps.close();
    }

    public ArrayList<Banco> dameBancos(int idCliente) throws SQLException {
        ArrayList<Banco> lstBancos = new ArrayList<Banco>();
        String sql = "SELECT razonSocial, codigoBanco, nombreCorto FROM bancosSat bs "
                + " INNER JOIN clientesBancos c "
                + " on c.idBanco = bs.idBanco"
                + "  WHERE c.codigoCliente = '" + idCliente + "'";
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                Banco banco = new Banco();
//                banco.setIdBanco(rs.getInt("idBanco"));
                banco.setNombreCorto(rs.getString("nombreCorto"));
                banco.setCodigoBanco(rs.getInt("codigoBanco"));
                lstBancos.add(banco);
            }
        } finally {
            st.close();
            cn.close();
        }

        return lstBancos;
    }

    public ArrayList<Banco> dameBancos() throws SQLException {
        ArrayList<Banco> lstBancos = new ArrayList<Banco>();
        String sql = "SELECT * FROM bancosSat bs";
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Banco banco = new Banco();
                banco.setIdBanco(rs.getInt("idBanco"));
                banco.setNombreCorto(rs.getString("nombreCorto"));
                banco.setCodigoBanco(rs.getInt("codigoBanco"));
                lstBancos.add(banco);
            }
        } finally {
            st.close();
            cn.close();
        }
        return lstBancos;
    }

}
