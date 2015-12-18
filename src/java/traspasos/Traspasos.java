package traspasos;

import traspasos.dominio.Traspaso;
import traspasos.to.TOTraspaso;

/**
 *
 * @author jesc
 */
public class Traspasos {
    
    public static void convertir(TOTraspaso toMov, Traspaso mov) {
        mov.setSolicitudFolio(toMov.getSolicitudFolio());
        mov.setSolicitudFecha(toMov.getSolicitudFecha());
        mov.setSolicitudIdUsuario(toMov.getSolicitudIdUsuario());
        mov.setSolicitudEstatus(toMov.getSolicitudEstatus());
        movimientos.Movimientos.convertir(toMov, mov);
        mov.setIdSolicitud(toMov.getReferencia());
    }
    
    public static TOTraspaso convertir(Traspaso traspaso) {
        TOTraspaso to = new TOTraspaso();
        to.setSolicitudFolio(traspaso.getSolicitudFolio());
        to.setSolicitudFecha(traspaso.getSolicitudFecha());
        to.setSolicitudIdUsuario(traspaso.getSolicitudIdUsuario());
        to.setSolicitudProietario(traspaso.getSolicitudProietario());
        to.setSolicitudEstatus(traspaso.getSolicitudEstatus());
        movimientos.Movimientos.convertir(traspaso, to);
        to.setIdReferencia(traspaso.getAlmacenDestino().getIdAlmacen());
        to.setReferencia(traspaso.getIdSolicitud());
        return to;
    }
}
