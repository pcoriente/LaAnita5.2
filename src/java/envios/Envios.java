package envios;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import utilerias.Utilerias;

/**
 *
 * @author jesc
 */
public class Envios {

    public static ArrayList<Double> obtenerEnvioPeso(Connection cn, int idEnvio) throws SQLException {
        ArrayList<Double> pesos = new ArrayList<>();
        String strSQL = "SELECT SUM(SD.cantSolicitada*E.peso/E.piezas) AS peso, SUM(ESD.directos*E.peso) AS pesoDirectos\n"
                + "FROM enviosSolicitudesDetalle ESD\n"
                + "INNER JOIN solicitudesDetalle SD ON SD.idSolicitud=ESD.idSolicitud AND SD.idEmpaque=ESD.idEmpaque\n"
                + "INNER JOIN empaques E ON E.idEmpaque=SD.idEmpaque\n"
                + "INNER JOIN enviosSolicitudes ES ON ES.idSolicitud=SD.idSolicitud\n"
                + "INNER JOIN envios N ON N.idEnvio=ES.idEnvio\n"
                + "WHERE N.idEnvio=" + idEnvio;

        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            rs.next();
            pesos.add(rs.getDouble("peso"));
            pesos.add(rs.getDouble("pesoDirectos"));
        }
        return pesos;
    }

    public static int obtenerEstatusEnvioTraspaso(Connection cn, int idEnvio, int idAlmacenDestino) throws SQLException {
        int estatus = 0;
        String strSQL = "SELECT S.estatus\n"
                + "FROM enviosSolicitudes ES\n"
                + "INNER JOIN envios E ON E.idEnvio=ES.idEnvio\n"
                + "INNER JOIN solicitudes S ON S.idSolicitud=ES.idSolicitud\n"
                + "WHERE E.idEnvio=" + idEnvio + " AND S.idAlmacen=" + idAlmacenDestino;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                estatus = rs.getInt("estatus");
            }
        }
        return estatus;
    }

    public static int obtenerIdMovtoRecepcion(Connection cn, int idSolicitud) throws SQLException, Exception {
        int estatus = 0;
        int idAlmacen = 0;
        int idMovtoRecepcion = 0;
        String strSQL = "SELECT idAlmacenOrigen FROM solicitudes WHERE idSolicitud=" + idSolicitud;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                idAlmacen = rs.getInt("idAlmacenOrigen");
            } else {
                throw new SQLException("No se encontró la solicitud !!!");
            }
            strSQL = "SELECT M.idMovto, M.idReferencia, M.estatus FROM movimientos M\n"
                    + "INNER JOIN solicitudes S ON S.idSolicitud=M.referencia\n"
                    + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=35 AND S.idSolicitud=" + idSolicitud;
            rs = st.executeQuery(strSQL);
            if (rs.next()) {
                estatus = rs.getInt("estatus");
                if (estatus == 7) {
                    strSQL = "SELECT idMovto, estatus FROM movimientos\n"
                            + "WHERE idAlmacen=" + rs.getInt("idReferencia") + " AND idTipo=9 AND referencia=" + rs.getInt("idMovto");
                    rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        if(rs.getInt("estatus")!=7) {
                            throw new Exception("La recepción no se ha cerrado");
                        }
                        idMovtoRecepcion = rs.getInt("idMovto");
                    } else {
                        throw new Exception("No se encontró la recepción !!!");
                    }
                } else if(estatus == 5) {
                    throw new Exception("El traspaso no se ha cerrado !!!");
                } else {
                    throw new Exception("Checar estatus del traspaso !!!");
                }
            } else {
                throw new SQLException("No se ha aceptado la solicitud !!!");
            }
        }
        return idMovtoRecepcion;
    }
    
    public static void obtenerEstatusTraspaso(Connection cn, int idSolicitud) throws SQLException, Exception {
        int estatus = 0;
        int idAlmacen = 0;
        String strSQL = "SELECT idAlmacenOrigen FROM solicitudes WHERE idSolicitud=" + idSolicitud;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                idAlmacen = rs.getInt("idAlmacenOrigen");
            } else {
                throw new Exception("No se encontró la solicitud !!!");
            }
            strSQL = "SELECT M.idMovto, M.idReferencia, M.estatus\n"
                    + "FROM movimientos M INNER JOIN solicitudes S ON S.idSolicitud=M.referencia\n"
                    + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=35 AND S.idSolicitud=" + idSolicitud;
            rs = st.executeQuery(strSQL);
            if (rs.next()) {
                estatus = rs.getInt("estatus");
                if(estatus != 7) {
                    throw new Exception("El traspaso no se ha completado (estatus = " + estatus + ") !!!");
                } else {
                    strSQL = "SELECT estatus FROM movimientos\n"
                            + "WHERE idAlmacen=" + rs.getInt("idReferencia") + " AND idTipo=9 AND referencia=" + rs.getInt("idMovto");
                    rs = st.executeQuery(strSQL);
                    if(rs.next()) {
                        estatus = rs.getInt("estatus");
                        if(estatus != 7) {
                            throw new Exception("La recepción no se ha completado (estatus = " + estatus + ") !!!");
                        }
                    } else {
                        throw new SQLException("No se encontró la recepción !!!");
                    }
                }
            } else {
                throw new Exception("No se ha aceptado la solicitud !!!");
            }
        }
    }

    public static int obtenerEstatusRecepcion(Connection cn, int idMovtoRecepcion) throws SQLException {
        int estatus = 0;
        String strSQL = "SELECT estatus FROM movimientos WHERE idMovto=" + idMovtoRecepcion;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                estatus = rs.getInt("estatus");
            }
        }
        return estatus;
    }

    public static int obtenerEstatusTraspasoDirecto(Connection cn, int idSolicitud) throws SQLException {
        int estatus = 0;
        int idAlmacen = 0;
        String strSQL = "SELECT idAlmacenOrigen FROM solicitudes WHERE idSolicitud=" + idSolicitud;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                idAlmacen = rs.getInt("idAlmacenOrigen");
            } else {
                throw new SQLException("No se encontro la solicitud !!!");
            }
            strSQL = "SELECT M.idMovto, M.idReferencia, M.estatus FROM movimientos M\n"
                    + "INNER JOIN solicitudes S ON S.idSolicitud=M.referencia\n"
                    + "WHERE M.idAlmacen=" + idAlmacen + " AND M.idTipo=35 AND S.idSolicitud=" + idSolicitud;
            rs = st.executeQuery(strSQL);
            if (rs.next()) {
                estatus = rs.getInt("estatus");
                if (estatus == 0 || estatus == 5) {
                    estatus = 1; // El traspaso no se ha realizado
                } else {
                    strSQL = "SELECT estatus FROM movimientos\n"
                            + "WHERE idAlmacen=" + rs.getInt("idReferencia") + " AND idTipo=9 AND referencia=" + rs.getInt("idMovto");
                    rs = st.executeQuery(strSQL);
                    if (rs.next()) {
                        estatus = rs.getInt("estatus");
                        if (estatus == 0 || estatus == 5) {
                            estatus = 2; // No se ha realizado la recepcion
                        } else {
                            estatus = 3; // Recepcion lista
                        }
                    } else {
                        throw new SQLException("No se encontró la recepción !!!");
                    }
                }
            } else {
                estatus = 1;
            }
        }
        return estatus;
    }

    public static Date obtenerFechaProduccion(Connection cn, int idSolicitud, boolean envio) throws SQLException {
        Date fechaProduccion = Utilerias.fechaInicial();
        String strSQL = "SELECT fechaProduccion FROM enviosSolicitudes WHERE idSolicitud=" + idSolicitud;
        if (!envio) {
            strSQL = "SELECT ES.fechaProduccion FROM solicitudes S\n"
                    + "INNER JOIN enviosPedidos EP ON EP.idSolicitud=S.idSolicitud\n"
                    + "INNER JOIN enviosSolicitudes ES ON ES.idEnvio=EP.idEnvio\n"
                    + "INNER JOIN solicitudes S1 ON S1.idSolicitud=ES.idSolicitud\n"
                    + "WHERE S.idSolicitud=" + idSolicitud + " AND S1.idAlmacen=S.idAlmacen";
        }
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                fechaProduccion = new Date(rs.getDate("fechaProduccion").getTime());
            }
        }
        return fechaProduccion;
    }
}
