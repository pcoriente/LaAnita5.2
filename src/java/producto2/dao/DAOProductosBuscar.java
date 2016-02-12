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
import producto2.to.TOProducto;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOProductosBuscar {
    private DataSource ds;
    
    public DAOProductosBuscar() throws NamingException {
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
    
//    public ArrayList<TOProducto> obtenerCombo(int idProducto) throws SQLException {
//        ArrayList<TOProducto> productos=new ArrayList<TOProducto>();
//        String strSQL=""
//                + "SELECT e.idEmpaque, e.cod_pro, e.idProducto, e.piezas, e.dun14, e.peso, e.volumen"
//                + "     , u.idUnidad as idUnidadEmpaque, u.unidad as unidadEmpaque, u.abreviatura as abreviaturaEmpaque"
//                + "     , isnull(se.idEmpaque, 0) as idSubEmpaque, se.piezas as piezasSubEmpaque"
//                + "     , su.idUnidad as idUnidadSubEmpaque, su.unidad as unidadSubEmpaque, su.abreviatura as abreviaturaSubEmpaque "
//                + "FROM empaquesCombos c "
//                + "INNER JOIN empaques e ON e.idEmpaque=c.idSubEmpaque "
//                + "INNER JOIN empaquesUnidades u ON u.idUnidad=e.idUnidadEmpaque "
//                + "LEFT JOIN empaques se ON se.idEmpaque=e.idSubEmpaque "
//                + "LEFT JOIN empaquesUnidades su ON su.idUnidad=se.idUnidadEmpaque "
//                + "WHERE c.idEmpaque="+idProducto;
//        Connection cn=ds.getConnection();
//        Statement st=cn.createStatement();
//        try {
//            ResultSet rs=st.executeQuery(strSQL);
//            while(rs.next()) {
//                productos.add(this.construir(rs));
//            }
//        } finally {
//            cn.close();
//        }
//        return productos;
//    }
    
    public ArrayList<TOProducto> obtenerSimilares(int idProducto) throws SQLException {
        ArrayList<TOProducto> toSimilares=new ArrayList<>();
        String strSQL=sqlEmpaque()+
                "INNER JOIN empaquesSimilares S ON S.idSimilar=e.idEmpaque\n" +
                "WHERE S.idEmpaque="+idProducto+" AND S.idSimilar!="+idProducto;
        Connection cn=ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                toSimilares.add(this.construir(rs));
            }
        } finally {
            cn.close();
        }
        return toSimilares;
    }
    
    public ArrayList<TOProducto> obtenerFormulasClasificacion(int idTipo, int idGrupo, int idSubGrupo, int idInsumo) throws SQLException {
        ArrayList<TOProducto> productos=new ArrayList<>();
        String strSQL=sqlEmpaque()+
            "INNER JOIN productos p on p.idProducto=e.idProducto\n" +
            "LEFT JOIN productosPartes pp on pp.idParte=p.idParte\n" +
            "INNER JOIN formulas F ON F.idEmpaque=e.idEmpaque\n";
        if(idInsumo!=0) {
            strSQL=strSQL+"INNER JOIN formulasInsumos I ON I.idFormula=F.idFormula\n";
        }
//        strSQL=strSQL+"WHERE ";
        if(idTipo!=0) {
            strSQL=strSQL+="WHERE p.idTipo="+idTipo;
        }
        if(idGrupo!=0) {
            strSQL=strSQL+(idTipo!=0?" AND ":"WHERE ")+"p.idGrupo="+idGrupo;
            if(idSubGrupo!=0) {
                strSQL=strSQL+" AND p.idSubGrupo="+idSubGrupo;
            }
        }
        if(idInsumo!=0) {
            strSQL=strSQL+(idTipo!=0 || idGrupo!=0?" AND ":"WHERE ")+"I.idProducto="+idInsumo;
        }
        strSQL=strSQL+"\nORDER BY p.idProducto, pp.parte, p.descripcion";
        Connection cn=ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                productos.add(this.construir(rs));
            }
        } finally {
            cn.close();
        }
        return productos;
    }
    
    public ArrayList<TOProducto> obtenerProductosClasificacion(int idGrupo, int idSubGrupo) throws SQLException {
        ArrayList<TOProducto> productos=new ArrayList<>();
        String strSQL=sqlEmpaque()+
            "INNER JOIN productos p on p.idProducto=e.idProducto\n" +
            "LEFT JOIN productosPartes pp on pp.idParte=p.idParte\n" +
            "WHERE p.idGrupo="+idGrupo;
        if(idSubGrupo!=0) {
            strSQL=strSQL+" AND p.idSubGrupo="+idSubGrupo;
        }
        strSQL=strSQL+"\nORDER BY pp.parte, p.descripcion";
        Connection cn=ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                productos.add(this.construir(rs));
            }
        } finally {
            cn.close();
        }
        return productos;
    }
    
    public ArrayList<TOProducto> obtenerProductosDescripcion(String descripcion) throws SQLException {
        ArrayList<TOProducto> productos=new ArrayList<>();
        String strSQL=sqlEmpaque()+
            "INNER JOIN productos p on p.idProducto=e.idProducto\n" +
            "LEFT JOIN productosPartes pp on pp.idParte=p.idParte\n" +
            "WHERE p.descripcion like '%"+descripcion+"%'\n"+
            "ORDER BY pp.parte, p.descripcion";
        Connection cn=ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                productos.add(this.construir(rs));
            }
        } finally {
            cn.close();
        }
        return productos;
    }
    
    public ArrayList<TOProducto> obtenerProductosParte(String parte) throws SQLException {
        ArrayList<TOProducto> productos=new ArrayList<>();
        String strSQL=sqlEmpaque()+
            "INNER JOIN productos p on p.idProducto=e.idProducto\n" +
            "WHERE p.idParte IN (SELECT idParte FROM productosPartes WHERE parte like '%"+parte+"%')\n"+
            "ORDER BY p.descripcion";
        Connection cn=ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                productos.add(this.construir(rs));
            }
        } finally {
            cn.close();
        }
        return productos;
    }
    
    public ArrayList<TOProducto> obtenerProductosParte(int idParte) throws SQLException {
        ArrayList<TOProducto> productos=new ArrayList<>();
        String strSQL=sqlEmpaque()+
            "INNER JOIN productos p on p.idProducto=e.idProducto\n" +
            "WHERE p.idParte="+idParte+"\n"+
            "ORDER BY p.descripcion";
        Connection cn=ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                productos.add(this.construir(rs));
            }
        } finally {
            cn.close();
        }
        return productos;
    }
    
    public ArrayList<TOProducto> obtenerProductos(int idArticulo) throws SQLException {
        ArrayList<TOProducto> productos=new ArrayList<>();
        String strSQL=sqlEmpaque()+
                "WHERE e.idProducto="+idArticulo+"\n" +
                "ORDER BY cod_pro";
        Connection cn=ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                productos.add(this.construir(rs));
            }
        } finally {
            cn.close();
        }
        return productos;
    }
    
    public TOProducto obtenerProductoSku(String sku) throws SQLException {
        TOProducto to=null;
        String strSQL=sqlEmpaque()+
                "WHERE e.cod_pro='"+sku+"'";
        Connection cn=ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                to=this.construir(rs);
            }
        } finally {
            cn.close();
        }
        return to;
    }
    
    public TOProducto obtenerProducto(int idProducto) throws SQLException {
        TOProducto to=null;
        String strSQL=sqlEmpaque()+
                "WHERE e.idEmpaque="+idProducto;
        Connection cn=ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                to=this.construir(rs);
            } else {
                throw new SQLException("No se encontro producto id="+idProducto+" o no tiene unidad de empaque !!!");
            }
        } finally {
            cn.close();
        }
        return to;
    }
    
