package pedidos.dao;

import java.sql.Connection;
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
import leyenda.dao.DAOBancosLeyendas;
import pedidos.Pedidos;
import pedidos.dominio.EntregasWallMart;
import pedidos.dominio.Textual;
import pedidos.to.TOPedido;
import pedidos.to.TOPedidoProducto;
import tiendas.Tiendas;
import tiendas.to.TOTienda;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author Usuario
 */
public class DAOCargaPedidos {

    int idUusario, idCedis;
    private DataSource ds = null;

    public DAOCargaPedidos() {

        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
            this.idUusario = usuarioSesion.getUsuario().getId();
            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
        } catch (NamingException ex) {
            Logger.getLogger(DAOBancosLeyendas.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public ArrayList<TOPedido> crearPedidos(int idEmp, int idGpoCte, int idFto, ArrayList<Textual> textual, String electronico) throws SQLException {
        TOTienda toTienda;
        String strSQL, oc = "";
        int ct = 0;
        TOPedido toPed = new TOPedido(28);
        TOPedidoProducto toProd = new TOPedidoProducto();
        ArrayList<TOPedido> pedidos = new ArrayList<>();
        try (Connection cn = ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs;
                for (Textual che : textual) {
                    if (!che.getOrdenCompra().equals(oc) || (che.getOrdenCompra().equals(oc) && che.getCodigoTienda() != ct)) {
//                    if (!che.getOrdenCompra().equals(oc) || (che.getOrdenCompra().equals(oc) && che.getCodigoTienda() != ct)){
                        toPed = new TOPedido(28);
                        toPed.setElectronico(electronico);

                        toTienda = Tiendas.validaTienda(cn, che.getCodigoTienda(), idGpoCte, idFto);
                        if (toTienda == null) {
                            throw new SQLException("La Tienda " + che.getCodigoTienda() + " No Existe");
                        }
                        strSQL = "SELECT A.idAlmacen\n"
                                + "FROM agentes G\n"
                                + "INNER JOIN almacenes A ON A.idCedis=G.idCedis AND A.idEmpresa=" + idEmp + " AND A.pedidoElectronico=1\n"
                                + "WHERE G.idAgente=" + toTienda.getIdAgente();
                        rs = st.executeQuery(strSQL);
                        if (!rs.next()) {
                            throw new SQLException("No se encontró el almacén !!");
                        }
                        oc = che.getOrdenCompra();
                        ct = che.getCodigoTienda();
                        toPed.setIdEmpresa(idEmp);
                        toPed.setIdAlmacen(rs.getInt("idAlmacen"));
                        toPed.setTipoDeCambio(1);
                        toPed.setOrdenDeCompra(oc);
                        toPed.setOrdenDeCompraFecha(che.getFechaElaboracion());
                        toPed.setEntregaFecha(che.getFechaEmbarque());
                        toPed.setEntregaFechaMaxima(che.getFechaCancelacion());
                        toPed.setIdImpuestoZona(idGpoCte);
                        toPed.setIdReferencia(toTienda.getIdTienda());
                        toPed.setIdImpuestoZona(toTienda.getIdImpuestoZona());
                        toPed.setIdUsuario(this.idUusario);
                        toPed.setEstatus(5);
                        Pedidos.agregarPedidoConOrden(cn, toPed, 1);
                        pedidos.add(toPed);
                    }
                    strSQL = "SELECT E.cod_pro, E.idEmpaque, E.piezas, P.idImpuesto\n"
                            + "FROM empaquesUpcs U\n"
                            + "INNER JOIN empaques E ON E.idEmpaque=U.idProducto\n"
                            + "INNER JOIN productos P ON P.idProducto=E.idProducto\n"
                            + "WHERE U.upc='" + che.getUpc() + "'";
                    rs = st.executeQuery(strSQL);
                    if (!rs.next()) {
                        throw new SQLException("¡¡ No se encontró el UPC pedido !!\n"
                                + "en tabla empaquesUpcs (UPC='" + che.getUpc() + "')");
                    }
                    toProd.setIdPedido(toPed.getReferencia());
                    toProd.setIdProducto(rs.getInt("idEmpaque"));
                    toProd.setPiezas(rs.getInt("piezas"));
                    toProd.setCantOrdenada(che.getCantidad() * rs.getInt("piezas"));
                    toProd.setIdImpuestoGrupo(rs.getInt("idImpuesto"));
                    toProd.setIdMovto(toPed.getIdMovto());
                    Pedidos.agregaProductoPedido(cn, toPed, toProd);
//                    if (toProd.getUnitario() != che.getCosto()) {
//                        throw new SQLException("¡¡ El precio unitario no coincide con el pedido !!\n"
//                                + "codigoTienda=" + che.getCodigoTienda() + " (idTienda=" + toPed.getIdReferencia() + "), sku='" + rs.getString("cod_pro") + "' (idEmpaque=" + toProd.getIdProducto()+")");
//                    }
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);

            }

        }
        return pedidos;
    }
//Esta consulta se paso a la clase tiendas 
//    public int validaTienda(int codigoTienda, int idGrupoCte, int idFormato) throws SQLException {
//        int idTienda = 0;
//        Connection cn = ds.getConnection();
//        try (Statement st = cn.createStatement()) {
//
//            String sq = "SELECT cT.idTienda from clientesTiendas cT \n"
//                    + "inner join clientesTiendasCodigos cTC on cTC.idTienda = cT.idTienda\n"
//                    + "inner join clientes c on c.idCliente=cT.idCliente\n"
//                    + "where codigoTienda =" + codigoTienda + " and c.idGrupoCte=" + idGrupoCte + (idFormato != 0 ? " and cT.idFormato = " + idFormato : "");
//            ResultSet rs = st.executeQuery(sq);
//            if (rs.next()) {
//                idTienda = rs.getInt("idTienda");
//            } else {
//                throw new SQLException("El Numero de Tienda " + codigoTienda + " no Existe");
//            }
//        }
//        return idTienda;
//    }

    public void escribeEntregasWallMart(ArrayList<EntregasWallMart> gln) throws SQLException {
        try (Connection cn = ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                String BorraSql = "truncate table entregasWallMart";
                st.executeUpdate(BorraSql);
                for (EntregasWallMart ew : gln) {
                    String sentenciaSQL = "INSERT entregasWallMart (idGln,idTienda) values("
                            + "'" + ew.getIdGln() + "','" + ew.getIdTienda() + "')";
                    st.execute(sentenciaSQL);
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

//    public HashMap leeEntregasWallMart() throws SQLException {
//        Connection cn = ds.getConnection();
//        HashMap glns = new HashMap();
//        Statement st = cn.createStatement();
//        String sql = "SELECT  * FROM entregasWallMart";
//        try {
//            ResultSet rs = st.executeQuery(sql);
//            while (rs.next()) {
//                EntregasWallMart entregasWallMart = new EntregasWallMart();
//                entregasWallMart.setIdGln(rs.getString("idGln"));
//                entregasWallMart.setIdTienda(rs.getString("idTienda"));
//                String cIdGln = entregasWallMart.getIdGln().substring(8, 13);
//                String cIdTienda = entregasWallMart.getIdTienda();
//                glns.put(cIdGln, cIdTienda);
//            }
//        } finally {
//            cn.close();
//        }
//        return glns;
//    }
}
