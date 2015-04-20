package impuestos.dao;

import impuestos.dominio.Impuesto;
import impuestos.dominio.ImpuestoGrupo;
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
public class DAOGrupos {
    private DataSource ds;
    
    public DAOGrupos() throws NamingException {
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
    
    public void eliminarImpuesto(int idImpuesto) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("begin Transaction");
            int total=0;
            ResultSet rs=st.executeQuery("SELECT COUNT(*) AS total FROM impuestosGruposDetalle WHERE idImpuesto="+idImpuesto);
            if(rs.next()) {
                total=rs.getInt("total");
            }
            if(total==0) {
                st.executeUpdate("DELETE FROM impuestos WHERE idImpuesto="+idImpuesto);
            } else {
                throw new SQLException("El impuesto no puede ser eliminado pues esta en uso !!!");
            }
            st.executeUpdate("commit Transaction");
        } catch(SQLException e) {
            st.executeUpdate("rollback Transaction");
            throw e;
        } finally {
            cn.close();
        }
    }
    
    public void modificarImpuesto(Impuesto impuesto) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("UPDATE impuestos "
                    + "SET impuesto='"+impuesto.getImpuesto()+"', aplicable="+(impuesto.isAplicable()?1:0)+", "
                    + "     modo="+impuesto.getModo()+", acreditable="+(impuesto.isAcreditable()?1:0)+", acumulable="+(impuesto.isAcumulable()?1:0)+" "
                    + "WHERE idImpuesto="+impuesto.getIdImpuesto());
        } finally {
            cn.close();
        }
    }
    
    public int agregarImpuesto(Impuesto impuesto) throws SQLException {
        int idImpuesto = 0;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("begin Transaction");
            st.executeUpdate("INSERT INTO impuestos (impuesto, aplicable, modo, acreditable, acumulable) "
                    + "VALUES ('" + impuesto.getImpuesto() + "', "+(impuesto.isAplicable()?1:0)+", "+impuesto.getModo()+", "+(impuesto.isAcreditable()?1:0)+", "+(impuesto.isAcumulable()?1:0)+")");
            ResultSet rs=st.executeQuery("SELECT MAX(idImpuesto) as idImpuesto FROM impuestos");
            if(rs.next()) {
                idImpuesto=rs.getInt("idImpuesto");
            }
            st.executeUpdate("commit Transaction");
        } catch (SQLException ex) {
            st.executeUpdate("rollback Transaction");
            throw (ex);
        } finally {
            cn.close();
        }
        return idImpuesto;
    }
    
    public ArrayList<Impuesto> obtenerImpuestos() throws SQLException {
        ArrayList<Impuesto> impuestos = new ArrayList<Impuesto>();
        String strSQL = "SELECT * FROM impuestos ORDER BY impuesto";
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                impuestos.add(new Impuesto(rs.getInt("idImpuesto"), rs.getString("impuesto"), rs.getBoolean("aplicable"), rs.getInt("modo"), rs.getBoolean("acreditable"), rs.getBoolean("acumulable")));
            }
        } finally {
            cn.close();
        }
        return impuestos;
    }
    
//    public ArrayList<Impuesto> eliminarImpuesto(int idGrupo, int idImpuesto) throws SQLException {
//        ArrayList<Impuesto> impuestos=new ArrayList<Impuesto>();
//        Connection cn = this.ds.getConnection();
//        Statement st = cn.createStatement();
//        try {
//            st.executeUpdate("begin Transaction");
//            st.executeUpdate("DELETE FROM impuestosGruposDetalle WHERE idGrupo="+idGrupo+" AND idImpuesto="+idImpuesto);
//            ResultSet rs=st.executeQuery(sqlAgregados(idGrupo));
//            while(rs.next()) {
//                impuestos.add(new Impuesto(rs.getInt("idImpuesto"),rs.getString("impuesto"), rs.getBoolean("aplicable"), rs.getInt("modo"), rs.getBoolean("acreditable"), rs.getBoolean("acumulable")));
//            }
//            st.executeUpdate("commit Transaction");
//        } catch (SQLException ex) {
//            st.executeUpdate("rollback Transaction");
//            throw (ex);
//        } finally {
//            cn.close();
//        }
//        return impuestos;
//    }
    
