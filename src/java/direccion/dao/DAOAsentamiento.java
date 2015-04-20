package direccion.dao;

import direccion.dominio.Asentamiento;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author Julio
 */
public class DAOAsentamiento {
    private String tabla="sepomex";
    private DataSource ds;

    public DAOAsentamiento() throws NamingException {
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
    
    public Asentamiento obtener(String codAsentamiento) throws SQLException {
        Asentamiento asentamiento=null;
        ResultSet rs=null;
        String cEstado=codAsentamiento.substring(0, 2);
        String cMunicipio=codAsentamiento.substring(2,5);
        String cAsenta=codAsentamiento.substring(5);
        Connection cn=ds.getConnection();
        String strSQL="SELECT * FROM "+this.tabla+" "
                + "WHERE c_estado=? AND c_mnpio=? AND id_asenta_cpcons=?";
        try {
            PreparedStatement sentencia = cn.prepareStatement(strSQL);
            sentencia.setString(1, cEstado);
            sentencia.setString(2, cMunicipio);
            sentencia.setString(3, cAsenta);
            rs = sentencia.executeQuery();
            if(rs.next()) asentamiento=construir(rs);
        } finally {
            cn.close();
        }
        return asentamiento;
    }
    
    public Asentamiento[] obtenerAsentamientos(String codigoPostal) throws SQLException {
        Asentamiento[] asentamientos;
        ResultSet rs=null;
        
        Connection cn=ds.getConnection();
        String strSQL="SELECT * FROM "+this.tabla+" "
                + "WHERE d_codigo='"+codigoPostal+"' ORDER BY d_asenta";
        try {
            Statement sentencia = cn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = sentencia.executeQuery(strSQL);
            if(rs.next()) {
                rs.last();
                asentamientos=new Asentamiento[rs.getRow()];
                
                int i=0;
                rs.beforeFirst();
                while(rs.next()) {
                    asentamientos[i++]=construir(rs);
                }
            } else {
                asentamientos=new Asentamiento[0];
            }
        } finally {
            cn.close();
        }
        return asentamientos;
    }
    
    private Asentamiento construir(ResultSet rs) throws SQLException {
//        String str;
        Asentamiento asentamiento=new Asentamiento();
        asentamiento.setCodAsentamiento(rs.getString("c_estado")+rs.getString("c_mnpio")+rs.getString("id_asenta_cpcons"));
        asentamiento.setCodigoPostal(rs.getString("d_codigo"));
        asentamiento.setcTipo(rs.getString("c_tipo_asenta"));
        asentamiento.setTipo(rs.getString("d_tipo_asenta"));
        asentamiento.setAsentamiento(rs.getString("d_asenta"));
        asentamiento.setCodEstado(rs.getString("c_estado"));
        asentamiento.setEstado(rs.getString("d_estado"));
        asentamiento.setCodMunicipio(rs.getString("c_mnpio"));
        asentamiento.setMunicipio(rs.getString("d_mnpio"));
        asentamiento.setCiudad(rs.getString("d_ciudad"));
//        str=rs.getString("d_ciudad");
//        if(str.isEmpty()) {
//            asentamiento.setCiudad(rs.getString("d_asenta"));
//            asentamiento.setAsentamiento("");
//        } else {
//            asentamiento.setCiudad(str);
//            asentamiento.setAsentamiento(rs.getString("d_asenta"));
//        } 
        return asentamiento;
    }
}
