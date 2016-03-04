package tiendas;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import tiendas.to.TOTienda;

/**
 *
 * @author jesc
 */
public class Tiendas {
    
    public static TOTienda validaTienda(Connection cn, int codigoTienda) throws SQLException {
        TOTienda toTienda = null;
        String strSQL = Tiendas.sqlTienda() + "\n"
                + "WHERE TC.codigoTienda=" + codigoTienda;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if(rs.next()) {
                toTienda = construir(rs);
            }
        }
        return toTienda;
    }
    
    public static TOTienda obtenerTienda(Connection cn, int idTienda) throws SQLException {
        TOTienda toTienda = null;
        String strSQL = Tiendas.sqlTienda() + "\n"
                + "WHERE T.idTienda=" + idTienda;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if(rs.next()) {
                toTienda = construir(rs);
            }
        }
        return toTienda;
    }
    
    public static TOTienda construir(ResultSet rs) throws SQLException {
        TOTienda to = new TOTienda();
        to.setIdTienda(rs.getInt("idTienda"));
        to.setTienda(rs.getString("tienda"));
        to.setIdDireccion(rs.getInt("idDireccion"));
        to.setIdCliente(rs.getInt("idCliente"));
        to.setContribuyente(rs.getString("contribuyente"));
        to.setIdFormato(rs.getInt("idFormato"));
        to.setIdAgente(rs.getInt("idAgente"));
        to.setIdRuta(rs.getInt("idRuta"));
        to.setIdImpuestoZona(rs.getInt("idImpuestoZona"));
        to.setCodigoTienda(rs.getInt("codigoCliente"));
        to.setEstado(rs.getInt("estado"));
        return to;
    }
    
    public static String sqlTienda() {
        return "SELECT T.*, Y.contribuyente, ISNULL(TC.codigoTienda, 0) AS codigoTienda\n"
                + "FROM clientesTiendas T\n"
                + "INNER JOIN clientes C ON C.idCliente=T.idCliente\n"
                + "INNER JOIN contribuyentes Y ON Y.idContribuyente=C.idContribuyente\n"
                + "LEFT JOIN clientesTiendasCodigos TC ON TC.idTienda=T.idTienda";
    }
    
}
