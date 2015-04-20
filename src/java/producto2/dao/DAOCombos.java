package producto2.dao;

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
import producto2.to.TOProductoCombo;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOCombos {
    private DataSource ds;
    
    public DAOCombos() throws NamingException {
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
    
    public void grabarCombo(ArrayList<TOProductoCombo> tos, int idProducto) throws SQLException {
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.execute("BEGIN TRANSACTION");
            st.executeUpdate("DELETE FROM empaquesCombos WHERE idEmpaque="+idProducto);
            for(TOProductoCombo to:tos) {
                st.executeUpdate("INSERT INTO empaquesCombos (idEmpaque, idSubEmpaque, piezas) "
                        + "VALUES("+idProducto+", "+to.getIdSubProducto()+", "+to.getPiezas()+")");
            }
            st.execute("COMMIT TRANSACTION");
        } catch(SQLException ex) {
            st.execute("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            cn.close();
        }
    } 
    
    public ArrayList<TOProductoCombo> obtenerCombo(int idProducto) throws SQLException {
        ArrayList<TOProductoCombo> productos=new ArrayList<TOProductoCombo>();
        String strSQL="SELECT idSubempaque AS idSubProducto, piezas FROM empaquesCombos WHERE idEmpaque="+idProducto;
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                productos.add(new TOProductoCombo(rs.getInt("idSubProducto"), rs.getInt("piezas")));
            }
        } finally {
            cn.close();
        }
        return productos;
    }
}
