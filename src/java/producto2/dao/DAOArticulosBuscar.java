package producto2.dao;

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
import producto2.dominio.Articulo;
import producto2.dominio.Grupo;
import producto2.dominio.Marca;
import producto2.dominio.Parte;
import producto2.dominio.Presentacion;
import producto2.dominio.SubGrupo;
import producto2.dominio.Tipo;
import unidadesMedida.UnidadMedida;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOArticulosBuscar {
    private String tabla="productos";
    private DataSource ds;
    
    public DAOArticulosBuscar() throws NamingException {
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
    
    public Articulo obtenerArticuloUPC(String upc) throws SQLException {
        Articulo articulo=null;
        String strSQL=""
                + "SELECT a.idProducto, pa.idParte, pa.parte, a.descripcion, t.idTipo, t.tipo,"
                + "     isnull(sg.idSubGrupo, 0) as idSubGrupo, isnull(sg.subGrupo, '') as subGrupo, "
                + "     isnull(g.idGrupo, 0) as idGrupo, isnull(g.codigoGrupo, 0) as codigoGrupo, isnull(g.grupo, '') as grupo, "
                + "     isnull(m.idMarca, 0) as idMarca, isnull(m.marca, '') as marca, "
                + "     COALESCE(u.idPresentacion, 0) AS idPresentacion, COALESCE(u.presentacion, '') AS presentacion, COALESCE(u.abreviatura, '') AS abreviatura, p.contenido,"
                + "     COALESCE(um.idUnidadMedida, 0) AS idUnidadMedida, COALESCE(um.unidadMedida, '') as unidadMedida, COALESCE(um.abreviatura, '') as medAbrev, 0 as idTipoUnidadMedida,"
                + "     i.idGrupo as idImpuestoGrupo, i.grupo as impuestoGrupo "
                + "FROM "+this.tabla+"Upcs u "
                + "INNER JOIN "+this.tabla+" a ON a.idProducto=u.idProducto"
                + "INNER JOIN "+this.tabla+"Partes pa ON pa.idParte=a.idParte "
                + "INNER JOIN "+this.tabla+"Tipos t ON t.idTipo=a.idTipo "
                + "LEFT JOIN "+this.tabla+"SubGrupos sg ON sg.idSubGrupo=a.idSubGrupo "
                + "LEFT JOIN "+this.tabla+"Grupos g ON g.idGrupo=a.idGrupo "
                + "LEFT JOIN "+this.tabla+"Presentaciones u ON u.idPresentacion=a.idPresentacion "
                + "LEFT JOIN unidadesMedida um ON um.idUnidadMedida=a.idUnidadMedida "
                + "LEFT JOIN "+this.tabla+"Marcas m on m.idMarca=a.idMarca "
                + "INNER JOIN impuestosGrupos i ON i.idGrupo=a.idImpuesto "
                + "WHERE u.upc='"+upc+"'";
        Connection cn=ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                articulo=construir(rs);
            }
        } finally {
            cn.close();
        }
        return articulo;
    }
    
    public ArrayList<Articulo> obtenerArticulos(String descripcion) throws SQLException {
        ArrayList<Articulo> articulos=new ArrayList<>();
        String strSQL=sqlArticulo() +" WHERE a.descripcion like '%"+descripcion+"%'";
        Connection cn=ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                articulos.add(construir(rs));
            }
        } finally {
            cn.close();
        }
        return articulos;
    }
    
    public ArrayList<Articulo> obtenerArticulos(Parte parte) throws SQLException {
        ArrayList<Articulo> articulos=new ArrayList<>();
        String strSQL=sqlArticulo() +" WHERE a.idParte IN (SELECT idParte FROM "+this.tabla+"Partes WHERE parte like '%"+parte.getParte()+"%')";
        Connection cn=ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                articulos.add(construir(rs));
            }
        } finally {
            cn.close();
        }
        return articulos;
    }
    
    public ArrayList<Articulo> obtenerArticulos(int idParte) throws SQLException {
        ArrayList<Articulo> articulos=new ArrayList<>();
        String strSQL=sqlArticulo() +" WHERE a.idParte="+idParte;
        Connection cn=ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                articulos.add(construir(rs));
            }
        } finally {
            cn.close();
        }
        return articulos;
    }
    
    public Articulo obtenerArticulo(int idArticulo) throws SQLException {
        Articulo articulo=null;
        String strSQL=sqlArticulo() + " WHERE a.idProducto="+idArticulo;
        Connection cn=ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                articulo=construir(rs);
            }
        } finally {
            cn.close();
        }
        return articulo;
    }
    
    private Articulo construir(ResultSet rs) throws SQLException {
        Articulo articulo=new Articulo();
        articulo.setIdArticulo(rs.getInt("idProducto"));
        articulo.setParte(new Parte(rs.getInt("idParte"), rs.getString("parte")));
        articulo.setDescripcion(rs.getString("descripcion"));
        articulo.setMarca(new Marca(rs.getInt("idMarca"), rs.getString("marca"), false));
        articulo.setTipo(new Tipo(rs.getInt("idTipo"), rs.getString("tipo")));
        articulo.setGrupo(new Grupo(rs.getInt("idGrupo"), rs.getInt("codigoGrupo"), rs.getString("grupo")));
        articulo.setSubGrupo(new SubGrupo(rs.getInt("idSubGrupo"), rs.getString("subGrupo")));
        articulo.setPresentacion(new Presentacion(rs.getInt("idPresentacion"), rs.getString("presentacion"), rs.getString("abreviatura")));
        articulo.setContenido(rs.getDouble("contenido"));
        articulo.setUnidadMedida(new UnidadMedida(rs.getInt("idUnidadMedida"), rs.getString("unidadMedida"), rs.getString("medAbrev")));
        articulo.setImpuestoGrupo(new ImpuestoGrupo(rs.getInt("idImpuestoGrupo"), rs.getString("impuestoGrupo")));
        return articulo;
    }
    
    private String sqlArticulo() {
        String strSQL=""
                + "SELECT a.idProducto, pa.idParte, pa.parte, a.descripcion, "
                + "     isnull(t.idTipo, 0) as idTipo, isnull(t.tipo, '') as tipo,"
                + "     isnull(g.idGrupo, 0) as idGrupo, isnull(g.codigoGrupo, 0) as codigoGrupo, isnull(g.grupo, '') as grupo, "
                + "     isnull(sg.idSubGrupo, 0) as idSubGrupo, isnull(sg.subGrupo, '') as subGrupo, "
                + "     isnull(m.idMarca, 0) as idMarca, isnull(m.marca, '') as marca, "
                + "     COALESCE(u.idPresentacion, 0) AS idPresentacion, COALESCE(u.presentacion, '') AS presentacion, COALESCE(u.abreviatura, '') AS abreviatura, a.contenido,"
                + "     COALESCE(um.idUnidadMedida, 0) AS idUnidadMedida, COALESCE(um.unidadMedida, '') as unidadMedida, COALESCE(um.abreviatura, '') as medAbrev, 0 as idTipoUnidadMedida,"
                + "     i.idGrupo as idImpuestoGrupo, i.grupo as impuestoGrupo "
                + "FROM "+this.tabla+" a "
                + "INNER JOIN "+this.tabla+"Partes pa ON pa.idParte=a.idParte "
                + "INNER JOIN "+this.tabla+"Tipos t ON t.idTipo=a.idTipo "
                + "LEFT JOIN "+this.tabla+"SubGrupos sg ON sg.idSubGrupo=a.idSubGrupo "
                + "LEFT JOIN "+this.tabla+"Grupos g ON g.idGrupo=a.idGrupo "
                + "LEFT JOIN "+this.tabla+"Presentaciones u ON u.idPresentacion=a.idPresentacion "
                + "LEFT JOIN unidadesMedida um ON um.idUnidadMedida=a.idUnidadMedida "
                + "LEFT JOIN "+this.tabla+"Marcas m on m.idMarca=a.idMarca "
                + "INNER JOIN impuestosGrupos i ON i.idGrupo=a.idImpuesto";
        return strSQL;
    }
}