//    private String sqlAgregados(int idGrupo) {
//        String strSQL="SELECT i.* "
//                    + "FROM impuestosGruposDetalle gd "
//                    + "INNER JOIN impuestos i ON i.idImpuesto=gd.idImpuesto "
//                    + "WHERE gd.idGrupo="+idGrupo;
//        return strSQL;
//    }
    
//    public ArrayList<Impuesto> agregarImpuesto(int idGrupo, int idImpuesto) throws SQLException {
//        ArrayList<Impuesto> impuestos=new ArrayList<Impuesto>();
//        Connection cn = this.ds.getConnection();
//        Statement st = cn.createStatement();
//        try {
//            st.executeUpdate("begin Transaction");
//            st.executeUpdate("INSERT INTO impuestosGruposDetalle (idGrupo, idImpuesto) VALUES ("+idGrupo+", "+idImpuesto+")");
//            ResultSet rs=st.executeQuery(sqlAgregados(idGrupo));
//            while(rs.next()) {
//                impuestos.add(new Impuesto(rs.getInt("idImpuesto"),rs.getString("impuesto"), rs.getBoolean("aplicable"), rs.getInt("modo"), rs.getBoolean("acreditable"), rs.getBoolean("acumulable")));
//            }
//            st.executeUpdate("commit Transaction");
//        } catch (SQLException ex) {
//            st.executeUpdate("rollback Transaction");
//            throw (ex);
//        } finally {
//            cn.close();
//        }
//        return impuestos;
//    }
    
    public ArrayList<Impuesto> obtenerImpuestosAgregados(int idGrupo) throws SQLException {
        ArrayList<Impuesto> impuestos=new ArrayList<Impuesto>();
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
//            ResultSet rs=st.executeQuery(sqlAgregados(idGrupo));
            String strSQL="SELECT i.* "
                    + "FROM impuestosGruposDetalle gd "
                    + "INNER JOIN impuestos i ON i.idImpuesto=gd.idImpuesto "
                    + "WHERE gd.idGrupo="+idGrupo;
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                impuestos.add(new Impuesto(rs.getInt("idImpuesto"),rs.getString("impuesto"), rs.getBoolean("aplicable"), rs.getInt("modo"), rs.getBoolean("acreditable"), rs.getBoolean("acumulable")));
            }
        } finally {
            cn.close();
        }
        return impuestos;
    }
    
    public ArrayList<Impuesto> obtenerImpuestosDisponibles(int idGrupo) throws SQLException {
        ArrayList<Impuesto> impuestos=new ArrayList<Impuesto>();
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs=st.executeQuery("SELECT * FROM impuestos "
                    + "WHERE idImpuesto not in (SELECT idImpuesto FROM impuestosGruposDetalle WHERE idGrupo="+idGrupo+")");
            while(rs.next()) {
                impuestos.add(new Impuesto(rs.getInt("idImpuesto"),rs.getString("impuesto"), rs.getBoolean("aplicable"), rs.getInt("modo"), rs.getBoolean("acreditable"), rs.getBoolean("acumulable")));
            }
        } finally {
            cn.close();
        }
        return impuestos;
    }
    
