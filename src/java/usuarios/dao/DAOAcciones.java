package usuarios.dao;

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
import usuarios.dominio.Accion;

/**
 *
 * @author JULIOS
 */
public class DAOAcciones {
    private DataSource ds;
    private int idPerfil;
    
    public DAOAcciones() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/"+usuarioSesion.getJndi());
        this.idPerfil=usuarioSesion.getUsuario().getIdPerfil();
    }
    
    public ArrayList<Accion> obtenerAcciones(int idModulo) throws SQLException {
        ArrayList<Accion> acciones=new ArrayList<Accion>();
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            String strSQL="SELECT a.idAccion, a.accion, a.idBoton "
                    + "FROM usuarioPerfil up "
                    + "INNER JOIN webSystem.dbo.acciones a ON a.idAccion=up.idAccion "
                    + "WHERE up.idPerfil="+this.idPerfil+" AND up.idModulo="+idModulo;
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                acciones.add(new Accion(rs.getInt("idAccion"), rs.getString("accion"), rs.getString("idBoton")));
            }
        } finally {
            cn.close();
        }
        return acciones;
    }
}
