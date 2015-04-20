package entradas.dao;

import entradas.dominio.MovimientoProducto;
import impuestos.dominio.ImpuestosProducto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Format;
import java.text.SimpleDateFormat;
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

/**
 *
 * @author jsolis
 */
public class DAOEntradas {
    int idUsuario;
    private DataSource ds = null;

    public DAOEntradas() throws NamingException {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
            this.idUsuario=usuarioSesion.getUsuario().getId();

            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
        } catch (NamingException ex) {
            throw (ex);
        }
    }
    
    public int obtenerLote(java.util.Date fecha) {
        int i=1;
        return i;
    }
    
    public boolean grabarEntradaAlmacen(int idAlmacen, int idMovto, double tipoCambio, int idFactura, ArrayList<MovimientoProducto> productos) throws SQLException {
        int capturados;
        boolean ok=false;
        int idLote;
        java.util.Date fechaCaducidad;
        fechaCaducidad = new java.util.Date();
        Format formatter=new SimpleDateFormat("yyyy-MM-dd");
        
        Connection cn=this.ds.getConnection();
        String strSQL1="UPDATE movimientosDetalle " +
                    "SET cantRecibida=? " +
                    "WHERE idMovto="+idMovto+" AND idEmpaque=?";
        PreparedStatement ps1=cn.prepareStatement(strSQL1);
        
        String strSQL3="INSERT INTO kardex (idAlmacen, idMovto, idTipoMovto, idEmpaque, idLote, fecha, existenciaAnterior, cantidad) " +
                    "VALUES ("+idAlmacen+", "+idMovto+", 2, ?, ?, GETDATE(), ?, ?)";
        PreparedStatement ps3=cn.prepareStatement(strSQL3);
        
        String strSQL4="INSERT INTO almacenesEmpaques (idAlmacen, idEmpaque, existencia, existenciaOficina, promedioPonderado, existenciaMinima, existenciaMaxima, idMovtoEntrada) " +
                    "VALUES ("+idAlmacen+", ?, ?, 0, 0, 0, 0, 0)";
        PreparedStatement ps4=cn.prepareStatement(strSQL4);
        
        String strSQL5="UPDATE almacenesEmpaques " +
                    "SET existencia=existencia+? " +
                    "WHERE idAlmacen="+idAlmacen+" AND idEmpaque=?";
        PreparedStatement ps5=cn.prepareStatement(strSQL5);
        
        ResultSet rs;
        int idEmpaque;
        double existenciaAnterior;
        Statement st=cn.createStatement();
        try {
            capturados=0;
            st.executeUpdate("BEGIN TRANSACTION");
            
            //rs = st.executeQuery("SELECT idLote FROM lotes WHERE fecha='" + formatter.format(fechaCaducidad) + "'");
            rs = st.executeQuery("SELECT idLote FROM lotes WHERE fecha='2014-02-12'");
            if(rs.next()) {
                idLote = rs.getInt("idLote");
            } else {
                idLote = 0;
            }
            
            for(MovimientoProducto p: productos) {
                idEmpaque=p.getProducto().getIdProducto();
//                fechaCaducidad=utilerias.Utilerias.addDays(fechaCaducidad, p.getEmpaque().getProducto().getDiasCaducidad);
//                fechaCaducidad=utilerias.Utilerias.addDays(fechaCaducidad, 365);
                
                if(p.getCantFacturada()> 0) {
                    capturados++;
                    
                    ps1.setDouble(1, p.getCantRecibida());
                    ps1.setInt(2, idEmpaque);
                    ps1.executeUpdate();
                    
                    rs=st.executeQuery("SELECT existencia " +
                                        "FROM almacenesEmpaques " +
                                        "WHERE idAlmacen="+idAlmacen+" AND idEmpaque="+idEmpaque);
                    if(rs.next()) {
                        existenciaAnterior=rs.getDouble("existencia");
                        
                        ps5.setDouble(1, p.getCantRecibida());
                        ps5.setInt(2, idEmpaque);
                        ps5.executeUpdate();
                    } else {
                        existenciaAnterior=0;
                        
                        ps4.setInt(1, idEmpaque);
                        ps4.setDouble(2, p.getCantRecibida());
                        ps4.executeUpdate();
                    }
                    ps3.setInt(1, idEmpaque);
                    ps3.setInt(2, idLote);
                    ps3.setDouble(3, existenciaAnterior);
                    ps3.setDouble(4, p.getCantRecibida());
                    ps3.executeUpdate();
                }
            }
            if(capturados>0) {
                st.executeUpdate("UPDATE facturas SET cerradaAlmacen=1 WHERE idFactura="+idFactura);
            }
            st.executeUpdate("COMMIT TRANSACTION");
            ok=true;
        } catch(SQLException e) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw(e);
        } finally {
            cn.close();
        }
        return ok;
    }
    
    public boolean grabarEntradaOficina(int idAlmacen, int idMovto, double tipoCambio, int idFactura, ArrayList<MovimientoProducto> productos) throws SQLException {
        int capturados;
        boolean ok=false;
        ArrayList<ImpuestosProducto> impuestos;
        
        Connection cn=this.ds.getConnection();
        String strSQL1="UPDATE movimientosDetalle " +
                    "SET costo=?, desctoProducto1=?, desctoProducto2=?, desctoConfidencial=?, unitario=?, cantFacturada=?, cantSinCargo=? " +
                    "WHERE idMovto="+idMovto+" AND idEmpaque=?";
        PreparedStatement ps1=cn.prepareStatement(strSQL1);
        
        String strSQL2="UPDATE movimientosDetalleImpuestos " +
                    "SET importe=? " +
                    "WHERE idMovto="+idMovto+" AND idEmpaque=?";
        PreparedStatement ps2=cn.prepareStatement(strSQL2);
        
        String strSQL3="INSERT INTO kardex (idAlmacen, idMovto, idTipoMovto, idEmpaque, idLote, fecha, existenciaAnterior, cantidad) " +
                    "VALUES ("+idAlmacen+", "+idMovto+", 1, ?, 0, GETDATE(), ?, ?)";
        PreparedStatement ps3=cn.prepareStatement(strSQL3);
        
        String strSQL4="INSERT INTO almacenesEmpaques (idAlmacen, idEmpaque, existencia, existenciaOficina, promedioPonderado, existenciaMinima, existenciaMaxima, idMovtoEntrada) " +
                    "VALUES (?, ?, 0, ?, ?, 0, 0, ?)";
        PreparedStatement ps4=cn.prepareStatement(strSQL4);
        
        String strSQL5="UPDATE almacenesEmpaques " +
                    "SET existenciaOficina=existenciaOficina+?, " +
                        "promedioPonderado=(existenciaOficina*promedioPonderado+?*?)/(existenciaOficina+?+?) " +
                    "WHERE idAlmacen="+idAlmacen+" AND idEmpaque=?";
        PreparedStatement ps5=cn.prepareStatement(strSQL5);
        
        ResultSet rs;
        int idEmpaque;
        double existenciaAnterior;
        Statement st=cn.createStatement();
        try {
            capturados=0;
            st.executeUpdate("BEGIN TRANSACTION");
            
            st.executeUpdate("UPDATE movimientos SET tipoCambio="+tipoCambio+" WHERE idMovto="+idMovto);
            
            //rs=st.executeQuery("select DATEPART(weekday, getdate()-1) AS DIA, DATEPART(week, GETDATE()) AS SEM, DATEPART(YEAR, GETDATE())%10 AS ANIO");
            //lote=""+rs.getInt("DIA")+String.format("%02d", rs.getInt("SEM"))+rs.getInt("ANIO")+"1";
            
            for(MovimientoProducto p: productos) {
                idEmpaque=p.getProducto().getIdProducto();
                
                if(p.getCantFacturada()> 0) {
                    capturados++;
                    
                    ps1.setDouble(1, p.getCosto());
                    ps1.setDouble(2, p.getDesctoProducto1());
                    ps1.setDouble(3, p.getDesctoProducto2());
                    ps1.setDouble(4, p.getDesctoConfidencial());
                    ps1.setDouble(5, p.getUnitario());
                    ps1.setDouble(6, p.getCantFacturada());
                    ps1.setDouble(7, p.getCantSinCargo());
                    ps1.setInt(8, idEmpaque);
                    ps1.executeUpdate();

                    impuestos=p.getImpuestos();
                    for(ImpuestosProducto i:impuestos) {
                        ps2.setDouble(1, i.getImporte());
                        ps2.setInt(2, idEmpaque);
                        ps2.executeUpdate();
                    }
                    
                    rs=st.executeQuery("SELECT existenciaOficina " +
                                        "FROM almacenesEmpaques " +
                                        "WHERE idAlmacen="+idAlmacen+" AND idEmpaque="+idEmpaque);
                    if(rs.next()) {
                        existenciaAnterior=rs.getDouble("existenciaOficina");
                        
                        ps5.setDouble(1, p.getCantFacturada()+p.getCantSinCargo());
                        ps5.setDouble(2, p.getCantFacturada());
                        ps5.setDouble(3, p.getUnitario());
                        ps5.setDouble(4, p.getCantFacturada());
                        ps5.setDouble(5, p.getCantSinCargo());
                        ps5.setInt(6, idEmpaque);
                        ps5.executeUpdate();
                    } else {
                        existenciaAnterior=0;
                        
                        ps4.setInt(1, idAlmacen);
                        ps4.setInt(2, idEmpaque);
                        ps4.setDouble(3, p.getCantFacturada()+p.getCantSinCargo());
                        ps4.setDouble(4, p.getUnitario()*p.getCantFacturada()/(p.getCantFacturada()+p.getCantSinCargo()));
                        ps4.setInt(5, idMovto);                 // El id del ultimo movimiento es el id de esta entrada
                        ps4.executeUpdate();
                    }
                    ps3.setInt(1, idEmpaque);
                    ps3.setDouble(2, existenciaAnterior);
                    ps3.setDouble(3, p.getCantFacturada()+p.getCantSinCargo());
                    ps3.executeUpdate();
                }
            }
            if(capturados>0) {
                st.executeUpdate("UPDATE facturas SET cerradaOficina=1 WHERE idFactura="+idFactura);
            }
            st.executeUpdate("COMMIT TRANSACTION");
            ok=true;
        } catch(SQLException e) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw(e);
        } finally {
            cn.close();
        }
        return ok;
    }
    
