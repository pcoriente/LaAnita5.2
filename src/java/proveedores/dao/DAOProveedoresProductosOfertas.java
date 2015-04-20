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
import proveedores.dominio.ProveedorProductoOferta;
import usuarios.dominio.UsuarioSesion;
import utilerias.Utilerias;

/**
 *
 * @author jsolis
 */
public class DAOProveedoresProductosOfertas {
    private DataSource ds;
    
    public DAOProveedoresProductosOfertas() throws NamingException {
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
    
    public void eliminar(ProveedorProductoOferta oferta, int idProveedor, int idProducto) throws SQLException {
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        Format formatter=new SimpleDateFormat("yyyy-MM-dd");
        
        String strSQL="DELETE proveedoresOfertas "
                + " WHERE idProveedor="+idProveedor+" AND idProducto="+idProducto+" AND inicioVigencia='"+formatter.format(oferta.getInicioVigencia())+"'";
        try {
            st.executeUpdate(strSQL);
        } finally {
            cn.close();
        }
    }
    
    public void modificar(ProveedorProductoOferta oferta, int idProveedor, int idProducto) throws SQLException {
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        Format formatter=new SimpleDateFormat("yyyy-MM-dd");
        
        String strSQL="UPDATE proveedoresOfertas "
                + "SET ptjeOferta="+oferta.getPtjeOferta()
                + ", precioOferta="+oferta.getPrecioOferta()
                + ", sinCargoBase="+oferta.getBase()
                + ", sinCargoCant="+oferta.getSinCargo();
                if(oferta.getFinVigencia()!=null) {
                    strSQL+=", finVigencia='"+formatter.format(oferta.getFinVigencia())+"'";
                }
                strSQL+=" WHERE idProveedor="+idProveedor+" AND idProducto="+idProducto+" AND inicioVigencia='"+formatter.format(oferta.getInicioVigencia())+"'";
        try {
            st.executeUpdate(strSQL);
        } finally {
            cn.close();
        }
    }
    
    public boolean agregar(ProveedorProductoOferta oferta, int idProveedor, int idProducto) throws SQLException {
        boolean nuevo=true;
        String strSQL;
        Format formatter=new SimpleDateFormat("yyyy-MM-dd");
        
        if(oferta.getFinVigencia()==null) {
            strSQL="INSERT INTO proveedoresOfertas (idProveedor, idProducto, fechaLista, precioOferta, ptjeOferta, sinCargoBase, sinCargoCant, inicioVigencia) "
                + "VALUES ("+idProveedor+", "+idProducto+", CONVERT (date, GETDATE()), "+oferta.getPrecioOferta()+", "+oferta.getPtjeOferta()+", "+oferta.getBase()+", "+oferta.getSinCargo()+", '"+formatter.format(oferta.getInicioVigencia())+"')";
        } else {
            strSQL="INSERT INTO proveedoresOfertas (idProveedor, idProducto, fechaLista, precioOferta, ptjeOferta, sinCargoBase, sinCargoCant, inicioVigencia, finVigencia) "
                + "VALUES ("+idProveedor+", "+idProducto+", CONVERT (date, GETDATE()), "+oferta.getPrecioOferta()+", "+oferta.getPtjeOferta()+", "+oferta.getBase()+", "+oferta.getSinCargo()+", '"+formatter.format(oferta.getInicioVigencia())+"', '"+formatter.format(oferta.getFinVigencia())+"')";
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
    
    public ProveedorProductoOferta obtenerOferta(int idProveedor, int idProducto, Date inicioVigencia) throws SQLException {
        java.sql.Date fecha=new java.sql.Date(inicioVigencia.getTime());
        ProveedorProductoOferta precio=new ProveedorProductoOferta();
        
        Connection cn=ds.getConnection();
        String strSQL="SELECT fechaLista, ptjeOferta, precioOferta, sinCargoBase, sinCargoCant, inicioVigencia, finVigencia "
                + "FROM proveedoresOfertas "
                + "WHERE idProveedor="+idProveedor+" AND idProducto="+idProducto+"  AND inicioVigencia='"+fecha.toString()+"'";
        try {
            Statement sentencia = cn.createStatement();
            ResultSet rs = sentencia.executeQuery(strSQL);
            if(rs.next()) {
                precio=construir(rs);
            }
        } finally {
            cn.close();
        }
        return precio;
    }
    
    public ArrayList<ProveedorProductoOferta> obtenerOfertas(int idProveedor, int idProducto) throws SQLException {
        ArrayList<ProveedorProductoOferta> lista=new ArrayList<ProveedorProductoOferta>();
        
        Connection cn=ds.getConnection();
        String strSQL="SELECT fechaLista, ptjeOferta, precioOferta, sinCargoBase, sinCargoCant, inicioVigencia, finVigencia "
                + "FROM proveedoresOfertas "
                + "WHERE idProveedor="+idProveedor+" AND idProducto="+idProducto+" "
                + "     AND (CONVERT(date, GETDATE()) BETWEEN inicioVigencia AND finVigencia OR inicioVigencia > CONVERT(date, GETDATE()))";
        try {
            Statement sentencia = cn.createStatement();
            ResultSet rs = sentencia.executeQuery(strSQL);
            while(rs.next()) {
                lista.add(construir(rs));
            }
        } finally {
            cn.close();
        }
        return lista;
    }
    
    public ProveedorProductoOferta construir(ResultSet rs) throws SQLException {
        ProveedorProductoOferta oferta=new ProveedorProductoOferta();
        oferta.setFechaLista(Utilerias.date2String(rs.getDate("fechaLista")));
        oferta.setPtjeOferta(rs.getDouble("ptjeOferta"));
        oferta.setPrecioOferta(rs.getDouble("precioOferta"));
        oferta.setBase(rs.getInt("sinCargoBase"));
        oferta.setSinCargo(rs.getInt("sinCargoCant"));
        oferta.setInicioVigencia(new java.util.Date(rs.getDate("inicioVigencia").getTime()));
        Date finaliza=rs.getDate("finVigencia");
        if(finaliza==null) {
            oferta.setFinVigencia(null);
        } else {
            oferta.setFinVigencia(new java.util.Date(finaliza.getTime()));
        }
        oferta.setNuevo(false);
        return oferta;
    }
}
