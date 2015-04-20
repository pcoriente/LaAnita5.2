package entradas.dao;

import entradas.to.TOComprobante;
import java.sql.Connection;
import java.sql.Date;
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
 * @author jesc
 */
public class DAOComprobantes {
    int idUsuario;
    private DataSource ds = null;

    public DAOComprobantes() throws NamingException {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
            this.idUsuario=usuarioSesion.getUsuario().getId();

            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
        } catch (NamingException ex) {
            throw (ex);
        }
    }
    
    public boolean cancelar(int idComprobante) throws SQLException {
        boolean ok=false;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate("UPDATE comprobantes SET propietario=0 WHERE idComprobante="+idComprobante);
            ok=true;
        } finally {
            cn.close();
        }
        return ok;
    }
    
    public boolean asegurarComprobante(int idComprobante, boolean oficina) throws SQLException {
        boolean ok=true;
        int propietario=0;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.execute("BEGIN TRANSACTION");
            ResultSet rs=st.executeQuery("SELECT propietario FROM comprobantes WHERE idComprobante="+idComprobante);
            if(rs.next()) {
                propietario=rs.getInt("propietario");
            } else {
                throw new SQLException("No se encotro el comprobante");
            }
            if(propietario==0) {
                st.executeUpdate("UPDATE comprobantes SET propietario="+this.idUsuario+" WHERE idComprobante="+idComprobante);
            } else if(propietario!=this.idUsuario) {
                ok=false;
            }
            st.execute("COMMIT TRANSACTION");
        } catch(SQLException ex) {
            st.execute("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            cn.close();
        }
        return ok;
    }
    
    public boolean obtenerEstadoAlmacen(int idComprobante) throws SQLException {
        boolean cerrada=false;   // 0.-Abierta; 1.-Cerrada
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery("SELECT statusAlmacen FROM comprobantes WHERE idComprobante="+idComprobante);
            if(rs.next()) {
                cerrada=rs.getBoolean("statusAlmacen");
            }
        } finally {
            cn.close();
        }
        return cerrada;
    }
    
    public boolean obtenerEstadoOficina(int idComprobante) throws SQLException {
        boolean cerrada=false;   // 0.-Abierta; 1.-Cerrada
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery("SELECT statusOficina FROM comprobantes WHERE idComprobante="+idComprobante);
            if(rs.next()) {
                cerrada=rs.getBoolean("statusOficina");
            }
        } finally {
            cn.close();
        }
        return cerrada;
    }
    
    public void modificar(TOComprobante c) throws SQLException {
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            Date fechaFactura=new java.sql.Date(c.getFecha().getTime());
            String strSQL="UPDATE comprobantes "
                    + "SET remision='"+c.getRemision()+"', serie='"+c.getSerie()+"', numero='"+c.getNumero()+"', fecha='"+fechaFactura.toString()+"', propietario=0 "
                    + "WHERE idComprobante="+c.getIdComprobante();
            st.executeUpdate(strSQL);
        } finally {
            cn.close();
        }
    }
    
    public int agregar(TOComprobante c) throws SQLException {
        int idComprobante=0;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");
            Date fechaFactura=new java.sql.Date(c.getFecha().getTime());
            st.executeUpdate("INSERT INTO comprobantes (idAlmacen, idProveedor, tipoComprobante, remision, serie, numero, fecha, idUsuario, statusOficina, statusAlmacen, propietario) "
                            + "VALUES ("+c.getIdAlmacen()+", "+c.getIdProveedor()+", "+c.getTipoComprobante()+", '"+c.getRemision()+"', '"+c.getSerie()+"', '"+c.getNumero()+"', '"+fechaFactura.toString()+"', "+this.idUsuario+", 0, 0, 0)");
            ResultSet rs=st.executeQuery("SELECT @@IDENTITY AS idComprobante");
            if(rs.next()) {
                idComprobante=rs.getInt("idComprobante");
            }
            st.executeUpdate("COMMIT TRANSACTION");
        } catch(SQLException e) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw(e);
        } finally {
            cn.close();
        }
        return idComprobante;
    }
    
    public TOComprobante obtenerComprobante(int idAlmacen, int idProveedor, int tipoComprobante, String serie, String numero) throws SQLException {
        TOComprobante c=null;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery("SELECT * FROM comprobantes "
                    + "WHERE idProveedor="+idProveedor+" AND tipoComprobante="+tipoComprobante+" AND serie='"+serie+"' AND numero='"+numero+"'");
            if(rs.next()) {
                c=construir(rs);
            }
        } finally {
            cn.close();
        }
        return c;
    }
    
    public TOComprobante obtenerComprobante(int idComprobante) throws SQLException {
        TOComprobante f=null;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery("SELECT * FROM comprobantes "
                    + "WHERE idComprobante="+idComprobante);
            if(rs.next()) {
                f=construir(rs);
            }
        } finally {
            cn.close();
        }
        return f;
    }
    
    public ArrayList<TOComprobante> obtenerComprobantes(int idAlmacen, int idProveedor, int tipoComprobante, Date fechaInicial, Date fechaFinal) throws SQLException {
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        ArrayList<TOComprobante> comprobantes=new ArrayList<TOComprobante>();
        try {
            ResultSet rs=st.executeQuery("SELECT * FROM comprobantes " +
                    "WHERE idAlmacen="+idAlmacen+" AND idProveedor=" + idProveedor + " AND tipoComprobante="+tipoComprobante+" AND FECHA BETWEEN '"+fechaInicial+"' AND '"+fechaFinal+"' " +
                    "ORDER BY FECHA DESC");
            while(rs.next()) {
                comprobantes.add(construir(rs));
            }
        } finally {
            cn.close();
        }
        return comprobantes;
    }
    
    public ArrayList<TOComprobante> obtenerSolicitudes(int idAlmacen) throws SQLException {
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        ArrayList<TOComprobante> comprobantes=new ArrayList<TOComprobante>();
        String strSQL="SELECT c.idComprobante, m.idAlmacen, c.idProveedor, c.tipoComprobante, c.serie, c.numero, c.idUsuario, c.fecha, c.statusOficina, c.statusAlmacen " +
                        "FROM movimientos m " +
                        "INNER JOIN comprobantes c ON c.idComprobante=m.idReferencia " +
                        "WHERE m.idTipo=2 AND m.status=1 AND c.idAlmacen="+idAlmacen+" " +
                        "ORDER BY c.fecha DESC";
        try {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                comprobantes.add(construir(rs));
            }
        } finally {
            cn.close();
        }
        return comprobantes;
    }
    
    public ArrayList<TOComprobante> obtenerComprobantes(int idAlmacen, int idProveedor, int tipoComprobante) throws SQLException {
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        ArrayList<TOComprobante> comprobantes=new ArrayList<TOComprobante>();
        try {
            String strSQL="SELECT * FROM comprobantes " +
                            "WHERE idAlmacen="+idAlmacen+" AND idProveedor="+idProveedor+" AND tipoComprobante="+tipoComprobante+ " " +
                            "ORDER BY fecha DESC";
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                comprobantes.add(construir(rs));
            }
        } finally {
            cn.close();
        }
        return comprobantes;
    }
    
    private TOComprobante construir(ResultSet rs) throws SQLException {
        TOComprobante c=new TOComprobante();
        c.setIdComprobante(rs.getInt("idComprobante"));
        c.setIdAlmacen(rs.getInt("idAlmacen"));
        c.setIdProveedor(rs.getInt("idProveedor"));
        c.setTipoComprobante(rs.getInt("tipoComprobante"));
        c.setRemision(rs.getString("remision"));
        c.setSerie(rs.getString("serie"));
        c.setNumero(rs.getString("numero"));
        c.setFecha(new java.util.Date(rs.getDate("fecha").getTime()));
        c.setStatusOficina(rs.getByte("statusOficina"));
        c.setStatusAlmacen(rs.getByte("statusAlmacen"));
        c.setPropietario(rs.getInt("propietario"));
        return c;
    }
}
