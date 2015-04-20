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
import producto2.dominio.Empaque;
import producto2.dominio.SubProducto;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOSubProductos {
    private DataSource ds;
    
    public DAOSubProductos() throws NamingException {
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
            String strSQL="SELECT COUNT(*) AS total FROM empaquesSubEmpaques WHERE idSubEmpaque="+idProducto;
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                total=rs.getInt("total");
            }
            if(total==0) {
                strSQL="SELECT COUNT(*) AS total FROM empaques WHERE idSubEmpaque="+idProducto;
                rs=st.executeQuery(strSQL);
                if(rs.next()) {
                    total=rs.getInt("total");
                }
                if(total==0) {
                    strSQL="DELETE FROM empaquesSubEmpaques WHERE idEmpaque="+idProducto;
                    st.executeUpdate(strSQL);
                } else {
                    throw new SQLException("No se puede eliminar, esta siendo utilizado como subProducto");
                }
            } else {
                throw new SQLException("No se puede eliminar, esta siendo utilizado como subEmpaque");
            }
            st.executeUpdate("COMMIT TRANSACTION");
        } catch(SQLException ex) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw(ex);
        } finally {
            cn.close();
        }
    }
    
    public int agregar(int idArticulo, SubProducto subProducto) throws SQLException {
        int idProducto=0;
        String strSQL="INSERT INTO empaquesSubEmpaques (idProducto, idUnidad, piezas, idSubEmpaque) "
                + "VALUES ("+idArticulo+", "+subProducto.getEmpaque().getIdEmpaque()+", "+subProducto.getPiezas()+", "+(subProducto.getSubSubProducto()==null?0:subProducto.getSubSubProducto().getIdProducto())+")";
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate("begin transaction");
            st.executeUpdate(strSQL);
            ResultSet rs=st.executeQuery("SELECT MAX(idEmpaque) AS idEmpaque FROM empaquesSubEmpaques");
            if(rs.next()) {
                idProducto=rs.getInt("idEmpaque");
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
    
//    public ArrayList<SubProducto> obtenerSubProductos(int idArticulo) throws SQLException {
//        ArrayList<SubProducto> lstProductos=new ArrayList<SubProducto>();
//        String strSQL="SELECT e.idEmpaque, e.idSubEmpaque, e.piezas, u.idUnidad, u.unidad, u.abreviatura as abreviaturaEmpaque "
//                + "FROM empaques e "
//                + "INNER JOIN empaquesUnidades u ON u.idUnidad=e.idUnidadEmpaque "
//                + "WHERE e.idProducto="+idArticulo;
//        Connection cn=ds.getConnection();
//        Statement st=cn.createStatement();
//        try {
//            Empaque empaque;
//            ResultSet rs=st.executeQuery(strSQL);
//            while(rs.next()) {
//                empaque=new Empaque(rs.getInt("idUnidad"), rs.getString("unidad"), rs.getString("abreviaturaEmpaque"));
//                lstProductos.add(new SubProducto(rs.getInt("idEmpaque"), rs.getInt("piezas"), empaque));
//            }
//        } finally {
//            cn.close();
//        }
//        return lstProductos;
//    }
    
//    public ArrayList<SubProducto> obtenerSubProductos(int idArticulo) throws SQLException {
//        ArrayList<SubProducto> lstProductos=new ArrayList<SubProducto>();
//        String strSQL="SELECT e.idEmpaque, e.idSubEmpaque, e.piezas, u.idUnidad, u.unidad, u.abreviatura as abreviaturaEmpaque "
//                + "FROM empaques e "
//                + "INNER JOIN empaquesUnidades u ON u.idUnidad=e.idUnidadEmpaque "
//                + "WHERE e.idProducto="+idArticulo;
//        Connection cn=ds.getConnection();
//        Statement st=cn.createStatement();
//        try {
//            SubProducto subProducto;
//            ResultSet rs=st.executeQuery(strSQL);
//            while(rs.next()) {
//                subProducto=new SubProducto(rs.getInt("idEmpaque"));
//                subProducto.setPiezas(rs.getInt("piezas"));
//                subProducto.setEmpaque(new Empaque(rs.getInt("idUnidad"), rs.getString("unidad"), rs.getString("abreviaturaEmpaque")));
//                subProducto.setSubProducto(this.obtenerSubProducto(rs.getInt("idSubEmpaque")));
//                lstProductos.add(subProducto);
//            }
//        } finally {
//            cn.close();
//        }
//        return lstProductos;
//    }
    
    public ArrayList<SubProducto> obtenerSubProductos(int idArticulo) throws SQLException {
        ArrayList<SubProducto> lstProductos=new ArrayList<SubProducto>();
        String strSQL="SELECT E.idEmpaque, E.piezas, U.idUnidad, U.unidad, U.abreviatura AS abreviaturaEmpaque, E.idSubEmpaque " +
                        "FROM empaquesSubEmpaques E " +
                        "INNER JOIN empaquesUnidades U ON U.idUnidad=E.idUnidad " +
                        "WHERE E.idProducto="+idArticulo;
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            SubProducto subProducto;
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                subProducto=new SubProducto(rs.getInt("idEmpaque"));
                subProducto.setPiezas(rs.getInt("piezas"));
                subProducto.setEmpaque(new Empaque(rs.getInt("idUnidad"), rs.getString("unidad"), rs.getString("abreviaturaEmpaque")));
                subProducto.setSubSubProducto(this.obtenerSubProducto(rs.getInt("idSubEmpaque")));
                lstProductos.add(subProducto);
            }
        } finally {
            cn.close();
        }
        return lstProductos;
    }
    
