package leyenda.dao;

import leyenda.dominio.BancoLeyenda;
import bancos.MbBanco;
import java.sql.*;
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
import leyenda.dominio.LeyendaBanco;
import usuarios.dominio.UsuarioSesion;

public class DAOBancosLeyendas {

    protected static DataSource ds;
//public static Connection cn;    

    public DAOBancosLeyendas() {

        // CODIGO QUE SIRVE PARA LA CONECCION DE GLASSfISH
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");

            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
        } catch (NamingException ex) {
            Logger.getLogger(DAOBancosLeyendas.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
//metodos para obtener datos de la coneccion.

    public ArrayList<BancoLeyenda> dameBancos() throws SQLException {
        ArrayList<BancoLeyenda> Tabla = new ArrayList();
        Connection cn;
        cn = ds.getConnection();
        ResultSet rs = null;
        String strSQL = "select * from bancosSat ORDER BY(idBanco) ";
        try {
            PreparedStatement sentencia = cn.prepareStatement(strSQL);
            rs = sentencia.executeQuery();
            creaBanco(rs, Tabla);
        } finally {
            cn.close();
        }
        return Tabla;
    }

    public void creaBanco(ResultSet rs, ArrayList Tabla) throws SQLException {
        while (rs.next()) {
            BancoLeyenda b = new BancoLeyenda();
            b.setIdBanco(rs.getInt("idbanco"));
            b.setRfc(rs.getString("rfc"));
            b.setCodigoBanco(rs.getInt("codigoBanco"));
            b.setRazonSocial(rs.getString("razonSocial"));
            b.setNombreCorto(rs.getString("nombreCorto"));
            Tabla.add(b);
        }
    }

    public void dameUsuario(BancoLeyenda banco) throws SQLException {
        Connection cnn = null;
        cnn = ds.getConnection();

        String stringSQL = "UPDATE bancosSat  SET rfc=?, codigoBanco = ?, razonSocial = ?, nombreCorto =? where  idBanco = ?";
        PreparedStatement ps = cnn.prepareStatement(stringSQL);
        ps.setString(1, banco.getRfc());
        ps.setInt(2, banco.getCodigoBanco());
        ps.setString(3, banco.getRazonSocial());
        ps.setString(4, banco.getNombreCorto());
        ps.setInt(5, banco.getIdBanco());
        ps.executeUpdate();
        ps.close();
    }

    public void eliminarUsuario(int id) throws SQLException {
        Connection cn = null;
        cn = ds.getConnection();
        String eliminar = "DELETE FROM bancosSat WHERE idBanco = ? ";
        PreparedStatement ps = cn.prepareStatement(eliminar);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

//    -----------------------------Metodos Para Leyenda BancoLeyenda--------------------------------
//    --------------Metodo para desplegar Los datos de la tabla----------------
    public ArrayList<LeyendaBanco> dameDatosLeyenda() throws SQLException {
        ArrayList<LeyendaBanco> Tabla = new ArrayList();
        Connection cne;
        cne = ds.getConnection();
        ResultSet rs = null;
        String strSQL = "select * from leyendasPagos ORDER BY(idLeyenda)";
        try {
            PreparedStatement sentencia = cne.prepareStatement(strSQL);
            rs = sentencia.executeQuery();
            RecorreDatosLeyenda(rs, Tabla);
        } finally {
            cne.close();
        }
        return Tabla;
    }

    public void RecorreDatosLeyenda(ResultSet rs, ArrayList Tabla) throws SQLException {
        while (rs.next()) {
            LeyendaBanco lb = new LeyendaBanco();
            lb.setIdLeyenda(rs.getInt("idLeyenda"));
            lb.setLeyenda(rs.getString("leyenda"));
            Tabla.add(lb);
        }
    }

    public void Mactualizar(LeyendaBanco lb) throws SQLException {
        Connection cnn = null;
        cnn = ds.getConnection();
        String stringSQL = "UPDATE leyendasPagos  SET leyenda = ? where  idLeyenda = ?";
        PreparedStatement ps = cnn.prepareStatement(stringSQL);
        ps.setString(1, lb.getLeyenda());
        ps.setInt(2, lb.getIdLeyenda());
        ps.executeUpdate();
        ps.close();

    }

    public void eliminarUsuariol(int id) throws SQLException {
        Connection cn = null;
        cn = ds.getConnection();
        String eliminar = "DELETE FROM leyendasPagos WHERE idLeyenda = ? ";
        PreparedStatement ps = cn.prepareStatement(eliminar);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public void guardarL(String mbl) throws SQLException {
        String sSQL = "INSERT INTO leyendasPagos (leyenda)"
                + "VALUES('" + mbl + "')";
        Connection cn;
        cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("begin Transaction");
            st.executeUpdate(sSQL);
            st.executeUpdate("commit Transaction");
            cn.close();
        } catch (SQLException ex) {
            st.executeUpdate("rollback Transaction");
            throw (ex);
        } finally {
            cn.close();
        }

    }
//    -----------------------------------metodo para combo box bancos--------------------

    public BancoLeyenda[] obtenerPaises() throws SQLException {
        BancoLeyenda[] bancos = null;
        ResultSet rs = null;

        Connection cn = ds.getConnection();
        String strSQL = "SELECT * FROM bancosSat";
        try {
            Statement sentencia = cn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = sentencia.executeQuery(strSQL);
            if (rs.next()) {
                int i = 0;
                rs.last();
                bancos = new BancoLeyenda[rs.getRow()];

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

    private BancoLeyenda construir(ResultSet rs) throws SQLException {
        BancoLeyenda Bn = new BancoLeyenda();
        Bn.setIdBanco(rs.getInt("idBanco"));
        Bn.setNombreCorto(rs.getString("nombreCorto"));
        return Bn;
    }

    BancoLeyenda obtener(int idBanco) throws SQLException {
        BancoLeyenda banco = null;
        ResultSet rs = null;

        Connection cn = ds.getConnection();
        String sql = "SELECT * FROM bancosSat where idBanco = ?" + idBanco;
        try {
            PreparedStatement sentencia = cn.prepareStatement(sql);
            sentencia.setInt(1, idBanco);
            rs = sentencia.executeQuery();
            if (rs.next()) {
                banco = construir(rs);
            }

        } finally {
            cn.close();
        }

        return banco;

    }

    public int agregarDatos(ClienteBanco b) throws SQLException, Exception {
        ResultSet rs = null;
        int idClientes = 0;
        String sql = "INSERT INTO  ClientesBancos  codigoCliente, idBanco, numCta, medioPago" + "VALUES (?, ?, ?,?,?)";
        Connection cn = ds.getConnection();

        try {
            cn.setAutoCommit(false);

            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, b.getCodigoCliente());
            ps.setInt(2, b.getIdBanco());
            ps.setString(3, b.getNumCtaPago());
            ps.setString(4, b.getMedioPago());

            if (rs.next()) {
                idClientes = rs.getInt("idClienteBanco");
            }
            cn.commit();
        } catch (Exception ex) {
            cn.rollback();
            throw (ex);
        } finally {
            cn.close();
        }
        return idClientes;
    }

    public void consulta() throws SQLException {
        String sql = "SELECT* FROM leyendasPagos";
        Connection c;
        c = ds.getConnection();
        PreparedStatement ps = c.prepareStatement(sql);
        ps.executeQuery();
    }

    public void agregarDato(String rfc, int codigoBanco, String razonSocial, String nombreCorto) throws SQLException {
        String sql = "INSERT INTO  bancosSat ( rfc, codigoBanco, razonSocial, nombreCorto)"
                + "VALUES ('" + rfc + "','" + codigoBanco + "','" + razonSocial + "','" + nombreCorto + "')";
        Connection cn;
        cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("begin Transaction");
            st.executeUpdate(sql);
            st.executeUpdate("commit Transaction");
            cn.close();
        } catch (SQLException ex) {
            st.executeUpdate("rollback Transaction");
            throw (ex);
        } finally {
            cn.close();
        }
    }

    public LeyendaBanco obtenerLeyendas(int id) throws SQLException {
        Connection cn = ds.getConnection();
        LeyendaBanco lb = new LeyendaBanco();
        ResultSet rs;
        String sql = "SELECT * FROM leyendasPagos where idLeyenda =" + id;
        try {
            PreparedStatement sentencia = cn.prepareStatement(sql);
            rs = sentencia.executeQuery();
            if (rs.next()) {
                lb.setIdLeyenda(rs.getInt("idLeyenda"));
                lb.setLeyenda(rs.getString("leyenda"));
            }
        } finally {
            cn.close();
        }
        return lb;
    }

    public BancoLeyenda obtenerDatos(int id) throws SQLException {
        BancoLeyenda bl =  new BancoLeyenda();
         Connection cn = ds.getConnection();
         ResultSet rs;
        String sql="SELECT * FROM bancosSat where idBanco ="+id;
         try {
            PreparedStatement sentencia = cn.prepareStatement(sql);
            rs = sentencia.executeQuery();
            if (rs.next()) {
                bl.setIdBanco(rs.getInt("idBanco"));
                bl.setRfc(rs.getString("rfc"));
                bl.setCodigoBanco(rs.getInt("codigoBanco"));
                bl.setRazonSocial(rs.getString("razonSocial"));
                bl.setNombreCorto(rs.getString("nombreCorto"));
            }
        } finally {
            cn.close();
        }
        return bl;
        
    }
}
