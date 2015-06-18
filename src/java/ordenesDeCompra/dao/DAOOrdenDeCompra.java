package ordenesDeCompra.dao;

import contactos.dominio.Contacto;
import cotizaciones.dao.DAOCotizaciones;
import direccion.dao.DAODirecciones;
import empresas.dao.DAOEmpresas;
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
import monedas.Moneda;
import ordenesDeCompra.dominio.OrdenCompraDetalle;
import ordenesDeCompra.dominio.OrdenCompraEncabezado;
import producto2.dominio.Producto;
import proveedores.dao.DAOProveedores;
import proveedores.dominio.MiniProveedor;
import proveedores.dominio.Proveedor;
import usuarios.dominio.UsuarioSesion;

public class DAOOrdenDeCompra {

    private final DataSource ds;
    private UsuarioSesion usuarioSesion;

    public DAOOrdenDeCompra() throws NamingException {
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

    public int aseguraOrdenCompra(int idOrdenCompra) throws SQLException {
        int propietario = 0;
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");

            ResultSet rs = st.executeQuery("SELECT propietario FROM ordenCompra WHERE idOrdenCompra=" + idOrdenCompra);
            if (rs.next()) {
                propietario = rs.getInt("propietario");
                if (propietario == 0) {
                    propietario = this.usuarioSesion.getUsuario().getId();
                    st.executeUpdate("UPDATE ordenCompra SET propietario=" + propietario + ", estado=5 "
                            + "WHERE idOrdenCompra=" + idOrdenCompra);
                }
            }
            st.executeUpdate("COMMIT TRANSACTION");
        } catch (SQLException e) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw (e);
        } finally {
            cn.close();
        }
        return propietario;
    }

    public ArrayList<OrdenCompraEncabezado> listaOrdenes() throws SQLException, NamingException {
        ArrayList<OrdenCompraEncabezado> lista = new ArrayList<OrdenCompraEncabezado>();
        Connection cn = ds.getConnection();
        Statement sentencia = cn.createStatement();
        try {

            String stringSQL = "select oc.idOrdenCompra, oc.fechaCreacion, oc.fechaFinalizacion, oc.fechaPuesta, oc.fechaEntrega, oc.estado, oc.idMoneda \n"
                    + "                                       , m.idMoneda, m.Moneda, m.codigoIso\n"
                    + "                                       , isnull(c.idCotizacion, 0) as idCotizacion, isnull(c.idRequisicion,0) as idRequisicion, isnull(c.desctoComercial,0.00) as desctoComercial, isnull(c.desctoProntoPago,0.00) as desctoProntoPago\n"
                    + "                                       , isnull(c.idProveedor,0) as idProveedor, isnull(c.idDireccionEntrega,0) as idDireccionEntrega\n"
                    + "                                       , isnull(c.nombreComercial,'') as nombreComercial, isnull(c.idDirEmp,0) as idDireEmpre, isnull(c.idDireccion, 0) as idDireccion\n"
                    + "                               from ordenCompra oc\n"
                    + "                               inner join webSystem.dbo.monedas m on m.idMoneda=oc.idMoneda\n"
                    + "                               left join (select c.idCotizacion, c.idRequisicion, c.descuentoCotizacion as desctoComercial, c.descuentoProntoPago as desctoProntoPago\n"
                    + "                                               , p.idProveedor, p.idDireccionEntrega, eg.nombreComercial, eg.idDireccion as idDirEmp, d.idDireccion\n"
                    + "                                           from cotizaciones c\n"
                    + "                                           inner join proveedores p on p.idProveedor = c.idProveedor\n"
                    + "                                           inner join contribuyentes co on co.idContribuyente = p.idContribuyente\n"
                    + "                                           inner join requisiciones r on r.idRequisicion = c.idRequisicion\n"
                    + "                                           inner join empresasGrupo eg on eg.idEmpresa = r.idEmpresa\n"
                    + "                                           inner join direcciones d on d.idDireccion = co.idDireccion) c on c.idCotizacion=oc.idCotizacion\n"
                    + "                               where oc.estado >0\n"
                    + "                               order by oc.idOrdenCompra desc";

            ResultSet rs = sentencia.executeQuery(stringSQL);
            while (rs.next()) {
                lista.add(construirOCEncabezado(rs));
            }
        } finally {
            cn.close();
        }
        return lista;
    }

    public ArrayList<OrdenCompraEncabezado> listaOrdenesAlmacen(int idProveedor, int status) throws SQLException, NamingException {
        ArrayList<OrdenCompraEncabezado> lista = new ArrayList<OrdenCompraEncabezado>();
        String stringSQL = "select oc.idOrdenCompra, oc.fechaCreacion, oc.fechaFinalizacion, oc.fechaPuesta, oc.fechaEntrega, oc.estadoAlmacen as estado, oc.idMoneda \n"
                + "                                       , m.idMoneda, m.Moneda, m.codigoIso\n"
                + "                                       , isnull(c.idCotizacion, 0) as idCotizacion, isnull(c.idRequisicion,0) as idRequisicion, isnull(oc.desctoComercial,0.00) as desctoComercial, isnull(oc.desctoProntoPago,0.00) as desctoProntoPago\n"
                + "                                       , isnull(c.idProveedor,0) as idProveedor, isnull(c.idDireccionEntrega,0) as idDireccionEntrega\n"
                + "                                       , isnull(c.nombreComercial,'') as nombreComercial, isnull(c.idDirEmp,0) as idDireEmpre,  isnull(c.idDireccion, 0) as idDireccion\n"
                + "                               from ordenCompra oc\n"
                + "                               inner join webSystem.dbo.monedas m on m.idMoneda=oc.idMoneda\n"
                + "                               left join (select c.idCotizacion, c.idRequisicion, c.descuentoCotizacion as desctoComercial, c.descuentoProntoPago as desctoProntoPago\n"
                + "                                               , p.idProveedor, p.idDireccionEntrega, eg.nombreComercial, eg.idDireccion as idDirEmp, d.idDireccion\n"
                + "                                           from cotizaciones c\n"
                + "                                           inner join proveedores p on p.idProveedor = c.idProveedor\n"
                + "                                           inner join contribuyentes co on co.idContribuyente = p.idContribuyente\n"
                + "                                           inner join requisiciones r on r.idRequisicion = c.idRequisicion\n"
                + "                                           inner join empresasGrupo eg on eg.idEmpresa = r.idEmpresa\n"
                + "                                           inner join direcciones d on d.idDireccion = co.idDireccion) c on c.idCotizacion=oc.idCotizacion\n"
                + "                               where oc.idProveedor=" + idProveedor + " and oc.estadoAlmacen=" + status + "\n"
                + "                               order by oc.fechaCreacion desc";
        Connection cn = ds.getConnection();
        Statement sentencia = cn.createStatement();
        try {
            ResultSet rs = sentencia.executeQuery(stringSQL);
            while (rs.next()) {
                lista.add(construirOCEncabezado(rs));
            }
        } finally {
            sentencia.close();
            cn.close();
        }
        return lista;
    }

    public ArrayList<OrdenCompraEncabezado> listaOrdenes(int idProveedor, int status) throws SQLException, NamingException {
        ArrayList<OrdenCompraEncabezado> lista = new ArrayList<OrdenCompraEncabezado>();
        String stringSQL = "select oc.idOrdenCompra, oc.fechaCreacion, oc.fechaFinalizacion, oc.fechaPuesta, oc.fechaEntrega, oc.estado, oc.idMoneda \n"
                + "                                       , m.idMoneda, m.Moneda, m.codigoIso\n"
                + "                                       , isnull(c.idCotizacion, 0) as idCotizacion, isnull(c.idRequisicion,0) as idRequisicion, isnull(oc.desctoComercial,0.00) as desctoComercial, isnull(oc.desctoProntoPago,0.00) as desctoProntoPago\n"
                + "                                       , isnull(c.idProveedor,0) as idProveedor, isnull(c.idDireccionEntrega,0) as idDireccionEntrega\n"
                + "                                       , isnull(c.nombreComercial,'') as nombreComercial, isnull(c.idDirEmp,0) as idDireEmpre,  isnull(c.idDireccion, 0) as idDireccion\n"
                + "                               from ordenCompra oc\n"
                + "                               inner join webSystem.dbo.monedas m on m.idMoneda=oc.idMoneda\n"
                + "                               left join (select c.idCotizacion, c.idRequisicion, c.descuentoCotizacion as desctoComercial, c.descuentoProntoPago as desctoProntoPago\n"
                + "                                               , p.idProveedor, p.idDireccionEntrega, eg.nombreComercial, eg.idDireccion as idDirEmp, d.idDireccion\n"
                + "                                           from cotizaciones c\n"
                + "                                           inner join proveedores p on p.idProveedor = c.idProveedor\n"
                + "                                           inner join contribuyentes co on co.idContribuyente = p.idContribuyente\n"
                + "                                           inner join requisiciones r on r.idRequisicion = c.idRequisicion\n"
                + "                                           inner join empresasGrupo eg on eg.idEmpresa = r.idEmpresa\n"
                + "                                           inner join direcciones d on d.idDireccion = co.idDireccion) c on c.idCotizacion=oc.idCotizacion\n"
                + "                               where oc.idProveedor=" + idProveedor + " and oc.estado=" + status + "\n"
                + "                               order by oc.fechaCreacion desc";
        Connection cn = ds.getConnection();
        Statement sentencia = cn.createStatement();
        try {
            ResultSet rs = sentencia.executeQuery(stringSQL);
            while (rs.next()) {
                lista.add(construirOCEncabezado(rs));
            }
        } finally {
            sentencia.close();
            cn.close();
        }
        return lista;
    }

    private OrdenCompraEncabezado construirOCEncabezado(ResultSet rs) throws SQLException, NamingException {
        OrdenCompraEncabezado oce = new OrdenCompraEncabezado();
        Moneda moneda = new Moneda();
        moneda.setIdMoneda(rs.getInt("idMoneda"));
        moneda.setMoneda(rs.getString("moneda"));
        moneda.setCodigoIso(rs.getString("codigoIso"));

        oce.setIdOrdenCompra(rs.getInt("idOrdenCompra"));
        oce.setIdCotizacion(rs.getInt("idCotizacion"));
        oce.setIdRequisicion(rs.getInt("idRequisicion"));

        DAOEmpresas daoE = new DAOEmpresas();
        oce.setEmpresa(daoE.obtenerEmpresaConverter(rs.getInt("idDireEmpre")));
        oce.setNombreComercial(rs.getString("nombreComercial"));
        oce.setDesctoComercial(rs.getDouble("desctoComercial"));
        oce.setDesctoProntoPago(rs.getDouble("desctoProntoPago"));

        DAOProveedores daoP = new DAOProveedores();
        int idProveedor = rs.getInt("idProveedor");
        if (idProveedor == 0) {
            oce.setProveedor(new Proveedor());
        } else {
            oce.setProveedor(daoP.obtenerProveedor(idProveedor));
        }
        int idDireccion = oce.getProveedor().getContribuyente().getDireccion().getIdDireccion(); //correcion daap
        DAODirecciones daoD = new DAODirecciones();
        if (idDireccion != 0) {
            oce.getProveedor().setDireccionFiscal(daoD.obtener(idDireccion)); // correcion daap
        }
        int idDireccionEntrega = oce.getProveedor().getDireccionEntrega().getIdDireccion();
        if (idDireccionEntrega != 0) {
            oce.getProveedor().setDireccionEntrega(daoD.obtener(idDireccionEntrega));
        }
        oce.setFechaCreacion(utilerias.Utilerias.date2String(rs.getDate("fechaCreacion")));
        oce.setFechaFinalizacion(utilerias.Utilerias.date2String(rs.getDate("fechaFinalizacion")));
        oce.setFechaPuesta(utilerias.Utilerias.date2String(rs.getDate("fechaPuesta")));
        oce.setFechaEntrega(utilerias.Utilerias.date2String(rs.getDate("fechaEntrega")));
        oce.setEstado(rs.getInt("estado"));
        switch (rs.getInt("estado")) {
            case 0:
                oce.setStatus("Rechazado");
                break;
            case 1:
                oce.setStatus("Activado");
                break;
            case 2:
                oce.setStatus("Ordenado");
                break;
            case 3:
                oce.setStatus("No Aprobado");
                break;
            case 4:
                oce.setStatus("Cerrado");
                break;
            case 5:
                oce.setStatus("Recibiendo");
                break;
            default:
                oce.setStatus("Desconocido");
        }
        oce.setMoneda(moneda);
        return oce;
    }

    public ArrayList<OrdenCompraDetalle> consultaOrdenCompra(int idOC) throws SQLException, NamingException {
        ArrayList<OrdenCompraDetalle> lista = new ArrayList<OrdenCompraDetalle>();
        Connection cn = ds.getConnection();
        Statement sentencia = cn.createStatement();
        try {
            String stringSQL = "select oc.idOrdenCompra, oc.idCotizacion, ocd.idEmpaque, ocd.cantOrdenada, ocd.costoOrdenado"
                    + "           , ocd.cantRecibidaOficina, ocd.cantRecibidaAlmacen, ocd.cantOrdenadaSinCargo"
                    + "           , ocd.descuentoProducto, ocd.descuentoProducto2, ocd.sku, isnull(r.idEmpresa, 0) as idEmpresa "
                    + "from ordenCompra oc "
                    + "inner join ordenCompraDetalle ocd on ocd.idOrdenCompra = oc.idOrdenCompra "
                    + "left join cotizaciones c on c.idCotizacion=oc.idCotizacion "
                    + "left join requisiciones r on r.idRequisicion=c.idRequisicion "
                    + "where oc.idOrdenCompra=" + idOC;
            ResultSet rs = sentencia.executeQuery(stringSQL);
            while (rs.next()) {
                lista.add(construirOCDetalle(rs));
            }
        } finally {
            sentencia.close();
            cn.close();
        }
        return lista;
    }

    private OrdenCompraDetalle construirOCDetalle(ResultSet rs) throws SQLException, NamingException {
        OrdenCompraDetalle ocd = new OrdenCompraDetalle();
        DAOCotizaciones daoC = new DAOCotizaciones();
        ocd.setCotizacionDetalle(daoC.dameCotizacion(rs.getInt("idCotizacion")));
        ocd.setProducto(new Producto());
        ocd.getProducto().setIdProducto(rs.getInt("idEmpaque"));
        ocd.setIdOrdenCompra(rs.getInt("idOrdenCompra"));
        ocd.setCantOrdenada(rs.getDouble("cantOrdenada"));
        ocd.setCantOrdenadaSinCargo(rs.getDouble("cantOrdenadaSinCargo"));
        ocd.setCantRecibidaOficina(rs.getDouble("cantRecibidaOficina"));
        ocd.setCantRecibidaAlmacen(rs.getDouble("cantRecibidaAlmacen"));
        ocd.setCantidadSolicitada(rs.getDouble("cantOrdenada"));
        ocd.setCostoOrdenado(rs.getDouble("costoOrdenado"));
        ocd.setDescuentoProducto(rs.getDouble("descuentoProducto"));
        ocd.setDescuentoProducto2(rs.getDouble("descuentoProducto2"));
        return ocd;
    }

    public void actualizarCantidadOrdenada(int idOrden, int idProd, double cc) throws SQLException {

        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        PreparedStatement ps2;
        try {
            //CABECERO
            String strSQL2 = "UPDATE ordenCompraDetalle SET cantOrdenada=" + cc + "  WHERE idOrdenCompra=" + idOrden + " and idEmpaque=" + idProd + "";
            ps2 = cn.prepareStatement(strSQL2);
            ps2.executeUpdate();
        } catch (SQLException e) {
            throw (e);
        } finally {
            cn.close();
        }
    }

    public void procesarOrdenCompra(int idOrden) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        PreparedStatement ps2;
        try {
            //CABECERO
            String strSQL2 = "UPDATE ordenCompra SET estado=2, estadoAlmacen=2 WHERE idOrdenCompra=" + idOrden;
            ps2 = cn.prepareStatement(strSQL2);
            ps2.executeUpdate();
        } catch (SQLException e) {
            throw (e);
        } finally {
            cn.close();
        }
    }

    public void cancelarOrdenCompra(int idOrden) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        PreparedStatement ps2;
        try {

            //CABECERO
            String strSQL2 = "UPDATE ordenCompra SET estado=0, estadoAlmacen=0 WHERE idOrdenCompra=" + idOrden;
            ps2 = cn.prepareStatement(strSQL2);
            ps2.executeUpdate();
        } catch (SQLException e) {
            throw (e);
        } finally {
            cn.close();
        }
    }

    public ArrayList<Contacto> obtenerContactos(int idOC) throws SQLException {
        ArrayList<Contacto> lista;
        lista = new ArrayList<>();
        ResultSet rs;
        try (Connection cn = ds.getConnection()) {

            String stringSQL = "select con.correo from ordenCompra oc\n"
                    + "inner join cotizaciones c on c.idCotizacion=oc.idCotizacion\n"
                    + "inner join contactos con on con.idPadre=c.idProveedor\n"
                    + "where oc.idOrdenCompra=" + idOC;

            Statement sentencia = cn.createStatement();
            rs = sentencia.executeQuery(stringSQL);
            while (rs.next()) {
                lista.add(construirContactos(rs));
            }
        }
        return lista;

    }

    private Contacto construirContactos(ResultSet rs) throws SQLException {
        Contacto cont = new Contacto();
        cont.setCorreo(rs.getString("correo"));
        return cont;
    }

    public int obtenerIdUsuario() {
        return this.usuarioSesion.getUsuario().getId();
    }