//    public int agregarEntrada(Entrada entrada, ArrayList<MovimientoProducto> productos, int idAlmacen) throws SQLException, NamingException {
//        int idMovto=0;
//        int idEmpaque;
////        double unitario;
//        int idImpuestoGrupo;
////        DAOImpuestosProducto daoImps=new DAOImpuestosProducto();
//        
//        Connection cn=this.ds.getConnection();
//        String strSQL="INSERT INTO movimientosDetalle (idMovto, idEmpaque, costo, desctoProducto1, desctoProducto2, desctoConfidencial, unitario, cantOrdenada, cantRecibida, idImpuestoGrupo, cantSinCargo, fecha) "
//                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, getdate())";
//        PreparedStatement ps=cn.prepareStatement(strSQL);
//        Statement st=cn.createStatement();
//        try {
//            st.executeUpdate("BEGIN TRANSACTION");
//            
//            strSQL="INSERT INTO movimientos (idTipo, idAlmacen, idComprobante, idOrdenCompra, idMoneda, tipoCambio, desctoComercial, desctoProntoPago, idUsuario) "
//                    + "VALUES (1, "+idAlmacen+", "+entrada.getComprobante().getIdComprobante()+", "+entrada.getIdOrdenCompra()+", 0, 1, "+entrada.getDesctoComercial()+", "+entrada.getDesctoProntoPago()+", "+this.idUsuario+")";
//            st.executeUpdate(strSQL);
//            
//            ResultSet rs=st.executeQuery("SELECT @@IDENTITY AS idMovto");
//            if(rs.next()) {
//                idMovto=rs.getInt("idMovto");
//            }
//            strSQL="INSERT INTO comprobantesOrdenesCompra (idComprobante, idOrdenCompra, idEntrada) " +
//                   "VALUES ("+entrada.getComprobante().getIdComprobante()+", "+entrada.getIdOrdenCompra()+", "+idMovto+")";
//            st.executeUpdate(strSQL);
//            
//            for(MovimientoProducto p: productos) {
//                ps.setInt(1, idMovto);
//                ps.setInt(2, p.getEmpaque().getIdEmpaque());
//                ps.setDouble(3, p.getPrecio());
//                ps.setDouble(4, p.getDesctoProducto1());
//                ps.setDouble(5, p.getDesctoProducto2());
//                ps.setDouble(6, p.getDesctoConfidencial());
//                ps.setDouble(7, p.getUnitario());
//                ps.setDouble(8, p.getCantOrdenada());
//                ps.setDouble(9, 0);
//                ps.setInt(10, p.getEmpaque().getProducto().getImpuestoGrupo().getIdGrupo());
//                ps.setDouble(11, p.getCantSinCargo());
//                ps.executeUpdate();
//                
//                idEmpaque=p.getEmpaque().getIdEmpaque();
//                idImpuestoGrupo=p.getEmpaque().getProducto().getImpuestoGrupo().getIdGrupo();
//                this.agregarImpuestosProducto(cn, idImpuestoGrupo, entrada.getIdImpuestoZona(), idMovto, idEmpaque);
//                this.calculaImpuestosProducto(cn, idMovto, idEmpaque, p.getUnitario(), p.getEmpaque().getPiezas());
//            }
//            st.executeUpdate("COMMIT TRANSACTION");
//        } catch(SQLException e) {
//            st.executeUpdate("ROLLBACK TRANSACTION");
//            throw(e);
//        } finally {
//            cn.close();
//        }
//        return idMovto;
//    }
    
    private void calculaImpuestosProducto(Connection cn, int idMovto, int idEmpaque, double unitario, double piezas) throws SQLException {
        Statement st=cn.createStatement();
        String strSQL="UPDATE movimientosDetalleImpuestos " +
                        "SET importe=CASE WHEN aplicable=0 THEN 0 " +
                                        "WHEN modo=1 THEN " + unitario + "*valor/100.00 " +
                                        "ELSE "+piezas+"*valor END " +
                        "WHERE idMovto="+idMovto+" AND idEmpaque="+idEmpaque;
        st.executeUpdate(strSQL);
    }
    
    private void agregarImpuestosProducto(Connection cn, int idImpuestoGrupo, int idZona, int idMovto, int idEmpaque) throws SQLException {
        Statement st=cn.createStatement();
        String strSQL="insert into movimientosDetalleImpuestos (idMovto, idEmpaque, idImpuesto, impuesto, valor, aplicable, modo, acreditable, importe) " +
                        "select "+idMovto+", "+idEmpaque+", id.idImpuesto, i.impuesto, id.valor, i.aplicable, i.modo, i.acreditable, 0.00 as importe " +
                        "from impuestosDetalle id " +
                        "inner join impuestos i on i.idImpuesto=id.idImpuesto " +
                        "where id.idGrupo="+idImpuestoGrupo+" and id.idZona="+idZona+" and GETDATE() between fechaInicial and fechaFinal";
        st.executeUpdate(strSQL);
    }
    
    public int buscarEntrada(int idComprobante, int idOrdenCompra) throws SQLException {
        int idEntrada=0;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery("SELECT idEntrada FROM comprobantesOrdenesCompra " +
                                         "WHERE idComprobante="+idComprobante+" AND idOrdenCompra="+idOrdenCompra);
            if(rs.next()) {
                idEntrada=rs.getInt("idEntrada");
            }
        } finally {
            cn.close();
        }
        return idEntrada;
    }
    
    public ArrayList<MovimientoProducto> obtenerDetalleEntrada(int idMovto) throws SQLException, NamingException {
        ArrayList<MovimientoProducto> lstProductos=new ArrayList<MovimientoProducto>();
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            MovimientoProducto prod;
            ResultSet rs=st.executeQuery("SELECT * FROM movimientosDetalle WHERE idMovto="+idMovto);
            while(rs.next()) {
                prod=construirProducto(rs);
                lstProductos.add(prod);
            }
        } finally {
            cn.close();
        }
        return lstProductos;
    }
    
    public int existeMovimiento(int idAlmacen, int idFactura, int idOrdenCompra) throws SQLException {
        int idMovto=0;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery("SELECT idMovto "
                    + "FROM movimientos "
                    + "WHERE idTipo=1 AND idAlmacen="+idAlmacen+" AND idFactura="+idFactura+" AND idOrdenCompra="+idOrdenCompra);
            if(rs.next()) {
                idMovto=rs.getInt("idMovto");
            }
        } finally {
            cn.close();
        }
        return idMovto;
    }
    
