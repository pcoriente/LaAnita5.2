package requisiciones.dao;

import cotizaciones.dominio.CotizacionDetalle;
import cotizaciones.dominio.CotizacionEncabezado;
import cotizaciones.to.TOCotizacionDetalle;
import empresas.dao.DAOMiniEmpresas;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import requisiciones.dominio.RequisicionDetalle;
import requisiciones.dominio.RequisicionEncabezado;
import requisiciones.to.TORequisicionDetalle;
import requisiciones.to.TORequisicionEncabezado;
import usuarios.dominio.UsuarioSesion;

public class DAORequisiciones {

    private final DataSource ds;
    private UsuarioSesion usuarioSesion;

    public DAORequisiciones() throws NamingException {
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

    public void guardarRequisicion(int idEmpresa, int idDepto, int idSolicito, ArrayList<RequisicionDetalle> pr) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        Statement ps1;
        PreparedStatement ps2;

        try {
            st.executeUpdate("BEGIN TRANSACTION");
            //CABECERO
            String strSQL1 = "INSERT INTO requisiciones(idEmpresa, idDepto, idSolicito, fechaRequisicion) VALUES (" + idEmpresa + ", " + idDepto + ", " + idSolicito + ",GETDATE())";
            String strSQLIdentity = "SELECT @@IDENTITY AS idReq"; //CAMBIO IDENTITY

            ps1 = cn.createStatement();
            ps1.executeUpdate(strSQL1);
//            ps1 = cn.CStatement(strSQLIdentity);
            ResultSet rs = ps1.executeQuery(strSQLIdentity);
            int identity = 0;
            if (rs.next()) {
                identity = rs.getInt("idReq");
            }
            // DETALLE

            // 17/feb/2014-- cambiar tabla en requisiciones, quitar idProducto por idEmpaque
            String strSQL2 = "INSERT INTO requisicionDetalle(idRequisicion,idEmpaque, cantidadSolicitada, cantidadAutorizada) VALUES (?,?,?,?)";
            ps2 = cn.prepareStatement(strSQL2);

            for (RequisicionDetalle e : pr) {
                ps2.setInt(1, identity);
                ps2.setInt(2, e.getProducto().getIdProducto()); //cambio a Empaque
                ps2.setDouble(3, e.getCantidad());
                ps2.setDouble(4, e.getCantidad());
                ps2.executeUpdate();
            }
            st.executeUpdate("COMMIT TRANSACTION");
        } catch (SQLException e) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw (e);
        } finally {
            cn.close();
        }
    }

    public ArrayList<RequisicionEncabezado> dameRequisicion() throws SQLException, NamingException {
        ArrayList<RequisicionEncabezado> lista = new ArrayList<RequisicionEncabezado>();
        ResultSet rs;
        Connection cn = ds.getConnection();
        try {
            String stringSQL = "select r.idRequisicion, r.idEmpresa, r.idDepto, e.idEmpleado, r.idAprobo, r.fechaRequisicion, r.fechaAprobacion, r.estado from requisiciones r\n"
                    + "                    inner join empleados e on e.idEmpleado= r.idSolicito\n"
                    + "                    where r.estado between 0 and 2\n"
                    + "                    order by  idRequisicion desc";
            Statement sentencia = cn.createStatement();
            rs = sentencia.executeQuery(stringSQL);
            while (rs.next()) {
                lista.add(construirCabecero2(rs));
            }
        } finally {
            cn.close();
        }
        return lista;
    }

    private RequisicionEncabezado construirCabecero2(ResultSet rs) throws SQLException, NamingException {
        DAOUsuarioRequisiciones daoU = new DAOUsuarioRequisiciones();
        DAODepto daoD = new DAODepto();
        DAOMiniEmpresas daoM = new DAOMiniEmpresas();
        RequisicionEncabezado re = new RequisicionEncabezado();
        re.setIdRequisicion((rs.getInt("idRequisicion")));
        re.setMiniEmpresa(daoM.obtenerMiniEmpresa(rs.getInt("idEmpresa")));
        re.setDepto(daoD.obtenerDeptoConverter(rs.getInt("idDepto")));
        re.setUsuario(daoU.obtenerUsuarioConverter(rs.getInt("idEmpleado")));
        re.setFechaRequisicion(utilerias.Utilerias.date2String(rs.getDate("fechaRequisicion")));
        re.setEmpleadoAprobo(usuarioSesion.getUsuario().getUsuario());
        re.setFechaAprobacion(utilerias.Utilerias.date2String(rs.getDate("fechaAprobacion")));
        re.setStatus(rs.getInt("estado"));
        int state = rs.getInt("estado");
        switch (state) {
            case 0:
                re.setEstado("Rechazado");
                break;
            case 1:
                re.setEstado("Solicitado");
                break;
            case 2:
                re.setEstado("Aprobado");
                break;
            default:
                String noAprobado = "No Aprobado";
        }
        return re;
    }

    public ArrayList<TORequisicionDetalle> dameRequisicionDetalle(int idReq) throws SQLException {
        ArrayList<TORequisicionDetalle> lista = new ArrayList<TORequisicionDetalle>();

        Connection cn = ds.getConnection();
        try {

            String stringSQL = "select rd.idRequisicion, rd.idEmpaque, rd.cantidadSolicitada, rd.cantidadAutorizada "
                    + "from requisicionDetalle rd\n"
                    + "where idRequisicion=" + idReq;

            Statement sentencia = cn.createStatement();
            ResultSet rs = sentencia.executeQuery(stringSQL);
            while (rs.next()) {
                try {
                    lista.add(construirDetalle(rs));
                } catch (NamingException ex) {
                    Logger.getLogger(DAORequisiciones.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } finally {
            cn.close();
        }
        return lista;
    }

    private TORequisicionDetalle construirDetalle(ResultSet rs) throws SQLException, NamingException {
//        RequisicionDetalle to = new RequisicionDetalle();
//        DAOEmpaques daoEmp = new DAOEmpaques();
//        DAOProductos daoProds = new DAOProductos();
//        TOEmpaque toE = daoEmp.obtenerEmpaque(rs.getInt("idEmpaque"));
//        Empaque empaque = this.convertir(toE, daoProds.obtenerProducto(toE.getIdProducto()));

        TORequisicionDetalle to = new TORequisicionDetalle();
        to.setIdRequisicion(rs.getInt("idRequisicion"));
        //   to.setEmpaque(daoEmp.obtenerEmpaque(rs.getInt("idEmpaque")));
//        to.setEmpaque(empaque);
        to.setIdProducto(rs.getInt("idEmpaque"));
        to.setCantidad(rs.getDouble("cantidadSolicitada"));
        to.setCantidadAutorizada(rs.getDouble("cantidadAutorizada"));
        return to;
    }

    public TORequisicionEncabezado dameRequisicionEncabezado(int idRequisi) throws SQLException {
        TORequisicionEncabezado toRE = new TORequisicionEncabezado();
        ResultSet rs;
        Connection cn = ds.getConnection();
        try {
            String stringSQL = "select r.idRequisicion, r.idEmpresa, r.IdDepto, e.idEmpleado, r.idAprobo, r.fechaRequisicion, r.fechaAprobacion, r.estado from requisiciones r\n"
                    + "inner join empleados e on r.idSolicito=e.idEmpleado\n"
                    + "where idRequisicion=" + idRequisi;
            Statement sentencia = cn.createStatement();
            rs = sentencia.executeQuery(stringSQL);
            if (rs.next()) {
                toRE = construirCabecero1(rs);
            }
        } finally {
            cn.close();
            return toRE;
        }
    }

    private TORequisicionEncabezado construirCabecero1(ResultSet rs) throws SQLException {
        TORequisicionEncabezado to = new TORequisicionEncabezado();
        to.setIdRequisicion(rs.getInt("idRequisicion"));
        to.setIdEmpresa(rs.getInt("idEmpresa"));
        to.setIdDepto(rs.getInt("idDepto"));
        to.setIdSolicito(rs.getInt("idEmpleado"));
        to.setIdAprobo(rs.getInt("idAprobo"));
        to.setFechaRequisicion(rs.getDate("fechaRequisicion"));
        to.setFechaAprobacion(rs.getDate("fechaAprobacion"));
        to.setEmpleadoAprobo(usuarioSesion.getUsuario().getUsuario());
        to.setStatus(rs.getInt("estado"));
        return to;
    }

    public void actualizaRequisicion(int idReq, int estado) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        PreparedStatement ps4;
        try {
            //  st.executeUpdate("begin transaction");

            int us = usuarioSesion.getUsuario().getId();
            if (us == 0) {
                System.out.println("El usuario no existe...");
            } else {
                String strSQL4 = "UPDATE requisiciones SET  fechaAprobacion=GETDATE(),idAprobo='" + us + "', estado='" + estado + "' WHERE idRequisicion=" + idReq;
                ps4 = cn.prepareStatement(strSQL4);
                ps4.executeUpdate();
            }


        } catch (SQLException e) {
            //   st.executeUpdate("rollback transaction");
            throw (e);
        } finally {
            cn.close();
        }
    }

    public void eliminaProductoAprobar(int idReq, int idEmp) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        PreparedStatement ps1;
        PreparedStatement ps2;
        try {
            st.executeUpdate("begin transaction");
            //CABECERO
            String strSQL2 = "UPDATE requisicionDetalle SET cantidadAutorizada= 0 WHERE idRequisicion=" + idReq + " and idEmpaque=" + idEmp + "";
            ps2 = cn.prepareStatement(strSQL2);
            ps2.executeUpdate();
            st.executeUpdate("commit transaction");
        } catch (SQLException e) {
            st.executeUpdate("rollback transaction");
            throw (e);
        } finally {
            cn.close();
        }
    }

    public ArrayList<TORequisicionDetalle> dameRequisicionDetalleAprobar(int idRequisi) throws SQLException, NamingException {
        ArrayList<TORequisicionDetalle> lista = new ArrayList<TORequisicionDetalle>();
        ResultSet rs;
        Connection cn = ds.getConnection();
        try {

            String stringSQL = "select rd.idRequisicion,rd.idEmpaque,rd.cantidadSolicitada, rd.cantidadAutorizada "
                    + "from requisicionDetalle rd\n"
                    + "                    where cantidadAutorizada <> 0 and idRequisicion=" + idRequisi;

            Statement sentencia = cn.createStatement();
            rs = sentencia.executeQuery(stringSQL);
            while (rs.next()) {
                lista.add(construirDetalle(rs));
            }
        } finally {
            cn.close();
        }
        return lista;
    }

    public void modificaProductoAprobar(int idReq, int idEmp, double cantidad) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        PreparedStatement ps2;
        try {
            st.executeUpdate("begin transaction");
            //CABECERO
            String strSQL2 = "UPDATE requisicionDetalle SET cantidadAutorizada=" + cantidad + "  WHERE idRequisicion=" + idReq + " and idEmpaque=" + idEmp + "";
            ps2 = cn.prepareStatement(strSQL2);
            ps2.executeUpdate();
            st.executeUpdate("commit transaction");
        } catch (SQLException e) {
            st.executeUpdate("rollback transaction");
            throw (e);
        } finally {
            cn.close();
        }
    }

    public void modificarAprobacion(int idReq, int idEmp) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        PreparedStatement ps2, ps3;
        int cant = 2;
        try {
            if (cant != 0) {
                st.executeUpdate("begin transaction");
                String strSQL2 = "UPDATE requisiciones SET idAprobo=0,estado=1,fechaAprobacion=1900-01-01 WHERE idRequisicion=" + idReq;
                ps2 = cn.prepareStatement(strSQL2);
                ps2.executeUpdate();
                st.executeUpdate("commit transaction");
            } else {
                st.executeUpdate("begin transaction");
                String strSQL3 = "UPDATE requisicionDetalle SET cantidadAutorizada==cantidadSolicitada  WHERE idRequisicion=" + idReq + " and idEmpaque=" + idEmp;
                ps3 = cn.prepareStatement(strSQL3);
                ps3.executeUpdate();
                st.executeUpdate("commit transaction");
            }
        } catch (SQLException e) {
            st.executeUpdate("rollback transaction");
            throw (e);
        } finally {
            cn.close();
        }
    }

    //COTIZACIONES
    public void grabarCotizacion(CotizacionEncabezado ce, ArrayList<CotizacionDetalle> cd) throws SQLException {
        int idProv = ce.getIdProveedor();

        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        PreparedStatement ps1, ps2, ps3, ps4;

        try {
            st.executeUpdate("begin transaction");
            //CABECERO
            String strSQL1 = "INSERT INTO cotizaciones(idRequisicion, idProveedor, idMoneda, folioProveedor, fechaCotizacion, descuentoCotizacion,descuentoProntoPago, observaciones, estado, numCotizaciones )"
                    + " VALUES (" + ce.getIdRequisicion() + ", " + idProv + ", " + ce.getIdMoneda() + ",'Folio' ,GETDATE(), " + ce.getDescuentoCotizacion() + ", " + ce.getDescuentoProntoPago() + ", 'hola', " + 1 + ", " + 1 + ")";
            String strSQLIdentity = "SELECT @@IDENTITY AS idCot";
            ps1 = cn.prepareStatement(strSQL1);
            ps1.executeUpdate();
            ps3 = cn.prepareStatement(strSQLIdentity);
            ResultSet rs = ps3.executeQuery();
            int identity = 0;
            if (rs.next()) {
                identity = rs.getInt("idCot");

            }
            // DETALLE
            String strSQL2 = "INSERT INTO cotizacionesDetalle(idCotizacion,idEmpaque, cantidadCotizada, costoCotizado, descuentoProducto, descuentoProducto2,neto,subtotal) VALUES (?,?,?,?,?,?,?,?)";
            ps2 = cn.prepareStatement(strSQL2);

            for (CotizacionDetalle e : cd) {
                ps2.setInt(1, identity);
                ps2.setInt(2, e.getProducto().getIdProducto());
                ps2.setDouble(3, e.getCantidadCotizada());
                ps2.setDouble(4, e.getCostoCotizado());
                ps2.setDouble(5, e.getDescuentoProducto());
                ps2.setDouble(6, e.getDescuentoProducto2());
                ps2.setDouble(7, e.getNeto());
                ps2.setDouble(8, e.getSubtotal());
                ps2.executeUpdate();
            }


//            String strSQL3 = "UPDATE requisiciones SET estado=3  WHERE idRequisicion=" + ce.getIdRequisicion();
//            ps4 = cn.prepareStatement(strSQL3);
//            ps4.executeUpdate();

            st.executeUpdate("commit transaction");
        } catch (SQLException e) {
            st.executeUpdate("rollback transaction");
            throw (e);
        } finally {
            cn.close();
        }


    }

    public ArrayList<TOCotizacionDetalle> dameRequisicionDetalleCotizar(int idRequisi) throws SQLException, NamingException {
        ArrayList<TOCotizacionDetalle> lista = new ArrayList<TOCotizacionDetalle>();
        ResultSet rs;
        Connection cn = ds.getConnection();
        //   this.grabarCotizacionInicial(idRequisi);
        try {

            String stringSQL = "select rd.idRequisicion, rd.idEmpaque, rd.cantidadSolicitada, rd.cantidadAutorizada "
                    + "from requisicionDetalle rd "
                    + "where cantidadAutorizada > 0 and idRequisicion=" + idRequisi;
            Statement sentencia = cn.createStatement();
            rs = sentencia.executeQuery(stringSQL);
            while (rs.next()) {
                lista.add(construirCotizacionDetalle(rs));
            }
        } finally {
            cn.close();
        }
        return lista;
    }

    public TOCotizacionDetalle construirCotizacionDetalle(ResultSet rs) throws NamingException, SQLException {
//        CotizacionDetalle cd = new CotizacionDetalle();
//        RequisicionDetalle rd = new RequisicionDetalle();
//        DAOEmpaques daoEmp = new DAOEmpaques();
//        //REQUISICION
//        DAOProductos daoProds = new DAOProductos();
//        TOEmpaque to = daoEmp.obtenerEmpaque(rs.getInt("idEmpaque"));
//        Empaque empaque = this.convertir(to, daoProds.obtenerProducto(to.getIdProducto()));
//
//        
//        cd.setIdRequisicion(rs.getInt("idRequisicion"));
//        cd.setEmpaque(empaque);
////        rd.setCantidad(rs.getInt("cantidadSolicitada"));
////        rd.setCantidadAutorizada(rs.getInt("cantidadAutorizada"));
//        //COTIZACION
//        // cd.setRequisicionDetalle(rd);
//        cd.setCantidadAutorizada(rs.getInt("cantidadAutorizada"));
//        cd.setCantidadCotizada(rs.getDouble("cantidadAutorizada"));
//        cd.setCostoCotizado(0);
//        cd.setNeto(0);
//        cd.setSubtotal(0);
//        cd.setDescuentoProducto(0);
//        cd.setDescuentoProducto2(0);

        TOCotizacionDetalle to = new TOCotizacionDetalle();
        to.setIdRequisicion(rs.getInt("idRequisicion"));
        to.setIdProducto(rs.getInt("idEmpaque"));
        to.setCantidadAutorizada(rs.getDouble("cantidadAutorizada"));
        return to;
    }

    public void actualizarCantidadCotizada(int idCot, int idEmp, int cc) throws SQLException {

        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        PreparedStatement ps2;
        try {

            //CABECERO
            String strSQL2 = "UPDATE cotizacionesDetalle SET cantidadCotizada=" + cc + "  WHERE idCotizacion=" + idCot + " and idEmpaque=" + idEmp + "";
            ps2 = cn.prepareStatement(strSQL2);
            ps2.executeUpdate();
        } catch (SQLException e) {
            throw (e);
        } finally {
            cn.close();
        }

    }

    public void actualizarPrecioDescuento(int idCot, int idEmp, int costo, int desc) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        PreparedStatement ps2;
        try {

            //CABECERO
            String strSQL2 = "UPDATE cotizacionesDetalle SET costoCotizado=" + costo + ", descuentoProducto=" + desc + "  WHERE idCotizacion=" + idCot + " and idEmpaque=" + idEmp + "";
            ps2 = cn.prepareStatement(strSQL2);
            ps2.executeUpdate();
        } catch (SQLException e) {
            throw (e);
        } finally {
            cn.close();
        }

    }

    public void cerrarCotizacion(int idReq) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        PreparedStatement ps4;
        try {
            String strSQL3 = "UPDATE requisiciones SET estado=3  WHERE idRequisicion=" + idReq;
            ps4 = cn.prepareStatement(strSQL3);
            ps4.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DAORequisiciones.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            cn.close();
        }
    }

    public int numCotizaciones(int idReq) throws SQLException {
        // CotizacionEncabezado ce = new CotizacionEncabezado();
        int coti = 0;
        ResultSet rs;
        Connection cn = ds.getConnection();
        try {
            String stringSQL = "select  count(numCotizaciones) as numCotiza\n"
                    + " from cotizaciones where idRequisicion=" + idReq;
            Statement sentencia = cn.createStatement();
            rs = sentencia.executeQuery(stringSQL);
            while (rs.next()) {
                coti = rs.getInt("numCotiza");
            }
            // ce = cotizaciones(rs);
        } finally {
            cn.close();
        }
        return coti;

    }
//
//    private CotizacionEncabezado cotizaciones(ResultSet rs) throws SQLException {
//        CotizacionEncabezado ce = new CotizacionEncabezado();
//
//        ce.setNumCotizaciones(rs.getInt("numCotiza"));
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
