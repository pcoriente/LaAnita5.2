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
    
    public Usuario login(String login, String password, String jndi, int idDbs) throws NamingException, SQLException, Exception {
        Usuario usuario=null;
        Connection cn=ds.getConnection();
        
        Statement st = cn.createStatement();
        try {
            String strSQL="SELECT u.idUsuario, u.usuario, u.password, u.email, isnull(a.idPerfil, 0) as idPerfil "
                    + "FROM usuarios u "
                    + "LEFT JOIN (SELECT idUsuario, idPerfil FROM accesos WHERE idDbs="+idDbs+") a ON a.idUsuario=u.idUsuario "
                    + "WHERE u.login='"+login+"'";
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                String clave=Utilerias.md5(password);
                if(rs.getString("password").equals(clave)) {
                    usuario=this.construirUsuario(rs);
                } else {
                    usuario=new Usuario();
                }
            }
        } finally {
            cn.close();
        }
        return usuario;
    }
    
    private Usuario construirUsuario(ResultSet rs) throws SQLException {
        Usuario usuario=new Usuario();
        usuario.setId(rs.getInt("idUsuario"));
        usuario.setUsuario(rs.getString("usuario"));
        usuario.setCorreo(rs.getString("email"));
        usuario.setIdPerfil(rs.getInt("idPerfil"));
        return usuario;
    }
}