//    public ArrayList<Entrada> obtenerEntradas(int idFactura) throws SQLException {
//        ArrayList<Entrada> entradas=new ArrayList<Entrada>();
//        Connection cn=this.ds.getConnection();
//        Statement st=cn.createStatement();
//        try {
//            ResultSet rs=st.executeQuery("SELECT idMovto, idTipo, idAlmacen, idFactura, idOrdenCompra, idMoneda, tipoCambio, desctoComercial, desctoProntoPago, fecha, idUsuario "
//                    + "FROM movimientos "
//                    + "WHERE idTipo=1 AND idFactura="+idFactura);
//            while(rs.next()) {
//                entradas.add(construir(rs));
//            }
//        } finally {
//            cn.close();
//        }
//        return entradas;
//    }
    
    public int agregarMovimiento(int idAlmacen, int idFactura, int idOrdenCompra) throws SQLException {
        int idMovto=0;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");
            st.executeUpdate("INSERT INTO movimientos (idTipo, idAlmacen, idFactura, idOrdenCompra, idMoneda, tipoCambio, desctoComercial, desctoProntoPago, idUsuario, tipoComprobante) "
                    + "VALUES (1, "+idAlmacen+", "+idFactura+", "+idOrdenCompra+",0, 1, 0.00, 0.00, "+this.idUsuario+", 0)");
            ResultSet rs=st.executeQuery("SELECT @@IDENTITY AS idMovto");
            if(rs.next()) {
                idMovto=rs.getInt("idMovto");
            }
            st.executeUpdate("COMMIT TRANSACTION");
        } catch(SQLException e) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw(e);
        } finally {
            cn.close();
        }
        return idMovto;
    }
    