//    public TOProducto construir(ResultSet rs) throws SQLException {
//        TOProducto to=new TOProducto();
//        to.setIdProducto(rs.getInt("idEmpaque"));
//        to.setCod_pro(rs.getString("cod_pro"));
//        to.setIdArticulo(rs.getInt("idProducto"));
//        to.setPiezas(rs.getInt("piezas"));
//        Empaque empaque=new Empaque(rs.getInt("idUnidadEmpaque"), rs.getString("unidadEmpaque"), rs.getString("abreviaturaEmpaque"));
//        to.setEmpaque(empaque);
//        SubProducto sub=new SubProducto(rs.getInt("idSubEmpaque"), rs.getInt("piezasSubEmpaque"), new Empaque(rs.getInt("idUnidadSubEmpaque"), rs.getString("unidadSubEmpaque"), rs.getString("abreviaturaSubEmpaque")));
//        to.setSubProducto(sub);
//        to.setDun14(rs.getString("dun14"));
//        to.setPeso(rs.getDouble("peso"));
//        to.setVolumen(rs.getDouble("volumen"));
//        return to;
//    }
    
    public TOProducto construir(ResultSet rs) throws SQLException {
        TOProducto to=new TOProducto();
        to.setIdProducto(rs.getInt("idEmpaque"));
        to.setCod_pro(rs.getString("cod_pro"));
        to.setIdArticulo(rs.getInt("idProducto"));
        to.setPiezas(rs.getInt("piezas"));
        Empaque empaque=new Empaque(rs.getInt("idUnidadEmpaque"), rs.getString("unidadEmpaque"), rs.getString("abreviaturaEmpaque"));
        to.setEmpaque(empaque);
        SubProducto sub=new SubProducto(rs.getInt("idSubEmpaque"));
        to.setSubProducto(sub);
        to.setDun14(rs.getString("dun14"));
        to.setPeso(rs.getDouble("peso"));
        to.setVolumen(rs.getDouble("volumen"));
        to.setSufijo(rs.getString("sufijo"));
        to.setDiasCaducidad(rs.getInt("diasCaducidad"));
        return to;
    }
    
