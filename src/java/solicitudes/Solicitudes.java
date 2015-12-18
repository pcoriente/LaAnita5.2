package solicitudes;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import traspasos.to.TOTraspaso;

/**
 *
 * @author jesc
 */
public class Solicitudes {

    public static void liberarSolicitud(Connection cn, int idSolicitud, int idUsuario) throws SQLException {
        String strSQL = "SELECT propietario FROM solicitudes WHERE idSolicitud=" + idSolicitud;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                if (rs.getInt("propietario") == idUsuario) {
                    strSQL = "UPDATE solicitudes SET propietario=0 WHERE idSolicitud=" + idSolicitud;
                    st.executeUpdate(strSQL);
                }
            }
        }
    }

    public static void bloquearSolicitud(Connection cn, TOTraspaso toTraspaso, int idUsuario, int estatus) throws SQLException {
        int propietario = 0;
        String strSQL = "SELECT propietario, estatus FROM solicitudes WHERE idSolicitud=" + toTraspaso.getReferencia();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                toTraspaso.setEstatus(rs.getInt("estatus"));
                propietario = rs.getInt("propietario");
                if (toTraspaso.getEstatus() == estatus && propietario == 0) {
                    strSQL = "UPDATE solicitudes SET propietario=" + idUsuario + "\n"
                            + "WHERE idSolicitud=" + toTraspaso.getReferencia();
                    st.executeUpdate(strSQL);
                    toTraspaso.setPropietario(idUsuario);
                } else {
                    toTraspaso.setPropietario(propietario);
                }
                toTraspaso.setIdUsuario(idUsuario);
            } else {
                throw new SQLException("La solicitud ya no existe !!!");
            }
        }
    }
}
