package proveedores.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import proveedores.dominio.ProveedorProductoPrecio;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jsolis
 */
public class DAOProveedoresProductosPrecios {
    private DataSource ds;
    
    public DAOProveedoresProductosPrecios() throws NamingException {
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
    
    public void eliminar(ProveedorProductoPrecio precio, int idProveedor, int idProducto) throws SQLException {
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        Format formatter=new SimpleDateFormat("yyyy-MM-dd");
        
        String strSQL="DELETE proveedoresPrecios\n"
                + " WHERE idProveedor="+idProveedor+" AND idProducto="+idProducto+" AND iniVigencia='"+formatter.format(precio.getIniVigencia())+"'";
        try {
            st.executeUpdate(strSQL);
        } finally {
            cn.close();
        }
    }
    
    public void modificar(ProveedorProductoPrecio precio, int idProveedor, int idProducto) throws SQLException {
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        Format formatter=new SimpleDateFormat("yyyy-MM-dd");
        
        String strSQL="UPDATE proveedoresPrecios\n"
                + "SET precioLista="+precio.getPrecioLista()
                + ", desctoProducto1="+precio.getDesctoProducto1()
                + ", desctoProducto2="+precio.getDesctoProducto2()
                + ", desctoConfidencial="+precio.getDesctoConfidencial();
                if(precio.getFinVigencia()!=null) {
                    strSQL+=", finVigencia='"+formatter.format(precio.getFinVigencia())+"'";
                }
                strSQL+="\n";
                strSQL+="WHERE idProveedor="+idProveedor+" AND idProducto="+idProducto+" AND iniVigencia='"+formatter.format(precio.getIniVigencia())+"'";
        try {
            st.executeUpdate(strSQL);
        } finally {
            cn.close();
        }
    }
    
    public boolean agregar(ProveedorProductoPrecio precio, int idProveedor, int idProducto) throws SQLException {
        boolean nuevo=true;
        String strSQL;
        Format formatter=new SimpleDateFormat("yyyy-MM-dd");
        
        if(precio.getFinVigencia()==null) {
            strSQL="INSERT INTO proveedoresPrecios (idProveedor, idProducto, fechaLista, precioLista, desctoProducto1, desctoProducto2, desctoConfidencial, iniVigencia)\n"
                + "VALUES ("+idProveedor+", "+idProducto+", CONVERT (date, GETDATE()), "+precio.getPrecioLista()+", "+precio.getDesctoProducto1()+", "+precio.getDesctoProducto2()+", "+precio.getDesctoConfidencial()+", '"+formatter.format(precio.getIniVigencia())+"')";
        } else {
            strSQL="INSERT INTO proveedoresPrecios (idProveedor, idProducto, fechaLista, precioLista, desctoProducto1, desctoProducto2, desctoConfidencial, inicioVigencia, finVigencia)\n"
                + "VALUES ("+idProveedor+", "+idProducto+", CONVERT (date, GETDATE()), "+precio.getPrecioLista()+", "+precio.getDesctoProducto1()+", "+precio.getDesctoProducto2()+", "+precio.getDesctoConfidencial()+", '"+formatter.format(precio.getIniVigencia())+"', '"+formatter.format(precio.getFinVigencia())+"')";
        }
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate(strSQL);
            nuevo=false;
        } finally {
            cn.close();
        }
        return nuevo;
    }
    
    public ProveedorProductoPrecio obtenerPrecio(int idProveedor, int idProducto, Date inicioVigencia) throws SQLException {
        java.sql.Date fecha=new java.sql.Date(inicioVigencia.getTime());
        ProveedorProductoPrecio precio=new ProveedorProductoPrecio();
        
        Connection cn=ds.getConnection();
        String strSQL="SELECT * FROM proveedoresPrecios\n"
                + "WHERE idProveedor="+idProveedor+" AND idProducto="+idProducto+"  AND iniVigencia='"+fecha.toString()+"'";
        try {
            Statement sentencia = cn.createStatement();
            ResultSet rs = sentencia.executeQuery(strSQL);
            if(rs.next()) {
                precio=this.construir(rs);
            }
        } finally {
            cn.close();
        }
        return precio;
    }
    
    public ArrayList<ProveedorProductoPrecio> obtenerPrecios(int idProveedor, int idProducto) throws SQLException {
        ArrayList<ProveedorProductoPrecio> lista=new ArrayList<>();
        
        Connection cn=ds.getConnection();
        String strSQL="SELECT * FROM proveedoresPrecios\n"
                + "WHERE idProveedor="+idProveedor+" AND idProducto="+idProducto+"\n"
                + "     AND (CONVERT(date, GETDATE()) BETWEEN iniVigencia AND finVigencia OR iniVigencia > CONVERT(date, GETDATE()))";
        try {
            Statement sentencia = cn.createStatement();
            ResultSet rs = sentencia.executeQuery(strSQL);
            while(rs.next()) {
                lista.add(this.construir(rs));
            }
        } finally {
            cn.close();
        }
        return lista;
    }
    
    private double calcularPrecioNeto(ProveedorProductoPrecio precio) {
        return Math.round(precio.getPrecioLista()*(1-precio.getDesctoProducto1()/100.00)*(1-precio.getDesctoProducto2()/100.00)*(1-precio.getDesctoConfidencial()/100.00)*100.00)/100.00;
    }
    
    public ProveedorProductoPrecio construir(ResultSet rs) throws SQLException {
        ProveedorProductoPrecio precio=new ProveedorProductoPrecio();
        precio.setFechaLista(utilerias.Utilerias.date2String(rs.getDate("fechaLista")));
        precio.setPrecioLista(rs.getDouble("precioLista"));
        precio.setDesctoProducto1(rs.getDouble("desctoProducto1"));
        precio.setDesctoProducto2(rs.getDouble("desctoProducto2"));
        precio.setDesctoConfidencial(rs.getDouble("desctoConfidencial"));
        precio.setPrecioNeto(calcularPrecioNeto(precio));
        precio.setIniVigencia(new java.util.Date(rs.getDate("iniVigencia").getTime()));
        Date finaliza=rs.getDate("finVigencia");
        if(finaliza==null) {
            precio.setFinVigencia(null);
        } else {
            precio.setFinVigencia(new java.util.Date(finaliza.getTime()));
        }
        precio.setNuevo(false);
        return precio;
    }
}
