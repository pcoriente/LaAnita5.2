package envios;

import envios.to.TOEnvioProducto;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author jesc
 */
public class Envios {
    
    public static void grabarSolicitada(Connection cn, TOEnvioProducto toProd) throws SQLException {
        String strSQL = "UPDATE solicitudesDetalle\n"
                + "SET cantSolicitada=" + toProd.getCantSolicitada() + "\n"
                + "WHERE idSolicitud=" + toProd.getIdSolicitud() + " AND idEmpaque=" + toProd.getIdProducto();
        try (Statement st = cn.createStatement()) {
            st.executeUpdate(strSQL);
        }
    }
    
}