//    private String sqlEmpaque() {
//        String strSQL=""
//                + "SELECT e.idEmpaque, e.cod_pro, e.idProducto, e.piezas, e.dun14, e.peso, e.volumen"
//                + "     , u.idUnidad as idUnidadEmpaque, u.unidad as unidadEmpaque, u.abreviatura as abreviaturaEmpaque"
//                + "     , isnull(se.idEmpaque, 0) as idSubEmpaque, se.piezas as piezasSubEmpaque"
//                + "     , su.idUnidad as idUnidadSubEmpaque, su.unidad as unidadSubEmpaque, su.abreviatura as abreviaturaSubEmpaque "
//                + "FROM empaques e "
//                + "INNER JOIN empaquesUnidades u ON u.idUnidad=e.idUnidadEmpaque "
//                + "LEFT JOIN empaques se ON se.idEmpaque=e.idSubEmpaque "
//                + "LEFT JOIN empaquesUnidades su ON su.idUnidad=se.idUnidadEmpaque";
//        return strSQL;
//    }
    
     private String sqlEmpaque() {
        String strSQL=""
                + "SELECT e.idEmpaque, e.cod_pro, e.idProducto, e.piezas, e.idSubEmpaque, e.dun14, e.peso, e.volumen\n"
                + "     , u.idUnidad as idUnidadEmpaque, u.unidad as unidadEmpaque, u.abreviatura as abreviaturaEmpaque\n"
                + "     , e.sufijo, e.diasCaducidad\n"
                + "FROM empaques e\n"
                + "INNER JOIN empaquesUnidades u ON u.idUnidad=e.idUnidadEmpaque\n";
        return strSQL;
    }
}
