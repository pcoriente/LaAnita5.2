package solicitudes;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import solicitudes.dominio.Solicitud;
import solicitudes.to.TOSolicitud;
import solicitudes.to.TOSolicitudProducto;
import traspasos.to.TOTraspaso;

/**
 *
 * @author jesc
 */
public class Solicitudes {
    
    public static void agregarProducto(Connection cn, TOSolicitudProducto to) throws SQLException {
        try (Statement st = cn.createStatement()) {
            String strSQL = "INSERT INTO solicitudesDetalle (idSolicitud, idEmpaque, cantSolicitada)\n"
                    + "VALUES (" + to.getIdSolicitud() + ", " + to.getIdProducto() + ", " + to.getCantSolicitada() + ")";
            st.executeUpdate(strSQL);
        }
    }
    
    public static int agregarSolicitudDirecto(Connection cn, int idAlmacen, int idAlmacenOrigen, int idUsuario) throws SQLException {
        TOSolicitud toSolicitud = new TOSolicitud();
        toSolicitud.setDirecto(1);
        toSolicitud.setIdAlmacen(idAlmacen);
        toSolicitud.setIdAlmacenOrigen(idAlmacenOrigen);
        toSolicitud.setIdUsuario(idUsuario);
        Solicitudes.agrega(cn, toSolicitud);
        Solicitudes.grabar(cn, toSolicitud);
        return toSolicitud.getIdSolicitud();
    }

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
                if (propietario == 0) {
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
    
    public static void grabar(Connection cn, TOSolicitud to) throws SQLException {
        try (Statement st = cn.createStatement()) {
            to.setEstatus(1);
            to.setFolio(movimientos.Movimientos.obtenMovimientoFolioOficina(cn, to.getIdAlmacen(), 53));

            String strSQL = "UPDATE solicitudes\n"
                    + "SET folio=" + to.getFolio() + ", fecha=GETDATE(), idUsuario=" + to.getIdUsuario() + ", propietario=" + to.getPropietario() + ", estatus=" + to.getEstatus() + "\n"
                    + "WHERE idSolicitud=" + to.getIdSolicitud();
            st.executeUpdate(strSQL);

            strSQL = "SELECT fecha FROM solicitudes WHERE idSolicitud=" + to.getIdSolicitud();
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                to.setFecha(new java.util.Date(rs.getDate("fecha").getTime()));
            }
            strSQL = "DELETE FROM solicitudesDetalle WHERE idSolicitud=" + to.getIdSolicitud() + " AND cantSolicitada=0";
            st.executeUpdate(strSQL);
        }
    }

    public static void convertir(Solicitud solicitud, TOSolicitud toSolicitud) {
        toSolicitud.setIdSolicitud(solicitud.getIdSolicitud());
        toSolicitud.setIdAlmacen(solicitud.getAlmacen().getIdAlmacen());
        toSolicitud.setFolio(solicitud.getFolio());
        toSolicitud.setFecha(solicitud.getFecha());
        toSolicitud.setIdUsuario(solicitud.getIdUsuario());
        toSolicitud.setIdAlmacenOrigen(solicitud.getAlmacenOrigen().getIdAlmacen());
        toSolicitud.setIdUsuarioOrigen(solicitud.getIdUsuarioOrigen());
        toSolicitud.setPropietario(solicitud.getPropietario());
        toSolicitud.setEstatus(solicitud.getEstatus());
        toSolicitud.setEnvio(solicitud.isEnvio()?1:0);
        toSolicitud.setDirecto(solicitud.isDirecto()?1:0);
    }

    public static void convertir(TOSolicitud toSolicitud, Solicitud solicitud) {
        solicitud.setIdSolicitud(toSolicitud.getIdSolicitud());
        solicitud.setFolio(toSolicitud.getFolio());
        solicitud.setFecha(toSolicitud.getFecha());
        solicitud.setIdUsuarioOrigen(toSolicitud.getIdUsuarioOrigen());
        solicitud.setIdUsuario(toSolicitud.getIdUsuario());
        solicitud.setPropietario(toSolicitud.getPropietario());
        solicitud.setEstatus(toSolicitud.getEstatus());
        solicitud.setEnvio(toSolicitud.getEnvio()!=0);
        solicitud.setDirecto(toSolicitud.getDirecto()!=0);
    }

    public static void construir(TOSolicitud toSolicitud, ResultSet rs) throws SQLException {
        toSolicitud.setIdSolicitud(rs.getInt("idSolicitud"));
        toSolicitud.setIdAlmacen(rs.getInt("idAlmacen"));
        toSolicitud.setFolio(rs.getInt("folio"));
        toSolicitud.setFecha(new java.util.Date(rs.getTimestamp("fecha").getTime()));
        toSolicitud.setIdAlmacenOrigen(rs.getInt("idAlmacenOrigen"));
        toSolicitud.setIdUsuarioOrigen(rs.getInt("idUsuarioOrigen"));
        toSolicitud.setIdUsuario(rs.getInt("idUsuario"));
        toSolicitud.setPropietario(rs.getInt("propietario"));
        toSolicitud.setEstatus(rs.getInt("estatus"));
        toSolicitud.setEnvio(rs.getInt("envio"));
        toSolicitud.setDirecto(rs.getInt("directo"));
    }

    public static void agrega(Connection cn, TOSolicitud toSolicitud) throws SQLException {
        String strSQL;
        try (Statement st = cn.createStatement()) {
            strSQL = "INSERT INTO solicitudes (idAlmacen, folio, fecha, idAlmacenOrigen, idUsuarioOrigen, idUsuario, propietario, estatus, envio, directo)\n"
                    + "VALUES (" + toSolicitud.getIdAlmacen() + ", 0, GETDATE(), " + toSolicitud.getIdAlmacenOrigen() + ", 0, " + toSolicitud.getIdUsuario() + ", " + toSolicitud.getPropietario() + ", " + toSolicitud.getEstatus() + ", " + toSolicitud.getEnvio() + ", " + toSolicitud.getDirecto() + ")";
            st.executeUpdate(strSQL);

            ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idSolicitud");
            if (rs.next()) {
                toSolicitud.setIdSolicitud(rs.getInt("idSolicitud"));
            }
            strSQL = "SELECT fecha FROM solicitudes WHERE idSolicitud=" + toSolicitud.getIdSolicitud();
            rs = st.executeQuery(strSQL);
            if (rs.next()) {
                toSolicitud.setFecha(new java.util.Date(rs.getTimestamp("fecha").getTime()));
            }
        }
    }
}
