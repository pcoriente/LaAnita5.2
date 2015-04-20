package cotizaciones.dao;

import cotizaciones.dominio.CotizacionDetalle;
import cotizaciones.dominio.CotizacionEncabezado;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
import producto2.dominio.Producto;
import usuarios.dominio.UsuarioSesion;

public class DAOCotizaciones {

    private final DataSource ds;
    private UsuarioSesion usuarioSesion;

    public DAOCotizaciones() throws NamingException {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");

            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
        } catch (NamingException ex) {
            throw (ex);
        }
    }

    public ArrayList<CotizacionEncabezado> listaCotizaciones() throws SQLException {
        ArrayList<CotizacionEncabezado> lista = new ArrayList<CotizacionEncabezado>();
        ResultSet rs;
        Connection cn = ds.getConnection();
        try {

            String stringSQL = "SELECT c.idRequisicion,  ed.depto, r.fechaRequisicion, r.fechaAprobacion,  COUNT(distinct cd.idEmpaque) as numProductos, COUNT(distinct cd.idCotizacion)as numCotizaciones,c.estado,c.idMoneda\n"
                    + "                    FROM cotizaciones c\n"
                    + "                    INNER JOIN cotizacionesDetalle cd ON cd.idCotizacion= c.idCotizacion\n"
                    + "                    INNER JOIN requisiciones r ON r.idRequisicion = c.idRequisicion\n"
                    + "                    INNER JOIN empleadosDeptos ed ON ed.idDepto = r.idDepto\n"
                    + "                    where c.estado =1\n"
                    + "                    GROUP BY c.idRequisicion,  ed.depto, r.fechaRequisicion, r.fechaAprobacion, c.estado, c.idMoneda"
                    + "                    order by  idRequisicion desc";
            Statement sentencia = cn.createStatement();
            rs = sentencia.executeQuery(stringSQL);
            while (rs.next()) {
                lista.add(construir(rs));
            }
        } finally {
            cn.close();
        }
        return lista;
    }

    private CotizacionEncabezado construir(ResultSet rs) throws SQLException {
        CotizacionEncabezado ce = new CotizacionEncabezado();
        ce.setIdRequisicion(rs.getInt("idRequisicion"));
        ce.setDepto(rs.getString("depto"));
        ce.setFechaRequisicion(utilerias.Utilerias.date2String(rs.getDate("fechaRequisicion")));
        ce.setFechaAprobacion(utilerias.Utilerias.date2String(rs.getDate("fechaAprobacion")));
        ce.setNumCotizaciones(rs.getInt("numCotizaciones"));
        ce.setNumProductos(rs.getInt("numProductos"));
        ce.setEstado(rs.getInt("estado"));
        ce.setIdMoneda(rs.getInt("idMoneda"));

        return ce;

    }

    public ArrayList<CotizacionDetalle> dameProductoCotizacionesProveedores(int idReq) throws SQLException, NamingException {
        ArrayList<CotizacionDetalle> lista = new ArrayList<CotizacionDetalle>();
        ResultSet rs;
        Connection cn = ds.getConnection();
        try {

            String stringSQL = "select distinct( cd.idProducto),c.idRequisicion from cotizacionesDetalle cd\n"
                    + "       inner join cotizaciones c on c.idCotizacion = cd.idCotizacion\n"
                    + "       where c.idRequisicion=" + idReq;
            Statement sentencia = cn.createStatement();
            rs = sentencia.executeQuery(stringSQL);
            while (rs.next()) {
                lista.add(construirConsultaProducto(rs));
            }
        } finally {
            cn.close();
        }
        return lista;
    }
    
    private CotizacionDetalle construirConsultaProducto(ResultSet rs) throws SQLException, NamingException {
//        DAOEmpaques daoE = new DAOEmpaques();
//        CotizacionEncabezado ce = new CotizacionEncabezado();
//        DAOProductos daoProds = new DAOProductos();
//        TOEmpaque to = daoE.obtenerEmpaque(rs.getInt("idEmpaque"));
//        Empaque empaque = convertir(to, daoProds.obtenerProducto(to.getIdProducto()));
        CotizacionDetalle cd = new CotizacionDetalle();
        cd.setProducto(new Producto());
        cd.getProducto().setIdProducto(rs.getInt("idProducto"));
        cd.setIdCotizacion(rs.getInt("idRequisicion"));
        return cd;
    }

    public ArrayList<CotizacionDetalle> consultaCotizacionesProveedores(int idReq, int idProducto) throws SQLException, NamingException {
        ArrayList<CotizacionDetalle> lista = new ArrayList<CotizacionDetalle>();
        ResultSet rs;
        Connection cn = ds.getConnection();
        try {
            String stringSQL = "select c.idProveedor, c.descuentoCotizacion, c.descuentoProntoPago, c.idProveedor, cd.*, pro.nombreComercial, cb.contribuyente   "
                    + "from cotizacionesDetalle cd\n"
                    + " inner join cotizaciones c on c.idCotizacion = cd.idCotizacion\n"
                    + " inner join proveedores pro on c.idProveedor = pro.idProveedor\n"
                    + " inner join contribuyentes cb on cb.idContribuyente = pro.idContribuyente\n"
                    + " where c.idRequisicion=" + idReq
                    + " and cd.idProducto = " + idProducto;
            Statement sentencia = cn.createStatement();
            rs = sentencia.executeQuery(stringSQL);
            while (rs.next()) {
                lista.add(construirConsulta(rs));
            }
        } finally {
            cn.close();
        }
        return lista;
    }

    private CotizacionDetalle construirConsulta(ResultSet rs) throws SQLException, NamingException {
//        DAOEmpaques daoE = new DAOEmpaques();
//        DAOProductos daoProds = new DAOProductos();
//        TOEmpaque to = daoE.obtenerEmpaque(rs.getInt("idEmpaque"));
//        Empaque empaque = convertir(to, daoProds.obtenerProducto(to.getIdProducto()));
        
        CotizacionEncabezado ce = new CotizacionEncabezado();
        ce.setIdProveedor(rs.getInt("idProveedor"));
        ce.setDescuentoCotizacion(rs.getDouble("descuentoCotizacion"));
        ce.setDescuentoProntoPago(rs.getDouble("descuentoProntoPago"));
        
        CotizacionDetalle cd = new CotizacionDetalle();
        cd.getProveedor().setNombreComercial(rs.getString("contribuyente"));
        cd.setIdCotizacion(rs.getInt("idCotizacion"));
        cd.setProducto(new Producto());
        cd.getProducto().setIdProducto(rs.getInt("idProducto"));
//        cd.setEmpaque(empaque);
        cd.setCantidadCotizada(rs.getDouble("cantidadCotizada"));
        cd.setCostoCotizado(rs.getDouble("costoCotizado"));
        cd.setDescuentoProducto(rs.getDouble("descuentoProducto"));
        cd.setDescuentoProducto2(rs.getDouble("descuentoProducto2"));
        cd.setNeto(rs.getDouble("neto"));
        cd.setSubtotal(rs.getDouble("subtotal"));
        cd.setCotizacionEncabezado(ce);
        cd.getProveedor().setIdProveedor(rs.getInt("idProveedor"));
        return cd;
    }

