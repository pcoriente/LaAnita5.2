package producto2.dao;

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
import producto2.dominio.Producto;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOProductos {
    private DataSource ds;
    
    public DAOProductos() throws NamingException {
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
    
    public void eliminar(int idProducto) throws SQLException {
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");
            
            int total=0;
            String strSQL="SELECT COUNT(*) AS total FROM empaques WHERE idSubEmpaque="+idProducto;
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                total=rs.getInt("total");
            }
            if(total==0) {
                strSQL="DELETE FROM empaquesUpcs WHERE idProducto="+idProducto;
                st.executeUpdate(strSQL);
                
                strSQL="DELETE FROM empaques WHERE idEmpaque="+idProducto;
                st.executeUpdate(strSQL);
            } else {
                throw new SQLException("No se puede eliminar, esta siendo utilizado como subProducto");
            }
            st.executeUpdate("COMMIT TRANSACTION");
        } catch(SQLException ex) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw(ex);
        } finally {
            cn.close();
        }
    }
    
    public void modificar(Producto to) throws SQLException {
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        String strSQL="UPDATE empaques "
            + "SET cod_pro='"+to.getCod_pro()+"', idProducto="+to.getArticulo().getIdArticulo() + ", "
            + "piezas="+to.getPiezas()+", idUnidadEmpaque="+to.getEmpaque().getIdEmpaque()+", idSubEmpaque="+to.getSubProducto().getIdProducto()+", "
            + "dun14='"+to.getDun14()+"', peso="+to.getPeso()+", volumen= "+to.getVolumen()+ " "
            + "WHERE idEmpaque="+to.getIdProducto();
        try {
            st.executeUpdate(strSQL);
        } catch(SQLException ex) {
            st.executeUpdate("rollback transaction");
            throw(ex);
        } finally {
            cn.close();
        }
    }
    
    public int agregar(Producto to) throws SQLException {
        int idProducto=0;
        String strSQL="INSERT INTO empaques (cod_pro, idProducto, piezas, idUnidadEmpaque, idSubEmpaque, dun14, peso, volumen) "
                + "VALUES ('"+to.getCod_pro() + "', " + to.getArticulo().getIdArticulo() + ", "
                + " "+to.getPiezas() + ", " + to.getEmpaque().getIdEmpaque() + ", " + to.getSubProducto().getIdProducto() + ","
                + " '"+to.getDun14() + "', " + to.getPeso() + ", " + to.getVolumen() + ")";
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate("begin transaction");
            st.executeUpdate(strSQL);
            ResultSet rs=st.executeQuery("SELECT MAX(idEmpaque) AS idProducto FROM empaques");
            if(rs.next()) {
                idProducto=rs.getInt("idProducto");
            }
            st.executeUpdate("commit transaction");
        } catch(SQLException ex) {
            st.executeUpdate("rollback transaction");
            throw(ex);
        } finally {
            cn.close();
        }
        return idProducto;
    }
}
