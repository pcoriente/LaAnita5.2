package impuestos.dao;

import impuestos.dominio.Impuesto;
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

public class DAOImpuestos {

    private DataSource ds;

    public DAOImpuestos() throws NamingException {
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
    
    public void eliminar(int idImpuesto) throws SQLException {
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
    
    public void modificar(Impuesto impuesto) throws SQLException {
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
    
    public int agregar(Impuesto impuesto) throws SQLException {
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
    
    public Impuesto obtenerImpuesto(int idImpuesto) throws SQLException {
        Impuesto tipo = null;
        String strSQL = "SELECT * FROM impuestos WHERE idImpuesto=" + idImpuesto;
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                tipo = new Impuesto(rs.getInt("idImpuesto"), rs.getString("impuesto"), rs.getBoolean("aplicable"), rs.getInt("modo"), rs.getBoolean("acreditable"), rs.getBoolean("acumulable"));
            }
        } finally {
            cn.close();
        }
        return tipo;
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
}