//    COMENTAREO - JULIO SOLIS
//    public ArrayList<CotizacionEncabezado> consultaCotizacionesProveedoresEncabezado(int idReq) throws SQLException, NamingException {
//        ArrayList<CotizacionEncabezado> lista = new ArrayList<CotizacionEncabezado>();
//        ResultSet rs;
//        Connection cn = ds.getConnection();
//        try {
//
//            String stringSQL = "select cd.* from cotizaciones c\n"
//                    + "inner join cotizacionesDetalle cd ON cd.idCotizacion=c.idCotizacion\n"
//                    + "where c.idRequisicion=" + idReq;
//
//            Statement sentencia = cn.createStatement();
//            rs = sentencia.executeQuery(stringSQL);
//            while (rs.next()) {
//                lista.add(construirConsultaEncabezado(rs));
//            }
//        } finally {
//            cn.close();
//        }
//        return lista;
//    }
//
//    private CotizacionEncabezado construirConsultaEncabezado(ResultSet rs) throws SQLException, NamingException {
//        
//        
////        DAOEmpaques daoE = new DAOEmpaques();
////        DAOProductos daoProds = new DAOProductos();
////        TOEmpaque to = daoE.obtenerEmpaque(rs.getInt("idEmpaque"));
////        Empaque empaque = convertir(to, daoProds.obtenerProducto(to.getIdProducto()));
//        CotizacionDetalle cd = new CotizacionDetalle();
//        cd.setIdCotizacion(rs.getInt("idCotizacion"));
//        cd.setProducto(null);
//        cd.setCantidadCotizada(rs.getDouble("cantidadCotizada"));
//        cd.setCostoCotizado(rs.getDouble("costoCotizado"));
//        cd.setDescuentoProducto(rs.getDouble("descuentoProducto"));
//        cd.setNeto(rs.getDouble("neto"));
//        cd.setSubtotal(rs.getDouble("subtotal"));
//        
//        CotizacionEncabezado ce = new CotizacionEncabezado();
//        ce.setCotizacionDetalle(cd);
//        return ce;
//    }

    public void guardarOrdenCompraTotal(CotizacionEncabezado ce, ArrayList<CotizacionDetalle> ordenCompra) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        PreparedStatement ps1;
        PreparedStatement ps2;
        PreparedStatement ps3;
        int idProveedor = 0;
        int ident = 0;

        for (CotizacionDetalle c : ordenCompra) {

            int idCot = c.getIdCotizacion();
            int idMon = ce.getIdMoneda();
            int idProv = c.getCotizacionEncabezado().getIdProveedor();
            //   double cantAutorizada = c.getCantidadAutorizada();
            double dC = c.getCotizacionEncabezado().getDescuentoCotizacion();
            double dPP = c.getCotizacionEncabezado().getDescuentoProntoPago();

            this.cambiaEstadoCotizacion(idCot);
            int identity = 0;
            //CABECERO
            if (idProv != idProveedor) {
                idProveedor = idProv;

                String strSQL1 = "INSERT INTO ordenCompra(idCotizacion, fechaCreacion, fechaFinalizacion, fechaPuesta, estado, desctoComercial, desctoProntoPago,fechaEntrega,idMoneda,idProveedor) VALUES(" + idCot + ", GETDATE(), GETDATE(), GETDATE(), 1, " + dC + ", " + dPP + ", GETDATE()," + idMon + "," + idProv + ")";
                ps1 = cn.prepareStatement(strSQL1);
                ps1.executeUpdate();
                String strSQLIdentity = "SELECT @@IDENTITY as idOrd";
                ps1 = cn.prepareStatement(strSQLIdentity);
                ResultSet rs = ps1.executeQuery();
                while (rs.next()) {
                    identity = rs.getInt("idOrd");
                }
                ident = identity;

            }


            // DETALLE
            String stringSQL2 = "INSERT INTO ordenCompraDetalle (idOrdenCompra,interno, idProducto, sku, cantOrdenada, costoOrdenado, descuentoProducto, descuentoProducto2, desctoConfidencial, sinCargoBase, sinCargoCant, ptjeOferta, margen, idImpuestosGrupo, idMarca)"
                    + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement ps = cn.prepareStatement(stringSQL2);

            ps.setInt(1, ident);
            ps.setInt(2, 1);
            ps.setInt(3, c.getProducto().getIdProducto());
            ps.setString(4, "null");
            ps.setDouble(5, c.getCantidadCotizada());
            ps.setDouble(6, c.getCostoCotizado());
            ps.setDouble(7, c.getDescuentoProducto());
            ps.setDouble(8, c.getDescuentoProducto2());
            ps.setDouble(9, 0.00);
            ps.setInt(10, 0);
            ps.setInt(11, 0);
            ps.setDouble(12, 0.00);
            ps.setDouble(13, 0.00);
            ps.setInt(14, 0);
            ps.setInt(15, 0);

            try {
                ps.executeUpdate();
                String sql = "UPDATE cotizaciones set estado = '2' WHERE  idRequisicion = " + ce.getIdRequisicion();
                st.executeUpdate(sql);
            } catch (Exception e) {
                System.err.println(e);
            }


        } //FOR DETALLE


        cn.close();
    }// FOR CABECERO

    public CotizacionDetalle dameCotizacion(int idCot) throws SQLException, NamingException {
        CotizacionDetalle cd = new CotizacionDetalle();
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery("SELECT * FROM cotizacionesDetalle WHERE idCotizacion=" + idCot);
            if (rs.next()) {
                cd = construirCD(rs); //CHECAR.... SOLAMENTE TRAE UN PRODUCTO
           }
        } finally {
            cn.close();
        }
        return cd;
    }

    private CotizacionDetalle construirCD(ResultSet rs) throws NamingException, SQLException {
        CotizacionDetalle cd = new CotizacionDetalle();
        cd.setIdCotizacion(rs.getInt("idCotizacion"));
        cd.setProducto(new Producto());
        cd.getProducto().setIdProducto(rs.getInt("idProducto"));
        cd.setCantidadCotizada(rs.getDouble("cantidadCotizada"));
        cd.setCostoCotizado(rs.getDouble("costoCotizado"));
        cd.setDescuentoProducto(rs.getDouble("descuentoProducto"));
        cd.setDescuentoProducto2(rs.getDouble("descuentoProducto2"));
        cd.setNeto(rs.getDouble("neto"));
        cd.setSubtotal(rs.getDouble("subtotal"));
        return cd;
    }

    public void cambiaEstadoCotizacion(int idCot) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        PreparedStatement ps2;
        try {

            //CABECERO
            String strSQL2 = "UPDATE cotizaciones SET estado=2  WHERE idCotizacion=" + idCot;
            ps2 = cn.prepareStatement(strSQL2);
            ps2.executeUpdate();
        } catch (SQLException e) {
            throw (e);
        } finally {
            cn.close();
        }
    }
