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
import producto2.dominio.Parte;
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
            ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
        } catch (NamingException ex) {
            throw (ex);
        }
    }

    public void modificar(int idEmpresa, int idProveedor, ProveedorProducto pp) throws SQLException {
        String strSQL = "UPDATE proveedoresProductos\n"
                + "SET sku='" + pp.getSku() + "'"
                + ", idUnidadEmpaque=" + pp.getEmpaque().getIdEmpaque()
                + ", piezas=" + pp.getPiezas()
                + ", idMarca=" + pp.getMarca().getIdMarca()
                + ", idParte=" + pp.getParte().getIdParte()
                + ", descripcion='" + pp.getDescripcion() + "'"
                + ", idPresentacion=" + pp.getPresentacion().getIdPresentacion()
                + ", contenido=" + pp.getContenido()
                + ", idUnidadMedida=" + pp.getUnidadMedida().getIdUnidadMedida()
                + ", idUnidadMedida2=" + pp.getUnidadMedida2().getIdUnidadMedida()
                + ", idImpuestosGrupo=" + pp.getImpuestoGrupo().getIdGrupo()
                + ", diasEntrega=" + pp.getDiasEntrega() + "\n"
                + "WHERE idEmpresa=" + idEmpresa + " AND idProveedor=" + idProveedor + " AND idEmpaque=" + pp.getEquivalencia().getIdProducto();
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            st.executeUpdate(strSQL);
        } finally {
            cn.close();
        }
    }

    public void agregar(int idEmpresa, int idProveedor, ProveedorProducto pp) throws SQLException {
        String strSQL = "INSERT INTO proveedoresProductos (idEmpresa, idProveedor, idEmpaque, sku, idUnidadEmpaque, piezas, idMarca, idParte, descripcion, idPresentacion, contenido, idUnidadMedida, idUnidadMedida2, idImpuestosGrupo, diasEntrega) "
                + "     VALUES (" + idEmpresa + ", " + idProveedor + ", " + pp.getEquivalencia().getIdProducto() + ", '" + pp.getSku() + "', " + pp.getEmpaque().getIdEmpaque() + ", " + pp.getPiezas() + ", " + pp.getMarca().getIdMarca() + ""
                + "             , " + pp.getParte().getIdParte() + ", '" + pp.getDescripcion() + "', " + pp.getPresentacion().getIdPresentacion() + ", " + pp.getContenido() + ""
                + "             , " + pp.getUnidadMedida().getIdUnidadMedida() + ", " + pp.getUnidadMedida2().getIdUnidadMedida() + ", " + pp.getImpuestoGrupo().getIdGrupo() + ", " + pp.getDiasEntrega() + ")";
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            st.executeUpdate(strSQL);
            pp.setNuevo(false);
        } finally {
            cn.close();
        }
    }

    public ArrayList<ProveedorProducto> obtenerProductos(int idEmpresa, int idProveedor) throws SQLException {
        ArrayList<ProveedorProducto> lista = new ArrayList<>();

        Connection cn = ds.getConnection();
        String strSQL = this.sqlProducto() + "\n"
                + "WHERE p.idEmpresa=" + idEmpresa + " AND p.idProveedor=" + idProveedor + "\n"
                + "ORDER BY p.sku";
        try {
            Statement sentencia = cn.createStatement();
            ResultSet rs = sentencia.executeQuery(strSQL);
            while (rs.next()) {
                lista.add(this.construir(rs));
            }
        } finally {
            cn.close();
        }
        return lista;
    }

    public ProveedorProducto obtenerProducto(int idEmpresa, int idProveedor, int idProducto) throws SQLException {
        ProveedorProducto p = null;

        Connection cn = ds.getConnection();
        String strSQL = this.sqlProducto() + "\n"
                + "WHERE p.idEmpresa=" + idEmpresa + " AND p.idProveedor=" + idProveedor + " AND p.idEmpaque=" + idProducto;
        try {
            Statement sentencia = cn.createStatement();
            ResultSet rs = sentencia.executeQuery(strSQL);
            if (rs.next()) {
                p = this.construir(rs);
            }
        } finally {
            cn.close();
        }
        return p;
    }

    private ProveedorProducto construir(ResultSet rs) throws SQLException {
        ProveedorProducto pp = new ProveedorProducto();
        pp.setNuevo(false);
        pp.setEquivalencia(new Producto());
        pp.getEquivalencia().setIdProducto(rs.getInt("idEmpaque"));
        pp.setSku(rs.getString("sku"));
        pp.setDiasEntrega(rs.getInt("diasEntrega"));
        pp.setEmpaque(new Empaque(rs.getInt("idUnidad"), rs.getString("unidad"), rs.getString("unidAbrev")));
        pp.setPiezas(rs.getInt("piezas"));
        pp.setMarca(new Marca(rs.getInt("idMarca"), rs.getString("marca"), false));
        pp.setParte(new Parte(rs.getInt("idParte"), rs.getString("parte")));
        pp.setDescripcion(rs.getString("descripcion"));
        pp.setPresentacion(new Presentacion(rs.getInt("idPresentacion"), rs.getString("presentacion"), rs.getString("presAbrev")));
        pp.setContenido(rs.getDouble("contenido"));
        pp.setUnidadMedida(new UnidadMedida(rs.getInt("idUnidadMedida1"), rs.getString("unidadMedida1"), rs.getString("abreviatura1")));
        pp.setUnidadMedida2(new UnidadMedida(rs.getInt("idUnidadMedida2"), rs.getString("unidadMedida2"), rs.getString("abreviatura2")));
        pp.setImpuestoGrupo(new ImpuestoGrupo(rs.getInt("idGrupo"), rs.getString("grupo")));
        pp.setUltimaCompraFecha(new java.util.Date(rs.getTimestamp("ultimaCompraFecha").getTime()));
        pp.setUltimaCompraPrecio(rs.getDouble("ultimaCompraPrecio"));
        return pp;
    }

    private String sqlProducto() {
        return "select p.idEmpaque\n"
                + " , p.sku\n"
                + " , u.idUnidad, u.unidad, u.abreviatura as unidAbrev\n"
                + " , p.piezas\n"
                + " , isnull(m.idMarca, 0) as idMarca, isnull(m.marca, '') as marca\n"
                + " , isnull(ppt.idParte, 0) as idParte, isnull(ppt.parte, '') as parte\n"
                + " , p.descripcion\n"
                + " , pp.idPresentacion, pp.presentacion, pp.abreviatura as presAbrev\n"
                + " , p.contenido\n"
                + " , um1.idUnidadMedida as idUnidadMedida1, um1.unidadMedida as unidadMedida1, um1.abreviatura as abreviatura1\n"
                + " , isnull(um2.idUnidadMedida, 0) as idUnidadMedida2, isnull(um2.unidadMedida, '') as unidadMedida2, isnull(um2.abreviatura, '') as abreviatura2\n"
                + " , i.idGrupo, i.grupo\n"
                + " , p.diasEntrega\n"
                + " , isnull(d.fecha, '1900-01-01') as ultimaCompraFecha, isnull(d.unitario, 0.00) as ultimaCompraPrecio\n"
                + "from proveedoresProductos p\n"
                + "inner join empaquesUnidades u on u.idUnidad=p.idUnidadEmpaque\n"
                + "left join productosMarcas m on m.idMarca=p.idMarca\n"
                + "inner join productosPartes ppt on ppt.idParte=p.idParte\n"
                + "inner join productosPresentaciones pp on pp.idPresentacion=p.idPresentacion\n"
                + "inner join unidadesMedida um1 on um1.idUnidadMedida=p.idUnidadMedida\n"
                + "left join unidadesMedida um2 on um2.idUnidadMedida=p.idUnidadMedida2\n"
                + "inner join impuestosGrupos i on i.idGrupo=p.idImpuestosGrupo\n"
                + "left join movimientosDetalle d on d.idMovto=p.idMovtoUltimaCompra and d.idEmpaque=p.idEmpaque\n";
    }
}