//    public void modificarDetalle(int idGrupo, ArrayList<Impuesto> impuestos) throws SQLException {
//        Connection cn = this.ds.getConnection();
//        Statement st = cn.createStatement();
//        PreparedStatement pSt = cn.prepareStatement("INSERT INTO impuestosGruposDetalle (idGrupo, idImpuesto) VALUES (?, ?)");
//        try {
//            st.executeUpdate("begin Transaction");
//            st.executeUpdate("DELETE FROM impuestosGruposDetalle WHERE idGrupo="+idGrupo);
//            for(Impuesto i: impuestos) {
//                pSt.setInt(idGrupo, 1);
//                pSt.setInt(i.getIdImpuesto(), 2);
//                pSt.executeUpdate();
//            }
//            st.executeUpdate("commit Transaction");
//        } catch (SQLException ex) {
//            st.executeUpdate("rollback Transaction");
//            throw (ex);
//        } finally {
//            cn.close();
//        }
//    }
    
    public boolean eliminarGrupo(int idGrupo) throws SQLException {
        boolean ok=false;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("begin Transaction");
            ResultSet rs=st.executeQuery("SELECT * FROM impuestosDetalle WHERE idGrupo="+idGrupo);
            if(!rs.next()) {
                st.executeUpdate("DELETE FROM impuestosGrupos WHERE idGrupo="+idGrupo);
                st.executeUpdate("DELETE FROM impuestosGruposDetalle WHERE idGrupo="+idGrupo);
                ok=true;
            }
            st.executeUpdate("commit Transaction");
        } catch (SQLException ex) {
            st.executeUpdate("rollback Transaction");
            throw (ex);
        } finally {
            cn.close();
        }
        return ok;
    }
    
    public void modificarGrupo(ImpuestoGrupo grupo, ArrayList<Impuesto> agregados) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("begin Transaction");
            st.executeUpdate("UPDATE impuestosGrupos SET grupo='"+grupo.getGrupo()+"' WHERE idGrupo="+grupo.getIdGrupo());
            st.executeUpdate("DELETE FROM impuestosGruposDetalle WHERE idGrupo="+grupo.getIdGrupo());
            for(Impuesto i: agregados) {
                st.executeUpdate("INSERT INTO impuestosGruposDetalle (idGrupo, idImpuesto) VALUES ("+grupo.getIdGrupo()+", "+i.getIdImpuesto()+")");
            }
            st.executeUpdate("commit Transaction");
        } catch(SQLException e) {
            st.executeUpdate("rollback Transaction");
        } finally {
            cn.close();
        }
    }
    
    public int agregarGrupo(ImpuestoGrupo grupo, ArrayList<Impuesto> agregados) throws SQLException {
        int idGrupo=0;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("begin Transaction");
            st.executeUpdate("INSERT INTO impuestosGrupos (grupo) VALUES ('"+grupo.getGrupo()+"')");
            ResultSet rs=st.executeQuery("SELECT MAX(idGrupo) AS idGrupo FROM impuestosGrupos");
            if(rs.next()) {
                idGrupo=rs.getInt("idGrupo");
            }
            for(Impuesto i: agregados) {
                st.executeUpdate("INSERT INTO impuestosGruposDetalle (idGrupo, idImpuesto) VALUES ("+idGrupo+", "+i.getIdImpuesto()+")");
            }
            st.executeUpdate("commit Transaction");
        } catch (SQLException ex) {
            st.executeUpdate("rollback Transaction");
            throw (ex);
        } finally {
            cn.close();
        }
        return idGrupo;
    }
    
    public ImpuestoGrupo obtenerGrupo(int idGrupo) throws SQLException {
        ImpuestoGrupo impuesto=null;
        String strSQL="SELECT * FROM impuestosGrupos WHERE idGrupo="+idGrupo;
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                impuesto=new ImpuestoGrupo(rs.getInt("idGrupo"), rs.getString("grupo"));
            }
        } finally {
            cn.close();
        }
        return impuesto;
    }
    
    public ArrayList<ImpuestoGrupo> obtenerGrupos() throws SQLException {
        ArrayList<ImpuestoGrupo> impuestos=new ArrayList<ImpuestoGrupo>();
        String strSQL="SELECT * FROM impuestosGrupos";
        Connection cn=ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                impuestos.add(new ImpuestoGrupo(rs.getInt("idGrupo"), rs.getString("grupo")));
            }
        } finally {
            cn.close();
        }
        return impuestos;
    }
}