//    public CotizacionEncabezado dameCotizacionEncabezado(int idCot) throws SQLException {
//        CotizacionEncabezado ce = new CotizacionEncabezado();
//        Connection cn = ds.getConnection();
//        Statement st = cn.createStatement();
//        try {
//            //  ResultSet rs = st.executeQuery("SELECT * FROM cotizacionesEncaebzado WHERE idCotizacion=" + idCot);
//            ResultSet rs = st.executeQuery("SELECT c.*, co.idDireccion FROM cotizaciones c \n"
//                    + "inner join proveedores p on p.idProveedor =c.idProveedor\n"
//                    + "inner join contribuyentes co on co.idContribuyente =p.idContribuyente\n"
//                    + "left join direcciones d on d.idDireccion = p.idDireccionEntrega\n"
//                    + "where c.idCotizacion="+ idCot);
//            if (rs.next()) {
//                ce = construirCE(rs);
//            }
//        } finally {
//            cn.close();
//        }
//        return ce;
//        
//        
//        
//    }
//    
//    private CotizacionEncabezado construirCE(ResultSet rs) throws SQLException, NamingException {
//        
//        CotizacionEncabezado ce = new CotizacionEncabezado();
//        DAOOrdenDeCompra daoOC= new DAOOrdenDeCompra();
//        ce.setIdProveedor(rs.getInt("idProveedor"));
//        Direccion dire = daoOC.obtenerDireccion(rs.getInt("idDireccion"));
//
//        
//       
//       
//        
//        
//        
//        return ce;
//    }

//    private Empaque convertir(TOEmpaque to, Producto p) {
//        Empaque e = new Empaque();
//        e.setIdEmpaque(to.getIdEmpaque());
//        e.setCod_pro(to.getCod_pro());
//        e.setProducto(p);
//        e.setPiezas(to.getPiezas());
//        e.setUnidadEmpaque(to.getUnidadEmpaque());
//        e.setSubEmpaque(to.getSubEmpaque());
//        e.setDun14(to.getDun14());
//        e.setPeso(to.getPeso());
//        e.setVolumen(to.getVolumen());
//        return e;
//    }
}