//    public Entrada modificarEntrada(int idAlmacen, int idFactura, int idOrdenCompra) throws SQLException {
//        Entrada entrada=null;
//        Connection cn=this.ds.getConnection();
//        Statement st=cn.createStatement();
//        try {
//            st.executeUpdate("BEGIN TRANSACTION");
//            ResultSet rs=st.executeQuery("SELECT idMovto, idTipo, idAlmacen, idFactura, idOrdenCompra, idMoneda, tipoCambio, desctoComercial, desctoProntoPago, fecha, idUsuario "
//                        + "FROM movimientos "
//                        + "WHERE idTipo=1 AND idAlmacen="+idAlmacen+" AND idFactura="+idFactura+" AND idOrdenCompra="+idOrdenCompra);
//            if(rs.next()) {
//                entrada=construir(rs);
//            } else {
//                st.executeUpdate("INSERT INTO movimientos (idTipo, idAlmacen, idFactura, idOrdenCompra, idMoneda, tipoCambio, desctoComercial, desctoProntoPago, idUsuario) "
//                        + "VALUES (1, "+idAlmacen+", "+idFactura+", "+idOrdenCompra+", 0, 1, 0.00, 0.00, "+this.idUsuario+")");
//                rs=st.executeQuery("SELECT @@IDENTITY AS idMovto");
//                if(rs.next()) {
//                    entrada=new Entrada();
//                    entrada.setIdEntrada(rs.getInt("idMovto"));
//                    entrada.setDesctoComercial(0.00);
//                    entrada.setDesctoProntoPago(0.00);
//                    entrada.setIdUsuario(this.idUsuario);
//                }
//            }
//            st.executeUpdate("COMMIT TRANSACTION");
//        } catch(SQLException e) {
//            st.executeUpdate("ROLLBACK TRANSACTION");
//            throw(e);
//        } finally {
//            cn.close();
//        }
//        return entrada;
//    }
    
    
    
    
    /*
    public int agregarEntrada(int idProveedor, int idEmpresa, int idAlmacen, int idFactura) {
        int idEntrada=0;
        return idEntrada;
    }
    */
    public MovimientoProducto obtenerProducto(int idEntrada, int idEmpaque) throws SQLException {
        MovimientoProducto producto=null;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery("SELECT * FROM almacenes WHERE idEntrada="+idEntrada+" and idEmpaque="+idEmpaque);
            if(rs.next()) {
                producto=construirProducto(rs);
            }
        } finally {
            cn.close();
        }
        return producto;
    }
    
    public MovimientoProducto construirProducto(ResultSet rs) throws SQLException {
        MovimientoProducto producto=new MovimientoProducto();
//        producto.setEmpaque(new Empaque(rs.getInt("idEmpaque")));
        producto.setProducto(new Producto());
        producto.getProducto().setIdProducto(rs.getInt("idEmpaque"));
        producto.setDesctoProducto1(rs.getDouble("desctoProducto1"));
        producto.setDesctoProducto2(rs.getDouble("desctoProducto2"));
        producto.setDesctoConfidencial(rs.getDouble("desctoConfidencial"));
        producto.setCantOrdenada(rs.getDouble("cantOrdenada"));
        producto.setCantFacturada(rs.getDouble("cantFacturada"));
        producto.setCantSinCargo(rs.getDouble("cantSinCargo"));
        producto.setCantRecibida(rs.getDouble("cantRecibida"));
        producto.setCosto(rs.getDouble("costo"));
        return producto;
    }
}
