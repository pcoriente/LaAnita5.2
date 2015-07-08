package pedidos.DAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    public void eliminarPedido(int idPedidoOC, int idPedido) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "DELETE FROM pedidosOC WHERE idPedidoOC=" + idPedidoOC;
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM pedidosOCTienda WHERE idPedido=" + idPedido;
                st.executeUpdate(strSQL);

                strSQL = "DELETE FROM pedidosOCTiendaDetalle WHERE idPedido=" + idPedido;
                st.executeUpdate(strSQL);

                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw (e);
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void cerrarPedido(int idPedido) throws SQLException {
        String strSQL = "UPDATE pedidosOCTienda SET estatus=1 WHERE idPedido=" + idPedido;
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);
            }
        }
    }

    public boolean liberarPedido(int idPedido) throws SQLException, Exception {
        boolean liberado = true;
        String strSQL = "SELECT propietario FROM pedidosOCTienda WHERE idPedido=" + idPedido;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    int propietario = rs.getInt("propietario");
                    if (propietario == this.idUsuario) {
                        strSQL = "UPDATE pedidosOCTienda SET propietario=0 WHERE idPedido=" + idPedido;
                        st.executeUpdate(strSQL);
                    }
                } else {
                    throw new Exception("No se encontro el pedido !!!");
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } catch (Exception ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
            return liberado;
        }
    }

    public boolean asegurarPedido(int idPedido) throws SQLException, Exception {
        boolean asegurado = true;
        String strSQL = "SELECT propietario FROM pedidosOCTienda WHERE idPedido=" + idPedido;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    int propietario = rs.getInt("propietario");
                    if (propietario == 0) {
                        strSQL = "UPDATE pedidosOCTienda SET propietario=" + this.idUsuario + " WHERE idPedido=" + idPedido;
                        st.executeUpdate(strSQL);
                    } else if (propietario != this.idUsuario) {
                        asegurado = false;
                        strSQL = "SELECT * FROM webSystem.dbo.usuarios WHERE idUsuario=" + propietario;
                        rs = st.executeQuery(strSQL);
                        if (rs.next()) {
                            strSQL = rs.getString("usuario");
                         } else {
                            strSQL = "";
                        }
                        throw new Exception("No se puede asegurar el movimiento, lo tiene el usuario(id=" + propietario + "): " + strSQL + " !!!");
                    }
                } else {
                    throw new Exception("No se encontro el pedido !!!");
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } catch (Exception ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return asegurado;
    }
    
    public void agregarPedido(TOPedido to) throws SQLException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String strSQL = "INSERT INTO pedidosOC (idAlmacen, idCliente, fecha, ordenDeCompra, ordenDeCompraFecha, embarqueFecha, entregaFolio, entregaFecha, cancelado, canceladoMotivo, canceladoFecha)\n" +
                        "VALUES (" + to.getIdAlmacen() + ", "+to.getIdCliente() + ", GETDATE(), '" + to.getOrdenDeCompra() + "', '" + format.format(to.getOrdenDeCompraFecha()) + "', '1900-01-01', '', '1900-01-01', 0, '', '1900-01-01')";
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(strSQL);
                
                int idPedidoOC = 0;
                ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idPedidoOC");
                if (rs.next()) {
                    idPedidoOC = rs.getInt("idPedidoOC");
                }
                int idPedido=0;
                strSQL= "INSERT INTO pedidosOCTienda (idPedidoOC, idTienda, idMovto, idMovtoAlmacen, propietario, estatus)\n" +
                        "VALUES ("+idPedidoOC+", "+to.getIdTienda()+", 0, 0, 0, 0)";
                st.executeUpdate(strSQL);
                rs = st.executeQuery("SELECT @@IDENTITY AS idPedido");
                if (rs.next()) {
                    idPedido = rs.getInt("idPedido");
                }
                cn.commit();
                to.setIdPedido(idPedido);
                to.setIdPedidoOC(idPedidoOC);
            } catch (SQLException e) {
                cn.rollback();
                throw (e);
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private TOPedido construirPedido(ResultSet rs) throws SQLException {
        TOPedido to = new TOPedido();
        to.setIdPedido(rs.getInt("idPedido"));
        to.setIdPedido(rs.getInt("idPedidoOC"));
        to.setIdAlmacen(rs.getInt("idAlmacen"));
        to.setIdCliente(rs.getInt("idCliente"));
        to.setIdTienda(rs.getInt("idTienda"));
        to.setFecha(new java.util.Date(rs.getDate("fecha").getTime()));
        to.setEstatus(rs.getInt("estatus"));
        to.setOrdenDeCompra(rs.getString("ordenDeCompra"));
        to.setOrdenDeCompraFecha(new java.util.Date(rs.getDate("ordenDeCompraFecha").getTime()));
        to.setCanceladoFecha(new java.util.Date(rs.getDate("canceladoFecha").getTime()));
        to.setCanceladoMotivo(rs.getString("canceladoMotivo"));
        return to;
    }

    public ArrayList<TOPedido> obtenerPedidos(int idAlmacen, int estatus, Date fechaInicial) throws SQLException {
        String condicion = "=";
        if (estatus != 0) {
            condicion = "!=";
        }
        if (fechaInicial == null) {
            fechaInicial = new Date();
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<TOPedido> pedidos = new ArrayList<>();
        String strSQL = "SELECT T.idPedido, T.idTienda, T.propietario, T.estatus, P.*\n"
                + "FROM pedidosOCTienda T\n"
                + "INNER JOIN pedidosOC P ON P.idPedidoOC=T.idPedidoOC\n"
                + "WHERE P.idAlmacen=" + idAlmacen + " AND CONVERT(date, P.fecha) <= '" + format.format(fechaInicial) + "' AND T.estatus" + condicion + "0\n"
                + "ORDER BY T.idPedido, T.idTienda";
        try (Connection cn = this.ds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                ResultSet rs = st.executeQuery(strSQL);
                while (rs.next()) {
                    pedidos.add(this.construirPedido(rs));
                }
            }
        }
        return pedidos;
    }
}
