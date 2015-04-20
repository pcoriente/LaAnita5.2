package empresas.dao;

import empresas.dominio.MiniEmpresa;
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
 * @author JULIOS
 */
public class DAOMiniEmpresas {
    private  DataSource ds=null;

    public DAOMiniEmpresas() throws NamingException {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");

            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/"+usuarioSesion.getJndi());
        } catch (NamingException ex) {
            throw (ex);
        }
    }
    
    public ArrayList<MiniEmpresa> obtenerMiniEmpresas() throws SQLException {
        ArrayList<MiniEmpresa> minis = new ArrayList<MiniEmpresa>();
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery("SELECT * FROM empresasGrupo");
            while(rs.next()) {
                minis.add(construirMini(rs));
            }
        } finally {
            cn.close();
        }
        return minis;
    }
    
    public MiniEmpresa obtenerMiniEmpresa(int idEmpresa) throws SQLException {
        MiniEmpresa mini = null;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery("SELECT * FROM empresasGrupo WHERE idEmpresa=" + idEmpresa);
            if (rs.next()) {
                mini = construirMini(rs);
            }
        } finally {
            cn.close();
        }
        return mini;
    }
    
    private MiniEmpresa construirMini(ResultSet rs) throws SQLException {
        MiniEmpresa mini=new MiniEmpresa();
        mini.setIdEmpresa(rs.getInt("idEmpresa"));
        mini.setCodigoEmpresa(rs.getString("codigoEmpresa"));
        mini.setNombreComercial(rs.getString("nombreComercial"));
        return mini;
    }
}
