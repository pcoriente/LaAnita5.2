package ordenesDeCompra.dao;

import contactos.dominio.Contacto;
import cotizaciones.dao.DAOCotizaciones;
import direccion.dao.DAODirecciones;
import empresas.dao.DAOEmpresas;
import java.sql.Connection;
import java.sql.Date;
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
import monedas.DAOMonedas;
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
        ArrayList<OrdenCompraEncabezado> lista = new ArrayList<>();
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
                    + "                               where oc.estado >0 and oc.idCotizacion>0\n"
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
        ArrayList<OrdenCompraEncabezado> lista = new ArrayList<>();
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
        ArrayList<OrdenCompraEncabezado> lista = new ArrayList<>();
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
        ArrayList<OrdenCompraDetalle> lista = new ArrayList<>();
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

    public void guardarOrdenCompraDirecta(MiniProveedor mp, OrdenCompraEncabezado oced, ArrayList<OrdenCompraDetalle> ocd) throws SQLException {
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        // Date fEntrega= (Date) oced.getFechaEntregaDirectas();
        int idEncabezado = 0;
        String ordenDetalle;

        java.sql.Date fecha = new java.sql.Date(oced.getFechaEntregaDirectas().getTime());

        try {
            st.executeUpdate("BEGIN TRANSACTION");
            //CABECERO
            String ordenEncabezado = "INSERT INTO ordenCompra(idCotizacion, fechaCreacion, fechaFinalizacion, fechaPuesta, estado, desctoComercial,"
                    + " desctoProntoPago, fechaEntrega, idMoneda, idProveedor, estadoAlmacen, idEmpresa) "
                    + "VALUES(0, GETDATE(), GETDATE(), GETDATE(), 2, " + mp.getDesctoComercial() + ", "
                    + " " + mp.getDesctoProntoPago() + ", '" + fecha.toString() + "', " + oced.getMoneda().getIdMoneda() + ", " + mp.getIdProveedor() + ", 2," + oced.getEmpresa().getIdEmpresa() + ")";
            st.executeUpdate(ordenEncabezado);

            ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idEncabezado");
            if (rs.next()) {
                idEncabezado = rs.getInt("idEncabezado");
            }
            for (OrdenCompraDetalle oc : ocd) {
                ordenDetalle = "INSERT INTO ordenCompraDetalle "
                        + "(idOrdenCompra, interno, idEmpaque, sku,"
                        + " cantOrdenada, cantRecibidaOficina,cantRecibidaAlmacen,"
                        + " costoOrdenado, descuentoProducto, descuentoProducto2,"
                        + "desctoConfidencial, sinCargoBase, sinCargoCant,"
                        + " ptjeOferta, margen,"
                        + "idImpuestosGrupo, idMarca,"
                        + " cantOrdenadaSinCargo)"
                        + "VALUES( " + idEncabezado + ", 1, " + oc.getProducto().getIdProducto() + ", " + oc.getProducto().getCod_pro() + ", "
                        + " " + oc.getCantOrdenada() + ", " + oc.getCantRecibidaOficina() + ", " + oc.getCantRecibidaAlmacen() + ", "
                        + " " + oc.getCostoOrdenado() + ", " + oc.getDescuentoProducto() + ", " + oc.getDescuentoProducto2() + ", "
                        + " " + oc.getDesctoConfidencial() + " , " + oc.getSinCargoBase() + " ,  " + oc.getSinCargoCant() + ","
                        + " " + oc.getPtjeOferta() + ", " + oc.getMargen() + ", "
                        + " " + oc.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo() + ", " + oc.getProducto().getArticulo().getMarca().getIdMarca() + ","
                        + "  0)";
                st.executeUpdate(ordenDetalle);
            }
            st.executeUpdate("COMMIT TRANSACTION");
        } catch (SQLException ex) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            cn.close();
        }
    }

    public ArrayList<OrdenCompraEncabezado> listaOrdenesD() throws SQLException, NamingException {
        ArrayList<OrdenCompraEncabezado> listaD = new ArrayList<>();
        Connection cn = ds.getConnection();
        Statement sentencia = cn.createStatement();
        try {

            String stringSQL = "Select oc.idOrdenCompra, eg.nombreComercial, c.contribuyente, oc.fechaCreacion, oc.fechaEntrega,\n"
                    + "		oc.desctoComercial, oc.desctoProntoPago, oc.estado, oc.idMoneda, oc.idEmpresa, oc.idProveedor\n"
                    + "    from ordenCompra oc\n"
                    + "		inner join proveedores p on  p.idProveedor = oc.idProveedor\n"
                    + "		inner join contribuyentes c on c.idContribuyente =p.idContribuyente\n"
                    + "		inner join empresasGrupo eg on eg.idEmpresa = oc.idEmpresa\n"
                    + "	where oc.idCotizacion=0 and oc.estado <>0 \n"
                    + "	order by oc.idOrdenCompra desc";
            ResultSet rs = sentencia.executeQuery(stringSQL);
            while (rs.next()) {
                listaD.add(construirOCEncabezadoD(rs));
            }
        } finally {
            cn.close();
        }
        return listaD;
    }

    private OrdenCompraEncabezado construirOCEncabezadoD(ResultSet rs) throws SQLException, NamingException {

        OrdenCompraEncabezado oced = new OrdenCompraEncabezado();

        oced.setIdOrdenCompra(rs.getInt("idOrdenCompra"));

        DAOEmpresas daoE = new DAOEmpresas();
        oced.setEmpresa(daoE.obtenerEmpresaConverter(rs.getInt("idEmpresa")));

        oced.setNombreComercial(rs.getString("contribuyente"));

//
        DAOProveedores daoP = new DAOProveedores();
        int idProveedor = rs.getInt("idProveedor");
        if (idProveedor == 0) {
            oced.setProveedor(new Proveedor());
        } else {
            oced.setProveedor(daoP.obtenerProveedor(idProveedor));
        }

        oced.setFechaCreacion(utilerias.Utilerias.date2String(rs.getDate("fechaCreacion")));
        oced.setFechaEntrega(utilerias.Utilerias.date2String(rs.getDate("fechaEntrega")));

        oced.setDesctoComercial(rs.getDouble("desctoComercial"));
        oced.setDesctoProntoPago(rs.getDouble("desctoProntoPago"));

        DAOMonedas daoM = new DAOMonedas();
        oced.setMoneda(daoM.obtenerMoneda(rs.getInt("idMoneda")));

        int idDireccion = oced.getProveedor().getContribuyente().getDireccion().getIdDireccion(); //correcion daap
        DAODirecciones daoD = new DAODirecciones();
        if (idDireccion != 0) {
            oced.getProveedor().setDireccionFiscal(daoD.obtener(idDireccion)); // correcion daap
        }
        int idDireccionEntrega = oced.getProveedor().getDireccionEntrega().getIdDireccion();
        if (idDireccionEntrega != 0) {
            oced.getProveedor().setDireccionEntrega(daoD.obtener(idDireccionEntrega));
        }

        oced.setEstado(rs.getInt("estado"));
        switch (rs.getInt("estado")) {
            case 0:
                oced.setStatus("Rechazado");
                break;
            case 1:
                oced.setStatus("Activado");
                break;
            case 2:
                oced.setStatus("Ordenado");
                break;
            case 3:
                oced.setStatus("No Aprobado");
                break;
            case 4:
                oced.setStatus("Cerrado");
                break;
            case 5:
                oced.setStatus("Recibiendo");
                break;
            default:
                oced.setStatus("Desconocido");
        }

        return oced;
    }

    public Double ObtenerUltimoCosto(int idEmpaque) throws SQLException {
        Double ultimoCosto = 0.00;
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();

        String sql = "SELECT EE.*, D.unitario, M.fecha\n"
                + "FROM empresasEmpaques EE\n"
                + "     INNER JOIN movimientosDetalle D ON D.idMovto=EE.idMovtoUltimaEntrada AND D.idEmpaque=EE.idEmpaque\n"
                + "     INNER JOIN movimientos M ON M.idMovto=D.idMovto\n"
                + "WHERE EE.idEmpaque=" + idEmpaque;
        try {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                ultimoCosto = rs.getDouble("D.unitario");
            }
        } finally {
            st.close();
            cn.close();
        }
        return ultimoCosto;
    }
}