package proveedores.dao;

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
import producto2.dominio.Empaque;
import producto2.dominio.Marca;
import producto2.dominio.Presentacion;
import producto2.dominio.Producto;
import proveedores.dominio.ProveedorProducto;
import unidadesMedida.UnidadMedida;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jsolis
 */
public class DAOProveedoresProductos {
    private DataSource ds;
    
    public DAOProveedoresProductos() throws NamingException {
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
    
    public void modificar(ProveedorProducto pp, int idProveedor) throws SQLException {
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        String strSQL="UPDATE proveedoresProductos"
                + " SET sku='"+pp.getSku()+"'"
                + ",    diasEntrega="+pp.getDiasEntrega()
                + ",    idUnidadEmpaque="+pp.getEmpaque().getIdEmpaque()
                + ",    piezas="+pp.getPiezas()
                + ",    idMarca="+pp.getMarca().getIdMarca()
                + ",    producto='"+pp.getProducto()+"'"
                + ",    idPresentacion="+pp.getPresentacion().getIdPresentacion()
                + ",    contenido="+pp.getContenido()
                + ",    idUnidadMedida="+pp.getUnidadMedida().getIdUnidadMedida()
                + ",    idUnidadMedida2="+pp.getUnidadMedida2().getIdUnidadMedida()
                + ",    idImpuestosGrupo="+pp.getImpuestoGrupo().getIdGrupo()
                + ",    idEquivalencia="+pp.getEquivalencia().getIdProducto()
                + " WHERE idProveedor="+idProveedor+" AND idProducto="+pp.getIdProducto();
        try {
            st.executeUpdate(strSQL);
        } finally {
            cn.close();
        }
    }
    
    public int agregar(ProveedorProducto pp, int idProveedor) throws SQLException {
        int idProducto=0;
        String strSQL="INSERT INTO proveedoresProductos (idProveedor, sku, diasEntrega, idUnidadEmpaque, piezas, idMarca, producto, idPresentacion, contenido, idUnidadMedida, idUnidadMedida2, idImpuestosGrupo, idEquivalencia) "
                    + "     VALUES ("+idProveedor+", '"+pp.getSku()+"', "+pp.getDiasEntrega()+", "+pp.getEmpaque().getIdEmpaque()+", "+pp.getPiezas()+", "+pp.getMarca().getIdMarca()+""
                    + "             , '"+pp.getProducto()+"', "+pp.getPresentacion().getIdPresentacion()+", "+pp.getContenido()+""
                    + "             , "+pp.getUnidadMedida().getIdUnidadMedida()+", "+pp.getUnidadMedida2().getIdUnidadMedida()+", "+pp.getImpuestoGrupo().getIdGrupo()+", "+pp.getEquivalencia().getIdProducto()+")";
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate("begin transaction");
            st.executeUpdate(strSQL);
            ResultSet rs=st.executeQuery("SELECT @@IDENTITY AS idProducto");
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
    
    public ArrayList<ProveedorProducto> obtenerProductos(int idProveedor) throws SQLException {
        ArrayList<ProveedorProducto> lista=new ArrayList<ProveedorProducto>();
        
        Connection cn=ds.getConnection();
        String strSQL=this.sqlProducto()+" WHERE p.idProveedor="+idProveedor+" ORDER BY p.sku";
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
    
    public ProveedorProducto obtenerProducto(int idProveedor, int idProducto) throws SQLException {
        ProveedorProducto p=null;
        
        Connection cn=ds.getConnection();
        String strSQL=this.sqlProducto()+" WHERE p.idProveedor="+idProveedor+" AND idProducto="+idProducto;
        try {
            Statement sentencia = cn.createStatement();
            ResultSet rs = sentencia.executeQuery(strSQL);
            if(rs.next()) {
                p=construir(rs);
            }
        } finally {
            cn.close();
        }
        return p;
    }
    
    private ProveedorProducto construir(ResultSet rs) throws SQLException {
        ProveedorProducto pp=new ProveedorProducto();
        pp.setIdProducto(rs.getInt("idProducto"));
        pp.setSku(rs.getString("sku"));
        pp.setDiasEntrega(rs.getInt("diasEntrega"));
        pp.setUltimaCompraFecha(rs.getDate("ultimaCompraFecha"));
        pp.setUltimaCompraPrecio(rs.getDouble("ultimaCompraPrecio"));
        pp.setEmpaque(new Empaque(rs.getInt("idUnidad"), rs.getString("unidad"), rs.getString("unidAbrev")));
        pp.setPiezas(rs.getInt("piezas"));
        pp.setMarca(new Marca(rs.getInt("idMarca"), rs.getString("marca"), false));
        pp.setProducto(rs.getString("producto"));
        pp.setPresentacion(new Presentacion(rs.getInt("idPresentacion"), rs.getString("presentacion"), rs.getString("presAbrev")));
        pp.setContenido(rs.getDouble("contenido"));
        pp.setUnidadMedida(new UnidadMedida(rs.getInt("idUnidadMedida1"), rs.getString("unidadMedida1"), rs.getString("abreviatura1")));
        pp.setUnidadMedida2(new UnidadMedida(rs.getInt("idUnidadMedida2"), rs.getString("unidadMedida2"), rs.getString("abreviatura2")));
        pp.setImpuestoGrupo(new ImpuestoGrupo(rs.getInt("idGrupo"), rs.getString("grupo")));
        pp.setEquivalencia(new Producto());
        pp.getEquivalencia().setIdProducto(rs.getInt("idEquivalencia"));
        return pp;
    }
    
    private String sqlProducto() {
        return "select p.idProducto, p.sku, p.diasEntrega, p.ultimaCompraFecha, p.ultimaCompraPrecio, p.idEquivalencia\n" +
                "	,u.idUnidad, u.unidad, u.abreviatura as unidAbrev\n" +
                "       , p.piezas\n" +
                "	,isnull(m.idMarca, 0) as idMarca, isnull(m.marca, '') as marca\n" +
                "       , p.producto\n" +
                "       ,pp.idPresentacion, pp.presentacion, pp.abreviatura as presAbrev\n" +
                "       , p.contenido\n" +
                "	,um1.idUnidadMedida as idUnidadMedida1, um1.unidadMedida as unidadMedida1, um1.abreviatura as abreviatura1\n" +
                "	,isnull(um2.idUnidadMedida, 0) as idUnidadMedida2, isnull(um2.unidadMedida, '') as unidadMedida2, isnull(um2.abreviatura, '') as abreviatura2\n" +
                "       , i.idGrupo, i.grupo \n" +
                "from proveedoresProductos p\n" +
                "inner join empaquesUnidades u on u.idUnidad=p.idUnidadEmpaque\n" +
                "left join productosMarcas m on m.idMarca=p.idMarca\n" +
                "inner join productosPresentaciones pp on pp.idPresentacion=p.idPresentacion\n" +
                "inner join unidadesMedida um1 on um1.idUnidadMedida=p.idUnidadMedida\n" +
                "left join unidadesMedida um2 on um2.idUnidadMedida=p.idUnidadMedida2\n" +
                "inner join impuestosGrupos i on i.idGrupo=p.idImpuestosGrupo";
    }
}
