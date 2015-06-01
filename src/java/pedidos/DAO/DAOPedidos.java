package pedidos.DAO;

import entradas.dao.DAOMovimientos1;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import pedidos.to.TOPedido;
import pedidos.to.TOPedidoProducto;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOPedidos {
    int idUsuario;
    int idCedis;
    private DataSource ds = null;

    public DAOPedidos() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
        this.idUsuario = usuarioSesion.getUsuario().getId();
        this.idCedis = usuarioSesion.getUsuario().getIdCedis();

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
    }
    
    public void eliminarPedido(int idPedido) throws SQLException {
        String strSQL;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.execute("BEGIN TRANSACTION");
            
            strSQL="DELETE FROM pedidosOC WHERE idPedido="+idPedido;
            st.executeUpdate(strSQL);
            
            strSQL="DELETE FROM pedidosOCTienda WHERE idPedido="+idPedido;
            st.executeUpdate(strSQL);
            
            strSQL="DELETE FROM pedidosOCTiendaDetalle WHERE idPedido="+idPedido;
            st.executeUpdate(strSQL);
            
            st.execute("COMMIT TRANSACTION");
        } catch (SQLException e) {
            st.execute("ROLLBACK TRANSACTION");
            throw(e);
        } finally {
            st.close();
            cn.close();
        }
    }
    
    public void cerrarPedido(int idPedido) throws SQLException {
        String strSQL="UPDATE pedidosOC SET status=1 WHERE idPedido="+idPedido;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate(strSQL);
        } finally {
            st.close();
            cn.close();
        }
    }
    
    public boolean liberarPedido(int idPedido) throws SQLException, Exception {
        boolean liberado=true;
        String strSQL="SELECT propietario FROM pedidosOC WHERE idPedido="+idPedido;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.execute("BEGIN TRANSACTION");
            
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                int propietario=rs.getInt("propietario");
                if(propietario==this.idUsuario) {
                    strSQL="UPDATE pedidosOC SET propietario=0 WHERE idPedido="+idPedido;
                    st.executeUpdate(strSQL);
                }
            } else {
                throw new Exception("No se encontro el pedido !!!");
            }
            st.execute("COMMIT TRANSACTION");
            
        } catch (SQLException ex) {
            st.execute("ROLLBACK TRANSACTION");
            throw ex;
        } catch (Exception ex) {
            st.execute("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            cn.close();
        }
        return liberado;
    }
    
//    public void grabarMovimiento(TOPedidoProducto to) throws SQLException {
//        String strSQL = "UPDATE pedidosOCTiendaDetalle "+
//                        "SET cantFacturada="+to.getCantFacturada()+" "+
//                        "WHERE idPedido="+to.getIdPedido()+" AND idEmpaque="+to.getIdEmpaque();
//        Connection cn = this.ds.getConnection();
//        Statement st = cn.createStatement();
//        try {
//            st.executeUpdate(strSQL);
//        } finally {
//            st.close();
//            cn.close();
//        }
//    }
    
//    public void traspasarSinCargo(int idPedido, int idProdOrigen, TOPedidoProducto to, double cantidad) throws SQLException {
//        String strSQL="";
//        Connection cn = this.ds.getConnection();
//        Statement st = cn.createStatement();
//        try {
//            st.execute("BEGIN TRANSACTION");
//            
//            if(to.getIdPedido()==0) {
//                to.setIdPedido(idPedido);
//                to.setCantSinCargo(cantidad);
//                this.agregarProducto(to);
//            } else {
//                strSQL= "UPDATE pedidosOCTiendaDetalle\n" +
//                        "SET cantSinCargo=cantSinCargo+"+cantidad+"\n" +
//                        "WHERE idPedido="+idPedido+" AND idEmpaque="+to.getIdEmpaque();
//                st.executeUpdate(strSQL);
//            }
//            strSQL= "UPDATE pedidosOCTiendaDetalle SET cantSinCargo=cantSinCargo-"+cantidad+"\n" +
//                    "WHERE idPedido="+idPedido+" AND idEmpaque="+idProdOrigen;
//            st.executeUpdate(strSQL);
//            
//            st.execute("COMMIT TRANSACTION");
//        } finally {
//            st.close();
//            cn.close();
//        }
//    }
    
