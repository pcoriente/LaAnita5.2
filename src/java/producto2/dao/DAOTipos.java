package producto2.dao;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
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
import producto2.dominio.Tipo;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
@Named(value = "dAOTipos")
@SessionScoped
public class DAOTipos implements Serializable {
    private DataSource ds;
    
    public DAOTipos() throws NamingException {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
            
            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/"+usuarioSesion.getJndi());
        } catch (NamingException ex) {
            throw(ex);
        }
    }
    
    public Tipo obtenerTipo(int idTipo) throws SQLException {
        Tipo tipo=null;
        String strSQL="SELECT * FROM productosTipos WHERE idTipo="+idTipo;
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                tipo=new Tipo(rs.getInt("idTipo"), rs.getString("tipo"));
            }
        } finally {
            cn.close();
        }
        return tipo;
    }
    
    public ArrayList<Tipo> obtenerTipos() throws SQLException {
        ArrayList<Tipo> tipos=new ArrayList<Tipo>();
        String strSQL="SELECT * FROM productosTipos ORDER BY tipo";
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                tipos.add(new Tipo(rs.getInt("idTipo"), rs.getString("tipo")));
            }
        } finally {
            cn.close();
        }
        return tipos;
    }
}
