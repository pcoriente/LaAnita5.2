package dbs.dao;

import dbs.dominio.Dbs;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import usuarios.dominio.Usuario;
import utilerias.Utilerias;

public class DAODbs {

    private final DataSource ds;

    public DAODbs() throws NamingException {
        try {
            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/jdbc/__webSystem");
        } catch (NamingException ex) {
            throw (ex);
        }
    }

    public Dbs[] obtenerDbs() throws SQLException {
        Dbs[] dbs = null;
        Connection cn = ds.getConnection();

        try {
            String strSQL = "SELECT * FROM basesDeDatos";
            Statement sentencia = cn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = sentencia.executeQuery(strSQL);
            if (rs.next()) {
                int i = 0;
                rs.last();
                dbs = new Dbs[rs.getRow()];

                rs.beforeFirst();
                while (rs.next()) {
                    dbs[i++] = construir(rs);
                }
            }
        } finally {
            cn.close();
        }
        return dbs;
    }

    private Dbs construir(ResultSet rs) throws SQLException {
        Dbs dbs = new Dbs();
        dbs.setIdDbs(rs.getInt("idBaseDeDatos"));
        dbs.setNombreBds(rs.getString("baseDeDatos"));
        dbs.setJndiDbs(rs.getString("jndi"));
        return dbs;
    }

    public Dbs obtener(int idDbs) throws SQLException {
        Dbs dbs = null;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery("SELECT * FROM basesDeDatos WHERE idBaseDeDatos=" + idDbs);
            if (rs.next()) {
                dbs = construir(rs);
            }
        } finally {
            cn.close();
        }
        return dbs;
    }
    
    private Usuario construirUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("idUsuario"));
        usuario.setUsuario(rs.getString("usuario"));
        usuario.setCorreo(rs.getString("email"));
        usuario.setIdPerfil(rs.getInt("idPerfil"));
        usuario.setIdCedis(rs.getInt("idCedis"));
        usuario.setIdCedisZona(rs.getInt("idCedisZona"));
        return usuario;
    }

    public Usuario login(String login, String password, int idDbs, String baseDeDatos) throws NamingException, SQLException, Exception {
        Usuario usuario = null;
        Connection cn = ds.getConnection();

        Statement st = cn.createStatement();
        try {
            String strSQL = "SELECT U.idUsuario, U.usuario, U.password, U.email, ISNULL(A.idPerfil, 0) as idPerfil\n"
                    + "	, ISNULL(C.idCedis, 0) AS idCedis, ISNULL(C.idCedisZona, 0) AS idCedisZona\n"
                    + "FROM usuarios U\n"
                    + "LEFT JOIN (SELECT * FROM accesos WHERE idDbs=" + idDbs +") A ON A.idUsuario=U.idUsuario\n"
                    + "LEFT JOIN " + baseDeDatos + ".dbo.usuarioConfig C ON C.idUsuario=U.idUsuario\n"
                    + "WHERE U.login='" + login + "'";
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
//                String claveVer = rs.getString("password");
                String clave = Utilerias.md51(password.trim());
                if (rs.getString("password").equals(clave)) {
                    usuario = this.construirUsuario(rs);
                } else {
                    usuario = new Usuario();
                }
            }
        } finally {
            cn.close();
        }
        return usuario;
    }
}