//    public void guardarOrdenCompraDirecta(OrdenCompraEncabezado oce, ArrayList<OrdenCompraDetalle> ordenCompraDetalle) throws SQLException {
//        Connection cn = this.ds.getConnection();
//        Statement st = cn.createStatement();
//        Statement cs1 = cn.createStatement();
//        Statement cs2 = cn.createStatement();
//        PreparedStatement ps3;
//        int idProveedor = 0;
//        int ident = 0;
//
//        try {
//            st.executeUpdate("BEGIN TRANSACTION");
//            //CABECERO
//            for (OrdenCompraDetalle c : ordenCompraDetalle) {
//                int idCot = c.getIdCotizacion();
//                int idMon = ce.getIdMoneda();
//                int idProv = c.getCotizacionEncabezado().getIdProveedor();
//                //   double cantAutorizada = c.getCantidadAutorizada();
//                double dC = c.getCotizacionEncabezado().getDescuentoCotizacion();
//                double dPP = c.getCotizacionEncabezado().getDescuentoProntoPago();
//                //     this.cambiaEstadoCotizacion(idCot);
//                int identity = 0;
//                if (idProv != idProveedor) {
//                    idProveedor = idProv;
//                    String strSQL1 = "INSERT INTO ordenCompra(idCotizacion, fechaCreacion, fechaFinalizacion, fechaPuesta, estado, desctoComercial, desctoProntoPago,fechaEntrega,idMoneda,idProveedor,estadoAlmacen) VALUES(" + idCot + ", GETDATE(), GETDATE(), GETDATE(), 1, " + dC + ", " + dPP + ", GETDATE()," + idMon + "," + idProv + ",1)";
//                    //  cs1 = cn.prepareStatement();
//                    cs1.executeUpdate(strSQL1);
//                    String strSQLIdentity = "SELECT @@IDENTITY as idOrd";
//                    //   cs2 = cn.prepareStatement(strSQLIdentity);
//                    ResultSet rs = cs2.executeQuery(strSQLIdentity);
//                    while (rs.next()) {
//                        identity = rs.getInt("idOrd");
//                    }
//                    ident = identity;
//                }
//                // DETALLE
//                String stringSQL2 = "INSERT INTO ordenCompraDetalle "
//                        + "(idOrdenCompra, interno, idEmpaque, sku, cantOrdenada, cantRecibidaOficina,"
//                        + "cantRecibidaAlmacen, costoOrdenado, descuentoProducto, descuentoProducto2,"
//                        + "desctoConfidencial, sinCargoBase, sinCargoCant, ptjeOferta, margen,"
//                        + "idImpuestosGrupo, idMarca, cantOrdenadaSinCargo)"
//                        + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
//                ps3 = cn.prepareStatement(stringSQL2);
//
//                ps3.setInt(1, ident);
//                ps3.setInt(2, 1); //interno
//                ps3.setInt(3, c.getProducto().getIdProducto()); //idEmpaque
//                ps3.setString(4, "null"); //sku
//                ps3.setDouble(5, c.getCantidadCotizada()); //cantOrdenada
//                ps3.setDouble(6, 0.00); //cantRecibidaOficina
//                ps3.setDouble(7, 0.00); //cantRecibidaAlmacen
//                ps3.setDouble(8, c.getCostoCotizado()); //costoOrdenado
//                ps3.setDouble(9, c.getDescuentoProducto()); //descuentoProducto
//                ps3.setDouble(10, c.getDescuentoProducto2()); //descuentoProducto2
//                ps3.setDouble(11, 0.00); //desctoConfidencial
//                ps3.setInt(12, 0); //sinCargoBase
//                ps3.setInt(13, 0); //sinCargoCant
//                ps3.setDouble(14, 0.00); //ptjeOferta
//                ps3.setDouble(15, 0.00); //margen
//                ps3.setInt(16, 0); //idImpuestosGrupo
//                ps3.setInt(17, 0); //idMarca
//                ps3.setDouble(18, 0.00); // cantOrdenadaSinCargo
//                ps3.executeUpdate();
//
//            } //FOR DETALLE
//            //    try {
//            String sql = "UPDATE cotizaciones set estado = 2 WHERE  idRequisicion = " + ce.getIdRequisicion();
//            st.executeUpdate(sql);
//            //    } catch (Exception e) {
//            //     System.err.println(e + "Entro en la excepcion");
//            //  }
//            st.executeUpdate("COMMIT TRANSACTION");
//        } catch (SQLException e) {
//            st.executeUpdate("ROLLBACK TRANSACTION");
//            System.err.println(e);
//            throw (e);
//        } finally {
//            cn.close();
//        }
//        //  cn.close();
//    }// FOR 
    public void guardarOrdenCompraDirecta(MiniProveedor miniProveedor, OrdenCompraEncabezado ordenCompraEncabezadoDirecta, ArrayList<OrdenCompraDetalle> ordenCompraDetallesDirectas) throws SQLException {
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        int idEncabezado = 0;
        String encabezadoOrden = "INSERT INTO ordenCompra(idCotizacion, fechaCreacion, fechaFinalizacion, fechaPuesta, estado, desctoComercial, desctoProntoPago,fechaEntrega,idMoneda,idProveedor,estadoAlmacen) "
                + "VALUES(0, GETDATE(), GETDATE(), GETDATE(), 1, 2, " + miniProveedor.getDesctoComercial() + ",    " + miniProveedor.getDesctoProntoPago() + ", GETDATE(), " + ordenCompraEncabezadoDirecta.getMoneda().getIdMoneda() + "," + miniProveedor.getIdProveedor() + ",2)";
        PreparedStatement ps = null;
        try {
            ps = cn.prepareStatement(encabezadoOrden);
            ps.executeUpdate();
            ResultSet rs = null;
            rs = ps.executeQuery("SELECT @@IDENTITY AS idEncabezado");
            while (rs.next()) {
                idEncabezado = rs.getInt("idEncabezado");
            }
            st.executeUpdate("begin transaction");
            for (OrdenCompraDetalle oc : ordenCompraDetallesDirectas) {
                String stringSQL2 = "INSERT INTO ordenCompraDetalle "
                        + "(idOrdenCompra, interno, idEmpaque, sku, cantOrdenada, cantRecibidaOficina,"
                        + "cantRecibidaAlmacen, costoOrdenado, descuentoProducto, descuentoProducto2,"
                        + "desctoConfidencial, sinCargoBase, sinCargoCant, ptjeOferta, margen,"
                        + "idImpuestosGrupo, idMarca, cantOrdenadaSinCargo)"
                        + "VALUES( "+idEncabezado+", '1', "+oc.getIdEmpaque()+", "+oc.getProducto().getCod_pro()+" "
                        + " "+oc.getCantOrdenada()+", "+oc.getCantRecibidaOficina()+", "+oc.getCantRecibidaAlmacen()+" "
                        + " "+oc.getCostoOrdenado()+", "+oc.getDescuentoProducto()+", "+oc.getDescuentoProducto2()+" "
                        + " "+oc.getDesctoConfidencial()+" , "+oc.getSinCargoBase()+" , "+oc.getPtjeOferta()+", "+oc.getMargen()+" "
                        + " "+oc.getProducto().getArticulo().getImpuestoGrupo()+", "+oc.getProducto().getArticulo().getMarca().getIdMarca()+" , "
                        + "0)";
                ps = cn.prepareStatement(stringSQL2);
//                oc.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo();
                //SELECT TABLA impuestosDetalles
               //utilizar el metodo q tiene en daoMovimientos
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            st.executeUpdate("rollback transaction");
            throw ex;
        } finally {
            cn.close();
        }

        st.executeUpdate("commit transaction");



    }
}