//    public void agregarProducto(int idPedido, int idEmpresa, TOPedidoProducto to, int idTienda) throws SQLException {
//        String strSQL = "INSERT INTO pedidosOCTiendaDetalle (idPedido, idEmpaque, cantFacturada, cantSinCargo, unitario, idImpuestoGrupo)\n" +
//                        "VALUES ("+idPedido+", "+to.getIdEmpaque()+", "+to.getCantFacturada()+", "+to.getCantSinCargo()+", "+to.getUnitario()+", "+to.getIdImpuestoGrupo()+")";
//        Connection cn = this.ds.getConnection();
//        Statement st = cn.createStatement();
//        try {
//            st.executeUpdate(strSQL);
//            to.setIdPedido(idPedido);
//            
//            this.calcularPrecioNeto(idPedido, to, idTienda);
//        } finally {
//            st.close();
//            cn.close();
//        }
//    }
    
    public TOPedidoProducto obtenerPedidoProducto(int idPedido, int idProducto) throws SQLException {
        TOPedidoProducto to=null;
        String strSQL= "SELECT * FROM pedidosOCTiendaDetalle WHERE idPedido="+idPedido+" AND idEmpaque="+idProducto;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                to=this.construirProducto(rs);
            }
        } finally {
            st.close();
            cn.close();
        }
        return to;
    }
    
    public ArrayList<TOPedidoProducto> obtenerSimilaresPedido(int idPedido, int idProducto) throws SQLException {
        ArrayList<TOPedidoProducto> productos=new ArrayList<TOPedidoProducto>();
        String strSQL= "SELECT CASE WHEN S.idEmpaque=S.idSimilar THEN 1 ELSE 0 END AS principal\n" +
                        "	, ISNULL(D.idPedido, 0) AS idPedido, ISNULL(D.idEmpaque,S.idEmpaque) AS idEmpaque\n" +
                        "	, ISNULL(D.cantFacturada, 0) AS cantFacturada, ISNULL(D.cantSinCargo, 0) AS cantSinCargo\n" +
                        "	, ISNULL(D.unitario, 0) AS unitario, P.idImpuesto AS idImpuestoGrupo\n" +
                        "FROM empaquesSimilares S\n" +
                        "LEFT JOIN (SELECT * FROM pedidosOCTiendaDetalle WHERE idPedido="+idPedido+") D ON D.idEmpaque=S.idEmpaque\n" +
                        "INNER JOIN empaques E ON E.idEmpaque=S.idEmpaque\n" +
                        "INNER JOIN productos P ON P.idProducto=E.idProducto\n" +
                        "WHERE S.idSimilar="+idProducto+" AND S.idSimilar!=S.idEmpaque\n" +
                        "ORDER BY principal DESC, idPedido DESC";
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                productos.add(this.construirProducto(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return productos;
    }
    
    public ArrayList<TOPedidoProducto> obtenerPedidoSimilares(int idPedido, int idProducto) throws SQLException {
        ArrayList<TOPedidoProducto> productos=new ArrayList<TOPedidoProducto>();
        String strSQL= "SELECT D.*\n" +
                        "FROM empaquesSimilares S\n" +
                        "INNER JOIN pedidosOCTiendaDetalle D ON D.idEmpaque=S.idEmpaque\n" +
                        "WHERE D.idPedido="+idPedido+" AND S.idSimilar="+idProducto;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                productos.add(this.construirProducto(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return productos;
    }
    
    public TOPedidoProducto construirProducto(ResultSet rs) throws SQLException {
        TOPedidoProducto to=new TOPedidoProducto();
        to.setIdPedido(rs.getInt("idPedido"));
        to.setIdEmpaque(rs.getInt("idEmpaque"));
        to.setCantFacturada(rs.getDouble("cantFacturada"));
        to.setCantSinCargo(rs.getDouble("cantSinCargo"));
        to.setUnitario(rs.getDouble("unitario"));
        to.setIdImpuestoGrupo(rs.getInt("idImpuestoGrupo"));
        return to;
    }
    
    public ArrayList<TOPedidoProducto> obtenerPedidoDetalle(int idPedido) throws SQLException {
        ArrayList<TOPedidoProducto> productos=new ArrayList<TOPedidoProducto>();
        String strSQL = "SELECT * FROM pedidosOCTiendaDetalle WHERE idPedido="+idPedido;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                productos.add(this.construirProducto(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return productos;
    }
    
    public boolean asegurarPedido(int idPedido) throws SQLException, Exception {
        boolean asegurado=true;
        String strSQL="SELECT propietario FROM pedidosOC WHERE idPedido="+idPedido;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.execute("BEGIN TRANSACTION");
            
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                int propietario=rs.getInt("propietario");
                if(propietario==0) {
                    strSQL="UPDATE pedidosOC SET propietario="+this.idUsuario+" WHERE idPedido="+idPedido;
                    st.executeUpdate(strSQL);
                } else if(propietario!=this.idUsuario) {
                    asegurado=false;
                    strSQL="SELECT * FROM webSystem.dbo.usuarios WHERE idUsuario="+propietario;
                    rs=st.executeQuery(strSQL);
                    if(rs.next()) {
                        strSQL=rs.getString("usuario");
                    } else {
                        strSQL="";
                    }
                    throw new Exception("No se puede asegurar el movimiento, lo tiene el usuario(id="+propietario+"): "+strSQL+" !!!");
                }
            } else {
                throw new Exception("No se encontro el pedido !!!");
            }
            st.execute("COMMIT TRANSACTION");
            
        } catch (SQLException ex) {
            st.execute("ROLLBACK TRANSACTION");
            throw ex;
        } catch (Exception ex) {
            st.execute("ROLLBACK TRANSACTION");
            throw ex;
        } finally {
            st.close();
            cn.close();
        }
        return asegurado;
    }
    
    public int agregarPedido(TOPedido to) throws SQLException {
        int idPedido=0;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        java.sql.Date fecha, ordenDeCompraFecha, cancelacionFecha;
//        fecha=new java.sql.Date(to.getFecha().getTime());
//        ordenDeCompraFecha=new java.sql.Date(to.getOrdenDeCompraFecha().getTime());
//        cancelacionFecha=new java.sql.Date(to.getCancelacionFecha().getTime());
        String strSQL = "INSERT INTO pedidosOC (idAlmacen, idTienda, fecha, status, propietario, ordenDeCompra, ordenDeCompraFecha, cancelacionMotivo, cancelacionFecha)\n" +
                        "VALUES ("+to.getIdAlmacen()+", "+to.getIdTienda()+", GETDATE(), "+to.getStatus()+", "+this.idUsuario+", '"+to.getOrdenDeCompra()+"', '"+format.format(to.getOrdenDeCompraFecha())+"', '"+to.getCancelacionMotivo()+"', '2001-01-01')";
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");
            
            st.executeUpdate(strSQL);
            ResultSet rs=st.executeQuery("SELECT @@IDENTITY AS idPedido");
            if (rs.next()) {
                idPedido = rs.getInt("idPedido");
            }
            st.executeUpdate("COMMIT TRANSACTION");
            
        } catch (SQLException e) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw(e);
        } finally {
            st.close();
            cn.close();
        }
        return idPedido;
    }
    
    private TOPedido construirPedido(ResultSet rs) throws SQLException {
        TOPedido to=new TOPedido();
        to.setIdPedido(rs.getInt("idPedido"));
        to.setIdEmpresa(rs.getInt("idEmpresa"));
        to.setIdAlmacen(rs.getInt("idAlmacen"));
        to.setIdTienda(rs.getInt("idTienda"));
        to.setOrdenDeCompra(rs.getString("ordenDeCompra"));
        to.setOrdenDeCompraFecha(new java.util.Date(rs.getDate("ordenDeCompraFecha").getTime()));
        to.setFecha(new java.util.Date(rs.getDate("fecha").getTime()));
        to.setStatus(rs.getInt("status"));
        to.setCancelacionFecha(new java.util.Date(rs.getDate("cancelacionFecha").getTime()));
        to.setCancelacionMotivo(rs.getString("cancelacionMotivo"));
        return to;
    }
    
    public ArrayList<TOPedido> obtenerPedidos(int idAlmacen, int status, Date fechaInicial) throws SQLException {
        if(fechaInicial==null) {
            fechaInicial=new Date();
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TOPedido> pedidos=new ArrayList<TOPedido>();
        String strSQL = "SELECT P.*, A.idEmpresa\n" +
                        "FROM pedidosOC P\n" +
                        "INNER JOIN almacenes A on A.idAlmacen=P.idAlmacen\n" +
                        "WHERE P.idAlmacen="+idAlmacen+" AND P.status BETWEEN 0 AND "+status+" AND CONVERT(date, P.fecha) <= '" + format.format(fechaInicial) + "'";
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                pedidos.add(this.construirPedido(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return pedidos;
    }
}
