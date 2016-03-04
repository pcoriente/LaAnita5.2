package pedidos.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
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
import pedidos.dominio.Chedraui;
import pedidos.dominio.EntregasWallMart;
import pedidos.to.TOPedido;
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

    public void crearPedidos(int idEmp, int idGpoCte, int idFto, ArrayList<Chedraui> chedraui, String electronico) throws SQLException {
        TOTienda toTienda;
        String strSQL, oc = "";
        TOPedido toPed = new TOPedido(28);
        toPed.setElectronico(electronico);
        try (Connection cn = ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                for (Chedraui che : chedraui) {
                    if (!che.getOrdenCompra().equals(oc)) {
                        toTienda = Tiendas.validaTienda(cn, che.getCodigoTienda(), idGpoCte, idFto);
                        if (toTienda == null) {
                            throw new SQLException("La Tienda " + che.getCodigoTienda() + " No Existe");
                        }
                        strSQL = "SELECT A.idAlmacen\n"
                                + "FROM agentes G\n"
                                + "INNER JOIN almacenes A ON A.idCedis=G.idCedis AND A.idEmpresa=" + idEmp + " AND A.pedidoElectronico=1\n"
                                + "WHERE G.idAgente=" + toTienda.getIdAgente();
                        ResultSet rs = st.executeQuery(strSQL);
                        if (!rs.next()) {
                            throw new SQLException("No se encontró el almacén !!");
                        }
                        oc = che.getOrdenCompra();
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
                        Pedidos.agregarPedido(cn, toPed, 1);
                    }
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

    public HashMap leeEntregasWallMart() throws SQLException {
        Connection cn = ds.getConnection();
        HashMap glns = new HashMap();
        Statement st = cn.createStatement();
        String sql = "SELECT  * FROM entregasWallMart";
        try {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                EntregasWallMart entregasWallMart = new EntregasWallMart();
                entregasWallMart.setIdGln(rs.getString("idGln"));
                entregasWallMart.setIdTienda(rs.getString("idTienda"));
                String cIdGln = entregasWallMart.getIdGln().substring(8, 13);
                String cIdTienda = entregasWallMart.getIdTienda();
                glns.put(cIdGln, cIdTienda);
            }
        } finally {
            cn.close();
        }
        return glns;
    }
}