//    public SubProducto obtenerSubProducto(int idSubProducto) throws SQLException {
//        SubProducto subProducto=null;
//        String strSQL=""
//                + "SELECT e.piezas, e.idSubEmpaque, u.idUnidad, u.unidad, u.abreviatura as abreviaturaEmpaque "
//                + "FROM empaques e "
//                + "INNER JOIN empaquesUnidades u ON u.idUnidad=e.idUnidadEmpaque "
//                + "WHERE e.idEmpaque="+idSubProducto;
//        Connection cn=ds.getConnection();
//        Statement st=cn.createStatement();
//        try {
//            Empaque empaque;
//            ResultSet rs=st.executeQuery(strSQL);
//            if(rs.next()) {
//                empaque=new Empaque(rs.getInt("idUnidad"), rs.getString("unidad"), rs.getString("abreviaturaEmpaque"));
//                subProducto=new SubProducto(idSubProducto, rs.getInt("piezas"), empaque);
//                if(rs.getInt("idSubEmpaque")!=0) {
//                    subProducto.setSubProducto(obtenerSubProducto(rs.getInt("idSubEmpaque")));
//                }
//            }
//        } finally {
//            cn.close();
//        }
//        return subProducto;
//    }
    
//    public SubProducto obtenerSubProducto(int idSubProducto) throws SQLException {
//        SubProducto subProducto=null;
//        String strSQL=""
//                + "SELECT e.idEmpaque, e.piezas, e.idSubEmpaque, u.idUnidad, u.unidad, u.abreviatura as abreviaturaEmpaque "
//                + "FROM empaques e "
//                + "INNER JOIN empaquesUnidades u ON u.idUnidad=e.idUnidadEmpaque "
//                + "WHERE e.idEmpaque="+idSubProducto;
//        Connection cn=ds.getConnection();
//        Statement st=cn.createStatement();
//        try {
//            ResultSet rs=st.executeQuery(strSQL);
//            if(rs.next()) {
//                subProducto=new SubProducto(idSubProducto);
//                subProducto.setPiezas(rs.getInt("piezas"));
//                subProducto.setEmpaque(new Empaque(rs.getInt("idUnidad"), rs.getString("unidad"), rs.getString("abreviaturaEmpaque")));
//                if(rs.getInt("idSubEmpaque")!=0 && rs.getInt("idSubEmpaque")!=idSubProducto) {
//                    subProducto.setSubProducto(obtenerSubProducto(rs.getInt("idSubEmpaque")));
//                }
//            }
//        } finally {
//            cn.close();
//        }
//        return subProducto;
//    }
    
    public SubProducto obtenerSubProducto(int idSubEmpaque) throws SQLException {
        SubProducto subProducto=null;
         String strSQL="SELECT E.idEmpaque, E.piezas, U.idUnidad, U.unidad, U.abreviatura AS abreviaturaEmpaque, E.idSubEmpaque " +
                        "FROM empaquesSubEmpaques E " +
                        "INNER JOIN empaquesUnidades U ON U.idUnidad=E.idUnidad " +
                        "WHERE E.idEmpaque="+idSubEmpaque;
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                subProducto=new SubProducto(idSubEmpaque);
                subProducto.setPiezas(rs.getInt("piezas"));
                subProducto.setEmpaque(new Empaque(rs.getInt("idUnidad"), rs.getString("unidad"), rs.getString("abreviaturaEmpaque")));
                if(rs.getInt("idSubEmpaque")!=0 && rs.getInt("idSubEmpaque")!=idSubEmpaque) {
                    subProducto.setSubSubProducto(obtenerSubProducto(rs.getInt("idSubEmpaque")));
                }
            }
        } finally {
            cn.close();
        }
        return subProducto;
    }
}
