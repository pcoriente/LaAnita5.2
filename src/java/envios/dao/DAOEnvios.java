package envios.dao;

import envios.dominio.EnvioPedido;
import envios.to.TOEnvio;
import envios.to.TOEnvioProducto;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOEnvios {
    private DataSource ds;
    
    public DAOEnvios() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/"+usuarioSesion.getJndi());
    }
    
    public void enviarDirectos(TOEnvioProducto to, double oldValue) throws SQLException {
        String strSQL;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            if(oldValue==0) {
                strSQL= "INSERT INTO enviosPedidosDetalle (idEnvio, idMovto, idEmpaque, cantidad, peso) " +
                        "VALUES("+to.getIdEnvio()+", "+to.getIdMovto()+", "+to.getIdEmpaque()+", "+to.getEnviados()+", "+to.getPeso()+")";
                st.executeUpdate(strSQL);
                
                strSQL= "UPDATE movimientosDetalle SET cantRecibida=cantRecibida+"+to.getEnviados()+" " +
                        "WHERE idMovto="+to.getIdMovto()+" AND idEmpaque="+to.getIdEmpaque();
                st.executeUpdate(strSQL);
            } else if(to.getEnviados()==0) {
                strSQL= "DELETE FROM enviosPedidosDetalle " +
                        "WHERE idEnvio="+to.getIdEnvio()+" AND idMovto="+to.getIdMovto()+" AND idEmpaque="+to.getIdEmpaque();
                st.executeUpdate(strSQL);
                
                strSQL= "UPDATE movimientosDetalle SET cantRecibida=cantRecibida-"+oldValue+" "+
                        "WHERE idMovto="+to.getIdMovto()+" AND idEmpaque="+to.getIdEmpaque();
                st.executeUpdate(strSQL);
            } else {
                strSQL= "UPDATE enviosPedidosDetalle SET cantidad="+to.getEnviados()+" "+
                        "WHERE idEnvio="+to.getIdEnvio()+" AND idMovto="+to.getIdMovto()+" AND idEmpaque="+to.getIdEmpaque();
                st.executeUpdate(strSQL);
                
                if(oldValue < to.getEnviados()) {
                    strSQL= "UPDATE movimientosDetalle SET cantRecibida=cantRecibida+"+(to.getEnviados()-oldValue)+" " +
                            "WHERE idMovto="+to.getIdMovto()+" AND idEmpaque="+to.getIdEmpaque();
                    st.executeUpdate(strSQL);
                } else {
                    strSQL= "UPDATE movimientosDetalle SET cantRecibida=cantRecibida-"+(oldValue-to.getEnviados())+" " +
                            "WHERE idMovto="+to.getIdMovto()+" AND idEmpaque="+to.getIdEmpaque();
                    st.executeUpdate(strSQL);
                }
            }
        } finally {
            st.close();
            cn.close();
        }
    }
    
    private TOEnvioProducto construir(ResultSet rs) throws SQLException {
        TOEnvioProducto to= new TOEnvioProducto();
        to.setIdEnvio(rs.getInt("idEnvio"));
        to.setIdMovto(rs.getInt("idMovto"));
        to.setIdEmpaque(rs.getInt("idEmpaque"));
        to.setEnviados(rs.getDouble("enviados"));
        to.setPendientes(rs.getDouble("pendientes"));
        to.setPeso(rs.getDouble("peso"));
        return to;
    }
    
    public ArrayList<TOEnvioProducto> obtenerEnvioDetalle(int idEnvio, int idMovto) throws SQLException {
        ArrayList<TOEnvioProducto> detalle=new ArrayList<TOEnvioProducto>();
        String strSQL = "SELECT CASE WHEN PD.idEnvio IS NULL THEN D.idEnvio ELSE PD.idEnvio END AS idEnvio, D.idMovto, D.idEmpaque\n" +
            "		, ISNULL(PD.enviados,0) AS enviados, D.pendientes, D.peso\n" +
            "FROM (SELECT 0 AS idEnvio, D.idMovto, D.idEmpaque, D.cantFacturada+D.cantSinCargo-D.cantRecibida AS pendientes, E.peso\n" +
            "       FROM movimientosDetalle D INNER JOIN empaques E ON E.idEmpaque=D.idEmpaque WHERE D.idMovto="+idMovto+") D\n" +
            "LEFT JOIN (SELECT D.idEnvio, D.idMovto, D.idProducto, D.cantidad AS enviados\n" +
            "		FROM enviosPedidosDetalle D WHERE D.idEnvio="+idEnvio+" AND D.idMovto="+idMovto+") PD ON PD.idProducto=D.idEmpaque";
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                detalle.add(this.construir(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return detalle;
    }
    
    private EnvioPedido convertirPedido(ResultSet rs) throws SQLException {
        EnvioPedido pedido=new EnvioPedido();
        pedido.setIdEnvio(rs.getInt("idEnvio"));
        pedido.setIdMovto(rs.getInt("idMovto"));
        pedido.setAgregado(rs.getBoolean("agregado"));
        pedido.setDirecto(rs.getBoolean("directo"));
        pedido.setOrdenDeCarga(rs.getString("ordenDeCarga"));
        pedido.setFolio(rs.getInt("folio"));
        pedido.setFecha(new Date(rs.getDate("fecha").getTime()));
        pedido.setFormato(rs.getString("formato"));
        pedido.setIdTienda(rs.getInt("idTienda"));
        pedido.setTienda(rs.getString("tienda"));
        return pedido;
    }
    
    public ArrayList<EnvioPedido> obtenerFincadosEnvio(int idAlmacen, int idEnvio) throws SQLException {
        // En cantRecibida se graba el total de enviados del empaque
        // Los pedidos fincados que no se han enviado ( PARA NADA ) y los pedidos directos en el envio en cuestion o NO enviados completamente.
        // Los pedidos directos en movimientos tienen cedis=1 y almacen=3, pero en enviosPedidos tiene el cedis y almacen originales.
        ArrayList<EnvioPedido> pedidos=new ArrayList<EnvioPedido>();
        String strSQL = "SELECT 0 AS idEnvio, M.idMovto, 0 as agregado, 0 AS directo, 0 AS ordenDeCarga, M.folio, M.fecha, F.formato, T.idTienda, T.tienda\n" +
                        "FROM movimientos M\n" +
                        "INNER JOIN clientesTiendas T ON T.idTienda=M.idReferencia\n" +
                        "INNER JOIN clientesFormatos F ON F.idFormato=T.idFormato\n" +
                        "WHERE M.idAlmacen="+idAlmacen+" AND M.idTipo=28 AND M.status=0 AND M.referencia!=0\n" +
                        "UNION\n" +
                        "SELECT P.idEnvio, M.idMovto, 1 AS agregado, EP.directo, 0 AS ordenDeCarga, M.folio, M.fecha, F.formato, T.idTienda, T.tienda\n" +
                        "FROM (SELECT P.idEnvio, P.idMovto\n" +
                        "       FROM enviosPedidos P\n" +
                        "       INNER JOIN movimientosDetalle D ON D.idMovto=P.idMovto\n" +
                        "       WHERE P.idAlmacen="+idAlmacen+" AND (P.idEnvio="+idEnvio+" OR P.idEnvio=0)\n" +
                        "       GROUP BY P.idEnvio, P.idMovto\n" +
                        "       HAVING P.idEnvio="+idEnvio+" OR SUM(D.cantFacturada+D.cantSinCargo) > SUM(D.cantRecibida)) P\n" +
                        "INNER JOIN movimientos M ON M.idMovto=P.idMovto\n" +
                        "INNER JOIN enviosPedidos EP ON EP.idEnvio=P.idEnvio AND EP.idMovto=P.idMovto\n" +
                        "INNER JOIN clientesTiendas T ON T.idTienda=M.idReferencia\n" +
                        "INNER JOIN clientesFormatos F ON F.idFormato=T.idFormato\n" +
                        "ORDER BY idMovto";
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                pedidos.add(convertirPedido(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return pedidos;
    }
    
//    public ArrayList<EnvioPedido> obtenerFincadosEnvio(int idAlmacen, int idEnvio) throws SQLException {
//        ArrayList<EnvioPedido> pedidos=new ArrayList<EnvioPedido>();
//        String strSQL = "SELECT 0 AS idEnvio, M.idMovto, 0 AS idAlmacen, 0 AS ordenDeCarga, M.folio, M.fecha, F.formato, T.idTienda, T.tienda\n" +
//                        "FROM movimientosDetalle D\n" +
//                        "INNER JOIN movimientos M ON M.idMovto=D.idMovto\n" +
//                        "INNER JOIN clientesTiendas T ON T.idTienda=M.idReferencia\n" +
//                        "INNER JOIN clientesFormatos F ON F.idTienda=T.idTienda\n" +
//                        "LEFT JOIN enviosPedidos P ON P.idEnvio="+idEnvio+" AND P.idMovto=M.idMovto AND P.idTipoMovto=M.idTipo\n" +
//                        "WHERE M.idAlmacen="+idAlmacen+" AND M.idTipo=28 AND M.referencia!=0 AND P.idEnvio IS NULL\n" +
//                        "GROUP BY M.idMovto, M.status, M.folio, M.fecha, T.tienda\n" +
//                        "HAVING M.status = 0 OR SUM(D.cantFacturada+D.cantSinCargo) > SUM(D.cantRecibida)\n" +
//                        "UNION\n" +
//                        "SELECT P.idEnvio, P.idMovto, P.idAlmacen, P.ordenDeCarga, M.folio, M.fecha, F.formato, T.idTienda, T.tienda\n" +
//                        "FROM enviosPedidos P\n" +
//                        "INNER JOIN movimientos M ON M.idMovto=P.idMovto\n" +
//                        "INNER JOIN clientesTiendas T ON T.idTienda=M.idReferencia\n" +
//                        "INNER JOIN clientesFormatos F ON F.idTienda=T.idTienda\n" +
//                        "WHERE P.idEnvio="+idEnvio+"\n" +
//                        "ORDER BY idMovto";
//        Connection cn=this.ds.getConnection();
//        Statement st=cn.createStatement();
//        try {
//            ResultSet rs=st.executeQuery(strSQL);
//            while(rs.next()) {
//                pedidos.add(convertirPedido(rs));
//            }
//        } finally {
//            st.close();
//            cn.close();
//        }
//        return pedidos;
//    }
    
    public TOEnvio obtenerEnvio(int idEnvio) throws SQLException {
        TOEnvio to=null;
        String strSQL="SELECT * FROM envios WHERE idEnvio="+idEnvio;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                to=this.convertir(rs);
            }
        } finally {
            st.close();
            cn.close();
        }
        return to;
    }
    
    public ArrayList<TOEnvio> obtenerEnvios(int idCedis, int idAlmacen) throws SQLException {
        ArrayList<TOEnvio> envios=new ArrayList<TOEnvio>();
        String strSQL = "SELECT * FROM envios " +
                        "WHERE idCedis="+idCedis+" AND idAlmacen="+idAlmacen+" " +
                            "AND generado BETWEEN CONVERT(varchar(10), DATEADD(DAY, -30, getdate()), 120) AND CONVERT(varchar, getdate(), 120) " +
                        "ORDER BY generado DESC";
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                envios.add(convertir(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return envios;
    }
    
    private TOEnvio convertir(ResultSet rs) throws SQLException {
        TOEnvio to=new TOEnvio();
        to.setIdEnvio(rs.getInt("idEnvio"));
        to.setIdCedis(rs.getInt("idCedis"));
        to.setIdEmpresa(rs.getInt("idEmpresa"));
        to.setIdAlmacen(rs.getInt("idAlmacen"));
        to.setGenerado(new Date(rs.getDate("generado").getTime()));
        to.setEnviado(new Date(rs.getDate("enviado").getTime()));
        to.setPeso(rs.getDouble("peso"));
        to.setStatus(rs.getInt("status"));
        to.setPrioridad(rs.getInt("prioridad"));
        to.setIdChofer(rs.getInt("idChofer"));
        to.setIdCamion(rs.getInt("idCamion"));
        return to;
    }
    
//    public TOEnvio obtenerEnvio(int idEnvio) throws SQLException {
//        TOEnvio to=null;
//        String strSQL="SELECT * FROM envios WHERE idEnvio="+idEnvio;
//        Connection cn=this.ds.getConnection();
//        Statement st=cn.createStatement();
//        try {
//            ResultSet rs=st.executeQuery(strSQL);
//            if(rs.next()) {
//                to=convertir(rs);
//            }
//        } finally {
//            st.close();
//            cn.close();
//        }
//        return to;
//    }
}
